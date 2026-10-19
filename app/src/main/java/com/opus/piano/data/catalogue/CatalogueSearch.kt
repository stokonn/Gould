package com.opus.piano.data.catalogue

import androidx.sqlite.db.SimpleSQLiteQuery
import java.util.Base64
import java.security.MessageDigest

object SearchText {
    fun normalize(value:String)=CatalogueValidation.normalized(value.take(256)).replace(Regex("([a-z])([0-9])"),"$1 $2")
    /** One edit only, no fuzzy equivalence for identity. */
    fun near(a:String,b:String):Boolean {
        if(kotlin.math.abs(a.length-b.length)>1)return false
        var i=0;var j=0;var changes=0
        while(i<a.length && j<b.length){if(a[i]==b[j]){i++;j++}else{if(++changes>1)return false;if(a.length>=b.length)i++;if(b.length>=a.length)j++}}
        return changes+(a.length-i)+(b.length-j)<=1
    }
    fun hash(value:String)=MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString(""){"%02x".format(it)}
}
data class SearchFilter(val category:String="all",val artistId:String?=null,val period:String?=null) {
    init {require(category in setOf("all","works","artists","albums","lists"));artistId?.let {CatalogueValidation.uuid(it)};require(period==null || period.length<=80)}
    val kinds get()=when(category){"works"->listOf("work","arrangement");"artists"->listOf("artist");"albums"->listOf("album");"lists"->listOf("collection");else->listOf("work","arrangement","artist","album","collection")}
}
data class SearchCursor(val queryHash:String,val generation:String,val lastRank:Int,val lastId:String) {
    fun encode()=Base64.getUrlEncoder().withoutPadding().encodeToString(listOf("2",queryHash,generation,"relevance-id-v2",lastRank.toString(),lastId).joinToString("|").toByteArray())
    companion object {
        fun decode(value:String,hash:String,generation:String):SearchCursor {
            require(value.length<=1024)
            val parts=String(Base64.getUrlDecoder().decode(value),Charsets.UTF_8).split('|')
            require(parts.size==6 && parts[0]=="2" && parts[1]==hash && parts[2]==generation && parts[3]=="relevance-id-v2") {"Stale or incompatible search cursor"}
            CatalogueValidation.uuid(parts[5]);return SearchCursor(hash,generation,parts[4].toInt().also {require(it in 0..3)},parts[5])
        }
    }
}
data class CatalogueSearchPage(val items:List<CatalogueEntity>,val nextCursor:String?,val generation:String)

