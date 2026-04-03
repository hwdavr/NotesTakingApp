package com.example.notesapp.ui.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notesapp.data.local.FolderEntity
import com.example.notesapp.data.repository.FolderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class FoldersViewModel(
    private val folderRepository: FolderRepository
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")

    private val allFolders = folderRepository.getFolders()

    val uiState: StateFlow<List<FolderEntity>> = combine(allFolders, searchQuery) { folders, query ->
        if (query.isBlank()) {
            folders
        } else {
            folders.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun onSearchChanged(query: String) {
        searchQuery.value = query
    }

    class Factory(private val folderRepository: FolderRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FoldersViewModel(folderRepository) as T
        }
    }
}
