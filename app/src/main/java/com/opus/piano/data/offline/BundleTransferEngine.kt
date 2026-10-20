package com.opus.piano.data.offline

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Typed transfer failures. The runner maps each one to a **legal** state-machine transition,
 * so an error can never wedge a bundle in an unservable state.
 */
sealed class BundleTransferError(message: String) : Exception(message) {
    /**
     * Policy says stop now ([code] is a [BundlePolicy.Reasons] value). Already-installed
     * content is untouched; only the current partial is dropped (files always restart —
     * no validated HTTP range support, see OFFLINE-CACHE.md).
     */
    class Paused(val code: String, message: String) : BundleTransferError(message)

    /** Storage ceiling/reserve refused the write: retryable once space is available. */
    class Storage(message: String) : BundleTransferError(message)

    /** Bytes did not match the declared size/sha256: never publish, restart the file later. */
    class Integrity(message: String) : BundleTransferError(message)

    /** Rights/manifest validation failure: QUARANTINED, never silently READY. */
    class Rights(message: String) : BundleTransferError(message)

    /** Permanent source problem (missing/disallowed URL, hard 4xx): not retried automatically. */
    class Source(message: String) : BundleTransferError(message)

    /** Transient transport problem (5xx/429/timeout/IO): retried with WorkManager backoff. */
    class Transport(message: String) : BundleTransferError(message)
}

/**
 * Streaming transfer engine for one bundle file.
 *
 * Guarantees (D7 / OFFLINE-CACHE.md):
 *  - policy ([BundleNetwork] + per-job allowance) is re-evaluated before the file **and**
 *    during streaming every [gateEveryBytes], so consent revocation, Wi-Fi to mobile-data
 *    transitions and Data Saver stop the transfer within one gate window
 *  - hard byte cap: the manifest's declared size is the maximum ever written, which bounds
 *    "unknown size" exposure even if a source misbehaves
 *  - staging is disposable and content-addressed (`<sha256>.part`); a partial is never
 *    mistaken for complete content and is deleted on any stop, so a later attempt restarts
 *    the bounded file cleanly
 *  - hashes are verified here before the file may enter the blob store
 *
 * Pure JVM-testable: `connect` is injectable (same pattern as HttpsProviderTransport), and
 * no Android API is touched.
 */
