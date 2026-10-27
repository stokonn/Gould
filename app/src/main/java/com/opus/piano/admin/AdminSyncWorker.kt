package com.opus.piano.admin

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.opus.piano.OpusApplication
import com.opus.piano.R
import java.util.concurrent.TimeUnit

/** Optional, approximate 3h check; no push, continuous polling or decisions. Stores
 * only a digest of a COMPLETE bounded queue. Incomplete/failed scans never notify.
 * Work input contains account/endpoint digest only, never a bearer token/payload. */
class AdminSyncWorker @JvmOverloads constructor(
    context: Context,
    parameters: WorkerParameters,
    private val repositoryOverride: AdminRepository? = null,
) : CoroutineWorker(context,parameters) {
    override suspend fun doWork(): Result {
        val app=applicationContext as OpusApplication
        val settings=app.adminSettings
        val scheduledScope=inputData.getString(SCOPE) ?: return Result.success()
        fun scopeCurrent()=scheduledScope==scope(app) && settings.syncPolicy.background
        if(!scopeCurrent() || settings.endpoint.isBlank() || app.adminSession.readToken()==null) return Result.success()
        val connectivity=applicationContext.getSystemService(ConnectivityManager::class.java)
        val capabilities=connectivity.getNetworkCapabilities(connectivity.activeNetwork) ?: return Result.success()
        if(!settings.syncPolicy.permitsNetwork(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI),connectivity.isActiveNetworkMetered)) return Result.success()
        val admin=repositoryOverride ?: app.adminRepository
        val role=admin.verifyRole()
        if(role !is AdminResult.Ok || !role.value.canQueueRead || !scopeCurrent()) return Result.success()
        val principal=role.value.principalId
        val signature=queueSignature(admin.api) ?: return Result.success()
        if(!scopeCurrent()) return Result.success()
        // Re-check fresh role after the bounded walk to reject revocation during a scan.
        val fresh=admin.verifyRole()
        if(fresh !is AdminResult.Ok || !fresh.value.canQueueRead || fresh.value.principalId!=principal || !scopeCurrent()) return Result.success()
        val key=adminFingerprint("$scheduledScope:$principal")
        val previous=settings.syncSignature(key)
        settings.saveSyncSignature(key,signature)
        val permitted=Build.VERSION.SDK_INT<33 || ContextCompat.checkSelfPermission(applicationContext,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED
        if(previous!=null && previous!=signature && settings.syncPolicy.permitsNotification(permitted)) notifyChange()
        return Result.success()
    }

    private fun notifyChange() {
        val manager=applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL,applicationContext.getString(R.string.stage7b_administration),NotificationManager.IMPORTANCE_LOW))
        val launch=applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        val intent=launch?.let { PendingIntent.getActivity(applicationContext,701,it,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT) }
        val notice=NotificationCompat.Builder(applicationContext,CHANNEL)
            .setSmallIcon(R.drawable.ic_goldberg_monochrome)
            .setContentTitle(applicationContext.getString(R.string.stage7b_administration))
            .setContentText(applicationContext.getString(R.string.stage7b_queue_notification))
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setAutoCancel(true)
            .setContentIntent(intent).build()
        try { manager.notify(NOTIFICATION_ID,notice) } catch (_: SecurityException) { /* permission revoked while checking */ }
    }

    companion object {
        const val NAME="goldberg-admin-queue-check"
        private const val SCOPE="scope"
        private const val CHANNEL="goldberg-admin-queue"
        const val NOTIFICATION_ID=701
        fun scope(app: OpusApplication)=adminFingerprint("${app.identity.account.value.orEmpty()}\n${app.adminSettings.endpoint}")
        fun schedule(app: OpusApplication) {
            val manager=WorkManager.getInstance(app)
            val policy=app.adminSettings.syncPolicy
            if(!policy.background) { manager.cancelUniqueWork(NAME); return }
            val work=PeriodicWorkRequestBuilder<AdminSyncWorker>(3,TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(if(policy.mobileConsent) NetworkType.CONNECTED else NetworkType.UNMETERED).build())
                .setInputData(workDataOf(SCOPE to scope(app))).build()
            manager.enqueueUniquePeriodicWork(NAME,ExistingPeriodicWorkPolicy.UPDATE,work)
        }
        fun cancel(app: OpusApplication) {
            WorkManager.getInstance(app).cancelUniqueWork(NAME)
            app.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
        }
    }
}

/** Exact complete-or-unavailable snapshot. Pure API seam supports JVM coverage. */
internal suspend fun queueSignature(api: AdminApi,maxPages: Int=8): String? {
    var cursor: String?=null
    val seen=mutableSetOf<String>()
    val rows=linkedMapOf<String,String>()
    repeat(maxPages) {
        when(val result=api.queue(cursor,null)) {
            is AdminResult.Err -> return null
            is AdminResult.Ok -> {
                result.value.items.forEach { row -> rows[row.id]="${row.id}:${row.revision}:${row.status.wire}" }
                cursor=result.value.nextCursor
                if(cursor==null) return adminFingerprint(rows.values.sorted().joinToString("\n"))
                if(!seen.add(cursor!!)) return null
            }
        }
    }
    return null
}
