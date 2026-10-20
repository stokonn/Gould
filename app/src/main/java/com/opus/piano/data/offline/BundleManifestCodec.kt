package com.opus.piano.data.offline

import org.json.JSONArray
import org.json.JSONObject

/**
 * Versioned JSON codec for durable bundle manifests (Stage 5).
 *
 * Manifests are metadata only: they never contain personal practice data and never leave
 * app-private storage. The codec is deliberately strict because a manifest is the root that
 * keeps content-addressed files alive:
 *
 *  - [encode]/[decode] are bounded to [MAX_ENCODED_BYTES] (1 MiB) and pin [FORMAT_VERSION]
 *  - [validateFile] enforces sha256/MIME shape plus mandatory license/attribution/source
 *    with https-only license/source URLs (D5.5: a bundle cannot be published without
 *    offline credits)
 *  - [requirePath] rejects absolute paths, drive/ADS separators, backslashes, traversal
 *    segments, `.git` references, NUL and oversized paths before any path is ever used
 *
 * NOTE for tooling: this source intentionally contains no raw control characters. The NUL
 * character is detected by code point (Char.code == 0), never written as a literal byte.
 */
object BundleManifestCodec {

    /** On-disk format generation. Readers refuse anything else instead of guessing. */
    const val FORMAT_VERSION = 1

    /** A manifest is metadata, never a content transport: hard 1 MiB bound. */
    const val MAX_ENCODED_BYTES = 1024 * 1024

    /** Absolute bound for a logical relative path (segment rules are checked separately). */
    const val MAX_PATH_CHARS = 512

    private val SHA256 = Regex("[0-9a-f]{64}")
    private val MIME = Regex(
        "[a-z0-9][a-z0-9!#$&^_.+-]*/[a-z0-9][a-z0-9!#$&^_.+-]*",
        RegexOption.IGNORE_CASE
    )

    // ---------------------------------------------------------------- paths

    /**
     * Rejects any logical relative path that could escape the bundle root or smuggle
     * control data. Called for every file on both encode and decode.
     */
    fun requirePath(path: String) {
        require(path.isNotEmpty()) { "relativePath must not be empty" }
        require(path.length <= MAX_PATH_CHARS) { "relativePath exceeds $MAX_PATH_CHARS characters" }
        require(!path.startsWith("/")) { "relativePath must not be absolute: '$path'" }
        require(!path.contains(':')) { "relativePath must not contain ':': '$path'" }
        require(!path.contains('\\')) { "relativePath must not contain a backslash: '$path'" }
        require(path.none { it.code == 0 }) { "relativePath must not contain NUL" }
        val segments = path.split('/')
        require(segments.none { it.isEmpty() }) { "relativePath must not contain empty segments: '$path'" }
        require(segments.none { it == "." || it == ".." }) {
            "relativePath must not contain '.' or '..' segments: '$path'"
        }
        require(segments.none { it == ".git" }) { "relativePath must not reference '.git': '$path'" }
    }

    // ---------------------------------------------------------------- rights

    private fun isHttps(url: String): Boolean = url.startsWith("https://", ignoreCase = true)

    /** Mandatory offline credits per file; https-only for every retained URL. */
    fun validateRights(rights: BundleRights) {
        require(rights.license.isNotBlank()) { "per-file license is mandatory" }
        require(rights.attribution.isNotBlank()) { "per-file attribution is mandatory" }
        require(rights.sourceName.isNotBlank()) { "per-file source is mandatory" }
        require(rights.licenseUrl == null || isHttps(rights.licenseUrl)) {
            "licenseUrl must be https: '${rights.licenseUrl}'"
        }
        require(rights.sourceUrl == null || isHttps(rights.sourceUrl)) {
            "sourceUrl must be https: '${rights.sourceUrl}'"
        }
    }