class BundleTransferEngine(
    private val stagingDir: File,
    private val network: BundleNetwork,
    private val meteredAllowed: () -> Boolean,
    private val admission: (Long) -> Boolean,
    private val connect: (URL) -> HttpURLConnection = { it.openConnection() as HttpURLConnection },
    private val onProgress: suspend (writtenBytes: Long) -> Unit = {},
    private val gateEveryBytes: Long = GATE_EVERY_BYTES
) {

    /**
     * Downloads [file] into staging and returns the verified staged file (sha256 checked).
     * Throws [BundleTransferError] subclasses; never writes outside [stagingDir].
     */
    suspend fun fetch(file: BundleFile): File = withContext(Dispatchers.IO) {
        // Content rights are revalidated on every attempt, not trusted from an earlier run.
        try {
            BundleManifestCodec.validateFile(file)
        } catch (e: BundleTransferError) {
            throw e
        } catch (e: Exception) {
            throw BundleTransferError.Rights("invalid bundle file metadata: ${e.message}")
        }
        val source = file.rights.sourceUrl
            ?: throw BundleTransferError.Source("missing source url for ${file.relativePath}")
        val url = requireSourceUrl(source)
        gate("file_start")
        if (!admission(file.bytes)) {
            throw BundleTransferError.Storage("storage admission refused for ${file.bytes} bytes")
        }

        stagingDir.mkdirs()
        val staged = File(stagingDir, file.sha256 + BundleStore.STAGED_SUFFIX)
        // No validated Range/ETag support: restart this bounded file rather than resume blind.
        if (staged.exists() && !staged.delete()) {
            throw BundleTransferError.Storage("cannot reset staged file for ${file.relativePath}")
        }

        var completed = false
        try {
            val connection = connect(url)
            val cancellation = CoroutineScope(currentCoroutineContext()).launch(
                Dispatchers.IO, start = CoroutineStart.UNDISPATCHED
            ) {
                try {
                    awaitCancellation()
                } finally {
                    runCatching { connection.disconnect() }
                }
            }
            try {
                connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
                connection.readTimeout = READ_TIMEOUT_MILLIS
                connection.instanceFollowRedirects = false
                connection.useCaches = false
                connection.setRequestProperty("User-Agent", USER_AGENT)
                connection.setRequestProperty("Accept-Encoding", "identity")

                val status = try {
                    connection.responseCode
                } catch (e: Exception) {
                    throw BundleTransferError.Transport("connect failed: ${e.javaClass.simpleName}")
                }
                when {
                    status == 200 -> Unit
                    status == 408 || status == 429 || status >= 500 ->
                        throw BundleTransferError.Transport("http_$status")
                    else -> throw BundleTransferError.Source("http_$status")
                }
                val declared = connection.contentLengthLong
                if (declared >= 0 && declared > file.bytes) {
                    throw BundleTransferError.Integrity(
                        "source returned $declared bytes but manifest declares ${file.bytes}"
                    )
                }

                var written = 0L
                var sinceGate = 0L
                connection.inputStream.use { input ->
                    FileOutputStream(staged).use { output ->
                        val buffer = ByteArray(READ_BUFFER_BYTES)
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            val read = input.read(buffer)
                            if (read < 0) break
                            if (written + read > file.bytes) {
                                throw BundleTransferError.Integrity(
                                    "response exceeds declared ${file.bytes} bytes"
                                )
                            }
                            output.write(buffer, 0, read)
                            written += read
                            sinceGate += read
                            if (sinceGate >= gateEveryBytes) {
                                sinceGate = 0
                                gate("streaming")
                                onProgress(written)
                            }
                        }
                        output.flush()
                    }
                }
                if (written != file.bytes) {
                    throw BundleTransferError.Integrity("length mismatch: $written != ${file.bytes}")
                }
                gate("complete")
                onProgress(written)
                val actual = BundleStoreLogic.sha256Of(staged)
                if (actual != file.sha256) {
                    throw BundleTransferError.Integrity(
                        "sha256 mismatch for ${file.relativePath}"
                    )
                }
                completed = true
                staged
            } finally {
                cancellation.cancel()
            }
        } catch (e: CancellationException) {
            if (!completed) staged.delete()
            throw e
        } catch (e: Exception) {
            if (!completed) staged.delete()
            // A stopped worker must surface as cancellation, never as a transfer failure.
            if (!currentCoroutineContext().isActive) {
                throw CancellationException("transfer cancelled")
            }
            throw translate(e)
        }
    }

    /** Policy gate shared with the runner; stops within one gate window mid-stream. */
    private fun gate(where: String) {
        val reason = BundlePolicy.Reasons.stopReason(network.facts(), meteredAllowed()) ?: return
        throw BundleTransferError.Paused(reason, "$reason during $where")
    }

    private fun translate(e: Exception): Exception = when (e) {
        is BundleTransferError -> e
        is java.io.IOException ->
            if (e.message?.contains("space", ignoreCase = true) == true) {
                BundleTransferError.Storage("staging write failed: ${e.message}")
            } else {
                BundleTransferError.Transport("io: ${e.javaClass.simpleName}")
            }
        is java.util.concurrent.TimeoutException -> BundleTransferError.Transport("timeout")
        else -> e
    }

    companion object {
        const val GATE_EVERY_BYTES = 64L * 1024
        const val READ_BUFFER_BYTES = 64 * 1024
        const val CONNECT_TIMEOUT_MILLIS = 10_000
        const val READ_TIMEOUT_MILLIS = 20_000
        const val USER_AGENT = "Goldberg/0.11-stage5 (https://github.com/stokonn/Goldberg)"

        /**
         * https-only, fixed-port, no-credentials, provider allowlist. Bundle bytes may only
         * come from the same hosts the catalogue providers are allowed to talk to — this is
         * both a rights control (no arbitrary hotlinking) and an SSRF control.
         */
        val ALLOWED_HOSTS = setOf(
            "api.openopus.org",
            "musicbrainz.org",
            "www.wikidata.org",
            "en.wikipedia.org",
            "cs.wikipedia.org",
            "commons.wikimedia.org",
            "upload.wikimedia.org",
            "thumb.wikimedia.org"
        )

        fun requireSourceUrl(source: String): URL {
            val url = try {
                URL(source)
            } catch (_: Exception) {
                throw BundleTransferError.Source("unparseable source url")
            }
            val approved = url.protocol.equals("https", ignoreCase = true) &&
                url.port in setOf(-1, 443) &&
                url.userInfo == null &&
                url.host.lowercase(java.util.Locale.ROOT) in ALLOWED_HOSTS
            if (!approved) {
                throw BundleTransferError.Source("source '${url.host}' is not an approved https provider")
            }
            return url
        }
    }
}
