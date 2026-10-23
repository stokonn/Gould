package com.opus.piano

import android.content.Context
import android.content.ContextWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.admin.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Native storage/JSON/boundary checks. Synthetic data; no auth or backend claim. */
@RunWith(AndroidJUnit4::class)
class Stage7bAdminRuntimeTest {
    private fun isolatedStore(check: (Context, String) -> Unit) {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "stage7b-ci-${System.nanoTime()}"
        val context = object : ContextWrapper(base) {
            // Stores resolve applicationContext before opening preferences. Keep
            // that resolution inside this disposable test context as well.
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(ignored: String, mode: Int) = base.getSharedPreferences(name, mode)
        }
        try { check(context, name) } finally { base.deleteSharedPreferences(name) }
    }

    @Test fun keystoreSessionSurvivesStoreRecreationAndTamperingClearsIt() = isolatedStore { context, name ->
        val token = "synthetic-ci-session-not-a-credential"
        KeystoreAdminSessionStore(context).writeToken(token)
        assertEquals(token, KeystoreAdminSessionStore(context).readToken())
        val prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        assertFalse(prefs.getString("session_token", "")!!.contains(token))
        assertTrue(prefs.edit().putString("session_token", "malformed-ciphertext").commit())
        assertNull(KeystoreAdminSessionStore(context).readToken())
        assertFalse(prefs.contains("session_token"))
    }

    @Test fun androidMarkersPersistAcrossStoreRecreationAndStayAccountScoped() = isolatedStore { context, _ ->
        val first = AndroidAdminMarkerStore(context)
        first.save("synthetic-account-a", AdminMarkerKind.DEFERRED, setOf("synthetic-submission"))
        val restarted = AndroidAdminMarkerStore(context)
        assertEquals(setOf("synthetic-submission"), restarted.load("synthetic-account-a", AdminMarkerKind.DEFERRED))
        assertTrue(restarted.load("synthetic-account-b", AdminMarkerKind.DEFERRED).isEmpty())
        assertTrue(restarted.load("synthetic-account-a", AdminMarkerKind.RE_REVIEW).isEmpty())
    }

    @Test fun savedEndpointAndKeystoreTokenCannotEnableApplicationNetworking() = isolatedStore { context, _ ->
        val store = KeystoreAdminSessionStore(context)
        store.writeToken("synthetic-ci-session-not-a-credential")
        assertNotNull(store.readToken())
        var reads = 0
        val api = AdminApi.forApplication({ "https://must-not-contact.invalid" }, { reads++; store.readToken() })
        runBlocking {
            assertEquals(AdminResult.Err(AdminFailure.ContractUnavailable), api.capabilities())
            assertEquals(AdminResult.Err(AdminFailure.ContractUnavailable), api.queue(null, null))
            assertEquals(AdminResult.Err(AdminFailure.ContractUnavailable), api.decide("synthetic", JSONObject()))
        }
        assertEquals(0, reads)
    }

    @Test fun nativeJsonPreservesExactAuditBigintAndRejectsRevisionOverflow() {
        val event = JSONObject("""{"seq":"9007199254740993","ts":1,"actor":"synthetic","action":"synthetic","subject":"synthetic","prevHash":"a","thisHash":"b"}""")
        assertEquals(9007199254740993L, AdminAuditEvent.parse(event).seq)
        val row = JSONObject("""{"id":"synthetic","owner":"synthetic","status":"submitted","revision":4294967297,"createdAt":1}""")
        try { AdminQueueItem.parse(row); fail("Overflow accepted by Android JSON codec") }
        catch (_: IllegalArgumentException) { }
    }
}
