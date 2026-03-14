package com.sileneer.hydrogenbrowser.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryEntry::class, BookmarkEntry::class], version = 3, exportSchema = false)
abstract class HydrogenDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: HydrogenDatabase? = null

        fun getInstance(context: Context): HydrogenDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HydrogenDatabase::class.java,
                    "hydrogen_browser.db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
