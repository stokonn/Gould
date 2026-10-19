package com.opus.piano

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.catalogue.CataloguePortraits
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class Stage4PortraitTest {
    private val context=InstrumentationRegistry.getInstrumentation().targetContext
    private fun id()=UUID.randomUUID().toString()
    private fun image():ByteArray {
        val bitmap=Bitmap.createBitmap(12,16,Bitmap.Config.ARGB_8888)
        return ByteArrayOutputStream().use {out->bitmap.compress(Bitmap.CompressFormat.PNG,100,out);bitmap.recycle();out.toByteArray()}
    }
    private fun metadata(bytes:ByteArray)=JSONObject().put("sha1",MessageDigest.getInstance("SHA-1").digest(bytes).joinToString(""){"%02x".format(it)})
        .put("timestamp","2026-10-19T00:00:00Z").put("descriptionurl","https://commons.wikimedia.org/wiki/File:Authored.png")
        .put("url","https://upload.wikimedia.org/wikipedia/commons/a/aa/Authored.png").put("size",bytes.size).put("width",12).put("height",16).put("mime","image/png")
        .put("extmetadata",JSONObject().put("Artist",JSONObject().put("value","<b>Authored artist</b>"))
            .put("ObjectName",JSONObject().put("value","Authored title")).put("Credit",JSONObject().put("value","Authored source credit"))
            .put("Copyright",JSONObject().put("value","Authored copyright notice")).put("LicenseShortName",JSONObject().put("value","CC BY-SA 4.0"))
            .put("LicenseUrl",JSONObject().put("value","https://creativecommons.org/licenses/by-sa/4.0/")))
    private fun connection(bytes:ByteArray)=object:HttpURLConnection(URL("https://upload.wikimedia.org/fixture")) {
        override fun connect(){}
        override fun disconnect(){}
        override fun usingProxy()=false
        override fun getResponseCode()=200
        override fun getContentType()="image/png"
        override fun getContentLengthLong()=bytes.size.toLong()
        override fun getInputStream()=ByteArrayInputStream(bytes)
    }
    @Test fun verifiedOriginalRetainsCreditsAndOfflineReadRejectsTamperedRightsOrBytes()=runBlocking {
        val directory=File(context.cacheDir,"stage4-portrait-${id()}");val key=id();val bytes=image();var calls=0;var permitted=true
        val portraits=CataloguePortraits(directory,reserveBytes={0},connect={calls++;connection(bytes)}){permitted}
        try {
            val original=metadata(bytes)
            assertTrue(portraits.download(key,original));assertFalse(original.has("downloadSha256"))
            permitted=false;assertNotNull(portraits.read(key));assertEquals(1,calls)
            val credit=portraits.credits(key)!!
            for(value in listOf("Authored artist","Authored title","Authored source credit","Authored copyright notice"))assertTrue(credit.getString("displayAttribution").contains(value))
            assertFalse(credit.getString("displayAttribution").contains("<b>"))
            val manifest=File(directory,"$key.json");val saved=manifest.readText()
            val corrupted=JSONObject(saved);corrupted.getJSONObject("extmetadata").getJSONObject("LicenseShortName").put("value","CC BY-NC 4.0")
            manifest.writeText(corrupted.toString());assertNull(portraits.read(key))
            manifest.writeText(saved);File(directory,credit.getString("downloadSha256")+".img").writeBytes(byteArrayOf(1,2,3));assertNull(portraits.read(key))
        }finally {directory.deleteRecursively()}
    }
    @Test fun changedLengthDimensionsMimeOrRevisionNeverPublishesAndFailedRefreshKeepsPriorPortrait()=runBlocking {
        val directory=File(context.cacheDir,"stage4-portrait-${id()}");val key=id();val bytes=image()
        val portraits=CataloguePortraits(directory,reserveBytes={0},connect={connection(bytes)}){true}
        try {
            assertTrue(portraits.download(key,metadata(bytes)))
            val before=File(directory,"$key.json").readText()
            for((field,value) in listOf("size" to bytes.size+1,"width" to 99,"mime" to "image/jpeg","sha1" to "0".repeat(40))) {
                assertTrue(field,runCatching {portraits.download(key,metadata(bytes).put(field,value))}.isFailure)
                assertEquals(before,File(directory,"$key.json").readText());assertNotNull(portraits.read(key))
            }
        }finally {directory.deleteRecursively()}
    }
    @Test fun missingConsentBlocksTransferAndInsufficientReserveCannotReplaceSavedPortrait()=runBlocking {
        val directory=File(context.cacheDir,"stage4-portrait-${id()}");val key=id();val bytes=image();var calls=0;var permitted=false;var reserve=0L
        val portraits=CataloguePortraits(directory,reserveBytes={reserve},connect={calls++;connection(bytes)}){permitted}
        try {
            assertFalse(portraits.download(key,metadata(bytes)));assertEquals(0,calls)
            permitted=true;assertTrue(portraits.download(key,metadata(bytes)))
            val before=File(directory,"$key.json").readText();reserve=Long.MAX_VALUE
            assertTrue(runCatching {portraits.download(key,metadata(bytes))}.isFailure)
            assertEquals(before,File(directory,"$key.json").readText());assertNotNull(portraits.read(key))
        }finally {directory.deleteRecursively()}
    }
}
