package com.opus.piano.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "session_intervals", indices = [Index("sessionId")], foreignKeys = [ForeignKey(entity = PracticeSession::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.CASCADE)])
data class SessionInterval(@PrimaryKey(autoGenerate = true) val id: Long = 0, val sessionId: Long = 0,
    val partIndex: Int, val startMillis: Long, val endMillis: Long, val creditedMillis: Long, val reason: String)
@Entity(tableName = "practice_drafts")
data class PracticeDraft(@PrimaryKey val id: Int = 1, val payload: String, val updatedAt: Long)
@Entity(tableName = "local_app_state")
data class LocalAppState(@PrimaryKey val id: Int = 1, val onboardingDone: Boolean = false,
    val reminderEnabled: Boolean = false, val reminderHour: Int = 18, val reminderMinute: Int = 0,
    val generation: Long = 0, val lastReminderDay: String = "")
@Entity(tableName = "practice_recovery_archives")
data class RecoveryArchive(@PrimaryKey(autoGenerate = true) val id: Long = 0, val payload: String, val updatedAt: Long, val archivedAt: Long)
@Dao
interface PracticeDao {
    @Query("SELECT * FROM practice_drafts WHERE id=1") suspend fun draft(): PracticeDraft?
    @Upsert suspend fun checkpoint(value: PracticeDraft)
    @Insert suspend fun archive(value: RecoveryArchive)
    @Query("SELECT * FROM practice_recovery_archives ORDER BY id") suspend fun archives(): List<RecoveryArchive>
    @Query("DELETE FROM practice_drafts") suspend fun clearDraft()
    @Query("SELECT * FROM sessions WHERE completionToken=:token") suspend fun completed(token: String): PracticeSession?
    @Query("""SELECT * FROM sessions s WHERE
        (s.completedAt < :beforeTime OR (s.completedAt = :beforeTime AND s.id < :beforeId))
        AND ((:pieceId IS NULL AND :catalogId IS NULL) OR
            EXISTS (SELECT 1 FROM session_parts p WHERE p.sessionId=s.id
                AND ((:pieceId IS NOT NULL AND p.pieceId=:pieceId) OR (:catalogId IS NOT NULL AND p.catalogWorkId=:catalogId))) OR
            (NOT EXISTS (SELECT 1 FROM session_parts p WHERE p.sessionId=s.id) AND
                ((:pieceId IS NOT NULL AND s.pieceId=:pieceId) OR (:catalogId IS NOT NULL AND s.catalogWorkId=:catalogId))))
        ORDER BY s.completedAt DESC, s.id DESC LIMIT :limit""")
    suspend fun historyPage(pieceId: Long?, catalogId: String?, beforeTime: Long, beforeId: Long, limit: Int): List<PracticeSession>
    @Query("SELECT * FROM session_parts WHERE sessionId IN (:ids) ORDER BY sessionId,id")
    suspend fun historyParts(ids: List<Long>): List<SessionPart>
    @Query("SELECT * FROM sessions WHERE id=:id") suspend fun session(id: Long): PracticeSession?
    @Query("SELECT * FROM session_intervals WHERE sessionId=:id ORDER BY startMillis,id") suspend fun intervals(id: Long): List<SessionInterval>
    @Query("SELECT * FROM session_intervals ORDER BY sessionId,startMillis,id") suspend fun allIntervals(): List<SessionInterval>
    @Insert suspend fun intervals(values: List<SessionInterval>)
    @Query("UPDATE sessions SET notes=:notes, reflection=:reflection WHERE id=:id") suspend fun notes(id: Long, notes: String, reflection: String)
    @Query("UPDATE session_parts SET notes=:notes WHERE id=:id AND sessionId=:sessionId") suspend fun partNote(sessionId: Long, id: Long, notes: String)
    @Query("SELECT * FROM local_app_state WHERE id=1") fun local(): Flow<LocalAppState?>
    @Query("SELECT * FROM local_app_state WHERE id=1") suspend fun localNow(): LocalAppState?
    @Upsert suspend fun local(value: LocalAppState)
}
