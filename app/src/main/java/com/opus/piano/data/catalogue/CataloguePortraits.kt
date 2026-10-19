package com.opus.piano.data.catalogue

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AtomicFile
import com.opus.piano.data.catalogue.providers.CommonsAdapter
import com.opus.piano.data.catalogue.providers.ProviderJson
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Explicit tiny portrait requests only. No image URL is used without file-specific rights. */
class CataloguePortraits(private val directory:File,private val reserveBytes:()->Long={128L*1024*1024},private val connect:(URL)->HttpURLConnection={it.openConnection() as HttpURLConnection},private val intake:CatalogueIntake=CatalogueIntake(),private val permitted:()->Boolean) {
    private val gate=Mutex()
    private fun hash(bytes:ByteArray)=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}
    private fun key(id:String):String {CatalogueValidation.uuid(id);return id}
    suspend fun download(id:String,metadata:JSONObject):Boolean=gate.withLock {downloadOriginal(id,JSONObject(metadata.toString()))}
    private suspend fun downloadOriginal(id:String,metadata:JSONObject):Boolean=withContext(Dispatchers.IO) {
        key(id)
        require(metadata.toString().toByteArray().size<=60000)
        val tree=ProviderJson.parse(metadata.toString().toByteArray())
        @Suppress("UNCHECKED_CAST") val rights=tree["extmetadata"] as? Map<String,Any?> ?: return@withContext false
        require(CommonsAdapter().approved(rights))
        require(metadata.optString("sha1").matches(Regex("[0-9a-f]{40}")) && metadata.optString("timestamp").isNotBlank())
        val source=URL(metadata.getString("descriptionurl"));require(source.protocol=="https" && source.host=="commons.wikimedia.org" && source.port in setOf(-1,443) && source.userInfo==null && source.path.startsWith("/wiki/File:"))
        require(metadata.optLong("size") in 1..MAX_BYTES.toLong())
        val width=metadata.getLong("width");val height=metadata.getLong("height")
        require(width>0 && height>0 && height<=1024*1024 && width<=1024*1024/height)
        val url=URL(metadata.getString("url"));require(url.protocol=="https" && url.host=="upload.wikimedia.org" && url.port in setOf(-1,443) && url.userInfo==null)
        if(!permitted())return@withContext false
        val connection=connect(url)
        val cancellation=CoroutineScope(currentCoroutineContext()).launch(Dispatchers.IO,start=CoroutineStart.UNDISPATCHED){try{awaitCancellation()}finally{connection.disconnect()}}
        try {
            connection.connectTimeout=10000;connection.readTimeout=10000;connection.instanceFollowRedirects=false
            connection.setRequestProperty("User-Agent","Goldberg/0.11-stage4 (https://github.com/stokonn/Goldberg)")
            require(connection.responseCode==200 && connection.contentType?.substringBefore(';') in setOf("image/jpeg","image/png","image/webp"))
            require(connection.contentLengthLong<=MAX_BYTES)
            val out=ByteArrayOutputStream()
            connection.inputStream.use {input->val buffer=ByteArray(8192);while(true){currentCoroutineContext().ensureActive();check(permitted());val read=input.read(buffer);if(read<0)break;require(out.size()+read<=MAX_BYTES);out.write(buffer,0,read)}}
            val bytes=out.toByteArray()
            require(bytes.size.toLong()==metadata.getLong("size")) {"Source image length changed"}
            val sourceHash=MessageDigest.getInstance("SHA-1").digest(bytes).joinToString(""){"%02x".format(it)}
            require(sourceHash==metadata.getString("sha1")) {"Source image revision changed"}
            val bounds=BitmapFactory.Options().apply {inJustDecodeBounds=true}
            BitmapFactory.decodeByteArray(bytes,0,bytes.size,bounds)
            require(bounds.outWidth.toLong()==metadata.getLong("width") && bounds.outHeight.toLong()==metadata.getLong("height") && bounds.outMimeType==metadata.getString("mime"))
            require(decode(bytes)?.also {it.recycle()}!=null)
            currentCoroutineContext().ensureActive();check(permitted())
            directory.mkdirs()
            intake.commit(bytes.size.toLong()+65536) {
                require(CatalogueBudget.fits(directory.usableSpace,bytes.size.toLong()+65536,reserveBytes()))
                currentCoroutineContext().ensureActive();check(permitted())
                val revision=hash(bytes);val image=AtomicFile(File(directory,revision+".img"));val stream=image.startWrite()
                try {stream.write(bytes);image.finishWrite(stream)}catch(e:Exception){image.failWrite(stream);throw e}
                val manifest=metadata.put("downloadSha256",revision).put("bytes",bytes.size).put("transformation","Original file; display scaling only, no crop")
                val pointer=AtomicFile(File(directory,key(id)+".json"));val written=pointer.startWrite()
                try {written.write(manifest.toString().toByteArray());pointer.finishWrite(written)}catch(e:Exception){pointer.failWrite(written);throw e}
                trim();true
            }
        } finally {cancellation.cancel();connection.disconnect()}
    }
    data class Snapshot(val bitmap:Bitmap,val credits:JSONObject)
    /** Resolve bytes and attribution under one lock from the same verified manifest revision. */
    suspend fun snapshot(id:String):Snapshot?=gate.withLock {readOriginal(id)}
    suspend fun read(id:String):Bitmap?=snapshot(id)?.bitmap
    private suspend fun readOriginal(id:String):Snapshot?=withContext(Dispatchers.IO) {
        try {
            val manifest=AtomicFile(File(directory,key(id)+".json"));if(!manifest.baseFile.exists())return@withContext null
            require(manifest.baseFile.length()<=65536)
            val o=JSONObject(manifest.openRead().use {it.bufferedReader().readText()});val digest=o.getString("downloadSha256");require(digest.matches(Regex("[a-f0-9]{64}")))
            val tree=ProviderJson.parse(o.toString().toByteArray())
            @Suppress("UNCHECKED_CAST") val rights=tree["extmetadata"] as? Map<String,Any?> ?: return@withContext null
            require(CommonsAdapter().approved(rights))
            val file=File(directory,digest+".img");require(file.length() in 1..MAX_BYTES.toLong())
            val bytes=file.readBytes();require(hash(bytes)==digest);val now=System.currentTimeMillis();manifest.baseFile.setLastModified(now);file.setLastModified(now)
            val bitmap=decode(bytes) ?: return@withContext null
            Snapshot(bitmap,displayCredits(o))
        }catch(e:CancellationException){throw e}catch(_:Exception){null}
    }
    suspend fun credits(id:String):JSONObject?=snapshot(id)?.let {it.bitmap.recycle();it.credits}
    private fun displayCredits(metadata:JSONObject):JSONObject {
        val fields=metadata.getJSONObject("extmetadata")
        val display=CommonsAdapter.attributionFields.filter {it !in setOf("LicenseUrl","Restrictions","AttributionRequired","Copyrighted")}.mapNotNull {key->
            fields.optJSONObject(key)?.optString("value")?.takeIf {it.isNotBlank()}?.let {value->
                android.text.Html.fromHtml(value,android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
            }
        }.filter {it.isNotBlank()}.distinct().joinToString("\n")
        return metadata.put("displayAttribution",display)
    }
    private fun trim() {
        val files=directory.listFiles()?.filter {it.isFile && it.name.matches(Regex("(?:[a-f0-9]{64}\\.img|[a-f0-9-]{36}\\.json)"))}?.sortedBy {it.lastModified()}.orEmpty()
        var bytes=files.sumOf {it.length()}
        for(file in files)if(bytes>16L*1024*1024 || System.currentTimeMillis()-file.lastModified()>30L*86400000){val length=file.length();if(file.delete())bytes-=length}
    }
    companion object {
        const val MAX_BYTES=512*1024
        fun decode(bytes:ByteArray):Bitmap? {
            if(bytes.size>MAX_BYTES)return null
            val bounds=BitmapFactory.Options().apply {inJustDecodeBounds=true};BitmapFactory.decodeByteArray(bytes,0,bytes.size,bounds)
            if(bounds.outWidth<=0 || bounds.outHeight<=0 || bounds.outWidth.toLong()*bounds.outHeight>1024*1024)return null
            return BitmapFactory.decodeByteArray(bytes,0,bytes.size)
        }
    }
}
