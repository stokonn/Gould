package com.opus.piano.data

import java.time.*

object PracticeInsights {
    fun metric(id:String,pieces:List<Piece>,sessions:List<PracticeSession>,today:LocalDate,zone:ZoneId):Long {
        val valid=sessions.filter(AchievementEngine::valid)
        return when(id) {
            "pieces" -> pieces.size.toLong()
            "sessions" -> valid.size.toLong()
            "streak" -> currentPracticeStreak(valid,today,zone).toLong()
            else -> {
                val from=when(id) { "week" -> today.minusDays((today.dayOfWeek.value-1).toLong());"month" -> today.withDayOfMonth(1);else -> today }
                valid.filter { AchievementEngine.recordedDay(it,zone) in from..today }.sumOf { it.durationMillis }
            }
        }
    }
    fun forPiece(sessions:List<PracticeSession>,parts:List<SessionPart>,pieceId:Long?,catalogId:String?,links:List<SessionPieceLink> = emptyList()):List<PracticeSession> {
        val measured=attributedSessions(sessions.filter(AchievementEngine::valid),parts).filter { (pieceId!=null && it.pieceId==pieceId)||(catalogId!=null && it.catalogWorkId==catalogId) }
            .groupBy {it.id}.map {(_,rows)->rows.first().copy(durationMillis=rows.sumOf {it.durationMillis})}
        val annotated=links.filter {(pieceId!=null && it.pieceId==pieceId)||(catalogId!=null && it.catalogWorkId==catalogId)}.mapNotNull {link->sessions.firstOrNull {it.id==link.sessionId}?.copy(durationMillis=0)}
        return (measured+annotated).distinctBy {it.id}.sortedWith(compareByDescending<PracticeSession> {it.completedAt}.thenByDescending {it.id})
    }
    fun recent(pieces:List<Piece>,sessions:List<PracticeSession>,parts:List<SessionPart>):List<Piece> {
        val times=attributedSessions(sessions.filter(AchievementEngine::valid),parts).filter { it.durationMillis>0 }.groupBy { it.pieceId }
            .mapValues { (_,v)->v.maxOf { it.completedAt } }
        return pieces.filter { it.id in times }.sortedWith(compareByDescending<Piece> { times[it.id] }.thenBy { it.id }).take(2)
    }
    fun sorted(pieces:List<Piece>,order:String,last:Map<Long?,Long>):List<Piece> = when(order) {
        "Composer" -> pieces.sortedWith(compareBy({catalogText(it.composer)},{catalogText(it.title)},{it.id}))
        "Recently added" -> pieces.sortedWith(compareByDescending<Piece> { it.createdAt }.thenByDescending { it.id })
        "Alphabetical" -> pieces.sortedWith(compareBy({catalogText(it.title)},{it.id}))
        else -> pieces.sortedWith(compareByDescending<Piece> { last[it.id] ?: Long.MIN_VALUE }.thenBy { catalogText(it.title) }.thenBy { it.id })
    }
}
