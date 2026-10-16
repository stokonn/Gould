package com.opus.piano.data

import androidx.room.withTransaction
import java.io.Writer
import java.time.Instant

object PracticeCsv {
    val sessionColumns = listOf("session_id","started_at_utc","completed_at_utc","practice_day","practice_zone","mode","timing_quality","credited_ms","elapsed_ms","detected_active_ms","manual_pause_ms","auto_pause_ms","interruption_ms","piece_count","session_notes","reflection")
    val partColumns = listOf("session_id","part_id","practice_day","piece_id","catalog_work_id","title","composer","credited_ms","piece_session_notes")
    fun field(value: Any?): String {
        if (value == null) return ""
        if (value is Number) return value.toString()
        var text = value.toString()
        if (text.trimStart().firstOrNull() in listOf('=','+','-','@') || text.firstOrNull() in listOf('\t','\r','\n')) text = "'" + text
        return "\"${text.replace("\"","\"\"")}\""
    }
    fun row(writer: Writer, values: List<Any?>) { writer.write(values.joinToString(",", transform = ::field)); writer.write("\r\n") }
}
class CsvExportRepository(private val db: OpusDatabase) {
    suspend fun export(writer: Writer, pieces: Boolean) = db.withTransaction {
        writer.write("\uFEFF")
        PracticeCsv.row(writer,if (pieces) PracticeCsv.partColumns else PracticeCsv.sessionColumns)
        val sessions = db.dao().allSessions().sortedBy { it.id }
        val parts = db.personalDao().allParts().groupBy { it.sessionId }
        val intervals = db.practiceDao().allIntervals().groupBy { it.sessionId }
        sessions.forEach { s ->
            if (pieces) parts[s.id].orEmpty().forEach { p -> PracticeCsv.row(writer,listOf(s.id,p.id,s.practiceDay,p.pieceId,p.catalogWorkId,p.title,p.composer,p.durationMillis,p.notes)) }
            else {
                fun paused(reason: String): Long? = if (s.elapsedMillis == null) null else intervals[s.id].orEmpty().filter { it.reason == reason }.sumOf { it.endMillis - it.startMillis - it.creditedMillis }
                PracticeCsv.row(writer,listOf(s.id,Instant.ofEpochMilli(s.startedAt),Instant.ofEpochMilli(s.completedAt),s.practiceDay,s.practiceZone,s.practiceMode,s.timingQuality,s.durationMillis,s.elapsedMillis,s.detectedActiveMillis,paused("ManualPause"),paused("Silence"),paused("Interruption"),parts[s.id].orEmpty().count { it.durationMillis > 0 },s.notes,s.reflection))
            }
        }
        writer.flush()
    }
}
