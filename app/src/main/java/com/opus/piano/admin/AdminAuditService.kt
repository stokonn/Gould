package com.opus.piano.admin

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class AdminAuditState(
    val items: List<AdminAuditEvent> = emptyList(), val cursor: String? = null,
    val loaded: Boolean = false, val loading: Boolean = false, val failure: AdminFailure? = null,
)
/** Read-only projection. Search/filters cover fetched pages only (G3), never a
 * claim of server-wide completeness. UI preserves server chronological ordering. */
class AdminAuditService(private val api: AdminApi) {
    private val mutable = MutableStateFlow(AdminAuditState())
    val state: StateFlow<AdminAuditState> = mutable
    private val mutex = Mutex()
    private var generation = 0
    private val cursors = mutableSetOf<String>()
    fun clear() { generation++; mutable.value=AdminAuditState(); cursors.clear() }
    suspend fun load(reset: Boolean = false) = mutex.withLock {
        val old=mutable.value
        if (!reset && old.loaded && old.cursor == null) return@withLock
        val stamp=generation
        mutable.value=old.copy(loading=true,failure=null)
        try {
            when(val result=api.audit(if(reset) null else old.cursor)) {
                is AdminResult.Err -> if(stamp==generation) mutable.value=mutable.value.copy(failure=result.failure)
                is AdminResult.Ok -> if(stamp==generation) {
                    val next=result.value.nextCursor
                    if(!reset && next!=null && next in cursors) {
                        mutable.value=old.copy(failure=AdminFailure.InvalidPayload)
                    } else {
                        if(reset) cursors.clear()
                        if(next!=null) cursors.add(next)
                        val events=(if(reset) emptyList() else old.items)+result.value.items
                        mutable.value=AdminAuditState(events.distinctBy { it.seq }.sortedBy { it.seq },next,loaded=true)
                    }
                }
            }
        } finally { if(stamp==generation) mutable.value=mutable.value.copy(loading=false) }
    }
    fun filtered(query: String, filter: String, principal: String, now: Long = System.currentTimeMillis()): List<AdminAuditEvent> {
        val needle=query.trim().lowercase()
        val today=java.time.Instant.ofEpochMilli(now).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        return mutable.value.items.filter {
            (!filter.startsWith("today_") ||
                (it.action == "decision.${filter.removePrefix("today_")}" &&
                java.time.Instant.ofEpochMilli(it.ts).atZone(java.time.ZoneId.systemDefault()).toLocalDate()==today)) &&
            (filter != "mine" || it.actor==principal) &&
            (filter != "moderation" || it.action.startsWith("decision.")) &&
            (needle.isEmpty() || listOf(it.action,it.actor,it.subject,it.seq.toString()).any { v -> v.lowercase().contains(needle) })
        }
    }
}
