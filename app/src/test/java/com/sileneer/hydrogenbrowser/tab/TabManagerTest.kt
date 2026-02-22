package com.sileneer.hydrogenbrowser.tab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TabManagerTest {

    private lateinit var tabManager: TabManager

    @Before
    fun setUp() {
        tabManager = TabManager()
    }

    @Test
    fun `initial state has one tab`() {
        assertEquals(1, tabManager.tabCount)
        assertEquals(0, tabManager.activeTabIndex)
    }

    @Test
    fun `addTab increases count and sets new tab as active`() {
        tabManager.addTab()
        assertEquals(2, tabManager.tabCount)
        assertEquals(1, tabManager.activeTabIndex)
    }

    @Test
    fun `switchTo changes active tab`() {
        tabManager.addTab()
        tabManager.addTab()
        tabManager.switchTo(0)
        assertEquals(0, tabManager.activeTabIndex)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `switchTo invalid index throws`() {
        tabManager.switchTo(5)
    }

    @Test
    fun `closeTab removes tab and adjusts active index`() {
        tabManager.addTab()
        tabManager.addTab()
        assertEquals(3, tabManager.tabCount)

        tabManager.switchTo(2)
        assertTrue(tabManager.closeTab(2))
        assertEquals(2, tabManager.tabCount)
        assertEquals(1, tabManager.activeTabIndex)
    }

    @Test
    fun `cannot close last tab`() {
        assertFalse(tabManager.closeTab(0))
        assertEquals(1, tabManager.tabCount)
    }

    @Test
    fun `updateActiveTab saves url and title`() {
        tabManager.updateActiveTab("https://example.com", "Example")
        assertEquals("https://example.com", tabManager.activeTab.url)
        assertEquals("Example", tabManager.activeTab.title)
    }

    @Test
    fun `tabs are independent`() {
        tabManager.updateActiveTab("https://first.com", "First")
        tabManager.addTab()
        tabManager.updateActiveTab("https://second.com", "Second")

        tabManager.switchTo(0)
        assertEquals("https://first.com", tabManager.activeTab.url)

        tabManager.switchTo(1)
        assertEquals("https://second.com", tabManager.activeTab.url)
    }
}
