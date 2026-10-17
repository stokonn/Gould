package com.opus.piano.data

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged

import androidx.room.*
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

enum class LearningStatus(val label: String) { PLANNED("To learn"), LEARNING("Learning"), POLISHING("Polishing"), READY("Performance ready") }
enum class Difficulty(val label: String) { UNRATED("Not assessed"), BEGINNER("Beginner"), INTERMEDIATE("Intermediate"), ADVANCED("Advanced"), EXPERT("Expert") }

@Entity(tableName = "pieces", indices = [Index(value = ["catalogWorkId"], unique = true)])
data class Piece(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val composer: String,
    val difficulty: Difficulty = Difficulty.INTERMEDIATE,
    val status: LearningStatus = LearningStatus.PLANNED,
    val notes: String = "",
    val catalogWorkId: String? = null,
    @ColumnInfo(defaultValue = "0") val createdAt: Long = 0,
    @ColumnInfo(defaultValue = "''") val instrument: String = "",
    @ColumnInfo(defaultValue = "''") val genre: String = "",
    @ColumnInfo(defaultValue = "''") val musicalEra: String = ""
)

// Snapshot titles preserve meaningful history when a repertoire piece is edited or deleted.
@Entity(tableName = "sessions", indices = [Index("completedAt"), Index("pieceId"), Index(value = ["completionToken"], unique = true)])
data class PracticeSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pieceId: Long?,
    val pieceTitle: String,
    val startedAt: Long,
    val completedAt: Long,
    val durationMillis: Long,
    val practiceDay: String? = null,
    val practiceZone: String? = null,
    val catalogWorkId: String? = null,
    val composerId: String? = null,
    @ColumnInfo(defaultValue = "''") val musicalEra: String = "",
    val elapsedMillis: Long? = null,
    val detectedActiveMillis: Long? = null,
    @ColumnInfo(defaultValue = "'Manual'") val practiceMode: String = "Manual",
    @ColumnInfo(defaultValue = "'Legacy'") val timingQuality: String = "Legacy",
    @ColumnInfo(defaultValue = "''") val notes: String = "",
    @ColumnInfo(defaultValue = "''") val reflection: String = "",
    val completionToken: String? = null
)
class Converters {
    @TypeConverter fun status(value: String) = LearningStatus.valueOf(value)
    @TypeConverter fun status(value: LearningStatus) = value.name
    @TypeConverter fun difficulty(value: String) = Difficulty.valueOf(value)
    @TypeConverter fun difficulty(value: Difficulty) = value.name
}
@Dao
interface OpusDao {
    @Query("SELECT * FROM pieces ORDER BY title COLLATE NOCASE") fun observePieces(): Flow<List<Piece>>
    @Query("SELECT * FROM sessions ORDER BY completedAt DESC") fun observeSessions(): Flow<List<PracticeSession>>
    @Query("SELECT * FROM pieces WHERE catalogWorkId = :id LIMIT 1") suspend fun catalogPiece(id: String): Piece?
    @Query("SELECT * FROM pieces") suspend fun allPieces(): List<Piece>
    @Query("SELECT * FROM sessions") suspend fun allSessions(): List<PracticeSession>
    @Update suspend fun updateSession(session: PracticeSession)
    @Query("SELECT * FROM pieces WHERE id = :id") suspend fun piece(id: Long): Piece?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertCatalogPiece(piece: Piece): Long
    @Upsert suspend fun save(piece: Piece)
    @Delete suspend fun delete(piece: Piece)
    @Insert suspend fun insert(session: PracticeSession): Long
}
@Database(entities = [Piece::class, PracticeSession::class, CatalogComposer::class, CatalogWork::class, CatalogCache::class, SavedRecording::class, BadgeUnlock::class, SessionPart::class, ProfileSettings::class, SessionInterval::class, PracticeDraft::class, LocalAppState::class, RecoveryArchive::class, SessionPieceLink::class, FavoriteComposer::class], version = 8, exportSchema = true)
@TypeConverters(Converters::class)
abstract class OpusDatabase : RoomDatabase() {
    abstract fun journalEditsDao(): JournalEditsDao
    abstract fun practiceDao(): PracticeDao
    abstract fun personalDao(): PersonalDao
    abstract fun dao(): OpusDao
    abstract fun catalogDao(): CatalogDao
    abstract fun experienceDao(): ExperienceDao
    companion object {
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS session_piece_links (sessionId INTEGER NOT NULL, pieceId INTEGER NOT NULL, title TEXT NOT NULL, composer TEXT NOT NULL, catalogWorkId TEXT, composerId TEXT, musicalEra TEXT NOT NULL, addedAt INTEGER NOT NULL, PRIMARY KEY(sessionId,pieceId), FOREIGN KEY(sessionId) REFERENCES sessions(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE TABLE IF NOT EXISTS favorite_composers (composerId TEXT NOT NULL PRIMARY KEY, addedAt INTEGER NOT NULL)")
            }
        }
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE profile_settings ADD COLUMN favoriteArtist TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE profile_settings ADD COLUMN favoritePiece TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE profile_settings ADD COLUMN favoriteEra TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE profile_settings ADD COLUMN biography TEXT NOT NULL DEFAULT ''")
                db.execSQL("CREATE TABLE IF NOT EXISTS practice_recovery_archives (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, payload TEXT NOT NULL, updatedAt INTEGER NOT NULL, archivedAt INTEGER NOT NULL)")
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sessions ADD COLUMN elapsedMillis INTEGER")
                db.execSQL("ALTER TABLE sessions ADD COLUMN detectedActiveMillis INTEGER")
                db.execSQL("ALTER TABLE sessions ADD COLUMN practiceMode TEXT NOT NULL DEFAULT 'Manual'")
                db.execSQL("ALTER TABLE sessions ADD COLUMN timingQuality TEXT NOT NULL DEFAULT 'Legacy'")
                db.execSQL("ALTER TABLE sessions ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sessions ADD COLUMN reflection TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sessions ADD COLUMN completionToken TEXT")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_sessions_completionToken ON sessions(completionToken)")
                db.execSQL("ALTER TABLE profile_settings ADD COLUMN smartEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE profile_settings ADD COLUMN sensitivity INTEGER NOT NULL DEFAULT 50")
                db.execSQL("ALTER TABLE profile_settings ADD COLUMN pauseSeconds INTEGER NOT NULL DEFAULT 5")
                db.execSQL("CREATE TABLE IF NOT EXISTS session_intervals (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, sessionId INTEGER NOT NULL, partIndex INTEGER NOT NULL, startMillis INTEGER NOT NULL, endMillis INTEGER NOT NULL, creditedMillis INTEGER NOT NULL, reason TEXT NOT NULL, FOREIGN KEY(sessionId) REFERENCES sessions(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_session_intervals_sessionId ON session_intervals(sessionId)")
                db.execSQL("CREATE TABLE IF NOT EXISTS practice_drafts (id INTEGER NOT NULL PRIMARY KEY, payload TEXT NOT NULL, updatedAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS local_app_state (id INTEGER NOT NULL PRIMARY KEY, onboardingDone INTEGER NOT NULL, reminderEnabled INTEGER NOT NULL, reminderHour INTEGER NOT NULL, reminderMinute INTEGER NOT NULL, generation INTEGER NOT NULL, lastReminderDay TEXT NOT NULL)")
                db.execSQL("INSERT INTO local_app_state VALUES(1,1,0,18,0,0,'')")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE profile_settings ADD COLUMN typography TEXT NOT NULL DEFAULT 'Editorial'")
                db.execSQL("ALTER TABLE profile_settings ADD COLUMN statistics TEXT NOT NULL DEFAULT 'time,pieces,sessions,streak'")
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_completedAt ON sessions(completedAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_pieceId ON sessions(pieceId)")
                db.execSQL("CREATE TABLE IF NOT EXISTS session_parts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, sessionId INTEGER NOT NULL, pieceId INTEGER, title TEXT NOT NULL, composer TEXT NOT NULL, catalogWorkId TEXT, composerId TEXT, musicalEra TEXT NOT NULL, durationMillis INTEGER NOT NULL, notes TEXT NOT NULL, FOREIGN KEY(sessionId) REFERENCES sessions(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_session_parts_sessionId ON session_parts(sessionId)")
                db.execSQL("INSERT INTO session_parts(sessionId, pieceId, title, composer, catalogWorkId, composerId, musicalEra, durationMillis, notes) SELECT s.id, s.pieceId, s.pieceTitle, COALESCE(p.composer, ''), s.catalogWorkId, s.composerId, s.musicalEra, s.durationMillis, '' FROM sessions s LEFT JOIN pieces p ON s.pieceId = p.id")
                db.execSQL("CREATE TABLE IF NOT EXISTS profile_settings (id INTEGER NOT NULL PRIMARY KEY, username TEXT NOT NULL, avatar TEXT NOT NULL, theme TEXT NOT NULL, showcase TEXT NOT NULL)")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE catalog_works ADD COLUMN popular INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE catalog_works ADD COLUMN recommended INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE pieces ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE pieces ADD COLUMN instrument TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE pieces ADD COLUMN genre TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE pieces ADD COLUMN musicalEra TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sessions ADD COLUMN practiceDay TEXT")
                db.execSQL("ALTER TABLE sessions ADD COLUMN practiceZone TEXT")
                db.execSQL("ALTER TABLE sessions ADD COLUMN catalogWorkId TEXT")
                db.execSQL("ALTER TABLE sessions ADD COLUMN composerId TEXT")
                db.execSQL("ALTER TABLE sessions ADD COLUMN musicalEra TEXT NOT NULL DEFAULT ''")
                db.execSQL("CREATE TABLE IF NOT EXISTS saved_recordings (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, workKey TEXT NOT NULL, url TEXT NOT NULL, title TEXT NOT NULL, performer TEXT NOT NULL, platform TEXT NOT NULL, savedAt INTEGER NOT NULL, preferred INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_saved_recordings_workKey ON saved_recordings(workKey)")
                db.execSQL("CREATE TABLE IF NOT EXISTS badge_unlocks (achievementId TEXT NOT NULL PRIMARY KEY, unlockedAt INTEGER NOT NULL)")
            }
        }
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pieces ADD COLUMN catalogWorkId TEXT")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_pieces_catalogWorkId ON pieces(catalogWorkId)")
                db.execSQL("CREATE TABLE IF NOT EXISTS catalog_composers (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, birth TEXT NOT NULL, death TEXT NOT NULL, epoch TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS catalog_works (id TEXT NOT NULL PRIMARY KEY, composerId TEXT NOT NULL, title TEXT NOT NULL, subtitle TEXT NOT NULL, genre TEXT NOT NULL, searchTerms TEXT NOT NULL, catalogue TEXT NOT NULL, catalogueNumber TEXT NOT NULL, musicalKey TEXT NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_catalog_works_composerId ON catalog_works(composerId)")
                db.execSQL("CREATE TABLE IF NOT EXISTS catalog_cache (cacheKey TEXT NOT NULL PRIMARY KEY, payload TEXT NOT NULL, fetchedAt INTEGER NOT NULL)")
            }
        }
    }
}
class OpusRepository(private val db: OpusDatabase, private val achievements: AchievementStore) {
    private val dao = db.dao()
    val pieces = dao.observePieces()
    val sessions = dao.observeSessions()
    val parts = db.personalDao().parts()
    // One transactional snapshot avoids pairing newly restored sessions with old attribution rows.
    val journal = db.invalidationTracker.createFlow("pieces", "sessions", "session_parts", "session_piece_links").map {
        db.withTransaction { JournalSnapshot(dao.allPieces(), dao.allSessions(), db.personalDao().allParts(),db.journalEditsDao().allLinks()) }
    }.distinctUntilChanged()
    suspend fun save(piece: Piece) = db.withTransaction {
        val existing = if (piece.id > 0) dao.piece(piece.id) else null
        dao.save(piece.copy(createdAt = existing?.createdAt ?: piece.createdAt.takeIf { it > 0 } ?: System.currentTimeMillis()))
        achievements.evaluateInTransaction()
    }
    suspend fun delete(piece: Piece) = db.withTransaction { dao.delete(piece); achievements.evaluateInTransaction() }
    suspend fun complete(session: PracticeSession, parts: List<SessionPart> = emptyList()) = db.withTransaction {
        require(session.durationMillis >= 0)
        require(parts.isEmpty() || parts.sumOf { it.durationMillis } == session.durationMillis)
        require(parts.all { it.durationMillis >= 0 })
        val piece = session.pieceId?.let { dao.piece(it) }
        val sourceId = piece?.catalogWorkId ?: session.catalogWorkId
        val work = sourceId?.let { db.catalogDao().work(it) }
        val composer = work?.composerId?.let { db.catalogDao().composer(it) }
        val zone = java.time.ZoneId.systemDefault()
        val sessionId = dao.insert(session.copy(practiceDay = java.time.Instant.ofEpochMilli(session.completedAt).atZone(zone).toLocalDate().toString(),
            practiceZone = zone.id, catalogWorkId = sourceId, composerId = composer?.id ?: session.composerId,
            musicalEra = session.musicalEra.ifBlank { piece?.musicalEra.orEmpty() }.ifBlank { composer?.epoch.orEmpty() }))
        val entries = parts.ifEmpty { listOf(SessionPart(pieceId = session.pieceId, title = session.pieceTitle, durationMillis = session.durationMillis)) }
        db.personalDao().insertParts(entries.map { part ->
            val p = part.pieceId?.let { dao.piece(it) }
            val w = (part.catalogWorkId ?: p?.catalogWorkId)?.let { db.catalogDao().work(it) }
            val c = w?.composerId?.let { db.catalogDao().composer(it) }
            part.copy(id = 0, sessionId = sessionId, catalogWorkId = part.catalogWorkId ?: p?.catalogWorkId,
                composerId = part.composerId ?: c?.id ?: p?.let { ComposerIdentity.resolve(it, emptyMap()) },
                musicalEra = part.musicalEra.ifBlank { p?.musicalEra.orEmpty() }.ifBlank { c?.epoch.orEmpty() })
        })
        achievements.evaluateInTransaction()
    }
}

data class JournalSnapshot(val pieces:List<Piece>,val sessions:List<PracticeSession>,val parts:List<SessionPart>,val links:List<SessionPieceLink>)
