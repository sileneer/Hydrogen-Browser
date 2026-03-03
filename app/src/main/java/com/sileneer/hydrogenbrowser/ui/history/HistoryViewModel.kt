package com.sileneer.hydrogenbrowser.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sileneer.hydrogenbrowser.data.HistoryEntry
import com.sileneer.hydrogenbrowser.data.HistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val historyEntries: StateFlow<List<HistoryEntry>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllHistory()
            else repository.searchHistory(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch { repository.deleteEntry(id) }
    }

    fun clearAllHistory() {
        viewModelScope.launch { repository.clearAll() }
    }

    fun undoDelete(entry: HistoryEntry) {
        viewModelScope.launch { repository.reInsert(entry) }
    }
}
