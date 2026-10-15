package com.opus.piano.data

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.time.ZoneId

/** Evaluation happens on relevant persisted mutations, never on timer ticks or application launches alone. */
class AchievementStore(private val db: OpusDatabase, private val now: () -> Long = System::currentTimeMillis, private val zone: () -> ZoneId = ZoneId::systemDefault) {
    private val base = combine(db.dao().observePieces(), db.dao().observeSessions(), db.catalogDao().composers(), db.catalogDao().works(), db.experienceDao().unlocks()) { pieces, sessions, composers, works, unlocks ->
        Pair(Triple(pieces, sessions, CatalogSnapshot(composers, works)), unlocks)
    }.distinctUntilChanged().flowOn(Dispatchers.Default)
    val badges = combine(base, db.personalDao().parts()) { data, parts ->
        val (pieces, sessions, catalog) = data.first
        AchievementEngine.evaluate(AchievementEngine.facts(pieces, sessions, catalog, zone(), parts), data.second)
    }.distinctUntilChanged().flowOn(Dispatchers.Default)
    suspend fun evaluate() = db.withTransaction { evaluateInTransaction() }

    /** Call inside the same Room transaction as a repertoire/session/catalogue mutation. */
    suspend fun evaluateInTransaction() {
        val pieces = db.dao().allPieces()
        val composers = db.catalogDao().allComposers()
        val works = db.catalogDao().allWorks()
        val catalog = CatalogSnapshot(composers, works)
        val workMap = works.associateBy { it.id }
        val pieceMap = pieces.associateBy { it.id }
        val composerMap = composers.associateBy { it.id }
        val evaluationZone = zone()
        val sessions = db.dao().allSessions().map { old ->
            val piece = pieceMap[old.pieceId]
            val workId = old.catalogWorkId ?: piece?.catalogWorkId
            val composerId = old.composerId ?: workId?.let { workMap[it]?.composerId } ?: piece?.let { ComposerIdentity.resolve(it, workMap) }
            val session = old.copy(
                practiceDay = old.practiceDay ?: AchievementEngine.recordedDay(old, evaluationZone).toString(),
                practiceZone = old.practiceZone ?: evaluationZone.id,
                catalogWorkId = workId,
                composerId = composerId,
                musicalEra = old.musicalEra.ifBlank { piece?.musicalEra.orEmpty() }.ifBlank { composerMap[composerId]?.epoch.orEmpty() }
            )
            if (session != old) db.dao().updateSession(session)
            session
        }
        val facts = AchievementEngine.facts(pieces, sessions, catalog, evaluationZone, db.personalDao().allParts())
        val newlyEarned = AchievementEngine.newlyEarned(facts, db.experienceDao().allUnlocks(), now())
        if (newlyEarned.isNotEmpty()) db.experienceDao().unlock(newlyEarned)
    }
}
