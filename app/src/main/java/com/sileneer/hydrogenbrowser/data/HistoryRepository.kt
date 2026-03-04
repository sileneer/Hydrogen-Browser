package com.sileneer.hydrogenbrowser.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val dao: HistoryDao) {
    fun getAllHistory(): Flow<List<HistoryEntry>> = dao.getAllHistory()

    fun searchHistory(query: String): Flow<List<HistoryEntry>> = dao.searchHistory(query)

    suspend fun addEntry(url: String, title: String) {
        val recent = dao.getMostRecent()
        if (recent != null && recent.url == url) return
        dao.insert(HistoryEntry(url = url, title = title, timestamp = System.currentTimeMillis()))
    }

    suspend fun deleteEntry(id: Long) = dao.deleteById(id)

    suspend fun clearAll() = dao.deleteAll()

    suspend fun reInsert(entry: HistoryEntry) {
        dao.upsert(entry)
    }

    suspend fun updateFavicon(url: String, favicon: ByteArray) {
        dao.updateFavicon(url, favicon)
    }
}
