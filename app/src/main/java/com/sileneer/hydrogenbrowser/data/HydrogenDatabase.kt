package com.sileneer.hydrogenbrowser.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryEntry::class], version = 1, exportSchema = false)
abstract class HydrogenDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: HydrogenDatabase? = null

        fun getInstance(context: Context): HydrogenDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HydrogenDatabase::class.java,
                    "hydrogen_browser.db"
                ).fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
