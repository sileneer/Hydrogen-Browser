package com.sileneer.hydrogenbrowser.ui.browser

import android.app.Application
import android.os.Bundle
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
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

    // Stores WebView state per tab (keyed by tab id)
    private val webViewBundles = mutableMapOf<Int, Bundle>()

    fun resolveUrl(input: String): String {
        return UrlUtils.resolveInput(input, prefs.getSearchEngine())
    }

    fun getHomepage(): String = prefs.homepage

    fun getSearchEngine(): SearchEngine = prefs.getSearchEngine()

    fun onPageStarted(url: String?) {
        _currentUrl.value = url ?: ""
        _loadingProgress.value = 0f
    }

    fun onPageFinished(url: String?, title: String?) {
        _currentUrl.value = url ?: ""
        _pageTitle.value = title ?: ""
        _loadingProgress.value = -1f
    }

    fun onProgressChanged(progress: Int) {
        _loadingProgress.value = progress / 100f
    }

    fun onError(description: String?) {
        _loadingProgress.value = -1f
    }

    fun updateNavigation(canBack: Boolean, canForward: Boolean) {
        _canGoBack.value = canBack
        _canGoForward.value = canForward
    }

    fun saveCurrentTabState(url: String, title: String) {
        tabManager.updateActiveTab(url = url, title = title)
    }

    fun saveWebViewState(webView: WebView) {
        val bundle = Bundle()
        webView.saveState(bundle)
        webViewBundles[tabManager.activeTab.id] = bundle
    }

    fun restoreWebViewState(webView: WebView): Boolean {
        val bundle = webViewBundles[tabManager.activeTab.id]
        if (bundle != null) {
            webView.restoreState(bundle)
            return true
        }
        return false
    }

    fun addTab() {
        tabManager.addTab()
        _tabCount.value = tabManager.tabCount
    }

    fun switchTab(index: Int) {
        tabManager.switchTo(index)
    }

    fun updateTabCount() {
        _tabCount.value = tabManager.tabCount
    }
}
