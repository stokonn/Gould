package com.opus.piano.admin

import org.json.JSONArray
import org.json.JSONObject

/**
 * Contract-shaped data model for the verified Stage 6 admin API (see
 * docs/planning/goldberg-0.11/stage-7/stage-7b/CONTRACT-AUDIT.md).
 *
 * These classes mirror the C6 spike wire shapes byte-for-byte in FIELD NAME and TYPE:
 *   GET  /v1/me/capabilities      -> { principalId, capabilities:[..] }
 *   GET  /v1/admin/queue          -> { items:[{ id, owner, status, revision,
 *                                              evidenceCount, createdAt }], nextCursor }
 *   GET  /v1/admin/submissions/{id} -> { id, owner, status, revision,
 *                                        revisions:[{ revision, payload, createdAt }],
 *                                        reviewEvents:[{ revision, decision, actor_id,
 *                                                        reason, created_at }] }
 *   POST /v1/admin/submissions/{id}/decisions -> 201 { submissionId, decision, status,
 *                                                      revision, publishedRevisionId?,
 *                                                      entityUuid? }
 *   GET  /v1/admin/audit          -> { items:[{ seq, ts, actor, action, subject,
 *                                              prevHash, thisHash }], nextCursor, chainHead }
 *
 * Unknown JSON fields are tolerated (forward-compatible); NO field is invented. Payload
 * bodies are passed through as raw JSONObject/JSONArray so the comparison UI can render
 * contributor fields without this model asserting a schema the server did not define.
 */

/** Server-side moderation status. "Needs info" in the UI == server `changes_requested`. */
enum class AdminStatus(val wire: String) {
    SUBMITTED("submitted"), IN_REVIEW("in_review"), CHANGES_REQUESTED("changes_requested"),
    APPROVED("approved"), PUBLISHED("published"), REJECTED("rejected"), WITHDRAWN("withdrawn");

    /** True while the item is still awaiting a moderator decision (decidable per C6). */
    val decidable: Boolean get() = this == SUBMITTED || this == IN_REVIEW || this == CHANGES_REQUESTED

    companion object {
        fun fromWire(raw: String?): AdminStatus =
            entries.firstOrNull { it.wire == raw } ?: SUBMITTED
    }
}

/** Decision verb accepted by the server. `request_changes` -> status `changes_requested`. */
enum class AdminDecision(val wire: String) { APPROVE("approve"), REJECT("reject"), REQUEST_CHANGES("request_changes") }

data class AdminCapabilities(val principalId: String, val capabilities: Set<String>) {
    val canQueueRead get() = "queue.read" in capabilities
    val canDecide get() = "decide" in capabilities
    val canAuditRead get() = "audit.read" in capabilities
    /** Human role label for the dashboard header (server-derived, never client-asserted). */
    val roleLabel: String get() = when {
        "roles.manage" in capabilities -> "Administrator"
        canQueueRead -> "Moderator"
        else -> "Contributor"
    }
    companion object {
        fun parse(json: JSONObject): AdminCapabilities {
            val caps = JSONArray()
            val arr = json.optJSONArray("capabilities") ?: JSONArray()
            for (i in 0 until arr.length()) caps.put(arr.optString(i))
            val set = (0 until arr.length()).mapNotNull { arr.optString(it)?.takeIf(String::isNotBlank) }.toSet()
            return AdminCapabilities(json.optString("principalId", ""), set)
        }
    }
}

/** One queue row. Carries NO title/composer (gap G2) — display fields come from detail. */
data class AdminQueueItem(
    val id: String, val owner: String, val status: AdminStatus, val revision: Int,
    val evidenceCount: Int, val createdAt: Long
) {
    companion object {
        fun parse(o: JSONObject) = AdminQueueItem(
            id = o.optString("id"), owner = o.optString("owner"),
            status = AdminStatus.fromWire(o.optString("status")),
            revision = o.optInt("revision", 1), evidenceCount = o.optInt("evidenceCount", 0),
            createdAt = o.optLong("createdAt", 0L)
        )
    }
}

data class AdminQueuePage(val items: List<AdminQueueItem>, val nextCursor: String?) {
    companion object {
        fun parse(o: JSONObject): AdminQueuePage {
            val arr = o.optJSONArray("items") ?: JSONArray()
            val items = (0 until arr.length()).mapNotNull { arr.optJSONObject(it)?.let(AdminQueueItem::parse) }
            val cursor = o.optString("nextCursor").takeIf { it.isNotBlank() }
            return AdminQueuePage(items, cursor)
        }
    }
}

