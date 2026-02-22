package com.sileneer.hydrogenbrowser.tab

class TabManager {

    private val _tabs = mutableListOf(Tab(id = 0))
    val tabs: List<Tab> get() = _tabs

    private var _activeTabIndex = 0
    val activeTabIndex: Int get() = _activeTabIndex
    val activeTab: Tab get() = _tabs[_activeTabIndex]

    val tabCount: Int get() = _tabs.size

    private var nextId = 1

    fun addTab(): Tab {
        val tab = Tab(id = nextId++)
        _tabs.add(tab)
        _activeTabIndex = _tabs.size - 1
        return tab
    }

    fun switchTo(index: Int) {
        require(index in _tabs.indices)
        _activeTabIndex = index
    }

    fun closeTab(index: Int): Boolean {
        if (_tabs.size <= 1) return false
        _tabs.removeAt(index)
        if (_activeTabIndex >= _tabs.size) {
            _activeTabIndex = _tabs.size - 1
        }
        return true
    }

    fun updateActiveTab(url: String, title: String) {
        activeTab.url = url
        activeTab.title = title
    }
}
