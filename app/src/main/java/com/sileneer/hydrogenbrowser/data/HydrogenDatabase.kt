package com.sileneer.hydrogenbrowser.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [HistoryEntry::class, BookmarkEntry::class], version = 3, exportSchema = false)
abstract class HydrogenDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: HydrogenDatabase? = null

        /** v3 only adds the bookmarks table — history must survive the upgrade. */
        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `bookmarks` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`url` TEXT, " +
                        "`isFolder` INTEGER NOT NULL, " +
                        "`parentId` INTEGER, " +
                        "`position` INTEGER NOT NULL, " +
                        "`favicon` BLOB, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`parentId`) REFERENCES `bookmarks`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmarks_parentId` ON `bookmarks` (`parentId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmarks_url` ON `bookmarks` (`url`)")
            }
        }

        fun getInstance(context: Context): HydrogenDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HydrogenDatabase::class.java,
                    "hydrogen_browser.db"
                ).addMigrations(MIGRATION_2_3)
                    // ponytail: kept as the backstop for pre-v2 installs only; every
                    // future version bump needs its own Migration added above.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
