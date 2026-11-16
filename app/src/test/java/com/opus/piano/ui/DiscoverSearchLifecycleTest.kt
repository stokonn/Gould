package com.opus.piano.ui

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoverSearchLifecycleTest {
    private data class Request(val query:String,val category:String,val online:Boolean)
    private data class State(val query:String="",val category:String="all",val rows:List<String> = emptyList(),val loading:Boolean=false,val error:Boolean=false)
    private data class Reply(val rows:List<String>,val fail:Boolean=false,val started:CompletableDeferred<Unit> = CompletableDeferred(),
        val ingesting:CompletableDeferred<Unit> = CompletableDeferred(),val release:CompletableDeferred<Unit> = CompletableDeferred(),
        val finishIngestion:CompletableDeferred<Unit> = CompletableDeferred())
    private class FakeRepository {
        val calls=mutableListOf<Request>()
        val replies=mutableMapOf<String,Reply>()
        val cached=mutableMapOf<String,List<String>>()
        suspend fun search(request:Request,emit:(List<String>)->Unit) {
            calls+=request
            cached[request.category]?.let {emit(it);return}
            val reply=replies[request.category]
            if(reply==null && !request.online){emit(listOf("offline-${request.category}"));return}
            requireNotNull(reply)
            reply.started.complete(Unit)
            reply.release.await()
            reply.ingesting.complete(Unit)
            reply.finishIngestion.await()
            if(reply.fail)error("provider failed")
            emit(reply.rows)
        }
    }

    private suspend fun launchSearch(lifecycle:DiscoverSearchLifecycle,repository:FakeRepository,state:MutableList<State>,request:Request)=
        lifecycle.run(
            onStart={state[0]=State(request.query,request.category,loading=true)},
            execute={emit->repository.search(request,emit)},
            onResult={_,rows->state[0]=state[0].copy(rows=rows)},
            onFailure={_,_->state[0]=state[0].copy(error=true)},
            onFinish={state[0]=state[0].copy(loading=false)}
        )

    @Test fun allToArtistsWhileLocalSearchRunsRetainsQueryAndCompletesReplacement()=runTest {
        val lifecycle=DiscoverSearchLifecycle();val repository=FakeRepository();val state= mutableListOf(State())
        val old=Reply(listOf("old-all"));repository.replies["all"]=old
        val first=launch {launchSearch(lifecycle,repository,state,Request("Chopin","all",false))}
        runCurrent();old.started.await()
        first.cancel();first.join()
        launchSearch(lifecycle,repository,state,Request("Chopin","artists",false))
        runCurrent()
        assertEquals(Request("Chopin","artists",false),repository.calls.last())
        assertEquals("Chopin",state[0].query)
        assertEquals("artists",state[0].category)
        assertEquals(listOf("offline-artists"),state[0].rows)
        assertFalse(state[0].loading)
    }

    @Test fun allToWorksCancelsOnlineRequestAndReplacementCompletes()=runTest {
        val lifecycle=DiscoverSearchLifecycle();val repository=FakeRepository();val state= mutableListOf(State())
        val old=Reply(listOf("old-all"));val next=Reply(listOf("work-result"));repository.replies["all"]=old;repository.replies["works"]=next
        val first=launch {launchSearch(lifecycle,repository,state,Request("Goldberg","all",true))}
        runCurrent();old.started.await();first.cancel();first.join()
        val replacement=launch {launchSearch(lifecycle,repository,state,Request("Goldberg","works",true))}
        runCurrent();next.started.await();next.release.complete(Unit);runCurrent();next.finishIngestion.complete(Unit);runCurrent();replacement.join()
        assertEquals(listOf("Goldberg" to "all","Goldberg" to "works"),repository.calls.map {it.query to it.category})
        assertEquals(listOf("work-result"),state[0].rows)
        assertFalse(state[0].loading)
    }

    @Test fun worksToArtistsDuringIngestionCannotOverwriteOrClearNewLoading()=runTest {
        val lifecycle=DiscoverSearchLifecycle();val repository=FakeRepository();val state= mutableListOf(State())
        val old=Reply(listOf("stale-work"));val next=Reply(listOf("artist-result"));repository.replies["works"]=old;repository.replies["artists"]=next
        val first=launch {launchSearch(lifecycle,repository,state,Request("Bach","works",true))}
        runCurrent();old.started.await();old.release.complete(Unit);runCurrent();old.ingesting.await()
        val replacement=launch {launchSearch(lifecycle,repository,state,Request("Bach","artists",true))}
        runCurrent();next.started.await()
        first.cancel();runCurrent()
        assertTrue("obsolete cleanup must not clear replacement loading",state[0].loading)
        old.finishIngestion.complete(Unit);next.release.complete(Unit);runCurrent();next.finishIngestion.complete(Unit);runCurrent();first.join();replacement.join()
        assertEquals(listOf("artist-result"),state[0].rows)
        assertFalse(state[0].loading)
    }

    @Test fun rapidAllWorksArtistsSwitchKeepsOnlyLatestResult()=runTest {
        val lifecycle=DiscoverSearchLifecycle();val repository=FakeRepository();val state= mutableListOf(State())
        val replies=listOf("all","works","artists").associateWith {Reply(listOf("result-$it"))};repository.replies.putAll(replies)
        val jobs=mutableListOf<kotlinx.coroutines.Job>()
        for(category in listOf("all","works","artists")) {
            jobs.lastOrNull()?.cancel()
            jobs+=launch {launchSearch(lifecycle,repository,state,Request("Mozart",category,true))}
            runCurrent();replies.getValue(category).started.await()
        }
        replies.getValue("artists").release.complete(Unit);runCurrent();replies.getValue("artists").finishIngestion.complete(Unit);runCurrent()
        jobs.forEach {it.cancel();it.join()}
        assertEquals(listOf("Mozart" to "all","Mozart" to "works","Mozart" to "artists"),repository.calls.map {it.query to it.category})
        assertEquals(listOf("result-artists"),state[0].rows)
        assertFalse(state[0].loading)
    }

    @Test fun lateObsoleteEmissionCannotOverwriteOrFinishNewRequest()=runTest {
        val lifecycle=DiscoverSearchLifecycle();val state= mutableListOf(State());val oldResume=CompletableDeferred<Unit>();val newResume=CompletableDeferred<Unit>()
        val old=launch {
            lifecycle.run<String>(
                onStart={state[0]=State("query","all",loading=true)},
                execute={emit->withContext(NonCancellable){oldResume.await();emit("stale")}},
                onResult={_,rows->state[0]=state[0].copy(rows=listOf(rows))},
                onFailure={_,_->state[0]=state[0].copy(error=true)},
                onFinish={state[0]=state[0].copy(loading=false)}
            )
        }
        runCurrent()
        val replacement=launch {
            lifecycle.run<String>(
                onStart={state[0]=State("query","artists",loading=true)},
                execute={emit->newResume.await();emit("artist")},
                onResult={_,rows->state[0]=state[0].copy(rows=listOf(rows))},
                onFailure={_,_->state[0]=state[0].copy(error=true)},
                onFinish={state[0]=state[0].copy(loading=false)}
            )
        }
        runCurrent();old.cancel();oldResume.complete(Unit);runCurrent()
        assertTrue(state[0].loading);assertTrue(state[0].rows.isEmpty())
        newResume.complete(Unit);runCurrent();replacement.join();old.join()
        assertEquals(listOf("artist"),state[0].rows);assertFalse(state[0].loading)
    }

    @Test fun categorySwitchWorksOfflineAndUsesCachedReplacement()=runTest {
        val lifecycle=DiscoverSearchLifecycle();val repository=FakeRepository();val state= mutableListOf(State())
        launchSearch(lifecycle,repository,state,Request("Chopin","works",false))
        assertEquals(listOf("offline-works"),state[0].rows)
        repository.cached["artists"]=listOf("cached-artist")
        launchSearch(lifecycle,repository,state,Request("Chopin","artists",true))
        assertEquals(Request("Chopin","artists",true),repository.calls.last())
        assertEquals(listOf("cached-artist"),state[0].rows)
        assertFalse(state[0].loading)
    }

    @Test fun failureAndCancellationReachTerminalStateAndNextSearchSucceeds()=runTest {
        val lifecycle=DiscoverSearchLifecycle();val repository=FakeRepository();val state= mutableListOf(State())
        val failure=Reply(emptyList(),fail=true);repository.replies["all"]=failure
        val failed=launch {launchSearch(lifecycle,repository,state,Request("query","all",true))}
        runCurrent();failure.started.await();failure.release.complete(Unit);runCurrent();failure.finishIngestion.complete(Unit);runCurrent();failed.join()
        assertTrue(state[0].error);assertFalse(state[0].loading)
        val cancelled=Reply(listOf("obsolete"));val next=Reply(listOf("latest"));repository.replies["works"]=cancelled;repository.replies["artists"]=next
        val old=launch {launchSearch(lifecycle,repository,state,Request("query","works",true))}
        runCurrent();cancelled.started.await();old.cancel();old.join()
        val replacement=launch {launchSearch(lifecycle,repository,state,Request("query","artists",true))}
        runCurrent();next.release.complete(Unit);runCurrent();next.finishIngestion.complete(Unit);runCurrent();replacement.join()
        assertEquals(listOf("latest"),state[0].rows);assertFalse(state[0].loading);assertFalse(state[0].error)
    }
}
