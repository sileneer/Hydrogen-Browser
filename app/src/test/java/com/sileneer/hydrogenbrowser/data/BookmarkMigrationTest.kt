package com.sileneer.hydrogenbrowser.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Guards the v2 -> v3 upgrade path: adding the bookmarks table must not fall through to
 * destructive migration, and a parentId that no longer exists must not violate the FK.
 */
@RunWith(RobolectricTestRunner::class)
class BookmarkMigrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "migration_test.db"
    private var db: HydrogenDatabase? = null

    @Before
    fun createV2Database() {
        val file = context.getDatabasePath(dbName)
        file.parentFile?.mkdirs()
        file.delete()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { legacy ->
            legacy.execSQL(
                "CREATE TABLE IF NOT EXISTS `history_entries` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`url` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                    "`timestamp` INTEGER NOT NULL, `favicon` BLOB)"
            )
            legacy.execSQL(
                "INSERT INTO history_entries (url, title, timestamp) " +
                    "VALUES ('https://example.com', 'Example', 42)"
            )
            legacy.version = 2
        }
    }

    @After
    fun tearDown() {
        db?.close()
        context.getDatabasePath(dbName).delete()
    }

    private fun openMigrated(): HydrogenDatabase =
        Room.databaseBuilder(context, HydrogenDatabase::class.java, dbName)
            .addMigrations(HydrogenDatabase.MIGRATION_2_3)
            .allowMainThreadQueries()
            .build()
            .also { db = it }

    @Test
    fun `upgrading from v2 keeps history and creates a usable bookmarks table`() = runTest {
        val database = openMigrated()

        val survivor = database.historyDao().getMostRecent()
        assertEquals("https://example.com", survivor?.url)

        // A successful insert also proves the migrated DDL passed Room's schema validation.
        val folder = BookmarkRepository(database.bookmarkDao()).createFolder("Work")
        assertEquals("Work", database.bookmarkDao().getById(folder.id)?.title)
    }

    @Test
    fun `bookmarking into a deleted folder falls back to root instead of throwing`() = runTest {
        val repository = BookmarkRepository(openMigrated().bookmarkDao())
        val folder = repository.createFolder("Work")
        repository.deleteEntry(folder.id)

        val entry = repository.addBookmark("Example", "https://example.com", parentId = folder.id)

        assertNull(entry.parentId)
        assertEquals(1, repository.getChildrenOf(null).first().size)
    }

    @Test
    fun `undo re-insert clamps a parent that was deleted in the meantime`() = runTest {
        val repository = BookmarkRepository(openMigrated().bookmarkDao())
        val folder = repository.createFolder("Work")
        val bookmark = repository.addBookmark("Example", "https://example.com", parentId = folder.id)

        repository.deleteEntry(bookmark.id)
        repository.deleteEntry(folder.id)
        repository.reInsert(bookmark)

        assertNull(repository.getById(bookmark.id)?.parentId)
    }
}
