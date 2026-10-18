package com.opus.piano.data.catalogue
import org.junit.Assert.*
import org.junit.Test
class SearchContractTest {
    @Test fun accentPunctuationAndCatalogueLabels() {
        assertEquals("bwv 988",SearchText.normalize("BWV988"));assertEquals("bwv 988",SearchText.normalize("BWV 988"))
        assertEquals("zlutoucky kun",SearchText.normalize("Žluťoučký kůň"));assertEquals("op 9 2",SearchText.normalize("Op. 9/2"))
        assertTrue(SearchText.near("goldberg","goldbrg"));assertFalse(SearchText.near("goldberg","berg"))
    }
    @Test fun cursorBindingsRejectStaleOrDifferentQueries() {
        val id="12345678-1234-4234-8234-123456789abc";val cursor=SearchCursor("hash","generation",1,id).encode()
        assertEquals(id,SearchCursor.decode(cursor,"hash","generation").lastId)
        assertTrue(runCatching {SearchCursor.decode(cursor,"other-query","generation")}.isFailure)
        assertTrue(runCatching {SearchCursor.decode(cursor,"hash","other-generation")}.isFailure)
        assertTrue(runCatching {SearchCursor.decode("x".repeat(2000),"hash","generation")}.isFailure)
    }
    @Test fun categoriesKeepRecordingsAndEditionsInLinkedDetails() {
        assertEquals(listOf("work","arrangement"),SearchFilter("works").kinds)
        assertFalse("recording" in SearchFilter().kinds);assertFalse("release" in SearchFilter("albums").kinds)
    }
}
