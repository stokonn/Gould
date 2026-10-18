package com.opus.piano.data

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase

/** Durable journal-owned identities. Never depend on the optional catalogue file. */
@Entity(tableName = "personal_catalogue_identities", indices = [Index(value = ["canonicalId", "kind"], unique = true)])
data class PersonalCatalogueIdentity(
    @PrimaryKey val identityKey: String, val canonicalId: String, val kind: String,
    val provider: String, val legacyId: String?, val evidence: String
)
@Entity(tableName = "personal_catalogue_anchors", primaryKeys = ["ownerType", "ownerId", "role"],
    indices = [Index("identityKey")], foreignKeys = [ForeignKey(entity = PersonalCatalogueIdentity::class,
        parentColumns = ["identityKey"], childColumns = ["identityKey"])])
data class PersonalCatalogueAnchor(
    val ownerType: String, val ownerId: String, val role: String, val identityKey: String,
    val originalTitle: String, val originalCreator: String, val legacyId: String?, val evidence: String
)
@Entity(tableName = "personal_catalogue_state")
data class PersonalCatalogueState(@PrimaryKey val id: Int = 1, val scope: String)

@Dao
interface PersonalCatalogueDao {
    @Query("SELECT * FROM personal_catalogue_state WHERE id=1") suspend fun scope():PersonalCatalogueState?
    @Query("SELECT * FROM personal_catalogue_anchors WHERE ownerType = :type AND ownerId = :id ORDER BY role")
    suspend fun anchors(type: String, id: String): List<PersonalCatalogueAnchor>
    @Query("SELECT * FROM personal_catalogue_identities WHERE identityKey = :key")
    suspend fun identity(key: String): PersonalCatalogueIdentity?
    @Query("SELECT * FROM personal_catalogue_identities WHERE identityKey > :after ORDER BY identityKey LIMIT :limit")
    suspend fun identities(after: String, limit: Int): List<PersonalCatalogueIdentity>
    @Query("SELECT * FROM personal_catalogue_anchors WHERE identityKey = :key ORDER BY ownerType, ownerId LIMIT 1")
    suspend fun snapshot(key: String): PersonalCatalogueAnchor?
}

