package com.sileneer.hydrogenbrowser.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE CASE WHEN :parentId IS NULL THEN parentId IS NULL ELSE parentId = :parentId END ORDER BY isFolder DESC, position ASC")
    fun getChildrenOf(parentId: Long?): Flow<List<BookmarkEntry>>

    @Query("SELECT * FROM bookmarks WHERE isFolder = 0 AND (title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    fun searchBookmarks(query: String): Flow<List<BookmarkEntry>>

    @Query("SELECT * FROM bookmarks WHERE url = :url AND isFolder = 0 LIMIT 1")
    suspend fun getBookmarkByUrl(url: String): BookmarkEntry?

    @Query("SELECT * FROM bookmarks WHERE url = :url AND isFolder = 0 LIMIT 1")
    fun observeBookmarkByUrl(url: String): Flow<BookmarkEntry?>

    @Query("SELECT MAX(position) FROM bookmarks WHERE CASE WHEN :parentId IS NULL THEN parentId IS NULL ELSE parentId = :parentId END")
    suspend fun getMaxPosition(parentId: Long?): Int?

    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun getById(id: Long): BookmarkEntry?

    @Query("SELECT * FROM bookmarks WHERE isFolder = 1 ORDER BY title ASC")
    fun getAllFolders(): Flow<List<BookmarkEntry>>

    @Insert
    suspend fun insert(entry: BookmarkEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: BookmarkEntry): Long

    @Update
    suspend fun update(entry: BookmarkEntry)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
