package com.opus.piano.admin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Production [AdminTransport] over HttpURLConnection, modelled on the audited
 * `HttpsProviderTransport` (ProviderTransport.kt): https-only, no redirects,
 * bounded body, cancellation closes the socket. Kept free of any admin-specific
 * logic so tests can inject a fake transport instead.
 */
class HttpsAdminTransport(
    private val connect: (URL) -> HttpURLConnection = { it.openConnection() as HttpURLConnection },
) : AdminTransport {

    override suspend fun call(
        method: String,
        url: String,
        headers: Map<String, String>,
        body: ByteArray?,
    ): AdminHttpResponse = withContext(Dispatchers.IO) {
        val parsed = URL(url)
        require(parsed.protocol == "https" && parsed.port in setOf(-1, 443) && parsed.userInfo == null) {
            "Admin endpoint must be https on the default port"
        }
        require(body == null || body.size <= AdminApi.MAX_BODY_BYTES) { "Admin request body too large" }
        val connection = connect(parsed)
        // Cancellation closes a blocking socket without waiting for readTimeout.
        val cancellation = CoroutineScope(currentCoroutineContext())
            .launch(Dispatchers.IO, start = CoroutineStart.UNDISPATCHED) {
                try { awaitCancellation() } finally { connection.disconnect() }
            }
        try {
            connection.requestMethod = method
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.instanceFollowRedirects = false
            headers.forEach { (name, value) ->
                require(value.length <= 1024 && !value.contains('\r') && !value.contains('\n')) {
                    "Invalid header value"
                }
                connection.setRequestProperty(name, value)
            }
            if (body != null) {
                connection.doOutput = true
                connection.setFixedLengthStreamingMode(body.size)
                connection.outputStream.use { it.write(body) }
            }

            val code = connection.responseCode
            val retryAfter = connection.getHeaderField("Retry-After")?.toLongOrNull()
            val declared = connection.contentLengthLong
            require(declared <= AdminApi.MAX_BODY_BYTES) { "Admin response exceeds bound" }

            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val out = ByteArrayOutputStream()
            stream?.use { input ->
                val buffer = ByteArray(8192)
                while (true) {
                    currentCoroutineContext().ensureActive()
                    val n = input.read(buffer)
                    if (n < 0) break
                    require(out.size() + n <= AdminApi.MAX_BODY_BYTES) { "Admin response exceeds bound" }
                    out.write(buffer, 0, n)
                }
            }
            AdminHttpResponse(code, out.toByteArray(), retryAfter)
        } finally {
            cancellation.cancel()
            connection.disconnect()
        }
    }
}
