package com.opus.piano.data.catalogue

import androidx.room.withTransaction
import com.opus.piano.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Versioned optional-link evidence in the existing durable identity registry. Personal UUIDs
 * remain independent of catalogue UUIDs; portable v5 already preserves this evidence verbatim. */
class CatalogueRepertoire(private val personal:OpusDatabase) {
    data class Added(val pieceId:Long,val created:Boolean)
    suspend fun add(detail:DiscoverDetail,parts:List<DiscoverDetail> = emptyList(),separateEntries:Boolean=false):List<Added> = withContext(Dispatchers.IO) {
        require(detail.entity.kind in setOf("work","arrangement"))
        require(parts.size<=100 && parts.map {it.entity.id}.distinct().size==parts.size)
        parts.forEach {part->require(part.entity.kind=="work" && detail.relationships.any {it.first.type=="part" && it.first.toId==detail.entity.id && it.second.id==part.entity.id})}
        val selections=if(separateEntries && parts.isNotEmpty())parts else listOf(detail)
        personal.withTransaction {
            selections.map {selection->
                val identities=selection.identities.filter {it.reviewState=="reviewed"}
                require(identities.isNotEmpty()) {"Unverified catalogue candidate"}
                val keys=identities.map {"${it.provider}/${it.providerKind}/${it.externalId}"}
                val marker="catalogue-link-v1:"
                var existing:Long?=null
                personal.openHelper.readableDatabase.query("SELECT a.ownerId,i.evidence FROM personal_catalogue_anchors a JOIN personal_catalogue_identities i ON i.identityKey=a.identityKey JOIN pieces p ON CAST(p.id AS TEXT)=a.ownerId WHERE a.ownerType='piece' AND a.role='work' AND i.evidence LIKE ?",arrayOf("%$marker%")).use {c->while(c.moveToNext()){
                    val raw=c.getString(1).substringAfter(marker)
                    val saved=runCatching {JSONObject(raw)}.getOrNull() ?: continue
                    val providers=saved.optJSONArray("providers") ?: continue
                    if((0 until providers.length()).any {providers.optString(it) in keys})existing=c.getString(0).toLongOrNull()
                }}
                if(existing!=null)return@map Added(existing!!,false)
                val creator=selection.relationships.filter {it.first.type=="credit" && it.first.fromId==selection.entity.id}.joinToString(", "){it.second.title}
                val id=personal.dao().insertCatalogPiece(Piece(title=selection.entity.title,composer=creator,difficulty=Difficulty.UNRATED,createdAt=System.currentTimeMillis()))
                check(id>0)
                val anchor=personal.personalCatalogueDao().anchors("piece",id.toString()).single {it.role=="work"}
                val identity=requireNotNull(personal.personalCatalogueDao().identity(anchor.identityKey))
                val payload=JSONObject().put("version",1).put("catalogueId",selection.entity.id).put("kind",selection.entity.kind)
                    .put("providers",JSONArray(keys)).put("selectedParts",JSONArray(if(separateEntries)emptyList<String>() else parts.map {it.entity.id}))
                    .put("titleSnapshot",selection.entity.title).put("creatorSnapshot",creator)
                personal.openHelper.writableDatabase.execSQL("UPDATE personal_catalogue_identities SET evidence=? WHERE identityKey=?",arrayOf(identity.evidence+"\n"+marker+payload.toString(),identity.identityKey))
                Added(id,true)
            }
        }
    }
}
