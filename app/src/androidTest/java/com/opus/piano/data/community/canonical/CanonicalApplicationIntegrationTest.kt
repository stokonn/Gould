package com.opus.piano.data.community.canonical

import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.OpusApplication
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.security.MessageDigest

class CanonicalApplicationIntegrationTest {
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as OpusApplication
    private fun contract() = app.assets.open("community/admin-canonical-schema.json").bufferedReader().use { AdminContract(it.readText()) }
    @Test fun applicationCanonicalServiceRemainsOfflineAndUnauthenticated() = runBlocking {
        val repo = app.canonicalAdminRepository()
        repo.clear()
        assertFalse(repo.authenticated)
        assertTrue(runCatching { repo.challenge() }.isFailure)
        assertEquals(CanonicalAdminState(), repo.state.value)
    }
    @Test fun packagedContractAndAndroidStrictJsonPreserveCanonicalWire() {
        val bytes = app.assets.open("community/admin-canonical-schema.json").use { it.readBytes() }
        val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        assertEquals("d0802a9397d51a9c542a39c44f0cf4343c0ff9a5e4c536cb18cd3627f1afd9ca", hash)
        assertTrue(runCatching { contract().decode("EmptyRequest", "{\"schemaVersion\":1,\"schemaVersion\":1}", true) }.isFailure)
        assertEquals(1, contract().decode("EmptyRequest", "{\"schemaVersion\":1}", true).json().getInt("schemaVersion"))
    }
    @Test fun serviceRecreationDiscardsCredentialsAndSensitiveCache() {
        val first = CanonicalAdminRepository(CanonicalAdminClient(contract(), CanonicalAdminTransport { _, _, _, _ -> error("disabled") }))
        first.clear()
        val recreated = CanonicalAdminRepository(CanonicalAdminClient(contract(), CanonicalAdminTransport { _, _, _, _ -> error("disabled") }))
        assertFalse(recreated.authenticated); assertEquals(CanonicalAdminState(), recreated.state.value)
    }
    @Test fun androidSessionIdentityMismatchClearsCredentials() = runBlocking {
        val a = "00000000-0000-4000-8000-000000000001"
        val b = "00000000-0000-4000-8000-000000000002"
        val session = "00000000-0000-4000-8000-000000000003"
        val id = "00000000-0000-4000-8000-000000000004"
        var capabilitiesCalled = false
        val repo = CanonicalAdminRepository(CanonicalAdminClient(contract(), CanonicalAdminTransport { method, _, _, _ ->
            if (method == "POST") AdminResponse(201, JSONObject().put("schemaVersion",1).put("requestId",id)
                .put("principalId",a).put("sessionId",session).put("accessToken","synthetic_access").put("refreshToken","synthetic_refresh")
                .put("expiresAt","2026-10-25T15:00:00.000000Z").put("refreshExpiresAt","2026-10-26T15:00:00.000000Z").toString())
            else { capabilitiesCalled = true; AdminResponse(200,JSONObject().put("schemaVersion",1).put("requestId",id).put("principalId",b).put("sessionId",session).put("capabilities",org.json.JSONArray()).toString()) }
        }))
        assertTrue(runCatching { repo.authenticate("synthetic_challenge","synthetic_credential") }.isFailure)
        assertTrue(capabilitiesCalled)
        assertFalse(repo.authenticated); assertEquals(CanonicalAdminState(),repo.state.value)
    }
}
