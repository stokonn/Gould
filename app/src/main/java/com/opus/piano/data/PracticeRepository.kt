package com.opus.piano.data

import androidx.room.withTransaction
import com.opus.piano.practice.PracticeState
import org.json.JSONArray
import org.json.JSONObject

class PracticeRepository(private val db: OpusDatabase, private val journal: OpusRepository) : com.opus.piano.practice.PracticePersistence {
    private var recoveredOriginal:ByteArray?=null
    override suspend fun archiveRecovery(expected:String)=archiveRecoveryBytes(expected.toByteArray(Charsets.UTF_8))
    override suspend fun archiveRecoveryBytes(expected:ByteArray)=db.withTransaction {
        val original=requireNotNull(BackupState.rawDraft(db));check(original.first.contentEquals(expected))
        BackupState.preserve(db,original.first,original.second,"recovery-export","archived_after_export")
        db.openHelper.writableDatabase.execSQL("INSERT INTO practice_recovery_archives(payload,updatedAt,archivedAt) VALUES(CAST(? AS TEXT),?,?)",arrayOf(original.first,original.second,System.currentTimeMillis()))
        db.practiceDao().clearDraft()
    }
    override suspend fun recoveryBytes():ByteArray?=kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO){BackupState.rawDraft(db)?.first}
    override suspend fun recoveryData():String?=recoveryBytes()?.toString(Charsets.UTF_8)
    override suspend fun recover():PracticeState?=db.withTransaction {
        val original=BackupState.rawDraft(db) ?: return@withTransaction null
        val text=Charsets.UTF_8.newDecoder().onMalformedInput(java.nio.charset.CodingErrorAction.REPORT).decode(java.nio.ByteBuffer.wrap(original.first)).toString()
        val s=decode(text)
        require((s.token.isNotBlank() || (s.token.isEmpty() && s.elapsed==0L && s.wallElapsed==0L && s.detectedActive==0L && s.intervals.isEmpty())) && s.startedAt>=0 && s.sensitivity in 0..100 && s.pauseSeconds in 2..30)
        val completed=db.backupStateDao().completed(s.token)
        if(completed.isNotEmpty()) {
            val conflict=completed.any{it.durationMillis!=s.elapsed}
            BackupState.preserve(db,original.first,original.second,"recovery-token",if(conflict)"token_conflict" else "already_completed",s.token)
            check(!conflict){"Conflicting completion evidence; original draft retained"}
            db.practiceDao().clearDraft();return@withTransaction null
        }
        recoveredOriginal=original.first
        s.copy(execution=java.util.concurrent.ThreadLocalRandom.current().nextLong(1,Long.MAX_VALUE/2),running=false,status=if(s.status=="Review")"Review" else "Interrupted",timingQuality="Incomplete",ready=true)
    }
    override suspend fun checkpoint(state:PracticeState) {
        db.withTransaction {
            val original=BackupState.rawDraft(db)
            if(recoveredOriginal!=null && original!=null && original.first.contentEquals(recoveredOriginal!!))
                BackupState.preserve(db,original.first,original.second,"recovered-original","retained_before_checkpoint",state.token)
            db.practiceDao().checkpoint(PracticeDraft(payload=encode(state),updatedAt=System.currentTimeMillis()))
        }
        recoveredOriginal=null
    }
    override suspend fun clear()=db.withTransaction {
        BackupState.rawDraft(db)?.let {BackupState.preserve(db,it.first,it.second,"cleared-draft","retained_before_clear")}
        db.practiceDao().clearDraft()
    }
    override suspend fun complete(state: PracticeState): Long = db.withTransaction {
        val s = state
        db.practiceDao().completed(s.token)?.let { check(it.durationMillis==s.elapsed){"Conflicting completion token; original retained"};return@withTransaction it.id }
        require(s.token.isNotBlank() && s.token.length<=256)
        check(db.backupStateDao().completed(s.token).isEmpty()){ "Completion token already has retained evidence" }
        val parts = s.parts // Keep note-only entries and their stable interval indexes.
        journal.complete(PracticeSession(pieceId = null, pieceTitle = parts.filter { it.durationMillis > 0 || it.notes.isNotBlank() }.joinToString(" · ") { it.title }.ifBlank { "Practice notes" },
            startedAt = s.startedAt, completedAt = System.currentTimeMillis(), durationMillis = s.elapsed,
            elapsedMillis = s.wallElapsed, detectedActiveMillis = s.detectedActive.takeIf { s.mode != "Manual" },
            practiceMode = s.mode, timingQuality = s.timingQuality, notes = s.notes, reflection = s.reflection, completionToken = s.token), parts)
        val id = requireNotNull(db.practiceDao().completed(s.token)).id
        db.practiceDao().intervals(s.intervals.map { it.copy(id = 0, sessionId = id) })
        db.backupStateDao().completed(CompletionEvidence(s.token,s.elapsed))
        BackupState.rawDraft(db)?.let {BackupState.preserve(db,it.first,it.second,"completed-draft","already_completed",s.token)}
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
            s.queueOrder?.let { put("queue", JSONArray(it)) }
            s.queueUndo?.let { u -> put("queueUndo", JSONObject().apply {
                put("active", u.active); put("order", JSONArray(u.order)); put("removed", JSONArray(u.removed.toList()))
            }) }
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
            val o = com.opus.piano.data.backup.BackupJson.parse(raw); require(o.get("version") is Long && o.getLong("version") == 1L)
            fun typed(row:JSONObject,strings:List<String>,integers:List<String>) {
                strings.forEach{require(row.get(it) is String)}
                integers.forEach{key->
                    val value=row.get(key);require(value is Long || value is Int)
                    if(key in setOf("active","sensitivity","pause","part"))require((value as Number).toLong() in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong())
                }
            }
            typed(o,listOf("token","mode","notes","reflection","quality"),listOf("start","credited","elapsed","detected","active","sensitivity","pause"))
            require(o.get("smart") is Boolean && o.getString("token").length<=256)
            o.getJSONArray("parts").objects().forEach {p->
                typed(p,listOf("title","composer","era","notes"),listOf("duration"))
                require(p.isNull("piece") || p.get("piece") is Long || p.get("piece") is Int)
                require(p.isNull("catalog") || p.get("catalog") is String)
            }
            o.getJSONArray("intervals").objects().forEach{r->typed(r,listOf("reason"),listOf("part","start","end","credit"))}

            val parts = o.getJSONArray("parts").objects().map { p -> SessionPart(pieceId = if (p.isNull("piece")) null else p.getLong("piece"), title = p.getString("title"), composer = p.getString("composer"), catalogWorkId = if (p.isNull("catalog")) null else p.getString("catalog"), musicalEra = p.getString("era"), durationMillis = p.getLong("duration"), notes = p.getString("notes")) }
            require(parts.isNotEmpty() && parts.all { it.durationMillis >= 0 })
            val rows = o.getJSONArray("intervals").objects().map { r -> SessionInterval(partIndex = r.getInt("part"), startMillis = r.getLong("start"), endMillis = r.getLong("end"), creditedMillis = r.getLong("credit"), reason = r.getString("reason")) }
            val s = PracticeState(execution = o.optLong("execution",0), status = o.optString("status", "Interrupted"), token = o.getString("token"), startedAt = o.getLong("start"), elapsed = o.getLong("credited"), wallElapsed = o.getLong("elapsed"), detectedActive = o.getLong("detected"), mode = o.getString("mode"), smart = o.getBoolean("smart"), active = o.getInt("active"), notes = o.getString("notes"), reflection = o.getString("reflection"), sensitivity = o.getInt("sensitivity"), pauseSeconds = o.getInt("pause"), timingQuality = o.getString("quality"), parts = parts, intervals = rows,
                removed = o.getJSONArray("removed").let { a -> (0 until a.length()).map { a.getLong(it) }.toSet() })
            require(s.active in parts.indices && parts.fold(0L) { sum, part -> Math.addExact(sum,part.durationMillis) } == s.elapsed)
            require(rows.isNotEmpty() || s.wallElapsed == 0L) {"Missing draft timing evidence; original retained"}
            validateTiming(s.elapsed, s.wallElapsed, s.detectedActive.takeIf { s.mode != "Manual" }, s.mode, rows, parts.size)
            parts.forEachIndexed {index,part->
                require(rows.filter{it.partIndex==index}.fold(0L){sum,row->Math.addExact(sum,row.creditedMillis)}==part.durationMillis) {"Conflicting draft part timing; original retained"}
            }
            fun order(value: JSONArray): List<Int> {
                require(value.length() in 1..parts.size)
                val indexes = (0 until value.length()).map { i ->
                    val n = value.get(i); require(n is Long || n is Int)
                    (n as Number).toLong().also { require(it in parts.indices.first.toLong()..parts.indices.last.toLong()) }.toInt()
                }
                require(indexes.distinct().size == indexes.size)
                return indexes
            }
            val queue = if (o.has("queue")) order(o.getJSONArray("queue")) else null
            require(queue == null || s.active in queue)
            val undo = if (o.has("queueUndo")) o.getJSONObject("queueUndo").let { u ->
                typed(u, emptyList(), listOf("active"))
                val previous = order(u.getJSONArray("order"))
                val active = u.getInt("active"); require(active in previous)
                val removed = u.getJSONArray("removed").let { a ->
                    require(a.length() <= parts.size)
                    (0 until a.length()).map { i ->
                        require(a.get(i) is Long || a.get(i) is Int); a.getLong(i)
                    }.toSet()
                }
                com.opus.piano.practice.QueueIntent(active, previous, removed)
            } else null
            return s.copy(queueOrder = queue, queueUndo = undo)
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
                require(rows.filter { it.reason == "Detected" }.sumOf { it.creditedMillis } == (detected ?: 0L))
            }
        }
    }
}
