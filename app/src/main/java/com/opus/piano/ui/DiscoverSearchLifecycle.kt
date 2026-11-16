package com.opus.piano.ui

import kotlinx.coroutines.CancellationException

/** Owns loading, errors and emissions for the newest catalogue request only. */
internal class DiscoverSearchLifecycle {
    private var generation=0L

    fun invalidate():Long = ++generation

    suspend fun <T> run(
        onStart:(Long)->Unit,
        execute:suspend (emit:(T)->Unit)->Unit,
        onResult:(Long,T)->Unit,
        onFailure:(Long,Exception)->Unit,
        onFinish:(Long)->Unit
    ) {
        val request=++generation
        fun current()=request==generation
        try {
            onStart(request)
            execute {value->if(current())onResult(request,value)}
        } catch(cancelled:CancellationException) {
            throw cancelled
        } catch(error:Exception) {
            if(current())onFailure(request,error)
        } finally {
            if(current())onFinish(request)
        }
    }
}
