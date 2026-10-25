package com.opus.piano.data.catalogue

import org.json.JSONArray
import org.json.JSONObject

/** A display group retains every original row, including revision, rights and selection. */
data class AssertionGroup(val field:String,val value:String,val language:String,val evidence:List<SourcedAssertion>)

object CataloguePresentation {
    fun groups(rows:List<SourcedAssertion>):List<AssertionGroup> = rows.groupBy {row->
        val json=runCatching {JSONObject(row.assertion.valueJson)}.getOrNull()
        // Exact scalar equivalence only. Case, punctuation and conflicting values stay separate.
        Triple(row.assertion.field,json?.optString("value") ?: row.assertion.valueJson,json?.optString("language","und") ?: "und")
    }.map {(key,evidence)->AssertionGroup(key.first,key.second,key.third,evidence)}

    fun essentials(detail:DiscoverDetail):List<AssertionGroup> = groups(detail.assertions.filter {
        it.assertion.selected && it.rights.reviewState=="approved" &&
            it.assertion.field in setOf("subtitle","description","catalogue","catalogue_number","key","genre","epoch","birth","death")
    }).filter {it.value.isNotBlank() && it.value!=detail.entity.title}

    fun languageLabel(code:String,locale:java.util.Locale,unspecified:String):String =
        if(code.isBlank() || code=="und")unspecified else java.util.Locale.forLanguageTag(code).getDisplayName(locale).ifBlank {code}

    fun retrievedLabel(value:String,locale:java.util.Locale):String = runCatching {
        java.time.format.DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)
            .withLocale(locale).withZone(java.time.ZoneId.systemDefault()).format(java.time.Instant.parse(value))
    }.getOrDefault(value)

    /** A numeric legacy identifier or opaque key is never presented as an invented name.
     *  Digits alone only qualify for artists (e.g. a legacy composer id), so numeric-looking
     *  real titles such as an album named "1984" are preserved. */
    fun unnamed(kind:String,title:String):Boolean {
        val value=title.trim()
        return value.isEmpty() || runCatching {CatalogueValidation.uuid(value)}.isSuccess ||
            (kind=="artist" && value.length in 1..12 && value.all {it.isDigit()})
    }
    /** Presentation selects an approved source language, never translates or rewrites evidence. */
    fun biography(detail:DiscoverDetail,language:String):SourcedAssertion? {
        require(language in setOf("cs","en"))
        val rows=detail.assertions.filter {it.assertion.field=="biography" && it.rights.reviewState=="approved" && it.source.provider=="wikipedia"}
        for(selected in listOf(language,"en").distinct()) {
            rows.filter {JSONObject(it.assertion.valueJson).optString("language")==selected && JSONObject(it.assertion.valueJson).optString("value").isNotBlank()}
                .maxWithOrNull(compareBy<SourcedAssertion> {runCatching {java.time.Instant.parse(it.source.retrievedAt).toEpochMilli()}.getOrDefault(Long.MIN_VALUE)}.thenBy {it.source.id}.thenBy {it.assertion.id})?.let {return it}
        }
        return null
    }
    fun title(detail:DiscoverDetail,language:String):String {
        val names=detail.assertions.filter {it.assertion.field=="preferred_name" && it.rights.reviewState=="approved"}
        fun preferred(lang:String)=names.filter {JSONObject(it.assertion.valueJson).optString("language")==lang}.maxByOrNull {it.source.retrievedAt}?.let {JSONObject(it.assertion.valueJson).getString("value")}
        return preferred(language) ?: preferred("en") ?: detail.entity.title
    }
    fun conflicts(detail:DiscoverDetail):List<String> = detail.assertions.filter {it.assertion.field=="identity_conflict"}.map {it.assertion.field}.distinct()+detail.assertions.filter {it.assertion.field in setOf("catalogue","catalogue_number","durationMillis","birth","death")}.groupBy {it.assertion.field to JSONObject(it.assertion.valueJson).optString("language")}
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
