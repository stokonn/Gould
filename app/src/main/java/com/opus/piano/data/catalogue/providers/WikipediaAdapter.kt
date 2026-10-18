package com.opus.piano.data.catalogue.providers

class WikipediaAdapter : ProviderAdapter {
    override val id="wikipedia"
    override val intervalMillis=1200L
    override fun search(query:String,category:String,language:String,offset:Int)=emptyList<ProviderRequest>()
    fun document(title:String,language:String):ProviderRequest {require(language in setOf("en","cs"));return ProviderRequest("https://$language.wikipedia.org/w/rest.php/v1/page/${enc(title)}/with_html",id,"document",title)}
    override fun parse(request:ProviderRequest,root:Map<String,Any?>)=emptyList<ProviderRecord>()
    fun project(root:Map<String,Any?>):Map<String,Any?> {
        if(!licensedDocument(root))throw BlockedRightsException()
        return mapOf("title" to root.text("title"),"html" to root["html"],"revision" to root.obj("latest")["id"],"timestamp" to root.obj("latest").text("timestamp"),"license" to root.obj("license").text("url"))
    }
    fun licensedDocument(root:Map<String,Any?>):Boolean {
        val revision=root.obj("latest")["id"] as? Number ?: return false
        if(revision.toLong()<=0 || revision.toDouble()!=revision.toLong().toDouble() || root["html"] !is String)return false
        val url=root.obj("license").text("url")
        return url.matches(Regex("https://creativecommons\\.org/licenses/by-sa/4\\.0(?:/deed\\.(?:en|cs))?/?"))
    }
}
