package com.opus.piano.data.catalogue

import androidx.room.withTransaction

/** Catalogue-only repository. No personal or practice writes. */
class CatalogueRepository(private val db: CatalogueDatabase) {
    private val dao = db.dao()
    internal suspend fun importLegacyResponse(marker:String,results:List<com.opus.piano.data.catalogue.providers.ProviderResult>)=db.withTransaction {
        if(dao.state(marker)==null){results.forEach {CatalogueIngestion(db).accept(it)};dao.state(CatalogueState(marker,"ready"))}
    }
    /** Retained metadata without its original response is not new licence evidence. */
    internal suspend fun importLegacySnapshot(kind:String,row:org.json.JSONObject)=db.withTransaction {
        require(kind in setOf("artist","work"))
        val external=row.getString("id");val title=row.getString(if(kind=="artist")"name" else "title")
        require(external.isNotBlank() && title.isNotBlank())
        val payload=row.toString();val digest=SearchText.hash(payload)
        val marker="legacy-snapshot-v1:$kind:$external:$digest"
        if(dao.state(marker)!=null)return@withTransaction
        val provider=if(external.matches(Regex("[0-9]+")))"openopus" else "legacy"
        val providerKind=if(kind=="artist")"composer" else "work"
        val identity=dao.identity(provider,providerKind,external)
        // Do not replace reviewed provider assertions with an old normalized cache.
        if(identity?.reviewState=="reviewed"){dao.state(CatalogueState(marker,"ready"));return@withTransaction}
        val id=identity?.entityId ?: java.util.UUID.randomUUID().toString()
        if(dao.entity(id)==null) {
            val entity=CatalogueEntity(id,kind,title)
            if(kind=="artist")add(entity,CatalogueArtist(id,sortName=title))else add(entity,CatalogueWork(id))
        }else if(!dao.hasAlias(id,"und",title))addAlias(CatalogueAlias(entityId=id,language="und",value=title,normalized=CatalogueValidation.normalized(title)))
        if(identity==null)map(CatalogueExternalIdentity(provider,providerKind,external,id,"retained-local-normalized-snapshot;rights-unreviewed","observed"))
        val source=CatalogueSource(java.util.UUID.randomUUID().toString(),"legacy-local","$kind:$external",digest,digest,
            java.time.Instant.now().toString(),"local:retained-catalogue-snapshot")
        addSource(source)
        val rights=CatalogueRights(java.util.UUID.randomUUID().toString(),"unknown","","Retained local metadata; original provider response unavailable","unreviewed")
        addRights(rights)
        row.keys().forEach {field->if(field!="id")addAssertion(CatalogueAssertion(java.util.UUID.randomUUID().toString(),id,field,
            org.json.JSONObject().put("value",row.get(field)).put("language","und").toString(),source.id,rights.id,false))}
        if(kind=="work") {
            val composer=row.optString("composerId")
            val author=dao.identity(if(composer.matches(Regex("[0-9]+")))"openopus" else "legacy","composer",composer)
            if(author!=null && !dao.hasEdge(id,author.entityId,"credit"))relate(CatalogueRelationship(java.util.UUID.randomUUID().toString(),"credit",id,"work",author.entityId,"artist",role="composer",creditedAs=dao.entity(author.entityId)?.title.orEmpty(),sourceId=source.id))
        }
        dao.state(CatalogueState(marker,"ready"))
    }
    internal suspend fun ingest(result:com.opus.piano.data.catalogue.providers.ProviderResult)=CatalogueIngestion(db).accept(result)
    internal suspend fun supplement(id:String,result:com.opus.piano.data.catalogue.providers.ProviderResult)=db.withTransaction {
        require(result.license=="CC-BY-SA-4.0" && result.request.provider=="wikipedia" && dao.entity(id)!=null)
        val record=result.records.single()
        val language=record.fields.single {it.field=="biography"}.language
        require(language in setOf("cs","en"))
        val recordId="$language:"+result.request.externalId.orEmpty()
        val revision=record.fields.first {it.field=="article_revision"}.value+"|sha256="+result.hash
        val key="document:"+id+":"+com.opus.piano.data.catalogue.SearchText.hash("$recordId|$revision|"+record.fields.toString())
        if(dao.state(key)!=null)return@withTransaction
        val source=dao.sourceObservation("wikipedia",recordId,revision) ?: CatalogueSource(java.util.UUID.randomUUID().toString(),"wikipedia",recordId,revision,result.hash,result.observedAt,result.request.url).also {addSource(it)}
        val rights=CatalogueRights(java.util.UUID.randomUUID().toString(),result.license,result.licenseUrl,"Wikipedia contributors — "+result.request.url,"approved","Plain-text excerpt, truncated")
        addRights(rights)
        for(field in record.fields) {
            val payload=org.json.JSONObject().put("value",field.value).put("language",field.language).toString()
            if(!dao.hasAssertionValue(id,field.field,source.id,payload))addAssertion(CatalogueAssertion(java.util.UUID.randomUUID().toString(),id,field.field,payload,source.id,rights.id,!dao.hasAssertion(id,field.field)))
        }
        dao.state(CatalogueState(key,"ready"))
    }
    suspend fun states(prefix:String,limit:Int=200)=dao.states(prefix.replace("%","").replace("_","")+"%",limit.coerceIn(1,200))
    suspend fun removeState(key:String){dao.removeState(key);if(CatalogueObservationCache.prefixes.any {key.startsWith(it)})dao.removeState("access:$key")}
    internal suspend fun boundedState(prefix:String,key:String,value:String,maxEntries:Int=200)=CatalogueObservationCache(db).put(prefix,key,value,maxEntries=maxEntries)
    internal suspend fun touchObservation(key:String)=CatalogueObservationCache(db).touch(key)
    suspend fun generation():String=dao.state("generation")?.value ?: "initial"
    internal suspend fun workType(id:String)=dao.workType(id)
    internal suspend fun workTypes(ids:List<String>)=if(ids.isEmpty())emptyList() else dao.workTypes(ids)
    suspend fun assertions(id:String)=dao.assertions(id)
    suspend fun providerIdentity(provider:String,kind:String,id:String)=dao.identity(provider,kind,id)
    suspend fun identities(id:String)=dao.identities(id)
    suspend fun source(id:String)=dao.source(id)
    suspend fun rights(id:String)=dao.rights(id)
    suspend fun linked(id:String)=dao.allRelationships(id)
    suspend fun state(key:String)=dao.state(key)?.value
    suspend fun state(key:String,value:String)=dao.state(CatalogueState(key,value))
    /** Three bounded page-wide reads, never per-row UI I/O. Display context is not identity. */
    internal fun rowInfo(ids:List<String>):Map<String,CatalogueRowInfo> {
        if(ids.isEmpty())return emptyMap()
        require(ids.size<=40)
        val placeholders=ids.joinToString(","){"?"};val args=ids.toTypedArray()
        val types=mutableMapOf<String,String>();val providers=mutableMapOf<String,MutableSet<String>>()
        val composers=mutableMapOf<String,MutableSet<String>>()
        db.openHelper.readableDatabase.query("SELECT id,artistType FROM artists WHERE id IN ($placeholders)",args).use {c->
            while(c.moveToNext())types[c.getString(0)]=c.getString(1)
        }
        db.openHelper.readableDatabase.query("SELECT entityId,provider,providerKind FROM external_identities WHERE reviewState='reviewed' AND entityId IN ($placeholders) ORDER BY provider",args).use {c->
            while(c.moveToNext()) {
                val id=c.getString(0);val provider=c.getString(1)
                providers.getOrPut(id){linkedSetOf()}.add(provider)
                if(provider=="openopus" && c.getString(2)=="composer")types[id]="composer"
            }
        }
        db.openHelper.readableDatabase.query("SELECT r.fromId,e.title FROM relationships r JOIN entities e ON e.id=r.toId WHERE r.type='credit' AND r.role='composer' AND r.fromId IN ($placeholders) ORDER BY r.position,e.id",args).use {c->
            while(c.moveToNext())composers.getOrPut(c.getString(0)){linkedSetOf()}.add(c.getString(1))
        }
        return ids.associateWith {CatalogueRowInfo(types[it],composers[it]?.toList().orEmpty(),providers[it]?.toList().orEmpty())}
    }
    internal suspend fun aliasPrefix(prefix:String)=dao.aliasPrefix(prefix,prefix+"\uffff")
    internal suspend fun availablePeriods()=dao.availablePeriods().mapNotNull {runCatching {org.json.JSONObject(it).getString("value").takeIf {v->v.isNotBlank() && v.length<=80}}.getOrNull()}.distinct()
    internal suspend fun queryEntities(query:androidx.sqlite.db.SupportSQLiteQuery)=dao.queryEntities(query)
    internal fun postingCount(match:String):Long=db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM alias_fts WHERE alias_fts MATCH ?",arrayOf(match)).use {it.moveToFirst();it.getLong(0)}