internal suspend fun CatalogueRepository.searchPage(query:String,filter:SearchFilter=SearchFilter(),cursor:String?=null,limit:Int=40,excluded:Set<String> = emptySet()):CatalogueSearchPage {
    require(query.length<=256 && limit in 1..40 && excluded.size<=500)
    excluded.forEach {CatalogueValidation.uuid(it)}
    val normalized=SearchText.normalize(query);val generation=generation()
    val binding=SearchText.hash("$normalized|${filter.category}|${filter.artistId}|${filter.period}")
    val after=cursor?.let {SearchCursor.decode(it,binding,generation)}
    val args=mutableListOf<Any>()
    val placeholders=filter.kinds.joinToString(","){"?"}
    // Rank computed in SQLite; bounded aliases/FTS never materialize the catalogue in memory.
    val rank="CASE WHEN EXISTS(SELECT 1 FROM aliases ax WHERE ax.entityId=e.id AND ax.normalized=?) THEN 0 WHEN EXISTS(SELECT 1 FROM aliases ax WHERE ax.entityId=e.id AND ax.normalized LIKE ? ESCAPE '\\') THEN 1 ELSE 2 END"
    args+=normalized;args+="$normalized%"
    var where="e.status='active' AND e.kind IN ($placeholders)"
    where+=" AND NOT EXISTS(SELECT 1 FROM redirect_revisions rr WHERE rr.sourceId=e.id AND rr.targetId IS NOT NULL AND rr.revision=(SELECT MAX(latest.revision) FROM redirect_revisions latest WHERE latest.sourceId=e.id))"
    args.addAll(filter.kinds)
    if(excluded.isNotEmpty()){where+=" AND e.id NOT IN (${excluded.joinToString(","){"?"}})";args.addAll(excluded.sorted())}
    if(normalized.isNotBlank()) {
        val matches=normalized.split(' ').filter {it.isNotBlank()}.take(8).map {token->
            val alternatives=if(token.length in 4..40)aliasPrefix(token.take(2)).flatMap {it.split(' ')}.distinct().filter {it!=token && SearchText.near(token,it)}.sorted().take(3) else emptyList()
            (listOf(token)+alternatives).joinToString(" OR "){CatalogueValidation.query(it)}
        }.distinct().map {it to postingCount(it)}.sortedBy {it.second}.map {it.first}
        for((index,match) in matches.withIndex()) {
            // Anchor on the smallest FTS postings list. Remaining terms intersect alias rowids
            // for that entity, preserving matches across different aliases and artist credits.
            where+=if(index==0)" AND e.id IN (SELECT a.entityId FROM alias_fts JOIN aliases a ON a.rowid=alias_fts.rowid WHERE alias_fts MATCH ?)"
                else " AND EXISTS(SELECT 1 FROM aliases a WHERE a.entityId=e.id AND a.rowid IN (SELECT rowid FROM alias_fts WHERE alias_fts MATCH ?))"
            args+=match
        }
    }
    if(filter.artistId!=null){where+=" AND e.id IN (SELECT fromId FROM relationships WHERE type='credit' AND toId=?)";args+=filter.artistId}
    if(filter.period!=null) {
        // Open Opus supplies a composer's period, so include explicitly credited works too.
        // Match a complete projected field value, not a substring of another period label.
        val fragment="\"value\":"+org.json.JSONObject.quote(filter.period)
        val pattern="%"+fragment.replace("\\","\\\\").replace("%","\\%").replace("_","\\_")+"%"
        where+=" AND (e.id IN (SELECT entityId FROM assertions WHERE field='epoch' AND selected=1 AND valueJson LIKE ? ESCAPE '\\') OR e.id IN (SELECT r.fromId FROM relationships r JOIN assertions a ON a.entityId=r.toId WHERE r.type='credit' AND a.field='epoch' AND a.selected=1 AND a.valueJson LIKE ? ESCAPE '\\'))"
        args+=pattern;args+=pattern
    }
    // For a token query the anchor provides IDs; do not scan every entity of each broad kind.
    val index=if(normalized.isNotBlank())" INDEXED BY sqlite_autoindex_entities_1" else ""
    var sql="SELECT * FROM (SELECT e.*, $rank AS searchRank FROM entities e$index WHERE $where)"
    if(after!=null){sql+=" WHERE searchRank > ? OR (searchRank=? AND id>?)";args+=after.lastRank;args+=after.lastRank;args+=after.lastId}
    sql+=" ORDER BY searchRank,id LIMIT ?";args+=limit+1
    val rows=queryEntities(SimpleSQLiteQuery(sql,args.toTypedArray()))
    val page=rows.take(limit)
    val next=if(rows.size>limit) {
        val last=page.last()
        // Same ranking predicate as SQL, queried only for the final item.
        val exact=queryEntities(SimpleSQLiteQuery("SELECT e.* FROM entities e JOIN aliases a ON a.entityId=e.id WHERE e.id=? AND a.normalized=? LIMIT 1",arrayOf(last.id,normalized))).isNotEmpty()
        val prefix=exact || queryEntities(SimpleSQLiteQuery("SELECT e.* FROM entities e JOIN aliases a ON a.entityId=e.id WHERE e.id=? AND a.normalized LIKE ? LIMIT 1",arrayOf(last.id,"$normalized%"))).isNotEmpty()
        SearchCursor(binding,generation,if(exact)0 else if(prefix)1 else 2,last.id).encode()
    } else null
    return CatalogueSearchPage(page,next,generation)
}
