package com.opus.piano.data.catalogue

import androidx.room.*

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase

@Dao
internal interface CatalogueDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueEntity)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueWork)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueArrangement)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueArtist)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueAlbum)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueRelease)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueRecording)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueCollection)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueRelationship)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueExternalIdentity)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueIdentityRevision)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueRedirect)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueAlias)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueSource)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueRights)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueAssertion)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueDocument)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueMedia)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(row: CatalogueState)
    @Query("SELECT * FROM entities WHERE id = :id") suspend fun entity(id: String): CatalogueEntity?
    @Query("SELECT * FROM relationships WHERE fromId = :id ORDER BY type, medium, position, id LIMIT :limit") suspend fun relationships(id: String, limit: Int): List<CatalogueRelationship>
    @Query("SELECT e.* FROM entities e WHERE e.id > :after AND (:kind IS NULL OR e.kind = :kind) ORDER BY e.id LIMIT :limit") suspend fun page(kind: String?, after: String, limit: Int): List<CatalogueEntity>
    @Query("SELECT DISTINCT e.* FROM alias_fts JOIN aliases a ON a.rowid=alias_fts.rowid JOIN entities e ON e.id=a.entityId WHERE alias_fts MATCH :query AND e.id > :after AND e.status='active' AND (:kind IS NULL OR e.kind=:kind) ORDER BY e.id LIMIT :limit") suspend fun search(query: String, kind: String?, after: String, limit: Int): List<CatalogueEntity>
    @Query("SELECT * FROM redirect_revisions WHERE sourceId = :id ORDER BY revision DESC LIMIT 1") suspend fun redirect(id: String): CatalogueRedirect?
    @Query("SELECT * FROM external_identities WHERE provider = :provider AND providerKind = :kind AND externalId = :id") suspend fun identity(provider: String, kind: String, id: String): CatalogueExternalIdentity?
    @Query("SELECT * FROM assertions WHERE entityId=:id AND (:field IS NULL OR field=:field) ORDER BY field, selected DESC, id LIMIT 200") suspend fun assertions(id:String,field:String?=null):List<CatalogueAssertion>
    @Query("SELECT EXISTS(SELECT 1 FROM aliases WHERE entityId=:id AND language=:language AND value=:value)") suspend fun hasAlias(id:String,language:String,value:String):Boolean
    @Query("SELECT EXISTS(SELECT 1 FROM relationships WHERE fromId=:from AND toId=:to AND type=:type)") suspend fun hasEdge(from:String,to:String,type:String):Boolean
    @Query("SELECT * FROM sources WHERE provider=:provider AND recordId=:recordId AND revision=:revision LIMIT 1") suspend fun sourceObservation(provider:String,recordId:String,revision:String):CatalogueSource?
    @Query("SELECT * FROM sources WHERE id=:id") suspend fun source(id:String):CatalogueSource?
    @Query("SELECT * FROM external_identities WHERE entityId=:id ORDER BY provider,providerKind,externalId LIMIT 40") suspend fun identities(id:String):List<CatalogueExternalIdentity>
    @Query("SELECT * FROM relationships WHERE fromId=:id OR toId=:id ORDER BY type,medium,position,id LIMIT :limit") suspend fun allRelationships(id:String,limit:Int=100):List<CatalogueRelationship>
    @Query("SELECT normalized FROM aliases WHERE normalized>=:prefix AND normalized<:upper ORDER BY normalized LIMIT 200") suspend fun aliasPrefix(prefix:String,upper:String):List<String>
    @Query("SELECT DISTINCT valueJson FROM assertions WHERE field='epoch' AND selected=1 ORDER BY valueJson LIMIT 40") suspend fun availablePeriods():List<String>
    @androidx.room.RawQuery suspend fun queryEntities(query:androidx.sqlite.db.SupportSQLiteQuery):List<CatalogueEntity>
    @Update suspend fun updateIdentity(row: CatalogueExternalIdentity)
    @Query("SELECT * FROM rights WHERE id = :id") suspend fun rights(id: String): CatalogueRights?
    @Query("SELECT * FROM catalogue_state WHERE `key` = :key") suspend fun state(key: String): CatalogueState?
    @Query("SELECT * FROM catalogue_state WHERE `key` LIKE :pattern ORDER BY `key` LIMIT :limit") suspend fun states(pattern:String,limit:Int):List<CatalogueState>
    @Query("DELETE FROM catalogue_state WHERE `key`=:key") suspend fun removeState(key:String)
    @Upsert suspend fun state(row: CatalogueState)
}
@Database(entities = [CatalogueEntity::class, CatalogueWork::class, CatalogueArrangement::class, CatalogueArtist::class, CatalogueAlbum::class, CatalogueRelease::class, CatalogueRecording::class, CatalogueCollection::class, CatalogueRelationship::class, CatalogueExternalIdentity::class, CatalogueIdentityRevision::class, CatalogueRedirect::class, CatalogueAlias::class, CatalogueAliasFts::class, CatalogueSource::class, CatalogueRights::class, CatalogueAssertion::class, CatalogueDocument::class, CatalogueMedia::class, CatalogueState::class], version = 1, exportSchema = true)
abstract class CatalogueDatabase : RoomDatabase() {
    internal abstract fun dao(): CatalogueDao
    companion object {
        const val FILENAME = "goldberg-catalogue.db"
        fun builder(context: Context, name: String = FILENAME) =
            Room.databaseBuilder(context.applicationContext, CatalogueDatabase::class.java, name)
                .addCallback(Validation)
                // Preserve corrupt generations: default SQLite helper may delete on corruption.
                .openHelperFactory(PreservingOpenHelperFactory())
        private object Validation : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) { CatalogueValidation.install(db) }
        }
    }
}
