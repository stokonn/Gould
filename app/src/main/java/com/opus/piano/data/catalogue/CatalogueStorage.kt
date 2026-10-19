package com.opus.piano.data.catalogue

import android.content.Context
import java.io.File

/** Guards cover only the disposable catalogue, never opus.db or external backup paths. */
class CatalogueStorage(context:Context) {
    private val directory=context.getDatabasePath(CatalogueDatabase.FILENAME).parentFile!!
    private val criticalBackups=File(context.filesDir,"personal-backups")
    private val media=File(context.cacheDir,"catalogue-stage4")
    val intake=CatalogueIntake(::admits)
    data class Usage(val databaseBytes:Long,val mediaBytes:Long,val freeBytes:Long)
    fun usage():Usage=Usage(directory.listFiles()?.filter {it.name==CatalogueDatabase.FILENAME || it.name.startsWith(CatalogueDatabase.FILENAME+".") || it.name.startsWith(CatalogueDatabase.FILENAME+"-")}?.sumOf {it.length()} ?: 0,media.walkTopDown().maxDepth(3).filter {it.isFile}.sumOf {it.length()},directory.usableSpace)
    fun requiredReserve():Long {
        val personal=directory.listFiles()?.filter {it.name in setOf("opus.db","opus.db-wal","opus.db-shm","opus.db-journal")}?.sumOf {it.length()} ?: 0L
        val backups=criticalBackups.listFiles()?.filter {it.isFile}?.sumOf {it.length()} ?: 0L
        return CatalogueBudget.reserve(personal,backups)
    }
    fun admits(estimatedBytes:Long):Boolean {val u=usage();return estimatedBytes in 0..MAX_TOTAL && u.databaseBytes<=MAX_TOTAL-u.mediaBytes-estimatedBytes && CatalogueBudget.fits(u.freeBytes,estimatedBytes,requiredReserve())}
    fun trimMedia(now:Long=System.currentTimeMillis()) {
        val files=media.listFiles()?.filter {it.isFile}?.sortedBy {it.lastModified()}.orEmpty()
        var size=files.sumOf {it.length()}
        files.forEach {file->if(size>MAX_MEDIA || now-file.lastModified()>MAX_AGE){val bytes=file.length();if(file.delete())size-=bytes}}
    }
    companion object {const val MAX_TOTAL=256L*1024*1024;const val MAX_MEDIA=16L*1024*1024;const val MAX_RESPONSE=2L*1024*1024;const val MAX_INTAKE=8*MAX_RESPONSE;const val FREE_RESERVE=128L*1024*1024;const val MAX_AGE=30L*86400000}
}
