package com.opus.piano.data

import com.opus.piano.i18n.L
import com.opus.piano.R
import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private data class BackupColumn(val name: String, val type: String, val required: Boolean)

/** Format 4/schema 8 adds untimed history associations and composer favorites; formats 1/2/3 remain importable. */
class BackupRepository(private val context: Context, private val db: OpusDatabase) {
    companion object {
        const val MAX_BYTES = 32 * 1024 * 1024
        val tables = listOf("pieces", "sessions", "catalog_composers", "catalog_works", "catalog_cache", "saved_recordings", "badge_unlocks", "session_parts", "profile_settings", "session_intervals", "practice_recovery_archives", "session_piece_links", "favorite_composers")
    }
    suspend fun export(): String = withContext(Dispatchers.IO) {
        db.withTransaction {
            JSONObject().apply {
                put("format", "goldberg-backup"); put("version", 4); put("schema", 8); put("createdAt", System.currentTimeMillis())
                put("tables", JSONObject().apply { tables.forEach { table ->
                    val rows = JSONArray()
                    db.openHelper.writableDatabase.query("SELECT * FROM `$table`").use { c ->
                        while (c.moveToNext()) rows.put(JSONObject().apply { c.columnNames.forEachIndexed { i, name ->
                            put(name, if (c.isNull(i)) JSONObject.NULL else if (c.getType(i) == android.database.Cursor.FIELD_TYPE_INTEGER) c.getLong(i) else c.getString(i))
                        } })
                    }
                    put(table, rows)
                } })
            }.toString().also { require(it.toByteArray().size <= MAX_BYTES) { L.s(R.string.msg_backup_exceeds_32_mib_046e) } }
        }
    }
    private fun parse(raw: String): JSONObject {
        require(raw.toByteArray().size <= MAX_BYTES) { L.s(R.string.msg_backup_exceeds_32_mib_046e) }
        val root = JSONObject(raw)
        require(root.getString("format") == "goldberg-backup" && ((root.get("version") == 1 && root.get("schema") in listOf(4, 5)) || (root.get("version") == 2 && root.get("schema") == 6) || (root.get("version") == 3 && root.get("schema") == 7) || (root.get("version") == 4 && root.get("schema") == 8))) { L.s(R.string.unsupported_backup) }
        return root.getJSONObject("tables").also {
            val originalTables=tables.filterNot { table ->
                (root.getInt("schema")<8 && table in listOf("session_piece_links","favorite_composers")) ||
                (root.getInt("schema")<7 && table=="practice_recovery_archives") ||
                (root.getInt("schema")<6 && table=="session_intervals")
            }
            require(it.keys().asSequence().toSet()==originalTables.toSet()) {L.s(R.string.msg_incomplete_backup_bf05)}
            if(root.getInt("schema")<8) {it.put("session_piece_links",JSONArray());it.put("favorite_composers",JSONArray())}
            if (root.getInt("schema") < 7) {
                it.put("practice_recovery_archives", JSONArray())
                it.getJSONArray("profile_settings").objects().forEach { row ->
                    listOf("favoriteArtist", "favoritePiece", "favoriteEra", "biography").forEach { name -> row.put(name, "") }
                }
            }
            if (root.getInt("schema") < 6) {
                it.put("session_intervals", JSONArray())
                it.getJSONArray("sessions").objects().forEach { row ->
                    row.put("elapsedMillis", JSONObject.NULL); row.put("detectedActiveMillis", JSONObject.NULL)
                    row.put("practiceMode", "Manual"); row.put("timingQuality", "Legacy")
                    row.put("notes", ""); row.put("reflection", ""); row.put("completionToken", JSONObject.NULL)
                }
                it.getJSONArray("profile_settings").objects().forEach { row -> row.put("smartEnabled", 0); row.put("sensitivity", 50); row.put("pauseSeconds", 5) }
            }
            if (root.getInt("schema") == 4) it.getJSONArray("profile_settings").objects().forEach { row ->
                row.put("typography", "Editorial"); row.put("statistics", "time,pieces,sessions,streak")
            }
        }
    }
    private fun insert(target: OpusDatabase, data: JSONObject) {
        val sql = target.openHelper.writableDatabase
        tables.forEach { table ->
            val columns = mutableListOf<BackupColumn>()
            sql.query("PRAGMA table_info(`$table`)").use { c -> while (c.moveToNext()) columns += BackupColumn(c.getString(1), c.getString(2), c.getInt(3) == 1) }
            val rows = data.getJSONArray(table)
            require(rows.length() <= 100000) { L.s(R.string.msg_too_many_records_8725) }
            val statement = "INSERT INTO `$table` (${columns.joinToString { "`${it.name}`" }}) VALUES (${columns.joinToString { "?" }})"
            for (i in 0 until rows.length()) {
                val row = rows.getJSONObject(i)
                require(row.keys().asSequence().toSet() == columns.map { it.name }.toSet()) { L.s(R.string.msg_invalid_columns_in_table_e7f1, (table)) }
                val values: Array<Any?> = columns.map<BackupColumn, Any?> { column ->
                    val v = row.get(column.name)
                    if (v === JSONObject.NULL) { require(!column.required); null }
                    else if (column.type == "INTEGER") { require(v is Long || v is Int) { L.s(R.string.msg_invalid_number_e751) }; (v as Number).toLong() }
                    else { require(v is String && v.length <= 8 * 1024 * 1024) { L.s(R.string.msg_invalid_text_0999) }; v }
                }.toTypedArray()
                sql.execSQL(statement, values)
            }
        }
    }
    suspend fun validate(raw: String): String = withContext(Dispatchers.IO) {
        val data = parse(raw)
        val stage = Room.inMemoryDatabaseBuilder(context, OpusDatabase::class.java).build()
        try {
            stage.withTransaction { insert(stage, data) }
            val pieces = stage.dao().allPieces() // Also checks enum encodings.
            require(pieces.all { it.id > 0 && it.title.isNotBlank() && it.createdAt >= 0 }) { L.s(R.string.msg_invalid_repertoire_e71f) }
            val sessions = stage.dao().allSessions()
            require(sessions.all { it.id > 0 && it.durationMillis >= 0 && it.completedAt > 0 && it.startedAt >= 0 }) { L.s(R.string.msg_invalid_sessions_8359) }
            sessions.fold(0L) { total, s -> Math.addExact(total, s.durationMillis) }
            sessions.forEach { s -> s.practiceDay?.let { java.time.LocalDate.parse(it) }; s.practiceZone?.let { java.time.ZoneId.of(it) } }
            val parts = stage.personalDao().allParts()
            require(parts.all { it.id > 0 && it.durationMillis >= 0 }) { L.s(R.string.msg_invalid_piece_time_7785) }
            val grouped = parts.groupBy { it.sessionId }
            require(sessions.all { s -> (grouped[s.id]?.fold(0L) { total, p -> Math.addExact(total, p.durationMillis) } == s.durationMillis) || (grouped[s.id]==null && s.timingQuality=="Legacy") }) { L.s(R.string.msg_session_time_does_not_match_piece_times_2ac7) }
            val intervals = stage.practiceDao().allIntervals().groupBy { it.sessionId }
            sessions.forEach { session ->
                val entries = grouped[session.id].orEmpty()
                val rows = intervals[session.id].orEmpty()
                PracticeRepository.validateTiming(session.durationMillis, session.elapsedMillis, session.detectedActiveMillis, session.practiceMode, rows, entries.size)
                require(session.timingQuality in listOf("Legacy", "Measured", "Incomplete", "Estimated"))
                if (session.timingQuality == "Legacy") require(session.elapsedMillis == null && session.detectedActiveMillis == null && rows.isEmpty() && session.practiceMode == "Manual") { L.s(R.string.msg_invalid_legacy_measurements_4f8b) }
                if (session.timingQuality != "Legacy") require(session.elapsedMillis != null && (session.elapsedMillis == 0L || rows.isNotEmpty())) { L.s(R.string.msg_missing_timing_intervals_eb3a) }
                entries.forEachIndexed { index, part -> if (rows.isNotEmpty()) require(rows.filter { it.partIndex == index }.sumOf { it.creditedMillis } == part.durationMillis) }
            }
            val unlocks = stage.experienceDao().allUnlocks()
            require(unlocks.all { it.unlockedAt > 0 }) { L.s(R.string.msg_invalid_unlock_date_c907) }
            stage.personalDao().getSettings()?.let { p ->
                require(p.favoriteArtist.length<=120 && p.favoritePiece.length<=240 && p.favoriteEra.length<=80 && p.biography.length<=400)
                require(p.sensitivity in 0..100 && p.pauseSeconds in 2..30)
                require(p.typography in listOf("Editorial", "System") && StatisticLayout.valid(p.statistics)) { L.s(R.string.msg_invalid_appearance_or_statistics_3f1e) }
                require(p.id == 1 && p.username.length <= 80 && p.theme in listOf("System", "Light", "Dark")) { L.s(R.string.msg_invalid_profile_e51c) }
                val ids = p.showcase.split(',').filter { it.isNotBlank() }
                require(ids.size <= 5 && ids.distinct() == ids && ids.all { id -> unlocks.any { it.achievementId == id } }) { L.s(R.string.msg_invalid_badge_showcase_1ea2) }
                if (p.avatar.isNotBlank()) {
                    require(p.avatar.length <= 1024 * 1024)
                    val bytes = android.util.Base64.decode(p.avatar, android.util.Base64.DEFAULT)
                    val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                    require(bounds.outWidth in 1..512 && bounds.outHeight in 1..512) { L.s(R.string.msg_invalid_avatar_bb11) }
                }
            }
            stage.practiceDao().archives().forEach { require(it.payload.length <= 16*1024*1024 && it.updatedAt>=0 && it.archivedAt>0) }
            stage.journalEditsDao().allLinks().forEach { link ->
                val session=requireNotNull(sessions.firstOrNull {it.id==link.sessionId})
                require(link.pieceId>0 && link.title.isNotBlank() && link.addedAt>=0)
                require(grouped[session.id].orEmpty().none {it.pieceId==link.pieceId || (link.catalogWorkId!=null && it.catalogWorkId==link.catalogWorkId)})
                require(grouped[session.id]!=null || (session.pieceId!=link.pieceId && (link.catalogWorkId==null || session.catalogWorkId!=link.catalogWorkId)))
            }
            stage.journalEditsDao().allFavorites().forEach {require(it.composerId.isNotBlank() && it.composerId.length<=160 && it.addedAt>=0)}
            val profileRows = data.getJSONArray("profile_settings")
            require(profileRows.objects().all { it.getInt("smartEnabled") in 0..1 }) { L.s(R.string.msg_invalid_smart_preference_358b) }
            require(profileRows.length() <= 1 && (profileRows.length() == 0 || profileRows.getJSONObject(0).getInt("id") == 1)) { L.s(R.string.msg_invalid_profile_record_4521) }
            val links = data.getJSONArray("saved_recordings").objects()
            require(links.distinctBy { it.getString("workKey") to it.getString("url") }.size == links.size) { L.s(R.string.msg_duplicate_saved_links_0666) }
            require(links.groupBy { it.getString("workKey") }.values.all { rows -> rows.count { it.getInt("preferred") == 1 } <= 1 }) { L.s(R.string.msg_multiple_preferred_links_for_a_work_5576) }
            require(links.all { it.getLong("id") > 0 && it.getInt("preferred") in 0..1 && it.getLong("savedAt") >= 0 }) { L.s(R.string.msg_invalid_saved_links_a86b) }
            L.s(R.string.backup_summary,L.quantity(R.plurals.pieces_count,pieces.size),L.quantity(R.plurals.sessions_count,sessions.size),unlocks.size)
        } finally { stage.close() }
    }
    /** Replacement is explicit in the UI; a single transaction rolls back every row on failure. */
    suspend fun restore(raw: String) = withContext(Dispatchers.IO) {
        validate(raw)
        val data = parse(raw)
        db.withTransaction {
            tables.asReversed().forEach { db.openHelper.writableDatabase.execSQL("DELETE FROM `$it`") }
            insert(db, data)
            db.practiceDao().clearDraft()
        }
        db.invalidationTracker.refreshAsync()
    }
}
