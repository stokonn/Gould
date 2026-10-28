package com.opus.piano.data

/** Domain facts only. Recording events are emitted after verified file publication, never clicks. */
object StudioAchievementRules {
    const val RECORDING_SAVED = "recording_saved"
    const val PLAYBACK_VERIFIED = "playback_verified"
    const val NOTE_SAVED = "note_saved"

    fun recordingKey(sha256: String): String {
        require(sha256.matches(Regex("[a-f0-9]{64}")))
        // Content identity survives importing the same performance with another database ID.
        return "$RECORDING_SAVED:$sha256"
    }

    fun distinctRecordings(events: List<AchievementEvent>): Long = events.asSequence()
        .filter { it.kind == RECORDING_SAVED && it.value > 0 && it.occurredAt > 0 && it.subject.matches(Regex("[a-f0-9]{64}")) && it.eventKey == recordingKey(it.subject) }
        .map { it.subject }.distinct().count().toLong()

    fun foundingPractice(sessions: List<PracticeSession>, verifiedAt: Long, stableOneCutoff: Long?): Boolean {
        require(verifiedAt > 0 && (stableOneCutoff == null || stableOneCutoff > 0))
        return sessions.any { AchievementEngine.valid(it) && it.completedAt <= verifiedAt &&
            (stableOneCutoff == null || it.completedAt < stableOneCutoff) }
    }

    fun playbackThreshold(durationMillis: Long): Long {
        require(durationMillis > 0)
        return minOf(10_000, durationMillis)
    }

    fun versatile(sessions: List<PracticeSession>, parts: List<SessionPart>): Boolean {
        val bySession = parts.groupBy { it.sessionId }
        return sessions.filter(AchievementEngine::valid).any { session ->
            bySession[session.id].orEmpty().filter { it.durationMillis > 0 }.mapNotNull {
                it.catalogWorkId?.let { id -> "catalog:$id" } ?: it.pieceId?.let { id -> "piece:$id" }
            }.distinct().size >= 3
        }
    }
}

/** No imaginary release date: the real 1.0 Stable workflow must supply its actual cutoff. */
object FoundingPolicy { val stableOneCutoffMillis: Long? = null }
