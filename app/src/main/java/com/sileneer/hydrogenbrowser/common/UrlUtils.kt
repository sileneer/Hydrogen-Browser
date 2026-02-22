package com.sileneer.hydrogenbrowser.common

import android.util.Patterns

object UrlUtils {
    fun resolveInput(input: String, searchEngine: SearchEngine): String {
        return when {
            input.startsWith("http://") || input.startsWith("https://") -> input
            Patterns.WEB_URL.matcher(input).matches() -> "http://$input"
            else -> searchEngine.searchUrl + input
        }
    }
}
