package com.opus.piano.data.catalogue

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** One application-wide admission boundary for accepted rows and disposable cache publication.
 * The estimate includes staging alongside the prior valid revision. No personal writes use it. */
class CatalogueIntake(private val admits:(Long)->Boolean={true}) {
    private val gate=Mutex()
    suspend fun <T> commit(estimatedBytes:Long,write:suspend ()->T):T=gate.withLock {
        require(estimatedBytes>=0 && admits(estimatedBytes)){"Catalogue storage budget"}
        currentCoroutineContext().ensureActive()
        write()
    }
}
