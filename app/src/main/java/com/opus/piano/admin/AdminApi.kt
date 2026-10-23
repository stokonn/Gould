package com.opus.piano.admin

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Single contract-shaped admin HTTP client (Stage 7B.2).
 *
 * Contract source: docs/planning/goldberg-0.11/stage-7/stage-7b/CONTRACT-AUDIT.md —
 * only verified C6 endpoints are callable. No endpoint, query parameter or body field
 * is invented; missing Stage 6 capabilities (G1 stats, G2 queue search, G3 audit
 * server search) are deliberately ABSENT here and surfaced honestly by the UI.
 *
 * Security posture:
 *  - Base URL is configurable and EMPTY by default -> every call fails with
 *    [AdminFailure.NotConfigured] (honest "service not configured" state, never a
 *    fabricated response).
 *  - https-only, default port only, no userinfo, no redirects (credentials/cookies
 *    never follow a redirect to another host).
 *  - Bodies are bounded ([MAX_BODY_BYTES]); over-limit responses are a failure, not
 *    a truncated success.
 *  - Transport is injectable (mirrors `bundleConnectOverride`) for hermetic tests.
 */

/** Raw response of one HTTP call. Body is bounded and fully read. */
data class AdminHttpResponse(val code: Int, val body: ByteArray, val retryAfterSeconds: Long?) {
    override fun equals(other: Any?): Boolean =
        other is AdminHttpResponse && code == other.code && body.contentEquals(other.body) &&
            retryAfterSeconds == other.retryAfterSeconds
    override fun hashCode(): Int = 31 * code + body.contentHashCode()
}

/** Transport seam. Implementations must honour https-only and bounds; the client re-validates. */
fun interface AdminTransport {
    suspend fun call(method: String, url: String, headers: Map<String, String>, body: ByteArray?): AdminHttpResponse
}

/** Typed failure taxonomy — one entry per verified contract behaviour plus transport states. */
sealed class AdminFailure {
    /** Base URL unset/blank: the admin service is not configured on this install. */
    data object NotConfigured : AdminFailure()
    /** Base URL present but violates https-only/port/userinfo policy. */
    data object InvalidEndpoint : AdminFailure()
    /** No connection, DNS, timeout, or cancellation-neutral I/O problem. */
    data object Unreachable : AdminFailure()
    /** 400 / 422 — request rejected by validation. */
    data class Validation(val code: Int, val message: String) : AdminFailure()
    /** 401 — no/expired session; requires re-authentication. */
    data object Unauthorized : AdminFailure()
    /** 403 — authenticated but role lacks the capability (server always enforces). */
    data object Forbidden : AdminFailure()
    /** 404 — submission/id does not exist (also used for ID search misses). */
    data object NotFound : AdminFailure()
    /** 409 — revision/state conflict; caller must re-fetch and re-review. */
    data class Conflict(val message: String) : AdminFailure()
    /** 429 — rate limited; [retryAfterSeconds] when the server supplied it. */
    data class RateLimited(val retryAfterSeconds: Long?) : AdminFailure()
    /** 5xx / 503 — service-side failure or maintenance. */
    data class ServerError(val code: Int) : AdminFailure()
    /** 2xx whose body does not match the verified contract shape. */
    data object InvalidPayload : AdminFailure()

    /** Stable machine key for logging/tests (never shown raw to users; UI maps to strings). */
    val key: String get() = when (this) {
        NotConfigured -> "not_configured"
        InvalidEndpoint -> "invalid_endpoint"
        Unreachable -> "unreachable"
        is Validation -> "validation_$code"
        Unauthorized -> "unauthorized"
        Forbidden -> "forbidden"
        NotFound -> "not_found"
        is Conflict -> "conflict"
        is RateLimited -> "rate_limited"
        is ServerError -> "server_error_$code"
        InvalidPayload -> "invalid_payload"
    }
}

/** Result of a contract call: value or typed failure — never a partial value with a hidden error. */
sealed class AdminResult<out T> {
    data class Ok<T>(val value: T) : AdminResult<T>()
    data class Err(val failure: AdminFailure) : AdminResult<Nothing>()
    inline fun <R> fold(onOk: (T) -> R, onErr: (AdminFailure) -> R): R = when (this) {
        is Ok -> onOk(value)
        is Err -> onErr(failure)
    }
}

/**
 * Parsed response of one endpoint. [raw] is retained only where the contract passes
 * bodies through (submission payload), so no field the server defines is dropped.
 */
data class AdminCapabilitiesResponse(val principalId: String, val capabilities: Set<String>)

