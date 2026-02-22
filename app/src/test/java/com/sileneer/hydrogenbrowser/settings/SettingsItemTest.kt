package com.sileneer.hydrogenbrowser.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SettingsItemTest {

    @Test
    fun `entries count is 4`() {
        assertEquals(4, SettingsItem.entries.size)
    }

    @Test
    fun `each item has a non-zero titleRes`() {
        for (item in SettingsItem.entries) {
            assertNotEquals("${item.name} should have a valid titleRes", 0, item.titleRes)
        }
    }

    @Test
    fun `entries are in expected order`() {
        val entries = SettingsItem.entries
        assertEquals(SettingsItem.SEARCH_ENGINE, entries[0])
        assertEquals(SettingsItem.HOMEPAGE, entries[1])
        assertEquals(SettingsItem.ABOUT, entries[2])
        assertEquals(SettingsItem.OPEN_SOURCE, entries[3])
    }
}
