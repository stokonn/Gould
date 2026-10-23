package com.opus.piano.admin

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Session token storage for the admin role (Stage 7B.2).
 *
 * The interface is deliberately tiny and JVM-testable; the production implementation
 * encrypts at rest with an Android Keystore AES-GCM key (hardware-backed where
 * available). The backing SharedPreferences file inherits the app-wide exclusion from
 * backup (`android:allowBackup="false"` + `fullBackupContent="false"`), so the token
 * never leaves the device via backup/transfer. Tokens are never logged and are cleared
 * on logout/revocation.
 */
interface AdminSessionStore {
    fun readToken(): String?
    fun writeToken(token: String)
    fun clear()
}

/** In-memory store for JVM tests and previews. */
class InMemoryAdminSessionStore : AdminSessionStore {
    @Volatile private var token: String? = null
    override fun readToken(): String? = token
    override fun writeToken(token: String) { this.token = token }
    override fun clear() { token = null }
}

/**
 * Keystore-backed store. Cipher text + IV live in a private prefs file; the key itself
 * never leaves the Keystore. A Keystore failure (unsupported device, corrupted entry)
 * degrades to [clear] — a lost token forces re-authentication, never plaintext storage.
 */
class KeystoreAdminSessionStore(context: Context) : AdminSessionStore {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    @Synchronized
    override fun readToken(): String? {
        val payload = prefs.getString(KEY_TOKEN, null) ?: return null
        val bytes = try {
            Base64.decode(payload, Base64.NO_WRAP)
        } catch (_: Exception) {
            clear(); return null
        }
        if (bytes.size <= IV_BYTES) { clear(); return null }
        val iv = bytes.copyOfRange(0, IV_BYTES)
        val sealed = bytes.copyOfRange(IV_BYTES, bytes.size)
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_BITS, iv))
            String(cipher.doFinal(sealed), Charsets.UTF_8)
        } catch (_: Exception) {
            clear(); null
        }
    }

    @Synchronized
    override fun writeToken(token: String) {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey())
            val sealed = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
            val joined = cipher.iv + sealed
            prefs.edit().putString(KEY_TOKEN, Base64.encodeToString(joined, Base64.NO_WRAP)).apply()
        } catch (_: Exception) {
            // Never fall back to storing a plaintext token.
            clear()
        }
    }

    @Synchronized
    override fun clear() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "goldberg-admin-session"
        private const val KEY_TOKEN = "session_token"
        private const val FILE_NAME = "goldberg-admin-session"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_BYTES = 12
        private const val TAG_BITS = 128
    }
}
