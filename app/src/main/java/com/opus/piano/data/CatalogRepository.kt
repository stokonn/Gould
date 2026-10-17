package com.opus.piano.data

import com.opus.piano.i18n.L
import com.opus.piano.R
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
open class CatalogHttp(private val imageAllowed: () -> Boolean = { false }) {
    private val gate = Mutex()
    open suspend fun bytes(url: String, image: Boolean = false): ByteArray = withContext(Dispatchers.IO) {
        gate.withLock {
            if(image) check(imageAllowed()) { L.s(R.string.media_consent_needed) }
            val parsed = URL(url)
            val hosts = setOf("api.openopus.org", "www.wikidata.org", "en.wikipedia.org", "commons.wikimedia.org", "upload.wikimedia.org", "thumb.wikimedia.org")
            require(parsed.protocol == "https" && parsed.host in hosts) { L.s(R.string.msg_unsupported_source_edd7) }
            val connection = parsed.openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 12000
                connection.readTimeout = 15000
                connection.instanceFollowRedirects = false
                connection.setRequestProperty("User-Agent", "Goldberg/0.9.2 (Android; com.opus.piano; piano practice catalogue)")
                connection.setRequestProperty("Accept", if (image) "image/*" else "application/json")
                val code = connection.responseCode
                if (code !in 200..299) throw IOException(if (code == 429) L.s(R.string.msg_source_is_busy_please_try_again_later_b502) else L.s(R.string.msg_source_returned_http_code_e6c0, (code)))
                val limit = if (image) 4 * 1024 * 1024 else 8 * 1024 * 1024
                val result = java.io.ByteArrayOutputStream()
                connection.inputStream.use { input ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        if(image) check(imageAllowed()) {L.s(R.string.media_consent_needed)}
                        val read = input.read(buffer)
                        if (read < 0) break
                        if (result.size() + read > limit) throw IOException(L.s(R.string.msg_response_is_too_large_162e))
                        result.write(buffer, 0, read)
                    }
                }
                result.toByteArray()
            } finally { connection.disconnect() }
        }
    }
    open suspend fun json(url: String): JSONObject {
        val value = JSONObject(bytes(url).toString(Charsets.UTF_8))
        if (value.has("error")) throw IOException(L.s(R.string.msg_the_source_could_not_complete_the_request_0279))
        return value
    }
}

class CatalogRepository(private val db: OpusDatabase, private val filesDir: File, http: CatalogHttp? = null, private val achievements: AchievementStore = AchievementStore(db), private val mediaAllowed: () -> Boolean = { false }) {
    private val http = http ?: CatalogHttp(mediaAllowed)
    private val cacheGate = Mutex()
    private val dao = db.catalogDao()
    companion object {
        const val PORTRAIT_LIMIT = 32L * 1024 * 1024
        const val RESPONSE_LIMIT = 16L * 1024 * 1024
    }
    private val portraitDirectory get() = File(filesDir, "catalog-portraits")
    private fun trimPortraits() {
        val files = portraitDirectory.listFiles()?.filter { it.isFile }?.sortedBy { it.lastModified() }.orEmpty()
        var bytes = files.sumOf { it.length() }
        files.forEach { if (bytes > PORTRAIT_LIMIT) { val size = it.length(); if (it.delete()) bytes -= size } }
    }
    private fun responseBytes(): Long = db.openHelper.readableDatabase.query("SELECT COALESCE(SUM(length(CAST(payload AS BLOB))), 0) FROM catalog_cache").use { it.moveToFirst(); it.getLong(0) }
    private fun trimResponses() {
        val sql = db.openHelper.writableDatabase
        var bytes = responseBytes()
        if (bytes <= RESPONSE_LIMIT) return
        val victims = mutableListOf<String>()
        sql.query("SELECT cacheKey, length(CAST(payload AS BLOB)) FROM catalog_cache ORDER BY fetchedAt, cacheKey").use { rows ->
            while (bytes > RESPONSE_LIMIT && rows.moveToNext()) { victims += rows.getString(0); bytes -= rows.getLong(1) }
        }
        victims.forEach { sql.execSQL("DELETE FROM catalog_cache WHERE cacheKey = ?", arrayOf(it)) }
    }
    suspend fun cacheBytes(): Long = withContext(Dispatchers.IO) { cacheGate.withLock {
        (portraitDirectory.listFiles()?.sumOf { it.length() } ?: 0L) + responseBytes()
    } }
    /** Only downloaded responses/images are disposable. Never delete normalized catalogue or personal tables. */
    suspend fun clearCache() = withContext(Dispatchers.IO) { cacheGate.withLock {
        db.openHelper.writableDatabase.execSQL("DELETE FROM catalog_cache")
        portraitDirectory.listFiles()?.filter { it.isFile }?.forEach { check(it.delete()) { L.s(R.string.msg_could_not_clear_a_cached_portrait_c3d6) } }
        db.invalidationTracker.refreshAsync()
    } }
    /** Wait for current downloads before replacing data; no old response can write across restore. */
    suspend fun restoreJournal(block: suspend () -> Unit) = withContext(Dispatchers.IO) { cacheGate.withLock {
        block(); runCatching { trimResponses(); trimPortraits() }
    } }

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

