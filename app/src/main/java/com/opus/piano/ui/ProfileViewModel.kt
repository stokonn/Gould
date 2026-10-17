package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import android.app.Application
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.opus.piano.OpusApplication
import com.opus.piano.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.ByteArrayOutputStream

class ProfileViewModel(app: Application) : AndroidViewModel(app) {
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
    val preview = MutableStateFlow<String?>(null)
    private var pending: String? = null
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
        db.withTransaction { val old = db.personalDao().getSettings() ?: ProfileSettings(); db.personalDao().saveSettings(old.copy(avatar = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP))) }
    }
    fun export(uri: Uri) = action {
        val raw = backup.export()
        getApplication<Application>().contentResolver.openOutputStream(uri, "wt")?.use { it.write(raw.toByteArray()) } ?: error(L.s(R.string.msg_cannot_write_this_file_151a))
        notice.value = L.s(R.string.msg_backup_exported_6f92)
    }
    fun inspect(uri: Uri) = action {
        pending = null; preview.value = null
        val raw = getApplication<Application>().contentResolver.openInputStream(uri)?.use { it.readBytesLimited(BackupRepository.MAX_BYTES).toString(Charsets.UTF_8) } ?: error(L.s(R.string.msg_cannot_read_this_file_8312))
        val summary = backup.validate(raw)
        pending = raw; preview.value = summary
    }
    fun cancelRestore() { if (!busy.value) { preview.value = null; pending = null } }
    fun restore(done: () -> Unit) = action {
        val raw = pending ?: error(L.s(R.string.msg_select_a_backup_again_ca8b))
        (getApplication<Application>() as OpusApplication).practice.replacing {
            (getApplication<Application>() as OpusApplication).catalogRepository.restoreJournal { backup.restore(raw) }
        }; pending = null; preview.value = null; notice.value = L.s(R.string.msg_backup_restored_52ad)
        withContext(Dispatchers.Main) { done() }
    }
}
private fun java.io.InputStream.readBytesLimited(limit: Int): ByteArray {
    val out = ByteArrayOutputStream(); val buffer = ByteArray(8192)
    while (true) { val count = read(buffer); if (count < 0) break; require(out.size() + count <= limit) { L.s(R.string.msg_file_is_too_large_3801) }; out.write(buffer, 0, count) }
    return out.toByteArray()
}
