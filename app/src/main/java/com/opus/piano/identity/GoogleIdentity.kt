package com.opus.piano.identity

import android.content.Context
import androidx.credentials.*
import androidx.room.withTransaction
import com.google.android.libraries.identity.googleid.*
import com.opus.piano.R
import com.opus.piano.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.security.SecureRandom

/** Local identity only. Tokens are never stored, logged or sent to a Goldberg backend. */
class GoogleIdentity(private val context: Context, private val db: OpusDatabase) {
    private val prefs = context.getSharedPreferences("goldberg-identity", Context.MODE_PRIVATE)
    val account = MutableStateFlow(prefs.getString("account", null))
    private val lock = Mutex()
    fun manualName() { check(prefs.edit().putBoolean("manual-name", true).commit()) }
    fun manualAvatar() { check(prefs.edit().putBoolean("manual-avatar", true).commit()) }

    suspend fun signIn(activity: Context) = lock.withLock {
        val nonce = android.util.Base64.encodeToString(ByteArray(32).also { SecureRandom().nextBytes(it) }, android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE)
        val option = GetSignInWithGoogleOption.Builder(context.getString(R.string.google_web_client_id)).setNonce(nonce).build()
        val response = CredentialManager.create(context).getCredential(activity, GetCredentialRequest(listOf(option)))
        val credential = response.credential
        require(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
        val google = GoogleIdTokenCredential.createFrom(credential.data)
        require(google.id.isNotBlank() && google.idToken.isNotBlank())
        // Credential Manager obtains this from the Google provider. There is no remote authorization here.
        // A future protected backend MUST verify the ID token server-side; decoding it is not verification.
        withContext(Dispatchers.IO) {
            val oldGoogleName = prefs.getString("google-name", null)
            db.withTransaction {
                val old = db.personalDao().getSettings() ?: ProfileSettings()
                val name = IdentityPolicy.name(old.username, oldGoogleName, google.displayName, prefs.getBoolean("manual-name", false))
                db.personalDao().saveSettings(old.copy(username = name))
            }
            check(prefs.edit().putString("account", google.id).putString("google-name", google.displayName)
                .putString("photo", google.profilePictureUri?.toString()).commit())
            account.value = google.id
        }
    }

    suspend fun signOut() = lock.withLock {
        // Clear provider selection first; a failure leaves a retryable signed-in UI.
        CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        withContext(Dispatchers.IO) {
            check(prefs.edit().remove("account").remove("photo").commit())
            account.value = null
        }
        // Keep the local nickname, avatar, favorites and all journal data.
    }
    fun googleName():String?=prefs.getString("google-name",null)
    suspend fun useGoogleName()=lock.withLock {withContext(Dispatchers.IO) {
        check(account.value!=null)
        val name=requireNotNull(googleName()?.trim()?.take(80)?.takeIf {it.isNotBlank()})
        db.withTransaction {val current=db.personalDao().getSettings() ?: ProfileSettings();db.personalDao().saveSettings(current.copy(username=name))}
        check(prefs.edit().putBoolean("manual-name",false).commit())
    }}
    suspend fun importPhoto(replaceExisting:Boolean=false, allowed: () -> Boolean) = lock.withLock {
        withContext(Dispatchers.IO) {
            val url = photo()?.let { java.net.URI(it) } ?: return@withContext
            if (account.value == null || !allowed() || url.scheme != "https" ||
                !(url.host == "googleusercontent.com" || url.host?.endsWith(".googleusercontent.com") == true)) return@withContext
            val initial = db.personalDao().getSettings() ?: ProfileSettings()
            if (!replaceExisting && !mayImportAvatar(initial.avatar)) return@withContext
            val connection = url.toURL().openConnection() as javax.net.ssl.HttpsURLConnection
            connection.connectTimeout = 8000; connection.readTimeout = 8000; connection.instanceFollowRedirects = false
            val bytes = try {
                require(connection.responseCode == 200)
                connection.inputStream.use { input ->
                    val out = java.io.ByteArrayOutputStream(); val buffer = ByteArray(8192)
                    while (true) {
                        val n = input.read(buffer); if (n < 0) break
                        check(allowed()); require(out.size() + n <= 2 * 1024 * 1024); out.write(buffer, 0, n)
                    }
                    out.toByteArray()
                }
            } finally { connection.disconnect() }
            val bitmap = requireNotNull(decodeGoogleAvatar(bytes))
            val output = java.io.ByteArrayOutputStream()
            try { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 88, output) } finally { bitmap.recycle() }
            check(allowed())
            db.withTransaction {
                val current = db.personalDao().getSettings() ?: ProfileSettings()
                if (current.avatar==initial.avatar && (replaceExisting || mayImportAvatar(current.avatar))) db.personalDao().saveSettings(current.copy(avatar = android.util.Base64.encodeToString(output.toByteArray(), android.util.Base64.NO_WRAP)))
            }
        }
    }
    fun photo(): String? = prefs.getString("photo", null)
    fun mayImportAvatar(current: String) = current.isBlank() && !prefs.getBoolean("manual-avatar", false)
}

object IdentityPolicy {
    fun name(current: String, previousGoogle: String?, incoming: String?, manual: Boolean): String =
        if (!manual && (current == "Musician" || current.isBlank() || current == previousGoogle))
            incoming?.trim()?.take(80)?.ifBlank { current } ?: current
        else current
}

/** Bounded bitmap decoding shared by the real import path and Android regression tests. */
internal fun decodeGoogleAvatar(bytes: ByteArray): android.graphics.Bitmap? {
    val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    require(bounds.outWidth in 1..8192 && bounds.outHeight in 1..8192)
    val options = android.graphics.BitmapFactory.Options().apply {
        inSampleSize = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / inSampleSize > 512) inSampleSize *= 2
    }
    return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
}
