package com.opus.piano.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class BadgeCategory(val label: String) { PRACTICE("Practice"), REPERTOIRE("Repertoire"), COMPOSERS("Composers") }
enum class BadgeRarity(val label: String) { COMMON("Common"), RARE("Rare"), EPIC("Epic"), LEGENDARY("Legendary") }
enum class MedalArt { NOTE, LYRE, CALENDAR, PIANO, ARCHIVE, CROWN, CHOPIN, BACH, MOZART, BAROQUE, MICROPHONE, PEN, EAR }
enum class AchievementMetric { DISCOVERY, SESSIONS, STREAK, PRACTICE_MILLIS, PIECES, MASTERED, PIANO_MASTERED, CHOPIN_PIECES, CHOPIN_MASTERED, BACH_PIECES, MOZART_PIECES, BAROQUE_PRACTICED, RECORDING_SAVED_COUNT, SESSION_NOTES_COUNT, VERSATILE_SESSIONS, PLAYBACK_COUNT, FOUNDING_PRACTICE }
data class AchievementDefinition(
    val id: String, val name: String, val description: String, val category: BadgeCategory,
    val rarity: BadgeRarity, val artwork: MedalArt, val metric: AchievementMetric,
    val target: Long, val requirement: String, val unit: String
)
object AchievementRegistry {
    val definitions = listOf(
        AchievementDefinition("curious_pianist", "The Curious Pianist", "Discover Bach’s Secret.", BadgeCategory.PRACTICE, BadgeRarity.RARE, MedalArt.BACH, AchievementMetric.DISCOVERY, 1, "Discover the hidden harpsichord in About.", "discoveries"),
        AchievementDefinition("first_notes", "First Notes", "The beginning of your musical journal.", BadgeCategory.PRACTICE, BadgeRarity.COMMON, MedalArt.NOTE, AchievementMetric.SESSIONS, 1, "Complete your first valid practice session.", "sessions"),
        AchievementDefinition("dedicated_musician", "Dedicated Musician", "Returning to the piano becomes a habit.", BadgeCategory.PRACTICE, BadgeRarity.COMMON, MedalArt.LYRE, AchievementMetric.SESSIONS, 10, "Complete 10 valid practice sessions.", "sessions"),
        AchievementDefinition("week_streak", "Week Streak", "A week with music in every day.", BadgeCategory.PRACTICE, BadgeRarity.RARE, MedalArt.CALENDAR, AchievementMetric.STREAK, 7, "Practice on 7 consecutive recorded local calendar days.", "consecutive days"),
        AchievementDefinition("month_streak", "Month Streak", "Thirty days of dedication.", BadgeCategory.PRACTICE, BadgeRarity.EPIC, MedalArt.CALENDAR, AchievementMetric.STREAK, 30, "Practice on 30 consecutive recorded local calendar days.", "consecutive days"),
        AchievementDefinition("century_club", "Century Club", "One hundred hours, one note at a time.", BadgeCategory.PRACTICE, BadgeRarity.EPIC, MedalArt.LYRE, AchievementMetric.PRACTICE_MILLIS, 360000000, "Accumulate 100 hours of recorded practice.", "hours"),
        AchievementDefinition("first_piece", "First Piece", "A composition to call your own.", BadgeCategory.REPERTOIRE, BadgeRarity.COMMON, MedalArt.ARCHIVE, AchievementMetric.PIECES, 1, "Have your first composition in repertoire.", "compositions"),
        AchievementDefinition("growing_collection", "Growing Collection", "Your personal archive takes shape.", BadgeCategory.REPERTOIRE, BadgeRarity.COMMON, MedalArt.ARCHIVE, AchievementMetric.PIECES, 10, "Have 10 compositions in repertoire.", "compositions"),
        AchievementDefinition("dedicated_collector", "Dedicated Collector", "An archive of musical possibilities.", BadgeCategory.REPERTOIRE, BadgeRarity.RARE, MedalArt.ARCHIVE, AchievementMetric.PIECES, 25, "Have 25 compositions in repertoire.", "compositions"),
        AchievementDefinition("grand_repertoire", "Grand Repertoire", "Fifty works, countless discoveries.", BadgeCategory.REPERTOIRE, BadgeRarity.EPIC, MedalArt.ARCHIVE, AchievementMetric.PIECES, 50, "Have 50 compositions in repertoire.", "compositions"),
        AchievementDefinition("the_maestro", "The Maestro", "A lifetime of music brought to readiness.", BadgeCategory.REPERTOIRE, BadgeRarity.LEGENDARY, MedalArt.CROWN, AchievementMetric.MASTERED, 100, "Mark 100 compositions Performance ready.", "mastered compositions"),
        AchievementDefinition("piano_virtuoso", "Piano Virtuoso", "Twenty-five works at your fingertips.", BadgeCategory.REPERTOIRE, BadgeRarity.EPIC, MedalArt.PIANO, AchievementMetric.PIANO_MASTERED, 25, "Mark 25 explicitly identified piano compositions Performance ready.", "mastered piano works"),
        AchievementDefinition("chopin_enthusiast", "Chopin Enthusiast", "Discover a poet of the piano.", BadgeCategory.COMPOSERS, BadgeRarity.RARE, MedalArt.CHOPIN, AchievementMetric.CHOPIN_PIECES, 5, "Have 5 compositions by Frédéric Chopin in repertoire.", "Chopin works"),
        AchievementDefinition("chopin_master", "Chopin Master", "A deep understanding of Chopin's world.", BadgeCategory.COMPOSERS, BadgeRarity.LEGENDARY, MedalArt.CHOPIN, AchievementMetric.CHOPIN_MASTERED, 25, "Mark 25 Chopin compositions Performance ready.", "mastered Chopin works"),
        AchievementDefinition("baroque_lover", "Baroque Lover", "Explore the voices of the Baroque.", BadgeCategory.COMPOSERS, BadgeRarity.RARE, MedalArt.BAROQUE, AchievementMetric.BAROQUE_PRACTICED, 5, "Complete valid practice on 5 distinct compositions identified as Baroque.", "distinct Baroque works"),
        AchievementDefinition("bach_explorer", "Bach Explorer", "Find new paths through counterpoint.", BadgeCategory.COMPOSERS, BadgeRarity.RARE, MedalArt.BACH, AchievementMetric.BACH_PIECES, 5, "Have 5 compositions by Johann Sebastian Bach in repertoire.", "Bach works"),
        AchievementDefinition("mozart_admirer", "Mozart Admirer", "Clarity, grace, and musical discovery.", BadgeCategory.COMPOSERS, BadgeRarity.RARE, MedalArt.MOZART, AchievementMetric.MOZART_PIECES, 5, "Have 5 compositions by Wolfgang Amadeus Mozart in repertoire.", "Mozart works"),
        // Gould 0.12 additions: genuine qualifying events only; no click awards.
        AchievementDefinition("first_take", "First Take", "Your first recording saved on this device.", BadgeCategory.PRACTICE, BadgeRarity.COMMON, MedalArt.MICROPHONE, AchievementMetric.RECORDING_SAVED_COUNT, 1, "Save one verified, nonempty recording.", "recordings"),
        AchievementDefinition("recording_artist", "Recording Artist", "A growing library of your own performances.", BadgeCategory.PRACTICE, BadgeRarity.RARE, MedalArt.MICROPHONE, AchievementMetric.RECORDING_SAVED_COUNT, 10, "Save 10 distinct verified recordings, each counted once.", "recordings"),
        AchievementDefinition("versatile_pianist", "Versatile Pianist", "Three voices in a single sitting.", BadgeCategory.PRACTICE, BadgeRarity.RARE, MedalArt.PIANO, AchievementMetric.VERSATILE_SESSIONS, 1, "Practice three distinct compositions in one valid session.", "sessions"),
        AchievementDefinition("musical_journal", "Musical Journal", "Words that shape your musical story.", BadgeCategory.PRACTICE, BadgeRarity.RARE, MedalArt.PEN, AchievementMetric.SESSION_NOTES_COUNT, 10, "Write 10 distinct practice notes, each counted once.", "notes"),
        AchievementDefinition("critical_listener", "Critical Listener", "Listening is its own discipline.", BadgeCategory.PRACTICE, BadgeRarity.RARE, MedalArt.EAR, AchievementMetric.PLAYBACK_COUNT, 1, "Listen through one of your saved recordings.", "listening sessions"),
        AchievementDefinition("founding_pianist", "Founding Pianist", "You were here before version 1.0.", BadgeCategory.PRACTICE, BadgeRarity.LEGENDARY, MedalArt.CROWN, AchievementMetric.FOUNDING_PRACTICE, 1, "Complete valid practice before the first stable release.", "founder")
    )
    init { require(definitions.map { it.id }.distinct().size == definitions.size) }
}

