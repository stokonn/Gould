package com.opus.piano

import android.content.Context
import android.content.ContextWrapper
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.opus.piano.data.OpusDatabase
import com.opus.piano.identity.decodeGoogleAvatar
import com.opus.piano.identity.GoogleIdentity
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Local persistence policy, not a simulated claim of Google authentication. */
@RunWith(AndroidJUnit4::class)
class GoogleIdentityPersistenceTest {
    @Test fun googleAvatarDecodePreservesSmallImagesAndBoundsLargeImages() {
        for ((width, height) in listOf(16 to 16, 1024 to 768)) {
            val source = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            val output = java.io.ByteArrayOutputStream()
            try { assertTrue(source.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output)) }
            finally { source.recycle() }
            val decoded = requireNotNull(decodeGoogleAvatar(output.toByteArray()))
            try {
                assertEquals(if (width > 512) 512 else width, decoded.width)
                assertEquals(if (height > 512) 384 else height, decoded.height)
            } finally { decoded.recycle() }
        }
    }

    @Test fun manualAvatarChoiceSurvivesIdentityRepositoryRestart() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "identity-test-${System.nanoTime()}"
        val context = object : ContextWrapper(base) {
            override fun getSharedPreferences(ignored: String, mode: Int) = base.getSharedPreferences(name, mode)
        }
        val db = Room.inMemoryDatabaseBuilder(base, OpusDatabase::class.java).build()
        try {
            val identity = GoogleIdentity(context, db)
            assertNull(identity.account.value)
            assertTrue(identity.mayImportAvatar(""))
            assertFalse(identity.mayImportAvatar("existing local image"))
            identity.manualAvatar()
            val restarted = GoogleIdentity(context, db)
            assertFalse(restarted.mayImportAvatar(""))
            assertNull(restarted.account.value)
        } finally { db.close(); base.deleteSharedPreferences(name) }
    }
}
