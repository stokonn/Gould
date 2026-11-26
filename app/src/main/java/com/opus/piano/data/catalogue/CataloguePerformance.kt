package com.opus.piano.data.catalogue

import android.os.SystemClock
import android.os.Trace
import android.util.Log
import kotlinx.coroutines.CancellationException

/** Timings only; never includes search text, repertoire content, IDs, or provider payloads. */
internal object CataloguePerformance {
    private const val TAG = "GouldCataloguePerf"

    data class Context(
        val sessionId: String = "startup",
        val source: String = "none",
        val category: String = "none"
    ) {
        fun fields():String {
            val safeSession=if(sessionId=="startup" || runCatching {java.util.UUID.fromString(sessionId)}.isSuccess)sessionId else "invalid"
            val safeSource=source.takeIf {it in setOf("none","local","local_cache","online","local_online")} ?: "invalid"
            val safeCategory=category.takeIf {it in setOf("none","all","works","artists","albums","lists")} ?: "invalid"
            return "session_id=$safeSession source=$safeSource category=$safeCategory"
        }
    }

    fun start(): Long = SystemClock.elapsedRealtimeNanos()

    fun event(phase: String, details: String, context: Context = Context()) {
        Log.i(TAG, "phase=$phase ${context.fields()} $details")
    }

    fun finish(phase: String, startedAt: Long, details: String = "", context: Context = Context()) {
        val elapsedMs = (SystemClock.elapsedRealtimeNanos() - startedAt) / 1_000_000.0
        Log.i(TAG, "phase=$phase duration_ms=${"%.3f".format(java.util.Locale.ROOT, elapsedMs)} ${context.fields()} $details".trim())
    }

    fun <T> measure(phase: String, details: String = "", context: Context = Context(), block: () -> T): T {
        val startedAt = start()
        Trace.beginSection("catalogue:$phase")
        var outcome = "error"
        try {
            return block().also { outcome = "ok" }
        } catch(e: CancellationException) {
            outcome = "cancelled"
            throw e
        } finally {
            Trace.endSection()
            finish(phase, startedAt, "outcome=$outcome $details", context)
        }
    }

    suspend fun <T> measureSuspend(phase: String, details: String = "", context: Context = Context(), block: suspend () -> T): T {
        val startedAt = start()
        var outcome = "error"
        try {
            return block().also { outcome = "ok" }
        } catch(e: CancellationException) {
            outcome = "cancelled"
            throw e
        } finally {
            finish(phase, startedAt, "outcome=$outcome $details", context)
        }
    }
}
