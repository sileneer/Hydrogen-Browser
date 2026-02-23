package com.sileneer.hydrogenbrowser.ui.browser

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.AndroidViewModel
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.sileneer.hydrogenbrowser.common.PreferencesRepository
import com.sileneer.hydrogenbrowser.common.SearchEngine
import com.sileneer.hydrogenbrowser.common.UrlUtils
import com.sileneer.hydrogenbrowser.tab.TabManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    val prefs = PreferencesRepository(application)
    val tabManager = TabManager()

    // One WebView per tab, keyed by tab id
    private val webViews = mutableMapOf<Int, WebView>()

    private val _activeTabId = MutableStateFlow(0)
    val activeTabId: StateFlow<Int> = _activeTabId.asStateFlow()

    private val _currentUrl = MutableStateFlow("")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _pageTitle = MutableStateFlow("")
    val pageTitle: StateFlow<String> = _pageTitle.asStateFlow()

    private val _loadingProgress = MutableStateFlow(-1f)
    val loadingProgress: StateFlow<Float> = _loadingProgress.asStateFlow()

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private val _canGoForward = MutableStateFlow(false)
    val canGoForward: StateFlow<Boolean> = _canGoForward.asStateFlow()

    private val _tabCount = MutableStateFlow(1)
    val tabCount: StateFlow<Int> = _tabCount.asStateFlow()

    init {
        createWebViewForTab(tabManager.activeTab.id, getHomepage())
    }

    fun resolveUrl(input: String): String {
        return UrlUtils.resolveInput(input, prefs.getSearchEngine())
    }

    fun getHomepage(): String = prefs.homepage

    fun getSearchEngine(): SearchEngine = prefs.getSearchEngine()

    fun getActiveWebView(): WebView? = webViews[tabManager.activeTab.id]

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebViewForTab(tabId: Int, initialUrl: String? = null): WebView {
        val vm = this
        return WebView(getApplication<Application>()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.setSupportMultipleWindows(true)

            try {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                    WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true)
                }
            } catch (_: UnsupportedOperationException) {
                // Not supported in this environment
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    if (vm.tabManager.activeTab.id == tabId) {
                        vm._currentUrl.value = url ?: ""
                        vm._loadingProgress.value = 0f
                        vm._canGoBack.value = view?.canGoBack() ?: false
                        vm._canGoForward.value = view?.canGoForward() ?: false
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    // Always update tab metadata
                    val tab = vm.tabManager.tabs.find { it.id == tabId }
                    tab?.url = url ?: ""
                    tab?.title = view?.title ?: ""

                    // Only update UI state if this is the active tab
                    if (vm.tabManager.activeTab.id == tabId) {
                        vm._currentUrl.value = url ?: ""
                        vm._pageTitle.value = view?.title ?: ""
                        vm._loadingProgress.value = -1f
                        vm._canGoBack.value = view?.canGoBack() ?: false
                        vm._canGoForward.value = view?.canGoForward() ?: false
                    }
                }

                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler?,
                    error: SslError?
                ) {
                    handler?.cancel()
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    if (request?.isForMainFrame == true) {
                        if (vm.tabManager.activeTab.id == tabId) {
                            vm._loadingProgress.value = -1f
                        }
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    if (vm.tabManager.activeTab.id == tabId) {
                        vm._loadingProgress.value = newProgress / 100f
                    }
                }
            }

            if (initialUrl != null) {
                loadUrl(initialUrl)
            }
        }.also {
            webViews[tabId] = it
        }
    }

    fun addTab() {
        val tab = tabManager.addTab()
        createWebViewForTab(tab.id, getHomepage())
        _activeTabId.value = tab.id
        _tabCount.value = tabManager.tabCount
        _currentUrl.value = ""
        _pageTitle.value = ""
        _canGoBack.value = false
        _canGoForward.value = false
        _loadingProgress.value = 0f
    }

    fun switchTab(index: Int) {
        if (index == tabManager.activeTabIndex) return

        // Pause current WebView
        webViews[tabManager.activeTab.id]?.onPause()

        tabManager.switchTo(index)
        _activeTabId.value = tabManager.activeTab.id

        // Resume new WebView and update UI state
        val webView = webViews[tabManager.activeTab.id]
        webView?.onResume()
        if (webView != null) {
            _currentUrl.value = webView.url ?: ""
            _pageTitle.value = webView.title ?: ""
            _canGoBack.value = webView.canGoBack()
            _canGoForward.value = webView.canGoForward()
            _loadingProgress.value = -1f
        }
    }

    fun closeTab(index: Int): Boolean {
        val tabToClose = tabManager.tabs[index]
        val closed = tabManager.closeTab(index)
        if (closed) {
            webViews[tabToClose.id]?.destroy()
            webViews.remove(tabToClose.id)
            _activeTabId.value = tabManager.activeTab.id
            _tabCount.value = tabManager.tabCount

            val webView = webViews[tabManager.activeTab.id]
            webView?.onResume()
            if (webView != null) {
                _currentUrl.value = webView.url ?: ""
                _pageTitle.value = webView.title ?: ""
                _canGoBack.value = webView.canGoBack()
                _canGoForward.value = webView.canGoForward()
                _loadingProgress.value = -1f
            }
        }
        return closed
    }

    override fun onCleared() {
        super.onCleared()
        webViews.values.forEach { it.destroy() }
        webViews.clear()
    }
}
