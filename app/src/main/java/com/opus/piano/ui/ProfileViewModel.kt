package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import android.app.Application
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.opus.piano.OpusApplication
import com.opus.piano.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.ByteArrayOutputStream

class ProfileViewModel(app: Application, private val savedState: SavedStateHandle,
    private val draftDirectory: java.io.File = java.io.File(app.filesDir, "profile-drafts")) : AndroidViewModel(app) {
    private val db = (app as OpusApplication).database
    private val identity = (app as OpusApplication).identity
    val googleAccount = identity.account
    val favoriteComposers=db.journalEditsDao().favorites().stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
    fun favoriteComposer(id:String,selected:Boolean) {viewModelScope.launch {try {JournalEdits(db).favorite(id,selected)} catch(c:CancellationException) {throw c} catch(_:Exception) {notice.value=L.s(R.string.ui010_favorite_failed)}}}
    fun favoriteArtist(value:String)=edit {it.copy(favoriteArtist=value.trim().take(120))}
    fun googleSignIn(context: android.content.Context) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            try {
                identity.signIn(context)
                // A photo failure must not undo a successful account selection.
                try { identity.importPhoto { (getApplication<Application>() as OpusApplication).media.allowed() } }
                catch (e: CancellationException) { throw e }
                catch (_: Exception) { /* Keep successful identity when optional picture is unavailable. */ }
                notice.value = L.s(R.string.google_signed_in)
            } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                notice.value = L.s(R.string.google_cancelled)
            } catch (e: androidx.credentials.exceptions.NoCredentialException) {
                notice.value = L.s(R.string.google_no_credential)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { notice.value = L.s(R.string.google_failed) }
            finally { busy.value = false }
        }
    }
    fun googleSignOut() {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            try { identity.signOut() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { notice.value = L.s(R.string.google_failed) }
            finally { busy.value = false }
        }
    }
    fun useGoogleName()=action {identity.useGoogleName()}
    fun replaceGooglePhoto()=action {check((getApplication<Application>() as OpusApplication).media.allowed());check(identity.photo()!=null);identity.importPhoto(replaceExisting=true) {(getApplication<Application>() as OpusApplication).media.allowed()}}
    fun refreshGooglePhoto() = action { identity.importPhoto { (getApplication<Application>() as OpusApplication).media.allowed() } }
    private val backup = BackupRepository(app, db)
    val settings = db.personalDao().settings().map { it ?: ProfileSettings() }.stateIn(viewModelScope, SharingStarted.Eagerly, ProfileSettings())
    val local = db.practiceDao().local().map { it ?: LocalAppState() }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    fun finishOnboarding() = action { db.withTransaction { db.practiceDao().local((db.practiceDao().localNow() ?: LocalAppState()).copy(onboardingDone = true)) } }
    fun smartPreference(enabled: Boolean) = edit { it.copy(smartEnabled = enabled) }
    fun smartSettings(sensitivity: Int, pause: Int) = edit { it.copy(sensitivity = sensitivity.coerceIn(0,100), pauseSeconds = pause.coerceIn(2,30)) }
    fun reminder(enabled: Boolean, hour: Int, minute: Int) = action { com.opus.piano.reminders.ReminderScheduler(getApplication()).configure(enabled,hour,minute) }
    fun csv(uri: Uri, parts: Boolean) = action {
        getApplication<Application>().contentResolver.openOutputStream(uri,"wt")?.bufferedWriter(Charsets.UTF_8)?.use { CsvExportRepository(db).export(it,parts) } ?: error(L.s(R.string.msg_cannot_write_export_0170))
        notice.value = L.s(R.string.msg_csv_exported_5f14)
    }
    fun curious() = action { db.experienceDao().unlock(listOf(BadgeUnlock("curious_pianist",System.currentTimeMillis()))) }
    val notice = MutableStateFlow<String?>(null)
    val busy = MutableStateFlow(false)
    private val draftKey = "profile.edit.v1"
    private fun draftNow() = savedState.get<ArrayList<String>>(draftKey)?.let(ProfileEditDraft::restore)
    val editDraft = savedState.getStateFlow<ArrayList<String>?>(draftKey, null)
        .map { it?.let(ProfileEditDraft::restore) }.stateIn(viewModelScope, SharingStarted.Eagerly, draftNow())
    val editError = MutableStateFlow<String?>(null)
    val draftAvatarBusy = MutableStateFlow(false)
    val draftLoaded = MutableStateFlow(false)
    val draftLoadError = MutableStateFlow(false)
    val draftRecoveryVisible = MutableStateFlow(false)
    private val draftStoreLock = Mutex()
    private val draftGeneration = java.util.concurrent.atomic.AtomicLong()
    private val draftFile get() = android.util.AtomicFile(java.io.File(draftDirectory, "editor-v1.json"))
    init { viewModelScope.launch { loadProfileDraft() } }
    private suspend fun loadProfileDraft() {
        try {
            val restored = withContext(Dispatchers.IO) { draftStoreLock.withLock {
                val exists = draftFile.baseFile.exists() || java.io.File(draftFile.baseFile.path + ".bak").exists()
                if (!exists) null else {
                    val raw = draftFile.openRead().use { it.readBytesLimited(64 * 1024) }.toString(Charsets.UTF_8)
                    val json = org.json.JSONObject(raw)
                    require(json.getInt("version") == 1)
                    val fields = json.getJSONArray("fields")
                    requireNotNull(ProfileEditDraft.restore((0 until fields.length()).map { fields.getString(it) }))
                }
            } }
            if (draftNow() == null && restored != null) savedState[draftKey] = restored.fields()
            draftLoadError.value = false
            draftLoaded.value = true
            draftNow()?.let(::retainDraft)
        } catch (c: CancellationException) { throw c }
        catch (_: Exception) { draftLoadError.value = true; draftLoaded.value = false; draftRecoveryVisible.value = true }
    }
    fun dismissDraftRecovery() { draftRecoveryVisible.value = false }
    fun retryProfileDraft() { viewModelScope.launch { loadProfileDraft() } }
    fun exportProfileDraft(uri: Uri) = action {
        draftStoreLock.withLock {
            getApplication<Application>().contentResolver.openOutputStream(uri, "wt")?.use { output ->
                draftFile.openRead().use { it.copyTo(output, 64 * 1024) }
            } ?: error(L.s(R.string.msg_cannot_write_export_0170))
        }
    }
    private fun writeDraft(draft: ProfileEditDraft?) {
        if (draft == null) {
            draftFile.delete()
            check(!draftFile.baseFile.exists() && !java.io.File(draftFile.baseFile.path + ".bak").exists())
            return
        }
        check(draftDirectory.isDirectory || draftDirectory.mkdirs())
        val output = draftFile.startWrite()
        try {
            output.write(org.json.JSONObject().put("version", 1).put("fields", org.json.JSONArray(draft.fields())).toString().toByteArray(Charsets.UTF_8))
            draftFile.finishWrite(output)
        } catch (error: Exception) { draftFile.failWrite(output); throw error }
    }
    private fun retainDraft(draft: ProfileEditDraft) {
        savedState[draftKey] = draft.fields()
        val generation = draftGeneration.incrementAndGet()
        viewModelScope.launch {
            try { withContext(Dispatchers.IO) { draftStoreLock.withLock {
                if (draftGeneration.get() == generation) writeDraft(draft)
            } } }
            catch (c: CancellationException) { throw c }
            catch (_: Exception) { editError.value = L.s(R.string.profile_draft_storage_failed) }
        }
    }
    private suspend fun clearPersistedDraft() = withContext(Dispatchers.IO) {
        draftStoreLock.withLock { draftGeneration.incrementAndGet(); writeDraft(null) }
    }
    private fun avatarHash(value: String) = java.security.MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    private fun avatarFile(name: String): java.io.File {
        require(name.matches(Regex("[a-f0-9-]+\\.jpg")))
        return java.io.File(draftDirectory, name)
    }
    fun beginProfileEdit() {
        if (draftLoadError.value) { draftRecoveryVisible.value = true; return }
        if (draftNow() != null || busy.value || !draftLoaded.value) return
        busy.value = true
        viewModelScope.launch {
            try {
                val draft = withContext(Dispatchers.IO) {
                    val original = db.personalDao().getSettings() ?: ProfileSettings()
                    val values = ProfileEditValues.from(original)
                    ProfileEditDraft(values, values, avatarHash(original.avatar))
                }
                retainDraft(draft)
                editError.value = null
            } catch (c: CancellationException) { throw c }
            catch (_: Exception) { notice.value = L.s(R.string.profile_edit_failed) }
            finally { busy.value = false }
        }
    }
    fun changeProfileDraft(change: (ProfileEditValues) -> ProfileEditValues) {
        if (busy.value || draftAvatarBusy.value) return
        draftNow()?.let { retainDraft(it.copy(value = change(it.value))) }
        editError.value = null
    }
    fun defaultDraftAvatar() {
        if (busy.value || draftAvatarBusy.value) return
        draftNow()?.let { retainDraft(it.copy(avatar = "default")) }
    }
    fun chooseDraftAvatar(uri: Uri) {
        if (busy.value || draftAvatarBusy.value || draftNow() == null) return
        draftAvatarBusy.value = true
        viewModelScope.launch {
            try {
                val name = withContext(Dispatchers.IO) {
                    val encoded = encodeAvatar(uri)
                    val file = avatarFile("${java.util.UUID.randomUUID()}.jpg")
                    check(file.parentFile!!.isDirectory || file.parentFile!!.mkdirs())
                    java.io.FileOutputStream(file).use { it.write(Base64.decode(encoded, Base64.NO_WRAP)); it.fd.sync() }
                    file.name
                }
                draftNow()?.let { retainDraft(it.copy(avatar = name)) }
            } catch (c: CancellationException) { throw c }
            catch (_: Exception) { editError.value = L.s(R.string.profile_edit_failed) }
            finally { draftAvatarBusy.value = false }
        }
    }
    suspend fun draftAvatar(draft: ProfileEditDraft): Bitmap? = withContext(Dispatchers.IO) {
        if (draft.avatar == "keep") return@withContext null
        if (draft.avatar == "default") null else BitmapFactory.decodeFile(avatarFile(draft.avatar).path)
    }
    fun discardProfileEdit() {
        if (busy.value || draftAvatarBusy.value) return
        val old = draftNow() ?: return
        busy.value = true
        viewModelScope.launch {
            try {
                clearPersistedDraft()
                savedState[draftKey] = null
                editError.value = null
                // Only private staged images from this editor, never the persisted avatar.
                if (old.avatar !in listOf("keep", "default")) withContext(Dispatchers.IO) { runCatching { avatarFile(old.avatar).delete() } }
            } catch (c: CancellationException) { throw c }
            catch (_: Exception) { editError.value = L.s(R.string.profile_draft_storage_failed) }
            finally { busy.value = false }
        }
    }
    fun saveProfileEdit() {
        if (busy.value || draftAvatarBusy.value) return
        val draft = draftNow() ?: return
        busy.value = true
        editError.value = null
        viewModelScope.launch {
            var committed = false
            try {
                withContext(Dispatchers.IO) {
                    val avatar = when (draft.avatar) {
                        "keep" -> null
                        "default" -> ""
                        else -> Base64.encodeToString(avatarFile(draft.avatar).readBytes(), Base64.NO_WRAP)
                    }
                    identity.editLocalProfile(draft.value.name != draft.original.name, avatar != null) {
                        db.withTransaction {
                            val current = db.personalDao().getSettings() ?: ProfileSettings()
                            val merged = mergeProfileEdit(current, draft.original, draft.value)
                            if (avatar != null && avatarHash(current.avatar) != draft.originalAvatarHash && current.avatar != avatar)
                                throw ProfileEditConflict()
                            db.personalDao().saveSettings(if (avatar == null) merged else merged.copy(avatar = avatar))
                        }
                    }
                }
                committed = true
                clearPersistedDraft()
                // No dismissal/"Saved" before the transaction really succeeded.
                savedState[draftKey] = null
                notice.value = L.s(R.string.profile_edit_saved)
                if (draft.avatar !in listOf("keep", "default")) withContext(Dispatchers.IO) { runCatching { avatarFile(draft.avatar).delete() } }
            } catch (c: CancellationException) { throw c }
            catch (_: ProfileEditConflict) { editError.value = L.s(R.string.profile_edit_conflict) }
            catch (_: Exception) { editError.value = L.s(if (committed) R.string.profile_saved_draft_retained else R.string.profile_edit_failed) }
            finally { busy.value = false }
        }
    }
    val preview = MutableStateFlow<String?>(null)
    private var pending: java.io.File? = null
    val restoreState=db.backupStateDao().observeState().stateIn(viewModelScope,SharingStarted.Eagerly,null)
    fun retryRestoreSettings()=backupAction {backup.replayPendingPreferences()}
    private fun backupAction(block:suspend()->Unit)=action {
        try {block()} catch(c:CancellationException){throw c} catch(e:Exception) {
            val message=if(db.backupStateDao().state()?.status=="pending") R.string.backup_v5_pending
                else if(e is java.io.IOException) R.string.backup_storage_failed else R.string.backup_validation_failed
            throw IllegalStateException(L.s(message),e)
        }
    }
    private fun action(block: suspend () -> Unit) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch { try { withContext(Dispatchers.IO) { block() } }
            catch (c: CancellationException) { throw c }
            catch (e: Exception) { notice.value = L.s(R.string.msg_operation_failed_please_retry_e_message_orem_bb69, (e.message.orEmpty().take(160))) }
            finally { busy.value = false }
        }
    }
    private fun edit(change: (ProfileSettings) -> ProfileSettings) = action {
        db.withTransaction { db.personalDao().saveSettings(change(db.personalDao().getSettings() ?: ProfileSettings())) }
    }
    fun personalization(artist: String, piece: String, era: String, bio: String) = edit {
        it.copy(favoriteArtist=artist.trim().take(120), favoritePiece=piece.trim().take(240), favoriteEra=era.trim().take(80), biography=bio.trim().take(400))
    }
    fun username(value: String) = edit { identity.manualName(); it.copy(username = value.trim().take(80).ifBlank { "Musician" }) }
    fun resetAppearance() = action {
        db.withTransaction { val old=db.personalDao().getSettings() ?: ProfileSettings();db.personalDao().saveSettings(old.copy(theme="System",typography="Editorial",statistics="time,pieces,sessions,streak")) }
        (getApplication<Application>() as OpusApplication).appearance.reset()
    }
    fun theme(value: String) = edit { require(value in listOf("System", "Light", "Dark")); it.copy(theme = value) }
    fun showcase(ids: List<String>) = action {
        db.withTransaction {
            val unlocked = db.experienceDao().allUnlocks().map { it.achievementId }.toSet()
            val old = db.personalDao().getSettings() ?: ProfileSettings()
            db.personalDao().saveSettings(old.copy(showcase = Showcase.select(ids, unlocked).joinToString(",")))
        }
    }
    fun typography(value: String) = edit { require(value in listOf("Editorial", "System")); it.copy(typography = value) }
    fun statistics(ids: List<String>) = edit { it.copy(statistics = StatisticLayout.normalize(ids).joinToString(",")) }
    val cacheUsage = MutableStateFlow<Long?>(null)
    fun measureCache() = action { cacheUsage.value = (getApplication<Application>() as OpusApplication).catalogRepository.cacheBytes() }
    fun clearCache() = action {
        val catalog = (getApplication<Application>() as OpusApplication).catalogRepository
        catalog.clearCache(); cacheUsage.value = catalog.cacheBytes()
        notice.value = L.s(R.string.msg_disposable_cache_cleared_your_journal_and_sa_18c6)
    }
    fun clearAvatar() = edit { identity.manualAvatar(); it.copy(avatar = "") }
    fun avatar(uri: Uri) = action {
        identity.manualAvatar()
        val encoded = encodeAvatar(uri)
        db.withTransaction { val old = db.personalDao().getSettings() ?: ProfileSettings(); db.personalDao().saveSettings(old.copy(avatar = encoded)) }
    }
    private fun encodeAvatar(uri: Uri): String {
        val resolver = getApplication<Application>().contentResolver
        val bytes = resolver.openInputStream(uri)?.use { it.readBytesLimited(16 * 1024 * 1024) } ?: error(L.s(R.string.msg_image_unavailable_ad4c))
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { L.s(R.string.msg_unsupported_image_80e4) }
        val options = BitmapFactory.Options().apply { inSampleSize = 1; while (maxOf(bounds.outWidth, bounds.outHeight) / inSampleSize > 512) inSampleSize *= 2 }
        val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: error(L.s(R.string.msg_unsupported_image_80e4))
        val ratio = minOf(1f, 512f / maxOf(image.width, image.height))
        val scaled = Bitmap.createScaledBitmap(image, (image.width * ratio).toInt().coerceAtLeast(1), (image.height * ratio).toInt().coerceAtLeast(1), true)
        val output = ByteArrayOutputStream(); scaled.compress(Bitmap.CompressFormat.JPEG, 88, output)
        if (scaled !== image) image.recycle(); scaled.recycle()
        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }
    fun export(uri: Uri) = backupAction {
        val file = backup.exportSnapshot()
        getApplication<Application>().contentResolver.openOutputStream(uri, "wt")?.use { out->file.inputStream().use {it.copyTo(out)} } ?: error(L.s(R.string.msg_cannot_write_this_file_151a))
        notice.value = L.s(R.string.msg_backup_exported_6f92)+" · "+L.s(R.string.studio_backup_audio_excluded)
    }
    fun inspect(uri: Uri) = backupAction {
        pending = null; preview.value = null
        val raw = getApplication<Application>().contentResolver.openInputStream(uri)?.use { backup.receive(it) } ?: error(L.s(R.string.msg_cannot_read_this_file_8312))
        val summary = backup.validate(raw)
        pending = raw; preview.value = summary
    }
    fun cancelRestore() { if (!busy.value) { preview.value = null; pending = null } }
    fun restore() = backupAction {
        val raw = pending ?: error(L.s(R.string.msg_select_a_backup_again_ca8b))
        (getApplication<Application>() as OpusApplication).practice.replacing(preservesRecovery=true) {
            (getApplication<Application>() as OpusApplication).catalogRepository.restoreJournal { backup.restore(raw) }
        }; pending = null; preview.value = null; notice.value = L.s(R.string.msg_backup_restored_52ad)
    }
}
private fun java.io.InputStream.readBytesLimited(limit: Int): ByteArray {
    val out = ByteArrayOutputStream(); val buffer = ByteArray(8192)
    while (true) { val count = read(buffer); if (count < 0) break; require(out.size() + count <= limit) { L.s(R.string.msg_file_is_too_large_3801) }; out.write(buffer, 0, count) }
    return out.toByteArray()
}
