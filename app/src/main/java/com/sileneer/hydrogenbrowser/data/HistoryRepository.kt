package com.sileneer.hydrogenbrowser.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val dao: HistoryDao) {
    fun getAllHistory(): Flow<List<HistoryEntry>> = dao.getAllHistory()

    fun searchHistory(query: String): Flow<List<HistoryEntry>> = dao.searchHistory(query)

    suspend fun addEntry(url: String, title: String) {
        dao.insert(HistoryEntry(url = url, title = title, timestamp = System.currentTimeMillis()))
    }

    suspend fun deleteEntry(id: Long) = dao.deleteById(id)

    suspend fun clearAll() = dao.deleteAll()

    suspend fun reInsert(entry: HistoryEntry) {
        dao.upsert(entry)
    }
}
