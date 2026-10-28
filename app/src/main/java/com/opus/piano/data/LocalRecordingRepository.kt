package com.opus.piano.data

import androidx.room.withTransaction
import com.opus.piano.audio.DurableWav
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** Personal audio is durable data, never a media cache. All paths are repository-owned IDs. */
class LocalRecordingRepository(private val db: OpusDatabase, private val directory: File) {
    val recordings = db.studioDao().recordings()
    private fun file(name: String): File {
        require(name.matches(Regex("[0-9a-f-]{36}\\.wav(?:\\.part)?")))
        val target = File(directory,name)
        require(target.canonicalFile.parentFile == directory.canonicalFile)
        return target
    }
    suspend fun prepare(rate: Int, title: String, composer: String, token: String?): Pair<LocalAudio,DurableWav> = withContext(Dispatchers.IO) {
        require(rate in 8000..96000)
        val id=UUID.randomUUID().toString()
        val row=LocalAudio(id,System.currentTimeMillis(),relativeFile="$id.wav.part",sampleRate=rate,
            title=title.take(1000),composer=composer.take(500),sessionToken=token?.takeIf { it.isNotBlank() })
        // Identity is durable before any sample or running state is published.
        db.studioDao().insertAudio(row)
        try {
            val writer=DurableWav.create(file(row.relativeFile),rate)
            val running=row.copy(state="Recording");db.studioDao().updateAudio(running)
            running to writer
        } catch(error:Exception) {
            db.studioDao().updateAudio(row.copy(state="Recoverable",error="Storage"));throw error
        }
    }
    suspend fun interrupted(id: String, reason: String) = withContext(Dispatchers.IO) {
        db.studioDao().audio(id)?.let { row ->
            if(row.state !in setOf("Saved","Deleted")) db.studioDao().updateAudio(row.copy(state="Recoverable",error=reason.take(100),stoppedAt=System.currentTimeMillis()))
        }
    }
    suspend fun finalize(id: String, writer: DurableWav): LocalAudio = withContext(Dispatchers.IO) {
        val row=requireNotNull(db.studioDao().audio(id))
        require(row.state in setOf("Recording","Recoverable","Finalizing"))
        db.studioDao().updateAudio(row.copy(state="Finalizing"))
        try {
            val saved=writer.finish(file("$id.wav"))
            require(saved.samples >= row.sampleRate / 1000)
            publish(row,saved.file,saved.samples,saved.sha256)
        } catch(error:Exception) { interrupted(id,"Finalize");throw error }
    }
    private suspend fun publish(row: LocalAudio, audio: File, samples: Long, hash: String): LocalAudio = db.withTransaction {
        val saved=row.copy(state="Saved",relativeFile=audio.name,sampleCount=samples,
            byteCount=audio.length(),sha256=hash,stoppedAt=row.stoppedAt.takeIf { it>0 } ?: System.currentTimeMillis(),error="")
        require(saved.durationMillis>0 && audio.length()==DurableWav.HEADER+samples*2)
        db.studioDao().updateAudio(saved)
        db.studioDao().event(AchievementEvent(StudioAchievementRules.recordingKey(hash),StudioAchievementRules.RECORDING_SAVED,hash,System.currentTimeMillis()))
        saved
    }
    /** Startup performs metadata reconciliation only; never capture or automatic publication. */
    suspend fun reconcile() = withContext(Dispatchers.IO) {
        db.studioDao().allAudio().filter { it.state in setOf("Preparing","Recording","Finalizing") }.forEach {
            interrupted(it.id,"Interrupted")
        }
    }
    suspend fun recover(id: String): LocalAudio = withContext(Dispatchers.IO) {
        val row=requireNotNull(db.studioDao().audio(id));require(row.state=="Recoverable")
        val published=file("$id.wav")
        if(published.exists()) {
            // Crash after no-clobber file publication but before Room commit.
            validateWav(published,row.sampleRate)
            return@withContext publish(row,published,(published.length()-DurableWav.HEADER)/2,DurableWav.hash(published))
        }
        DurableWav.recover(file("$id.wav.part"),row.sampleRate).use { finalize(id,it) }
    }
    suspend fun playable(id: String): File = withContext(Dispatchers.IO) {
        val row=requireNotNull(db.studioDao().audio(id));require(row.state=="Saved")
        val audio=file(row.relativeFile)
        validateWav(audio,row.sampleRate)
        require(audio.length()==row.byteCount && DurableWav.hash(audio)==row.sha256) { "Recording integrity mismatch; original retained" }
        audio
    }
    suspend fun exportOriginal(id: String, output: java.io.OutputStream) = withContext(Dispatchers.IO) {
        val row=requireNotNull(db.studioDao().audio(id))
        require(row.state!="Deleted")
        // Recovery export deliberately preserves unchanged bytes even when playback
        // integrity fails. The caller labels it as an original/unverified export.
        val source=file("$id.wav").takeIf { it.isFile } ?: file("$id.wav.part")
        source.inputStream().use { it.copyTo(output,64*1024) }
    }
    /** Only an explicit confirmed user deletion calls this; never cache clearing or retention. */
    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        val row=requireNotNull(db.studioDao().audio(id));require(row.state !in setOf("Preparing","Recording","Finalizing"))
        db.studioDao().updateAudio(row.copy(state="Deleting"))
        for(name in listOf("$id.wav","$id.wav.part")) { val audio=file(name);check(!audio.exists() || audio.delete()) { "Recording deletion can be retried" } }
        // Keep event evidence and a tombstone so a retry cannot recreate earned progress.
        db.studioDao().updateAudio(row.copy(state="Deleted",error=""))
    }
    suspend fun verifiedPlayback(id: String, playedMillis: Long) = withContext(Dispatchers.IO) {
        val row=requireNotNull(db.studioDao().audio(id));require(row.state=="Saved")
        if(playedMillis>=StudioAchievementRules.playbackThreshold(row.durationMillis)) {
            playable(id)
            db.studioDao().event(AchievementEvent("${StudioAchievementRules.PLAYBACK_VERIFIED}:${row.sha256}",StudioAchievementRules.PLAYBACK_VERIFIED,row.sha256,System.currentTimeMillis(),playedMillis))
        }
    }
    companion object {
        fun validateWav(audio: File, rate: Int) {
            require(audio.isFile && audio.length()>DurableWav.HEADER)
            val bytes=audio.length()-DurableWav.HEADER
            val header=ByteArray(DurableWav.HEADER);audio.inputStream().use { input ->java.io.DataInputStream(input).readFully(header) }
            require(header.contentEquals(DurableWav.header(rate,bytes))) { "Recording header invalid; original retained" }
        }
    }
}
