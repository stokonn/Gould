package com.opus.piano.data.catalogue.providers

class CommonsAdapter : ProviderAdapter {
    override val id="commons"
    override val intervalMillis=1200L
    override fun search(query:String,category:String,language:String,offset:Int)=emptyList<ProviderRequest>()
    fun metadata(filename:String):ProviderRequest {require(filename.startsWith("File:"));return ProviderRequest("https://commons.wikimedia.org/w/api.php?action=query&format=json&formatversion=2&maxlag=5&prop=imageinfo&iiprop=url%7Csha1%7Csize%7Cmime%7Ctimestamp%7Cextmetadata&iiurlwidth=320&titles=${enc(filename)}",id,"metadata",filename)}
    override fun parse(request:ProviderRequest,root:Map<String,Any?>)=emptyList<ProviderRecord>()
    fun project(root:Map<String,Any?>):Map<String,Any?> {
        val info=root.obj("query").rows("pages").firstOrNull()?.rows("imageinfo")?.firstOrNull() ?: return emptyMap()
        val metadata=info.obj("extmetadata")
        if(!approved(metadata))return mapOf("blocked" to true)
        require(info.text("sha1").isNotBlank() && info.text("descriptionurl").startsWith("https://commons.wikimedia.org/wiki/File:"))
        return info.filterKeys {it in setOf("url","descriptionurl","thumburl","sha1","size","width","height","mime","timestamp")} +
            ("extmetadata" to attributionFields.mapNotNull {key->metadata.obj(key).text("value").takeIf {it.isNotBlank()}?.let {key to mapOf("value" to it)}}.toMap())
    }
    fun approved(metadata:Map<String,Any?>):Boolean {
        fun value(key:String)=metadata.obj(key).text("value")
        val license=value("LicenseShortName");val url=value("LicenseUrl").trimEnd('/')
        val expected=mapOf("CC BY 4.0" to "https://creativecommons.org/licenses/by/4.0","CC BY-SA 4.0" to "https://creativecommons.org/licenses/by-sa/4.0","CC BY 3.0" to "https://creativecommons.org/licenses/by/3.0","CC BY-SA 3.0" to "https://creativecommons.org/licenses/by-sa/3.0")
        return value("Artist").isNotBlank() && value("Restrictions").isBlank() && expected[license]==url
    }
    companion object {
        // Retain supplied credit/title/copyright and license notices, excluding unrelated prose
        // and unreviewed nested keys. The exact license/version allowlist remains unchanged.
        val attributionFields=listOf("ObjectName","Artist","Credit","Attribution","AttributionRequired","Copyright","Copyrighted","UsageTerms","LicenseShortName","LicenseUrl","Restrictions")
    }
}
