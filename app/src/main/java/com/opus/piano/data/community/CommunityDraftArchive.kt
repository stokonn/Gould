package com.opus.piano.data.community

import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.util.UUID

/** Separate user-directed draft recovery format; unrelated to Gould's portable backup versions. */
object CommunityDraftArchive {
    const val MIME_TYPE = "application/vnd.gould.community-drafts+json"
    const val MAX_BYTES = 16 * 1024 * 1024
    private const val FORMAT = "gould-community-drafts"

    fun readBounded(input: InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            require(output.size() + count <= MAX_BYTES) { "Draft archive is too large" }
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    fun encode(owner: CommunityOwner, drafts: List<CommunityDraft>): ByteArray {
        require(drafts.all { it.owner == owner })
        val archive = JSONObject()
            .put("format", FORMAT)
            .put("version", 1)
            .put("ownerType", owner.key.substringBefore(':'))
            .apply { if (owner.key.startsWith("principal:")) put("principalId", owner.key.substringAfter(':')) }
            .put("drafts", JSONArray().apply { drafts.sortedBy { it.id }.forEach { put(encodeDraft(it)) } })
            .toString().toByteArray(Charsets.UTF_8)
        require(archive.size <= MAX_BYTES) { "Draft archive is too large" }
        return archive
    }

    /** Fully parse and validate before the caller is allowed to write any imported record. */
    data class Contents(val principalId: String?, val drafts: List<CommunityDraft>)

    fun decode(bytes: ByteArray, currentPrincipalId: String?): Contents = try {
        decodeChecked(bytes, currentPrincipalId)
    } catch (failure: IllegalArgumentException) {
        throw failure
    } catch (failure: Exception) {
        throw IllegalArgumentException("Malformed draft archive", failure)
    }

    private fun decodeChecked(bytes: ByteArray, currentPrincipalId: String?): Contents {
        require(bytes.size <= MAX_BYTES) { "Draft archive is too large" }
        val text = Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString()
        val root = JSONObject(text)
        keys(root, setOf("format", "version", "ownerType", "principalId", "drafts"), setOf("format", "version", "ownerType", "drafts"))
        require(root.getString("format") == FORMAT && root.get("version") is Int && root.getInt("version") == 1) {
            "Unsupported draft archive"
        }
        val archiveOwner = root.getString("ownerType")
        require(archiveOwner == "guest" || archiveOwner == "principal") { "Invalid draft owner" }
        val archivePrincipal = if (archiveOwner == "principal") {
            val principalId = root.getString("principalId")
            require(UUID.fromString(principalId).toString() == principalId && currentPrincipalId == principalId) {
                "Draft archive belongs to a different account"
            }
            principalId
        } else {
            require(!root.has("principalId") && currentPrincipalId == null) {
                "Guest drafts can only be restored to guest storage"
            }
            null
        }
        val parsedOwner = archivePrincipal?.let { CommunityOwner("principal:$it") }
            ?: CommunityOwner("guest:00000000-0000-4000-8000-000000000000")
        val records = root.getJSONArray("drafts")
        val ids = HashSet<String>(records.length())
        val drafts = (0 until records.length()).map { index ->
            val draft = decodeDraft(records.getJSONObject(index), parsedOwner)
            require(ids.add(draft.id)) { "Duplicate draft ID" }
            draft
        }
        return Contents(archivePrincipal, drafts)
    }

    private fun encodeDraft(draft: CommunityDraft): JSONObject = JSONObject()
        .put("id", draft.id)
        .put("version", draft.version)
        .put("payload", CommunityCodec.payload(draft.payload, validate = false))
        .put("pending", draft.pending?.let { pending ->
            JSONObject().put("operation", pending.operation.name).put("body", pending.body).put("key", pending.key)
                .apply {
                    pending.outcome?.let { put("outcome", it.name) }
                    pending.retryAtMillis?.let { put("retryAtMillis", it) }
                }
        } ?: JSONObject.NULL)
        .put("receipt", draft.receipt ?: JSONObject.NULL)

    private fun decodeDraft(value: JSONObject, owner: CommunityOwner): CommunityDraft {
        keys(value, setOf("id", "version", "payload", "pending", "receipt"))
        val id = value.getString("id")
        require(UUID.fromString(id).toString() == id)
        val version = value.get("version")
        require((version is Int || version is Long) && (version as Number).toLong() > 0)
        val payloadJson = value.getJSONObject("payload")
        // Editable drafts may intentionally be incomplete. Once a request has been
        // frozen, however, its payload must satisfy the same semantic rules as a
        // request that the repository could have produced.
        val payload = CommunityCodec.decodePayload(payloadJson, validate = false)
        val pending = if (value.isNull("pending")) null else value.getJSONObject("pending").let { p ->
            keys(p, setOf("operation", "body", "key", "outcome", "retryAtMillis"), setOf("operation", "body", "key"))
            val key = p.getString("key")
            require(UUID.fromString(key).toString() == key && key == id)
            val body = p.getString("body")
            require(body.toByteArray(Charsets.UTF_8).size <= 16_384)
            val retryAt = if (p.has("retryAtMillis")) {
                p.get("retryAtMillis").also { require(it is Int || it is Long) }
                p.getLong("retryAtMillis").also { require(it >= 0) }
            } else null
            PendingContribution(
                ContributionOperation.valueOf(p.getString("operation")), body, key,
                if (p.has("outcome")) CommunityProblem.valueOf(p.getString("outcome")) else null,
                retryAt,
            ).also {
                CommunityCodec.decodePayload(payloadJson, validate = true)
                validateFrozenRequest(it, id, payload)
            }
        }
        val receipt = if (value.isNull("receipt")) null else value.getString("receipt").also {
            require(UUID.fromString(it).toString() == it)
        }
        return CommunityDraft(id, owner, (version as Number).toLong(), payload, pending, receipt)
    }

    private fun validateFrozenRequest(pending: PendingContribution, draftId: String, payload: ContributionPayload) {
        val request = JSONObject(pending.body)
        val common = setOf("schemaVersion", "expectedRevision", "idempotencyKey")
        val operationKeys = when (pending.operation) {
            ContributionOperation.CREATE -> common + "payload"
            ContributionOperation.REVISE -> common + setOf("id", "expectedSubmissionRevision", "payload")
            ContributionOperation.WITHDRAW -> common + setOf("id", "expectedSubmissionRevision", "reason")
        }
        val required = if (pending.operation == ContributionOperation.WITHDRAW) operationKeys - "reason" else operationKeys
        keys(request, operationKeys, required)
        require(request.get("schemaVersion") is Int && request.getInt("schemaVersion") == CommunityCodec.SCHEMA_VERSION)
        val expected = request.get("expectedRevision")
        require((expected is Int || expected is Long) && (expected as Number).toLong() in 0..9_007_199_254_740_991L)
        require(request.getString("idempotencyKey") == draftId)
        if (pending.operation != ContributionOperation.CREATE) {
            val submissionId = request.getString("id")
            require(UUID.fromString(submissionId).toString() == submissionId)
            val submissionRevision = request.get("expectedSubmissionRevision")
            require((submissionRevision is Int || submissionRevision is Long) && (submissionRevision as Number).toLong() in 1..9_007_199_254_740_991L)
        }
        val command = when (pending.operation) {
            ContributionOperation.CREATE -> ContributionCommand(ContributionOperation.CREATE, (expected as Number).toLong())
            ContributionOperation.REVISE, ContributionOperation.WITHDRAW -> ContributionCommand(
                operation = pending.operation,
                expectedRevision = (expected as Number).toLong(),
                submissionId = request.getString("id"),
                expectedSubmissionRevision = request.getLong("expectedSubmissionRevision"),
                reason = if (pending.operation == ContributionOperation.WITHDRAW && request.has("reason")) request.getString("reason") else null,
            )
        }
        // Re-encode with the production codec and require the entire frozen body to
        // match: operation, revisions, payload, reason, and idempotency key.
        require(CommunityCodec.request(payload, command, pending.key) == pending.body)
    }

    private fun keys(value: JSONObject, allowed: Set<String>, required: Set<String> = allowed) {
        val actual = value.keys().asSequence().toSet()
        require(actual.all { it in allowed } && actual.containsAll(required)) { "Incomplete or unsupported draft archive record" }
    }
}
