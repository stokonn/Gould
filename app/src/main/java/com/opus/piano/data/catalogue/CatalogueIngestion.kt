package com.opus.piano.data.catalogue

import androidx.room.withTransaction
import com.opus.piano.data.catalogue.providers.*
import org.json.JSONObject
import java.util.UUID

/** Append-only accepted assertions. A provider identity does not prove a cross-provider match. */
internal class CatalogueIngestion(private val db:CatalogueDatabase) {
    private val dao=db.dao()
    private fun sourceUrl(provider:String,kind:String,id:String):String=when(provider){
        "musicbrainz"->"https://musicbrainz.org/$kind/$id"
        "wikidata"->"https://www.wikidata.org/wiki/$id"
        "openopus"->"https://api.openopus.org/"+(if(kind=="composer")"composer/list/ids/$id.json" else "work/detail/$id.json")
        else->error("Unsupported accepted source")
    }
    private fun uuid()=UUID.randomUUID().toString()
    suspend fun accept(result:ProviderResult):List<String> = db.withTransaction {
        if(result.state!=SourceState.FRESH)return@withTransaction emptyList()
        require(result.license=="CC0-1.0")
        val repo=CatalogueRepository(db)
        val sourceKey=SearchText.hash("${result.request.provider}:${result.request.url}:${result.hash}")
        val previous=dao.state("source:$sourceKey")
        if(previous!=null)return@withTransaction result.records.mapNotNull {dao.identity(result.request.provider,it.providerKind,it.externalId)?.entityId}
        val recordSources=mutableMapOf<Pair<String,String>,CatalogueSource>()
        val rights=CatalogueRights(uuid(),result.license,result.licenseUrl,result.request.provider+" metadata","approved")
        repo.addRights(rights)
        val ids=mutableListOf<String>()
        for(record in result.records.take(100)) {
            val musicIdentity=record.fields.singleOrNull {it.field=="verified_musicbrainz_identity"}?.value?.split('/')
            if(record.classificationPending || result.request.provider=="wikidata" && record.kind=="work" && musicIdentity?.size==2 && dao.identity("musicbrainz",musicIdentity[0],musicIdentity[1])==null) {
                repo.observeCandidate(result,record);continue
            }
            val source=CatalogueSource(uuid(),result.request.provider,record.providerKind+":"+record.externalId,
                result.revision+"|sha256="+result.hash,result.hash,result.observedAt,sourceUrl(result.request.provider,record.providerKind,record.externalId))
            // Same provider revision with different bytes remains a separate auditable observation.
            val prior=dao.sourceObservation(source.provider,source.recordId,source.revision)
            if(prior==null)repo.addSource(source)
            val observed=prior ?: source
            recordSources[record.providerKind to record.externalId]=observed
            require(record.kind in CatalogueValidation.kinds && record.externalId.isNotBlank() && record.title.isNotBlank())
            val identity=dao.identity(result.request.provider,record.providerKind,record.externalId)
            val cross=record.fields.singleOrNull {it.field=="verified_musicbrainz_identity"}?.value?.split('/')
            val verifiedCross=if(result.request.provider=="wikidata" && cross?.size==2)dao.identity("musicbrainz",cross[0],cross[1]) else null
            if(verifiedCross!=null && dao.entity(verifiedCross.entityId)?.kind!=record.kind) {
                repo.observeCandidate(result,record,"typed_identity_conflict");recordSources.remove(record.providerKind to record.externalId);continue
            }
            val id=identity?.entityId ?: verifiedCross?.entityId?.takeIf {dao.entity(it)?.kind==record.kind}
                ?: repo.candidateId(result.request.provider,record.providerKind,record.externalId) ?: uuid()
            if(dao.entity(id)==null) {
                val root=CatalogueEntity(id,record.kind,record.title)
                val subtype:Any=when(record.kind){
                    "artist"->CatalogueArtist(id,artistType=record.subtype.takeIf {it in setOf("person","group","other")} ?: "other",sortName=record.title)
                    "album"->CatalogueAlbum(id,albumType=record.subtype.takeIf {it in setOf("album","ep","single","soundtrack")} ?: "unknown")
                    "release"->CatalogueRelease(id)
                    "recording"->CatalogueRecording(id)
                    "arrangement"->CatalogueArrangement(id)
                    "collection"->CatalogueCollection(id)
                    else->CatalogueWork(id,workType=record.subtype.takeIf {it in setOf("composition","movement","part","medley","mashup")} ?: "composition")
                }
                repo.add(root,subtype)
            } else if(dao.entity(id)?.kind!=record.kind) {
                repo.observeCandidate(result,record,"typed_identity_conflict");recordSources.remove(record.providerKind to record.externalId);continue
            }
            if(identity!=null && identity.reviewState!="reviewed")repo.map(identity.copy(reviewState="reviewed",revision=identity.revision+1,evidence="provider typed primary identifier confirmed",sourceId=observed.id))
            if(identity==null)repo.map(CatalogueExternalIdentity(result.request.provider,record.providerKind,record.externalId,id,if(verifiedCross!=null)"explicit Wikidata typed MusicBrainz claim: ${cross!!.joinToString("/")}" else "provider typed primary identifier; no cross-provider equivalence","reviewed",sourceId=observed.id))
            if(result.request.provider=="wikidata" && cross?.size==2 && verifiedCross==null && record.kind!="work")repo.map(CatalogueExternalIdentity("musicbrainz",cross[0],cross[1],id,"explicit Wikidata typed MusicBrainz claim: ${cross.joinToString("/")}","reviewed",sourceId=observed.id))
            repo.removeState(candidateKey(result.request.provider,record.providerKind,record.externalId))
            if(record.kind=="work" && record.subtype in setOf("movement","part","medley","mashup"))
                db.openHelper.writableDatabase.execSQL("UPDATE works SET workType=? WHERE id=? AND workType='composition'",arrayOf(record.subtype,id))
            val fields=(listOf(FieldValue("title",record.title))+record.fields+record.aliases.map {FieldValue("alias",it.second,it.first)}).distinct()
            for(field in fields) {
                val payload=JSONObject().put("value",field.value).put("language",field.language).put("confidence","provider_assertion").put("reviewState","source_verified").put("providerRevision",result.revision).toString()
                // First accepted value remains selected; contradictions are retained and visible.
                val selected=dao.assertions(id,field.field).isEmpty()
                if(dao.assertions(id,field.field).none {it.sourceId==observed.id})repo.addAssertion(CatalogueAssertion(uuid(),id,field.field,payload,observed.id,rights.id,selected))
            }
            val aliases=record.aliases+listOf("und" to (record.title+" "+record.credits.joinToString(" "){it.name}+" "+record.fields.filter {it.field in setOf("catalogue","catalogue_number","searchterms","subtitle")}.joinToString(" "){it.value}))
            for((language,value) in aliases.distinct())if(value.isNotBlank() && !dao.hasAlias(id,language,value))repo.addAlias(CatalogueAlias(entityId=id,language=language,value=value,normalized=CatalogueValidation.normalized(value)))
            ids+=id
        }
        // Credits with provider IDs are explicit evidence, independently typed. Never title-match.
        for(record in result.records) {
            val observed=recordSources[record.providerKind to record.externalId] ?: continue
            val from=dao.identity(result.request.provider,record.providerKind,record.externalId)?.entityId ?: continue
            for((position,credit) in record.credits.withIndex()) {
                val providerKind=if(result.request.provider=="openopus")"composer" else "artist"
                var target=dao.identity(result.request.provider,providerKind,credit.id)?.entityId
                if(target==null) {
                    target=uuid();repo.add(CatalogueEntity(target,"artist",credit.name),CatalogueArtist(target,artistType="other"))
                    repo.map(CatalogueExternalIdentity(result.request.provider,providerKind,credit.id,target,"explicit provider artist credit","reviewed",sourceId=observed.id))
                }
                if(!dao.hasEdge(from,target,"credit"))repo.relate(CatalogueRelationship(uuid(),"credit",from,record.kind,target,"artist",position,role=credit.role,creditedAs=credit.name,sourceId=observed.id))
            }
        }
        CatalogueEdges(db).ingest(result)
        dao.state(CatalogueState("source:$sourceKey","accepted"))
        dao.state(CatalogueState("generation",uuid()))
        ids
    }
}
