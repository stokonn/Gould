package com.opus.piano.data.catalogue

import com.opus.piano.data.catalogue.providers.*
import org.json.JSONObject
import java.util.UUID

data class CatalogueCandidate(val id:String,val provider:String,val providerKind:String,val externalId:String,val kindHint:String,val title:String,val reason:String)
internal fun candidateKey(provider:String,kind:String,id:String)="candidate:$provider:$kind:$id"
internal fun candidateFromJson(raw:String):CatalogueCandidate {
    val o=JSONObject(raw);return CatalogueCandidate(o.getString("id"),o.getString("provider"),o.getString("providerKind"),o.getString("externalId"),o.getString("kindHint"),o.getString("title"),o.getString("reason"))
}
internal suspend fun CatalogueRepository.observeCandidate(result:ProviderResult,record:ProviderRecord,reason:String="classification_pending"):String {
    val key=candidateKey(result.request.provider,record.providerKind,record.externalId)
    val existing=state(key)?.let {runCatching {candidateFromJson(it)}.getOrNull()}
    val id=existing?.id ?: UUID.randomUUID().toString()
    val value=JSONObject().put("id",id).put("provider",result.request.provider).put("providerKind",record.providerKind).put("externalId",record.externalId)
        .put("kindHint",record.kind).put("title",record.title).put("reason",reason).put("observedAt",result.observedAt).put("payloadSha256",result.hash).put("revision",result.revision).toString()
    boundedState("candidate:",key,value);return id
}
internal suspend fun CatalogueRepository.candidateId(provider:String,kind:String,id:String)=state(candidateKey(provider,kind,id))?.let {runCatching {candidateFromJson(it).id}.getOrNull()}
internal suspend fun CatalogueRepository.candidates(query:String,filter:SearchFilter,excluded:Set<String> = emptySet()):List<CatalogueCandidate> {
    val tokens=SearchText.normalize(query).split(' ').filter {it.isNotBlank()}
    return states("candidate:",200).mapNotNull {runCatching {candidateFromJson(it.value)}.getOrNull()}.filter {candidate->
        candidate.id !in excluded && (candidate.kindHint in filter.kinds || filter.category=="all" && candidate.kindHint=="unknown") && entity(candidate.id)==null && tokens.all {SearchText.normalize(candidate.title).contains(it)}
    }.take(40)
}
