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

    // simplifyForDisplay tests
    @Test
    fun `simplifyForDisplay strips https`() {
        assertEquals("example.com", UrlUtils.simplifyForDisplay("https://example.com"))
    }

    @Test
    fun `simplifyForDisplay strips http`() {
        assertEquals("example.com", UrlUtils.simplifyForDisplay("http://example.com"))
    }

    @Test
    fun `simplifyForDisplay strips www prefix`() {
        assertEquals("example.com", UrlUtils.simplifyForDisplay("https://www.example.com"))
    }

    @Test
    fun `simplifyForDisplay strips m prefix`() {
        assertEquals("example.com", UrlUtils.simplifyForDisplay("https://m.example.com"))
    }

    @Test
    fun `simplifyForDisplay strips trailing slash`() {
        assertEquals("example.com", UrlUtils.simplifyForDisplay("https://example.com/"))
    }

    @Test
    fun `simplifyForDisplay keeps path after slash`() {
        assertEquals("example.com/page", UrlUtils.simplifyForDisplay("https://example.com/page"))
    }

    @Test
    fun `simplifyForDisplay handles empty string`() {
        assertEquals("", UrlUtils.simplifyForDisplay(""))
    }
}
