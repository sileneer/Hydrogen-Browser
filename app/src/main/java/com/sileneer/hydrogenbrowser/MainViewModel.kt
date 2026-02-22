package com.sileneer.hydrogenbrowser

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.sileneer.hydrogenbrowser.common.PreferencesRepository
import com.sileneer.hydrogenbrowser.common.SearchEngine
import com.sileneer.hydrogenbrowser.common.UrlUtils
import com.sileneer.hydrogenbrowser.tab.TabManager

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val prefs = PreferencesRepository(application)
    val tabManager = TabManager()

    fun resolveUrl(input: String): String {
        return UrlUtils.resolveInput(input, prefs.getSearchEngine())
    }

    fun getHomepage(): String = prefs.homepage

    fun getSearchEngine(): SearchEngine = prefs.getSearchEngine()
}
