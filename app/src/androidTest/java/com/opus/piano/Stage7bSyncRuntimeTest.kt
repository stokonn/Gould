package com.opus.piano

import android.app.NotificationManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.*
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.opus.piano.admin.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/** Controlled Android WorkManager venue; no real periodic delay/backend claim. */
@RunWith(AndroidJUnit4::class)
class Stage7bSyncRuntimeTest {
    private val inst = InstrumentationRegistry.getInstrumentation()
    private val app get() = inst.targetContext.applicationContext as OpusApplication
    private fun worker(scope: String?) = TestListenableWorkerBuilder<AdminSyncWorker>(app)
        .setInputData(if (scope == null) Data.EMPTY else workDataOf("scope" to scope)).build()

    @Test fun uniqueThreeHourWorkUsesConsentConstraintsAndCancels() {
        val endpoint = app.adminSettings.endpoint
        val policy = app.adminSettings.syncPolicy
        WorkManagerTestInitHelper.initializeTestWorkManager(app)
        val manager = WorkManager.getInstance(app)
        try {
            app.adminSettings.endpoint = "https://synthetic-scheduling.invalid"
            app.adminSettings.syncPolicy = AdminSyncPolicy(background = true)
            AdminSyncWorker.schedule(app)
            var info = manager.getWorkInfosForUniqueWork(AdminSyncWorker.NAME).get().single { !it.state.isFinished }
            assertEquals(NetworkType.UNMETERED, info.constraints.requiredNetworkType)
            assertEquals(TimeUnit.HOURS.toMillis(3), info.periodicityInfo!!.repeatIntervalMillis)
            val first = info.id
            AdminSyncWorker.schedule(app)
            assertEquals(first, manager.getWorkInfosForUniqueWork(AdminSyncWorker.NAME).get().single { !it.state.isFinished }.id)
            app.adminSettings.syncPolicy = AdminSyncPolicy(background = true, mobileConsent = true)
            AdminSyncWorker.schedule(app)
            info = manager.getWorkInfosForUniqueWork(AdminSyncWorker.NAME).get().single { !it.state.isFinished }
            assertEquals(NetworkType.CONNECTED, info.constraints.requiredNetworkType)
            AdminSyncWorker.cancel(app)
            assertTrue(manager.getWorkInfosForUniqueWork(AdminSyncWorker.NAME).get().all { it.state == WorkInfo.State.CANCELLED })
            app.adminSettings.syncPolicy = AdminSyncPolicy()
            AdminSyncWorker.schedule(app)
            assertTrue(manager.getWorkInfosForUniqueWork(AdminSyncWorker.NAME).get().all { it.state.isFinished })
        } finally {
            AdminSyncWorker.cancel(app)
            manager.cancelUniqueWork(AdminSyncWorker.NAME).result.get()
            app.adminSettings.syncPolicy = AdminSyncPolicy()
            app.adminSettings.endpoint = endpoint; app.adminSettings.syncPolicy = policy
            WorkManagerTestInitHelper.closeWorkDatabase()
        }
    }

    @Test fun staleEndpointAndMissingSessionCannotNotify() = runBlocking {
        val endpoint = app.adminSettings.endpoint
        val policy = app.adminSettings.syncPolicy
        val session = app.adminSession.readToken()
        val manager = app.getSystemService(NotificationManager::class.java)
        try {
            app.adminSettings.endpoint = "https://synthetic-account-a.invalid"
            app.adminSettings.syncPolicy = AdminSyncPolicy(true, true, true)
            val staleScope = AdminSyncWorker.scope(app)
            app.adminSettings.endpoint = "https://synthetic-account-b.invalid"
            assertEquals(AdminSyncPolicy(), app.adminSettings.syncPolicy)
            app.adminSettings.syncPolicy = AdminSyncPolicy(true, true, true)
            app.adminSession.clear()
            assertEquals(ListenableWorker.Result.success(), worker(staleScope).doWork())
            assertEquals(ListenableWorker.Result.success(), worker(AdminSyncWorker.scope(app)).doWork())
            assertEquals(ListenableWorker.Result.success(), worker(null).doWork())
            assertTrue(manager.activeNotifications.none { it.id == AdminSyncWorker.NOTIFICATION_ID })
        } finally {
            manager.cancel(AdminSyncWorker.NOTIFICATION_ID)
            app.adminSettings.syncPolicy = AdminSyncPolicy()
            app.adminSettings.endpoint = "https://synthetic-account-a.invalid"
            app.adminSettings.syncPolicy = AdminSyncPolicy()
            app.adminSettings.endpoint = endpoint; app.adminSettings.syncPolicy = policy
            if (session == null) app.adminSession.clear() else app.adminSession.writeToken(session)
        }
    }
}
