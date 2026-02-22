package com.sileneer.hydrogenbrowser.tab

data class Tab(
    val id: Int,
    var url: String = "",
    var title: String = ""
) {
    val displayTitle: String
        get() = title.ifEmpty { url.ifEmpty { "New Tab" } }
}
