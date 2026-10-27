package com.opus.piano.data.community

enum class CommunityProblem { DISABLED, OFFLINE, UNAVAILABLE, VALIDATION, UNAUTHENTICATED, FORBIDDEN, NOT_FOUND, CONFLICT, IDEMPOTENCY_MISMATCH, RATE_LIMITED, SERVER, STORAGE, FROZEN, OWNERSHIP }
data class CommunityFailure(val problem: CommunityProblem, val retryAtMillis: Long?=null)
object CommunityErrors {
    fun map(status: Int, retryAfter: String?, now: Long): CommunityFailure {
        val kind=when(status) {400->CommunityProblem.VALIDATION;401->CommunityProblem.UNAUTHENTICATED;403->CommunityProblem.FORBIDDEN;404->CommunityProblem.NOT_FOUND;409->CommunityProblem.CONFLICT;422->CommunityProblem.IDEMPOTENCY_MISMATCH;429->CommunityProblem.RATE_LIMITED;503->CommunityProblem.UNAVAILABLE;else->CommunityProblem.SERVER}
        val retry=if(status==429) {
            val seconds=retryAfter?.toLongOrNull()?.takeIf {it>=0}
            if(seconds!=null) runCatching {Math.addExact(now,Math.multiplyExact(seconds,1000))}.getOrNull()
            else runCatching {java.time.ZonedDateTime.parse(retryAfter,java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli().coerceAtLeast(now)}.getOrNull()
        } else null
        return CommunityFailure(kind,retry)
    }
}
