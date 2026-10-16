package com.opus.piano

import com.opus.piano.data.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class AchievementEngineTest {
    private val utc = ZoneId.of("UTC")
    private val empty = CatalogSnapshot(emptyList(), emptyList())
    private fun session(day: String, duration: Long = 1000, pieceId: Long? = null, id: Long = 1) = PracticeSession(id, pieceId, "Fixture", 1, LocalDate.parse(day).atTime(12, 0).toInstant(ZoneOffset.UTC).toEpochMilli(), duration, day, "UTC")
    private fun facts(pieces: List<Piece> = emptyList(), sessions: List<PracticeSession> = emptyList(), catalog: CatalogSnapshot = empty) = AchievementEngine.facts(pieces, sessions, catalog, utc)
    private fun eligible(facts: AchievementFacts) = AchievementEngine.newlyEarned(facts, emptyList(), 123456).map { it.achievementId }.toSet()

    @Test fun appLaunchAndSubsecondSessionsDoNotUnlockPracticeBadges() {
        assertTrue(eligible(facts()).isEmpty())
        assertFalse("first_notes" in eligible(facts(sessions = listOf(session("2026-01-01", 999)))))
        assertFalse("first_notes" in eligible(facts(sessions = listOf(session("2026-01-01").copy(completedAt = 0)))))
        assertTrue("first_notes" in eligible(facts(sessions = listOf(session("2026-01-01")))))
    }
    @Test fun tenSessionThresholdAndDurationProgressUseOnlyValidSessions() {
        val nine = (1..9).map { session("2026-01-01", id = it.toLong()) }
        assertFalse("dedicated_musician" in eligible(facts(sessions = nine)))
        val ten = facts(sessions = nine + session("2026-01-01", id = 10) + session("2026-01-02", 999))
        assertEquals(10L, ten[AchievementMetric.SESSIONS])
        assertEquals(10000L, ten[AchievementMetric.PRACTICE_MILLIS])
        assertEquals(1L, ten[AchievementMetric.STREAK])
        assertTrue("dedicated_musician" in eligible(ten))
    }
    @Test fun allDefinedMilestonesCanBeEarnedFromRealFacts() {
        val pieces = (1..100).map { id -> Piece(id.toLong(), "Fixture $id", "Display name", status = LearningStatus.READY, catalogWorkId = "$id", instrument = if (id <= 25) "Piano" else "") }
        val works = pieces.map { p -> CatalogWork(p.id.toString(), when (p.id) { in 1..25 -> ComposerIdentity.CHOPIN; in 26..30 -> ComposerIdentity.BACH; in 31..35 -> ComposerIdentity.MOZART; else -> "999" }, p.title) }
        val composers = listOf(CatalogComposer(ComposerIdentity.CHOPIN, "Fixture Chopin", epoch = "Romantic"), CatalogComposer(ComposerIdentity.BACH, "Fixture Bach", epoch = "Baroque"), CatalogComposer(ComposerIdentity.MOZART, "Fixture Mozart", epoch = "Classical"))
        val sessions = (0..29).map { i -> session(LocalDate.of(2026, 1, 1).plusDays(i.toLong()).toString(), if (i == 0) 358260000 else 60000, if (i < 5) 26L + i else 1L, i.toLong()) }
        val measured = facts(pieces, sessions, CatalogSnapshot(composers, works))
        assertEquals(360000000L, measured[AchievementMetric.PRACTICE_MILLIS])
        assertEquals(30L, measured[AchievementMetric.STREAK])
        assertEquals(25L, measured[AchievementMetric.PIANO_MASTERED])
        assertEquals(5L, measured[AchievementMetric.BAROQUE_PRACTICED])
        assertEquals(AchievementRegistry.definitions.filter { it.metric != AchievementMetric.DISCOVERY }.map { it.id }.toSet(), eligible(measured))
    }
    @Test fun collectionThresholdsAreExactAndMasteryUsesExistingReadyStatus() {
        listOf(1 to "first_piece", 10 to "growing_collection", 25 to "dedicated_collector", 50 to "grand_repertoire").forEach { (target, badge) ->
            fun pieces(n: Int) = (1..n).map { Piece(it.toLong(), "Fixture $it", "Composer", status = LearningStatus.POLISHING) }
            assertFalse(badge in eligible(facts(pieces(target - 1))))
            assertTrue(badge in eligible(facts(pieces(target))))
            assertEquals(0L, facts(pieces(target))[AchievementMetric.MASTERED])
        }
        assertTrue(mastered(Piece(title = "Fixture", composer = "Composer", status = LearningStatus.READY)))
        assertFalse(mastered(Piece(title = "Fixture", composer = "Composer", status = LearningStatus.POLISHING)))
    }
    @Test fun keyboardGenreDoesNotFabricatePianoInstrumentation() {
        val p = Piece(title = "Suite", composer = "Composer", status = LearningStatus.READY, catalogWorkId = "1")
        val work = CatalogWork("1", "87", "Suite", genre = "Keyboard")
        assertFalse(pianoComposition(p, work))
        assertTrue(pianoComposition(p.copy(instrument = "piano"), work))
        assertTrue(pianoComposition(p, work.copy(subtitle = "For piano and violin")))
        assertEquals(0L, facts(listOf(p), catalog = CatalogSnapshot(emptyList(), listOf(work)))[AchievementMetric.PIANO_MASTERED])
    }
    @Test fun stableComposerIdWinsOverAnEditedDisplayName() {
        val p = Piece(title = "Fixture", composer = "Wolfgang Amadeus Mozart", catalogWorkId = "1")
        assertEquals(ComposerIdentity.CHOPIN, ComposerIdentity.resolve(p, mapOf("1" to CatalogWork("1", ComposerIdentity.CHOPIN, "Fixture"))))
        assertEquals(ComposerIdentity.CHOPIN, ComposerIdentity.resolve(p.copy(catalogWorkId = null, composer = "Frederic Chopin"), emptyMap()))
        assertNull(ComposerIdentity.resolve(p.copy(catalogWorkId = null, composer = "Mozart"), emptyMap()))
        assertNull(ComposerIdentity.resolve(p.copy(catalogWorkId = null, composer = "Bach"), emptyMap()))
    }
    @Test fun baroquePracticeCountsDistinctWorksAndSurvivesRepertoireDeletion() {
        val recorded = (1..5).map { i -> session("2026-01-01", pieceId = i.toLong(), id = i.toLong()).copy(catalogWorkId = "$i", musicalEra = "Baroque", composerId = ComposerIdentity.BACH) }
        val measured = facts(sessions = recorded + recorded + session("2026-01-01").copy(musicalEra = "Baroque"))
        assertEquals(5L, measured[AchievementMetric.BAROQUE_PRACTICED])
        assertTrue("baroque_lover" in eligible(measured))
    }
    @Test fun repeatedEvaluationKeepsTheFirstVerificationTimestampAndNeverRevokes() {
        val measured = facts(listOf(Piece(title = "Fixture", composer = "Composer")))
        val first = AchievementEngine.newlyEarned(measured, emptyList(), 1000)
        assertEquals(listOf(BadgeUnlock("first_piece", 1000)), first)
        assertTrue(AchievementEngine.newlyEarned(measured, first, 9999).isEmpty())
        val afterDeletion = AchievementEngine.evaluate(facts(), first).first { it.definition.id == "first_piece" }
        assertTrue(afterDeletion.unlocked)
        assertEquals(1000L, afterDeletion.unlockedAt)
        assertEquals(0L, afterDeletion.current)
    }
    @Test fun weekAndMonthNeedConsecutiveCalendarDaysAndSkipMissingDays() {
        val start = LocalDate.of(2026, 1, 1)
        val six = (0..5).map { session(start.plusDays(it.toLong()).toString()) }
        assertFalse("week_streak" in eligible(facts(sessions = six)))
        assertTrue("week_streak" in eligible(facts(sessions = six + session("2026-01-07"))))
        assertFalse("week_streak" in eligible(facts(sessions = six + session("2026-01-08"))))
        assertEquals(2L, AchievementEngine.longestStreak(listOf(start, start, start.plusDays(1), start.plusDays(3))))
        val thirty = (0..29).map { session(start.plusDays(it.toLong()).toString()) }
        assertTrue("month_streak" in eligible(facts(sessions = thirty)))
        assertFalse("month_streak" in eligible(facts(sessions = thirty.dropLast(1))))
    }
    @Test fun midnightUsesTheCompletionDayAndTimeZoneChangesDoNotRewriteIt() {
        val before = session("2026-02-01").copy(completedAt = Instant.parse("2026-02-01T23:59:59Z").toEpochMilli(), practiceDay = null, practiceZone = null)
        val after = before.copy(completedAt = Instant.parse("2026-02-02T00:00:01Z").toEpochMilli())
        assertEquals(LocalDate.of(2026, 2, 1), AchievementEngine.recordedDay(before, utc))
        assertEquals(LocalDate.of(2026, 2, 2), AchievementEngine.recordedDay(after, utc))
        val frozen = after.copy(practiceDay = "2026-02-02", practiceZone = "UTC")
        assertEquals(AchievementEngine.recordedDay(frozen, utc), AchievementEngine.recordedDay(frozen, ZoneId.of("America/Los_Angeles")))
        assertEquals(2L, facts(sessions = listOf(before, after))[AchievementMetric.STREAK])
    }
    @Test fun daylightSavingUsesDatesRatherThanElapsed24HourWindows() {
        val prague = ZoneId.of("Europe/Prague")
        fun fromLocal(text: String) = session("2026-01-01").copy(practiceDay = null, completedAt = LocalDateTime.parse(text).atZone(prague).toInstant().toEpochMilli())
        val spring = listOf(fromLocal("2026-03-28T12:00:00"), fromLocal("2026-03-29T12:00:00"), fromLocal("2026-03-30T12:00:00"))
        assertEquals(23 * 3600000L, spring[1].completedAt - spring[0].completedAt)
        assertEquals(3L, AchievementEngine.longestStreak(spring.map { AchievementEngine.recordedDay(it, prague) }))
        val fall = listOf(fromLocal("2026-10-24T12:00:00"), fromLocal("2026-10-25T12:00:00"))
        assertEquals(25 * 3600000L, fall[1].completedAt - fall[0].completedAt)
        assertEquals(2L, AchievementEngine.longestStreak(fall.map { AchievementEngine.recordedDay(it, prague) }))
    }
}
