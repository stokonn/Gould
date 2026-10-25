package com.opus.piano.data.community.canonical

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.time.Instant
import java.util.Base64

/** The caller supplies the reviewed shared schema; no protocol/version is inferred. */
class AdminContract(schema: String) {
    private val definitions = JSONObject(schema).getJSONObject("\$defs")

    fun decode(dto: String, raw: String, request: Boolean = false): AdminDocument {
        require(raw.toByteArray(Charsets.UTF_8).size <= if (request) 16384 else 524288)
        StrictAdminJson.check(raw)
        val tokener = JSONTokener(raw)
        val value = tokener.nextValue()
        require(value is JSONObject && tokener.nextClean() == '\u0000')
        validate(definitions.getJSONObject(dto), value, request, 0)
        return AdminDocument(dto, value.toString())
    }

    private fun validate(rule: JSONObject, value: Any, request: Boolean, depth: Int) {
        require(depth <= 24)
        if (rule.has("\$ref")) {
            val ref = rule.getString("\$ref")
            require(ref.startsWith("#/\$defs/"))
            validate(definitions.getJSONObject(ref.substringAfterLast('/')), value, request, depth + 1)
            return
        }
        rule.optJSONArray("anyOf")?.let { alternatives ->
            require((0 until alternatives.length()).any { i ->
                runCatching { validate(alternatives.getJSONObject(i), value, request, depth + 1) }.isSuccess
            })
            return
        }
        if (rule.has("const")) require(same(rule.get("const"), value))
        rule.optJSONArray("enum")?.let { values -> require((0 until values.length()).any { same(values.get(it), value) }) }
        when (rule.optString("type")) {
            "null" -> require(value === JSONObject.NULL)
            "object" -> {
                require(value is JSONObject)
                val props = rule.getJSONObject("properties")
                val required = rule.getJSONArray("required")
                for (i in 0 until required.length()) require(value.has(required.getString(i)))
                for (key in value.keys()) {
                    if (props.has(key)) validate(props.getJSONObject(key), value.get(key), request, depth + 1)
                    else require(!request) // additive responses only; never permissive requests
                }
            }
            "array" -> {
                require(value is JSONArray)
                require(value.length() >= rule.optInt("minItems", 0) && value.length() <= rule.optInt("maxItems", 1000))
                for (i in 0 until value.length()) validate(rule.getJSONObject("items"), value.get(i), request, depth + 1)
            }
            "string" -> {
                require(value is String && Charsets.UTF_8.newEncoder().canEncode(value))
                require(value.length >= rule.optInt("minLength", 0) && value.length <= rule.optInt("maxLength", 524288))
                require(value.toByteArray(Charsets.UTF_8).size <= rule.optInt("x-max-utf8-bytes", 524288))
                if (rule.has("pattern")) require(Regex(rule.getString("pattern")).matches(value))
                if (rule.optString("format") == "date-time") {
                    require(java.time.format.DateTimeFormatterBuilder().appendInstant(6).toFormatter().format(Instant.parse(value)) == value)
                }
                if (rule.has("x-int8-min")) {
                    require(value.length <= 19)
                    val number = value.toLong()
                    require(number >= rule.getString("x-int8-min").toLong() && number <= rule.getString("x-int8-max").toLong())
                }
                if (rule.optBoolean("x-base64url")) {
                    val decoded = Base64.getUrlDecoder().decode(value)
                    require(Base64.getUrlEncoder().withoutPadding().encodeToString(decoded) == value)
                }
                if (rule.optBoolean("x-plain-text")) {
                    require(value.isNotBlank() && value.none { it < ' ' || it == '\u007f' || it == '<' || it == '>' })
                    require(!Regex("&#|&lt;|&gt;|javascript:|data:|vbscript:", RegexOption.IGNORE_CASE).containsMatchIn(value))
                }
            }
        }
    }

    private fun same(a: Any, b: Any): Boolean = when {
        a is Number -> b is Number && a.toString() == b.toString()
        else -> a == b
    }
}

/** Copy on access: callers cannot mutate a previously validated response or review. */
class AdminDocument internal constructor(val dto: String, private val encoded: String) {
    fun json(): JSONObject = JSONObject(encoded)
    override fun toString(): String = "AdminDocument($dto)"
}

/** org.json accepts JavaScript-like input; enforce the JSON wire grammar first. */
internal object StrictAdminJson {
    fun check(raw: String) {
        require(Charsets.UTF_8.newEncoder().canEncode(raw))
        var at = 0
        var nodes = 0
        fun space() { while (at < raw.length && raw[at] in " \t\r\n") at++ }
        fun string(): String {
            val start = at
            require(at < raw.length && raw[at++] == '"')
            while (at < raw.length) {
                val c = raw[at++]
                if (c == '"') return JSONTokener(raw.substring(start, at)).nextValue() as String
                require(c >= ' ')
                if (c == '\\') {
                    require(at < raw.length)
                    when (raw[at++]) {
                        '"', '\\', '/', 'b', 'f', 'n', 'r', 't' -> Unit
                        'u' -> { require(at + 4 <= raw.length && raw.substring(at, at + 4).all { it in "0123456789abcdefABCDEF" }); at += 4 }
                        else -> throw IllegalArgumentException("Invalid JSON")
                    }
                }
            }
            throw IllegalArgumentException("Invalid JSON")
        }
        fun value(depth: Int) {
            require(depth <= 24 && ++nodes <= 20000)
            space(); require(at < raw.length)
            when (raw[at]) {
                '"' -> string()
                '{', '[' -> {
                    val objectValue = raw[at++] == '{'
                    val close = if (objectValue) '}' else ']'
                    val keys = mutableSetOf<String>()
                    space()
                    if (at < raw.length && raw[at] == close) { at++; return }
                    while (true) {
                        if (objectValue) { space(); require(keys.add(string())); space(); require(at < raw.length && raw[at++] == ':') }
                        value(depth + 1); space(); require(at < raw.length)
                        if (raw[at] == close) { at++; break }
                        require(raw[at++] == ',')
                    }
                }
                't', 'f', 'n' -> {
                    val literal = when (raw[at]) { 't' -> "true"; 'f' -> "false"; else -> "null" }
                    require(raw.startsWith(literal, at)); at += literal.length
                }
                else -> {
                    val number = Regex("-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?").find(raw, at)
                    require(number != null && number.range.first == at); at = number.range.last + 1
                }
            }
        }
        value(0); space(); require(at == raw.length)
    }
}