    /** Full per-file validation: hash, size, MIME, language and rights + path safety. */
    fun validateFile(file: BundleFile) {
        require(file.sha256.matches(SHA256)) { "sha256 must be 64 lowercase hex characters" }
        require(file.bytes >= 0) { "bytes must not be negative" }
        require(file.mime.matches(MIME)) { "unsupported MIME type '${file.mime}'" }
        require(file.language == null || file.language.isNotBlank()) { "language must not be blank" }
        requirePath(file.relativePath)
        validateRights(file.rights)
    }

    // ---------------------------------------------------------------- encode

    fun encode(manifest: BundleManifest): String {
        manifest.files.forEach { validateFile(it) }
        val json = JSONObject().apply {
            put("formatVersion", FORMAT_VERSION)
            put("bundleId", manifest.bundleId)
            put("type", manifest.type.name)
            put("entityId", manifest.entityId)
            put("schemaVersion", manifest.schemaVersion)
            put("revision", manifest.revision)
            put("language", manifest.language)
            put("createdAt", manifest.createdAt)
            putNullable(this, "validatedAt", manifest.validatedAt)
            put("dependencies", JSONArray(manifest.dependencies))
            put("expectedBytes", manifest.expectedBytes)
            put("files", JSONArray().also { arr -> manifest.files.forEach { arr.put(fileJson(it)) } })
            putNullable(this, "providerKind", manifest.providerKind)
            putNullable(this, "providerRevision", manifest.providerRevision)
            put("attribution", manifest.attribution)
            put("minReaderVersion", manifest.minReaderVersion)
            put("state", manifest.state.name)
            put("progress", JSONObject().apply {
                put("completedBytes", manifest.progress.completedBytes)
                put("totalBytes", manifest.progress.totalBytes)
                put("completedFiles", manifest.progress.completedFiles)
            })
            putNullable(this, "lastError", manifest.lastError)
            put("updatedAt", manifest.updatedAt)
        }
        val raw = json.toString()
        require(raw.toByteArray(Charsets.UTF_8).size <= MAX_ENCODED_BYTES) {
            "manifest exceeds $MAX_ENCODED_BYTES bytes"
        }
        return raw
    }

    // ---------------------------------------------------------------- decode

    fun decode(raw: String): BundleManifest {
        require(raw.toByteArray(Charsets.UTF_8).size <= MAX_ENCODED_BYTES) {
            "manifest exceeds $MAX_ENCODED_BYTES bytes"
        }
        val json = JSONObject(raw)
        val version = json.optInt("formatVersion", -1)
        require(version == FORMAT_VERSION) {
            "unsupported manifest formatVersion $version (expected $FORMAT_VERSION)"
        }

        val manifest = BundleManifest(
            bundleId = requireString(json, "bundleId"),
            type = enumValue(json, "type", BundleType.entries),
            entityId = requireString(json, "entityId"),
            schemaVersion = requireInt(json, "schemaVersion"),
            revision = requireLong(json, "revision"),
            language = requireString(json, "language"),
            createdAt = requireLong(json, "createdAt"),
            validatedAt = nullableLong(json, "validatedAt"),
            dependencies = requireStringList(json, "dependencies"),
            expectedBytes = requireLong(json, "expectedBytes"),
            files = requireFiles(json),
            providerKind = nullableString(json, "providerKind"),
            providerRevision = nullableString(json, "providerRevision"),
            attribution = requireString(json, "attribution"),
            minReaderVersion = requireInt(json, "minReaderVersion"),
            state = enumValue(json, "state", BundleState.entries),
            progress = decodeProgress(json),
            lastError = nullableString(json, "lastError"),
            updatedAt = requireLong(json, "updatedAt")
        )
        // Re-run per-file validation so a tampered payload fails even if fields were reordered.
        manifest.files.forEach { validateFile(it) }
        return manifest
    }

    // ---------------------------------------------------------------- json helpers

    private fun putNullable(json: JSONObject, key: String, value: String?) {
        json.put(key, value ?: JSONObject.NULL)
    }

