package com.opus.piano.data.catalogue

import androidx.room.*

/** Catalogue schema 1. Personal data has no foreign key to any of these tables. */
@Entity(tableName = "entities", indices = [Index(value = ["id", "kind"], unique = true), Index(value = ["kind", "title", "id"])])
data class CatalogueEntity(@PrimaryKey val id: String, val kind: String, val title: String,
    val revision: Long = 1, val status: String = "active")
@Entity(tableName = "works", indices = [Index(value = ["id", "kind"])], foreignKeys = [ForeignKey(entity = CatalogueEntity::class, parentColumns = ["id", "kind"], childColumns = ["id", "kind"])])
data class CatalogueWork(@PrimaryKey val id: String, val kind: String = "work", val workType: String = "composition")
@Entity(tableName = "arrangements", indices = [Index(value = ["id", "kind"])], foreignKeys = [ForeignKey(entity = CatalogueEntity::class, parentColumns = ["id", "kind"], childColumns = ["id", "kind"])])
data class CatalogueArrangement(@PrimaryKey val id: String, val kind: String = "arrangement", val instrumentation: String = "")
@Entity(tableName = "artists", indices = [Index(value = ["id", "kind"])], foreignKeys = [ForeignKey(entity = CatalogueEntity::class, parentColumns = ["id", "kind"], childColumns = ["id", "kind"])])
data class CatalogueArtist(@PrimaryKey val id: String, val kind: String = "artist", val artistType: String = "person", val sortName: String = "", val birth: String? = null, val birthPrecision: String? = null, val death: String? = null, val deathPrecision: String? = null)
@Entity(tableName = "albums", indices = [Index(value = ["id", "kind"])], foreignKeys = [ForeignKey(entity = CatalogueEntity::class, parentColumns = ["id", "kind"], childColumns = ["id", "kind"])])
data class CatalogueAlbum(@PrimaryKey val id: String, val kind: String = "album", val albumType: String = "unknown")
@Entity(tableName = "releases", indices = [Index(value = ["id", "kind"])], foreignKeys = [ForeignKey(entity = CatalogueEntity::class, parentColumns = ["id", "kind"], childColumns = ["id", "kind"])])
data class CatalogueRelease(@PrimaryKey val id: String, val kind: String = "release", val edition: String = "", val date: String? = null, val datePrecision: String? = null)
@Entity(tableName = "recordings", indices = [Index(value = ["id", "kind"])], foreignKeys = [ForeignKey(entity = CatalogueEntity::class, parentColumns = ["id", "kind"], childColumns = ["id", "kind"])])
data class CatalogueRecording(@PrimaryKey val id: String, val kind: String = "recording", val durationMillis: Long? = null)
@Entity(tableName = "collections", indices = [Index(value = ["id", "kind"])], foreignKeys = [ForeignKey(entity = CatalogueEntity::class, parentColumns = ["id", "kind"], childColumns = ["id", "kind"])])
data class CatalogueCollection(@PrimaryKey val id: String, val kind: String = "collection", val description: String = "")

/** Typed edges: part, source, arrangement_source, credit, edition, track, performance, member.
 * member/track IDs are stable; positions are unique within their ordered container/medium. */
@Entity(tableName = "relationships", indices = [Index(value = ["fromId", "fromKind"]), Index(value = ["toId", "toKind"]), Index(value = ["type", "fromId", "position"]), Index("sourceId")],
    foreignKeys = [ForeignKey(entity = CatalogueEntity::class, parentColumns = ["id", "kind"], childColumns = ["fromId", "fromKind"]),
        ForeignKey(entity = CatalogueEntity::class, parentColumns = ["id", "kind"], childColumns = ["toId", "toKind"]),
        ForeignKey(entity = CatalogueSource::class, parentColumns = ["id"], childColumns = ["sourceId"])])
data class CatalogueRelationship(@PrimaryKey val id: String, val type: String,
    val fromId: String, val fromKind: String, val toId: String, val toKind: String,
    val position: Int = 0, val medium: Int = 1, val role: String = "", val creditedAs: String = "", val sourceId: String? = null)
@Entity(tableName = "external_identities", primaryKeys = ["provider", "providerKind", "externalId"], indices = [Index("entityId"), Index("sourceId")],
    foreignKeys = [ForeignKey(entity = CatalogueEntity::class, parentColumns = ["id"], childColumns = ["entityId"]), ForeignKey(entity = CatalogueSource::class, parentColumns = ["id"], childColumns = ["sourceId"])])
