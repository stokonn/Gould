package com.opus.piano.admin

import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class AdminQueueTest {
    private class Fixture {
        var version = 1
        var fail = false
        val calls = mutableListOf<String>()
        var page: (String) -> String = { """{"items":[${item("sub_a")}]}""" }
        var detail: (String) -> String = { id -> """{"id":"$id","owner":"u","status":"submitted","revision":$version,"revisions":[{"revision":$version,"payload":{"target":{"kind":"work"},"fields":[{"field":"title","newValue":"Chopin"}]}}]}""" }
        val markers = AdminMarkers(InMemoryAdminMarkerStore()) { "account" }
        val api = AdminApi({ "https://test.invalid" }, AdminTransport { _, url, _, _ ->
            calls += url
            if (fail) AdminHttpResponse(503, "{}".toByteArray(), null)
            else AdminHttpResponse(200, (if (url.contains("/queue")) page(url) else detail(url.substringAfterLast('/'))).toByteArray(), null)
        })
        fun service(policy: SearchPolicy = SearchPolicy()) = AdminQueueService(api, markers, policy)
        fun item(id: String) = """{"id":"$id","owner":"u","status":"submitted","revision":$version,"createdAt":1}"""
    }
    @Test fun duplicateIdsWithinAndAcrossPagesAreMerged() = runBlocking {
        val f=Fixture()
        f.page={ url -> if (url.contains("cursor=")) """{"items":[${f.item("sub_a")},${f.item("sub_b")}]}"""
        else """{"items":[${f.item("sub_a")},${f.item("sub_a")}],"nextCursor":"c"}""" }
        val s=f.service(); s.initialLoad(); s.loadMore()
        assertEquals(listOf("sub_a","sub_b"),s.state.value.rows.map{it.id})
    }
    @Test fun existingUnloadedRowsDoNotCreateFalseUpdatePill() = runBlocking {
        val f=Fixture()
        f.page={ url -> if(url.contains("cursor=")) """{"items":[${f.item("sub_b")}]}"""
        else """{"items":[${f.item("sub_a")}],"nextCursor":"c"}""" }
        val s=f.service(); s.initialLoad(); s.revalidate(); s.revalidate()
        assertEquals(0,s.state.value.pendingUpdates)
    }
    @Test fun revisionChangeCreatesPillWithoutReplacingVisibleRows() = runBlocking {
        val f=Fixture(); val s=f.service(); s.initialLoad(); f.version=2; s.revalidate()
        assertEquals(1,s.state.value.pendingUpdates); assertEquals(1,s.state.value.rows.single().revision)
        s.refresh(); assertEquals(0,s.state.value.pendingUpdates); assertEquals(2,s.state.value.rows.single().revision)
    }
    @Test fun failedApplyPreservesRowsAndUpdatePill() = runBlocking {
        val f=Fixture(); val s=f.service(); s.initialLoad(); f.version=2; s.revalidate(); f.fail=true; s.refresh()
        assertEquals(1,s.state.value.pendingUpdates); assertEquals(1,s.state.value.rows.single().revision)
        assertFalse(s.state.value.loading)
    }
    @Test fun partialApplyPreservesPreviouslyLoadedRows() = runBlocking {
        val f=Fixture(); val s=f.service(); s.initialLoad(); f.version=2; s.revalidate()
        f.page={ """{"items":[${f.item("sub_b")}],"nextCursor":"c"}""" }
        // A repeated cursor is invalid; the whole reload must be abandoned.
        s.refresh(); assertEquals("sub_a",s.state.value.rows.single().id)
        assertEquals(1,s.state.value.pendingUpdates)
    }
    @Test fun incompleteTextSearchNeverReturnsPartialMatches() = runBlocking {
        val f=Fixture(); f.page={ """{"items":[${f.item("sub_a")}],"nextCursor":"more"}""" }
        val s=f.service(SearchPolicy(maxQueuePages=1)); s.search("Chopin")
        assertEquals(AdminSearchState.RequiresServerSearch,s.state.value.search)
        assertEquals(1,f.calls.size)
    }
    @Test fun detailFailureAbortsWholeTextSearch() = runBlocking {
        val f=Fixture(); val api=AdminApi({"https://test.invalid"},AdminTransport { _,url,_,_ ->
            AdminHttpResponse(if(url.contains("/queue"))200 else 503,
                if(url.contains("/queue")) """{"items":[${f.item("sub_a")}]}""".toByteArray() else "{}".toByteArray(),null)
        })
        val s=AdminQueueService(api,f.markers); s.search("Chopin")
        assertTrue(s.state.value.search is AdminSearchState.Failed)
    }
    @Test fun titleSearchHydratesAllEligibleRows() = runBlocking {
        val f=Fixture(); val s=f.service(); s.search("Chopin")
        assertEquals("sub_a",(s.state.value.search as AdminSearchState.Results).rows.single().id)
        assertFalse(f.calls.any { "search=" in it })
    }
    @Test fun explicitIdLookupWorksOutsideQueue() = runBlocking {
        val f=Fixture(); val s=f.service(); s.search("#abc-def")
        assertEquals("abc-def",(s.state.value.search as AdminSearchState.Results).rows.single().id)
        assertEquals(1,f.calls.size)
    }
    @Test fun terminalHydrationClearsPersonalMarkersOnlyAfterServerConfirmation() = runBlocking {
        val f=Fixture(); f.markers.set(AdminMarkerKind.DEFERRED,"sub_a",true)
        f.detail={ """{"id":"sub_a","status":"published","revision":1}""" }
        val s=f.service(); s.ensureMarked()
        assertTrue(f.markers.all(AdminMarkerKind.DEFERRED).isEmpty())
    }
    @Test fun pageBudgetStopsRepeatedManualAppend() = runBlocking {
        val f=Fixture(); f.page={ """{"items":[${f.item("sub_a")}],"nextCursor":"c"}""" }
        val s=f.service(SearchPolicy(maxQueuePages=1)); s.initialLoad(); val calls=f.calls.size
        s.loadMore(); assertEquals(calls,f.calls.size)
    }
    @Test fun clearDropsQueueAndSearchState() = runBlocking {
        val f=Fixture(); val s=f.service(); s.initialLoad(); s.search("Chopin"); s.clear()
        assertEquals(AdminQueueState(),s.state.value)
    }
}
