package com.opus.piano.data.catalogue.providers

import org.junit.Assert.*
import org.junit.Test

class ProviderProjectionTest {
    private val mbid="12345678-1234-4234-8234-123456789abc"
    @Test fun providerContinuationRequiresExplicitProgressingBoundedEvidence() {
        val opus=OpenOpusAdapter();val first=opus.search("Authored","works","en").single()
        assertEquals(20,opus.nextOffset(first,mapOf("next" to 20)))
        assertNull(opus.nextOffset(first,mapOf("status" to mapOf("rows" to 9999))))
        assertTrue(runCatching {opus.nextOffset(first,mapOf("next" to mapOf("nested" to 20)))}.isFailure)
        assertTrue(runCatching {opus.nextOffset(first,mapOf("next" to 0))}.isFailure)
        assertTrue(runCatching {opus.nextOffset(first,mapOf("next" to 10001))}.isFailure)
        val mb=MusicBrainzAdapter();val request=mb.search("Authored","artists","en",20).single()
        assertEquals(20,request.offset)
        assertEquals(22,mb.nextOffset(request,mapOf("artist-count" to 25,"artist-offset" to 20,"artists" to listOf(mapOf("id" to mbid),mapOf("id" to mbid)))))
        assertTrue(runCatching {mb.nextOffset(request,mapOf("artist-count" to 25,"artist-offset" to 0))}.isFailure)
        val wd=WikidataAdapter();val candidate=wd.search("Authored","all","cs",3).single()
        assertTrue(candidate.url.contains("continue=3"));assertEquals(6,wd.nextOffset(candidate,mapOf("search-continue" to 6)))
        assertTrue(runCatching {wd.nextOffset(candidate,mapOf("search-continue" to 3.5))}.isFailure)
    }
    @Test fun musicBrainzProjectsOnlyAllowedScalarFieldsAndAliases() {
        val adapter=MusicBrainzAdapter();val request=adapter.search("Björk","artists","cs").single()
        assertTrue(request.url.contains("limit=20"));assertFalse(request.url.contains("inc=tags"))
        val row=mapOf("id" to mbid,"name" to "Björk","type" to "Person","tags" to listOf(mapOf("name" to "private")),"annotation" to "blocked", "country" to mapOf("malicious" to "nested"), "aliases" to listOf(mapOf("name" to "Björk Guðmundsdóttir","locale" to "is","unknown" to "not retained")))
        val result=adapter.parse(request,mapOf("artists" to listOf(row))).single()
        assertEquals("Björk",result.title);assertEquals(listOf("type"),result.fields.map {it.field});assertEquals("Björk Guðmundsdóttir",result.aliases.single().second)
        assertFalse(result.toString().contains("private"));assertFalse(result.toString().contains("malicious"))
    }
    @Test fun invalidAndMissingTypedIdsNeverBecomeAcceptedEntities() {
        val adapter=MusicBrainzAdapter();val request=adapter.search("Same title","works","en").single()
        assertTrue(adapter.parse(request,mapOf("works" to listOf(mapOf("title" to "No ID"),mapOf("id" to "123","title" to "Bad ID")))).isEmpty())
        val results=adapter.parse(request,mapOf("works" to listOf(mapOf("id" to mbid,"title" to "Same title"),mapOf("id" to mbid.replaceFirst('1','2'),"title" to "Same title"))))
        assertEquals(2,results.size);assertNotEquals(results[0].externalId,results[1].externalId)
    }
    @Test fun openOpusIgnoresAdvertisedRowCountAndUnlicensedPortrait() {
        val adapter=OpenOpusAdapter();val request=adapter.search("Goldberg","all","en").single()
        val row=mapOf("composer" to mapOf("id" to "87","name" to "Bach","complete_name" to "Johann Sebastian Bach","portrait" to "blocked"),"work" to mapOf("id" to "10074","title" to "Goldberg Variations, BWV 988","searchterms" to "Goldbergovy variace"))
        val result=adapter.parse(request,mapOf("status" to mapOf("success" to "true","rows" to 999),"results" to listOf(row)))
        assertEquals(2,result.size);assertFalse(result.toString().contains("blocked"));assertEquals("87",result.last().credits.single().id)
    }
    @Test fun jsonErrorsAreFailuresEvenUnderHttp200() {
        assertEquals(SourceState.RATE_LIMITED,ProviderErrors.classify(200,mapOf("error" to mapOf("code" to "maxlag"))))
        assertEquals(SourceState.INVALID,ProviderErrors.classify(200,mapOf("error" to mapOf("code" to "badvalue"))))
        assertEquals(SourceState.RATE_LIMITED,ProviderErrors.classify(503,emptyMap()))
        assertEquals(SourceState.MISSING,ProviderErrors.classify(404,emptyMap()))
        assertEquals(SourceState.UNAVAILABLE,ProviderErrors.classify(302,emptyMap()))
        assertNull(ProviderErrors.classify(200,emptyMap()))
    }
    @Test fun retryAfterSupportsSecondsDatesAndOverflow() {
        assertEquals(3000,ProviderErrors.retryAfter("3",0));assertEquals(0,ProviderErrors.retryAfter("invalid",0))
        assertEquals(1000,ProviderErrors.retryAfter("Thu, 01 Jan 1970 00:00:01 GMT",0))
        assertEquals(Long.MAX_VALUE,ProviderErrors.retryAfter(Long.MAX_VALUE.toString(),0))
    }
    @Test fun wikidataNamesAloneCannotEstablishMusicIdentity() {
        val adapter=WikidataAdapter();val request=adapter.lookup("Q123")
        assertTrue(adapter.parse(request,mapOf("entities" to mapOf("Q123" to mapOf("labels" to mapOf("en" to mapOf("value" to "Bach")))))).isEmpty())
        val entity=mapOf("lastrevid" to 123,"labels" to mapOf("cs" to mapOf("value" to "Příklad"),"en" to mapOf("value" to "Example")),"claims" to mapOf("P435" to listOf(mapOf("rank" to "normal","mainsnak" to mapOf("snaktype" to "value","datavalue" to mapOf("value" to mbid))))))
        val parsed=adapter.parse(request,mapOf("entities" to mapOf("Q123" to entity))).single()
        assertEquals("work",parsed.kind);assertEquals("work/$mbid",parsed.fields.first {it.field=="verified_musicbrainz_identity"}.value)
    }
    @Test fun unsupportedCommonsRightsAreBlocked() {
        val adapter=CommonsAdapter()
        fun meta(license:String,url:String)=mapOf("Artist" to mapOf("value" to "Fixture author"),"LicenseShortName" to mapOf("value" to license),"LicenseUrl" to mapOf("value" to url))
        assertFalse(adapter.approved(meta("Public domain","")));assertFalse(adapter.approved(meta("CC BY-NC 4.0","https://creativecommons.org/licenses/by-nc/4.0/")))
        assertTrue(adapter.approved(meta("CC BY-SA 4.0","https://creativecommons.org/licenses/by-sa/4.0/")))
        assertFalse(adapter.approved(meta("CC BY-SA 4.0","https://creativecommons.org/licenses/by/3.0/")))
    }
    @Test fun wikipediaRequiresBodyBoundRevisionAndLicense() {
        val adapter=WikipediaAdapter();assertFalse(adapter.licensedDocument(mapOf("html" to "<p>text</p>")))
        assertTrue(adapter.licensedDocument(mapOf("html" to "<p>Authored fixture</p>","latest" to mapOf("id" to 12),"license" to mapOf("url" to "https://creativecommons.org/licenses/by-sa/4.0/"))))
    }
    @Test fun wikipediaRecognizesVerifiedEditionDeedsButRejectsOtherLicensesAndHosts() {
        val adapter=WikipediaAdapter()
        for(suffix in listOf("","/","/deed.en","/deed.cs"))assertTrue(adapter.licensedDocument(mapOf("html" to "<p>Fixture</p>","latest" to mapOf("id" to 12),"license" to mapOf("url" to "https://creativecommons.org/licenses/by-sa/4.0"+suffix))))
        for(url in listOf("https://creativecommons.org.attacker.example/licenses/by-sa/4.0/","https://creativecommons.org/licenses/by-nc/4.0/","https://creativecommons.org/licenses/by-sa/4.0/deed.en?redirect=unknown"))assertFalse(adapter.licensedDocument(mapOf("html" to "fixture","latest" to mapOf("id" to 12),"license" to mapOf("url" to url))))
    }

}
