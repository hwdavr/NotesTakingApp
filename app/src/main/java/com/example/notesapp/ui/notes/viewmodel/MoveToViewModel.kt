package com.example.notesapp.ui.notes.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MoveToFolderDestination(
    val id: String?,
    val name: String,
    val depth: Int = 0
)
data class MoveToUiState(
    val itemType: String = "",
    val itemId: String = "",
    val searchQuery: String = "",
    val rootDestination: MoveToFolderDestination = MoveToFolderDestination(id = null, name = ""),
    val recentFolders: List<MoveToFolderDestination> = emptyList(),
    val folderResults: List<MoveToFolderDestination> = emptyList(),
    val canMove: Boolean = false
)
@HiltViewModel
class MoveToViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val itemType: String = savedStateHandle["itemType"] ?: ""
    private val itemId: String = savedStateHandle["itemId"] ?: ""
    private val searchQuery = MutableStateFlow("")
    private var latestFolders: List<Folder> = emptyList()
    private var latestNotes: List<Note> = emptyList()
    private val allFolders = folderRepository.getFolders()
    private val allNotes = noteRepository.getActiveNotes()
    val uiState: StateFlow<MoveToUiState> = combine(
        allFolders,
        allNotes,
        searchQuery
    ) { folders, notes, query ->
        latestFolders = folders
        latestNotes = notes
        val movingFolder = folders.firstOrNull { it.id == itemId }.takeIf { itemType == ITEM_TYPE_FOLDER }
        val movingNote = notes.firstOrNull { it.id == itemId }.takeIf { itemType == ITEM_TYPE_NOTE }
        val eligibleFolders = buildEligibleFolderDestinations(
            folders = folders,
            movingFolder = movingFolder
        )
        val normalizedQuery = query.trim()
        val filteredFolders = if (normalizedQuery.isBlank()) {
            eligibleFolders
        } else {
            eligibleFolders.filter { it.name.contains(normalizedQuery, ignoreCase = true) }
        }
        MoveToUiState(
            itemType = itemType,
            itemId = itemId,
            searchQuery = query,
            recentFolders = eligibleFolders.take(6),
            folderResults = filteredFolders,
            canMove = movingFolder != null || movingNote != null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MoveToUiState(itemType = itemType, itemId = itemId)
    )
    fun onSearchChanged(query: String) {
        searchQuery.value = query
    }
    fun moveTo(destinationFolderId: String?, onMoved: () -> Unit) {
        viewModelScope.launch {
            val state = uiState.value
            if (!state.canMove) return@launch
            when (state.itemType) {
                ITEM_TYPE_FOLDER -> {
                    val folders = latestFolders
                    val folder = folders.firstOrNull { it.id == state.itemId } ?: return@launch
                    if (destinationFolderId == folder.id || isDescendant(folders, folder.id, destinationFolderId)) {
                        return@launch
                    }
                    folderRepository.move(folder, destinationFolderId)
                }
                ITEM_TYPE_NOTE -> {
                    val note = latestNotes.firstOrNull { it.id == state.itemId } ?: return@launch
                    noteRepository.move(note, destinationFolderId)
                }
            }
            onMoved()
        }
    }
    private fun buildEligibleFolderDestinations(
        folders: List<Folder>,
        movingFolder: Folder?
    ): List<MoveToFolderDestination> {
        val invalidIds = if (movingFolder == null) {
            emptySet()
        } else {
            setOf(movingFolder.id) + collectDescendantFolderIds(folders, movingFolder.id)
        }
        fun build(parentId: String?, depth: Int): List<MoveToFolderDestination> = folders
            .filter { it.parentFolderId == parentId && it.id !in invalidIds }
            .flatMap { folder ->
                listOf(MoveToFolderDestination(folder.id, folder.name, depth)) +
                    build(folder.id, depth + 1)
            }
        return build(parentId = null, depth = 0)
    }
    private fun collectDescendantFolderIds(folders: List<Folder>, parentId: String): Set<String> {
        val directChildren = folders.filter { it.parentFolderId == parentId }
        return directChildren.map { it.id }.toSet() +
            directChildren.flatMap { collectDescendantFolderIds(folders, it.id) }
    }
    private fun isDescendant(folders: List<Folder>, folderId: String, possibleDescendantId: String?): Boolean {
        if (possibleDescendantId == null) return false
        return possibleDescendantId in collectDescendantFolderIds(folders, folderId)
    }
    companion object {
        const val ITEM_TYPE_FOLDER = "folder"
        const val ITEM_TYPE_NOTE = "note"
    }
}
