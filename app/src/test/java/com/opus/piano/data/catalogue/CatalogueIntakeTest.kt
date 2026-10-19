package com.opus.piano.data.catalogue

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class CatalogueIntakeTest {
    @Test fun concurrentAcceptedAndCachePublicationsRecheckAggregateUsage()=runBlocking {
        var used=0L;val admitted=mutableListOf<Long>()
        val intake=CatalogueIntake {bytes->admitted.add(used);used+bytes<=100}
        val entered=CompletableDeferred<Unit>();val release=CompletableDeferred<Unit>()
        val accepted=async {intake.commit(70){entered.complete(Unit);release.await();used+=70}}
        entered.await()
        val cache=async {runCatching {intake.commit(40){used+=40}}}
        yield();release.complete(Unit);accepted.await()
        assertTrue(cache.await().isFailure);assertEquals(70L,used);assertEquals(listOf(0L,70L),admitted)
    }
    @Test fun cancellationBeforePublicationAndFailedWritesReleaseAdmission()=runBlocking {
        val intake=CatalogueIntake();val entered=CompletableDeferred<Unit>();val release=CompletableDeferred<Unit>();var writes=0
        val owner=launch {intake.commit(1){entered.complete(Unit);release.await()}}
        entered.await()
        val waiter=launch {intake.commit(1){writes++}};yield();waiter.cancelAndJoin();release.complete(Unit);owner.join()
        assertEquals(0,writes)
        assertTrue(runCatching {intake.commit(1){error("Failed staging")}}.isFailure)
        intake.commit(1){writes++};assertEquals(1,writes)
        assertTrue(runCatching {intake.commit(-1){writes++}}.isFailure);assertEquals(1,writes)
    }
}
