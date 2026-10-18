package com.opus.piano.data.catalogue.providers

class WikipediaAdapter : ProviderAdapter {
    override val id="wikipedia"
    override val intervalMillis=1200L
    override fun search(query:String,category:String,language:String,offset:Int)=emptyList<ProviderRequest>()
    fun document(title:String,language:String):ProviderRequest {require(language in setOf("en","cs"));return ProviderRequest("https://$language.wikipedia.org/w/rest.php/v1/page/${enc(title)}/with_html",id,"document",title)}
    override fun parse(request:ProviderRequest,root:Map<String,Any?>)=emptyList<ProviderRecord>()
    fun project(root:Map<String,Any?>):Map<String,Any?> {
        require(licensedDocument(root)) {"Unverified article license or revision"}
        return mapOf("title" to root.text("title"),"html" to root["html"],"revision" to root.obj("latest")["id"],"timestamp" to root.obj("latest").text("timestamp"),"license" to root.obj("license").text("url"))
    }
    fun licensedDocument(root:Map<String,Any?>):Boolean = root.obj("latest")["id"] is Number &&
        root.obj("license").text("url").trimEnd('/') in setOf("https://creativecommons.org/licenses/by-sa/4.0","http://creativecommons.org/licenses/by-sa/4.0") && root["html"] is String
}
