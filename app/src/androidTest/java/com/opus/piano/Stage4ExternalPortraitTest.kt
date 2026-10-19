package com.opus.piano

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.catalogue.*
import com.opus.piano.data.catalogue.providers.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/** Opt-in acceptance of a separately acquired public original; never bundled into either APK.
 * This checks the production verification/cache/render path with a file-backed transfer, not
 * live Android networking. The host acquisition has its own source/hash evidence. */
@RunWith(AndroidJUnit4::class)
class Stage4ExternalPortraitTest {
    @Test fun separatelyAcquiredLicensedOriginalCanBeVerifiedAndRenderedOffline()=runBlocking {
        val args=InstrumentationRegistry.getArguments()
        assumeTrue("Requires separately acquired original and per-file metadata",args.containsKey("stage4PortraitMetadata") && args.containsKey("stage4PortraitOriginal"))
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val root=ProviderJson.parse(File(args.getString("stage4PortraitMetadata")!!).readBytes())
        @Suppress("UNCHECKED_CAST") val pages=root["metadata"] as List<Map<String,Any?>>
        val projection=CommonsAdapter().project(mapOf("query" to mapOf("pages" to pages)))
        assertFalse(projection["blocked"]==true)
        val metadata=JSONObject(projection)
        val bytes=File(args.getString("stage4PortraitOriginal")!!).readBytes()
        val key=UUID.randomUUID().toString()
        val ui=args.getString("stage4UiFixture")=="true"
        val directory=File(context.cacheDir,if(ui)"catalogue-stage4/portraits" else "stage4-public-portrait-$key")
        val portraits=CataloguePortraits(directory,connect={url->object:HttpURLConnection(url) {
            override fun connect(){}
            override fun disconnect(){}
            override fun usingProxy()=false
            override fun getResponseCode()=200
            override fun getContentType()=metadata.getString("mime")
            override fun getContentLengthLong()=bytes.size.toLong()
            override fun getInputStream()=ByteArrayInputStream(bytes)
        }}){true}
        try {
            assertTrue(portraits.download(key,metadata))
            val bitmap=portraits.read(key)!!
            assertEquals(metadata.getInt("width"),bitmap.width);assertEquals(metadata.getInt("height"),bitmap.height);bitmap.recycle()
            assertTrue(portraits.credits(key)!!.getString("displayAttribution").contains("Dennis Christians"))
            if(ui) {
                val app=context.applicationContext as OpusApplication
                val stored=app.localCatalogue.access {repo->
                    repo.add(CatalogueEntity(key,"artist","Michael Seltenreich — licensed portrait verification"),CatalogueArtist(key))
                    val source=CatalogueSource(UUID.randomUUID().toString(),"commons","File:Composer, Michael Seltenreich in Central Park, New York.jpg",metadata.getString("timestamp"),portraits.credits(key)!!.getString("downloadSha256"),"2026-10-19T00:00:00Z",metadata.getString("descriptionurl"))
                    val rights=CatalogueRights(UUID.randomUUID().toString(),"CC-BY-SA-4.0","https://creativecommons.org/licenses/by-sa/4.0/","Dennis Christians; source credit retained in the portrait manifest","approved","Display scaling only; no crop")
                    repo.addSource(source);repo.addRights(rights)
                    repo.addAssertion(CatalogueAssertion(UUID.randomUUID().toString(),key,"portrait_source",JSONObject().put("value",metadata.getString("descriptionurl")).put("language","und").toString(),source.id,rights.id,true))
                }
                assertTrue(stored is CatalogueAccess.Available)
                File(context.filesDir,"stage4-public-portrait-id.txt").writeText(key)
            }
            println("STAGE4_PUBLIC_PORTRAIT id=$key width=${metadata.getInt("width")} height=${metadata.getInt("height")} offlineRead=true fileBackedTransfer=true")
        }finally {if(!ui)directory.deleteRecursively()}
    }
}
