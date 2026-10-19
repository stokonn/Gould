package com.opus.piano

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.catalogue.providers.*
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Optional real-response parsing only. No network calls or private input; bytes stay outside APKs. */
@RunWith(AndroidJUnit4::class)
class Stage4ExternalProviderTest {
    @Test fun boundedObservedProviderResponsesPassNativeProjectionAndSearchPaging() {
        val path=InstrumentationRegistry.getArguments().getString("stage4ProviderDirectory")
        assumeTrue("Requires separately acquired bounded public provider responses",path!=null)
        val directory=File(path!!)
        fun read(name:String)=ProviderJson.parse(File(directory,name).readBytes())
        val mb=MusicBrainzAdapter();val request=mb.search("Johann Sebastian Bach","artists","en").single()
        val root=read("0-musicbrainz.json")
        val records=mb.parse(request,root)
        assertEquals(1,records.size);assertEquals("artist",records.single().kind)
        assertEquals("24f1766e-9635-4d58-a4d4-9413f9f98a4c",records.single().externalId)
        assertEquals(1,mb.nextOffset(request,root))
        assertTrue(records.single().fields.none {it.field in setOf("tags","annotation","rating")})
        val opus=OpenOpusAdapter()
        assertTrue(opus.parse(opus.search("Goldberg","all","en").single(),read("2-openopus.json")).isNotEmpty())
        val wiki=WikipediaAdapter()
        assertTrue(wiki.licensedDocument(read("3-wikipedia.json")))
        assertTrue(wiki.licensedDocument(read("4-wikipedia.json")))
    }
}
