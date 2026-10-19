package com.opus.piano.data.catalogue.providers

import java.net.URI

/** Only reviewed core fields/relationships are projected; annotations/tags/ratings remain denied. */
class MusicBrainzAdapter : ProviderAdapter {
    override val id="musicbrainz"
    override val intervalMillis=1100L
    override fun search(query:String,category:String,language:String,offset:Int):List<ProviderRequest> {
        require(offset in 0..10000)
        val kinds=when(category){"works"->listOf("work");"artists"->listOf("artist");"albums"->listOf("release-group");"lists"->listOf("series");else->listOf("work","artist","release-group")}
        val literal="\""+query.replace("\\","\\\\").replace("\"","\\\"")+"\""
        return kinds.map {ProviderRequest("https://musicbrainz.org/ws/2/$it/?query=${enc(literal)}&fmt=json&limit=20&offset=$offset",id,it,offset=offset)}
    }
    override fun nextOffset(request:ProviderRequest,root:Map<String,Any?>):Int? {
        if(request.externalId!=null || !root.containsKey(request.kind+"-count"))return null
        val count=pageInteger(root[request.kind+"-count"])
        val offset=root[request.kind+"-offset"]?.let(::pageInteger) ?: request.offset
        require(offset==request.offset)
        val rows=root.rows(plurals[request.kind] ?: return null).size
        if(rows==0 || offset+rows>=count)return null
        return (offset+rows).takeIf {it<=10000}
    }
    fun lookup(kind:String,externalId:String):ProviderRequest {
        require(kind in plurals && validId(externalId))
        val include=when(kind){
            "work"->"aliases+artist-rels+work-rels+recording-rels"
            "recording"->"artist-credits+work-rels+url-rels+releases+isrcs"
            "release"->"artist-credits+recordings+release-groups+url-rels"
            "release-group"->"artist-credits+releases"
            "artist"->"aliases"
            else->"work-rels+recording-rels+release-group-rels"
        }
        return ProviderRequest("https://musicbrainz.org/ws/2/$kind/$externalId?fmt=json&inc=${enc(include)}",id,kind,externalId)
    }
    override fun parse(request:ProviderRequest,root:Map<String,Any?>):List<ProviderRecord> {
        val key=plurals[request.kind] ?: error("Unknown music entity type")
        val rows=if(request.externalId==null)root.rows(key) else listOf(root).also {require(root.text("id")==request.externalId)}
        val result=linkedMapOf<Pair<String,String>,ProviderRecord>()
        fun put(record:ProviderRecord) {val k=record.providerKind to record.externalId;val old=result[k];if(old==null || old.classificationPending && !record.classificationPending)result[k]=record}
        for(row in rows) {
            val base=project(row,request.kind,request.externalId==null) ?: continue
            val relations=row.rows("relations")
            val edges=mutableListOf<ProviderEdge>()
            val fields=base.fields.toMutableList()
            val credits=base.credits.toMutableList()
            fun ref(record:ProviderRecord)=ProviderReference(record.providerKind,record.externalId,record.kind,record.title,record.subtype)
            for(relation in relations) {
                val targetType=relation.text("target-type")
                val type=relation.text("type");val direction=relation.text("direction")
                if(direction !in setOf("forward","backward"))continue
                val target=project(relation.obj(targetType),targetType,true)
                if(target!=null) {
                    when {
                        type=="parts" && relation.text("type-id")==PARTS && base.kind=="work" && target.kind=="work" -> {
                            val movement=(relation["attributes"] as? List<*>)?.contains("movement")==true
                            val child=if(direction=="forward")target.copy(classificationPending=false,subtype=if(movement)"movement" else "part") else base.copy(classificationPending=false,subtype=if(movement)"movement" else "part")
                            val parent=if(direction=="forward")base else target
                            put(child);put(parent)
                            val order=(relation["ordering-key"] as? Number)?.toInt()
                            if(order!=null && order>=0)edges+=ProviderEdge("part",ref(child),ref(parent),order)
                            else fields+=FieldValue("diagnostic","part_order_missing:${target.externalId}")
                        }
                        type=="arrangement" && relation.text("type-id")==ARRANGEMENT && base.providerKind=="work" && target.providerKind=="work" -> {
                            val arrangement=if(direction=="backward")base.copy(kind="arrangement",classificationPending=false,subtype="") else target.copy(kind="arrangement",classificationPending=false,subtype="")
                            val source=if(direction=="backward")target else base
                            put(arrangement);put(source);edges+=ProviderEdge("arrangement_source",ref(arrangement),ref(source))
                        }
                        type in setOf("composer","arranger","lyricist") && target.kind=="artist" -> {put(target);credits+=ProviderCredit(target.externalId,target.title,type)}
                        type=="performance" && base.kind=="recording" && target.providerKind=="work" -> {put(target);edges+=ProviderEdge("performance",ref(base),ref(target))}
                        type=="performance" && base.providerKind=="work" && target.kind=="recording" -> {put(target);edges+=ProviderEdge("performance",ref(target),ref(base))}
                        base.kind=="collection" && target.kind!="collection" -> {
                            val order=(relation["ordering-key"] as? Number)?.toInt()
                            if(order!=null && order>=0){put(target);edges+=ProviderEdge("member",ref(base),ref(target),order)}
                        }
                    }
                }
                if(base.kind=="recording" && targetType=="url" && relation.text("type-id") in setOf(FREE_STREAM,STREAM)) {
                    val url=relation.obj("url").text("resource")
                    if(ListeningDestination.accepts(url,"recording"))fields+=FieldValue("listening_url",url)
                }
            }
            if(base.kind=="album")row.rows("releases").forEach {r->project(r,"release",false)?.let {edition->put(edition);edges+=ProviderEdge("edition",ref(edition),ref(base))}}
            if(base.kind=="release") {
                project(row.obj("release-group"),"release-group",false)?.let {album->put(album);edges+=ProviderEdge("edition",ref(base),ref(album))}
                row.rows("media").forEach {medium->
                    val number=(medium["position"] as? Number)?.toInt() ?: return@forEach
                    medium.rows("tracks").forEach {track->
                        val position=(track["position"] as? Number)?.toInt() ?: return@forEach
                        project(track.obj("recording"),"recording",false)?.let {recording->put(recording);edges+=ProviderEdge("track",ref(base),ref(recording),position,number)}
                    }
                }
            }
            val revised=result[base.providerKind to base.externalId] ?: base
            put(revised.copy(fields=fields,credits=credits.distinct(),relationships=edges))
            // Enrichment of an already-present root must include its field/edge projection.
            result[base.providerKind to base.externalId]=revised.copy(fields=fields,credits=credits.distinct(),relationships=edges)
        }
        return result.values.take(100).toList()
    }
    private fun project(row:Map<String,Any?>,type:String,search:Boolean):ProviderRecord? {
        val external=row.text("id");if(!validId(external))return null
        val title=row.text(if(type in setOf("artist","series"))"name" else "title");if(title.isBlank() || type !in plurals)return null
        val kind=when(type){"release-group"->"album";"series"->"collection";else->type}
        val names=when(type){"work"->listOf("type","language");"artist"->listOf("sort-name","type","country");"release-group"->listOf("primary-type","first-release-date");"release"->listOf("date","country","barcode","status");"recording"->emptyList();else->listOf("type")}
        val fields=names.mapNotNull {f->row.text(f).takeIf {it.isNotBlank()}?.let {FieldValue(f,it)}}.toMutableList()
        if(type=="recording")(row["length"] as? Number)?.toLong()?.takeIf {it>=0}?.let {fields+=FieldValue("durationMillis",it.toString())}
        row.rows("aliases").filter {it["primary"]==true && it.text("locale") in setOf("cs","en")}.forEach {a->a.text("name").takeIf {it.isNotBlank()}?.let {fields+=FieldValue("preferred_name",it,a.text("locale"))}}
        val credits=row.rows("artist-credit").mapNotNull {c->val a=c.obj("artist");if(validId(a.text("id")) && a.text("name").isNotBlank())ProviderCredit(a.text("id"),a.text("name"),"artist") else null}
        val subtype=when(type){"artist"->when(row.text("type")){"Person"->"person";"Group","Orchestra","Choir"->"group";else->"other"};"release-group"->if((row["secondary-types"] as? List<*>)?.contains("Soundtrack")==true)"soundtrack" else row.text("primary-type").lowercase(java.util.Locale.ROOT);else->""}
        return ProviderRecord(type,external,kind,title,fields,row.rows("aliases").mapNotNull {a->a.text("name").takeIf {it.isNotBlank()}?.let {(a.text("locale").ifBlank {"und"}) to it}},subtype,credits,classificationPending=type=="work" && search)
    }
    companion object {
        private val plurals=mapOf("work" to "works","artist" to "artists","release-group" to "release-groups","release" to "releases","recording" to "recordings","series" to "series")
        internal fun validId(id:String)=id.matches(Regex("[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}"))
        const val PARTS="ca8d3642-ce5f-49f8-91f2-125d72524e6a"
        const val ARRANGEMENT="51975ed8-bbfa-486b-9f28-5947f4370299"
        const val FREE_STREAM="7e41ef12-a124-4324-afdb-fdbae687a89c"
        const val STREAM="b5f3058a-666c-406f-aafb-f9249fc7b122"
    }
}
/** Entity-bound links only; no title-derived URLs, credentials, redirects or arbitrary hosts. */
object ListeningDestination {
    fun accepts(url:String,kind:String):Boolean = runCatching {
        val uri=URI(url);require(kind in setOf("recording","release","album"));require(uri.scheme=="https" && uri.userInfo==null && uri.port in setOf(-1,443) && uri.fragment==null && uri.rawQuery==null && url.length<=2048)
        when(uri.host){
            "open.spotify.com"->uri.path.matches(Regex(if(kind=="recording")"/track/[A-Za-z0-9]{22}" else "/album/[A-Za-z0-9]{22}"))
            "music.youtube.com"->false // watch requires a reviewed query projection; do not accept arbitrary parameters
            "music.apple.com"->kind in setOf("release","album") && uri.path.matches(Regex("/[a-z]{2}/album/[^/]+/[0-9]+"))
            else->false
        }
    }.getOrDefault(false)
}
