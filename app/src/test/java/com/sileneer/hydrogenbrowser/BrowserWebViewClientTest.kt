package com.sileneer.hydrogenbrowser

import android.webkit.WebView
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BrowserWebViewClientTest {

    @Test
    fun `onPageStarted invokes callback with view url`() {
        var receivedUrl: String? = null
        val client = BrowserWebViewClient(
            onPageStarted = { receivedUrl = it },
            onPageFinished = { _, _ -> },
            onError = { _, _, _ -> }
        )
        val webView = mockk<WebView>(relaxed = true)
        every { webView.url } returns "https://example.com"

        client.onPageStarted(webView, "https://example.com", null)

        assertEquals("https://example.com", receivedUrl)
    }

    @Test
    fun `onPageFinished invokes callback with url and title`() {
        var receivedUrl: String? = null
        var receivedTitle: String? = null
        val client = BrowserWebViewClient(
            onPageStarted = { },
            onPageFinished = { url, title -> receivedUrl = url; receivedTitle = title },
            onError = { _, _, _ -> }
        )
        val webView = mockk<WebView>(relaxed = true)
        every { webView.url } returns "https://example.com"
        every { webView.title } returns "Example"

        client.onPageFinished(webView, "https://example.com")

        assertEquals("https://example.com", receivedUrl)
        assertEquals("Example", receivedTitle)
    }

    @Test
    fun `onPageStarted with null url`() {
        var receivedUrl: String? = "initial"
        val client = BrowserWebViewClient(
            onPageStarted = { receivedUrl = it },
            onPageFinished = { _, _ -> },
            onError = { _, _, _ -> }
        )
        val webView = mockk<WebView>(relaxed = true)
        every { webView.url } returns null

        client.onPageStarted(webView, "", null)

        assertNull(receivedUrl)
    }
}
