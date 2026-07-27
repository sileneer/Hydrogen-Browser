package com.sileneer.hydrogenbrowser.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class HistoryEntryTest {

    @Test
    fun `equals returns true for identical entries`() {
        val entry1 = HistoryEntry(id = 1, url = "https://example.com", title = "Example", timestamp = 1000L)
        val entry2 = HistoryEntry(id = 1, url = "https://example.com", title = "Example", timestamp = 1000L)
        assertEquals(entry1, entry2)
    }

    @Test
    fun `equals returns false for different urls`() {
        val entry1 = HistoryEntry(id = 1, url = "https://a.com", title = "A", timestamp = 1000L)
        val entry2 = HistoryEntry(id = 1, url = "https://b.com", title = "A", timestamp = 1000L)
        assertNotEquals(entry1, entry2)
    }

    @Test
    fun `equals handles both favicons null`() {
        val entry1 = HistoryEntry(id = 1, url = "https://a.com", title = "A", timestamp = 1000L, favicon = null)
        val entry2 = HistoryEntry(id = 1, url = "https://a.com", title = "A", timestamp = 1000L, favicon = null)
        assertEquals(entry1, entry2)
    }

    @Test
    fun `equals handles one favicon null`() {
        val entry1 = HistoryEntry(id = 1, url = "https://a.com", title = "A", timestamp = 1000L, favicon = byteArrayOf(1, 2))
        val entry2 = HistoryEntry(id = 1, url = "https://a.com", title = "A", timestamp = 1000L, favicon = null)
        assertNotEquals(entry1, entry2)
    }

    @Test
    fun `equals handles other favicon null`() {
        val entry1 = HistoryEntry(id = 1, url = "https://a.com", title = "A", timestamp = 1000L, favicon = null)
        val entry2 = HistoryEntry(id = 1, url = "https://a.com", title = "A", timestamp = 1000L, favicon = byteArrayOf(1, 2))
        assertNotEquals(entry1, entry2)
    }

    @Test
    fun `equals handles matching favicons`() {
        val entry1 = HistoryEntry(id = 1, url = "https://a.com", title = "A", timestamp = 1000L, favicon = byteArrayOf(1, 2, 3))
        val entry2 = HistoryEntry(id = 1, url = "https://a.com", title = "A", timestamp = 1000L, favicon = byteArrayOf(1, 2, 3))
        assertEquals(entry1, entry2)
    }

    @Test
    fun `equals handles different favicons`() {
        val entry1 = HistoryEntry(id = 1, url = "https://a.com", title = "A", timestamp = 1000L, favicon = byteArrayOf(1, 2, 3))
        val entry2 = HistoryEntry(id = 1, url = "https://a.com", title = "A", timestamp = 1000L, favicon = byteArrayOf(4, 5, 6))
        assertNotEquals(entry1, entry2)
    }

    @Test
    fun `hashCode is consistent for equal entries`() {
        val entry1 = HistoryEntry(id = 1, url = "https://a.com", title = "A", timestamp = 1000L, favicon = null)
        val entry2 = HistoryEntry(id = 1, url = "https://a.com", title = "A", timestamp = 1000L, favicon = null)
        assertEquals(entry1.hashCode(), entry2.hashCode())
    }

    @Test
    fun `hashCode handles null favicon`() {
        val entry = HistoryEntry(id = 1, url = "https://a.com", title = "A", timestamp = 1000L, favicon = null)
        // Should not throw
        entry.hashCode()
    }

    @Test
    fun `equals returns false for non-HistoryEntry`() {
        val entry = HistoryEntry(id = 1, url = "https://a.com", title = "A", timestamp = 1000L)
        assertNotEquals(entry, "not a history entry")
    }

    @Test
    fun `equals returns true for same reference`() {
        val entry = HistoryEntry(id = 1, url = "https://a.com", title = "A", timestamp = 1000L)
        assertEquals(entry, entry)
    }
}
