package com.opus.piano.data.catalogue

import org.junit.Assert.*
import java.util.Locale
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Legacy numeric/opaque fallbacks must never render as invented artist or work names. */
class CataloguePresentationTest {
    private fun row(id:String,value:String,language:String="und",field:String="genre",selected:Boolean=true)=SourcedAssertion(
        CatalogueAssertion(id,"entity",field,"{\"value\":\"$value\",\"language\":\"$language\"}","s$id","r$id",selected),
        CatalogueSource("s$id","openopus","work:1",id,"a".repeat(64),"2026-10-25T12:34:56Z","https://api.openopus.org/work/detail/1.json"),
        CatalogueRights("r$id","CC0-1.0","https://creativecommons.org/publicdomain/zero/1.0/","Open Opus metadata","approved"))
    @Test fun groupedEvidencePreservesEveryRevisionConflictLanguageAndSelection() {
        val rows=listOf(row("1","Keyboard"),row("2","Keyboard",selected=false),row("3","Orchestral",selected=false),row("4","Keyboard","en"))
        val groups=CataloguePresentation.groups(rows)
        assertEquals(3,groups.size)
        assertEquals(rows,groups.flatMap {it.evidence})
        assertEquals(2,groups.first().evidence.size)
        assertFalse(groups.first().evidence.last().assertion.selected)
        assertEquals(2,groups.map {it.value}.distinct().size)
    }
    @Test fun malformedHistoricalScalarRemainsEvidenceWithoutCrashingGrouping() {
        val original=row("legacy","Keyboard").let {it.copy(assertion=it.assertion.copy(valueJson="legacy raw evidence"))}
        val group=CataloguePresentation.groups(listOf(original)).single()
        assertEquals("legacy raw evidence",group.value)
        assertEquals(original,group.evidence.single())
    }
    @Test fun preparedDetailKeepsOriginalEvidenceAndDoesNotInventMissingContent() {
        val rows=listOf(row("1","Keyboard"),row("2","Keyboard",selected=false))
        val detail=DiscoverDetail(CatalogueEntity("entity","work","Original title"),emptyList(),rows,emptyList())
        val prepared=CataloguePresentation.prepare(detail,"cs")
        assertEquals("Original title",prepared.title)
        assertEquals(rows,prepared.groups.single().evidence)
        assertNull(prepared.biography);assertNull(prepared.biographyText);assertTrue(prepared.listeningUrls.isEmpty())
        val broken=row("broken","value",field="preferred_name").let {it.copy(assertion=it.assertion.copy(valueJson="unchanged raw legacy evidence"))}
        val recovered=CataloguePresentation.prepare(detail.copy(assertions=rows+broken),"cs")
        assertEquals("Original title",recovered.title)
        assertTrue(recovered.groups.flatMap {it.evidence}.contains(broken))
    }
    @Test fun dateAndLanguageLabelsUseRequestedLocaleWithoutChangingEvidence() {
        assertEquals("English",CataloguePresentation.languageLabel("en",Locale.ENGLISH,"Unspecified"))
        assertEquals("Unspecified",CataloguePresentation.languageLabel("und",Locale.ENGLISH,"Unspecified"))
        val raw="2026-10-25T12:34:56Z"
        assertFalse(CataloguePresentation.retrievedLabel(raw,Locale.ENGLISH).contains("T12:"))
        assertNotEquals(CataloguePresentation.retrievedLabel(raw,Locale.ENGLISH),CataloguePresentation.retrievedLabel(raw,Locale.forLanguageTag("cs")))
        assertEquals("legacy",CataloguePresentation.retrievedLabel("legacy",Locale.ENGLISH))
    }
    @Test fun legacyNumericAndOpaqueFallbacksAreNeverPresentedAsNames() {
        // A numeric legacy composer identifier is a label, not a name.
        assertTrue(CataloguePresentation.unnamed("artist", "87"))
        assertTrue(CataloguePresentation.unnamed("artist", "007"))
        assertTrue(CataloguePresentation.unnamed("artist", "   "))
        assertFalse(CataloguePresentation.unnamed("artist", "Johann Sebastian Bach"))
        // Numeric-looking real titles of non-artist kinds are preserved verbatim.
        assertFalse(CataloguePresentation.unnamed("album", "1984"))
        assertFalse(CataloguePresentation.unnamed("work", "4'33\""))
        assertFalse(CataloguePresentation.unnamed("collection", "2026"))
        // Personal copy falls back to a blank title or the opaque identity key.
        assertTrue(CataloguePresentation.unnamed("work", ""))
        assertTrue(CataloguePresentation.unnamed("work", "12345678-1234-4234-8234-123456789abc"))
        assertTrue(CataloguePresentation.unnamed("collection", " 12345678-1234-4234-8234-123456789abc "))
        assertFalse(CataloguePresentation.unnamed("collection", "12345678-1234-4234-8234-123456789abz"))
        assertFalse(CataloguePresentation.unnamed("collection", "Goldberg Variations"))
    }
}
