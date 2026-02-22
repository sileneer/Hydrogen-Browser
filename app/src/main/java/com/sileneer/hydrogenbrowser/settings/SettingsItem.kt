package com.sileneer.hydrogenbrowser.settings

import androidx.annotation.StringRes
import com.sileneer.hydrogenbrowser.R

enum class SettingsItem(@param:StringRes val titleRes: Int) {
    SEARCH_ENGINE(R.string.settings_search_engine),
    HOMEPAGE(R.string.settings_homepage),
    ABOUT(R.string.settings_about),
    OPEN_SOURCE(R.string.settings_open_source)
}
