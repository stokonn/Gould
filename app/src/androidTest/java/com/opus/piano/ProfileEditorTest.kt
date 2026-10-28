package com.opus.piano

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.*
import com.opus.piano.ui.ProfileViewModel
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileEditorTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val app = instrumentation.targetContext.applicationContext as OpusApplication
    private fun onMain(action: () -> Unit) = instrumentation.runOnMainSync(action)
    private suspend fun idle(model: ProfileViewModel) {
        withTimeout(10_000) { while (model.busy.value || model.draftAvatarBusy.value) delay(20) }
        instrumentation.waitForIdleSync()
    }
    private suspend fun fixture(test: suspend (ProfileViewModel, SavedStateHandle) -> Unit) {
        val previous = app.database.personalDao().getSettings()
        app.database.personalDao().saveSettings(ProfileSettings(username = "Editor test", favoriteEra = "Custom historical era"))
        val store = ViewModelStore()
        val draftDirectory = java.io.File(app.cacheDir, "profile-editor-test-${java.util.UUID.randomUUID()}")
        lateinit var model: ProfileViewModel
        lateinit var state: SavedStateHandle
        onMain {
            state = SavedStateHandle()
            model = ViewModelProvider(store, object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(app, state, draftDirectory) as T
            })[ProfileViewModel::class.java]
        }
        withTimeout(10_000) { while (!model.draftLoaded.value) delay(20) }
        try { test(model, state) } finally {
            onMain { store.clear() }
            app.database.personalDao().saveSettings(previous ?: ProfileSettings())
            draftDirectory.deleteRecursively()
        }
    }
    @Test fun saveAcknowledgesOnlyPersistedCompleteProfileAndRetainsOtherSettings() = runBlocking { fixture { model, _ ->
        onMain { model.beginProfileEdit() }; idle(model)
        onMain { model.changeProfileDraft { it.copy(name = "Confirmed", artist = "Dvořák", biography = "All fields saved", era = "Baroque") } }
        val old = app.database.personalDao().getSettings()!!
        app.database.personalDao().saveSettings(old.copy(theme = "Dark"))
        onMain { model.saveProfileEdit() }; idle(model)
        val saved = app.database.personalDao().getSettings()!!
        assertEquals("Confirmed", saved.username); assertEquals("Dvořák", saved.favoriteArtist)
        assertEquals("All fields saved", saved.biography); assertEquals("Baroque", saved.favoriteEra)
        assertEquals("Dark", saved.theme); assertNull(model.editError.value)
        assertNull(model.editDraft.value)
    } }
    @Test fun conflictingWriteKeepsDraftAndNeverReportsSuccess() = runBlocking { fixture { model, _ ->
        onMain { model.beginProfileEdit() }; idle(model)
        onMain { model.changeProfileDraft { it.copy(name = "Draft name") } }
        val old = app.database.personalDao().getSettings()!!
        app.database.personalDao().saveSettings(old.copy(username = "Newer name"))
        onMain { model.saveProfileEdit() }; idle(model)
        assertEquals("Newer name", app.database.personalDao().getSettings()!!.username)
        assertEquals("Draft name", model.editDraft.value!!.value.name)
        assertNotNull(model.editError.value); assertNull(model.notice.value)
    } }
    @Test fun discardLeavesProfileUnchangedAndDraftIsBundleCompatible() = runBlocking { fixture { model, state ->
        onMain { model.beginProfileEdit() }; idle(model)
        onMain { model.changeProfileDraft { it.copy(biography = "Unsaved Czech: příliš žluťoučký") } }
        val before = app.database.personalDao().getSettings()
        lateinit var restored: ProfileEditDraft
        onMain {
            val bundle = android.os.Bundle().apply { putStringArrayList("draft", state["profile.edit.v1"]) }
            val parcel = android.os.Parcel.obtain()
            try {
                parcel.writeBundle(bundle); parcel.setDataPosition(0)
                restored = ProfileEditDraft.restore(requireNotNull(parcel.readBundle()!!.getStringArrayList("draft")))!!
            } finally { parcel.recycle() }
            model.discardProfileEdit()
        }
        idle(model)
        assertTrue(restored.dirty); assertEquals("Unsaved Czech: příliš žluťoučký", restored.value.biography)
        assertEquals(before, app.database.personalDao().getSettings()); assertNull(model.editDraft.value)
    } }
    @Test fun coldEditorWithoutSavedBundleRestoresAtomicDiskDraftAndDoesNotWriteProfile() = runBlocking {
        val directory = java.io.File(app.cacheDir, "profile-cold-test-${java.util.UUID.randomUUID()}")
        val before = app.database.personalDao().getSettings()
        val stores = listOf(ViewModelStore(), ViewModelStore())
        fun create(index: Int): ProfileViewModel {
            lateinit var model: ProfileViewModel
            onMain { model = ViewModelProvider(stores[index], object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(app, SavedStateHandle(), directory) as T
            })[ProfileViewModel::class.java] }
            return model
        }
        try {
            val first = create(0)
            withTimeout(10_000) { while (!first.draftLoaded.value) delay(20) }
            onMain { first.beginProfileEdit() }; idle(first)
            onMain { first.changeProfileDraft { it.copy(biography = "Retained across a fresh task") } }
            withTimeout(10_000) { while (!withContext(Dispatchers.IO) {
                runCatching { java.io.File(directory, "editor-v1.json").readText().contains("Retained across a fresh task") }.getOrDefault(false)
            }) delay(20) }
            onMain { stores[0].clear() }
            val second = create(1)
            withTimeout(10_000) { while (!second.draftLoaded.value || second.editDraft.value == null) delay(20) }
            assertEquals("Retained across a fresh task", second.editDraft.value!!.value.biography)
            assertEquals(before, app.database.personalDao().getSettings())
            onMain { second.discardProfileEdit() }; idle(second)
            assertFalse(java.io.File(directory, "editor-v1.json").exists())
        } finally { onMain { stores.forEach { it.clear() } }; directory.deleteRecursively() }
    }
    @Test fun corruptPersistedDraftIsRetainedAndCannotBeSilentlyReplaced() = runBlocking {
        val directory = java.io.File(app.cacheDir, "profile-corrupt-test-${java.util.UUID.randomUUID()}").apply { mkdirs() }
        val original = java.io.File(directory, "editor-v1.json").apply { writeText("original unreadable draft bytes") }
        val bytes = original.readBytes(); val store = ViewModelStore()
        lateinit var model: ProfileViewModel
        try {
            onMain { model = ViewModelProvider(store, object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(app, SavedStateHandle(), directory) as T
            })[ProfileViewModel::class.java] }
            withTimeout(10_000) { while (!model.draftLoadError.value) delay(20) }
            onMain { model.beginProfileEdit() }
            assertNull(model.editDraft.value); assertFalse(model.draftLoaded.value)
            assertArrayEquals(bytes, original.readBytes())
        } finally { onMain { store.clear() }; directory.deleteRecursively() }
    }
}
