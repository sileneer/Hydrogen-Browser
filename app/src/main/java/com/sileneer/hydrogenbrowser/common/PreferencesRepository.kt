package com.sileneer.hydrogenbrowser.common

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

class PreferencesRepository(context: Context) {

    private val prefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)

    var homepage: String
        get() = prefs.getString(KEY_HOMEPAGE, DEFAULT_HOMEPAGE) ?: DEFAULT_HOMEPAGE
        set(value) = prefs.edit { putString(KEY_HOMEPAGE, value) }

    var searchEngineIndex: Int
        get() = prefs.getInt(KEY_SEARCH_ENGINE, 0)
        set(value) = prefs.edit { putInt(KEY_SEARCH_ENGINE, value) }

    fun getSearchEngine(): SearchEngine = SearchEngine.fromIndex(searchEngineIndex)

    fun getShortcuts(): List<Shortcut> {
        val json = prefs.getString(KEY_SHORTCUTS, null) ?: return DEFAULT_SHORTCUTS
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                Shortcut(obj.getString("name"), obj.getString("url"))
            }
        } catch (_: org.json.JSONException) {
            DEFAULT_SHORTCUTS
        }
    }

    fun saveShortcuts(list: List<Shortcut>) {
        val array = JSONArray()
        list.forEach { shortcut ->
            array.put(JSONObject().apply {
                put("name", shortcut.name)
                put("url", shortcut.url)
            })
        }
        prefs.edit { putString(KEY_SHORTCUTS, array.toString()) }
    }

    /**
     * New installs get the start page; upgraders who already saved a custom homepage keep it,
     * since this key is absent for them and defaulting to true would silently ignore their URL.
     */
    var homeButtonGoesToStartPage: Boolean
        get() = prefs.getBoolean(KEY_HOME_START_PAGE, !prefs.contains(KEY_HOMEPAGE))
        set(value) = prefs.edit { putBoolean(KEY_HOME_START_PAGE, value) }

    /** Last folder id used when bookmarking; null = root. */
    var lastBookmarkFolderId: Long?
        get() {
            val v = prefs.getLong(KEY_LAST_BOOKMARK_FOLDER, -1L)
            return if (v == -1L) null else v
        }
        set(value) = prefs.edit { putLong(KEY_LAST_BOOKMARK_FOLDER, value ?: -1L) }

    companion object {
        const val DEFAULT_HOMEPAGE = "www.google.com"
        private const val KEY_HOMEPAGE = "homepage"
        private const val KEY_SEARCH_ENGINE = "search engines"
        private const val KEY_SHORTCUTS = "shortcuts"
        private const val KEY_HOME_START_PAGE = "home_button_start_page"
        private const val KEY_LAST_BOOKMARK_FOLDER = "last_bookmark_folder"

        val DEFAULT_SHORTCUTS = listOf(
            Shortcut("Google", "https://www.google.com"),
            Shortcut("YouTube", "https://www.youtube.com"),
            Shortcut("Wikipedia", "https://www.wikipedia.org"),
            Shortcut("GitHub", "https://github.com"),
        )
    }
}
