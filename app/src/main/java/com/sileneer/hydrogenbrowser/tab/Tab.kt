package com.sileneer.hydrogenbrowser.tab

import android.graphics.Bitmap

class Tab(
    val id: Int,
    var url: String = "",
    var title: String = "",
    var thumbnail: Bitmap? = null
) {
    val displayTitle: String
        get() = title.ifEmpty { url.ifEmpty { "New Tab" } }
}
