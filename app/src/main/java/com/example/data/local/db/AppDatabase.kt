package com.example.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.model.PrayerRecord
import com.example.data.local.model.PrayerOffset
import com.example.data.local.model.QadaTally

@Database(
    entities = [PrayerRecord::class, PrayerOffset::class, QadaTally::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun prayerDao(): PrayerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "imad_salah_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
