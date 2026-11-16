package com.opus.piano.ui

import com.opus.piano.data.catalogue.CatalogueEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ArtistWorkProjectionTest {
    @Test fun titleSortIsStableAndTypeFilterComposesWithIt() {
        val rows=listOf(CatalogueEntity("2","work","Éclair"),CatalogueEntity("1","work",""),CatalogueEntity("3","work","Éclair"))
        val types=mapOf("2" to "song","1" to "study","3" to "song")

        assertEquals(listOf("1","2","3"),artistWorkProjection(rows,types,"All",false).map {it.id})
        assertEquals(listOf("2","3"),artistWorkProjection(rows,types,"song",false).map {it.id})
        assertEquals(listOf("2","3","1"),artistWorkProjection(rows,types,"All",true).map {it.id})
    }
}
