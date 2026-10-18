package com.opus.piano.data.catalogue.providers

class MusicBrainzAdapter : ProviderAdapter {
    override val id="musicbrainz"
    override val intervalMillis=1100L
    override fun search(query:String,category:String,language:String,offset:Int):List<ProviderRequest> {
        val kinds=when(category){"works"->listOf("work");"artists"->listOf("artist");"albums"->listOf("release-group");"lists"->listOf("series");else->listOf("work","artist","release-group")}
        // Quote user input as Lucene literal: punctuation cannot inject provider query syntax.
        val literal="\""+query.replace("\\","\\\\").replace("\"","\\\"")+"\""
        return kinds.map {ProviderRequest("https://musicbrainz.org/ws/2/$it/?query=${enc(literal)}&fmt=json&limit=20&offset=$offset",id,it)}
    }
    override fun parse(request:ProviderRequest,root:Map<String,Any?>):List<ProviderRecord> {
        val type=request.kind
        val key=mapOf("work" to "works","artist" to "artists","release-group" to "release-groups","release" to "releases","recording" to "recordings","series" to "series")[type] ?: error("Unknown type")
        val rows=if(request.externalId==null)root.rows(key) else listOf(root)
        return rows.mapNotNull { row ->
            val external=row.text("id");if(!external.matches(Regex("[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}")))return@mapNotNull null
            val title=row.text(if(type in setOf("artist","series"))"name" else "title");if(title.isBlank())return@mapNotNull null
            val kind=when(type){"release-group"->"album";"series"->"collection";else->type}
            val fields=when(type){"work"->listOf("type","language");"artist"->listOf("sort-name","type","country");"release-group"->listOf("primary-type","first-release-date");"release"->listOf("date","country","barcode","status");"recording"->emptyList();else->listOf("type")}
                .mapNotNull { f->row.text(f).takeIf {it.isNotBlank()}?.let {FieldValue(f,it)} }
            val credits=row.rows("artist-credit").mapNotNull { c->val a=c.obj("artist");val id=a.text("id");val name=a.text("name");if(id.matches(Regex("[0-9a-f-]{36}")) && name.isNotBlank())ProviderCredit(id,name,"artist") else null }
            ProviderRecord(type,external,kind,title,fields,row.rows("aliases").mapNotNull { a->a.text("name").takeIf {it.isNotBlank()}?.let {(a.text("locale").ifBlank {"und"}) to it} },
                when(type){"artist"->if(row.text("type")=="Person")"person" else "other";"release-group"->row.text("primary-type").lowercase();else->""},credits)
        }
    }
}