/** A contributor review event (immutable). Wire uses `actor_id`/`created_at` (snake). */
data class AdminReviewEvent(val revision: Int, val decision: String, val actorId: String, val reason: String, val createdAt: Long) {
    companion object {
        fun parse(o: JSONObject) = AdminReviewEvent(
            revision = o.optInt("revision", 1), decision = o.optString("decision"),
            actorId = o.optString("actor_id"), reason = o.optString("reason"),
            createdAt = o.optLong("created_at", 0L)
        )
    }
}

data class AdminRevision(val revision: Int, val payload: JSONObject, val createdAt: Long) {
    companion object {
        fun parse(o: JSONObject) = AdminRevision(
            revision = o.optInt("revision", 1), payload = o.optJSONObject("payload") ?: JSONObject(),
            createdAt = o.optLong("createdAt", 0L)
        )
    }
}

data class AdminSubmissionDetail(
    val id: String, val owner: String, val status: AdminStatus, val revision: Int,
    val revisions: List<AdminRevision>, val reviewEvents: List<AdminReviewEvent>
) {
    /** Latest verified server revision (never a stale cached one). */
    val latest: AdminRevision? = revisions.maxByOrNull { it.revision }
    companion object {
        fun parse(o: JSONObject): AdminSubmissionDetail {
            val revs = o.optJSONArray("revisions") ?: JSONArray()
            val evts = o.optJSONArray("reviewEvents") ?: JSONArray()
            return AdminSubmissionDetail(
                id = o.optString("id"), owner = o.optString("owner"),
                status = AdminStatus.fromWire(o.optString("status")), revision = o.optInt("revision", 1),
                revisions = (0 until revs.length()).mapNotNull { revs.optJSONObject(it)?.let(AdminRevision::parse) },
                reviewEvents = (0 until evts.length()).mapNotNull { evts.optJSONObject(it)?.let(AdminReviewEvent::parse) }
            )
        }
    }
}

data class AdminDecisionResult(
    val submissionId: String, val decision: String, val status: AdminStatus, val revision: Int,
    val publishedRevisionId: String?, val entityUuid: String?
) {
    companion object {
        fun parse(o: JSONObject) = AdminDecisionResult(
            submissionId = o.optString("submissionId"), decision = o.optString("decision"),
            status = AdminStatus.fromWire(o.optString("status")), revision = o.optInt("revision", 1),
            publishedRevisionId = o.optString("publishedRevisionId").takeIf { it.isNotBlank() },
            entityUuid = o.optString("entityUuid").takeIf { it.isNotBlank() }
        )
    }
}

data class AdminAuditEvent(
    val seq: Long, val ts: Long, val actor: String, val action: String,
    val subject: String, val prevHash: String, val thisHash: String
) {
    companion object {
        fun parse(o: JSONObject) = AdminAuditEvent(
            seq = o.optLong("seq"), ts = o.optLong("ts"), actor = o.optString("actor"),
            action = o.optString("action"), subject = o.optString("subject"),
            prevHash = o.optString("prevHash"), thisHash = o.optString("thisHash")
        )
    }
}

data class AdminAuditPage(val items: List<AdminAuditEvent>, val nextCursor: String?, val chainHead: String?) {
    companion object {
        fun parse(o: JSONObject): AdminAuditPage {
            val arr = o.optJSONArray("items") ?: JSONArray()
            val items = (0 until arr.length()).mapNotNull { arr.optJSONObject(it)?.let(AdminAuditEvent::parse) }
            return AdminAuditPage(items, o.optString("nextCursor").takeIf { it.isNotBlank() }, o.optString("chainHead"))
        }
    }
}

/** Build a decision request body exactly as the server validates it. */
fun buildDecisionBody(decision: AdminDecision, expectedRevision: Int, reason: String?, idempotencyKey: String): JSONObject =
    JSONObject()
        .put("schemaVersion", 1)
        .put("decision", decision.wire)
        .put("expectedRevision", expectedRevision)
        .put("idempotencyKey", idempotencyKey)
        .also { if (!reason.isNullOrBlank()) it.put("reason", reason) }
