package com.opus.piano.data.community.canonical

import com.opus.piano.admin.AdminHttpResponse
import com.opus.piano.admin.AdminTransport
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class CanonicalAdminRepositoryTest {
    private val root = generateSequence(File(requireNotNull(System.getProperty("user.dir")))) { it.parentFile }
        .first { File(it, "contracts/admin/shared/v1/contract.json").exists() }
    private val schema = File(root, "contracts/admin/v1/decision-proposal/dto.schema.json").readText()
    private val captures = JSONObject(File(root, "spikes/stage6-api-v1-local/results/http-pg/pg-ojxz4mbi/shared-contract-captures.json").readText()).getJSONArray("captures")
    private fun raw(dto: String): String {
        val value = (0 until captures.length()).map { captures.getJSONObject(it) }
            .first { it.optString("dto") == dto && !it.isNull("response") }.getJSONObject("response")
        val copy = JSONObject(value.toString())
        if (dto == "Capabilities") {
            val tokens = JSONObject(raw("Tokens"))
            copy.put("principalId", tokens.getString("principalId")).put("sessionId", tokens.getString("sessionId"))
        }
        return copy.toString()
    }
    private fun repo(transport: CanonicalAdminTransport) = CanonicalAdminRepository(CanonicalAdminClient(AdminContract(schema), transport))
    @Test fun applicationTransportCannotBeEnabledByEndpointOrToken() = runBlocking {
        val t = CanonicalHttpTransport({ "https://example.invalid" })
        assertTrue(runCatching { t.exchange("GET", "/v1/session", null, "token") }.isFailure)
    }
    @Test fun packagedSchemaIsExactlyTheApprovedContract() {
        assertEquals(schema, File(root, "app/src/main/assets/community/admin-canonical-schema.json").readText())
    }
    @Test fun canonicalServiceUsesActualAndroidTransportSeam() = runBlocking {
        val calls = mutableListOf<String>()
        val t = AdminTransport { method, url, headers, body ->
            calls += "$method $url"
            assertTrue(url.startsWith("https://fixture.invalid/v1/"))
            val pair = when {
                method == "POST" -> { assertNotNull(body); assertNotNull(headers["Content-Type"]); 201 to raw("Tokens") }
                url.endsWith("capabilities") -> 200 to raw("Capabilities")
                else -> 200 to raw("QueuePage")
            }
            AdminHttpResponse(pair.first, pair.second.toByteArray(), null)
        }
        val r = repo(CanonicalHttpTransport({ "https://fixture.invalid" }, t))
        r.authenticate("test", "test"); r.queue()
        assertTrue(r.authenticated); assertNotNull(r.state.value.capabilities); assertNotNull(r.state.value.queue)
        assertEquals(3, calls.size)
    }
    @Test fun logoutClearsServiceStateBeforeBlockedTransport() = runBlocking {
        val entered = CompletableDeferred<Unit>(); val release = CompletableDeferred<Unit>()
        val r = repo(CanonicalAdminTransport { method, path, _, _ ->
            when {
                method == "POST" -> AdminResponse(201, raw("Tokens"))
                method == "DELETE" -> { entered.complete(Unit); release.await(); AdminResponse(204, "") }
                path.endsWith("capabilities") -> AdminResponse(200, raw("Capabilities"))
                else -> AdminResponse(200, raw("QueuePage"))
            }
        })
        r.authenticate("test", "test"); r.queue()
        val job = async { r.logout() }; entered.await()
        assertFalse(r.authenticated); assertEquals(CanonicalAdminState(), r.state.value)
        release.complete(Unit); assertTrue(job.await().revocationConfirmed)
    }
    @Test fun accountClearRejectsLateQueueAndDiscardsCachedData() = runBlocking {
        val entered = CompletableDeferred<Unit>(); val release = CompletableDeferred<Unit>()
        val r = repo(CanonicalAdminTransport { method, path, _, _ ->
            when {
                method == "POST" -> AdminResponse(201, raw("Tokens"))
                path.endsWith("capabilities") -> AdminResponse(200, raw("Capabilities"))
                else -> { entered.complete(Unit); release.await(); AdminResponse(200, raw("QueuePage")) }
            }
        })
        r.authenticate("test", "test")
        val late = async { runCatching { r.queue() } }; entered.await(); r.clear(); release.complete(Unit)
        assertTrue(late.await().isFailure); assertEquals(CanonicalAdminState(), r.state.value); assertFalse(r.authenticated)
    }
    @Test fun outageInvalidatesSensitiveCachedDocumentsAndFreshProcessHasNoSession() = runBlocking {
        var outage = false
        val r = repo(CanonicalAdminTransport { method, path, _, _ ->
            when {
                method == "POST" -> AdminResponse(201, raw("Tokens"))
                path.endsWith("capabilities") -> AdminResponse(200, raw("Capabilities"))
                outage -> AdminResponse(503, "")
                else -> AdminResponse(200, raw("QueuePage"))
            }
        })
        r.authenticate("test", "test"); r.queue(); outage = true
        assertTrue(runCatching { r.queue() }.isFailure); assertNull(r.state.value.queue)
        assertFalse(repo(CanonicalAdminTransport { _, _, _, _ -> error("must not call") }).authenticated)
    }
    @Test fun invalidUtf8RedirectAndOversizedResponsesFailClosed() = runBlocking {
        for (response in listOf(AdminHttpResponse(200, byteArrayOf(0xc3.toByte(), 0x28), null), AdminHttpResponse(302, byteArrayOf(), null), AdminHttpResponse(200, ByteArray(524289), null), AdminHttpResponse(204, byteArrayOf(32), null))) {
            val t = CanonicalHttpTransport({ "https://fixture.invalid" }, AdminTransport { _, _, _, _ -> response })
            val result = runCatching { t.exchange("GET", "/v1/session", null, null) }
            if (response.code == 302) assertEquals(302, result.getOrThrow().status) else assertTrue(result.isFailure)
        }
    }
}
