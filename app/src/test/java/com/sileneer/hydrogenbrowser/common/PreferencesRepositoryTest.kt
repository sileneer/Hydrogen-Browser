package com.sileneer.hydrogenbrowser.common

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PreferencesRepositoryTest {

    private lateinit var prefs: PreferencesRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        prefs = PreferencesRepository(context)
    }

    @Test
    fun `default homepage is google`() {
        assertEquals("www.google.com", prefs.homepage)
    }

    @Test
    fun `set and get homepage`() {
        prefs.homepage = "www.example.com"
        assertEquals("www.example.com", prefs.homepage)
    }

    @Test
    fun `default search engine index is 0`() {
        assertEquals(0, prefs.searchEngineIndex)
    }

    @Test
    fun `set and get search engine index`() {
        prefs.searchEngineIndex = 2
        assertEquals(2, prefs.searchEngineIndex)
    }

    @Test
    fun `getSearchEngine returns correct enum for index`() {
        prefs.searchEngineIndex = 0
        assertEquals(SearchEngine.GOOGLE, prefs.getSearchEngine())

        prefs.searchEngineIndex = 1
        assertEquals(SearchEngine.BAIDU, prefs.getSearchEngine())

        prefs.searchEngineIndex = 2
        assertEquals(SearchEngine.BING, prefs.getSearchEngine())

        prefs.searchEngineIndex = 3
        assertEquals(SearchEngine.DUCKDUCKGO, prefs.getSearchEngine())
    }
}
