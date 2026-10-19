package com.opus.piano

import androidx.lifecycle.*
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import com.opus.piano.ui.CatalogViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class RepertoireSearchPrivacyTest {
    @Test fun privateSavedFilteringAndClearingNeverCallsHttpOrChangesOnlineQueryState()=runBlocking {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val context=instrumentation.targetContext
        val db=Room.inMemoryDatabaseBuilder(context,OpusDatabase::class.java).addCallback(PersonalCatalogueBridge.callback).build()
        val requests=AtomicInteger()
        val http=object:CatalogHttp() {
            override suspend fun bytes(url:String,image:Boolean):ByteArray {requests.incrementAndGet();error("Private local query reached HTTP")}
        }
        val store=ViewModelStore()
        val saved=SavedStateHandle(mapOf("catalogQuery" to "Authored public catalogue query"))
        try {
            db.catalogDao().putComposers(listOf(CatalogComposer("123","Authored cached composer")))
            db.catalogDao().putWorks(listOf(CatalogWork("456","123","Private rehearsal draft")))
            val repository=CatalogRepository(db,context.filesDir,http=http)
            val factory=object:ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST") override fun <T:ViewModel> create(modelClass:Class<T>):T=CatalogViewModel(repository,saved) as T
            }
            val model=ViewModelProvider(store,factory)[CatalogViewModel::class.java]
            model.filterSaved("Private rehearsal")
            assertEquals("456",withTimeout(5000){model.savedResults.first {it.works.size==1}}.works.single().id)
            model.filterSaved("")
            assertEquals("123",withTimeout(5000){model.savedResults.first {it.works.isEmpty() && it.composers.size==1}}.composers.single().id)
            // Exceeds the legacy350ms online debounce; a leaked dispatch would be observable.
            delay(750)
            assertEquals(0,requests.get())
            assertEquals("Authored public catalogue query",saved.get<String>("catalogQuery"))
            assertEquals("Authored public catalogue query",model.browse.value.query)
            assertEquals("",model.browse.value.activeQuery)
        }finally {instrumentation.runOnMainSync {store.clear()};db.close()}
    }
}
