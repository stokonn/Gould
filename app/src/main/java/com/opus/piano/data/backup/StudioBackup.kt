package com.opus.piano.data.backup

import com.opus.piano.audio.DurableWav
import com.opus.piano.data.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/** Versioned personal metadata only. Audio bytes are never embedded in bounded journal JSON. */
internal object StudioBackup {
    fun document(rows:(String)->JSONArray): JSONObject = JSONObject()
        .put("audioFilesIncluded",false)
        .put("tables",JSONObject().apply { StudioSchema.tables.forEach { put(it,rows(it)) } })

    fun tables(studio: JSONObject): JSONObject {
        BackupJson.keys(studio,setOf("audioFilesIncluded","tables"))
        require(studio.get("audioFilesIncluded") == false) { "Journal backup must disclose excluded audio" }
        return studio.getJSONObject("tables").also { BackupJson.keys(it,StudioSchema.tables.toSet()) }
    }

    fun audio(row: JSONObject) = LocalAudio(
        id=row.getString("id"),createdAt=row.integer("createdAt"),stoppedAt=row.integer("stoppedAt"),
        state=row.getString("state"),relativeFile=row.getString("relativeFile"),format=row.getString("format"),
        sampleRate=row.integer("sampleRate").also {require(it in 8000..96000)}.toInt(),sampleCount=row.integer("sampleCount"),
        byteCount=row.integer("byteCount"),sha256=row.getString("sha256"),title=row.getString("title"),composer=row.getString("composer"),
        sessionToken=if(row.isNull("sessionToken"))null else row.getString("sessionToken"),error=row.getString("error"))

    suspend fun validate(db: OpusDatabase) {
        db.studioDao().allAudio().forEach { r ->
            require(UUID.fromString(r.id).toString()==r.id && r.createdAt>0 && r.stoppedAt>=0)
            require(r.state in setOf("Preparing","Recording","Finalizing","Saved","Recoverable","Missing","Deleting","Deleted"))
            require(r.relativeFile in setOf("${r.id}.wav","${r.id}.wav.part") && r.format=="pcm_s16le_wav")
            require(r.sampleCount in 0..DurableWav.MAX_DATA_BYTES/2 && r.byteCount in 0..DurableWav.MAX_DATA_BYTES+44)
            require(r.sha256.isEmpty() || r.sha256.matches(Regex("[a-f0-9]{64}")))
            require(r.title.length<=1000 && r.composer.length<=500 && r.error.length<=100 && (r.sessionToken==null || r.sessionToken.length in 1..256))
            if(r.state in setOf("Saved","Missing")) require(r.durationMillis>0 && r.byteCount==r.sampleCount*2+44 && r.sha256.length==64)
            db.studioDao().audioParts(r.id).forEach { p ->
                require(p.offsetSamples>=0 && p.pieceKey.length<=1024 && p.title.length<=1000 && p.composer.length<=500)
                if(r.state=="Saved")require(p.offsetSamples<=r.sampleCount)
            }
        }
        db.studioDao().events().forEach { e ->
            require(e.eventKey.length in 1..512 && e.subject.length in 1..512 && e.occurredAt>0 && e.value>0)
            when(e.kind) {
                StudioAchievementRules.RECORDING_SAVED -> require(e.eventKey==StudioAchievementRules.recordingKey(e.subject) && e.value==1L)
                StudioAchievementRules.PLAYBACK_VERIFIED -> require(e.subject.matches(Regex("[a-f0-9]{64}")) && e.eventKey=="${e.kind}:${e.subject}")
                StudioAchievementRules.NOTE_SAVED -> require(e.eventKey=="${e.kind}:${e.subject}" && e.value==1L)
                else -> error("Unsupported achievement event")
            }
        }
        db.openHelper.readableDatabase.query("SELECT achievementId,unlockedAt,acknowledgedAt FROM badge_notices").use { c ->
            while(c.moveToNext())require(c.getString(0).length in 1..256 && c.getLong(1)>0 && c.getLong(2)>=0)
        }
    }

    /** Merge, never delete destination recordings/events. Called inside journal restore transaction. */
    suspend fun merge(db: OpusDatabase, studio: JSONObject, audioDirectory: File) {
        val data=tables(studio)
        val audios=data.getJSONArray("local_audio")
        for(i in 0 until audios.length()) {
            val incoming=audio(audios.getJSONObject(i))
            val existing=db.studioDao().audio(incoming.id)
            if(existing!=null) {
                require(existing.sha256.isEmpty() || incoming.sha256.isEmpty() || existing.sha256==incoming.sha256) { "Conflicting recording identity; originals retained" }
                if(existing.state=="Missing" && existing.sha256==incoming.sha256) {
                    val file=File(audioDirectory,"${existing.id}.wav")
                    val verified=file.isFile && file.length()==existing.byteCount && runCatching {
                        LocalRecordingRepository.validateWav(file,existing.sampleRate);DurableWav.hash(file)==existing.sha256
                    }.getOrDefault(false)
                    if(verified)db.studioDao().updateAudio(existing.copy(state="Saved",error=""))
                }
                continue
            }
            var normalized=incoming
            if(incoming.state in setOf("Preparing","Recording","Finalizing","Deleting")) normalized=incoming.copy(state="Recoverable",error="Imported")
            if(incoming.state in setOf("Saved","Missing")) {
                val file=File(audioDirectory,"${incoming.id}.wav")
                val present=file.isFile && file.length()==incoming.byteCount && runCatching {
                    LocalRecordingRepository.validateWav(file,incoming.sampleRate);DurableWav.hash(file)==incoming.sha256
                }.getOrDefault(false)
                normalized=incoming.copy(state=if(present)"Saved" else "Missing",error=if(present)"" else "AudioExcluded")
            }
            db.studioDao().insertAudio(normalized)
        }
        val parts=data.getJSONArray("local_audio_parts")
        for(i in 0 until parts.length()) {
            val p=parts.getJSONObject(i)
            val incoming=LocalAudioPart(p.getString("recordingId"),p.integer("offsetSamples"),p.getString("pieceKey"),p.getString("title"),p.getString("composer"))
            val old=db.studioDao().audioParts(incoming.recordingId).firstOrNull {it.offsetSamples==incoming.offsetSamples}
            require(old==null || old==incoming) { "Conflicting recording association; originals retained" }
            db.studioDao().associate(incoming)
        }
        val known=db.studioDao().events().associateBy {it.eventKey}
        val events=data.getJSONArray("achievement_events")
        for(i in 0 until events.length()) {
            val e=events.getJSONObject(i)
            val incoming=AchievementEvent(e.getString("eventKey"),e.getString("kind"),e.getString("subject"),e.integer("occurredAt"),e.integer("value"))
            val old=known[incoming.eventKey]
            require(old==null || (old.kind==incoming.kind && old.subject==incoming.subject)) { "Conflicting event identity" }
            db.studioDao().event(incoming)
        }
        val notices=data.getJSONArray("badge_notices")
        for(i in 0 until notices.length()) {
            val n=notices.getJSONObject(i);val id=n.getString("achievementId");val ack=n.integer("acknowledgedAt")
            db.studioDao().notice(BadgeNotice(id,n.integer("unlockedAt"),ack))
            if(ack>0)db.studioDao().acknowledge(id,ack)
        }
    }
}
