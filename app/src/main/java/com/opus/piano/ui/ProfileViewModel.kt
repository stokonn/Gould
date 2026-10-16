package com.opus.piano.ui

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
    private val backup = BackupRepository(app, db)
    val settings = db.personalDao().settings().map { it ?: ProfileSettings() }.stateIn(viewModelScope, SharingStarted.Eagerly, ProfileSettings())
    val notice = MutableStateFlow<String?>(null)
    val busy = MutableStateFlow(false)
    val preview = MutableStateFlow<String?>(null)
    private var pending: String? = null
    private fun action(block: suspend () -> Unit) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch { try { withContext(Dispatchers.IO) { block() } }
            catch (c: CancellationException) { throw c }
            catch (e: Exception) { notice.value = "Operation failed. Existing data is preserved. ${e.message.orEmpty().take(160)}" }
            finally { busy.value = false }
        }
    }
    private fun edit(change: (ProfileSettings) -> ProfileSettings) = action {
        db.withTransaction { db.personalDao().saveSettings(change(db.personalDao().getSettings() ?: ProfileSettings())) }
    }
    fun username(value: String) = edit { it.copy(username = value.trim().take(80).ifBlank { "Musician" }) }
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
        notice.value = "Disposable cache cleared. Your journal and saved catalogue are preserved."
    }
    fun clearAvatar() = edit { it.copy(avatar = "") }
    fun avatar(uri: Uri) = action {
        val resolver = getApplication<Application>().contentResolver
        val bytes = resolver.openInputStream(uri)?.use { it.readBytesLimited(16 * 1024 * 1024) } ?: error("Image unavailable.")
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Unsupported image." }
        val options = BitmapFactory.Options().apply { inSampleSize = 1; while (maxOf(bounds.outWidth, bounds.outHeight) / inSampleSize > 512) inSampleSize *= 2 }
        val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: error("Unsupported image.")
        val ratio = minOf(1f, 512f / maxOf(image.width, image.height))
        val scaled = Bitmap.createScaledBitmap(image, (image.width * ratio).toInt().coerceAtLeast(1), (image.height * ratio).toInt().coerceAtLeast(1), true)
        val output = ByteArrayOutputStream(); scaled.compress(Bitmap.CompressFormat.JPEG, 88, output)
        if (scaled !== image) image.recycle(); scaled.recycle()
        db.withTransaction { val old = db.personalDao().getSettings() ?: ProfileSettings(); db.personalDao().saveSettings(old.copy(avatar = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP))) }
    }
    fun export(uri: Uri) = action {
        val raw = backup.export()
        getApplication<Application>().contentResolver.openOutputStream(uri, "wt")?.use { it.write(raw.toByteArray()) } ?: error("Cannot write this file.")
        notice.value = "Backup exported."
    }
    fun inspect(uri: Uri) = action {
        pending = null; preview.value = null
        val raw = getApplication<Application>().contentResolver.openInputStream(uri)?.use { it.readBytesLimited(BackupRepository.MAX_BYTES).toString(Charsets.UTF_8) } ?: error("Cannot read this file.")
        val summary = backup.validate(raw)
        pending = raw; preview.value = summary
    }
    fun cancelRestore() { if (!busy.value) { preview.value = null; pending = null } }
    fun restore(done: () -> Unit) = action {
        val raw = pending ?: error("Select a backup again.")
        (getApplication<Application>() as OpusApplication).catalogRepository.restoreJournal { backup.restore(raw) }; pending = null; preview.value = null; notice.value = "Backup restored."
        withContext(Dispatchers.Main) { done() }
    }
}
private fun java.io.InputStream.readBytesLimited(limit: Int): ByteArray {
    val out = ByteArrayOutputStream(); val buffer = ByteArray(8192)
    while (true) { val count = read(buffer); if (count < 0) break; require(out.size() + count <= limit) { "File is too large." }; out.write(buffer, 0, count) }
    return out.toByteArray()
}
