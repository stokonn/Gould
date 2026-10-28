package com.opus.piano.data

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

/** Personal captured audio; deliberately separate from saved_recordings URL bookmarks.
 * Session identity is a snapshot, not a cascading FK: deleting a journal entry never deletes audio. */
@Entity(tableName = "local_audio", indices = [Index("createdAt"), Index("sessionToken")])
data class LocalAudio(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val stoppedAt: Long = 0,
    val state: String = "Preparing",
    val relativeFile: String,
    val format: String = "pcm_s16le_wav",
    val sampleRate: Int,
    val sampleCount: Long = 0,
    val byteCount: Long = 0,
    val sha256: String = "",
    val title: String = "",
    val composer: String = "",
    val sessionToken: String? = null,
    val error: String = ""
) {
    val durationMillis: Long get() = if (sampleRate > 0) sampleCount / sampleRate * 1000 + sampleCount % sampleRate * 1000 / sampleRate else 0
}

/** Capture offsets describe association only, never credited practice time. */
@Entity(tableName = "local_audio_parts", primaryKeys = ["recordingId", "offsetSamples"],
    foreignKeys = [ForeignKey(entity = LocalAudio::class, parentColumns = ["id"], childColumns = ["recordingId"], onDelete = ForeignKey.CASCADE)])
data class LocalAudioPart(val recordingId: String, val offsetSamples: Long, val pieceKey: String, val title: String, val composer: String)

/** Stable domain-event keys make playback/save/note evaluation idempotent across retries and restores. */
@Entity(tableName = "achievement_events", indices = [Index("kind")])
data class AchievementEvent(@PrimaryKey val eventKey: String, val kind: String, val subject: String, val occurredAt: Long, val value: Long = 1)

@Entity(tableName = "badge_notices")
data class BadgeNotice(@PrimaryKey val achievementId: String, val unlockedAt: Long, val acknowledgedAt: Long = 0)

@Dao
interface StudioDao {
    @Query("SELECT * FROM local_audio ORDER BY createdAt DESC,id DESC LIMIT :limit") fun recordings(limit: Int = 100): Flow<List<LocalAudio>>
    @Query("SELECT * FROM local_audio ORDER BY createdAt DESC,id DESC") suspend fun allAudio(): List<LocalAudio>
    @Query("SELECT * FROM local_audio WHERE id=:id") suspend fun audio(id: String): LocalAudio?
    @Insert suspend fun insertAudio(value: LocalAudio)
    @Update suspend fun updateAudio(value: LocalAudio)
    @Query("DELETE FROM local_audio WHERE id=:id") suspend fun deleteAudio(id: String)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun associate(value: LocalAudioPart)
    @Query("SELECT * FROM local_audio_parts WHERE recordingId=:id ORDER BY offsetSamples") suspend fun audioParts(id: String): List<LocalAudioPart>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun event(value: AchievementEvent): Long
    @Query("SELECT * FROM achievement_events ORDER BY occurredAt,eventKey") suspend fun events(): List<AchievementEvent>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun notice(value: BadgeNotice)
    @Query("SELECT * FROM badge_notices WHERE acknowledgedAt=0 ORDER BY unlockedAt,achievementId") fun pendingNotices(): Flow<List<BadgeNotice>>
    @Query("UPDATE badge_notices SET acknowledgedAt=:at WHERE achievementId=:id AND acknowledgedAt=0") suspend fun acknowledge(id: String, at: Long)
}

object StudioSchema {
    val tables = listOf("local_audio", "local_audio_parts", "achievement_events", "badge_notices")
    fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS local_audio (id TEXT NOT NULL PRIMARY KEY, createdAt INTEGER NOT NULL, stoppedAt INTEGER NOT NULL, state TEXT NOT NULL, relativeFile TEXT NOT NULL, format TEXT NOT NULL, sampleRate INTEGER NOT NULL, sampleCount INTEGER NOT NULL, byteCount INTEGER NOT NULL, sha256 TEXT NOT NULL, title TEXT NOT NULL, composer TEXT NOT NULL, sessionToken TEXT, error TEXT NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_local_audio_createdAt ON local_audio(createdAt)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_local_audio_sessionToken ON local_audio(sessionToken)")
        db.execSQL("CREATE TABLE IF NOT EXISTS local_audio_parts (recordingId TEXT NOT NULL, offsetSamples INTEGER NOT NULL, pieceKey TEXT NOT NULL, title TEXT NOT NULL, composer TEXT NOT NULL, PRIMARY KEY(recordingId,offsetSamples), FOREIGN KEY(recordingId) REFERENCES local_audio(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        db.execSQL("CREATE TABLE IF NOT EXISTS achievement_events (eventKey TEXT NOT NULL PRIMARY KEY, kind TEXT NOT NULL, subject TEXT NOT NULL, occurredAt INTEGER NOT NULL, value INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_achievement_events_kind ON achievement_events(kind)")
        db.execSQL("CREATE TABLE IF NOT EXISTS badge_notices (achievementId TEXT NOT NULL PRIMARY KEY, unlockedAt INTEGER NOT NULL, acknowledgedAt INTEGER NOT NULL)")
        // Existing awards are history, not a queue of new notifications after upgrading.
        db.execSQL("INSERT OR IGNORE INTO badge_notices SELECT achievementId,unlockedAt,unlockedAt FROM badge_unlocks")
    }
}
