package com.opus.piano.data.catalogue

import org.json.JSONArray
import org.json.JSONObject

object CataloguePresentation {
    fun title(detail:DiscoverDetail,language:String):String {
        val names=detail.assertions.filter {it.assertion.field=="preferred_name" && it.rights.reviewState=="approved"}
        fun preferred(lang:String)=names.filter {JSONObject(it.assertion.valueJson).optString("language")==lang}.maxByOrNull {it.source.retrievedAt}?.let {JSONObject(it.assertion.valueJson).getString("value")}
        return preferred(language) ?: preferred("en") ?: detail.entity.title
    }
    fun conflicts(detail:DiscoverDetail):List<String> = detail.assertions.filter {it.assertion.field in setOf("catalogue","catalogue_number","durationMillis","birth","death")}.groupBy {it.assertion.field to JSONObject(it.assertion.valueJson).optString("language")}
        .filterValues {rows->rows.map {SearchText.normalize(JSONObject(it.assertion.valueJson).optString("value"))}.distinct().size>1}.keys.map {it.first}.distinct()
    /** Catalogue-only export; this function cannot access personal history/preferences/recovery. */
    fun correction(detail:DiscoverDetail,field:String,proposed:String,reason:String):String {
        require(field.length in 1..80 && proposed.length in 1..4096 && reason.length in 1..4096)
        return JSONObject().put("format","goldberg-local-correction").put("version",1).put("status","local_unsubmitted").put("entityId",detail.entity.id).put("kind",detail.entity.kind)
            .put("field",field).put("proposedValue",proposed).put("reason",reason)
            .put("identities",JSONArray(detail.identities.map {JSONObject().put("provider",it.provider).put("providerKind",it.providerKind).put("externalId",it.externalId).put("evidence",it.evidence).put("revision",it.revision)}))
            .put("assertions",JSONArray(detail.assertions.map {row->JSONObject().put("field",row.assertion.field).put("original",JSONObject(row.assertion.valueJson)).put("provider",row.source.provider).put("revision",row.source.revision).put("sha256",row.source.payloadSha256)
                .put("sourceUrl",row.source.sourceUrl).put("retrievedAt",row.source.retrievedAt).put("license",row.rights.license).put("licenseUrl",row.rights.licenseUrl).put("attribution",row.rights.attribution)})).toString(2)
    }
}
