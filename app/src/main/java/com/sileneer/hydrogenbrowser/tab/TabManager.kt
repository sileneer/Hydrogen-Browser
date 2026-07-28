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

    fun closeTab(index: Int): Tab? {
        val closedTab = _tabs.removeAt(index)
        if (_tabs.isEmpty()) {
            val newTab = Tab(id = nextId++)
            _tabs.add(newTab)
            _activeTabIndex = 0
            return newTab
        }
        // Closing a tab before the active one shifts it down; closing the active one
        // keeps the index (the next tab slides in) and only needs clamping at the end.
        if (index < _activeTabIndex) {
            _activeTabIndex--
        } else if (_activeTabIndex >= _tabs.size) {
            _activeTabIndex = _tabs.size - 1
        }
        return null
    }

    fun indexOfId(tabId: Int): Int = _tabs.indexOfFirst { it.id == tabId }
}
