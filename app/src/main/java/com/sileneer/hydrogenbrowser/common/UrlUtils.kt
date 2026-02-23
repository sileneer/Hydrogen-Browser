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

    /**
     * Simplify a URL for address-bar display (like Chrome):
     * strip scheme, trivial subdomains (www., m.), and lone trailing slash.
     */
    fun simplifyForDisplay(url: String): String {
        var result = url
            .removePrefix("https://")
            .removePrefix("http://")
        if (result.startsWith("www.")) {
            result = result.removePrefix("www.")
        } else if (result.startsWith("m.")) {
            result = result.removePrefix("m.")
        }
        if (result.endsWith("/") && result.count { it == '/' } == 1) {
            result = result.removeSuffix("/")
        }
        return result
    }
}