data class CatalogueExternalIdentity(val provider: String, val providerKind: String, val externalId: String,
    val entityId: String, val evidence: String, val reviewState: String, val revision: Long = 1, val sourceId: String? = null)
@Entity(tableName = "identity_revisions", primaryKeys = ["provider", "providerKind", "externalId", "revision"], indices = [Index("entityId")],
    foreignKeys = [ForeignKey(entity = CatalogueEntity::class, parentColumns = ["id"], childColumns = ["entityId"])])
data class CatalogueIdentityRevision(val provider: String, val providerKind: String, val externalId: String,
    val revision: Long, val entityId: String, val evidence: String, val reviewState: String)
@Entity(tableName = "redirect_revisions", primaryKeys = ["sourceId", "revision"], indices = [Index(value = ["sourceId", "kind"]), Index(value = ["targetId", "kind"])],
    foreignKeys = [ForeignKey(entity = CatalogueEntity::class, parentColumns = ["id", "kind"], childColumns = ["sourceId", "kind"]), ForeignKey(entity = CatalogueEntity::class, parentColumns = ["id", "kind"], childColumns = ["targetId", "kind"])])
data class CatalogueRedirect(val sourceId: String, val revision: Long, val targetId: String?, val kind: String,
    val reason: String, val evidence: String, val reviewer: String)
@Entity(tableName = "aliases", indices = [Index(value = ["normalized", "entityId"]), Index(value = ["entityId", "language", "value"], unique = true)], foreignKeys = [ForeignKey(entity = CatalogueEntity::class, parentColumns = ["id"], childColumns = ["entityId"])])
data class CatalogueAlias(@PrimaryKey(autoGenerate = true) val rowid: Int = 0, val entityId: String,
    val language: String, val value: String, val normalized: String, val aliasType: String = "name")
@Fts4(contentEntity = CatalogueAlias::class, tokenizer = FtsOptions.TOKENIZER_UNICODE61)
@Entity(tableName = "alias_fts")
data class CatalogueAliasFts(val normalized: String)
@Entity(tableName = "sources", indices = [Index(value = ["provider", "recordId", "revision"], unique = true)])
data class CatalogueSource(@PrimaryKey val id: String, val provider: String, val recordId: String,
    val revision: String, val payloadSha256: String, val retrievedAt: String, val sourceUrl: String)
@Entity(tableName = "rights")
data class CatalogueRights(@PrimaryKey val id: String, val license: String, val licenseUrl: String,
    val attribution: String, val reviewState: String, val adaptation: String = "")
@Entity(tableName = "assertions", indices = [Index(value = ["entityId", "field"]), Index("sourceId"), Index("rightsId")],
    foreignKeys = [ForeignKey(entity = CatalogueEntity::class, parentColumns = ["id"], childColumns = ["entityId"]), ForeignKey(entity = CatalogueSource::class, parentColumns = ["id"], childColumns = ["sourceId"]), ForeignKey(entity = CatalogueRights::class, parentColumns = ["id"], childColumns = ["rightsId"])])
data class CatalogueAssertion(@PrimaryKey val id: String, val entityId: String, val field: String,
    val valueJson: String, val sourceId: String, val rightsId: String, val selected: Boolean = false)
@Entity(tableName = "documents", indices = [Index(value = ["entityId", "language", "sourceId", "revision"], unique = true), Index("sourceId"), Index("rightsId")],
    foreignKeys = [ForeignKey(entity = CatalogueEntity::class, parentColumns = ["id"], childColumns = ["entityId"]), ForeignKey(entity = CatalogueSource::class, parentColumns = ["id"], childColumns = ["sourceId"]), ForeignKey(entity = CatalogueRights::class, parentColumns = ["id"], childColumns = ["rightsId"])])
data class CatalogueDocument(@PrimaryKey val id: String, val entityId: String, val language: String,
    val revision: String, val contentSha256: String, val rightsId: String, val sourceId: String,
    val relativePath: String, val revisionUrl: String, val historyUrl: String, val fallbackReason: String = "")
@Entity(tableName = "media", indices = [Index("sourceId"), Index("rightsId"), Index("contentSha256")],
    foreignKeys = [ForeignKey(entity = CatalogueSource::class, parentColumns = ["id"], childColumns = ["sourceId"]), ForeignKey(entity = CatalogueRights::class, parentColumns = ["id"], childColumns = ["rightsId"])])
data class CatalogueMedia(@PrimaryKey val id: String, val contentSha256: String, val bytes: Long,
    val rightsId: String, val sourceId: String, val relativePath: String)
@Entity(tableName = "catalogue_state")
data class CatalogueState(@PrimaryKey val key: String, val value: String)
