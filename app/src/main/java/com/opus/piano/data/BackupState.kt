package com.opus.piano.data

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

@Entity(tableName="backup_restore_state")
data class BackupRestoreState(@PrimaryKey val id:Int=1,val generation:String,val preferencesJson:String,
    val preferencesSha256:String,val status:String,val sourceSha256:String,val previousSnapshot:String)
@Entity(tableName="recovery_evidence")
data class RecoveryEvidence(@PrimaryKey val id:String,val rawBytes:ByteArray,val payloadSha256:String,
    val updatedAt:Long,val archivedAt:Long,val origin:String,val classification:String,val completionToken:String?)
@Entity(tableName="practice_completion_evidence",primaryKeys=["token","durationMillis"])
data class CompletionEvidence(val token:String,val durationMillis:Long)
@Dao
interface BackupStateDao {
    @Query("SELECT * FROM backup_restore_state WHERE id=1") suspend fun state():BackupRestoreState?
    @Query("SELECT * FROM backup_restore_state WHERE id=1") fun observeState():Flow<BackupRestoreState?>
    @Upsert suspend fun state(value:BackupRestoreState)
    @Query("SELECT * FROM recovery_evidence WHERE id=:id") suspend fun evidence(id:String):RecoveryEvidence?
    @Query("SELECT * FROM recovery_evidence ORDER BY id") suspend fun evidence():List<RecoveryEvidence>
    @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun evidence(value:RecoveryEvidence)
    @Query("SELECT * FROM practice_completion_evidence WHERE token=:token ORDER BY durationMillis") suspend fun completed(token:String):List<CompletionEvidence>
    @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun completed(value:CompletionEvidence)
}
object BackupState {
    val ddl=listOf(
        "CREATE TABLE IF NOT EXISTS backup_restore_state (id INTEGER NOT NULL PRIMARY KEY, generation TEXT NOT NULL, preferencesJson TEXT NOT NULL, preferencesSha256 TEXT NOT NULL, status TEXT NOT NULL, sourceSha256 TEXT NOT NULL, previousSnapshot TEXT NOT NULL)",
        "CREATE TABLE IF NOT EXISTS recovery_evidence (id TEXT NOT NULL PRIMARY KEY, rawBytes BLOB NOT NULL, payloadSha256 TEXT NOT NULL, updatedAt INTEGER NOT NULL, archivedAt INTEGER NOT NULL, origin TEXT NOT NULL, classification TEXT NOT NULL, completionToken TEXT)",
        "CREATE TABLE IF NOT EXISTS practice_completion_evidence (token TEXT NOT NULL, durationMillis INTEGER NOT NULL, PRIMARY KEY(token,durationMillis))"
    )
    fun install(db:SupportSQLiteDatabase) {
        ddl.forEach {db.execSQL(it)}
        db.execSQL("INSERT OR IGNORE INTO practice_completion_evidence SELECT completionToken,durationMillis FROM sessions WHERE completionToken IS NOT NULL AND completionToken<>''")
        db.execSQL("CREATE TRIGGER IF NOT EXISTS completion_evidence_insert AFTER INSERT ON sessions WHEN NEW.completionToken IS NOT NULL AND NEW.completionToken<>'' BEGIN INSERT OR IGNORE INTO practice_completion_evidence SELECT NEW.completionToken,NEW.durationMillis WHERE NOT EXISTS(SELECT 1 FROM practice_completion_evidence WHERE token=NEW.completionToken AND durationMillis=NEW.durationMillis); END")
    }
    val callback=object:RoomDatabase.Callback(){override fun onCreate(db:SupportSQLiteDatabase){install(db)}}
    fun sha(bytes:ByteArray):String=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}
    fun rawPayload(db:OpusDatabase,table:String,id:Long,column:String="payload",key:String="id"):ByteArray {
        val sql=db.openHelper.readableDatabase
        val length=sql.query("SELECT length(CAST(`$column` AS BLOB)) FROM `$table` WHERE `$key`=?",arrayOf(id)).use {c->check(c.moveToFirst());c.getLong(0)}
        require(length in 0..(24L*1024*1024)){"Recovery exceeds portable resource limit; original retained"}
        val output=java.io.ByteArrayOutputStream(length.toInt());var offset=1L
        while(offset<=length){sql.query("SELECT substr(CAST(`$column` AS BLOB),?,65536) FROM `$table` WHERE `$key`=?",arrayOf(offset,id)).use {c->check(c.moveToFirst());val bytes=c.getBlob(0);output.write(bytes);offset+=bytes.size}}
        return output.toByteArray()
    }
    fun rawDraft(db:OpusDatabase):Pair<ByteArray,Long>?=db.openHelper.readableDatabase.query("SELECT updatedAt FROM practice_drafts WHERE id=1").use {c->if(c.moveToFirst()) rawPayload(db,"practice_drafts",1) to c.getLong(0) else null}
    suspend fun preserve(db:OpusDatabase,raw:ByteArray,updated:Long,origin:String,classification:String,token:String?=null) {
        val hash=sha(raw)
        val completion=token?.takeIf{it.length in 1..256 && it.isNotBlank()}
        val id=sha("$origin:$updated:$hash:$classification:$completion".toByteArray(Charsets.UTF_8))
        db.backupStateDao().evidence(RecoveryEvidence(id,raw,hash,updated,System.currentTimeMillis(),origin,classification,completion))
    }
}
