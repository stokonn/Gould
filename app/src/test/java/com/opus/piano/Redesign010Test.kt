package com.opus.piano

import com.opus.piano.data.*
import com.opus.piano.identity.IdentityPolicy
import org.junit.Assert.*
import org.junit.Test

class Redesign010Test {
    @Test fun artistSuggestionsMatchLocalNamesAndAccents() {
        val local=listOf(CatalogComposer("d","Antonín Dvořák"),CatalogComposer("b","Johann Sebastian Bach"),CatalogComposer("c","Carl Philipp Emanuel Bach"))
        assertEquals(listOf("d"),composerSuggestions(local,"antonin dvorak").map {it.id})
        assertEquals(2,composerSuggestions(local,"bach").size)
        assertTrue(composerSuggestions(local,"Mozart").isEmpty())
        assertTrue(composerSuggestions(local,"").isEmpty())
        assertEquals(8,composerSuggestions((1..20).map {CatalogComposer("$it","Composer $it")},"composer").size)
    }
    @Test fun accountChangeCannotOverwriteCustomNickname() {
        assertEquals("My name",IdentityPolicy.name("My name","Old account","New account",true))
        assertEquals("Local legacy name",IdentityPolicy.name("Local legacy name",null,"Google",false))
        assertEquals("New account",IdentityPolicy.name("Old account","Old account","New account",false))
    }
}
