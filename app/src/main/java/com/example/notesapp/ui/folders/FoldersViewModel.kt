package com.example.notesapp.ui.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SmartCollectionCounts(
    val allNotes: Int = 0,
    val favorites: Int = 0,
    val archive: Int = 0
)

data class FolderItemUi(
    val folder: Folder,
    val noteCount: Int
)

data class FoldersUiState(
    val smartCounts: SmartCollectionCounts = SmartCollectionCounts(),
    val folders: List<FolderItemUi> = emptyList()
)

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val smartCounts = MutableStateFlow(SmartCollectionCounts())
    private val folderCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())

    private val allFolders = folderRepository.getFolders()

    val uiState: StateFlow<FoldersUiState> = combine(
        allFolders, searchQuery, smartCounts, folderCounts
    ) { folders, query, counts, perFolderCounts ->
        val visibleFolders = if (query.isBlank()) {
            folders
        } else {
            folders.filter { it.name.contains(query, ignoreCase = true) }
        }
        FoldersUiState(
            smartCounts = counts,
            folders = visibleFolders.map { folder ->
                FolderItemUi(folder = folder, noteCount = perFolderCounts[folder.id] ?: 0)
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
            val folders = folderRepository.getFolders().first()

            smartCounts.value = SmartCollectionCounts(
                allNotes = noteRepository.getActiveNoteCount(),
                favorites = noteRepository.getFavoriteCount(),
                archive = noteRepository.getArchivedCount()
            )

            folderCounts.value = folders.associate { folder ->
                folder.id to async { noteRepository.getActiveNoteCountForFolder(folder.id) }.await()
            }
        }
    }
}
