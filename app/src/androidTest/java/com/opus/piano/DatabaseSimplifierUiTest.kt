package com.opus.piano

import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.catalogue.*
import com.opus.piano.data.catalogue.providers.*
import com.opus.piano.ui.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Production shared Library rows/detail; exported frames are component renders. */
@RunWith(AndroidJUnit4::class)
class DatabaseSimplifierUiTest {
    private val inst=InstrumentationRegistry.getInstrumentation()
    private fun nodes(n:AccessibilityNodeInfo?):List<AccessibilityNodeInfo> = if(n==null)emptyList() else listOf(n)+(0 until n.childCount).flatMap {nodes(n.getChild(it))}
    @android.annotation.SuppressLint("NewApi")
    private fun tree():List<AccessibilityNodeInfo>{inst.uiAutomation.clearCache();return nodes(inst.uiAutomation.rootInActiveWindow)}
    private fun await(label:String):AccessibilityNodeInfo {
        val deadline=android.os.SystemClock.uptimeMillis()+15000
        while(android.os.SystemClock.uptimeMillis()<deadline) {
            inst.waitForIdleSync();tree().firstOrNull {it.isVisibleToUser && it.text?.toString()==label}?.let {return it}
            Thread.sleep(80)
        }
        error("Missing visible shared catalogue title: $label")
    }
    @android.annotation.SuppressLint("NewApi")
    private fun export(name:String,bytes:ByteArray) {
        inst.uiAutomation.executeShellCommand("mkdir -p /data/local/tmp/goldberg-discover").use {ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()}
        val pipes=inst.uiAutomation.executeShellCommandRw("dd of=/data/local/tmp/goldberg-discover/$name")
        ParcelFileDescriptor.AutoCloseOutputStream(pipes[1]).use {it.write(bytes)}
        ParcelFileDescriptor.AutoCloseInputStream(pipes[0]).use {it.readBytes()}
    }
    @Test fun sharedLibraryRowOpensTheSameCanonicalDetailInBothThemesAndLargeType()=runBlocking {
        check(android.os.Build.HARDWARE in setOf("ranchu","goldfish"))
        val app=inst.targetContext.applicationContext as OpusApplication
        val external=UUID.randomUUID().toString();val title="Shared catalogue ${external.take(8)}"
        val id=(app.localCatalogue.access {it.ingest(ProviderResult(
            ProviderRequest("https://musicbrainz.org/ws/2/work/$external?fmt=json","musicbrainz","work"),SourceState.FRESH,
            listOf(ProviderRecord("work",external,"work",title,subtype="composition")),revision="synthetic-1",hash="e".repeat(64))).single()} as CatalogueAccess.Available).value
        for((theme,font) in listOf("Light" to 1f,"Dark" to 1f,"Dark" to 2f)) {
            var selected:String? by mutableStateOf(null)
            ActivityScenario.launch(MainActivity::class.java).use {scenario->
                scenario.onActivity {activity->activity.setContent {
                    OpusTheme(theme) {
                        val density=LocalDensity.current
                        CompositionLocalProvider(LocalDensity provides Density(density.density,font)) {
                            Surface(Modifier.fillMaxSize()) {Column(Modifier.fillMaxSize().padding(20.dp)) {
                                if(selected==null)SharedLibraryCatalogue(title,"works",{selected=it},{})
                                else DiscoverDetailScreen(selected!!,{selected=null},{selected=it},{})
                            }}
                        }
                    }
                }}
                val row=await(title);var clickable:AccessibilityNodeInfo?=row
                while(clickable!=null && !clickable.isClickable)clickable=clickable.parent
                assertNotNull(clickable)
                val bitmap=inst.uiAutomation.takeScreenshot();assertNotNull(bitmap)
                val bytes=java.io.ByteArrayOutputStream();assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,bytes));bitmap.recycle()
                export("simplifier-$theme-${font.toInt()}.png",bytes.toByteArray())
                export("simplifier-$theme-${font.toInt()}.txt",tree().joinToString("\n"){"${it.text} | ${it.contentDescription}"}.toByteArray())
                assertTrue(clickable!!.performAction(AccessibilityNodeInfo.ACTION_CLICK));inst.waitForIdleSync()
                await(title);assertEquals(id,selected)
            }
        }
    }
}