    private fun putNullable(json: JSONObject, key: String, value: Long?) {
        json.put(key, value ?: JSONObject.NULL)
    }

    private fun requireString(json: JSONObject, key: String): String {
        require(json.has(key) && !json.isNull(key)) { "missing required field '$key'" }
        val value = json.getString(key)
        require(value.isNotBlank()) { "field '$key' must not be blank" }
        return value
    }

    private fun nullableString(json: JSONObject, key: String): String? =
        if (!json.has(key) || json.isNull(key)) null else json.getString(key)

    private fun requireInt(json: JSONObject, key: String): Int {
        require(json.has(key) && !json.isNull(key)) { "missing required field '$key'" }
        return json.getInt(key)
    }

    private fun requireLong(json: JSONObject, key: String): Long {
        require(json.has(key) && !json.isNull(key)) { "missing required field '$key'" }
        return json.getLong(key)
    }

    private fun nullableLong(json: JSONObject, key: String): Long? =
        if (!json.has(key) || json.isNull(key)) null else json.getLong(key)

    private fun <T : Enum<T>> enumValue(json: JSONObject, key: String, values: List<T>): T {
        val name = requireString(json, key)
        return values.firstOrNull { it.name == name }
            ?: throw IllegalArgumentException("Unknown $key '$name'")
    }

    private fun requireStringList(json: JSONObject, key: String): List<String> {
        require(json.has(key) && !json.isNull(key)) { "missing required field '$key'" }
        val array = json.getJSONArray(key)
        return (0 until array.length()).map { index ->
            val value = array.getString(index)
            require(value.isNotBlank()) { "'$key[$index]' must not be blank" }
            value
        }
    }

    private fun requireFiles(json: JSONObject): List<BundleFile> {
        require(json.has("files") && !json.isNull("files")) { "missing required field 'files'" }
        val array = json.getJSONArray("files")
        return (0 until array.length()).map { index ->
            val obj = array.getJSONObject(index)
            BundleFile(
                sha256 = requireString(obj, "sha256"),
                bytes = requireLong(obj, "bytes"),
                mime = requireString(obj, "mime"),
                relativePath = requireString(obj, "relativePath"),
                language = nullableString(obj, "language"),
                rights = rightsFrom(obj)
            ).also { validateFile(it) }
        }
    }

    private fun decodeProgress(json: JSONObject): BundleProgress {
        val obj = json.optJSONObject("progress") ?: return BundleProgress()
        return BundleProgress(
            completedBytes = if (obj.isNull("completedBytes")) 0L else obj.optLong("completedBytes", 0L),
            totalBytes = if (obj.isNull("totalBytes")) 0L else obj.optLong("totalBytes", 0L),
            completedFiles = if (obj.isNull("completedFiles")) 0 else obj.optInt("completedFiles", 0)
        )
    }

    private fun fileJson(file: BundleFile): JSONObject = JSONObject().apply {
        put("sha256", file.sha256)
        put("bytes", file.bytes)
        put("mime", file.mime)
        put("relativePath", file.relativePath)
        put("language", file.language ?: JSONObject.NULL)
        put("rights", rightsJson(file.rights))
    }

    private fun rightsJson(rights: BundleRights): JSONObject = JSONObject().apply {
        put("license", rights.license)
        put("licenseUrl", rights.licenseUrl ?: JSONObject.NULL)
        put("attribution", rights.attribution)
        put("sourceName", rights.sourceName)
        put("sourceUrl", rights.sourceUrl ?: JSONObject.NULL)
    }

    private fun rightsFrom(json: JSONObject): BundleRights {
        val rights = json.optJSONObject("rights")
            ?: throw IllegalArgumentException("missing required field 'rights'")
        return BundleRights(
            license = requireString(rights, "license"),
            licenseUrl = nullableString(rights, "licenseUrl"),
            attribution = requireString(rights, "attribution"),
            sourceName = requireString(rights, "sourceName"),
            sourceUrl = nullableString(rights, "sourceUrl")
        )
    }
}
