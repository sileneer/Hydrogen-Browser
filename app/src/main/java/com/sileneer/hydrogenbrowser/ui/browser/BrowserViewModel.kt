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
import androidx.lifecycle.viewModelScope
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.sileneer.hydrogenbrowser.common.PreferencesRepository
import com.sileneer.hydrogenbrowser.common.SearchEngine
import com.sileneer.hydrogenbrowser.common.UrlUtils
import com.sileneer.hydrogenbrowser.data.BookmarkRepository
import com.sileneer.hydrogenbrowser.data.HistoryRepository
import com.sileneer.hydrogenbrowser.data.HydrogenDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.sileneer.hydrogenbrowser.tab.TabManager
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    val prefs = PreferencesRepository(application)
    val tabManager = TabManager()
    private val historyRepository: HistoryRepository
    private val bookmarkRepository: BookmarkRepository

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

    private val _favicon = MutableStateFlow<Bitmap?>(null)
    val favicon: StateFlow<Bitmap?> = _favicon.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _findInPageActive = MutableStateFlow(false)
    val findInPageActive: StateFlow<Boolean> = _findInPageActive.asStateFlow()

    init {
        val db = HydrogenDatabase.getInstance(application)
        historyRepository = HistoryRepository(db.historyDao())
        bookmarkRepository = BookmarkRepository(db.bookmarkDao())
        createWebViewForTab(tabManager.activeTab.id)
    }

    fun resolveUrl(input: String): String {
        return UrlUtils.resolveInput(input, prefs.getSearchEngine())
    }

    fun getHomepage(): String = prefs.homepage

    fun getSearchEngine(): SearchEngine = prefs.getSearchEngine()

    fun getActiveWebView(): WebView? = webViews[tabManager.activeTab.id]

    fun getHistoryRepository(): HistoryRepository = historyRepository

    fun getBookmarkRepository(): BookmarkRepository = bookmarkRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    val isCurrentPageBookmarked: StateFlow<Boolean> = _currentUrl
        .flatMapLatest { url ->
            if (url.isEmpty() || url == "about:blank") {
                flowOf(false)
            } else {
                bookmarkRepository.observeBookmarkByUrl(url).map { it != null }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun addBookmark() {
        val url = _currentUrl.value
        val title = _pageTitle.value
        if (url.isEmpty() || url == "about:blank") return
        val faviconBytes = _favicon.value?.let { bmp ->
            ByteArrayOutputStream().use { stream ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.toByteArray()
            }
        }
        viewModelScope.launch {
            bookmarkRepository.addBookmark(title, url, faviconBytes)
        }
    }

    fun removeBookmarkForCurrentPage() {
        val url = _currentUrl.value
        if (url.isEmpty()) return
        viewModelScope.launch {
            val bookmark = bookmarkRepository.getBookmarkByUrl(url)
            bookmark?.let { bookmarkRepository.deleteEntry(it.id) }
        }
    }

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
                        vm._errorMessage.value = null
                        vm._canGoBack.value = view?.canGoBack() ?: false
                        vm._canGoForward.value = view?.canGoForward() ?: false
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    // Always update tab metadata
                    val tab = vm.tabManager.tabs.find { it.id == tabId }
                    tab?.url = url ?: ""
                    tab?.title = view?.title ?: ""

                    // Record in history
                    val historyUrl = url ?: ""
                    val historyTitle = view?.title ?: ""
                    if (historyUrl.isNotEmpty() && historyUrl != "about:blank") {
                        vm.viewModelScope.launch {
                            vm.historyRepository.addEntry(historyUrl, historyTitle)
                        }
                    }

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
                            vm._errorMessage.value = error?.description?.toString()
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

                override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                    if (vm.tabManager.activeTab.id == tabId) {
                        vm._favicon.value = icon
                    }
                    view?.url?.let { url ->
                        if (icon != null && url.isNotEmpty() && url != "about:blank") {
                            val bytes = ByteArrayOutputStream().use { stream ->
                                icon.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                stream.toByteArray()
                            }
                            vm.viewModelScope.launch {
                                try {
                                    vm.historyRepository.updateFavicon(url, bytes)
                                } catch (_: Exception) {
                                    // Non-critical: favicon update failure shouldn't crash the app
                                }
                            }
                        }
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

    private fun syncUiStateFromWebView(webView: WebView?) {
        if (webView != null) {
            _currentUrl.value = webView.url ?: ""
            _pageTitle.value = webView.title ?: ""
            _canGoBack.value = webView.canGoBack()
            _canGoForward.value = webView.canGoForward()
            _loadingProgress.value = -1f
            _favicon.value = webView.favicon
            _errorMessage.value = null
        }
    }

    fun addTab() {
        val tab = tabManager.addTab()
        createWebViewForTab(tab.id)
        _activeTabId.value = tab.id
        _tabCount.value = tabManager.tabCount
        _currentUrl.value = ""
        _pageTitle.value = ""
        _canGoBack.value = false
        _canGoForward.value = false
        _loadingProgress.value = -1f
        _favicon.value = null
        _errorMessage.value = null
        dismissFindInPage()
    }

    fun switchTab(index: Int) {
        if (index == tabManager.activeTabIndex) return

        // Pause current WebView
        webViews[tabManager.activeTab.id]?.onPause()
        dismissFindInPage()

        tabManager.switchTo(index)
        _activeTabId.value = tabManager.activeTab.id

        // Resume new WebView and update UI state
        val webView = webViews[tabManager.activeTab.id]
        webView?.onResume()
        syncUiStateFromWebView(webView)
    }

    fun closeTab(index: Int): Boolean {
        val tabToClose = tabManager.tabs[index]
        val closed = tabManager.closeTab(index)
        if (closed) {
            webViews[tabToClose.id]?.destroy()
            webViews.remove(tabToClose.id)
            _activeTabId.value = tabManager.activeTab.id
            _tabCount.value = tabManager.tabCount
            dismissFindInPage()

            val webView = webViews[tabManager.activeTab.id]
            webView?.onResume()
            syncUiStateFromWebView(webView)
        }
        return closed
    }

    fun findInPage(query: String) {
        getActiveWebView()?.findAllAsync(query)
    }

    fun findNext(forward: Boolean) {
        getActiveWebView()?.findNext(forward)
    }

    fun showFindInPage() {
        _findInPageActive.value = true
    }

    fun dismissFindInPage() {
        _findInPageActive.value = false
        getActiveWebView()?.clearMatches()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        webViews.values.forEach { it.destroy() }
        webViews.clear()
    }
}
