package com.opus.piano.data.community

import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.util.UUID

enum class CommunityKind(val wire: String) { WORK("work"), ARRANGEMENT("arrangement"), ARTIST("artist"), ALBUM("album"), RELEASE("release"), RECORDING("recording"), COLLECTION("collection") }
@JvmInline value class CatalogueUuid(val value: String) { init { require(UUID.fromString(value).toString()==value && UUID.fromString(value).version() in 1..5 && UUID.fromString(value).variant()==2) } }
/** Decimal strings retain the entire positive PostgreSQL BIGINT range on reads. */
@JvmInline value class CommunityRevision(val value: String) { init { require(value.length<=19 && value.matches(Regex("[1-9][0-9]*")) && value.toBigInteger()<=Long.MAX_VALUE.toBigInteger()) } }
data class SourceEvidence(val type: String, val value: String, val license: String)
data class ContributionField(val field: String, val newValue: String, val evidence: List<SourceEvidence>)
enum class CommunityProvider(val wire: String) { WIKIDATA("wikidata"), MUSICBRAINZ("musicbrainz"), OPENOPUS("openopus") }
data class CommunityExternalIdentity(val provider: CommunityProvider, val providerKind: CommunityKind, val externalId: String)
data class ContributionPayload(val target: CatalogueUuid?, val kind: CommunityKind, val fields: List<ContributionField>, val intendedLicense: String, val changeKind: String="new_metadata", val externalIdentity: CommunityExternalIdentity?=null)
enum class ContributionOperation(val wire: String) { CREATE("submission.create"), REVISE("submission.revise"), WITHDRAW("submission.withdraw") }
data class ContributionCommand(val operation: ContributionOperation, val expectedRevision: Long=0, val submissionId: String?=null, val expectedSubmissionRevision: Long?=null, val reason: String?=null)
data class ProvenanceSummary(val licenses: List<String>, val sourceCount: Int)
data class PublicOverlayEntry(val kind: CommunityKind, val catalogueUuid: CatalogueUuid, val displayTitle: String, val revision: CommunityRevision, val provenanceSummary: ProvenanceSummary)
data class PublicChange(val sequence: CommunityRevision, val kind: CommunityKind, val catalogueUuid: CatalogueUuid, val revision: CommunityRevision, val event: String, val status: String, val createdAt: String, val provenanceSummary: ProvenanceSummary)
data class OverlayPage(val items: List<PublicOverlayEntry>, val nextCursor: String?)
data class ChangePage(val items: List<PublicChange>, val nextCursor: String?, val pollCursor: String?)

