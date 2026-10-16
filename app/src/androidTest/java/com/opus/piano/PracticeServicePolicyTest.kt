package com.opus.piano

import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.service.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PracticeServicePolicyTest {
    @Test fun serviceIsPrivateAndApplicationContextCannotStartMicrophone() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val service=context.packageManager.getServiceInfo(ComponentName(context,SmartPracticeService::class.java),PackageManager.GET_META_DATA)
        assertFalse(service.exported)
        if (android.os.Build.VERSION.SDK_INT>=29) assertTrue(service.foregroundServiceType and android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE != 0)
        assertFalse(SmartPracticeService.start(context.applicationContext))
    }
    @Test fun staleNotificationActionsNeverStartRecording() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val app=context.applicationContext as OpusApplication
        val before=app.practice.state.value
        PracticeActionReceiver().onReceive(context,android.content.Intent().setAction(SmartPracticeService.STOP).putExtra("token","stale"))
        assertEquals(before.token,app.practice.state.value.token)
        assertEquals(before.running,app.practice.state.value.running)
    }
}
