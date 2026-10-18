package com.opus.piano.data.catalogue

import androidx.room.withTransaction

/** Local contracts only; no provider requests, practice writes or downloadable content. */
class CatalogueRepository(private val db: CatalogueDatabase) {
    private val dao = db.dao()
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
        dao.insert(CatalogueIdentityRevision(row.provider,row.providerKind,row.externalId,row.revision,row.entityId,row.evidence,row.reviewState))
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
    }
    suspend fun resolve(id: String): CatalogueEntity? {
        CatalogueValidation.uuid(id)
        var current = id; val visited = mutableSetOf<String>()
        repeat(64) {
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
