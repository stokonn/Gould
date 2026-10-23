package com.opus.piano.data.community

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** No OAuth/profile integration. The future authorized service-auth flow owns enrollment. */
class AndroidCommunityCredentials(context: Context): CommunityCredentials {
    private val file=AtomicFile(File(context.noBackupFilesDir,"community-credentials.v1"))
    private val alias="goldberg.community.service.v1"
    @Synchronized private fun key(): SecretKey {
        val store=KeyStore.getInstance("AndroidKeyStore").apply {load(null)}
        (store.getKey(alias,null) as? SecretKey)?.let {return it}
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }
    private var cached: CommunitySession?=null
    @Synchronized override fun current(): CommunitySession? {
        cached?.let {return it}
        return runCatching {
            val data=file.openRead().use {it.readBytes()};require(data.size in 29..32768)
            val cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,key(),GCMParameterSpec(128,data.copyOfRange(0,12)));cipher.updateAAD(alias.toByteArray())
            val o=JSONObject(String(cipher.doFinal(data.copyOfRange(12,data.size)),Charsets.UTF_8))
            CommunitySession(o.getString("principal"),o.getString("credential"),o.getLong("expires"),o.getBoolean("submit"))
        }.getOrNull()?.also {cached=it}
    }
    /** Internal seam for synthetic instrumentation and future separately authorized auth. */
    @Synchronized internal fun store(session: CommunitySession) {
        val cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key());cipher.updateAAD(alias.toByteArray())
        val o=JSONObject().put("principal",session.principalId).put("credential",session.credential).put("expires",session.expiresAt).put("submit",session.canSubmit)
        val bytes=cipher.iv+cipher.doFinal(o.toString().toByteArray());val out=file.startWrite()
        try {out.write(bytes);file.finishWrite(out);cached=session}catch(t: Throwable){file.failWrite(out);throw t}
    }
    @Synchronized override fun clear() {cached=null;file.delete()}
}
