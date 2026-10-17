package com.opus.piano.data

import android.text.Html
import org.json.JSONObject

/** Never select a biography by surname alone. Ambiguous or unverified identities stay empty. */
internal class WikimediaProfile(private val http: CatalogHttp) {
    private suspend fun api(host: String, parameters: String) = http.json("https://$host/w/api.php?format=json&formatversion=2&${parameters.replace("|", "%7C")}")
    private fun values(entity: JSONObject, property: String): List<JSONObject> {
        val claims = entity.optJSONObject("claims")?.optJSONArray(property)?.objects().orEmpty().filter { it.text("rank") != "deprecated" }
        val ranked = claims.filter { it.text("rank") == "preferred" }.ifEmpty { claims }
        return ranked.mapNotNull { it.optJSONObject("mainsnak")?.optJSONObject("datavalue")?.optJSONObject("value") }
    }
    private fun date(entity: JSONObject, property: String): String {
        val date = values(entity, property).firstOrNull() ?: return ""
        val raw = date.text("time").removePrefix("+")
        return when {
            date.optInt("precision") >= 11 -> raw.take(10)
            date.optInt("precision") >= 9 -> raw.take(4)
            else -> ""
        }
    }
    suspend fun load(composer: CatalogComposer, portraitOnly: Boolean = false): ComposerProfile {
        val search = api("www.wikidata.org", "action=wbsearchentities&language=en&type=item&limit=5&search=${encoded(composer.name)}")
        val ids = search.optJSONArray("search")?.objects().orEmpty().map { it.getString("id") }
        if (ids.isEmpty()) return ComposerProfile()
        val entities = api("www.wikidata.org", "action=wbgetentities&ids=${ids.joinToString("|")}&props=labels|aliases|claims|sitelinks&languages=en")
            .getJSONObject("entities")
        val matches = ids.mapNotNull { entities.optJSONObject(it) }.filter { entity ->
            val names = listOfNotNull(entity.optJSONObject("labels")?.optJSONObject("en")?.text("value")) +
                entity.optJSONObject("aliases")?.optJSONArray("en")?.objects().orEmpty().map { it.text("value") }
            names.any { catalogText(it) == catalogText(composer.name) } &&
                composer.birth.take(4).isNotBlank() && date(entity, "P569").take(4) == composer.birth.take(4) &&
                values(entity, "P31").any { it.text("id") == "Q5" }
        }
        if (matches.size != 1) return ComposerProfile()
        val entity = matches.single()
        val wikidata = "https://www.wikidata.org/wiki/${entity.getString("id")}"
        var profile = ComposerProfile(wikidataUrl = wikidata, birth = date(entity, "P569"), death = date(entity, "P570"))
        val originIds = (values(entity, "P19") + values(entity, "P27")).map { it.text("id") }.filter { it.matches(Regex("Q\\d+")) }.distinct().take(3)
        if (!portraitOnly && originIds.isNotEmpty()) {
            try {
                val origins = api("www.wikidata.org", "action=wbgetentities&ids=${originIds.joinToString("|")}&props=labels&languages=en").getJSONObject("entities")
                profile = profile.copy(origin = originIds.mapNotNull { origins.optJSONObject(it)?.optJSONObject("labels")?.optJSONObject("en")?.text("value") }.distinct().joinToString(" · "))
            } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { /* Optional historical metadata. */ }
        }
        val title = entity.optJSONObject("sitelinks")?.optJSONObject("enwiki")?.text("title").orEmpty()
        if (!portraitOnly && title.isNotBlank()) {
            val response = api("en.wikipedia.org", "action=query&prop=extracts|info|pageprops&exintro=1&explaintext=1&inprop=url&meta=siteinfo&siprop=rightsinfo&titles=${encoded(title)}")
            val query = response.getJSONObject("query")
            val page = query.optJSONArray("pages")?.objects()?.singleOrNull()
            val rights = query.optJSONObject("rightsinfo")
            if (page != null && !page.has("missing") && page.optJSONObject("pageprops")?.has("disambiguation") != true && rights != null) {
                profile = profile.copy(text = page.text("extract"), articleUrl = page.text("fullurl"), license = rights.text("text"), licenseUrl = rights.text("url"))
            }
        }
        // A portrait may fail independently of the biography (e.g. an unlicensed/absent image).
        val filename = entity.optJSONObject("claims")?.optJSONArray("P18")?.objects()
            ?.firstOrNull { it.text("rank") != "deprecated" }?.optJSONObject("mainsnak")?.optJSONObject("datavalue")?.text("value").orEmpty()
        if (filename.isNotBlank()) {
            try { profile = profile.copy(portrait = portrait(filename, if(portraitOnly) 96 else 480)) }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { /* No image is preferable to an unverified image or a failed whole profile. */ }
        }
        return profile
    }
    private suspend fun portrait(filename: String, width: Int): Portrait? {
        val response = api("commons.wikimedia.org", "action=query&prop=imageinfo&iiprop=url|extmetadata&iiurlwidth=$width&titles=${encoded("File:$filename")}")
        val info = response.getJSONObject("query").optJSONArray("pages")?.objects()?.singleOrNull()
            ?.optJSONArray("imageinfo")?.optJSONObject(0) ?: return null
        val metadata = info.optJSONObject("extmetadata") ?: return null
        fun plain(key: String) = Html.fromHtml(metadata.optJSONObject(key)?.text("value").orEmpty(), Html.FROM_HTML_MODE_LEGACY).toString().trim()
        val license = plain("LicenseShortName")
        val url = plain("LicenseUrl").replace("http://", "https://").let { if (it.startsWith("//")) "https:$it" else it }
        val allowed = url.startsWith("https://creativecommons.org/licenses/by/") ||
            url.startsWith("https://creativecommons.org/licenses/by-sa/") || url.startsWith("https://creativecommons.org/publicdomain/") ||
            license.equals("Public domain", ignoreCase = true)
        val artist = plain("Artist")
        val source = info.text("descriptionurl")
        val image = info.text("thumburl")
        if (!allowed || artist.isBlank() || source.isBlank() || image.isBlank() || plain("Restrictions").isNotBlank()) return null
        return Portrait(image, artist, plain("Credit"), license, url, source)
    }
}

internal fun profileToJson(profile: ComposerProfile): JSONObject = JSONObject().apply {
    put("profileVersion", 5); put("origin", profile.origin); put("text", profile.text); put("articleUrl", profile.articleUrl); put("wikidataUrl", profile.wikidataUrl)
    put("birth", profile.birth); put("death", profile.death); put("license", profile.license); put("licenseUrl", profile.licenseUrl)
    profile.portrait?.let { p -> put("portrait", JSONObject().apply {
        put("url", p.url); put("artist", p.artist); put("credit", p.credit); put("license", p.license); put("licenseUrl", p.licenseUrl); put("sourceUrl", p.sourceUrl)
    }) }
}
internal fun profileFromJson(json: JSONObject): ComposerProfile = ComposerProfile(
    json.text("text"), json.text("articleUrl"), json.text("wikidataUrl"), json.text("birth"), json.text("death"),
    json.optJSONObject("portrait")?.let { Portrait(it.text("url"), it.text("artist"), it.text("credit"), it.text("license"), it.text("licenseUrl"), it.text("sourceUrl")) },
    json.text("license"), json.text("licenseUrl"), json.text("origin")
)
