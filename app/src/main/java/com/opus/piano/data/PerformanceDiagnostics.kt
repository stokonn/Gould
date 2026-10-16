package com.opus.piano.data

import android.os.Trace

/** Visible in a system trace. Section names never contain titles, notes or other journal data. */
internal inline fun <T> traceOperation(section: String, block: () -> T): T {
    Trace.beginSection(section)
    return try { block() } finally { Trace.endSection() }
}
