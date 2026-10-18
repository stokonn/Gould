package com.opus.piano.data.catalogue

import androidx.sqlite.db.SupportSQLiteDatabase
import java.text.Normalizer
import java.util.Locale
import java.util.UUID

/** SQLite guards apply even to direct DAO writes; repository transactions enforce graphs. */
internal object CatalogueValidation {
    val kinds = setOf("work", "arrangement", "artist", "album", "release", "recording", "collection")
    fun uuid(value: String) { require(UUID.fromString(value).toString() == value) { "Noncanonical UUID" } }
    fun normalized(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT).replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()
    fun query(value: String): String = normalized(value.take(512)).split(' ').filter { it.isNotBlank() }.take(8).joinToString(" ") { "\"$it*\"" }
    fun install(db: SupportSQLiteDatabase) {
        var guardId = 0
        fun guard(table: String, condition: String) {
            val id = guardId++
            listOf("INSERT", "UPDATE").forEach { event ->
                db.execSQL("CREATE TRIGGER validate_${table}_${id}_$event BEFORE $event ON $table WHEN NOT COALESCE(($condition),0) BEGIN SELECT RAISE(ABORT,'Invalid catalogue $table'); END")
            }
        }
        guard("entities", "NEW.kind IN ('work','arrangement','artist','album','release','recording','collection') AND length(trim(NEW.title))>0 AND NEW.revision>0 AND NEW.status IN ('active','redirect','quarantined') AND length(NEW.id)=36")
        mapOf("works" to "work", "arrangements" to "arrangement", "artists" to "artist", "albums" to "album", "releases" to "release", "recordings" to "recording", "collections" to "collection").forEach { (table, kind) -> guard(table, "NEW.kind='$kind'") }
        guard("works", "NEW.workType IN ('composition','movement','part','medley','mashup')")
        guard("artists", "NEW.artistType IN ('person','group','other') AND ((NEW.birth IS NULL AND NEW.birthPrecision IS NULL) OR (NEW.birth IS NOT NULL AND NEW.birthPrecision IN ('year','month','day'))) AND ((NEW.death IS NULL AND NEW.deathPrecision IS NULL) OR (NEW.death IS NOT NULL AND NEW.deathPrecision IN ('year','month','day')))")
        guard("albums", "NEW.albumType IN ('album','ep','single','soundtrack','unknown')")
        guard("recordings", "NEW.durationMillis IS NULL OR NEW.durationMillis>=0")
        guard("releases", "(NEW.date IS NULL AND NEW.datePrecision IS NULL) OR (NEW.date IS NOT NULL AND NEW.datePrecision IN ('year','month','day'))")
        guard("relationships", "NEW.position>=0 AND NEW.medium>0 AND (" +
            "(NEW.type IN ('part','source') AND NEW.fromKind='work' AND NEW.toKind='work' AND NEW.fromId<>NEW.toId) OR " +
            "(NEW.type='arrangement_source' AND NEW.fromKind='arrangement' AND NEW.toKind='work') OR " +
            "(NEW.type='credit' AND NEW.toKind='artist' AND length(trim(NEW.role))>0) OR " +
            "(NEW.type='edition' AND NEW.fromKind='release' AND NEW.toKind='album') OR " +
            "(NEW.type='track' AND NEW.fromKind='release' AND NEW.toKind='recording') OR " +
            "(NEW.type='performance' AND NEW.fromKind='recording' AND NEW.toKind IN ('work','arrangement')) OR " +
            "(NEW.type='member' AND NEW.fromKind='collection' AND NEW.toKind<>'collection'))")
        // Row identity is immutable. Append another relationship instead of silently retargeting it.
        listOf("sources", "rights", "identity_revisions", "redirect_revisions", "relationships").forEach { table ->
            listOf("UPDATE", "DELETE").forEach { event -> db.execSQL("CREATE TRIGGER immutable_${table}_$event BEFORE $event ON $table BEGIN SELECT RAISE(ABORT,'Immutable catalogue evidence'); END") }
        }
        db.execSQL("CREATE TRIGGER relationship_positions BEFORE INSERT ON relationships WHEN (NEW.type='member' AND EXISTS(SELECT 1 FROM relationships WHERE type='member' AND fromId=NEW.fromId AND position=NEW.position)) OR (NEW.type='track' AND EXISTS(SELECT 1 FROM relationships WHERE type='track' AND fromId=NEW.fromId AND medium=NEW.medium AND position=NEW.position)) OR (NEW.type='part' AND EXISTS(SELECT 1 FROM relationships WHERE type='part' AND (fromId=NEW.fromId OR (toId=NEW.toId AND position=NEW.position)))) OR (NEW.type='edition' AND EXISTS(SELECT 1 FROM relationships WHERE type='edition' AND fromId=NEW.fromId)) BEGIN SELECT RAISE(ABORT,'Duplicate ordered catalogue position or parent'); END")
        db.execSQL("CREATE TRIGGER relationship_work_type BEFORE INSERT ON relationships WHEN (NEW.type='part' AND NOT EXISTS(SELECT 1 FROM works WHERE id=NEW.fromId AND workType IN ('movement','part'))) OR (NEW.type='source' AND NOT EXISTS(SELECT 1 FROM works WHERE id=NEW.fromId AND workType IN ('medley','mashup'))) BEGIN SELECT RAISE(ABORT,'Wrong work relationship type'); END")
        db.execSQL("CREATE TRIGGER relationship_cycle BEFORE INSERT ON relationships WHEN NEW.type IN ('part','source') AND EXISTS(WITH RECURSIVE reach(id) AS (SELECT NEW.toId UNION SELECT r.toId FROM relationships r JOIN reach ON r.fromId=reach.id WHERE r.type IN ('part','source')) SELECT 1 FROM reach WHERE id=NEW.fromId) BEGIN SELECT RAISE(ABORT,'Catalogue relationship cycle'); END")
        guard("external_identities", "length(trim(NEW.provider))>0 AND length(trim(NEW.providerKind))>0 AND length(trim(NEW.externalId))>0 AND length(trim(NEW.evidence))>0 AND NEW.revision>0 AND NEW.reviewState IN ('observed','reviewed','superseded')")
        guard("rights", "NEW.reviewState IN ('approved','blocked','unknown') AND (NEW.reviewState<>'approved' OR (length(trim(NEW.license))>0 AND NEW.license IN ('CC0-1.0','CC-BY-4.0','CC-BY-SA-4.0','CC-BY-3.0','CC-BY-SA-3.0','Public-domain-verified') AND length(trim(NEW.licenseUrl))>0 AND length(trim(NEW.attribution))>0))")
        guard("sources", "length(trim(NEW.provider))>0 AND length(trim(NEW.recordId))>0 AND length(trim(NEW.revision))>0 AND length(NEW.payloadSha256)=64 AND length(trim(NEW.sourceUrl))>0")
        guard("assertions", "NEW.selected=0 OR EXISTS(SELECT 1 FROM rights WHERE id=NEW.rightsId AND reviewState='approved')")
        guard("documents", "NEW.language IN ('cs','en') AND length(NEW.contentSha256)=64 AND length(trim(NEW.revision))>0 AND EXISTS(SELECT 1 FROM rights WHERE id=NEW.rightsId AND reviewState='approved') AND length(NEW.relativePath)>0 AND NEW.relativePath NOT LIKE '/%' AND instr(NEW.relativePath,'..')=0 AND instr(NEW.relativePath,char(92))=0 AND length(NEW.revisionUrl)>0 AND length(NEW.historyUrl)>0")
        guard("media", "NEW.bytes>=0 AND length(NEW.contentSha256)=64 AND EXISTS(SELECT 1 FROM rights WHERE id=NEW.rightsId AND reviewState='approved') AND length(NEW.relativePath)>0 AND NEW.relativePath NOT LIKE '/%' AND instr(NEW.relativePath,'..')=0 AND instr(NEW.relativePath,char(92))=0")
    }
}
