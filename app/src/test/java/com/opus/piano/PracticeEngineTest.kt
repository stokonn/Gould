package com.opus.piano

import com.opus.piano.data.*
import com.opus.piano.practice.*
import org.junit.Assert.*
import org.junit.Test

class PracticeEngineTest {
    @Test fun manualPausesAndSwitchesReconcileExactly() {
        val e = PracticeEngine(); e.begin(0,1000,"test",false)
        e.advance(1000); e.select(1500,Piece(7,"Bach","J. S. Bach")); e.advance(2500)
        e.pause(3000); e.advance(5000); e.begin(5000,1000,"ignored",false); e.review(6000)
        assertEquals(4000,e.state.elapsed); assertEquals(6000,e.state.wallElapsed)
        assertEquals(listOf(1500L,2500L),e.state.parts.map { it.durationMillis })
        assertEquals(e.state.elapsed,e.state.intervals.sumOf { it.creditedMillis })
    }
    @Test fun smartDoesNotCreditSilenceAndAutomaticallyResumes() {
        val e=PracticeEngine(); e.begin(0,1000,"test",true)
        for (t in 100L..1000L step 100) e.evidence(t,true)
        val first=e.state.elapsed; assertTrue(first>0)
        for (t in 1100L..8000L step 100) e.evidence(t,false)
        assertEquals("Automatically paused",e.state.status)
        assertTrue(e.state.elapsed-first <= 100)
        for (t in 8100L..9000L step 100) e.evidence(t,true)
        e.review(9100)
        assertTrue(e.state.elapsed>first)
        assertEquals(e.state.elapsed,e.state.detectedActive)
        assertEquals(e.state.elapsed,e.state.parts.sumOf { it.durationMillis })
    }
    @Test fun missingEvidenceNeverCreditsAStall() {
        val e=PracticeEngine();e.begin(0,1,"test",true)
        for (t in 100L..1000L step 100) e.evidence(t,true)
        val credited=e.state.elapsed;e.advance(60000)
        assertEquals(credited,e.state.elapsed)
    }
    @Test fun mixedConversionRetainsManualCreditAndReviewFreezes() {
        val e=PracticeEngine(); e.begin(0,1,"test",false);e.pause(1000);e.begin(2000,1,"test",true)
        for (t in 2100L..3000L step 100) e.evidence(t,true)
        e.review(3100); val s=e.state;e.advance(9000)
        assertEquals("Mixed",s.mode);assertEquals(s.elapsed,e.state.elapsed);assertEquals(s.wallElapsed,e.state.wallElapsed)
        assertEquals(1000,s.elapsed-s.detectedActive)
    }
    @Test fun notesOnlyDoesNotEarnMilestones() {
        assertFalse(AchievementEngine.valid(PracticeSession(pieceId=null,pieceTitle="Notes",startedAt=1,completedAt=2,durationMillis=0)))
        assertTrue(AchievementEngine.newlyEarned(AchievementFacts(emptyMap()), emptyList(),123).none { it.achievementId=="curious_pianist" })
        assertTrue(AchievementEngine.evaluate(AchievementFacts(emptyMap()),listOf(BadgeUnlock("curious_pianist",123))).first { it.definition.id=="curious_pianist" }.unlocked)
    }
    @Test fun everyResumeChangesExecutionWithoutChangingSessionIdentity() {
        val e=PracticeEngine();e.begin(0,1,"first",true);val generation=e.state.execution
        e.pause(1000);e.begin(2000,1,"ignored",false)
        assertEquals("first",e.state.token);assertTrue(e.state.execution>generation)
    }
    @Test fun shortGapsAreBridgedButLongRestsAreExcluded() {
        val e=PracticeEngine();e.begin(0,1,"test",true)
        for (t in 100L..1000L step 100) e.evidence(t,true)
        e.evidence(1100,false)
        for (t in 1200L..1700L step 100) e.evidence(t,true)
        assertTrue(e.state.intervals.none { it.reason=="Silence" && it.startMillis>=1000 })
        val before=e.state.elapsed
        for (t in 1800L..6000L step 100) e.evidence(t,false)
        assertTrue(e.state.elapsed-before<=100)
    }
    @Test fun timingValidationRejectsMisleadingTotals() {
        val bad=listOf(SessionInterval(partIndex=0,startMillis=0,endMillis=1000,creditedMillis=1000,reason="Detected"))
        assertThrows(IllegalArgumentException::class.java) { PracticeRepository.validateTiming(500,1000,500,"Smart",bad,1) }
    }
}
