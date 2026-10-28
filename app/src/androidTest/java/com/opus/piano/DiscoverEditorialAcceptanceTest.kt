package com.opus.piano

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.catalogue.*
import com.opus.piano.data.catalogue.providers.*
import com.opus.piano.i18n.L
import com.opus.piano.ui.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.net.URLDecoder
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicBoolean

/** Real production composables/NavHost, recorded public CC0 responses and controlled transport.
 * No provider/cloud network is activated. Before frames replay the immutable RC layout only,
 * using the same preserved catalogue rows; they are not a claim of testing the RC binary. */
@RunWith(AndroidJUnit4::class)
class DiscoverEditorialAcceptanceTest {
    private val inst=InstrumentationRegistry.getInstrumentation()
    private val app get()=inst.targetContext.applicationContext as OpusApplication
    private val automation get()=inst.uiAutomation
    private fun shell(command:String)=automation.executeShellCommand(command).use {
        android.os.ParcelFileDescriptor.AutoCloseInputStream(it).bufferedReader().readText()
    }
    private fun nodes(root:AccessibilityNodeInfo?):List<AccessibilityNodeInfo> =
        if(root==null)emptyList() else listOf(root)+(0 until root.childCount).flatMap {nodes(root.getChild(it))}
    @android.annotation.SuppressLint("NewApi")
    private fun tree():List<AccessibilityNodeInfo> {automation.clearCache();return nodes(automation.rootInActiveWindow)}
    private fun await(message:String,condition:()->Boolean) {
        val end=android.os.SystemClock.uptimeMillis()+15000
        while(android.os.SystemClock.uptimeMillis()<end){inst.waitForIdleSync();if(condition())return;Thread.sleep(80)}
        fail(message+"\n"+tree().joinToString("\n"){it.text?.toString().orEmpty()})
    }
    private fun has(label:String)=tree().filter {it.isVisibleToUser}.any {it.text?.toString()==label || it.contentDescription?.toString()==label}
    private fun click(label:String) {
        var node:AccessibilityNodeInfo?=null
        await("Missing action $label") {
            node=tree().filter {it.isVisibleToUser && (it.text?.toString()==label || it.contentDescription?.toString()==label)}.mapNotNull {
                var n:AccessibilityNodeInfo?=it;while(n!=null && !n.isClickable)n=n.parent;n
            }.firstOrNull {it.isEnabled};node!=null
        }
        assertTrue(node!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        inst.waitForIdleSync()
    }
    private fun enter(value:String) {
        await("Missing search field"){tree().any {it.isEditable}}
        assertTrue(tree().first {it.isEditable}.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,value)
        }))
        inst.waitForIdleSync()
    }
    private fun scrollTo(label:String) {
        repeat(25) {
            val snapshot=tree()
            val matches=snapshot.filter {it.text?.toString()==label || it.contentDescription?.toString()==label}
            fun ancestorScroller(node:AccessibilityNodeInfo):AccessibilityNodeInfo? {
                var parent=node.parent
                while(parent!=null) {
                    if(parent.isScrollable)return parent
                    parent=parent.parent
                }
                return null
            }
            val scroller=matches.firstNotNullOfOrNull(::ancestorScroller) ?: snapshot.firstOrNull {node->
                node.isScrollable && node.actionList.any {it.id==AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN.id || it.id==AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP.id}
            }
            // A fitting LazyColumn has no scroll capability. Its visible action still needs
            // to be wholly within the window; absence of scrolling is not an app failure.
            val viewport=Rect().also {(scroller ?: automation.rootInActiveWindow)?.getBoundsInScreen(it)}
            fun actionNode(node:AccessibilityNodeInfo):AccessibilityNodeInfo {
                var parent:AccessibilityNodeInfo?=node
                while(parent!=null && !parent.isScrollable) {
                    if(parent.isClickable)return parent
                    parent=parent.parent
                }
                return node
            }
            val target=matches.firstOrNull {node->
                val action=actionNode(node)
                val bounds=Rect();action.getBoundsInScreen(bounds)
                action.isVisibleToUser && action.isEnabled && !bounds.isEmpty && viewport.contains(bounds)
            }
            if(target!=null)return
            val bounds=matches.firstOrNull()?.let {Rect().also(actionNode(it)::getBoundsInScreen)}
            val direction=if(bounds!=null && bounds.top<viewport.top)AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD else AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            scroller?.performAction(direction)
            Thread.sleep(250);inst.waitForIdleSync()
        }
        // Retain the app frame before ActivityScenario.use closes it during unwinding.
        runCatching {capture("discover-scroll-FAIL")}
        fail("Unreachable $label")
    }
    @android.annotation.SuppressLint("NewApi")
    private fun export(name:String,bytes:ByteArray) {
        shell("mkdir -p /data/local/tmp/goldberg-discover")
        val pipes=automation.executeShellCommandRw("dd of=/data/local/tmp/goldberg-discover/$name")
        android.os.ParcelFileDescriptor.AutoCloseOutputStream(pipes[1]).use {it.write(bytes)}
        android.os.ParcelFileDescriptor.AutoCloseInputStream(pipes[0]).use {it.readBytes()}
        val read=automation.executeShellCommand("cat /data/local/tmp/goldberg-discover/$name").use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
        }
        assertArrayEquals(bytes,read)
    }
    private fun capture(name:String) {
        inst.waitForIdleSync();Thread.sleep(350)
        val bitmap=automation.takeScreenshot();assertNotNull(bitmap)
        val bytes=java.io.ByteArrayOutputStream();assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,bytes));bitmap.recycle()
        export("$name.png",bytes.toByteArray())
        val text="locale=${L.locale} font=${app.resources.configuration.fontScale}\n"+tree().joinToString("\n") {
            val r=Rect();it.getBoundsInScreen(r)
            "bounds=$r text=${it.text} description=${it.contentDescription} clickable=${it.isClickable} enabled=${it.isEnabled} scrollable=${it.isScrollable} actions=${it.actionList.map {a->a.id}}"
        }
        export("$name.txt",text.toByteArray())
    }
    private fun fixture(name:String)=inst.context.assets.open("discover-repair/$name.json").use {it.readBytes()}

    @Test fun automaticSearchAndEditorialJourneysWithPreservedEvidence()=runBlocking {
        check(android.os.Build.VERSION.SDK_INT==36)
        check(android.os.Build.HARDWARE in setOf("ranchu","goldfish"))
        val originalConsent=app.catalogOnline.enabled.value
        val oldLocales=AppCompatDelegate.getApplicationLocales()
        val oldFont=shell("settings get system font_scale").trim()
        val calls=AtomicInteger();val cancelled=AtomicInteger()
        val available=AtomicBoolean(true);val invalid=AtomicBoolean(false)
        val adapter=object:ProviderAdapter by OpenOpusAdapter(){override val intervalMillis=0L}
        val cacheDirectory=File(app.cacheDir,"discover-editorial-test-cache")
        val responses=mapOf("Frédéric Chopin" to "chopin","Chopin" to "chopin","Ballade No. 1, Op. 23" to "ballade","Goldberg Variations" to "goldberg","Goldberg" to "goldberg","Johann Sebastian Bach" to "bach","Mozart" to "mozart")
        val client=ProviderClient(ProviderTransport {request->
            check(app.catalogOnline.enabled.value && available.get())
            calls.incrementAndGet()
            val query=URLDecoder.decode(request.url.substringAfter("/omnisearch/").substringBeforeLast('/'),"UTF-8")
            if(query=="obsolete")try {awaitCancellation()} finally {cancelled.incrementAndGet()}
            if(invalid.get())ProviderResponse(200,"{}".toByteArray())
            else ProviderResponse(200,fixture(responses[query] ?: "empty"),mapOf("Cache-Control" to "max-age=0"))
        },{app.catalogOnline.enabled.value && available.get()},cache=FileProviderCache(cacheDirectory))
        val repository=DiscoverRepository(app.localCatalogue,client,{app.catalogOnline.enabled.value && available.get()},adapters=listOf(adapter))
        var evidenceBefore=emptyList<String>()
        try {
            app.catalogOnline.setEnabled(false)
            inst.runOnMainSync {AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"));L.refresh("en")}
            lateinit var nav:NavHostController
            var pieceId:Long?=null
            val replay=mutableStateOf(false)
            val theme=mutableStateOf("Light")
            ActivityScenario.launch(MainActivity::class.java).use {scenario->
                scenario.onActivity {activity->
                    val catalog=CatalogViewModel(app.catalogRepository,SavedStateHandle())
                    val experience=ExperienceViewModel(app.achievements,app.recordings)
                    val model=OpusViewModel(app.repository,SavedStateHandle(),app.practice,app.recording,app)
                    activity.viewModelStore.put("discover-catalog",catalog)
                    activity.viewModelStore.put("discover-experience",experience)
                    activity.viewModelStore.put("discover-model",model)
                    activity.setContent {
                        OpusTheme(theme.value) {
                            Stage8ScaffoldWindowAppearance(activity)
                            Scaffold(bottomBar={JournalNavigationBar(stage8NavigationItems(),"DISCOVER",{})}) {padding->
                                Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
                                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top+WindowInsetsSides.Horizontal))) {
                                    val controller=rememberNavController();SideEffect {nav=controller}
                                    NavHost(controller,startDestination="discover") {
                                        composable("discover") {DiscoverScreen({controller.navigate("detail/$it")},{pieceId=it;controller.navigate("piece")},{},LibraryState(loading=false),repository)}
                                        composable("detail/{id}") {entry->
                                            val id=entry.arguments!!.getString("id")!!
                                            val back:()->Unit={controller.popBackStack();Unit}
                                            val open:(String)->Unit={controller.navigate("detail/$it")}
                                            val piece:(Long)->Unit={pieceId=it;controller.navigate("piece")}
                                            if(replay.value)com.opus.piano.ui.rc.DiscoverDetailScreen(id,back,open,piece)
                                            else DiscoverDetailScreen(id,back,open,piece)
                                        }
                                        composable("piece") {
                                            val library by model.library.collectAsState()
                                            CompositionScreen(null,pieceId,catalog,experience,library,model,{controller.popBackStack()},{},{},{_,_->},{})
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // Actual edit with consent off: no request. Enabling the persisted setting
                // triggers the same typed query automatically, without a per-query button.
                enter("Goldberg")
                Thread.sleep(650);assertEquals(0,calls.get())
                app.catalogOnline.setEnabled(true)
                await("Automatic online query never started"){calls.get()==1}
                await("Goldberg work not shown"){tree().any {it.text?.contains("Goldberg Variations")==true}}
                capture("en-light-auto-goldberg")
                assertFalse(has(L.s(R.string.stage7a_search_online)))
                assertFalse(has(L.s(R.string.discover_clear_cache)))
                click(L.s(R.string.discover_advanced))
                capture("en-light-search-advanced")
                val beforeHistoryChange=calls.get()
                scrollTo(L.s(R.string.discover_history))
                click(L.s(R.string.discover_history));Thread.sleep(650)
                click(L.s(R.string.discover_history));Thread.sleep(650)
                assertEquals("History preferences must not refetch a query",beforeHistoryChange,calls.get())
                repeat(3) {tree().firstOrNull {it.isScrollable}?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);Thread.sleep(150)}
                click(L.s(R.string.discover_advanced))
                // Fast edits never dispatch the intermediate empty/one-character queries.
                val before=calls.get()
                enter("");Thread.sleep(100);enter("x");Thread.sleep(100);enter("Mozart")
                await("Debounced Mozart missing"){calls.get()>before}
                assertEquals(before+1,calls.get())
                // Cancel a request that is already in flight, then display the new query.
                enter("obsolete");await("Slow request did not start"){calls.get()==before+2}
                click(L.s(R.string.discover_artists));Thread.sleep(100)
                click(L.s(R.string.discover_all))
                enter("Chopin");await("Obsolete request was not cancelled"){cancelled.get()==1}
                await("Composer missing"){has("Frédéric Chopin")}
                capture("en-light-auto-chopin")
                // Every required public query runs through native parsing/intake/search.
                for((name,query) in listOf("chopin" to "Frédéric Chopin","ballade" to "Ballade No. 1, Op. 23","goldberg" to "Goldberg Variations","bach" to "Johann Sebastian Bach","mozart" to "Mozart")) {
                    val count=calls.get();enter(query);await("No provider response for $query"){calls.get()>count}
                    Thread.sleep(250)
                    val page=repository.automaticSearch(query,SearchFilter(),"en",false).let {flow->
                        val all=mutableListOf<DiscoverSearch>();flow.collect {all+=it};all.last().page
                    }
                    assertTrue("No local results for $query",page.items.isNotEmpty())
                    export("query-$name.json",JSONObject().put("query",query).put("items",JSONArray(page.items.map {JSONObject().put("id",it.id).put("kind",it.kind).put("title",it.title)})).toString(2).toByteArray())
                    capture("en-light-query-$name")
                }
                val goldberg=(repository.automaticSearch("Goldberg Variations",SearchFilter("works"),"en",false).let {flow->
                    var last:DiscoverSearch?=null;flow.collect {last=it};last!!.page.items
                }).first {it.title.contains("Goldberg",true)}
                val detail=(repository.detail(goldberg.id) as CatalogueAccess.Available).value!!
                assertTrue(detail.relationships.any {it.first.role=="composer" && it.second.title=="Johann Sebastian Bach"})
                evidenceBefore=detail.assertions.map {it.assertion.toString()+it.source.toString()+it.rights.toString()}
                // Provider failure preserves the already ingested catalogue and readable UI.
                invalid.set(true);enter("Goldberg")
                await("Provider error not shown"){has(L.s(R.string.discover_source_warning))}
                capture("en-light-provider-failure")
                val networkCalls=calls.get();available.set(false);enter("Goldberg ")
                await("Connection state missing"){has(L.s(R.string.discover_connection_unavailable))}
                assertEquals(networkCalls,calls.get());capture("en-light-offline")
                available.set(true);invalid.set(false)
                inst.runOnMainSync {nav.navigate("detail/${goldberg.id}")}
                await("Missing primary action"){has(L.s(R.string.discover_add_work))}
                capture("en-light-detail-after")
                val composer=detail.relationships.first {it.first.role=="composer"}.second
                click(composer.title)
                await("Composer detail did not open"){has(L.s(R.string.discover_composer))}
                capture("en-light-composer-detail")
                scrollTo(L.s(R.string.discover_sources));click(L.s(R.string.discover_sources))
                capture("en-light-composer-credits")
                val composerDetail=(repository.detail(composer.id) as CatalogueAccess.Available).value!!
                val duplicate=CataloguePresentation.groups(composerDetail.assertions).first {it.evidence.size>1}
                val countLabel=L.s(R.string.discover_evidence_count,duplicate.evidence.size)
                scrollTo(countLabel);click(countLabel)
                val dateLabel=CataloguePresentation.retrievedLabel(duplicate.evidence.first().source.retrievedAt,L.locale)
                scrollTo(dateLabel)
                scrollTo(L.s(R.string.discover_license_cc0)+" · "+duplicate.evidence.first().rights.attribution)
                capture("en-light-composer-evidence-expanded")
                assertTrue(tree().any {it.text?.contains(L.s(R.string.discover_license_cc0))==true})
                val afterCredits=(repository.detail(composer.id) as CatalogueAccess.Available).value!!
                assertEquals(composerDetail.assertions,afterCredits.assertions)
                inst.runOnMainSync {nav.popBackStack()}
                await("Composer Back failed"){has(L.s(R.string.discover_add_work))}
                inst.runOnMainSync {replay.value=true}
                Thread.sleep(500);capture("en-light-detail-before-rc-layout")
                inst.runOnMainSync {replay.value=false}
                Thread.sleep(500)
                click(L.s(R.string.discover_add_work))
                await("Add did not navigate to Library composition"){pieceId!=null && has(L.s(R.string.select_in_practice))}
                capture("en-light-library-composition")
                assertFalse(has(L.s(R.string.msg_no_verified_movement_list_is_supplied_by_the_d571)))
                scrollTo(L.s(R.string.msg_sources_study_bfcb));click(L.s(R.string.msg_sources_study_bfcb))
                await("Study explanation missing"){has(L.s(R.string.msg_no_verified_movement_list_is_supplied_by_the_d571))}
                capture("en-light-library-study-expanded")
                val saved=app.database.dao().piece(pieceId!!)!!
                assertEquals(detail.entity.title,saved.title);assertTrue(saved.composer.contains("Johann Sebastian Bach"))
                inst.runOnMainSync {nav.popBackStack()}
                await("Detail Back failed"){has(L.s(R.string.discover_open_existing))}
                val after=(repository.detail(goldberg.id) as CatalogueAccess.Available).value!!
                assertEquals(evidenceBefore,after.assertions.map {it.assertion.toString()+it.source.toString()+it.rights.toString()})
                export("assertion-preservation.json",JSONObject().put("before",evidenceBefore.size).put("after",after.assertions.size).put("unchanged",true).toString().toByteArray())
            }
            // The shared catalogue also contains preserved, unreviewed journal snapshots.
            // This provider-metadata assertion must target the recorded provider identity,
            // not whichever similarly titled local work ranks first.
            val fixtureWork=JSONObject(fixture("goldberg").toString(Charsets.UTF_8)).getJSONArray("results")
            val external=(0 until fixtureWork.length()).map {fixtureWork.getJSONObject(it)}
                .first {it.optJSONObject("work")!=null}.getJSONObject("work").getString("id")
            val canonical=(app.localCatalogue.access {it.providerIdentity("openopus","work",external)} as CatalogueAccess.Available).value!!
            val work=(repository.detail(canonical.entityId) as CatalogueAccess.Available).value!!.entity
            for((size,pixels) in listOf("standard" to "824x1830","small" to "720x1280")) {
                shell("wm size $pixels");shell("wm density 320")
                for((locale,mode,font) in listOf(Triple("en","Light","1.0"),Triple("en","Dark","2.0"),Triple("cs","Light","2.0"),Triple("cs","Dark","1.0"))) {
                    shell("settings put system font_scale $font")
                    inst.runOnMainSync {AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale));L.refresh(locale)}
                    ActivityScenario.launch(MainActivity::class.java).use {scenario->
                        scenario.onActivity {activity->activity.setContent {
                            OpusTheme(mode) {
                                Stage8ScaffoldWindowAppearance(activity)
                                Scaffold(bottomBar={JournalNavigationBar(stage8NavigationItems(),"DISCOVER",{})}) {padding->
                                    Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
                                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top+WindowInsetsSides.Horizontal))) {
                                        DiscoverDetailScreen(work.id,{},{},{})
                                    }
                                }
                            }
                        }}
                        await("Matrix detail missing"){has(work.title)}
                        await("Verified genre not localized"){has(L.s(R.string.genre_keyboard))}
                        assertEquals(font.toFloat(),app.resources.configuration.fontScale,0.01f)
                        assertEquals(locale,L.locale.language)
                        capture("$size-$locale-$mode-f$font-detail")
                        scrollTo(L.s(R.string.discover_open_existing))
                        capture("$size-$locale-$mode-f$font-action")
                        scrollTo(L.s(R.string.discover_sources));click(L.s(R.string.discover_sources))
                        capture("$size-$locale-$mode-f$font-credits")
                    }
                }
            }
        } catch(failure:Throwable) {
            runCatching {capture("discover-FAIL")}
            throw failure
        } finally {
            runCatching {export("request-counts.json",JSONObject().put("calls",calls.get()).put("cancelled",cancelled.get()).toString().toByteArray())}
            app.catalogOnline.setEnabled(originalConsent)
            shell("settings put system font_scale $oldFont")
            shell("wm size reset");shell("wm density reset")
            inst.runOnMainSync {L.clearOverride();AppCompatDelegate.setApplicationLocales(oldLocales)}
            cacheDirectory.deleteRecursively()
        }
    }
}
