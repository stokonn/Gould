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

sealed interface CatalogueAccess<out T> {
    data class Available<T>(val value: T) : CatalogueAccess<T>
    data class Unavailable(val reason: String = "local_catalogue_unavailable") : CatalogueAccess<Nothing>
}

/** Application-owned, optional local store. No caller can hold a DB across a generation swap.
 * Construction does no I/O. All open/copy/query work runs on IO, outside journal transactions. */
class LocalCatalogueStore(
    context: Context,
    private val personal: OpusDatabase,
    private val name: String = CatalogueDatabase.FILENAME,
    private val opener: (String) -> CatalogueDatabase = { CatalogueDatabase.builder(context, it).build() }
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
    private fun open(): CatalogueDatabase = opened ?: opener(selected()).also { db ->
        try { db.openHelper.writableDatabase; opened = db } catch(e: Exception) { db.close(); throw e }
    }
    private suspend fun <T> attempt(block: suspend () -> T): CatalogueAccess<T> = try {
        CatalogueAccess.Available(block())
    } catch(e: CancellationException) { throw e } catch (_: Exception) {
        runCatching { opened?.close() }; opened = null
        CatalogueAccess.Unavailable()
    }
    suspend fun <T> access(block: suspend (CatalogueRepository) -> T): CatalogueAccess<T> = withContext(Dispatchers.IO) {
        lock.withLock { attempt { block(CatalogueRepository(open())) } }
    }
    /** At most 128 journal-owned identities per call. Progress and copied records commit together.
     * A new pass after completion picks up later personal rows; no journal refresh is triggered. */
    suspend fun initialize(): CatalogueAccess<Boolean> = withContext(Dispatchers.IO) {
        lock.withLock { attempt { copyBatch(open()) } }
    }
    private suspend fun copyBatch(db: CatalogueDatabase): Boolean {
        val dao = db.dao()
        val after = dao.state("copy_cursor")?.value.orEmpty()
        val rows = personal.personalCatalogueDao().identities(after, 128)
        db.withTransaction {
            val repository = CatalogueRepository(db)
            rows.forEach { identity ->
                currentCoroutineContext().ensureActive()
                if(dao.entity(identity.canonicalId) == null) {
                    val snapshot = personal.personalCatalogueDao().snapshot(identity.identityKey)
                    // Local snapshots have unreviewed rights and are NOT publication evidence.
                    val title = snapshot?.originalTitle?.ifBlank { null } ?: identity.legacyId ?: identity.identityKey
                    val root = CatalogueEntity(identity.canonicalId,identity.kind,title)
                    when(identity.kind) {
                        "work" -> repository.add(root,CatalogueWork(root.id))
                        "artist" -> repository.add(root,CatalogueArtist(root.id,sortName=title))
                        else -> error("Unsupported legacy identity kind")
                    }
                    if(identity.legacyId != null && identity.provider == "openopus") {
                        repository.map(CatalogueExternalIdentity("openopus",if(identity.kind=="artist") "composer" else "work",identity.legacyId,identity.canonicalId,identity.evidence,"observed"))
                    }
                }
            }
            dao.state(CatalogueState("copy_cursor", if(rows.size < 128) "" else rows.last().identityKey))
        }
        return rows.size < 128
    }
    /** Explicit retry/rebuild, never a destructive migration or automatic corruption handler.
     * Retain old/failed generations and sidecars. Publish only after the candidate validates. */
    suspend fun rebuild(): CatalogueAccess<Unit> = withContext(Dispatchers.IO) {
        lock.withLock { attempt {
            val candidateName = "$name.${UUID.randomUUID()}"
            val candidate = opener(candidateName)
            try {
                candidate.openHelper.writableDatabase
                while(!copyBatch(candidate)) { currentCoroutineContext().ensureActive() }
                candidate.openHelper.readableDatabase.query("PRAGMA foreign_key_check").use { check(!it.moveToFirst()) }
                candidate.openHelper.readableDatabase.query("PRAGMA integrity_check").use { check(it.moveToFirst() && it.getString(0)=="ok") }
                directory.mkdirs()
                val stream = pointer.startWrite()
                try { stream.write(candidateName.toByteArray(Charsets.UTF_8)); pointer.finishWrite(stream) }
                catch(e: Exception) { pointer.failWrite(stream); throw e }
                opened?.close(); opened = candidate
            } catch(e: Exception) { candidate.close(); throw e }
        } }
    }
    suspend fun close() = withContext(Dispatchers.IO) { lock.withLock { opened?.close(); opened = null } }
}
