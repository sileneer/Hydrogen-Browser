package com.sileneer.hydrogenbrowser.common

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlUtilsTest {

    @Test
    fun `http URL is returned as-is`() {
        val result = UrlUtils.resolveInput("http://example.com", SearchEngine.GOOGLE)
        assertEquals("http://example.com", result)
    }

    @Test
    fun `https URL is returned as-is`() {
        val result = UrlUtils.resolveInput("https://example.com", SearchEngine.GOOGLE)
        assertEquals("https://example.com", result)
    }

    @Test
    fun `https URL with path is returned as-is`() {
        val result = UrlUtils.resolveInput("https://example.com/path?q=1", SearchEngine.BING)
        assertEquals("https://example.com/path?q=1", result)
    }

    @Test
    fun `http URL with port is returned as-is`() {
        val result = UrlUtils.resolveInput("http://localhost:8080", SearchEngine.GOOGLE)
        assertEquals("http://localhost:8080", result)
    }

    @Test
    fun `each search engine produces correct prefix`() {
        for (engine in SearchEngine.entries) {
            val result = UrlUtils.resolveInput("http://test", engine)
            assertEquals("http://test", result)
        }
    }
}
