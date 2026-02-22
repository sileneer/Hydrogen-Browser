package com.sileneer.hydrogenbrowser.common

enum class SearchEngine(val displayName: String, val searchUrl: String) {
    GOOGLE("Google", "http://www.google.com/search?q="),
    BAIDU("Baidu", "http://www.baidu.com/s?wd="),
    BING("Bing", "https://www.bing.com/search?q="),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=");

    companion object {
        val displayNames: Array<String> = entries.map { it.displayName }.toTypedArray()

        fun fromIndex(index: Int): SearchEngine = entries[index]
    }
}