class AdminApi(
    /** Supplier of the configured base URL; empty/null => [AdminFailure.NotConfigured]. */
    private val baseUrl: () -> String?,
    private val transport: AdminTransport,
    /** Bearer token supplier; null => treated as unauthenticated (401 handling stays server-driven). */
    private val token: () -> String? = { null },
) {

    // ---- verified endpoints -------------------------------------------------

    suspend fun capabilities(): AdminResult<AdminCapabilitiesResponse> =
        get("$V1/me/capabilities").flatMap { obj ->
            val caps = obj.optJSONArray("capabilities")
                ?: return@flatMap AdminResult.Err(AdminFailure.InvalidPayload)
            val set = (0 until caps.length()).mapNotNull {
                caps.optString(it)?.takeIf(String::isNotBlank)
            }.toSet()
            AdminResult.Ok(AdminCapabilitiesResponse(obj.optString("principalId", ""), set))
        }

    /**
     * Queue page. Only the two contract-defined parameters are ever sent: `cursor`
     * and `status` (PENDING_STATUSES values). NO search parameter exists (gap G2).
     */
    suspend fun queue(cursor: String?, status: String?): AdminResult<AdminQueuePage> {
        val query = buildList {
            if (!cursor.isNullOrBlank()) add("cursor" to cursor)
            if (!status.isNullOrBlank()) add("status" to status)
        }
        return get("$V1/admin/queue", query).flatMap { AdminResult.Ok(AdminQueuePage.parse(it)) }
    }

    /** Submission detail — the only verified source of title/composer-bearing payload. */
    suspend fun submission(id: String): AdminResult<AdminSubmissionDetail> =
        get("$V1/admin/submissions/${encode(id)}").flatMap { AdminResult.Ok(AdminSubmissionDetail.parse(it)) }

    /**
     * POST a decision. [body] comes from [buildDecisionBody] (expectedRevision +
     * idempotencyKey are mandatory). A conflict (409) or stale revision is returned
     * as [AdminFailure.Conflict] — never silently retried or re-issued.
     */
    suspend fun decide(id: String, body: JSONObject): AdminResult<AdminDecisionResult> =
        post("$V1/admin/submissions/${encode(id)}/decisions", body)
            .flatMap { AdminResult.Ok(AdminDecisionResult.parse(it)) }

    /** Role-scoped audit page; only `cursor` is a contract-defined parameter (gap G3). */
    suspend fun audit(cursor: String?): AdminResult<AdminAuditPage> {
        val query = if (!cursor.isNullOrBlank()) listOf("cursor" to cursor) else emptyList()
        return get("$V1/admin/audit", query).flatMap { AdminResult.Ok(AdminAuditPage.parse(it)) }
    }

    // ---- request plumbing ---------------------------------------------------

    private suspend fun get(path: String, query: List<Pair<String, String>> = emptyList()) =
        request("GET", path + queryOf(query), null)

    private suspend fun post(path: String, body: JSONObject) =
        request("POST", path, body.toString().toByteArray(Charsets.UTF_8))

    private suspend fun request(method: String, path: String, body: ByteArray?): AdminResult<JSONObject> {
        val base = baseUrl()?.trim().orEmpty()
        if (base.isEmpty()) return AdminResult.Err(AdminFailure.NotConfigured)
        val endpoint = validateEndpoint(base)
            ?: return AdminResult.Err(AdminFailure.InvalidEndpoint)

        val headers = buildMap {
            put("Accept", "application/json")
            put("User-Agent", "Goldberg/0.11-stage7b (https://github.com/stokonn/Goldberg)")
            if (body != null) put("Content-Type", "application/json; charset=utf-8")
            token()?.takeIf { it.isNotBlank() }?.let { put("Authorization", "Bearer $it") }
        }

        val response = try {
            transport.call(method, endpoint + path, headers, body)
        } catch (c: CancellationException) {
            throw c
        } catch (_: Exception) {
            return AdminResult.Err(AdminFailure.Unreachable)
        }

        if (response.body.size > MAX_BODY_BYTES) return AdminResult.Err(AdminFailure.InvalidPayload)
        return mapOutcome(response)
    }

    private fun mapOutcome(response: AdminHttpResponse): AdminResult<JSONObject> {
        val text = String(response.body, Charsets.UTF_8)
        val code = response.code
        fun err(f: AdminFailure) = AdminResult.Err(f)
        return when {
            code == 401 -> err(AdminFailure.Unauthorized)
            code == 403 -> err(AdminFailure.Forbidden)
            code == 404 -> err(AdminFailure.NotFound)
            code == 409 -> err(AdminFailure.Conflict(serverMessage(text)))
            code == 429 -> err(AdminFailure.RateLimited(response.retryAfterSeconds))
            code in 400..499 -> err(AdminFailure.Validation(code, serverMessage(text)))
            code in 500..599 -> err(AdminFailure.ServerError(code))
            code in 200..299 -> {
                val obj = try {
                    if (text.isBlank()) null else JSONObject(text)
                } catch (_: Exception) {
                    null
                } ?: return err(AdminFailure.InvalidPayload)
                AdminResult.Ok(obj)
            }
            else -> err(AdminFailure.InvalidPayload)
        }
    }

    companion object {
        const val V1 = "/v1"
        /** Hard ceiling for any single response body. */
        const val MAX_BODY_BYTES = 512 * 1024
        /** Contract-defined queue page limit (C6 PAGE_LIMIT) — provisional, server may differ. */
        const val QUEUE_PAGE_LIMIT = 20

        /** https-only, default port, no userinfo. Returns normalized base (no trailing slash) or null. */
        fun validateEndpoint(base: String): String? = try {
            val url = URL(base)
            val ok = url.protocol == "https" &&
                url.port in setOf(-1, 443) &&
                url.userInfo == null &&
                !url.host.isNullOrBlank()
            if (ok) base.trimEnd('/') else null
        } catch (_: Exception) {
            null
        }

        private fun serverMessage(body: String): String = try {
            JSONObject(body).optString("message").takeIf { it.isNotBlank() }
                ?: JSONObject(body).optString("error").takeIf { it.isNotBlank() }
                ?: ""
        } catch (_: Exception) {
            ""
        }

        private fun queryOf(query: List<Pair<String, String>>): String =
            if (query.isEmpty()) "" else "?" + query.joinToString("&") { (k, v) ->
                "${enc(k)}=${enc(v)}"
            }

        private fun enc(value: String): String = java.net.URLEncoder.encode(value, "UTF-8")

        private fun encode(value: String): String = enc(value)
    }
}

/** Success projection that keeps the failure typed instead of throwing it away. */
internal fun <T, R> AdminResult<T>.flatMap(transform: (T) -> AdminResult<R>): AdminResult<R> = when (this) {
    is AdminResult.Ok -> transform(value)
    is AdminResult.Err -> this
}
