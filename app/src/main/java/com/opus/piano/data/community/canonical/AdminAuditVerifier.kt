package com.opus.piano.data.community.canonical

import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

/** Bounded verifier for one account-bound fixed-highwater audit walk, never a live-page total. */
class AdminAuditVerifier {
    private var highwater: String? = null
    private var head: String? = null
    private var coverage: String? = null
    private var previousSeq = 0L
    private var previousHash = "0".repeat(64)
    private var complete = false

    fun accept(page: AdminDocument) {
        require(page.dto == "AuditPage" && !complete)
        val o = page.json()
        val currentHighwater = o.getString("highwater")
        val currentHead = o.getString("headHash")
        val currentCoverage = o.getString("coverage")
        require(highwater == null || highwater == currentHighwater)
        require(head == null || head == currentHead)
        require(coverage == null || coverage == currentCoverage)
        var seq = previousSeq
        var hash = previousHash
        val items = o.getJSONArray("items")
        for (i in 0 until items.length()) {
            val event = items.getJSONObject(i)
            val next = event.getString("seq").toLong()
            require(next > seq && next <= currentHighwater.toLong())
            if (currentCoverage == "chain") {
                require(seq != Long.MAX_VALUE && next == seq + 1 && event.getString("prevHash") == hash)
            }
            verifyEvent(event)
            seq = next
            hash = event.getString("thisHash")
        }
        val done = o.isNull("nextCursor")
        require(done || items.length() > 0)
        if (done && currentCoverage == "chain") require(seq.toString() == currentHighwater && hash == currentHead)
        // Commit verifier state only after the entire page is valid.
        highwater = currentHighwater; head = currentHead; coverage = currentCoverage
        previousSeq = seq; previousHash = hash; complete = done
    }

    companion object {
        fun verifyEvent(event: JSONObject) {
            val content = event.getString("canonicalContent")
            StrictAdminJson.check(content)
            val fields = JSONArray(content)
            val subject = event.getJSONObject("subject")
            val expected = listOf("goldberg-audit-v1", event.get("seq"), event.getJSONObject("actor").get("principalId"),
                event.get("action"), subject.get("kind"), subject.get("id"), subject.get("revision"),
                event.get("beforeHash"), event.get("afterHash"), event.get("reason"), event.get("createdAt"), event.get("requestId"))
            require(fields.length() == expected.size && expected.indices.all { fields.get(it) == expected[it] })
            val input = event.getString("prevHash") + ":" + content
            val hash = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it.toInt() and 255) }
            require(hash == event.getString("thisHash"))
        }
    }
}
