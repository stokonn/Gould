package com.opus.piano.admin

import org.json.JSONObject

/** Exact legacy DTO primitives. Does not rename candidate fields, widen revision
 * ranges or activate a candidate adapter. Bad values fail closed at AdminApi. */
internal object AdminWireValues {
    fun unsignedLong(o: JSONObject, key: String, positive: Boolean = false): Long {
        val raw = o.opt(key)
        val text = when (raw) {
            is Int, is Long, is java.math.BigInteger -> raw.toString()
            is String -> raw
            else -> throw IllegalArgumentException("Expected exact integer")
        }
        require(text.matches(Regex("0|[1-9][0-9]*")))
        val value = text.toLongOrNull() ?: throw IllegalArgumentException("Integer outside Long range")
        require(!positive || value > 0)
        return value
    }

    fun revision(o: JSONObject, key: String = "revision"): Int {
        val value = unsignedLong(o, key, positive = true)
        require(value <= Int.MAX_VALUE)
        return value.toInt()
    }

    fun text(o: JSONObject, key: String): String {
        val raw = o.opt(key)
        require(raw is String && raw.isNotBlank())
        return raw
    }

    fun cursor(o: JSONObject): String? {
        if (!o.has("nextCursor") || o.isNull("nextCursor")) return null
        val raw = o.opt("nextCursor")
        require(raw is String) // Never serialize a candidate object cursor as a C6 string.
        return raw.takeIf { it.isNotBlank() }
    }
}
