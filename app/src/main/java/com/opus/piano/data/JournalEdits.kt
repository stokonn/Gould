package com.opus.piano.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/** No duration column by design: an after-the-fact association is not measured time. */
@Entity(tableName="session_piece_links",primaryKeys=["sessionId","pieceId"],foreignKeys=[ForeignKey(entity=PracticeSession::class,parentColumns=["id"],childColumns=["sessionId"],onDelete=ForeignKey.CASCADE)])
data class SessionPieceLink(val sessionId:Long,val pieceId:Long,val title:String,val composer:String,val catalogWorkId:String?,val composerId:String?,val musicalEra:String,val addedAt:Long)
@Entity(tableName="favorite_composers")
data class FavoriteComposer(@PrimaryKey val composerId:String,val addedAt:Long)
@Dao interface JournalEditsDao {
    @Query("SELECT * FROM session_piece_links ORDER BY addedAt,pieceId") suspend fun allLinks():List<SessionPieceLink>
    @Query("SELECT * FROM session_piece_links WHERE sessionId=:id ORDER BY addedAt,pieceId") suspend fun links(id:Long):List<SessionPieceLink>
    @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun insertLink(link:SessionPieceLink)
    @Query("DELETE FROM session_piece_links WHERE sessionId=:sessionId AND pieceId=:pieceId") suspend fun removeLink(sessionId:Long,pieceId:Long)
    @Query("SELECT * FROM favorite_composers ORDER BY addedAt,composerId") fun favorites():Flow<List<FavoriteComposer>>
    @Query("SELECT * FROM favorite_composers ORDER BY addedAt,composerId") suspend fun allFavorites():List<FavoriteComposer>
    @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun insertFavorite(value:FavoriteComposer)
    @Query("DELETE FROM favorite_composers WHERE composerId=:id") suspend fun removeFavorite(id:String)
}
class JournalEdits(private val db:OpusDatabase) {
    suspend fun favorite(id:String,selected:Boolean)=db.withTransaction {
        requireNotNull(db.catalogDao().composer(id))
        if(selected) db.journalEditsDao().insertFavorite(FavoriteComposer(id,System.currentTimeMillis())) else db.journalEditsDao().removeFavorite(id)
    }
    suspend fun addPiece(sessionId:Long,pieceId:Long)=db.withTransaction {
        val session=requireNotNull(db.practiceDao().session(sessionId))
        val piece=requireNotNull(db.dao().piece(pieceId))
        val measured=db.practiceDao().historyParts(listOf(sessionId))
        require(measured.none {it.pieceId==pieceId || (piece.catalogWorkId!=null && it.catalogWorkId==piece.catalogWorkId)})
        require(session.pieceId!=pieceId && (piece.catalogWorkId==null || session.catalogWorkId!=piece.catalogWorkId))
        val existing=db.journalEditsDao().links(sessionId)
        require(existing.none {it.pieceId!=pieceId && piece.catalogWorkId!=null && it.catalogWorkId==piece.catalogWorkId})
        val work=piece.catalogWorkId?.let {db.catalogDao().work(it)}
        db.journalEditsDao().insertLink(SessionPieceLink(sessionId,pieceId,piece.title,piece.composer,piece.catalogWorkId,work?.composerId,piece.musicalEra,System.currentTimeMillis()))
    }
    suspend fun removePiece(sessionId:Long,pieceId:Long)=db.withTransaction {
        requireNotNull(db.practiceDao().session(sessionId));db.journalEditsDao().removeLink(sessionId,pieceId)
    }
}
/** Local-only, accent-insensitive composer matching. No network discovery. */
fun composerSuggestions(composers:List<CatalogComposer>,query:String):List<CatalogComposer> {
    val tokens=catalogText(query).split(' ').filter {it.isNotBlank()}
    if(tokens.isEmpty()) return emptyList()
    return composers.filter {c->tokens.all {catalogText(c.name).contains(it)}}.sortedBy {catalogText(it.name)}.take(8)
}

/** Untimed annotations live in the existing session note; never truncate existing or new text. */
fun appendPieceAnnotation(original:String,title:String?,annotation:String):String {
    if(title==null || annotation.isBlank()) return original
    val combined=listOf(original.takeIf {it.isNotEmpty()}, "$title:\n$annotation").filterNotNull().joinToString("\n\n")
    require(combined.length<=10000) {"Session note exceeds 10000 characters"}
    return combined
}
