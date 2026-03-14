package com.sileneer.hydrogenbrowser.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookmarkEntryTest {

    @Test
    fun `equals returns true for identical bookmark entries`() {
        val e1 = BookmarkEntry(id = 1, title = "Google", url = "https://google.com", isFolder = false, parentId = null, position = 0, createdAt = 1000L)
        val e2 = BookmarkEntry(id = 1, title = "Google", url = "https://google.com", isFolder = false, parentId = null, position = 0, createdAt = 1000L)
        assertEquals(e1, e2)
    }

    @Test
    fun `equals returns false for different urls`() {
        val e1 = BookmarkEntry(id = 1, title = "A", url = "https://a.com", isFolder = false, parentId = null, position = 0, createdAt = 1000L)
        val e2 = BookmarkEntry(id = 1, title = "A", url = "https://b.com", isFolder = false, parentId = null, position = 0, createdAt = 1000L)
        assertNotEquals(e1, e2)
    }

    @Test
    fun `equals handles both favicons null`() {
        val e1 = BookmarkEntry(id = 1, title = "A", url = "https://a.com", isFolder = false, parentId = null, position = 0, createdAt = 1000L, favicon = null)
        val e2 = BookmarkEntry(id = 1, title = "A", url = "https://a.com", isFolder = false, parentId = null, position = 0, createdAt = 1000L, favicon = null)
        assertEquals(e1, e2)
    }

    @Test
    fun `equals handles one favicon null`() {
        val e1 = BookmarkEntry(id = 1, title = "A", url = "https://a.com", isFolder = false, parentId = null, position = 0, createdAt = 1000L, favicon = byteArrayOf(1, 2))
        val e2 = BookmarkEntry(id = 1, title = "A", url = "https://a.com", isFolder = false, parentId = null, position = 0, createdAt = 1000L, favicon = null)
        assertNotEquals(e1, e2)
    }

    @Test
    fun `equals handles matching favicons`() {
        val e1 = BookmarkEntry(id = 1, title = "A", url = "https://a.com", isFolder = false, parentId = null, position = 0, createdAt = 1000L, favicon = byteArrayOf(1, 2, 3))
        val e2 = BookmarkEntry(id = 1, title = "A", url = "https://a.com", isFolder = false, parentId = null, position = 0, createdAt = 1000L, favicon = byteArrayOf(1, 2, 3))
        assertEquals(e1, e2)
    }

    @Test
    fun `equals handles different favicons`() {
        val e1 = BookmarkEntry(id = 1, title = "A", url = "https://a.com", isFolder = false, parentId = null, position = 0, createdAt = 1000L, favicon = byteArrayOf(1, 2, 3))
        val e2 = BookmarkEntry(id = 1, title = "A", url = "https://a.com", isFolder = false, parentId = null, position = 0, createdAt = 1000L, favicon = byteArrayOf(4, 5, 6))
        assertNotEquals(e1, e2)
    }

    @Test
    fun `hashCode is consistent for equal entries`() {
        val e1 = BookmarkEntry(id = 1, title = "A", url = "https://a.com", isFolder = false, parentId = null, position = 0, createdAt = 1000L)
        val e2 = BookmarkEntry(id = 1, title = "A", url = "https://a.com", isFolder = false, parentId = null, position = 0, createdAt = 1000L)
        assertEquals(e1.hashCode(), e2.hashCode())
    }

    @Test
    fun `folder entry has null url`() {
        val folder = BookmarkEntry(id = 1, title = "My Folder", url = null, isFolder = true, parentId = null, position = 0, createdAt = 1000L)
        assertTrue(folder.isFolder)
        assertNull(folder.url)
    }

    @Test
    fun `nested bookmark has parentId set`() {
        val bookmark = BookmarkEntry(id = 2, title = "Child", url = "https://a.com", isFolder = false, parentId = 1, position = 0, createdAt = 1000L)
        assertEquals(1L, bookmark.parentId)
    }

    @Test
    fun `equals returns false for non-BookmarkEntry`() {
        val entry = BookmarkEntry(id = 1, title = "A", url = "https://a.com", isFolder = false, parentId = null, position = 0, createdAt = 1000L)
        assertNotEquals(entry, "not a bookmark entry")
    }

    @Test
    fun `equals returns true for same reference`() {
        val entry = BookmarkEntry(id = 1, title = "A", url = "https://a.com", isFolder = false, parentId = null, position = 0, createdAt = 1000L)
        assertEquals(entry, entry)
    }
}
