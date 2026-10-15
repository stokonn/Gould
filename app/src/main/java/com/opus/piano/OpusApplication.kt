package com.opus.piano

import android.app.Application
import androidx.room.Room
import com.opus.piano.data.*

class OpusApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(this, OpusDatabase::class.java, "opus.db")
            .addMigrations(OpusDatabase.MIGRATION_1_2, OpusDatabase.MIGRATION_2_3, OpusDatabase.MIGRATION_3_4).build()
    }
    val achievements by lazy { AchievementStore(database) }
    val recordings by lazy { RecordingRepository(database) }
    val repository by lazy { OpusRepository(database, achievements) }
    val catalogRepository by lazy { CatalogRepository(database, filesDir, achievements = achievements) }
}