data class AchievementFacts(val values: Map<AchievementMetric, Long>) {
    operator fun get(metric: AchievementMetric): Long = values[metric] ?: 0
}
data class BadgeProgress(val definition: AchievementDefinition, val current: Long, val unlockedAt: Long? = null) {
    val fraction: Float get() = (current.toDouble() / definition.target).coerceIn(0.0, 1.0).toFloat()
    val unlocked: Boolean get() = unlockedAt != null
    val progressText: String get() = if (definition.metric == AchievementMetric.PRACTICE_MILLIS)
        "${"%.1f".format(current / 3600000.0)} / ${definition.target / 3600000} hours"
        else "$current / ${definition.target} ${definition.unit}"
}
object AchievementEngine {
    fun valid(session: PracticeSession): Boolean = session.durationMillis >= 1000 && session.completedAt > 0
    fun recordedDay(session: PracticeSession, legacyZone: ZoneId): LocalDate =
        session.practiceDay?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: Instant.ofEpochMilli(session.completedAt).atZone(legacyZone).toLocalDate()
    fun longestStreak(days: Collection<LocalDate>): Long {
        var last: LocalDate? = null
        var run = 0L
        var best = 0L
        days.distinct().sorted().forEach { day ->
            run = if (last?.plusDays(1) == day) run + 1 else 1
            best = maxOf(best, run); last = day
        }
        return best
    }
    fun facts(pieces: List<Piece>, sessions: List<PracticeSession>, catalog: CatalogSnapshot, legacyZone: ZoneId, parts: List<SessionPart> = emptyList(), studioEvents: List<AchievementEvent> = emptyList(), foundingVerifiedAt: Long = 0): AchievementFacts {
        val works = catalog.works.associateBy { it.id }
        val composers = catalog.composers.associateBy { it.id }
        val byId = pieces.associateBy { it.id }
        val valid = sessions.filter(::valid)
        val byComposer = pieces.groupBy { ComposerIdentity.resolve(it, works) }
        val baroque = attributedSessions(valid, parts).filter { it.durationMillis >= 1000 }.mapNotNull { s ->
            val piece = byId[s.pieceId]
            val workId = s.catalogWorkId ?: piece?.catalogWorkId
            val composerId = s.composerId ?: workId?.let { works[it]?.composerId } ?: piece?.let { ComposerIdentity.resolve(it, works) }
            val era = s.musicalEra.ifBlank { piece?.musicalEra.orEmpty() }.ifBlank { composers[composerId]?.epoch.orEmpty() }
            if (catalogText(era) != "baroque") null
            else workId?.let { "catalog:$it" } ?: s.pieceId?.let { "piece:$it" }
        }.toSet().size.toLong()
        return AchievementFacts(mapOf(
            AchievementMetric.SESSIONS to valid.size.toLong(),
            AchievementMetric.STREAK to longestStreak(valid.map { recordedDay(it, legacyZone) }),
            AchievementMetric.PRACTICE_MILLIS to valid.fold(0L) { total, s -> if (Long.MAX_VALUE - total < s.durationMillis) Long.MAX_VALUE else total + s.durationMillis },
            AchievementMetric.PIECES to pieces.size.toLong(),
            AchievementMetric.MASTERED to pieces.count(::mastered).toLong(),
            AchievementMetric.PIANO_MASTERED to pieces.count { mastered(it) && pianoComposition(it, works[it.catalogWorkId]) }.toLong(),
            AchievementMetric.CHOPIN_PIECES to byComposer[ComposerIdentity.CHOPIN].orEmpty().size.toLong(),
            AchievementMetric.CHOPIN_MASTERED to byComposer[ComposerIdentity.CHOPIN].orEmpty().count(::mastered).toLong(),
            AchievementMetric.BACH_PIECES to byComposer[ComposerIdentity.BACH].orEmpty().size.toLong(),
            AchievementMetric.MOZART_PIECES to byComposer[ComposerIdentity.MOZART].orEmpty().size.toLong(),
            AchievementMetric.BAROQUE_PRACTICED to baroque,
            // Gould 0.12: genuine qualifying events from studio data; no click awards.
            AchievementMetric.RECORDING_SAVED_COUNT to StudioAchievementRules.distinctRecordings(studioEvents),
            AchievementMetric.SESSION_NOTES_COUNT to distinctNotes(valid, parts),
            AchievementMetric.VERSATILE_SESSIONS to if (StudioAchievementRules.versatile(valid, parts)) 1 else 0,
            AchievementMetric.PLAYBACK_COUNT to studioEvents.count { it.kind == StudioAchievementRules.PLAYBACK_VERIFIED && it.value > 0 }.toLong(),
            AchievementMetric.FOUNDING_PRACTICE to if (foundingVerifiedAt > 0 && StudioAchievementRules.foundingPractice(valid, foundingVerifiedAt, FoundingPolicy.stableOneCutoffMillis)) 1 else 0
        ))
    }
    fun evaluate(facts: AchievementFacts, unlocked: List<BadgeUnlock>): List<BadgeProgress> {
        val dates = unlocked.associate { it.achievementId to it.unlockedAt }
        return AchievementRegistry.definitions.map { BadgeProgress(it, if (it.metric == AchievementMetric.DISCOVERY && dates[it.id] != null) 1 else facts[it.metric], dates[it.id]) }
    }
    fun newlyEarned(facts: AchievementFacts, unlocked: List<BadgeUnlock>, verifiedAt: Long): List<BadgeUnlock> =
        evaluate(facts, unlocked).filter { !it.unlocked && it.current >= it.definition.target }.map { BadgeUnlock(it.definition.id, verifiedAt) }
    /** Distinct nonblank notes from valid sessions/parts; repeated edits/whitespace do not count. */
    private fun distinctNotes(sessions: List<PracticeSession>, parts: List<SessionPart>): Long {
        val sessionNotes = sessions.filter(::valid).mapNotNull { s -> s.notes.trim().takeIf { it.isNotBlank() } }
        val partNotes = parts.filter { p -> p.notes.trim().isNotBlank() && sessions.any { it.id == p.sessionId && valid(it) } }.map { it.notes.trim() }
        return (sessionNotes + partNotes).distinct().size.toLong()
    }
}