    suspend fun entity(id: String): CatalogueEntity? { CatalogueValidation.uuid(id); return dao.entity(id) }
    suspend fun page(kind: String? = null, after: String = "", limit: Int = 40): List<CatalogueEntity> {
        bounds(kind, after, limit); return dao.page(kind, after, limit)
    }
    suspend fun search(text: String, kind: String? = null, after: String = "", limit: Int = 40): List<CatalogueEntity> {
        bounds(kind, after, limit)
        val query = CatalogueValidation.query(text)
        return if (query.isBlank()) emptyList() else dao.search(query, kind, after, limit)
    }
    private fun bounds(kind: String?, after: String, limit: Int) {
        require(kind == null || kind in CatalogueValidation.kinds); require(limit in 1..100)
        if (after.isNotEmpty()) CatalogueValidation.uuid(after)
    }
    suspend fun relationships(id: String, limit: Int = 40): List<CatalogueRelationship> {
        CatalogueValidation.uuid(id); require(limit in 1..100); return dao.relationships(id, limit)
    }
    /** Root and subtype must commit together. Callers cannot insert a wrong-kind subtype. */
    suspend fun add(entity: CatalogueEntity, subtype: Any) = db.withTransaction {
        CatalogueValidation.uuid(entity.id); require(entity.kind in CatalogueValidation.kinds)
        dao.insert(entity)
        when (subtype) {
            is CatalogueWork -> { require(subtype.id == entity.id && entity.kind == subtype.kind && subtype.kind == "work"); dao.insert(subtype) }
            is CatalogueArrangement -> { require(subtype.id == entity.id && entity.kind == subtype.kind && subtype.kind == "arrangement"); dao.insert(subtype) }
            is CatalogueArtist -> { require(subtype.id == entity.id && entity.kind == subtype.kind && subtype.kind == "artist"); dao.insert(subtype) }
            is CatalogueAlbum -> { require(subtype.id == entity.id && entity.kind == subtype.kind && subtype.kind == "album"); dao.insert(subtype) }
            is CatalogueRelease -> { require(subtype.id == entity.id && entity.kind == subtype.kind && subtype.kind == "release"); dao.insert(subtype) }
            is CatalogueRecording -> { require(subtype.id == entity.id && entity.kind == subtype.kind && subtype.kind == "recording"); dao.insert(subtype) }
            is CatalogueCollection -> { require(subtype.id == entity.id && entity.kind == subtype.kind && subtype.kind == "collection"); dao.insert(subtype) }
            else -> error("Unknown catalogue subtype")
        }
        dao.insert(CatalogueAlias(entityId=entity.id, language="und", value=entity.title, normalized=CatalogueValidation.normalized(entity.title)))
    }
    suspend fun addAlias(alias: CatalogueAlias) {
        require(alias.normalized == CatalogueValidation.normalized(alias.value)); dao.insert(alias)
    }
    suspend fun relate(row: CatalogueRelationship) = db.withTransaction {
        CatalogueValidation.uuid(row.id)
        val from = requireNotNull(dao.entity(row.fromId)); val to = requireNotNull(dao.entity(row.toId))
        require(from.kind == row.fromKind && to.kind == row.toKind)
        if (row.type == "part") {
            val child = db.openHelper.readableDatabase.query("SELECT workType FROM works WHERE id=?", arrayOf(row.fromId)).use { c -> if (c.moveToFirst()) c.getString(0) else null }
            require(child in setOf("movement", "part"))
        }
        if (row.type == "source") {
            val type = db.openHelper.readableDatabase.query("SELECT workType FROM works WHERE id=?", arrayOf(row.fromId)).use { c -> if(c.moveToFirst()) c.getString(0) else null }
            require(type in setOf("medley", "mashup"))
        }
        dao.insert(row) // typed FKs, ordered uniqueness and recursive cycle guards in SQLite
    }
    suspend fun map(row: CatalogueExternalIdentity) = db.withTransaction {
        val target = requireNotNull(dao.entity(row.entityId))
        val previous = dao.identity(row.provider, row.providerKind, row.externalId)
        require(row.revision == (previous?.revision ?: 0) + 1)
        require(previous == null || dao.entity(previous.entityId)?.kind == target.kind)
        if (previous != null && previous.entityId != row.entityId) require(row.reviewState == "reviewed")
        val evidence=row.evidence+(row.sourceId?.let {"; accepted source:$it"} ?: "")
        dao.insert(CatalogueIdentityRevision(row.provider,row.providerKind,row.externalId,row.revision,row.entityId,evidence,row.reviewState))
        if(previous == null) dao.insert(row) else dao.updateIdentity(row)
    }
    suspend fun redirect(row: CatalogueRedirect) = db.withTransaction {
        val from = requireNotNull(dao.entity(row.sourceId))
        require(row.kind == from.kind && row.revision == (dao.redirect(row.sourceId)?.revision ?: 0) + 1)
        require(row.reason.isNotBlank() && row.evidence.isNotBlank() && row.reviewer.isNotBlank())
        if (row.targetId != null) {
            require(dao.entity(row.targetId)?.kind == row.kind)
            var current: String? = row.targetId
            val visited = mutableSetOf(row.sourceId)
            var depth = 0
            while(current != null) {
                require(visited.add(current) && ++depth <= 64) { "Cyclic or excessive redirect chain" }
                current = dao.redirect(current)?.targetId
            }
        }
        dao.insert(row) // null target is a new, auditable reversal; old evidence remains
        dao.state(CatalogueState("generation",java.util.UUID.randomUUID().toString()))
    }
    suspend fun resolve(id: String): CatalogueEntity? {
        CatalogueValidation.uuid(id)
        var current = id; val visited = mutableSetOf<String>()
        repeat(65) {
            check(visited.add(current))
            val redirect = dao.redirect(current)?.targetId ?: return dao.entity(current)
            current = redirect
        }
        error("Redirect chain exceeds bound")
    }
    suspend fun addSource(row: CatalogueSource) { dao.insert(row) }
    suspend fun addRights(row: CatalogueRights) { dao.insert(row) }
    suspend fun addAssertion(row: CatalogueAssertion) { dao.insert(row) }
    suspend fun addDocument(row: CatalogueDocument) { dao.insert(row) }
    suspend fun addMedia(row: CatalogueMedia) { dao.insert(row) }
}
