package com.example.notesapp.ui.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.Note
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

sealed class FolderTreeItem {
    data class FolderItem(val folder: Folder, val depth: Int, val noteCount: Int) : FolderTreeItem()
    data class NoteItem(val note: Note, val depth: Int) : FolderTreeItem()
}

data class FoldersUiState(
    val smartCounts: SmartCollectionCounts = SmartCollectionCounts(),
    val treeItems: List<FolderTreeItem> = emptyList(),
    val isSearchActive: Boolean = false
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
    private val allNotes = noteRepository.getActiveNotes()

    val uiState: StateFlow<FoldersUiState> = combine(
        allFolders, allNotes, searchQuery, smartCounts, folderCounts
    ) { folders, notes, query, counts, perFolderCounts ->
        val items = if (query.isBlank()) {
            buildTree(folders, notes, null, 0, perFolderCounts)
        } else {
            // Flattened search results
            folders.filter { it.name.contains(query, ignoreCase = true) }
                .map { FolderTreeItem.FolderItem(it, 0, perFolderCounts[it.id] ?: 0) }
        }
        
        FoldersUiState(
            smartCounts = counts,
            treeItems = items,
            isSearchActive = query.isNotBlank()
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

    fun addFolder(name: String, parentId: Long? = null) {
        viewModelScope.launch {
            folderRepository.insert(
                Folder(
                    name = name,
                    parentFolderId = parentId,
                    createdAt = System.currentTimeMillis()
                )
            )
            refreshCounts()
        }
    }

    private fun buildTree(
        folders: List<Folder>,
        notes: List<Note>,
        parentId: Long?,
        depth: Int,
        perFolderCounts: Map<Long, Int>
    ): List<FolderTreeItem> {
        val result = mutableListOf<FolderTreeItem>()
        
        folders.filter { it.parentFolderId == parentId }.forEach { folder ->
            result.add(FolderTreeItem.FolderItem(folder, depth, perFolderCounts[folder.id] ?: 0))
            
            // Add notes in this folder
            notes.filter { it.folderId == folder.id }.forEach { note ->
                result.add(FolderTreeItem.NoteItem(note, depth + 1))
            }
            
            // Recursively add subfolders
            result.addAll(buildTree(folders, notes, folder.id, depth + 1, perFolderCounts))
        }
        
        return result
    }
}
