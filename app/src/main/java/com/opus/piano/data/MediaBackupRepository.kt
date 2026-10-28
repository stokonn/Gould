package com.opus.piano.data

import android.content.Context
import android.util.AtomicFile
import com.opus.piano.audio.DurableWav
import com.opus.piano.data.backup.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.util.UUID

/** Explicit, local media backup. All bytes validate in staging before journal replacement.
 * A failed/interrupted operation leaves its receipt and originals available for retry. */
class MediaBackupRepository(
    context:Context,private val db:OpusDatabase,
    private val audioDirectory:File=File(context.filesDir,"recordings"),
    private val operationsDirectory:File=File(context.filesDir,"recording-restores"),
    private val journal:BackupRepository=BackupRepository(context,db,audioDirectory=audioDirectory),
    private val phase:(String)->Unit={}
) {
    companion object {private val gate=Mutex()}
    private fun operation(id:String):File {
        require(UUID.fromString(id).toString()==id)
        return File(operationsDirectory,id).also {require(it.canonicalFile.parentFile==operationsDirectory.canonicalFile)}
    }
    private fun receipt(directory:File,state:String) {
        val atomic=AtomicFile(File(directory,"receipt.json"));val output=atomic.startWrite()
        try {output.write(org.json.JSONObject().put("operation",directory.name).put("state",state).put("updatedAt",System.currentTimeMillis()).toString().toByteArray());atomic.finishWrite(output)}
        catch(error:Exception){atomic.failWrite(output);throw error}
        phase(state)
    }
    private fun rows(file:File):List<LocalAudio> {
        val root=BackupJson.parse(file);require(root.integer("version")==6L)
        val data=root.getJSONObject("sections").getJSONObject("studio").getJSONObject("data")
        val array=StudioBackup.tables(data).getJSONArray("local_audio")
        return (0 until array.length()).map {StudioBackup.audio(array.getJSONObject(it))}
    }
    suspend fun export(output:OutputStream)=withContext(Dispatchers.IO){gate.withLock {
        val snapshot=journal.exportSnapshot();val rows=rows(snapshot)
        require(rows.none {it.state in setOf("Preparing","Recording","Finalizing","Deleting")}) {"Stop recording before media export"}
        val entries=buildList {
            rows.filter {it.state!="Deleted"}.forEach {row->
                val candidates=if(row.state in setOf("Saved","Missing"))listOf(File(audioDirectory,"${row.id}.wav"))
                    else listOf(File(audioDirectory,"${row.id}.wav"),File(audioDirectory,"${row.id}.wav.part")).filter {it.isFile}
                if(row.state in setOf("Saved","Missing")) {
                    val file=candidates.single();LocalRecordingRepository.validateWav(file,row.sampleRate)
                    require(file.length()==row.byteCount && DurableWav.hash(file)==row.sha256) {"Audio unavailable or corrupt; journal-only export remains available"}
                }
                candidates.forEach {file->require(file.isFile);add(RecordingArchive.Entry("audio/${file.name}",file,DurableWav.hash(file),file.length()))}
            }
        }
        RecordingArchive.write(snapshot,entries,output)
    }}
    suspend fun receive(input:InputStream):String=withContext(Dispatchers.IO){gate.withLock {
        operationsDirectory.mkdirs();val id=UUID.randomUUID().toString()
        val staged=RecordingArchive.stage(input,operation(id))
        journal.validate(staged.journal);validateMedia(staged)
        receipt(staged.directory,"prepared");id
    }}
    private fun validateMedia(staged:RecordingArchive.Staged) {
        val rows=rows(staged.journal).associateBy {it.id}
        staged.entries.forEach {entry->
            val id=entry.file.name.substringBefore('.');val row=requireNotNull(rows[id]) {"Unowned archive recording"}
            require(row.state!="Deleted")
            if(row.state in setOf("Saved","Missing")) {
                require(entry.file.name=="${row.id}.wav" && entry.bytes==row.byteCount && entry.sha256==row.sha256)
                LocalRecordingRepository.validateWav(entry.file,row.sampleRate)
            }
        }
        rows.values.filter {it.state in setOf("Saved","Missing")}.forEach {row->
            require(staged.entries.any {it.name=="audio/${row.id}.wav"}) {"Media-inclusive archive is incomplete"}
        }
        require(rows.values.none {it.state in setOf("Preparing","Recording","Finalizing","Deleting")})
    }
    suspend fun restore(id:String)=withContext(Dispatchers.IO){gate.withLock {
        val staged=RecordingArchive.inspect(operation(id))
        journal.validate(staged.journal);validateMedia(staged)
        audioDirectory.mkdirs()
        // All collisions are checked before publishing any new file. Never overwrite originals.
        staged.entries.forEach {entry->val target=File(audioDirectory,entry.file.name)
            require(target.canonicalFile.parentFile==audioDirectory.canonicalFile)
            if(target.exists())require(target.isFile && target.length()==entry.bytes && DurableWav.hash(target)==entry.sha256) {"Existing recording conflicts; originals retained"}
        }
        staged.entries.forEach {entry->val target=File(audioDirectory,entry.file.name)
            if(!target.exists())Files.createLink(target.toPath(),entry.file.toPath())
        }
        receipt(staged.directory,"media_published")
        // On a crash, new files remain referenced by this replayable staging receipt;
        // the old journal is either intact or transactionally replaced with a saved snapshot.
        journal.replayPendingPreferences()
        receipt(staged.directory,"before_journal")
        journal.restore(staged.journal)
        receipt(staged.directory,"journal_restored")
        receipt(staged.directory,"complete")
    }}
    suspend fun pendingOperations():List<String> = withContext(Dispatchers.IO) {operationsDirectory.listFiles().orEmpty().filter {dir->
        dir.isDirectory && runCatching {UUID.fromString(dir.name).toString()==dir.name}.getOrDefault(false) &&
            runCatching {org.json.JSONObject(AtomicFile(File(dir,"receipt.json")).openRead().bufferedReader().use {it.readText()}).getString("state")!="complete"}.getOrDefault(true)
    }.map {it.name}}
}
