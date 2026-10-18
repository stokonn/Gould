package com.opus.piano.data.catalogue.providers

class OpenOpusAdapter : ProviderAdapter {
    override val id="openopus"
    override val intervalMillis=1200L // upstream publishes no numeric SLA; conservative serialized reads
    override fun search(query:String,category:String,language:String,offset:Int)=
        if(category in setOf("all","works","artists")) listOf(ProviderRequest("https://api.openopus.org/omnisearch/${enc(query)}/$offset.json",id,"search")) else emptyList()
    override fun parse(request:ProviderRequest,root:Map<String,Any?>):List<ProviderRecord> {
        require(root.obj("status").text("success")=="true")
        fun artist(c:Map<String,Any?>):ProviderRecord? {
            val key=c.text("id");val name=c.text("complete_name").ifBlank {c.text("name")}
            if(!key.matches(Regex("[0-9]{1,16}")) || name.isBlank())return null
            return ProviderRecord("composer",key,"artist",name, listOf("name","birth","death","epoch").mapNotNull { f->c.text(f).takeIf {it.isNotBlank()}?.let {FieldValue(f,it)} },listOf("und" to c.text("name")))
        }
        val results=mutableListOf<ProviderRecord>()
        val rows=root.rows("results").ifEmpty {root.rows("works").map {mapOf("work" to it,"composer" to root.obj("composer"))} + root.rows("composers").map {mapOf("composer" to it)}}
        rows.forEach { row ->
            val c=row.obj("composer");artist(c)?.let {results+=it}
            val w=row.obj("work"); val key=w.text("id");val title=w.text("title")
            if(key.matches(Regex("[0-9]{1,16}")) && title.isNotBlank())results+=ProviderRecord("work",key,"work",title,
                listOf("subtitle","genre","catalogue","catalogue_number","key","searchterms").mapNotNull { f->w.text(f).takeIf {it.isNotBlank()}?.let {FieldValue(f,it)} },
                subtype="composition",credits=artist(c)?.let {listOf(ProviderCredit(it.externalId,it.title,"composer"))}.orEmpty())
        }
        return results.distinctBy {it.providerKind to it.externalId}.take(80)
    }
}