/** Wire encoding selects contract fields explicitly. No personal domain entity is accepted. */
object CommunityCodec {
    const val SCHEMA_VERSION=1
    private const val MAX_SAFE=9007199254740991L
    private fun plain(v: String, max: Int) { require(v.isNotBlank() && v.toByteArray().size<=max && v.none { it<' ' || it=='<' || it=='>' || it=='\u007f' } && !Regex("&#|&lt;|&gt;|javascript:|data:|vbscript:",RegexOption.IGNORE_CASE).containsMatchIn(v)) }
    fun payload(p: ContributionPayload, validate: Boolean=true): JSONObject {
        fun plain(v: String,max: Int) { if(validate) CommunityCodec.plain(v,max) }
        if(validate) require(p.changeKind in setOf("new_metadata","correction"));plain(p.intendedLicense,80)
        if(validate) require(p.fields.size in 1..20 && p.fields.map { it.field }.distinct().size==p.fields.size)
        if(validate) require(p.target!=null||p.externalIdentity!=null)
        val target=JSONObject().put("kind",p.kind.wire)
        p.target?.let {target.put("catalogueUuid",it.value)}
        p.externalIdentity?.let {e ->
            if(validate) {
                plain(e.externalId,128)
                require(e.providerKind==p.kind)
                when(e.provider) {
                    CommunityProvider.WIKIDATA -> require(e.externalId.matches(Regex("Q[1-9][0-9]*")))
                    CommunityProvider.MUSICBRAINZ -> {require(p.kind in setOf(CommunityKind.WORK,CommunityKind.ARTIST,CommunityKind.RECORDING,CommunityKind.RELEASE,CommunityKind.ALBUM));CatalogueUuid(e.externalId)}
                    CommunityProvider.OPENOPUS -> require(p.kind in setOf(CommunityKind.WORK,CommunityKind.ARTIST) && e.externalId.matches(Regex("[1-9][0-9]*")))
                }
            }
            target.put("externalIdentity",JSONObject().put("provider",e.provider.wire).put("providerKind",e.providerKind.wire).put("externalId",e.externalId))
        }
        var urls=0
        val fields=JSONArray()
        p.fields.forEach { f ->
            plain(f.field,80); if(validate) require(f.field in setOf("title","name","displayTitle","description","genre","instrument","musicalEra"))
            plain(f.newValue,2048);if(validate) require(f.evidence.size in 1..10)
            val evidence=JSONArray()
            f.evidence.forEach { e ->
                if(validate) require(e.type in setOf("reference","source_url"));plain(e.license,80);plain(e.value,if(e.type=="source_url")2048 else 500)
                if(e.type=="source_url" && validate) { urls++;val u=runCatching {URI(e.value)}.getOrElse {throw IllegalArgumentException("Invalid evidence URL")};if(validate) require(u.scheme=="https" && u.host!=null && u.rawUserInfo==null && u.port==-1 && u.fragment==null) }
                evidence.put(JSONObject().put("type",e.type).put("value",e.value).put("license",e.license))
            }
            fields.put(JSONObject().put("field",f.field).put("newValue",f.newValue).put("evidence",evidence))
        }
        if(validate) require(urls<=10)
        return JSONObject().put("schemaVersion",1).put("target",target)
            .put("changeKind",p.changeKind).put("fields",fields).put("intendedLicense",p.intendedLicense)
    }
    fun request(p: ContributionPayload, command: ContributionCommand, key: String): String {
        require(UUID.fromString(key).toString()==key);require(command.expectedRevision in 0..MAX_SAFE)
        val out=JSONObject().put("schemaVersion",1).put("expectedRevision",command.expectedRevision).put("idempotencyKey",key)
        when(command.operation) {
            ContributionOperation.CREATE -> {require(command.submissionId==null && command.expectedSubmissionRevision==null && command.reason==null);out.put("payload",payload(p))}
            ContributionOperation.REVISE,ContributionOperation.WITHDRAW -> {
                require(UUID.fromString(requireNotNull(command.submissionId)).toString()==command.submissionId)
                require(requireNotNull(command.expectedSubmissionRevision) in 1..MAX_SAFE)
                out.put("id",command.submissionId).put("expectedSubmissionRevision",command.expectedSubmissionRevision)
                if(command.operation==ContributionOperation.REVISE) { require(command.reason==null);out.put("payload",payload(p)) }
                else command.reason?.let {plain(it,1000);out.put("reason",it)}
            }
        }
        return out.toString().also { require(it.toByteArray().size<=16384) }
    }
    /** Strict input decoder for persisted local payloads; rejects unknown/private fields at every level. */
    fun decodePayload(o: JSONObject, validate: Boolean=true): ContributionPayload {
        keys(o,setOf("schemaVersion","target","changeKind","fields","intendedLicense"));require(o.get("schemaVersion") is Int && o.getInt("schemaVersion")==1)
        val t=o.getJSONObject("target");keys(t,setOf("kind","catalogueUuid","externalIdentity"),setOf("kind"))
        val a=o.getJSONArray("fields");val fields=(0 until a.length()).map { i ->
            val f=a.getJSONObject(i);keys(f,setOf("field","newValue","evidence"));val e=f.getJSONArray("evidence")
            ContributionField(f.getString("field"),f.getString("newValue"),(0 until e.length()).map { j -> val v=e.getJSONObject(j);keys(v,setOf("type","value","license"));SourceEvidence(v.getString("type"),v.getString("value"),v.getString("license")) })
        }
        val external=t.optJSONObject("externalIdentity")?.let {e -> keys(e,setOf("provider","providerKind","externalId"));CommunityExternalIdentity(CommunityProvider.entries.first {it.wire==e.getString("provider")},CommunityKind.entries.first {it.wire==e.getString("providerKind")},e.getString("externalId"))}
        return ContributionPayload(if(t.has("catalogueUuid"))CatalogueUuid(t.getString("catalogueUuid")) else null,CommunityKind.entries.first { it.wire==t.getString("kind") },fields,o.getString("intendedLicense"),o.getString("changeKind"),external).also {if(validate)payload(it)}
    }
    private fun keys(o: JSONObject, allowed: Set<String>, required: Set<String> =allowed) {val actual=o.keys().asSequence().toSet();require(actual.all {it in allowed} && actual.containsAll(required))}
    private fun provenance(o: JSONObject): ProvenanceSummary {val a=o.getJSONArray("licenses");return ProvenanceSummary((0 until a.length()).map {a.getString(it)},o.getInt("sourceCount").also {require(it>=0)})}
    fun overlay(raw: String): OverlayPage {
        require(raw.toByteArray().size<=262144)
        val o=JSONObject(raw);val a=o.getJSONArray("items");require(a.length()<=20)
        return OverlayPage((0 until a.length()).map {i -> val v=a.getJSONObject(i);PublicOverlayEntry(CommunityKind.entries.first {it.wire==v.getString("kind")},CatalogueUuid(v.getString("catalogueUuid")),v.getString("displayTitle"),CommunityRevision(v.getString("revision")),provenance(v.getJSONObject("provenanceSummary")))},o.optString("nextCursor").takeUnless {it.isEmpty()||it=="null"})
    }
    fun changes(raw: String): ChangePage {
        require(raw.toByteArray().size<=262144)
        val o=JSONObject(raw);val a=o.getJSONArray("items");require(a.length()<=20)
        val items=(0 until a.length()).map {i -> val v=a.getJSONObject(i);PublicChange(CommunityRevision(v.getString("sequence")),CommunityKind.entries.first {it.wire==v.getString("kind")},CatalogueUuid(v.getString("catalogueUuid")),CommunityRevision(v.getString("revision")),v.getString("event").also {require(it in setOf("publication","supersession","quarantine","rollback"))},v.getString("status").also {require(it in setOf("published","superseded","quarantined"))},v.getString("createdAt"),provenance(v.getJSONObject("provenanceSummary")))}
        require(items.zipWithNext().all {(a,b)->a.sequence.value.toLong()<b.sequence.value.toLong()})
        return ChangePage(items,o.optString("nextCursor").takeUnless {it.isEmpty()||it=="null"},o.optString("pollCursor").takeUnless {it.isEmpty()||it=="null"})
    }
}
