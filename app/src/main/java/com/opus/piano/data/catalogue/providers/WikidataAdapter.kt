package com.opus.piano.data.catalogue.providers

/** Untyped search hits are observations. Only explicit typed music IDs establish mappings. */
class WikidataAdapter : ProviderAdapter {
    override val id="wikidata"
    override val intervalMillis=1200L
    override fun search(query:String,category:String,language:String,offset:Int)=if(category=="lists")emptyList() else listOf(candidates(query,language))
    fun candidates(query:String,language:String)=ProviderRequest("https://www.wikidata.org/w/api.php?action=wbsearchentities&format=json&maxlag=5&limit=3&language=${if(language=="cs")"cs" else "en"}&search=${enc(query)}",id,"candidate")
    fun lookup(qid:String):ProviderRequest {require(qid.matches(Regex("Q[1-9][0-9]{0,15}")));return ProviderRequest("https://www.wikidata.org/w/api.php?action=wbgetentities&format=json&maxlag=5&ids=$qid&props=labels%7Caliases%7Cdescriptions%7Cclaims%7Csitelinks&languages=cs%7Cen",id,"entity",qid)}
    override fun parse(request:ProviderRequest,root:Map<String,Any?>):List<ProviderRecord> {
        if(request.kind=="candidate")return emptyList()
        val entity=root.obj("entities").obj(request.externalId.orEmpty())
        if(entity.containsKey("missing"))return emptyList()
        val claims=entity.obj("claims")
        val mapping=listOf(Triple("P435","work","work"),Triple("P434","artist","artist"),Triple("P436","release-group","album"),Triple("P4404","recording","recording"))
        val identities=mapping.flatMap {(property,providerKind,kind)->claims.rows(property).filter {it.text("rank")!="deprecated" && it.obj("mainsnak").text("snaktype")=="value"}.mapNotNull {claim->
            val value=claim.obj("mainsnak").obj("datavalue").text("value")
            if(value.matches(Regex("[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}")))Triple(providerKind,kind,value) else null
        }}.distinct()
        if(identities.map {it.second}.distinct().size!=1 || identities.size!=1)return emptyList()
        val identity=identities.single()
        val aliases=mutableListOf<Pair<String,String>>()
        val fields=mutableListOf<FieldValue>()
        for(language in listOf("cs","en")) {
            val label=entity.obj("labels").obj(language).text("value")
            if(label.isNotBlank()){aliases+=language to label;fields+=FieldValue("preferred_name",label,language)}
            aliases+=entity.obj("aliases").rows(language).mapNotNull {it.text("value").takeIf {v->v.isNotBlank()}?.let {v->language to v}}
            entity.obj("descriptions").obj(language).text("value").takeIf {it.isNotBlank()}?.let {fields+=FieldValue("description",it,language)}
            val site=entity.obj("sitelinks").obj(language+"wiki")
            val title=site.text("title")
            if(title.isNotBlank())fields+=FieldValue("wikipedia_title",title,language)
        }
        val title=aliases.firstOrNull {it.first=="en"}?.second ?: aliases.firstOrNull()?.second ?: return emptyList()
        fields+=FieldValue("verified_musicbrainz_identity","${identity.first}/${identity.third}")
        fields+=FieldValue("wikidata_revision",(entity["lastrevid"] as? Number)?.toLong()?.toString() ?: return emptyList())
        // Retain claim evidence/rank/qualifiers/references via known JSON keys only; no linked prose.
        for(property in listOf("P18","P31","P86","P50","P136","P528","P571")+mapping.map {it.first}) {
            claims.rows(property).forEach {claim->
                val safe=claim.filterKeys {it in setOf("id","rank","mainsnak","qualifiers","qualifiers-order","references","type")}
                fields+=FieldValue("claim:$property",JsonProjection.write(WikidataProjection.claim(safe)))
                if(property=="P18" && claim.text("rank")!="deprecated")claim.obj("mainsnak").obj("datavalue").text("value").takeIf {it.isNotBlank()}?.let {fields+=FieldValue("commons_file","File:$it")}
            }
        }
        return listOf(ProviderRecord("entity",request.externalId!!,identity.second,title,fields,aliases))
    }
    fun observations(root:Map<String,Any?>)=root.rows("search").filter {it.text("id").matches(Regex("Q[1-9][0-9]*"))}.map {ProviderCandidate(it.text("id"),it.text("label"),it.text("description"))}
}
internal object JsonProjection {
    fun write(value:Any?):String=when(value){null->"null";is String->"\""+value.flatMap {c->when(c){'"'->"\\\"";'\\'->"\\\\";'\n'->"\\n";'\r'->"\\r";'\t'->"\\t";else->if(c<' ')"\\u%04x".format(c.code) else c.toString()}.toList()}.joinToString("")+"\"";is Number,is Boolean->value.toString();is Map<*,*>->value.entries.sortedBy {it.key.toString()}.joinToString(",","{","}"){write(it.key.toString())+":"+write(it.value)};is List<*>->value.joinToString(",","[","]"){write(it)};else->error("Unsupported JSON")}
}

/** Nested claims are projected explicitly; unknown datatypes and keys never pass through. */
internal object WikidataProjection {
    private fun snak(raw:Map<String,Any?>):Map<String,Any?> {
        val allowed=raw.filterKeys {it in setOf("snaktype","property","datatype")}.toMutableMap()
        val data=raw.obj("datavalue");val type=data.text("type")
        val value=data["value"]
        if(type in setOf("string","wikibase-entityid","monolingualtext","time","quantity","globecoordinate")) {
            val projected=if(value is Map<*,*>)value.filterKeys {it in setOf("entity-type","numeric-id","id","language","text","time","timezone","before","after","precision","calendarmodel","amount","unit","upperBound","lowerBound","latitude","longitude","altitude","globe")} else value.takeIf {it is String || it is Number}
            if(projected!=null)allowed["datavalue"]=mapOf("type" to type,"value" to projected)
        }
        return allowed
    }
    private fun snaks(raw:Map<String,Any?>)=raw.filterKeys {it.matches(Regex("P[1-9][0-9]*"))}.mapValues {(key,_)->raw.rows(key).map {snak(it)}}
    fun claim(raw:Map<String,Any?>):Map<String,Any?> = mapOf("id" to raw.text("id"),"rank" to raw.text("rank"),"mainsnak" to snak(raw.obj("mainsnak")),"qualifiers" to snaks(raw.obj("qualifiers")),
        "references" to raw.rows("references").map {mapOf("hash" to it.text("hash"),"snaks" to snaks(it.obj("snaks")))})
}
