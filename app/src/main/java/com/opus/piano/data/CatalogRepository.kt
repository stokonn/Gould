package com.opus.piano.data

import android.graphics.BitmapFactory
import android.graphics.Bitmap
import androidx.room.withTransaction
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest

internal fun JSONObject.text(key: String): String = if (isNull(key)) "" else optString(key, "")
internal fun JSONArray.objects(): List<JSONObject> = (0 until length()).mapNotNull { optJSONObject(it) }
internal fun encoded(value: String): String = URLEncoder.encode(value, "UTF-8").replace("+", "%20")

/** Small HTTPS-only client. No credentials, tracking, or repertoire data leave the device. */
class CatalogHttp {
    private val gate = Mutex()
    suspend fun bytes(url: String, image: Boolean = false): ByteArray = withContext(Dispatchers.IO) {
        gate.withLock {
            val parsed = URL(url)
            val hosts = setOf("api.openopus.org", "www.wikidata.org", "en.wikipedia.org", "commons.wikimedia.org", "upload.wikimedia.org", "thumb.wikimedia.org")
            require(parsed.protocol == "https" && parsed.host in hosts) { "Unsupported source" }
            val connection = parsed.openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 12000
                connection.readTimeout = 15000
                connection.instanceFollowRedirects = false
                connection.setRequestProperty("User-Agent", "Goldberg/0.6.0 (Android; com.opus.piano; piano practice catalogue)")
                connection.setRequestProperty("Accept", if (image) "image/*" else "application/json")
                val code = connection.responseCode
                if (code !in 200..299) throw IOException(if (code == 429) "Source is busy; please try again later." else "Source returned HTTP $code.")
                val limit = if (image) 4 * 1024 * 1024 else 8 * 1024 * 1024
                val result = java.io.ByteArrayOutputStream()
                connection.inputStream.use { input ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        if (result.size() + read > limit) throw IOException("Response is too large.")
                        result.write(buffer, 0, read)
                    }
                }
                result.toByteArray()
            } finally { connection.disconnect() }
        }
    }
    suspend fun json(url: String): JSONObject {
        val value = JSONObject(bytes(url).toString(Charsets.UTF_8))
        if (value.has("error")) throw IOException("The source could not complete the request.")
        return value
    }
}

class CatalogRepository(private val db: OpusDatabase, private val filesDir: File, private val http: CatalogHttp = CatalogHttp(), private val achievements: AchievementStore = AchievementStore(db)) {
    private val dao = db.catalogDao()
    private val importLock = Mutex()
    val snapshot = combine(dao.composers(), dao.works()) { composers, works -> CatalogSnapshot(composers, works) }

    private fun composer(json: JSONObject) = CatalogComposer(
        json.getString("id"), json.text("complete_name").ifBlank { json.getString("name") },
        json.text("birth"), json.text("death"), json.text("epoch")
    )
    private fun work(json: JSONObject, composerId: String) = CatalogWork(
        json.getString("id"), composerId, json.getString("title"), json.text("subtitle"), json.text("genre"),
        json.text("searchterms"), json.text("catalogue"), json.text("catalogue_number"), json.text("key"), json.text("popular") == "1", json.text("recommended") == "1"
    )

    private suspend fun ingest(json: JSONObject) {
        val composers = mutableListOf<CatalogComposer>()
        val works = mutableListOf<CatalogWork>()
        val popularProvided = mutableSetOf<String>()
        val recommendedProvided = mutableSetOf<String>()
        fun readWork(json: JSONObject, composerId: String) {
            val value = work(json, composerId)
            works += value
            if (json.has("popular")) popularProvided += value.id
            if (json.has("recommended")) recommendedProvided += value.id
        }
        json.optJSONArray("composers")?.objects()?.forEach { composers += composer(it) }
        json.optJSONObject("composer")?.let { c ->
            composers += composer(c)
            json.optJSONArray("works")?.objects()?.forEach { readWork(it, c.getString("id")) }
            json.optJSONObject("work")?.let { readWork(it, c.getString("id")) }
        }
        json.optJSONArray("results")?.objects()?.forEach { result ->
            val c = result.getJSONObject("composer")
            composers += composer(c)
            result.optJSONObject("work")?.let { readWork(it, c.getString("id")) }
        }
        db.withTransaction {
            var metadataChanged = false
            // Detail responses omit life dates; do not overwrite richer cached metadata.
            composers.distinctBy { it.id }.forEach { c ->
                val old = dao.composer(c.id)
                val merged = c.copy(birth = c.birth.ifBlank { old?.birth.orEmpty() }, death = c.death.ifBlank { old?.death.orEmpty() }, epoch = c.epoch.ifBlank { old?.epoch.orEmpty() })
                if (merged != old) { dao.putComposers(listOf(merged)); metadataChanged = true }
            }
            works.distinctBy { it.id }.forEach { w ->
                val old = dao.work(w.id)
                val merged = w.copy(catalogue = w.catalogue.ifBlank { old?.catalogue.orEmpty() }, catalogueNumber = w.catalogueNumber.ifBlank { old?.catalogueNumber.orEmpty() }, musicalKey = w.musicalKey.ifBlank { old?.musicalKey.orEmpty() },
                    popular = if (w.id in popularProvided) w.popular else old?.popular ?: false,
                    recommended = if (w.id in recommendedProvided) w.recommended else old?.recommended ?: false)
                if (merged != old) { dao.putWorks(listOf(merged)); metadataChanged = true }
            }
            if (metadataChanged) achievements.evaluateInTransaction()
        }
    }

