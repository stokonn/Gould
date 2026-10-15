package com.opus.piano.data

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private data class BackupColumn(val name: String, val type: String, val required: Boolean)

/** Format 1 is a complete schema-4 snapshot; SQL and file paths are never read from a backup. */
class BackupRepository(private val context: Context, private val db: OpusDatabase) {
    companion object {
        const val MAX_BYTES = 32 * 1024 * 1024
        val tables = listOf("pieces", "sessions", "catalog_composers", "catalog_works", "catalog_cache", "saved_recordings", "badge_unlocks", "session_parts", "profile_settings")
    }
    suspend fun export(): String = withContext(Dispatchers.IO) {
        db.withTransaction {
            JSONObject().apply {
                put("format", "goldberg-backup"); put("version", 1); put("schema", 4); put("createdAt", System.currentTimeMillis())
                put("tables", JSONObject().apply { tables.forEach { table ->
                    val rows = JSONArray()
                    db.openHelper.writableDatabase.query("SELECT * FROM `$table`").use { c ->
                        while (c.moveToNext()) rows.put(JSONObject().apply { c.columnNames.forEachIndexed { i, name ->
                            put(name, if (c.isNull(i)) JSONObject.NULL else if (c.getType(i) == android.database.Cursor.FIELD_TYPE_INTEGER) c.getLong(i) else c.getString(i))
                        } })
                    }
                    put(table, rows)
                } })
            }.toString().also { require(it.toByteArray().size <= MAX_BYTES) { "Backup exceeds 32 MiB." } }
        }
    }
    private fun parse(raw: String): JSONObject {
        require(raw.toByteArray().size <= MAX_BYTES) { "Backup exceeds 32 MiB." }
        val root = JSONObject(raw)
        require(root.getString("format") == "goldberg-backup" && root.get("version") == 1 && root.get("schema") == 4) { "Unsupported backup version." }
        return root.getJSONObject("tables").also { require(it.keys().asSequence().toSet() == tables.toSet()) { "Incomplete backup." } }
    }
    private fun insert(target: OpusDatabase, data: JSONObject) {
        val sql = target.openHelper.writableDatabase
        tables.forEach { table ->
            val columns = mutableListOf<BackupColumn>()
            sql.query("PRAGMA table_info(`$table`)").use { c -> while (c.moveToNext()) columns += BackupColumn(c.getString(1), c.getString(2), c.getInt(3) == 1) }
            val rows = data.getJSONArray(table)
            require(rows.length() <= 100000) { "Too many records." }
            val statement = "INSERT INTO `$table` (${columns.joinToString { "`${it.name}`" }}) VALUES (${columns.joinToString { "?" }})"
            for (i in 0 until rows.length()) {
                val row = rows.getJSONObject(i)
                require(row.keys().asSequence().toSet() == columns.map { it.name }.toSet()) { "Invalid columns in $table." }
                val values: Array<Any?> = columns.map<BackupColumn, Any?> { column ->
                    val v = row.get(column.name)
                    if (v === JSONObject.NULL) { require(!column.required); null }
                    else if (column.type == "INTEGER") { require(v is Long || v is Int) { "Invalid number." }; (v as Number).toLong() }
                    else { require(v is String && v.length <= 8 * 1024 * 1024) { "Invalid text." }; v }
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
            require(pieces.all { it.id > 0 && it.title.isNotBlank() && it.createdAt >= 0 }) { "Invalid repertoire." }
            val sessions = stage.dao().allSessions()
            require(sessions.all { it.id > 0 && it.durationMillis >= 0 && it.completedAt > 0 && it.startedAt >= 0 }) { "Invalid sessions." }
            sessions.fold(0L) { total, s -> Math.addExact(total, s.durationMillis) }
            sessions.forEach { s -> s.practiceDay?.let { java.time.LocalDate.parse(it) }; s.practiceZone?.let { java.time.ZoneId.of(it) } }
            val parts = stage.personalDao().allParts()
            require(parts.all { it.id > 0 && it.durationMillis >= 0 }) { "Invalid piece time." }
            val grouped = parts.groupBy { it.sessionId }
            require(sessions.all { s -> grouped[s.id]?.fold(0L) { total, p -> Math.addExact(total, p.durationMillis) } == s.durationMillis }) { "Session time does not match piece times." }
            val unlocks = stage.experienceDao().allUnlocks()
            require(unlocks.all { it.unlockedAt > 0 }) { "Invalid unlock date." }
            stage.personalDao().getSettings()?.let { p ->
                require(p.id == 1 && p.username.length <= 80 && p.theme in listOf("System", "Light", "Dark")) { "Invalid profile." }
                val ids = p.showcase.split(',').filter { it.isNotBlank() }
                require(ids.size <= 5 && ids.distinct() == ids && ids.all { id -> unlocks.any { it.achievementId == id } }) { "Invalid badge showcase." }
                if (p.avatar.isNotBlank()) {
                    require(p.avatar.length <= 1024 * 1024)
                    val bytes = android.util.Base64.decode(p.avatar, android.util.Base64.DEFAULT)
                    val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                    require(bounds.outWidth in 1..512 && bounds.outHeight in 1..512) { "Invalid avatar." }
                }
            }
            val profileRows = data.getJSONArray("profile_settings")
            require(profileRows.length() <= 1 && (profileRows.length() == 0 || profileRows.getJSONObject(0).getInt("id") == 1)) { "Invalid profile record." }
            val links = data.getJSONArray("saved_recordings").objects()
            require(links.distinctBy { it.getString("workKey") to it.getString("url") }.size == links.size) { "Duplicate saved links." }
            require(links.groupBy { it.getString("workKey") }.values.all { rows -> rows.count { it.getInt("preferred") == 1 } <= 1 }) { "Multiple preferred links for a work." }
            require(links.all { it.getLong("id") > 0 && it.getInt("preferred") in 0..1 && it.getLong("savedAt") >= 0 }) { "Invalid saved links." }
            "${pieces.size} repertoire pieces · ${sessions.size} sessions · ${unlocks.size} unlocked badges"
        } finally { stage.close() }
    }
    /** Replacement is explicit in the UI; a single transaction rolls back every row on failure. */
    suspend fun restore(raw: String) = withContext(Dispatchers.IO) {
        validate(raw)
        val data = parse(raw)
        db.withTransaction {
            tables.asReversed().forEach { db.openHelper.writableDatabase.execSQL("DELETE FROM `$it`") }
            insert(db, data)
        }
        db.invalidationTracker.refreshAsync()
    }
}