    private suspend fun openOpus(path: String, force: Boolean = false): CatalogLoad<JSONObject> = withContext(Dispatchers.IO) { cacheGate.withLock {
        val cached = dao.cache(path)
        if (!force && cached != null && System.currentTimeMillis() - cached.fetchedAt < 7 * 86400000L) {
            val json = JSONObject(cached.payload)
            // Normalized rows were committed atomically with this response; do not replay them on every hit.
            return@withContext CatalogLoad(json, true, fetchedAt = cached.fetchedAt)
        }
        try {
            val json = http.json("https://api.openopus.org/$path")
            val status = json.getJSONObject("status")
            if (status.text("success") != "true" && status.text("error") != "Nothing found") throw IOException(L.s(R.string.msg_open_opus_is_temporarily_unavailable_47ca))
            val now = System.currentTimeMillis()
            // Parse and persist atomically. A failed refresh leaves the previous cache untouched.
            db.withTransaction { ingest(json); dao.putCache(CatalogCache(path, json.toString(), now)); trimResponses() }
            CatalogLoad(json, false, fetchedAt = now)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) {
            if (cached == null) throw IOException(L.s(R.string.msg_unable_to_load_open_opus_check_your_connecti_8d3d), error)
            CatalogLoad(JSONObject(cached.payload), true, L.s(R.string.catalogue_saved_refresh_failed), cached.fetchedAt)
        }
    } }

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

    suspend fun profile(c: CatalogComposer, force: Boolean = false, download: Boolean = false): CatalogLoad<ComposerProfile> = withContext(Dispatchers.IO) { cacheGate.withLock {
        val key = "profile:${c.id}"
        val cached = dao.cache(key)
        if (!force && cached != null && JSONObject(cached.payload).optInt("profileVersion") >= 5 && System.currentTimeMillis() - cached.fetchedAt < 30 * 86400000L)
            return@withContext CatalogLoad(profileFromJson(JSONObject(cached.payload)), true, fetchedAt = cached.fetchedAt)
        if (!download || !mediaAllowed()) return@withContext CatalogLoad(cached?.let { profileFromJson(JSONObject(it.payload)) } ?: ComposerProfile(), true, fetchedAt=cached?.fetchedAt ?: 0)
        try {
            val profile = WikimediaProfile(http).load(c)
            check(mediaAllowed()) {L.s(R.string.media_consent_needed)}
            val now = System.currentTimeMillis()
            dao.putCache(CatalogCache(key, profileToJson(profile).toString(), now)); trimResponses()
            CatalogLoad(profile, false, fetchedAt = now)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) {
            if (cached == null) throw IOException(L.s(R.string.msg_biography_and_portrait_could_not_be_loaded_c_af9d), error)
            CatalogLoad(profileFromJson(JSONObject(cached.payload)), true, L.s(R.string.biography_saved_refresh_failed), cached.fetchedAt)
        }
    } }

    suspend fun removeSupplement(id: String) = withContext(Dispatchers.IO) { cacheGate.withLock {
        val key="profile:$id"
        val saved=dao.cache(key)
        saved?.let {profileFromJson(JSONObject(it.payload)).portrait}?.let {p->
            val digest=MessageDigest.getInstance("SHA-256").digest(p.url.toByteArray()).joinToString("") {"%02x".format(it)}
            val image=File(portraitDirectory,"$digest.img")
            if(image.exists()) check(image.delete())
        }
        db.openHelper.writableDatabase.execSQL("DELETE FROM catalog_cache WHERE cacheKey=?",arrayOf(key))
    } }
    suspend fun smallPortrait(c: CatalogComposer): Portrait? = withContext(Dispatchers.IO) { cacheGate.withLock {
        val key="small-portrait:${c.id}"
        val cached=dao.cache(key)
        if (cached != null && System.currentTimeMillis()-cached.fetchedAt<30L*86400000) return@withContext profileFromJson(JSONObject(cached.payload)).portrait
        if (!mediaAllowed()) return@withContext cached?.let { profileFromJson(JSONObject(it.payload)).portrait }
        val profile=WikimediaProfile(http).load(c, portraitOnly=true)
        if (!mediaAllowed()) return@withContext null
        dao.putCache(CatalogCache(key,profileToJson(profile).toString(),System.currentTimeMillis()));trimResponses()
        profile.portrait
    } }
    /** App-private persistent portrait cache, never part of repertoire or uploaded. */
    suspend fun portrait(url: String): Bitmap? = withContext(Dispatchers.IO) { cacheGate.withLock {
        val digest = MessageDigest.getInstance("SHA-256").digest(url.toByteArray()).joinToString("") { "%02x".format(it) }
        val directory = portraitDirectory.apply { mkdirs() }
        val file = File(directory, "$digest.img")
        if (file.exists()) decodePortrait(file.readBytes())?.let { file.setLastModified(System.currentTimeMillis()); return@withContext it }
        if (!mediaAllowed()) return@withContext null
        val bytes = http.bytes(url, image = true)
        if (!mediaAllowed()) return@withContext null
        val bitmap = decodePortrait(bytes) ?: return@withContext null
        val temporary = File.createTempFile("portrait-", ".tmp", directory)
        try { temporary.writeBytes(bytes); if (!temporary.renameTo(file)) throw IOException(L.s(R.string.msg_could_not_cache_portrait_538e)) }
        finally { temporary.delete() }
        trimPortraits()
        bitmap
    } }
}

/** Decode source images to at most about 512 pixels per side before allocating pixel storage. */
private fun decodePortrait(bytes: ByteArray): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    val options = BitmapFactory.Options().apply { inSampleSize = 1; while (maxOf(bounds.outWidth, bounds.outHeight) / inSampleSize > 512) inSampleSize *= 2 }
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
}
