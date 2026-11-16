package com.opus.piano.data.catalogue

import android.content.Context
import android.util.AtomicFile
import androidx.room.withTransaction
import com.opus.piano.data.OpusDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID
import java.io.File
import java.security.MessageDigest

sealed interface CatalogueAccess<out T> {
    data class Available<T>(val value: T) : CatalogueAccess<T>
    data class Rejected(val reason:String="invalid_catalogue_request") : CatalogueAccess<Nothing>
    data class Unavailable(val reason: String = "local_catalogue_unavailable", val cause: Throwable? = null) : CatalogueAccess<Nothing>
}

/** Application-owned, optional local store. No caller can hold a DB across a generation swap.
 * Construction does no I/O. All open/copy/query work runs on IO, outside journal transactions. */
class LocalCatalogueStore(
    context: Context,
    private val personal: OpusDatabase,
    private val name: String = CatalogueDatabase.FILENAME,
    private val intake: CatalogueIntake? = null,
    private val opener: (String) -> CatalogueDatabase = { CatalogueDatabase.builder(context, it).build() },
    private val storage: CatalogueStorage? = null
) {
    private val directory = context.getDatabasePath(name).parentFile!!
    private val pointer = AtomicFile(java.io.File(directory, "$name.current"))
    private val lock = Mutex()
    private var opened: CatalogueDatabase? = null
    private fun selected(): String {
        if(!pointer.baseFile.exists()) return name
        val value = pointer.openRead().use { input -> input.readBytes().also { require(it.size <= 256) }.toString(Charsets.UTF_8) }
        require(value == name || value.matches(Regex(Regex.escape(name) + "\\.[0-9a-f-]{36}")))
        return value
    }
    private fun open(context:CataloguePerformance.Context = CataloguePerformance.Context()): CatalogueDatabase = opened ?: CataloguePerformance.measure("catalogue_db_open", "connection=cold", context) {
        opener(selected()).also { db ->
            try {
                val sql = db.openHelper.writableDatabase
                CataloguePerformance.measure("catalogue_search_index_availability", context=context) {
                    val fts = sql.query("SELECT sql FROM sqlite_master WHERE type='table' AND name='alias_fts'").use {
                        check(it.moveToFirst()) { "Catalogue alias search index is missing" }
                        it.getString(0)
                    }
                    val indexCount = sql.query("PRAGMA index_list('entities')").use { cursor ->
                        var count = 0
                        while (cursor.moveToNext()) count++
                        count
                    }
                    val ftsVersion = Regex("FTS[0-9]+", RegexOption.IGNORE_CASE).find(fts)?.value?.uppercase() ?: "unknown"
                    CataloguePerformance.event("catalogue_search_indexes", "outcome=ok fts=$ftsVersion entity_index_count=$indexCount",context)
                }
                opened = db
            } catch(e: Exception) { db.close(); throw e }
        }
    }
    private suspend fun <T> attempt(block: suspend () -> T): CatalogueAccess<T> = try {
        CatalogueAccess.Available(block())
    } catch(e: CancellationException) { throw e } catch (_: IllegalArgumentException) {
        CatalogueAccess.Rejected()
    } catch (e: Exception) {
        runCatching { opened?.close() }; opened = null
        CatalogueAccess.Unavailable(cause = e)
    }
    suspend fun <T> access(block: suspend (CatalogueRepository) -> T): CatalogueAccess<T> = access(CataloguePerformance.Context(),block)
    internal suspend fun <T> access(context:CataloguePerformance.Context,block: suspend (CatalogueRepository) -> T): CatalogueAccess<T> = withContext(Dispatchers.IO) {
        lock.withLock { attempt {
            val db=open(context)
            val scope=personal.personalCatalogueDao().scope()?.scope.orEmpty()
            val indexedScope=db.dao().state("personal_scope")?.value
            require(indexedScope==null || indexedScope==scope){"Catalogue belongs to a different journal scope"}
            block(CatalogueRepository(db))
        } }
    }
    /** At most 128 journal-owned identities per call. Progress and copied records commit together.
     * A new pass after completion picks up later personal rows; no journal refresh is triggered. */
    suspend fun initialize(): CatalogueAccess<Boolean> = initialize(CataloguePerformance.Context())
    internal suspend fun initialize(context:CataloguePerformance.Context): CatalogueAccess<Boolean> = withContext(Dispatchers.IO) {
        lock.withLock { attempt {
            val db=open(context)
            val scope=personal.personalCatalogueDao().scope()?.scope.orEmpty()
            val previous=db.dao().state("personal_scope")?.value
            if(previous!=null && previous!=scope){rebuildUnlocked();true}else admitted(CatalogueStorage.MAX_INTAKE){copyBatch(db)}
        } }
    }
    private suspend fun copyBatch(db: CatalogueDatabase): Boolean {
        val dao = db.dao()
        val scope=personal.personalCatalogueDao().scope()?.scope.orEmpty()
        val after = dao.state("copy_cursor")?.value.orEmpty()
        val rows = personal.personalCatalogueDao().identities(after, 128)
        db.withTransaction {
            val repository = CatalogueRepository(db)
            rows.forEach { identity ->
                currentCoroutineContext().ensureActive()
                val existing = dao.entity(identity.canonicalId)
                require(existing == null || existing.kind == identity.kind) { "Catalogue ID conflicts with a personal identity kind" }
                if(existing == null) {
                    val snapshot = personal.personalCatalogueDao().snapshot(identity.identityKey)
                    // Local snapshots have unreviewed rights and are NOT publication evidence.
                    val title = snapshot?.originalTitle?.ifBlank { null } ?: identity.legacyId ?: identity.identityKey
                    // These journal-owned identities support linking only; they are not public results.
                    val root = CatalogueEntity(identity.canonicalId,identity.kind,title,status="quarantined")
                    when(identity.kind) {
                        "work" -> repository.add(root,CatalogueWork(root.id))
                        "artist" -> repository.add(root,CatalogueArtist(root.id,sortName=title))
                        "arrangement" -> repository.add(root,CatalogueArrangement(root.id))
                        "album" -> repository.add(root,CatalogueAlbum(root.id))
                        "release" -> repository.add(root,CatalogueRelease(root.id))
                        "recording" -> repository.add(root,CatalogueRecording(root.id))
                        "collection" -> repository.add(root,CatalogueCollection(root.id))
                        else -> error("Unsupported personal identity kind")
                    }
                    if(identity.legacyId != null && identity.provider == "openopus") {
                        repository.map(CatalogueExternalIdentity("openopus",if(identity.kind=="artist") "composer" else "work",identity.legacyId,identity.canonicalId,identity.evidence,"observed"))
                    }
                } else {
                    repository.quarantinePrivateIdentity(identity.canonicalId)
                }
            }
            check(personal.personalCatalogueDao().scope()?.scope.orEmpty()==scope){"Journal changed during catalogue copy"}
            dao.state(CatalogueState("personal_scope",scope))
            dao.state(CatalogueState("copy_cursor", if(rows.size < 128) "" else rows.last().identityKey))
        }
        return rows.size < 128
    }
    /** Explicit retry/rebuild, never a destructive migration or automatic corruption handler.
     * Retain old/failed generations and sidecars. Publish only after the candidate validates. */
    suspend fun rebuild(): CatalogueAccess<Unit> = withContext(Dispatchers.IO) {
        lock.withLock { attempt {rebuildUnlocked()} }
    }
    /** Install a hash-verified schema-1 database as a new generation; failures leave the active pointer untouched. */
    suspend fun installGeneratedDatabase(source: File, expectedSha256: String, expectedBytes: Long): CatalogueAccess<Unit> = withContext(Dispatchers.IO) {
        lock.withLock { attempt {
            require(source.isFile && source.length() == expectedBytes && expectedBytes > 0)
            require(storage?.admitsInstall(expectedBytes) != false) { "insufficient catalogue storage reserve" }
            val digest = CataloguePerformance.measure("catalogue_install_hash", "bytes=$expectedBytes") { MessageDigest.getInstance("SHA-256").let { md ->
                source.inputStream().buffered().use { input ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) { val count = input.read(buffer); if (count < 0) break; md.update(buffer, 0, count) }
                }
                md.digest().joinToString("") { "%02x".format(it) }
            } }
            require(digest == expectedSha256)
            val candidateName = "$name.${UUID.randomUUID()}"
            val candidateFile = File(directory, candidateName)
            directory.mkdirs()
            CataloguePerformance.measure("catalogue_install_copy", "bytes=$expectedBytes") { source.inputStream().buffered(1024 * 1024).use { input ->
                candidateFile.outputStream().buffered(1024 * 1024).use { output -> input.copyTo(output, 1024 * 1024) }
            } }
            val candidate = opener(candidateName)
            try {
                val sql = CataloguePerformance.measure("catalogue_candidate_open") { candidate.openHelper.writableDatabase }
                require(sql.version == 1)
                CataloguePerformance.measure("catalogue_integrity_check") { sql.query("PRAGMA integrity_check").use { check(it.moveToFirst() && it.getString(0) == "ok") } }
                CataloguePerformance.measure("catalogue_foreign_key_check") { sql.query("PRAGMA foreign_key_check").use { check(!it.moveToFirst()) } }
                CataloguePerformance.measure("catalogue_provenance_check") {
                    sql.query("SELECT COUNT(*) FROM entities e WHERE NOT EXISTS (SELECT 1 FROM assertions a JOIN rights r ON r.id=a.rightsId JOIN sources s ON s.id=a.sourceId WHERE a.entityId=e.id AND a.field='title' AND a.selected=1 AND r.reviewState='approved')").use { check(it.moveToFirst() && it.getLong(0) == 0L) }
                }
                CataloguePerformance.measure("catalogue_policy_checks") { listOf(
                    "SELECT 1 FROM sources WHERE provider <> 'musicbrainz' LIMIT 1",
                    "SELECT 1 FROM external_identities WHERE provider <> 'musicbrainz' LIMIT 1",
                    "SELECT 1 FROM rights WHERE license <> 'CC0-1.0' OR reviewState <> 'approved' LIMIT 1",
                    "SELECT 1 FROM assertions WHERE field NOT IN ('title','type','sort-name','disambiguation','birth','death') LIMIT 1",
                    "SELECT 1 FROM documents LIMIT 1",
                    "SELECT 1 FROM media LIMIT 1",
                    "SELECT 1 FROM entities WHERE kind NOT IN ('work','arrangement','artist','album','release','recording','collection') LIMIT 1"
                ).forEach { query -> sql.query(query).use { check(!it.moveToFirst()) { "Catalogue contains unsupported source or metadata" } } } }
                CataloguePerformance.measure("catalogue_fts_check") {
                    sql.query("SELECT sql FROM sqlite_master WHERE type='table' AND name='alias_fts'").use {
                        check(it.moveToFirst() && it.getString(0).contains("VIRTUAL TABLE", ignoreCase = true)) { "Catalogue alias FTS index is unavailable" }
                    }
                }
                while (!copyBatch(candidate)) currentCoroutineContext().ensureActive()
                candidate.dao().state(CatalogueState("generation", UUID.randomUUID().toString()))
                val previous = opened
                previous?.close()
                opened = null
                val stream = pointer.startWrite()
                try { stream.write(candidateName.toByteArray(Charsets.UTF_8)); pointer.finishWrite(stream) }
                catch (e: Exception) { pointer.failWrite(stream); throw e }
                opened = candidate
            } catch (e: Exception) { candidate.close(); throw e }
        } }
    }
    private suspend fun <T> admitted(bytes:Long,write:suspend ()->T):T =
        if(intake==null)write() else intake.commit(bytes,write)
    private suspend fun rebuildUnlocked() {
        val active=selected()
        val existing=directory.listFiles().orEmpty().filter {it.isFile && (it.name==active || it.name.startsWith("$active-"))}
            .fold(0L){total,file->(total+file.length().coerceAtMost(CatalogueStorage.MAX_TOTAL)).coerceAtMost(CatalogueStorage.MAX_TOTAL)}
        // Keep the original generation, staging, rebuilt indexes and journal headroom together.
        admitted(existing*3+CatalogueStorage.MAX_INTAKE){rebuildGeneration()}
    }
    private suspend fun rebuildGeneration() {
            val candidateName = "$name.${UUID.randomUUID()}"
            val candidate = opener(candidateName)
            try {
                candidate.openHelper.writableDatabase
                val previous=try {open()} catch(e:Exception) {
                    when(e) {
                        is PreservedCatalogueCorruption,is android.database.sqlite.SQLiteDatabaseCorruptException->null
                        else->throw e
                    }
                }
                val scope=personal.personalCatalogueDao().scope()?.scope.orEmpty()
                val priorScope=previous?.dao()?.state("personal_scope")?.value
                // An explicit same-journal rebuild repairs indexes without abandoning the accepted
                // ledger. A restored different journal keeps its old generation intact and isolated.
                if(previous!=null && (priorScope==null || priorScope==scope))copyLedger(previous,candidate)
                if(previous==null)candidate.dao().state(CatalogueState("retained_corrupt_generation",selected()))
                while(!copyBatch(candidate)) { currentCoroutineContext().ensureActive() }
                check(personal.personalCatalogueDao().scope()?.scope.orEmpty()==scope){"Journal changed during catalogue rebuild"}
                candidate.dao().state(CatalogueState("generation",UUID.randomUUID().toString()))
                candidate.openHelper.readableDatabase.query("PRAGMA foreign_key_check").use { check(!it.moveToFirst()) }
                candidate.openHelper.readableDatabase.query("PRAGMA integrity_check").use { check(it.moveToFirst() && it.getString(0)=="ok") }
                directory.mkdirs()
                val stream = pointer.startWrite()
                try { stream.write(candidateName.toByteArray(Charsets.UTF_8)); pointer.finishWrite(stream) }
                catch(e: Exception) { pointer.failWrite(stream); throw e }
                opened?.close(); opened = candidate
            } catch(e: Exception) { candidate.close(); throw e }
    }
    private suspend fun copyLedger(previous:CatalogueDatabase,candidate:CatalogueDatabase)=candidate.withTransaction {
        val source=previous.openHelper.readableDatabase
        val destination=candidate.openHelper.writableDatabase
        // Dependency order is explicit. FTS is regenerated by alias insertion, never copied as
        // opaque shadow tables. A CursorWindow and one bound row keep retained history off heap.
        val tables=listOf("entities","works","arrangements","artists","albums","releases","recordings","collections",
            "sources","rights","external_identities","identity_revisions","redirect_revisions","aliases","assertions","relationships","documents","media","catalogue_state")
        for(table in tables) {
            source.query("SELECT * FROM `$table`").use {cursor->
                val columns=cursor.columnNames.joinToString(","){"`$it`"}
                val placeholders=cursor.columnNames.joinToString(","){"?"}
                val statement=destination.compileStatement("INSERT INTO `$table` ($columns) VALUES ($placeholders)")
                try {
                    while(cursor.moveToNext()) {
                        currentCoroutineContext().ensureActive()
                        if(table=="catalogue_state" && cursor.getString(cursor.getColumnIndexOrThrow("key")) in setOf("copy_cursor","personal_scope"))continue
                        statement.clearBindings()
                        for(index in 0 until cursor.columnCount)when(cursor.getType(index)) {
                            android.database.Cursor.FIELD_TYPE_NULL->statement.bindNull(index+1)
                            android.database.Cursor.FIELD_TYPE_INTEGER->statement.bindLong(index+1,cursor.getLong(index))
                            android.database.Cursor.FIELD_TYPE_FLOAT->statement.bindDouble(index+1,cursor.getDouble(index))
                            android.database.Cursor.FIELD_TYPE_BLOB->statement.bindBlob(index+1,cursor.getBlob(index))
                            else->statement.bindString(index+1,cursor.getString(index))
                        }
                        statement.executeInsert()
                    }
                }finally {statement.close()}
            }
        }
    }
    suspend fun close() = withContext(Dispatchers.IO) { lock.withLock { opened?.close(); opened = null } }
}
