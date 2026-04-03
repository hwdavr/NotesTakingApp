package com.example.notesapp.ui.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notesapp.data.local.FolderEntity
import com.example.notesapp.data.repository.FolderRepository
import com.example.notesapp.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SmartCollectionCounts(
    val allNotes: Int = 0,
    val favorites: Int = 0,
    val archive: Int = 0
)

data class FolderItemUi(
    val folder: FolderEntity,
    val noteCount: Int
)

data class FoldersUiState(
    val smartCounts: SmartCollectionCounts = SmartCollectionCounts(),
    val folders: List<FolderItemUi> = emptyList()
)

class FoldersViewModel(
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val smartCounts = MutableStateFlow(SmartCollectionCounts())

    private val allFolders = folderRepository.getFolders()

    val uiState: StateFlow<FoldersUiState> = combine(allFolders, searchQuery, smartCounts) { folders, query, counts ->
        val visibleFolders = if (query.isBlank()) {
            folders
        } else {
            folders.filter { it.name.contains(query, ignoreCase = true) }
        }

        FoldersUiState(
            smartCounts = counts,
            folders = visibleFolders.map { folder ->
                FolderItemUi(folder = folder, noteCount = 0)
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FoldersUiState()
    )

    init {
        refreshCounts()
    }

    fun onSearchChanged(query: String) {
        searchQuery.value = query
    }

    private fun refreshCounts() {
        viewModelScope.launch {
            smartCounts.value = SmartCollectionCounts(
                allNotes = noteRepository.getActiveNoteCount(),
                favorites = noteRepository.getFavoriteCount(),
                archive = noteRepository.getArchivedCount()
            )
        }
    }

    class Factory(
        private val folderRepository: FolderRepository,
        private val noteRepository: NoteRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FoldersViewModel(folderRepository, noteRepository) as T
        }
    }
}