    private suspend fun openOpus(path: String, force: Boolean = false): CatalogLoad<JSONObject> = withContext(Dispatchers.IO) {
        val cached = dao.cache(path)
        if (!force && cached != null && System.currentTimeMillis() - cached.fetchedAt < 7 * 86400000L) {
            val json = JSONObject(cached.payload)
            ingest(json)
            return@withContext CatalogLoad(json, true, fetchedAt = cached.fetchedAt)
        }
        try {
            val json = http.json("https://api.openopus.org/$path")
            val status = json.getJSONObject("status")
            if (status.text("success") != "true" && status.text("error") != "Nothing found") throw IOException("Open Opus is temporarily unavailable.")
            val now = System.currentTimeMillis()
            // Parse and persist atomically. A failed refresh leaves the previous cache untouched.
            db.withTransaction { ingest(json); dao.putCache(CatalogCache(path, json.toString(), now)) }
            CatalogLoad(json, false, fetchedAt = now)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) {
            if (cached == null) throw IOException("Unable to load Open Opus. Check your connection and try again.", error)
            CatalogLoad(JSONObject(cached.payload), true, "Refresh failed. Showing the saved catalogue.", cached.fetchedAt)
        }
    }

    suspend fun featured(force: Boolean = false) = openOpus("composer/list/pop.json", force)
    suspend fun search(query: String, offset: Int = 0, force: Boolean = false): CatalogLoad<Int?> {
        val result = openOpus("omnisearch/${encoded(catalogText(query))}/$offset.json", force)
        val next = result.value.optInt("next", -1).takeIf { it > offset }
        return CatalogLoad(next, result.cached, result.warning, result.fetchedAt)
    }
    suspend fun composerWorks(id: String, force: Boolean = false): CatalogLoad<Int> {
        require(id.matches(Regex("\\d+")))
        val load = openOpus("work/list/composer/$id/genre/all.json", force)
        return CatalogLoad(load.value.optJSONArray("works")?.length() ?: 0, load.cached, load.warning, load.fetchedAt)
    }
    suspend fun detail(id: String, force: Boolean = false): CatalogLoad<Unit> {
        require(id.matches(Regex("\\d+")))
        val load = openOpus("work/detail/$id.json", force)
        return CatalogLoad(Unit, load.cached, load.warning, load.fetchedAt)
    }

    /** ID is authoritative. Only exact normalized title AND composer count as a manual duplicate. */
    suspend fun add(work: CatalogWork, composer: CatalogComposer): Boolean = importLock.withLock {
        db.withTransaction {
            val pieces = db.dao().allPieces()
            val exists = pieces.any {
                it.catalogWorkId == work.id || (it.catalogWorkId == null && catalogText(it.title) == catalogText(work.title) && catalogText(it.composer) == catalogText(composer.name))
            }
            if (exists) false else {
                // Keep a metadata snapshot even when adding from offline search suggestions.
                if (dao.composer(composer.id) == null) dao.putComposers(listOf(composer))
                if (dao.work(work.id) == null) dao.putWorks(listOf(work))
                val added = db.dao().insertCatalogPiece(Piece(title = work.title, composer = composer.name, difficulty = Difficulty.UNRATED, notes = work.subtitle, catalogWorkId = work.id, createdAt = System.currentTimeMillis())) != -1L
                if (added) achievements.evaluateInTransaction()
                added
            }
        }
    }

    suspend fun profile(c: CatalogComposer, force: Boolean = false): CatalogLoad<ComposerProfile> = withContext(Dispatchers.IO) {
        val key = "profile:${c.id}"
        val cached = dao.cache(key)
        if (!force && cached != null && JSONObject(cached.payload).optInt("profileVersion") >= 5 && System.currentTimeMillis() - cached.fetchedAt < 30 * 86400000L)
            return@withContext CatalogLoad(profileFromJson(JSONObject(cached.payload)), true, fetchedAt = cached.fetchedAt)
        try {
            val profile = WikimediaProfile(http).load(c)
            val now = System.currentTimeMillis()
            dao.putCache(CatalogCache(key, profileToJson(profile).toString(), now))
            CatalogLoad(profile, false, fetchedAt = now)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) {
            if (cached == null) throw IOException("Biography and portrait could not be loaded. Catalogue works remain available.", error)
            CatalogLoad(profileFromJson(JSONObject(cached.payload)), true, "Showing the saved biography; refresh failed.", cached.fetchedAt)
        }
    }

    /** App-private persistent portrait cache, never part of repertoire or uploaded. */
    suspend fun portrait(url: String): Bitmap? = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256").digest(url.toByteArray()).joinToString("") { "%02x".format(it) }
        val directory = File(filesDir, "catalog-portraits").apply { mkdirs() }
        val file = File(directory, "$digest.img")
        if (file.exists()) decodePortrait(file.readBytes())?.let { return@withContext it }
        val bytes = http.bytes(url, image = true)
        val bitmap = decodePortrait(bytes) ?: return@withContext null
        val temporary = File.createTempFile("portrait-", ".tmp", directory)
        try { temporary.writeBytes(bytes); if (!temporary.renameTo(file)) throw IOException("Could not cache portrait") }
        finally { temporary.delete() }
        bitmap
    }
}

/** Decode source images to at most about 512 pixels per side before allocating pixel storage. */
private fun decodePortrait(bytes: ByteArray): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    val options = BitmapFactory.Options().apply { inSampleSize = 1; while (maxOf(bounds.outWidth, bounds.outHeight) / inSampleSize > 512) inSampleSize *= 2 }
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
}
