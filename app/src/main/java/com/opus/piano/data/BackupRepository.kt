package com.opus.piano.data

import android.content.Context
import android.util.Base64
import androidx.room.Room
import androidx.room.withTransaction
import com.opus.piano.OpusApplication
import com.opus.piano.data.backup.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/** v5 critical journal snapshot; historical 1–4 normalization stays in HistoricalBackup. */
class BackupRepository(
    private val context:Context,private val db:OpusDatabase,
    private val preferences:PortablePreferences=PortablePreferences(context,live=(context.applicationContext as? OpusApplication)?.database===db),
    private val files:BackupFiles=BackupFiles(),private val storageDirectory:File=File(context.filesDir,"personal-backups"),private val phase:(String)->Unit={}
) {
    companion object {
        const val MAX_BYTES=BackupJson.MAX_BYTES
        val tables=HistoricalBackup.tables
        val journalTables=tables.filterNot {it in setOf("catalog_cache","practice_recovery_archives")}
        val identityTables=listOf("personal_catalogue_identities","personal_catalogue_anchors","personal_catalogue_state")
        private val gate=Mutex()
    }
    private val directory get()=storageDirectory
    private val legacy get()=HistoricalBackup(context,db)
    suspend fun exportLegacy():String=legacy.export()
    private class Budget {var chars=0L;var nodes=0L
        fun add(chars:Int,nodes:Int){this.chars+=chars;this.nodes+=nodes;require(this.chars<=MAX_BYTES && this.nodes<=500000){"Backup exceeds safe resource limits; nothing was truncated"}}
    }
    private fun rows(table:String,budget:Budget=Budget()):JSONArray {
        val array=JSONArray()
        db.openHelper.readableDatabase.query("SELECT * FROM `$table` ORDER BY rowid").use {c->
            while(c.moveToNext()) {
                require(array.length()<100000){"Too many backup records; nothing was truncated"}
                array.put(JSONObject().apply {c.columnNames.forEachIndexed {i,key->
                    val value:Any=when(c.getType(i)){android.database.Cursor.FIELD_TYPE_NULL->JSONObject.NULL;android.database.Cursor.FIELD_TYPE_INTEGER->c.getLong(i);else->c.getString(i)}
                    budget.add(if(value is String)value.length else 0,1)
                    put(key,value)
                }})
            }
        };return array
    }
    private fun rawRows(table:String,budget:Budget=Budget()):JSONArray {
        val a=JSONArray()
        db.openHelper.readableDatabase.query("SELECT id,updatedAt${if(table=="practice_recovery_archives") ",archivedAt" else ""} FROM `$table` ORDER BY id").use {c->
            while(c.moveToNext()) {
                val id=c.getLong(0);val raw=BackupState.rawPayload(db,table,id);budget.add((raw.size.toLong()*4/3).toInt(),6)
                a.put(JSONObject().put("id",id).put("updatedAt",c.getLong(1)).put("rawBase64",Base64.encodeToString(raw,Base64.NO_WRAP)).put("sha256",BackupState.sha(raw)).apply {if(table=="practice_recovery_archives")put("archivedAt",c.getLong(2))})
            }
        };return a
    }
    private fun evidenceRows(budget:Budget):JSONArray {
        val a=JSONArray()
        db.openHelper.readableDatabase.query("SELECT id,payloadSha256,updatedAt,archivedAt,origin,classification,completionToken,rowid FROM recovery_evidence ORDER BY id").use {c->while(c.moveToNext()){
            val raw=BackupState.rawPayload(db,"recovery_evidence",c.getLong(7),"rawBytes","rowid");budget.add((raw.size.toLong()*4/3).toInt(),9)
            a.put(JSONObject().put("id",c.getString(0)).put("sha256",c.getString(1)).put("updatedAt",c.getLong(2)).put("archivedAt",c.getLong(3)).put("origin",c.getString(4)).put("classification",c.getString(5)).put("completionToken",if(c.isNull(6))JSONObject.NULL else c.getString(6)).put("rawBase64",Base64.encodeToString(raw,Base64.NO_WRAP)))
        }};return a
    }
    private suspend fun document():JSONObject=db.withTransaction {
        PersonalCatalogueBridge.install(db.openHelper.writableDatabase);BackupState.install(db.openHelper.writableDatabase)
        val budget=Budget()
        val local=db.practiceDao().localNow() ?: LocalAppState()
        fun section(data:JSONObject)=JSONObject().put("version",1).put("data",data).put("sha256",BackupJson.hash(data))
        val root=JSONObject().put("format","goldberg-backup").put("version",5).put("journalSchema",10).put("createdAt",System.currentTimeMillis())
        val journal=JSONObject();journalTables.forEach {journal.put(it,rows(it,budget))}
        val identities=JSONObject();identityTables.forEach {identities.put(it,rows(it,budget))}
        val pending=db.backupStateDao().state()?.takeIf{it.status=="pending"}
        val desired=pending?.let{BackupJson.parse(it.preferencesJson).also{o->require(BackupJson.hash(o)==it.preferencesSha256)}} ?: preferences.snapshot()
        val settings=desired.put("local",JSONObject().put("onboardingDone",local.onboardingDone).put("reminderEnabled",local.reminderEnabled).put("reminderHour",local.reminderHour).put("reminderMinute",local.reminderMinute))
        root.put("sections",JSONObject().put("journal",section(journal)).put("identities",section(identities)).put("preferences",section(settings))
            .put("recovery",section(JSONObject().put("drafts",rawRows("practice_drafts",budget)).put("archives",rawRows("practice_recovery_archives",budget)).put("evidence",evidenceRows(budget)).put("completions",rows("practice_completion_evidence",budget)))))
        root.put("sha256",BackupJson.hash(root));root
    }
    suspend fun export():String=withContext(Dispatchers.IO) {
        val document=document();BackupJson.encodedSize(document)
        java.io.ByteArrayOutputStream().use {out->BackupJson.write(document,out);out.toString("UTF-8")}
    }
    suspend fun exportTo(file:File):File=withContext(Dispatchers.IO){files.publish(file,document())}
    suspend fun exportSnapshot():File=exportTo(File(directory,"export-${UUID.randomUUID()}.json"))
    suspend fun receive(input:java.io.InputStream):File=withContext(Dispatchers.IO){files.receive(File(directory,"input-${UUID.randomUUID()}.json"),input)}
    private fun sections(root:JSONObject):Map<String,JSONObject> {
        BackupJson.keys(root,setOf("format","version","journalSchema","createdAt","sections","sha256"))
        require(root.getString("format")=="goldberg-backup" && root.integer("version")==5L && root.integer("journalSchema")==10L && root.integer("createdAt")>0)
        val expected=root.getString("sha256");root.remove("sha256")
        try {require(BackupJson.hash(root)==expected){"Backup integrity mismatch"}} finally {root.put("sha256",expected)}
        val sections=root.getJSONObject("sections");BackupJson.keys(sections,setOf("journal","identities","preferences","recovery"))
        return sections.keys().asSequence().associateWith {key->
            val section=sections.getJSONObject(key);BackupJson.keys(section,setOf("version","sha256","data"));require(section.integer("version")==1L)
            section.getJSONObject("data").also {require(BackupJson.hash(it)==section.getString("sha256")){"Backup section integrity mismatch"}}
        }
    }
    private fun raw(row:JSONObject):ByteArray {
        val encoded=row.getString("rawBase64");require(encoded.length<=MAX_BYTES)
        val decoded=Base64.decode(encoded,Base64.NO_WRAP)
        require(Base64.encodeToString(decoded,Base64.NO_WRAP)==encoded && BackupState.sha(decoded)==row.getString("sha256")){"Recovery byte integrity mismatch"}
        require(row.integer("updatedAt")>=0);return decoded
    }
    private fun insertRows(target:OpusDatabase,table:String,array:JSONArray) {
        val sql=target.openHelper.writableDatabase
        val columns=sql.query("PRAGMA table_info(`$table`)").use {c->buildList {while(c.moveToNext())add(Triple(c.getString(1),c.getString(2),c.getInt(3)==1))}}
        require(array.length()<=100000)
        for(i in 0 until array.length()) {
            val row=array.getJSONObject(i);BackupJson.keys(row,columns.map{it.first}.toSet())
            val values=columns.map<Triple<String,String,Boolean>,Any?> {(key,type,required)->val v=row.get(key);when {v===JSONObject.NULL->{require(!required);null};type=="INTEGER"->{require(v is Long || v is Int);(v as Number).toLong()};else->{require(v is String);v}}}.toTypedArray()
            sql.execSQL("INSERT INTO `$table` (${columns.joinToString {"`${it.first}`"}}) VALUES (${columns.joinToString {"?"}})",values)
        }
    }
    private fun insertRecovery(target:OpusDatabase,recovery:JSONObject,includeDraft:Boolean=true) {
        val sql=target.openHelper.writableDatabase
        for(table in listOf("drafts","archives")) {
            val array=recovery.getJSONArray(table)
            for(i in 0 until array.length()) {
                val row=array.getJSONObject(i);val bytes=raw(row)
                if(table=="drafts" && includeDraft) sql.execSQL("INSERT INTO practice_drafts VALUES(1,CAST(? AS TEXT),?)",arrayOf(bytes,row.integer("updatedAt")))
                if(table=="archives") sql.execSQL("INSERT INTO practice_recovery_archives(id,payload,updatedAt,archivedAt) VALUES(?,CAST(? AS TEXT),?,?)",arrayOf(row.integer("id"),bytes,row.integer("updatedAt"),row.integer("archivedAt")))
            }
        }
    }
    private suspend fun validate(root:JSONObject):String {
        if(root.integer("version")!=5L)return legacy.validate(root.toString())
        val s=sections(root);val settings=s.getValue("preferences")
        BackupJson.keys(settings,setOf("appearance","locale","previousMeteredConsent","local"))
        val portable=JSONObject(settings.toString()).apply{remove("local")};PortablePreferences.validate(portable)
        val local=settings.getJSONObject("local");BackupJson.keys(local,setOf("onboardingDone","reminderEnabled","reminderHour","reminderMinute"))
        require(local.get("onboardingDone") is Boolean && local.get("reminderEnabled") is Boolean && local.integer("reminderHour") in 0..23 && local.integer("reminderMinute") in 0..59)
        val journal=s.getValue("journal");BackupJson.keys(journal,journalTables.toSet())
        // Reuse all historical semantic checks, without passing raw recovery bytes through strings.
        val legacyTables=JSONObject().apply{journal.keys().forEach{k->put(k,journal.get(k))}}.put("catalog_cache",JSONArray()).put("practice_recovery_archives",JSONArray())
        val summary=legacy.validateTables(legacyTables)
        val identities=s.getValue("identities");BackupJson.keys(identities,identityTables.toSet())
        val recovery=s.getValue("recovery");BackupJson.keys(recovery,setOf("drafts","archives","evidence","completions"))
        val drafts=recovery.getJSONArray("drafts");require(drafts.length()<=1)
        for(i in 0 until drafts.length()){val row=drafts.getJSONObject(i);BackupJson.keys(row,setOf("id","updatedAt","rawBase64","sha256"));require(row.integer("id")==1L);raw(row)}
        val archives=recovery.getJSONArray("archives");for(i in 0 until archives.length()){val row=archives.getJSONObject(i);BackupJson.keys(row,setOf("id","updatedAt","archivedAt","rawBase64","sha256"));require(row.integer("id")>0 && row.integer("archivedAt")>0);raw(row)}
        val evidence=recovery.getJSONArray("evidence");val ids=mutableSetOf<String>()
        for(i in 0 until evidence.length()) {val row=evidence.getJSONObject(i);BackupJson.keys(row,setOf("id","rawBase64","sha256","updatedAt","archivedAt","origin","classification","completionToken"));require(ids.add(row.getString("id")) && row.getString("id").matches(Regex("[a-f0-9]{64}")) && row.integer("archivedAt")>0 && row.getString("origin").isNotBlank() && row.getString("classification").isNotBlank());raw(row);require(row.isNull("completionToken") || row.getString("completionToken").length in 1..256)}
        val stage=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).build()
        try {
            stage.withTransaction {
                legacy.insert(stage,legacyTables)
                for(table in identityTables)insertRows(stage,table,identities.getJSONArray(table))
                insertRows(stage,"practice_completion_evidence",recovery.getJSONArray("completions"))
                insertRecovery(stage,recovery)
            }
            val uuids=mutableSetOf<String>()
            stage.openHelper.readableDatabase.query("SELECT canonicalId,kind,provider,identityKey FROM personal_catalogue_identities").use {c->while(c.moveToNext()){
                require(UUID.fromString(c.getString(0)).toString()==c.getString(0) && uuids.add(c.getString(0)))
                require(c.getString(1) in setOf("work","arrangement","artist","album","release","recording","collection") && c.getString(2).matches(Regex("[a-z][a-z0-9-]{0,31}")) && c.getString(3).length in 1..1024)
            }}
            stage.openHelper.readableDatabase.query("SELECT a.role,i.kind FROM personal_catalogue_anchors a JOIN personal_catalogue_identities i ON i.identityKey=a.identityKey").use {c->while(c.moveToNext()){val role=c.getString(0);val kind=c.getString(1);require(if(role=="work")kind in setOf("work","arrangement") else role==kind)}}
            for((table,type,key) in listOf(Triple("pieces","piece","CAST(id AS TEXT)"),Triple("sessions","session","CAST(id AS TEXT)"),Triple("session_parts","part","CAST(id AS TEXT)"),Triple("session_piece_links","link","CAST(sessionId AS TEXT)||':'||pieceId"),Triple("favorite_composers","favorite","composerId"),Triple("saved_recordings","recording","CAST(id AS TEXT)"))) {
                val role=if(type=="favorite")"artist" else "work"
                stage.openHelper.readableDatabase.query("SELECT $key FROM $table").use{c->while(c.moveToNext()) require(stage.personalCatalogueDao().anchors(type,c.getString(0)).any{it.role==role})}
            }
            val state=identities.getJSONArray("personal_catalogue_state");require(state.length()==1 && state.getJSONObject(0).integer("id")==1L)
            require(UUID.fromString(state.getJSONObject(0).getString("scope")).toString()==state.getJSONObject(0).getString("scope"))
            stage.openHelper.readableDatabase.query("SELECT ownerType,ownerId,role FROM personal_catalogue_anchors").use {c->while(c.moveToNext()){
                require(c.getString(0) in setOf("piece","session","part","link","favorite","recording") && c.getString(1).isNotBlank() && c.getString(2) in setOf("work","artist","arrangement","album","release","recording","collection"))
            }}
            stage.openHelper.readableDatabase.query("SELECT token,durationMillis FROM practice_completion_evidence").use {c->while(c.moveToNext())require(c.getString(0).length in 1..256 && c.getLong(1)>=0)}
            stage.dao().allSessions().forEach {session->session.completionToken?.let {token->require(token.length in 1..256 && stage.backupStateDao().completed(token).any{it.durationMillis==session.durationMillis})}}
            stage.openHelper.readableDatabase.query("PRAGMA foreign_key_check").use {require(!it.moveToFirst())}
        } finally {stage.close()}
        return summary
    }
    suspend fun validate(raw:String):String=withContext(Dispatchers.IO){validate(BackupJson.parse(raw))}
    suspend fun validate(file:File):String=withContext(Dispatchers.IO){validate(BackupJson.parse(file))}
    suspend fun restore(raw:String) { val file=receive(raw.byteInputStream(Charsets.UTF_8));restore(file) }
    suspend fun restore(file:File)=restoreRoot(withContext(Dispatchers.IO){BackupJson.parse(file)})
    private suspend fun restoreRoot(root:JSONObject)=withContext(Dispatchers.IO){gate.withLock {
        validate(root);phase("validated")
        require(db.backupStateDao().state()?.status!="pending"){"Journal already restored; finish pending settings before another import"}
        // Immutable complete destination snapshot precedes replacement, including unknown bytes.
        val previous=exportSnapshot();phase("destination_preserved")
        val source=files.publish(File(directory,"import-${BackupJson.hash(root)}.json"),root)
        val sourceHash=BackupJson.hash(root)
        val isV5=root.integer("version")==5L
        val sections=if(isV5)sections(root) else emptyMap()
        db.withTransaction {
            val sql=db.openHelper.writableDatabase
            val oldDraft=BackupState.rawDraft(db)
            val oldArchives=rawRows("practice_recovery_archives")
            val oldLocal=db.practiceDao().localNow() ?: LocalAppState()
            val oldIdentityContext=JSONObject();identityTables.forEach{oldIdentityContext.put(it,rows(it))}
            val oldScope=sql.query("SELECT scope FROM personal_catalogue_state WHERE id=1").use{c->if(c.moveToFirst())c.getString(0) else "legacy"}
            oldDraft?.let{BackupState.preserve(db,it.first,it.second,"destination:$oldScope","replaced_destination")}
            if(oldDraft!=null || oldArchives.length()>0 || oldIdentityContext.getJSONArray("personal_catalogue_identities").length()>0) BackupState.preserve(db,oldIdentityContext.toString().toByteArray(Charsets.UTF_8),0,"destination-context:$oldScope","identity_context")
            PersonalCatalogueBridge.suspendCapture(sql)
            sql.execSQL("DELETE FROM personal_catalogue_anchors");if(isV5)sql.execSQL("DELETE FROM personal_catalogue_identities");sql.execSQL("DELETE FROM personal_catalogue_state")
            tables.asReversed().forEach {sql.execSQL("DELETE FROM `$it`")};sql.execSQL("DELETE FROM practice_drafts")
            if(isV5) {
                val journal=JSONObject(sections.getValue("journal").toString()).put("catalog_cache",JSONArray()).put("practice_recovery_archives",JSONArray())
                legacy.insert(db,journal)
                for(table in identityTables)insertRows(db,table,sections.getValue("identities").getJSONArray(table))
                val recovery=sections.getValue("recovery")
                for(i in 0 until recovery.getJSONArray("completions").length()) {
                    val row=recovery.getJSONArray("completions").getJSONObject(i);db.backupStateDao().completed(CompletionEvidence(row.getString("token"),row.integer("durationMillis")))
                }
                insertRecovery(db,recovery)
                for(i in 0 until recovery.getJSONArray("evidence").length()) {
                    val row=recovery.getJSONArray("evidence").getJSONObject(i)
                    var evidence=RecoveryEvidence(row.getString("id"),raw(row),row.getString("sha256"),row.integer("updatedAt"),row.integer("archivedAt"),row.getString("origin"),row.getString("classification"),if(row.isNull("completionToken"))null else row.getString("completionToken"))
                    // Metadata and chunked payload avoid CursorWindow failures for large evidence.
                    val existing=sql.query("SELECT rowid,payloadSha256,updatedAt,archivedAt,origin,classification,completionToken FROM recovery_evidence WHERE id=?",arrayOf(evidence.id)).use {c->
                        if(!c.moveToFirst()) null else RecoveryEvidence(evidence.id,BackupState.rawPayload(db,"recovery_evidence",c.getLong(0),"rawBytes","rowid"),c.getString(1),c.getLong(2),c.getLong(3),c.getString(4),c.getString(5),if(c.isNull(6))null else c.getString(6))
                    }
                    if(existing!=null && (!existing.rawBytes.contentEquals(evidence.rawBytes) || existing.updatedAt!=evidence.updatedAt || existing.archivedAt!=evidence.archivedAt || existing.origin!=evidence.origin || existing.classification!=evidence.classification || existing.completionToken!=evidence.completionToken)) {
                        evidence=evidence.copy(id=BackupState.sha((row.toString()+evidence.id).toByteArray()),origin="conflicting-id:${evidence.id}:${evidence.origin}")
                    }
                    db.backupStateDao().evidence(evidence)
                }
                val settings=sections.getValue("preferences");val local=settings.getJSONObject("local")
                db.practiceDao().local(oldLocal.copy(onboardingDone=local.getBoolean("onboardingDone"),reminderEnabled=local.getBoolean("reminderEnabled"),reminderHour=local.getInt("reminderHour"),reminderMinute=local.getInt("reminderMinute"),generation=Math.addExact(oldLocal.generation,1),lastReminderDay=""))
                val portable=JSONObject(settings.toString()).apply{remove("local")};val json=portable.toString()
                db.backupStateDao().state(BackupRestoreState(generation=UUID.randomUUID().toString(),preferencesJson=json,preferencesSha256=BackupJson.hash(portable),status="pending",sourceSha256=sourceHash,previousSnapshot=previous.name))
            } else {
                legacy.insert(db,legacy.parse(root.toString()))
                sql.execSQL("INSERT INTO personal_catalogue_state VALUES(1,?)",arrayOf(UUID.randomUUID().toString()))
            }
            // Destination archives remain alongside imported archives; retain original IDs in evidence.
            for(i in 0 until oldArchives.length()) {
                val row=oldArchives.getJSONObject(i);val raw=raw(row)
                BackupState.preserve(db,raw,row.integer("updatedAt"),"destination-archive:${row.integer("id")}:${row.integer("archivedAt")}","retained_archive")
                sql.execSQL("INSERT INTO practice_recovery_archives(payload,updatedAt,archivedAt) SELECT CAST(? AS TEXT),?,? WHERE NOT EXISTS(SELECT 1 FROM practice_recovery_archives WHERE CAST(payload AS BLOB)=? AND updatedAt=? AND archivedAt=?)",arrayOf(raw,row.integer("updatedAt"),row.integer("archivedAt"),raw,row.integer("updatedAt"),row.integer("archivedAt")))
            }
            BackupState.rawDraft(db)?.let {(raw,updated)->
                val decoded=runCatching{PracticeRepository.decode(raw.toString(Charsets.UTF_8))}.getOrNull()
                val completions=decoded?.let{db.backupStateDao().completed(it.token)}.orEmpty()
                val conflict=decoded!=null && completions.any{it.durationMillis!=decoded.elapsed}
                val classification=when {decoded==null->"unreadable";conflict->"token_conflict";completions.isNotEmpty()->"already_completed";else->"imported_paused"}
                BackupState.preserve(db,raw,updated,"import:${BackupJson.hash(root)}",classification,decoded?.token)
                if(classification=="already_completed")db.practiceDao().clearDraft()
            }
            PersonalCatalogueBridge.install(sql);BackupState.install(sql);PersonalCatalogueBridge.reserveRestoredOwnerIds(sql)
            phase("before_commit")
        }
        db.invalidationTracker.refreshAsync();phase("after_commit")
        replayUnlocked()
    }}
    suspend fun replayPendingPreferences()=withContext(Dispatchers.IO){gate.withLock {replayUnlocked()}}
    private suspend fun replayUnlocked() {
        val state=db.backupStateDao().state() ?: return
        if(state.status!="pending")return
        val settings=BackupJson.parse(state.preferencesJson);require(BackupJson.hash(settings)==state.preferencesSha256)
        phase("before_preferences");preferences.apply(settings);phase("after_preferences")
        db.withTransaction {check(db.backupStateDao().state()?.generation==state.generation);db.backupStateDao().state(state.copy(status="applied"));phase("before_ack")}
        phase("applied")
    }
}
