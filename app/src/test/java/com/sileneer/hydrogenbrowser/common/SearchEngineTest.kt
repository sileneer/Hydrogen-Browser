package com.sileneer.hydrogenbrowser.common

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchEngineTest {

    @Test
    fun `fromIndex returns correct engine`() {
        assertEquals(SearchEngine.GOOGLE, SearchEngine.fromIndex(0))
        assertEquals(SearchEngine.BAIDU, SearchEngine.fromIndex(1))
        assertEquals(SearchEngine.BING, SearchEngine.fromIndex(2))
        assertEquals(SearchEngine.DUCKDUCKGO, SearchEngine.fromIndex(3))
    }

    @Test
    fun `fromIndex returns GOOGLE for invalid index`() {
        assertEquals(SearchEngine.GOOGLE, SearchEngine.fromIndex(99))
        assertEquals(SearchEngine.GOOGLE, SearchEngine.fromIndex(-1))
    }

    @Test
    fun `displayNames returns all engine names`() {
        val expected = arrayOf("Google", "Baidu", "Bing", "DuckDuckGo")
        assertArrayEquals(expected, SearchEngine.displayNames)
    }

    @Test
    fun `each engine has a non-empty search URL`() {
        for (engine in SearchEngine.entries) {
            assert(engine.searchUrl.isNotEmpty()) { "${engine.name} has empty searchUrl" }
            assert(engine.searchUrl.startsWith("http")) { "${engine.name} searchUrl doesn't start with http" }
        }
    }

    @Test
    fun `entries count matches expected`() {
        assertEquals(4, SearchEngine.entries.size)
    }
}
