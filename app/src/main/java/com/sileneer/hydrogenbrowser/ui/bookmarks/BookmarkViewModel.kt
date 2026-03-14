package com.sileneer.hydrogenbrowser.ui.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sileneer.hydrogenbrowser.data.BookmarkEntry
import com.sileneer.hydrogenbrowser.data.BookmarkRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookmarkViewModel(private val repository: BookmarkRepository) : ViewModel() {

    private val _currentFolder = MutableStateFlow<BookmarkEntry?>(null)
    val currentFolder: StateFlow<BookmarkEntry?> = _currentFolder.asStateFlow()

    private val _folderPath = MutableStateFlow<List<BookmarkEntry>>(emptyList())
    val folderPath: StateFlow<List<BookmarkEntry>> = _folderPath.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val children: StateFlow<List<BookmarkEntry>> = combine(_currentFolder, _searchQuery, _isSearching) { folder, query, searching ->
        Triple(folder, query, searching)
    }.flatMapLatest { (folder, query, searching) ->
        if (searching && query.isNotBlank()) {
            repository.searchBookmarks(query)
        } else {
            repository.getChildrenOf(folder?.id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFolders: StateFlow<List<BookmarkEntry>> = repository.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateToFolder(folder: BookmarkEntry?) {
        if (folder != null) {
            _folderPath.value = _folderPath.value + folder
        } else {
            _folderPath.value = emptyList()
        }
        _currentFolder.value = folder
    }

    fun navigateUp(): Boolean {
        val path = _folderPath.value
        if (path.isEmpty()) return false
        val newPath = path.dropLast(1)
        _folderPath.value = newPath
        _currentFolder.value = newPath.lastOrNull()
        return true
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        val newValue = !_isSearching.value
        _isSearching.value = newValue
        if (!newValue) {
            _searchQuery.value = ""
        }
    }

    fun deleteEntry(entry: BookmarkEntry) {
        viewModelScope.launch { repository.deleteEntry(entry.id) }
    }

    fun undoDelete(entry: BookmarkEntry) {
        viewModelScope.launch { repository.reInsert(entry) }
    }

    fun createFolder(name: String) {
        viewModelScope.launch { repository.createFolder(name, _currentFolder.value?.id) }
    }

    fun updateEntry(entry: BookmarkEntry) {
        viewModelScope.launch { repository.updateEntry(entry) }
    }

    fun moveEntry(id: Long, newParentId: Long?) {
        viewModelScope.launch { repository.moveEntry(id, newParentId) }
    }
}
