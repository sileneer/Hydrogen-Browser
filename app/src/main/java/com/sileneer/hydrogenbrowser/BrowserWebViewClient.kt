package com.sileneer.hydrogenbrowser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class BrowserWebViewClient(
    private val onPageStarted: (url: String?) -> Unit,
    private val onPageFinished: (url: String?, title: String?) -> Unit,
    private val onError: (errorCode: Int, description: String?, failingUrl: String?) -> Unit
) : WebViewClient() {

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        onPageStarted(view.url)
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        onPageFinished(view.url, view.title)
    }

    override fun onReceivedError(
        view: WebView, request: WebResourceRequest, error: WebResourceError
    ) {
        if (request.isForMainFrame) {
            onError(error.errorCode, error.description?.toString(), request.url?.toString())
        }
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(
        view: WebView, handler: SslErrorHandler, error: SslError
    ) {
        handler.cancel()
    }
}
