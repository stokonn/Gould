package com.opus.piano.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "session_parts", indices = [Index("sessionId")], foreignKeys = [ForeignKey(entity = PracticeSession::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.CASCADE)])
data class SessionPart(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long = 0,
    val pieceId: Long? = null,
    val title: String = "Free practice",
    val composer: String = "",
    val catalogWorkId: String? = null,
    val composerId: String? = null,
    val musicalEra: String = "",
    val durationMillis: Long = 0,
    val notes: String = ""
)
@Entity(tableName = "profile_settings")
data class ProfileSettings(@PrimaryKey val id: Int = 1, val username: String = "Musician", val avatar: String = "", val theme: String = "System", val showcase: String = "", @ColumnInfo(defaultValue = "'Editorial'") val typography: String = "Editorial", @ColumnInfo(defaultValue = "'time,pieces,sessions,streak'") val statistics: String = "time,pieces,sessions,streak",
    @ColumnInfo(defaultValue = "0") val smartEnabled: Boolean = false,
    @ColumnInfo(defaultValue = "50") val sensitivity: Int = 50,
    @ColumnInfo(defaultValue = "5") val pauseSeconds: Int = 5,
    @ColumnInfo(defaultValue = "''") val favoriteArtist: String = "",
    @ColumnInfo(defaultValue = "''") val favoritePiece: String = "",
    @ColumnInfo(defaultValue = "''") val favoriteEra: String = "",
    @ColumnInfo(defaultValue = "''") val biography: String = "") {
    val badgeIds: List<String> get() = showcase.split(',').filter { it.isNotBlank() }.distinct().take(5)
}
object Showcase {
    fun select(ids: List<String>, unlocked: Set<String>): List<String> = ids.filter { it in unlocked }.distinct().take(5)
}
@Dao
interface PersonalDao {
    @Query("SELECT * FROM session_parts ORDER BY id") fun parts(): Flow<List<SessionPart>>
    @Query("SELECT * FROM session_parts ORDER BY id") suspend fun allParts(): List<SessionPart>
    @Insert suspend fun insertParts(parts: List<SessionPart>)
    @Query("SELECT * FROM profile_settings WHERE id = 1") fun settings(): Flow<ProfileSettings?>
    @Query("SELECT * FROM profile_settings WHERE id = 1") suspend fun getSettings(): ProfileSettings?
    @Upsert suspend fun saveSettings(settings: ProfileSettings)
}
/** Piece attribution only. Never use this expansion to count sessions or total practice. */
fun attributedSessions(sessions: List<PracticeSession>, parts: List<SessionPart>): List<PracticeSession> {
    val groups = parts.groupBy { it.sessionId }
    return sessions.flatMap { s -> groups[s.id]?.filter { it.durationMillis > 0 }?.map { p -> s.copy(pieceId = p.pieceId, pieceTitle = p.title, durationMillis = p.durationMillis, catalogWorkId = p.catalogWorkId ?: s.catalogWorkId, composerId = p.composerId ?: s.composerId, musicalEra = p.musicalEra.ifBlank { s.musicalEra }) } ?: listOf(s) }
}
/** Monotonic deltas are charged to exactly one active entry, including free practice. */
fun charge(parts: List<SessionPart>, active: Int, delta: Long): List<SessionPart> {
    require(active in parts.indices && delta >= 0)
    return parts.mapIndexed { index, p -> if (index == active) p.copy(durationMillis = Math.addExact(p.durationMillis, delta)) else p }
}
