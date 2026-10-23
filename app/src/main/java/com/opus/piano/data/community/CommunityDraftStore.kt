package com.opus.piano.data.community

import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.UUID

data class CommunityOwner(val key: String) {
    init { require(key.matches(Regex("(guest|principal):[0-9a-f-]{36}")));require(UUID.fromString(key.substringAfter(':')).toString()==key.substringAfter(':')) }
}
data class PendingContribution(val operation: ContributionOperation, val body: String, val key: String, val outcome: CommunityProblem?=null, val retryAtMillis: Long?=null)
data class CommunityDraft(val id: String, val owner: CommunityOwner, val version: Long, val payload: ContributionPayload, val pending: PendingContribution?=null, val receipt: String?=null)
class DraftConflict: IllegalStateException("Draft changed; reload before saving")
/** Separate app-private files: no practice Room access, deletion, migration or export. */
class CommunityDraftStore(private val root: File, private val beforePublish: ()->Unit={}) {
    private val lockName=root.canonicalPath.intern()
    private fun <T> locked(block: ()->T): T=synchronized(lockName) {
        check(root.isDirectory||root.mkdirs());RandomAccessFile(File(root,".lock"),"rw").channel.use { channel -> channel.lock().use { block() } }
    }
    private fun folder(owner: CommunityOwner)=File(root,MessageDigest.getInstance("SHA-256").digest(owner.key.toByteArray()).joinToString("") {"%02x".format(it)})
    private fun path(owner: CommunityOwner,id: String): File { require(UUID.fromString(id).toString()==id);return File(folder(owner),"$id.json") }
    private fun read(file: File,owner: CommunityOwner): CommunityDraft {
        val o=JSONObject(file.readText());require(o.getInt("storageVersion")==1 && o.getString("owner")==owner.key)
        require(file.name==o.getString("id")+".json" && o.getLong("version")>0)
        val p=o.optJSONObject("pending");return CommunityDraft(o.getString("id"),owner,o.getLong("version"),CommunityCodec.decodePayload(o.getJSONObject("payload"),validate=false),p?.let {PendingContribution(ContributionOperation.valueOf(it.getString("operation")),it.getString("body"),it.getString("key"),it.optString("outcome").takeIf {v->v.isNotEmpty()&&v!="null"}?.let(CommunityProblem::valueOf),if(it.has("retryAt"))it.getLong("retryAt") else null)},o.optString("receipt").takeIf {it.isNotEmpty()&&it!="null"})
    }
    fun get(owner: CommunityOwner,id: String): CommunityDraft?=locked {path(owner,id).takeIf {it.exists()}?.let {read(it,owner)}}
    fun list(owner: CommunityOwner): List<CommunityDraft> =locked {folder(owner).listFiles()?.filter {it.extension=="json"}?.map {read(it,owner)}?.sortedBy {it.id}?:emptyList()}
    fun save(d: CommunityDraft,expectedVersion: Long): CommunityDraft=locked {
        val f=path(d.owner,d.id);val old=if(f.exists())read(f,d.owner) else null
        if((old?.version?:0)!=expectedVersion)throw DraftConflict()
        val next=d.copy(version=Math.addExact(expectedVersion,1));val o=JSONObject().put("storageVersion",1).put("id",d.id).put("owner",d.owner.key).put("version",next.version).put("payload",CommunityCodec.payload(d.payload,validate=false))
        d.pending?.let {p->o.put("pending",JSONObject().put("operation",p.operation.name).put("body",p.body).put("key",p.key).apply {p.outcome?.let {put("outcome",it.name)};p.retryAtMillis?.let {put("retryAt",it)}})}
        d.receipt?.let {o.put("receipt",it)}
        require(o.toString().toByteArray().size<=65536)
        check(f.parentFile!!.isDirectory||f.parentFile!!.mkdirs());val temp=File.createTempFile(".draft-",".tmp",f.parentFile)
        try {FileOutputStream(temp).use {it.write(o.toString().toByteArray());it.fd.sync()};beforePublish();Files.move(temp.toPath(),f.toPath(),StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING)}finally {temp.delete()}
        next
    }
    fun guest(): CommunityOwner=locked {
        val f=File(root,"installation");if(!f.exists()) {
            val tmp=File.createTempFile(".install-",".tmp",root)
            try {FileOutputStream(tmp).use {it.write(UUID.randomUUID().toString().toByteArray());it.fd.sync()};Files.move(tmp.toPath(),f.toPath(),StandardCopyOption.ATOMIC_MOVE)}finally {tmp.delete()}
        };CommunityOwner("guest:"+f.readText())
    }
}
