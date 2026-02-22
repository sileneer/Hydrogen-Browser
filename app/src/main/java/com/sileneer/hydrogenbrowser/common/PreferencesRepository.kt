package com.sileneer.hydrogenbrowser.common

import android.content.Context
import androidx.core.content.edit

class PreferencesRepository(context: Context) {

    private val prefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)

    var homepage: String
        get() = prefs.getString(KEY_HOMEPAGE, DEFAULT_HOMEPAGE) ?: DEFAULT_HOMEPAGE
        set(value) = prefs.edit { putString(KEY_HOMEPAGE, value) }

    var searchEngineIndex: Int
        get() = prefs.getInt(KEY_SEARCH_ENGINE, 0)
        set(value) = prefs.edit { putInt(KEY_SEARCH_ENGINE, value) }

    fun getSearchEngine(): SearchEngine = SearchEngine.fromIndex(searchEngineIndex)

    companion object {
        const val DEFAULT_HOMEPAGE = "www.google.com"
        private const val KEY_HOMEPAGE = "homepage"
        private const val KEY_SEARCH_ENGINE = "search engines"
    }
}