/** SQL is shared by migration, fresh databases and legacy restore, not catalogue startup. */
object PersonalCatalogueBridge {
    private const val UUID_SQL = "lower(hex(randomblob(4))) || '-' || lower(hex(randomblob(2))) || '-4' || substr(lower(hex(randomblob(2))),2) || '-' || substr('89ab',abs(random() % 4)+1,1) || substr(lower(hex(randomblob(2))),2) || '-' || lower(hex(randomblob(6)))"
    val create = listOf(
        "CREATE TABLE IF NOT EXISTS personal_catalogue_identities (identityKey TEXT NOT NULL PRIMARY KEY, canonicalId TEXT NOT NULL, kind TEXT NOT NULL, provider TEXT NOT NULL, legacyId TEXT, evidence TEXT NOT NULL)",
        "CREATE UNIQUE INDEX IF NOT EXISTS index_personal_catalogue_identities_canonicalId_kind ON personal_catalogue_identities(canonicalId,kind)",
        "CREATE TABLE IF NOT EXISTS personal_catalogue_anchors (ownerType TEXT NOT NULL, ownerId TEXT NOT NULL, role TEXT NOT NULL, identityKey TEXT NOT NULL, originalTitle TEXT NOT NULL, originalCreator TEXT NOT NULL, legacyId TEXT, evidence TEXT NOT NULL, PRIMARY KEY(ownerType,ownerId,role), FOREIGN KEY(identityKey) REFERENCES personal_catalogue_identities(identityKey) ON UPDATE NO ACTION ON DELETE NO ACTION)",
        "CREATE INDEX IF NOT EXISTS index_personal_catalogue_anchors_identityKey ON personal_catalogue_anchors(identityKey)",
        "CREATE TABLE IF NOT EXISTS personal_catalogue_state (id INTEGER NOT NULL PRIMARY KEY, scope TEXT NOT NULL)"
    )
    // A view keeps migration and subsequent row triggers on exactly the same identity rules.
    private val sources = listOf(
        "SELECT 'piece' ownerType, CAST(id AS TEXT) ownerId, 'work' role, id pieceId, catalogWorkId legacyId, title, composer creator FROM pieces",
        "SELECT 'session', CAST(id AS TEXT), 'work', pieceId, catalogWorkId, pieceTitle, '' FROM sessions",
        "SELECT 'part', CAST(id AS TEXT), 'work', pieceId, catalogWorkId, title, composer FROM session_parts",
        "SELECT 'link', CAST(sessionId AS TEXT)||':'||pieceId, 'work', pieceId, catalogWorkId, title, composer FROM session_piece_links",
        "SELECT 'favorite', composerId, 'artist', NULL, composerId, COALESCE((SELECT name FROM catalog_composers WHERE id=composerId),composerId), '' FROM favorite_composers",
        "SELECT 'recording', CAST(id AS TEXT), 'work', CASE WHEN workKey LIKE 'piece:%' THEN CAST(substr(workKey,7) AS INTEGER) END, CASE WHEN workKey LIKE 'catalog:%' THEN substr(workKey,9) END, title, performer FROM saved_recordings",
        "SELECT 'session', CAST(id AS TEXT), 'artist', NULL, composerId, COALESCE((SELECT name FROM catalog_composers WHERE id=composerId),composerId), '' FROM sessions WHERE composerId IS NOT NULL",
        "SELECT 'part', CAST(id AS TEXT), 'artist', NULL, composerId, composer, '' FROM session_parts WHERE composerId IS NOT NULL",
        "SELECT 'link', CAST(sessionId AS TEXT)||':'||pieceId, 'artist', NULL, composerId, composer, '' FROM session_piece_links WHERE composerId IS NOT NULL"
    )
    private val seedView = "CREATE VIEW IF NOT EXISTS personal_catalogue_seed AS SELECT ownerType,ownerId,role,legacyId,title,creator, CASE WHEN role='work' AND pieceId IS NOT NULL AND COALESCE(legacyId,'')='' AND COALESCE((SELECT catalogWorkId FROM pieces WHERE id=pieceId),'')<>'' THEN 'legacy:work:'||(SELECT catalogWorkId FROM pieces WHERE id=pieceId) WHEN legacyId IS NOT NULL AND legacyId <> '' THEN 'legacy:'||role||':'||legacyId ELSE 'local:'||(SELECT scope FROM personal_catalogue_state WHERE id=1)||':'||CASE WHEN pieceId IS NOT NULL THEN 'piece:'||pieceId ELSE ownerType||':'||ownerId END END identityKey FROM (" + sources.joinToString(" UNION ALL ") + ")"
    private fun seed(where: String = "1") = listOf(
        "INSERT OR IGNORE INTO personal_catalogue_identities(identityKey,canonicalId,kind,provider,legacyId,evidence) SELECT identityKey,$UUID_SQL,role,CASE WHEN legacyId IS NULL OR legacyId='' THEN 'local' WHEN legacyId NOT GLOB '*[^0-9]*' THEN 'openopus' ELSE 'legacy' END,legacyId,'legacy-personal-snapshot;rights-unreviewed' FROM personal_catalogue_seed WHERE ($where) AND NOT EXISTS(SELECT 1 FROM personal_catalogue_identities i WHERE i.identityKey=personal_catalogue_seed.identityKey) AND NOT EXISTS(SELECT 1 FROM personal_catalogue_anchors a WHERE a.ownerType=personal_catalogue_seed.ownerType AND a.ownerId=personal_catalogue_seed.ownerId AND a.role=personal_catalogue_seed.role)",
        "INSERT OR IGNORE INTO personal_catalogue_anchors SELECT ownerType,ownerId,role,identityKey,title,creator,legacyId,'original-personal-snapshot;rights-unreviewed' FROM personal_catalogue_seed WHERE ($where) AND NOT EXISTS(SELECT 1 FROM personal_catalogue_anchors a WHERE a.ownerType=personal_catalogue_seed.ownerType AND a.ownerId=personal_catalogue_seed.ownerId AND a.role=personal_catalogue_seed.role)"
    )
    fun suspendCapture(db: SupportSQLiteDatabase) {
        listOf("pieces","sessions","session_parts","session_piece_links","favorite_composers","saved_recordings").forEach { table ->
            listOf("INSERT","UPDATE").forEach { event -> db.execSQL("DROP TRIGGER IF EXISTS personal_anchor_${table}_$event") }
        }
    }
    fun install(db: SupportSQLiteDatabase) {
        create.forEach { db.execSQL(it) }
        db.execSQL("INSERT OR IGNORE INTO personal_catalogue_state SELECT 1,$UUID_SQL")
        db.execSQL(seedView)
        seed().forEach { db.execSQL(it) }
        listOf("pieces" to "piece", "sessions" to "session", "session_parts" to "part", "session_piece_links" to "link", "favorite_composers" to "favorite", "saved_recordings" to "recording").forEach { (table, owner) ->
            val key = when(table) { "session_piece_links" -> "CAST(NEW.sessionId AS TEXT)||':'||NEW.pieceId"; "favorite_composers" -> "NEW.composerId"; else -> "CAST(NEW.id AS TEXT)" }
            // First capture wins: personal snapshots never change during catalogue reconciliation.
            listOf("INSERT", "UPDATE").forEach { event ->
                db.execSQL("CREATE TRIGGER IF NOT EXISTS personal_anchor_${table}_$event AFTER $event ON $table BEGIN " + seed("ownerType='$owner' AND ownerId=($key)").joinToString("; ") + "; END")
            }
        }
    }
    /** A fresh restored SQLite sequence must not reuse retained/tombstoned owner IDs. */
    fun reserveRestoredOwnerIds(db:SupportSQLiteDatabase) {
        for((table,owner) in listOf("pieces" to "piece","sessions" to "session","session_parts" to "part","saved_recordings" to "recording")) {
            var high=0L
            db.query("SELECT ownerId FROM personal_catalogue_anchors WHERE ownerType=?",arrayOf(owner)).use{c->
                while(c.moveToNext())c.getString(0).toLongOrNull()?.takeIf{it>high}?.let{high=it}
            }
            if(table=="pieces") {
                db.query("SELECT pieceId FROM sessions UNION ALL SELECT pieceId FROM session_parts UNION ALL SELECT pieceId FROM session_piece_links").use{c->while(c.moveToNext())if(!c.isNull(0))high=maxOf(high,c.getLong(0))}
                db.query("SELECT substr(workKey,7) FROM saved_recordings WHERE workKey LIKE 'piece:%'").use{c->while(c.moveToNext())c.getString(0).toLongOrNull()?.let{high=maxOf(high,it)}}
            }
            val current=db.query("SELECT seq FROM sqlite_sequence WHERE name=?",arrayOf(table)).use{c->if(c.moveToFirst())c.getLong(0) else null}
            if(high>0 && (current==null || current<high)) {
                if(current==null)db.execSQL("INSERT INTO sqlite_sequence(name,seq) VALUES(?,?)",arrayOf(table,high))
                else db.execSQL("UPDATE sqlite_sequence SET seq=? WHERE name=?",arrayOf(high,table))
            }
        }
    }
    /** Legacy replacement has no portable identity section. New scope prevents reused local IDs
     * from adopting an unrelated journal's identity. Retain the old registry as durable evidence. */
    fun prepareLegacyRestore(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM personal_catalogue_anchors")
        db.execSQL("UPDATE personal_catalogue_state SET scope=$UUID_SQL WHERE id=1")
    }
    val callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) { install(db) }
    }
}
