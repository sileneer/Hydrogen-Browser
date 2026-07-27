package com.sileneer.hydrogenbrowser.data

import kotlinx.coroutines.flow.Flow

class BookmarkRepository(private val dao: BookmarkDao) {

    fun getChildrenOf(parentId: Long?): Flow<List<BookmarkEntry>> = dao.getChildrenOf(parentId)

    fun searchBookmarks(query: String): Flow<List<BookmarkEntry>> = dao.searchBookmarks(query)

    fun observeBookmarkByUrl(url: String): Flow<BookmarkEntry?> = dao.observeBookmarkByUrl(url)

    fun getAllFolders(): Flow<List<BookmarkEntry>> = dao.getAllFolders()

    suspend fun getBookmarkByUrl(url: String): BookmarkEntry? = dao.getBookmarkByUrl(url)

    suspend fun getById(id: Long): BookmarkEntry? = dao.getById(id)

    /**
     * A folder id can outlive the folder: it is cached in prefs and held by undo buffers,
     * and parentId is a real FK, so an insert against a deleted folder throws. Fall back to root.
     */
    private suspend fun existingParentOrRoot(parentId: Long?): Long? =
        parentId?.takeIf { dao.getById(it) != null }

    suspend fun addBookmark(
        title: String,
        url: String,
        favicon: ByteArray? = null,
        parentId: Long? = null
    ): BookmarkEntry {
        val safeParentId = existingParentOrRoot(parentId)
        val maxPos = dao.getMaxPosition(safeParentId) ?: -1
        val entry = BookmarkEntry(
            title = title,
            url = url,
            isFolder = false,
            parentId = safeParentId,
            position = maxPos + 1,
            favicon = favicon,
            createdAt = System.currentTimeMillis()
        )
        val id = dao.insert(entry)
        return entry.copy(id = id)
    }

    suspend fun createFolder(name: String, parentId: Long? = null): BookmarkEntry {
        val safeParentId = existingParentOrRoot(parentId)
        val maxPos = dao.getMaxPosition(safeParentId) ?: -1
        val entry = BookmarkEntry(
            title = name,
            url = null,
            isFolder = true,
            parentId = safeParentId,
            position = maxPos + 1,
            createdAt = System.currentTimeMillis()
        )
        val id = dao.insert(entry)
        return entry.copy(id = id)
    }

    suspend fun moveEntry(id: Long, newParentId: Long?) {
        val entry = dao.getById(id) ?: return
        if (entry.isFolder && newParentId != null) {
            var currentId: Long? = newParentId
            while (currentId != null) {
                if (currentId == id) return
                val parent = dao.getById(currentId)
                currentId = parent?.parentId
            }
        }
        val maxPos = dao.getMaxPosition(newParentId) ?: -1
        dao.update(entry.copy(parentId = newParentId, position = maxPos + 1))
    }

    suspend fun updateEntry(entry: BookmarkEntry) = dao.update(entry)

    suspend fun deleteEntry(id: Long) = dao.deleteById(id)

    suspend fun reInsert(entry: BookmarkEntry) {
        val safeParentId = existingParentOrRoot(entry.parentId)
        dao.upsert(if (safeParentId == entry.parentId) entry else entry.copy(parentId = safeParentId))
    }
}
