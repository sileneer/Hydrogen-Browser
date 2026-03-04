package com.sileneer.hydrogenbrowser.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntry>>

    @Query("SELECT * FROM history_entries WHERE title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHistory(query: String): Flow<List<HistoryEntry>>

    @Insert
    suspend fun insert(entry: HistoryEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: HistoryEntry)

    @Query("DELETE FROM history_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM history_entries")
    suspend fun deleteAll()

    @Query("SELECT * FROM history_entries ORDER BY timestamp DESC LIMIT 1")
    suspend fun getMostRecent(): HistoryEntry?

    @Query("UPDATE history_entries SET favicon = :favicon WHERE id = (SELECT id FROM history_entries WHERE url = :url ORDER BY timestamp DESC LIMIT 1)")
    suspend fun updateFavicon(url: String, favicon: ByteArray)
}
