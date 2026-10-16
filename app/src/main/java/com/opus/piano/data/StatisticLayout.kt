package com.opus.piano.data

/** Stable IDs are stored instead of translated labels. Unknown future IDs fall back safely. */
object StatisticLayout {
    val defaults = listOf("time", "pieces", "sessions", "streak")
    fun normalize(ids: List<String>): List<String> = (ids.filter { it in defaults }.distinct() + defaults).distinct()
    fun valid(raw: String): Boolean = raw.split(',').let { it.size == defaults.size && it.toSet() == defaults.toSet() }
}

data class JournalSummary(
    val totalMillis: Long = 0,
    val attributed: List<PracticeSession> = emptyList(),
    val partsBySession: Map<Long, List<SessionPart>> = emptyMap(),
    val practiceTotals: Map<Long?, Long> = emptyMap(),
    val lastPractice: Map<Long?, Long> = emptyMap()
)
fun summarizeJournal(sessions: List<PracticeSession>, parts: List<SessionPart>): JournalSummary {
    val attributed = attributedSessions(sessions.filter(AchievementEngine::valid), parts)
    val groups = attributed.groupBy { it.pieceId }
    return JournalSummary(sessions.sumOf { it.durationMillis }, attributed, parts.groupBy { it.sessionId },
        groups.mapValues { (_, rows) -> rows.sumOf { it.durationMillis } },
        groups.mapValues { (_, rows) -> rows.maxOf { it.completedAt } })
}
