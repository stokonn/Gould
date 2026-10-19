package com.opus.piano.data.catalogue

import com.opus.piano.data.catalogue.providers.*
import org.json.JSONObject
import java.util.UUID

/** Pending edges wait for typed endpoints. They never manufacture an accepted work/arrangement. */
internal class CatalogueEdges(private val db:CatalogueDatabase) {
    private val dao=db.dao()
    suspend fun ingest(result:ProviderResult) {
        val repo=CatalogueRepository(db)
        for((record,edge) in result.records.flatMap {record->record.relationships.map {record to it}}.take(100)) {
            fun ref(r:ProviderReference)=JSONObject().put("providerKind",r.providerKind).put("externalId",r.externalId)
            val value=JSONObject().put("provider",result.request.provider).put("from",ref(edge.from)).put("to",ref(edge.to)).put("type",edge.type).put("position",edge.position).put("medium",edge.medium).put("role",edge.role)
                .put("evidenceHash",result.hash)
            dao.sourceObservation(result.request.provider,record.providerKind+":"+record.externalId,result.revision+"|sha256="+result.hash)?.let {value.put("sourceId",it.id)}
            val key="pending-edge:"+SearchText.hash(value.toString())
            value.put("observedAt",result.observedAt)
            repo.boundedState("pending-edge:",key,value.toString())
        }
        for(pending in repo.states("pending-edge:",200)) {
            val o=JSONObject(pending.value);val provider=o.getString("provider")
            val from=o.getJSONObject("from");val to=o.getJSONObject("to")
            val fromIdentity=dao.identity(provider,from.getString("providerKind"),from.getString("externalId")) ?: continue
            val toIdentity=dao.identity(provider,to.getString("providerKind"),to.getString("externalId")) ?: continue
            val a=dao.entity(fromIdentity.entityId) ?: continue;val b=dao.entity(toIdentity.entityId) ?: continue
            if(dao.hasEdge(a.id,b.id,o.getString("type"))){repo.removeState(pending.key);continue}
            val type=o.getString("type")
            val previousWorkType=if(type=="part")dao.workType(a.id) else null
            if(type=="part") {
                // Only a parser with explicit structural evidence creates this edge.
                db.openHelper.writableDatabase.execSQL("UPDATE works SET workType='part' WHERE id=? AND workType='composition'",arrayOf(a.id))
            }
            try {
                db.openHelper.writableDatabase.execSQL("INSERT INTO relationships(id,type,fromId,fromKind,toId,toKind,position,medium,role,creditedAs,sourceId) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                    arrayOf<Any?>(UUID.randomUUID().toString(),type,a.id,a.kind,b.id,b.kind,o.getInt("position"),o.getInt("medium"),o.getString("role"),"",o.optString("sourceId").takeIf {it.isNotBlank()} ?: fromIdentity.sourceId))
                repo.removeState(pending.key)
            } catch(_:android.database.sqlite.SQLiteConstraintException) {
                // A rejected relation cannot leave behind its tentative subtype promotion.
                if(previousWorkType=="composition")db.openHelper.writableDatabase.execSQL("UPDATE works SET workType='composition' WHERE id=? AND workType='part'",arrayOf(a.id))
                // Invalid/cyclic/competing assertions remain exportable diagnostics, not accepted edges.
                repo.boundedState("diagnostic:","diagnostic:"+pending.key,o.put("state","quarantined_relationship").toString());repo.removeState(pending.key)
            }
        }
    }
}
