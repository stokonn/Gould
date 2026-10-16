package com.opus.piano

import com.opus.piano.data.*
import com.opus.piano.preferences.*
import com.opus.piano.ui.selectedTab
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class RedesignTest {
    private val today=LocalDate.of(2026,10,16)
    private val zone=ZoneId.of("Europe/Prague")
    private fun session(id:Long,day:LocalDate,time:Long)=PracticeSession(id,null,"Study",1,day.atStartOfDay(zone).toInstant().toEpochMilli(),time,day.toString(),zone.id)
    @Test fun homeMetricsUseCreditedTimeAndIgnoreNoteOnlyEntries() {
        val sessions=listOf(session(1,today,60000),session(2,today.minusDays(1),120000),session(3,today,0))
        assertEquals(60000,PracticeInsights.metric("today",emptyList(),sessions,today,zone))
        assertEquals(180000,PracticeInsights.metric("week",emptyList(),sessions,today,zone))
        assertEquals(2,PracticeInsights.metric("sessions",emptyList(),sessions,today,zone))
        assertEquals(2,PracticeInsights.metric("streak",emptyList(),sessions,today,zone))
        assertEquals(0,PracticeInsights.metric("today",emptyList(),emptyList(),today,zone))
    }
    @Test fun samePieceMultipleEntriesCountOneSessionAndReconcile() {
        val sessions=listOf(session(1,today,60000),session(2,today.minusDays(1),30000))
        val parts=listOf(SessionPart(id=1,sessionId=1,pieceId=7,durationMillis=20000),SessionPart(id=2,sessionId=1,pieceId=7,durationMillis=30000),SessionPart(id=3,sessionId=1,pieceId=8,durationMillis=10000),SessionPart(id=4,sessionId=2,pieceId=7,durationMillis=30000))
        val result=PracticeInsights.forPiece(sessions,parts,7,null)
        assertEquals(2,result.size);assertEquals(80000,result.sumOf { it.durationMillis })
        val recent=PracticeInsights.recent(listOf(Piece(7,"A","B"),Piece(8,"B","B")),sessions,parts)
        assertEquals(listOf(7L,8L),recent.map { it.id })
    }
    @Test fun presentationNormalizationKeepsExactlyTwoDistinctMetricsAndSafeOrder() {
        val a=AppearanceRules.normalize(Appearance(metrics=listOf("bogus","today","today"),homeSections=listOf("recent","recent","invalid"),profileSections=emptyList(),accent="invalid"))
        assertEquals(2,a.metrics.size);assertEquals(2,a.metrics.distinct().size)
        assertEquals(listOf("recent","statistics"),a.homeSections)
        assertEquals(AppearanceRules.profile,a.profileSections);assertEquals("Gold",a.accent)
        assertEquals(listOf("statistics","recent"),AppearanceRules.move(a.homeSections,"recent",1))
    }
    @Test fun repertoireDefaultsToRecentlyPracticedAndNeverPracticedIsDeterministic() {
        val pieces=listOf(Piece(3,"Z","B"),Piece(2,"A","B"),Piece(1,"B","B"))
        assertEquals("Recently practiced",Appearance().sort)
        assertEquals(listOf(3L,2L,1L),PracticeInsights.sorted(pieces,Appearance().sort,mapOf(3L to 123)).map { it.id })
        assertEquals(listOf(2L,1L,3L),PracticeInsights.sorted(pieces,"Alphabetical",emptyMap()).map { it.id })
    }
    @Test fun badgesAndHistoryBelongToProfileWithoutLosingRoutes() {
        listOf("BADGES","badge/curious_pianist","history","history/piece/7").forEach { assertEquals("HOME",selectedTab(it)) }
        assertEquals("REPERTOIRE",selectedTab("catalog/work/12"))
    }
}
