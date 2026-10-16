package com.opus.piano.data

import androidx.room.withTransaction
import com.opus.piano.practice.PracticeState
import org.json.JSONArray
import org.json.JSONObject

class PracticeRepository(private val db: OpusDatabase, private val journal: OpusRepository) : com.opus.piano.practice.PracticePersistence {
    override suspend fun recoveryData(): String? = db.practiceDao().draft()?.payload
    override suspend fun recover(): PracticeState? = db.practiceDao().draft()?.let {
        decode(it.payload).let { s -> s.copy(running = false, status = if (s.status == "Review") "Review" else "Interrupted", timingQuality = "Incomplete", ready = true) }
    }
    override suspend fun checkpoint(state: PracticeState) {
        db.practiceDao().checkpoint(PracticeDraft(payload = encode(state), updatedAt = System.currentTimeMillis()))
    }
    override suspend fun clear() = db.practiceDao().clearDraft()
    override suspend fun complete(state: PracticeState): Long = db.withTransaction {
        val s = state
        db.practiceDao().completed(s.token)?.let { db.practiceDao().clearDraft(); return@withTransaction it.id }
        val parts = s.parts // Keep note-only entries and their stable interval indexes.
        journal.complete(PracticeSession(pieceId = null, pieceTitle = parts.filter { it.durationMillis > 0 || it.notes.isNotBlank() }.joinToString(" · ") { it.title }.ifBlank { "Practice notes" },
            startedAt = s.startedAt, completedAt = System.currentTimeMillis(), durationMillis = s.elapsed,
            elapsedMillis = s.wallElapsed, detectedActiveMillis = s.detectedActive.takeIf { s.mode != "Manual" },
            practiceMode = s.mode, timingQuality = s.timingQuality, notes = s.notes, reflection = s.reflection, completionToken = s.token), parts)
        val id = requireNotNull(db.practiceDao().completed(s.token)).id
        db.practiceDao().intervals(s.intervals.map { it.copy(id = 0, sessionId = id) })
        db.practiceDao().clearDraft()
        id
    }
    suspend fun editNotes(id: Long, notes: String, reflection: String, parts: List<SessionPart>) = db.withTransaction {
        requireNotNull(db.practiceDao().session(id)) { "Session no longer exists." }
        db.practiceDao().notes(id, notes.take(10000), reflection.take(10000))
        parts.forEach { db.practiceDao().partNote(id, it.id, it.notes.take(10000)) }
    }
    companion object {
        fun encode(s: PracticeState): String = JSONObject().apply {
            put("execution", s.execution); put("version", 1); put("token", s.token); put("start", s.startedAt); put("credited", s.elapsed); put("elapsed", s.wallElapsed)
            put("detected", s.detectedActive); put("mode", s.mode); put("smart", s.smart); put("active", s.active)
            put("notes", s.notes); put("reflection", s.reflection); put("sensitivity", s.sensitivity); put("pause", s.pauseSeconds)
            put("status", s.status); put("quality", s.timingQuality); put("removed", JSONArray(s.removed.toList()))
            put("parts", JSONArray().apply { s.parts.forEach { p -> put(JSONObject().apply {
                put("piece", p.pieceId ?: JSONObject.NULL); put("title", p.title); put("composer", p.composer)
                put("catalog", p.catalogWorkId ?: JSONObject.NULL); put("era", p.musicalEra); put("duration", p.durationMillis); put("notes", p.notes)
            }) } })
            put("intervals", JSONArray().apply { s.intervals.forEach { r -> put(JSONObject().apply {
                put("part", r.partIndex); put("start", r.startMillis); put("end", r.endMillis); put("credit", r.creditedMillis); put("reason", r.reason)
            }) } })
        }.toString()
        fun decode(raw: String): PracticeState {
            require(raw.length <= 16 * 1024 * 1024)
            val o = JSONObject(raw); require(o.getInt("version") == 1)
            val parts = o.getJSONArray("parts").objects().map { p -> SessionPart(pieceId = if (p.isNull("piece")) null else p.getLong("piece"), title = p.getString("title"), composer = p.getString("composer"), catalogWorkId = if (p.isNull("catalog")) null else p.getString("catalog"), musicalEra = p.getString("era"), durationMillis = p.getLong("duration"), notes = p.getString("notes")) }
            require(parts.isNotEmpty() && parts.all { it.durationMillis >= 0 })
            val rows = o.getJSONArray("intervals").objects().map { r -> SessionInterval(partIndex = r.getInt("part"), startMillis = r.getLong("start"), endMillis = r.getLong("end"), creditedMillis = r.getLong("credit"), reason = r.getString("reason")) }
            val s = PracticeState(execution = o.optLong("execution",0), status = o.optString("status", "Interrupted"), token = o.getString("token"), startedAt = o.getLong("start"), elapsed = o.getLong("credited"), wallElapsed = o.getLong("elapsed"), detectedActive = o.getLong("detected"), mode = o.getString("mode"), smart = o.getBoolean("smart"), active = o.getInt("active"), notes = o.getString("notes"), reflection = o.getString("reflection"), sensitivity = o.getInt("sensitivity"), pauseSeconds = o.getInt("pause"), timingQuality = o.getString("quality"), parts = parts, intervals = rows,
                removed = o.getJSONArray("removed").let { a -> (0 until a.length()).map { a.getLong(it) }.toSet() })
            require(s.active in parts.indices && parts.sumOf { it.durationMillis } == s.elapsed)
            validateTiming(s.elapsed, s.wallElapsed, s.detectedActive.takeIf { s.mode != "Manual" }, s.mode, rows, parts.size)
            return s
        }
        fun validateTiming(credited: Long, elapsed: Long?, detected: Long?, mode: String, rows: List<SessionInterval>, partCount: Int) {
            require(mode in listOf("Manual", "Smart", "Mixed"))
            if (mode == "Manual") require(detected == null)
            if (mode != "Manual") require(detected != null)
            require(credited >= 0 && (elapsed == null || elapsed >= credited) && (detected == null || detected in 0..credited))
            if (mode == "Smart") require(detected == credited)
            require(rows.size <= 100000)
            var previous = 0L
            rows.forEach { r ->
                require(r.partIndex in 0 until partCount && r.startMillis == previous && r.endMillis >= r.startMillis)
                require(r.creditedMillis in 0..(r.endMillis - r.startMillis))
                require(r.reason in listOf("Detected", "Manual", "Silence", "ManualPause", "Interruption"))
                previous = r.endMillis
            }
            if (rows.isNotEmpty()) {
                require(rows.fold(0L) { a, r -> Math.addExact(a, r.creditedMillis) } == credited && previous == elapsed)
                require(rows.filter { it.reason == "Detected" }.sumOf { it.creditedMillis } == (detected ?: 0))
            }
        }
    }
}
