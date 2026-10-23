package com.opus.piano

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.*
import androidx.work.testing.TestListenableWorkerBuilder
import com.opus.piano.admin.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Real worker/NotificationManager with synthetic C6 responses. No live backend.
 * Permission grant is on the disposable emulator only and is removed with the
 * suite's app uninstall; revoking a live instrumentation UID can kill its runner. */
@RunWith(AndroidJUnit4::class)
class Stage7bNotificationRuntimeTest {
    private val inst = InstrumentationRegistry.getInstrumentation()
    private val app get() = inst.targetContext.applicationContext as OpusApplication
    private fun shell(command: String) = inst.uiAutomation.executeShellCommand(command).use {
        android.os.ParcelFileDescriptor.AutoCloseInputStream(it).bufferedReader().readText()
    }
    private class Fixture {
        var revision = 1
        var roleCode = 200
        var queueCode = 200
        var calls = 0
        var afterQueue: () -> Unit = {}
        var cancel = false
        val api = AdminApi({ "https://synthetic-worker.invalid" }, AdminTransport { _,url,_,_ ->
            calls++
            if (url.contains("capabilities")) AdminHttpResponse(roleCode,
                """{"principalId":"synthetic-moderator","capabilities":["queue.read"]}""".toByteArray(),null)
            else {
                if (cancel) throw CancellationException("synthetic cancelled scan")
                afterQueue()
                AdminHttpResponse(queueCode,"""{"items":[{"id":"synthetic-private-submission","owner":"synthetic-private-author","status":"submitted","revision":$revision,"createdAt":1}],"nextCursor":null}""".toByteArray(),null)
            }
        }, { "synthetic-not-a-credential" })
        val repo = AdminRepository(api, InMemoryAdminSessionStore(), AdminTrustPolicy(),
            AdminMarkers(InMemoryAdminMarkerStore()) { "synthetic" }, AdminStatsService(api))
    }
    private suspend fun execute(fixture: Fixture, scope: String = AdminSyncWorker.scope(app)) =
        TestListenableWorkerBuilder<AdminSyncWorker>(app).setInputData(workDataOf("scope" to scope))
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker =
                    AdminSyncWorker(appContext, workerParameters, fixture.repo)
            }).build().doWork()

    @Test fun completeChangedScanPermissionFailureRevocationCancellationAndOfflineAreFailClosed() = runBlocking {
        val endpoint = app.adminSettings.endpoint
        val policy = app.adminSettings.syncPolicy
        val token = app.adminSession.readToken()
        val notices = app.getSystemService(NotificationManager::class.java)
        val network = app.getSystemService(ConnectivityManager::class.java)
        val wifi = shell("settings get global wifi_on").trim()
        val mobile = shell("settings get global mobile_data").trim()
        val airplane = shell("settings get global airplane_mode_on").trim()
        try {
            app.adminSettings.endpoint = "https://synthetic-worker-${System.nanoTime()}.invalid"
            app.adminSettings.syncPolicy = AdminSyncPolicy(true,true,true)
            app.adminSession.writeToken("synthetic-not-a-credential")
            val fixture = Fixture()
            val scope = AdminSyncWorker.scope(app)
            val key = adminFingerprint("$scope:synthetic-moderator")
            fun none() = assertTrue(notices.activeNotifications.none { it.id == AdminSyncWorker.NOTIFICATION_ID })
            assertNotNull("CI default network must exist for controlled worker venue",network.activeNetwork)
            assertEquals(PackageManager.PERMISSION_DENIED,ContextCompat.checkSelfPermission(app,Manifest.permission.POST_NOTIFICATIONS))
            assertEquals(ListenableWorker.Result.success(),execute(fixture)); none()
            val initial = app.adminSettings.syncSignature(key); assertNotNull(initial)
            fixture.revision = 2
            assertEquals(ListenableWorker.Result.success(),execute(fixture)); none()
            val denied = app.adminSettings.syncSignature(key); assertNotEquals(initial,denied)
            shell("pm grant com.opus.piano android.permission.POST_NOTIFICATIONS")
            assertEquals(PackageManager.PERMISSION_GRANTED,ContextCompat.checkSelfPermission(app,Manifest.permission.POST_NOTIFICATIONS))
            fixture.revision = 3
            assertEquals(ListenableWorker.Result.success(),execute(fixture))
            val noticeDeadline = android.os.SystemClock.uptimeMillis()+10000
            while(notices.activeNotifications.none { it.id == AdminSyncWorker.NOTIFICATION_ID } && android.os.SystemClock.uptimeMillis()<noticeDeadline) Thread.sleep(50)
            val notice = notices.activeNotifications.single { it.id == AdminSyncWorker.NOTIFICATION_ID }.notification
            assertEquals(Notification.VISIBILITY_PRIVATE,notice.visibility)
            assertEquals(app.getString(R.string.stage7b_queue_notification),notice.extras.getString(Notification.EXTRA_TEXT))
            assertEquals(app.getString(R.string.stage7b_administration),notice.extras.getString(Notification.EXTRA_TITLE))
            assertNotNull(notice.contentIntent)
            notices.cancel(AdminSyncWorker.NOTIFICATION_ID)
            val cancelDeadline = android.os.SystemClock.uptimeMillis()+10000
            while(notices.activeNotifications.any { it.id == AdminSyncWorker.NOTIFICATION_ID } && android.os.SystemClock.uptimeMillis()<cancelDeadline) Thread.sleep(50)
            val good = app.adminSettings.syncSignature(key)
            fixture.revision = 4; fixture.queueCode = 503
            assertEquals(ListenableWorker.Result.success(),execute(fixture)); none()
            assertEquals(good,app.adminSettings.syncSignature(key))
            fixture.queueCode = 200
            fixture.afterQueue = { fixture.roleCode = 403 }
            assertEquals(ListenableWorker.Result.success(),execute(fixture)); none()
            assertEquals(good,app.adminSettings.syncSignature(key))
            fixture.roleCode = 200
            fixture.afterQueue = { app.adminSettings.endpoint = "https://synthetic-other-account.invalid" }
            assertEquals(ListenableWorker.Result.success(),execute(fixture,scope)); none()
            assertEquals(good,app.adminSettings.syncSignature(key))
            app.adminSettings.endpoint = "https://synthetic-other-account.invalid"
            app.adminSettings.syncPolicy = AdminSyncPolicy(true,true,true)
            fixture.afterQueue = {}; fixture.cancel = true
            try { execute(fixture); fail("Cancellation was swallowed") } catch (_: CancellationException) { }
            none()
            fixture.cancel = false
            val beforeOffline = fixture.calls
            shell("cmd connectivity airplane-mode enable"); shell("svc wifi disable"); shell("svc data disable")
            val deadline = android.os.SystemClock.uptimeMillis()+15000
            while(network.activeNetwork != null && android.os.SystemClock.uptimeMillis()<deadline) Thread.sleep(100)
            assertNull("Controlled offline state did not settle",network.activeNetwork)
            assertEquals(ListenableWorker.Result.success(),execute(fixture)); none()
            assertEquals(beforeOffline,fixture.calls)
            shell("cmd connectivity airplane-mode disable"); shell("svc wifi enable"); shell("svc data enable")
            val recoveryDeadline = android.os.SystemClock.uptimeMillis()+15000
            while(network.activeNetwork == null && android.os.SystemClock.uptimeMillis()<recoveryDeadline) Thread.sleep(100)
            assertNotNull("Controlled online recovery did not settle",network.activeNetwork)
            assertEquals(ListenableWorker.Result.success(),execute(fixture)); none()
            assertTrue(fixture.calls>beforeOffline)
            app.adminSettings.syncPolicy = AdminSyncPolicy(true,false,true)
            fixture.revision = 5
            assertEquals(ListenableWorker.Result.success(),execute(fixture)); none()
        } finally {
            notices.cancel(AdminSyncWorker.NOTIFICATION_ID)
            shell("cmd connectivity airplane-mode ${if(airplane=="1") "enable" else "disable"}")
            shell("svc wifi ${if(wifi=="1") "enable" else "disable"}")
            shell("svc data ${if(mobile=="1") "enable" else "disable"}")
            app.adminSettings.syncPolicy = AdminSyncPolicy()
            app.adminSettings.endpoint = "https://synthetic-other-account.invalid"
            app.adminSettings.syncPolicy = AdminSyncPolicy()
            app.adminSettings.endpoint = endpoint; app.adminSettings.syncPolicy = policy
            if(token==null) app.adminSession.clear() else app.adminSession.writeToken(token)
        }
    }
}
