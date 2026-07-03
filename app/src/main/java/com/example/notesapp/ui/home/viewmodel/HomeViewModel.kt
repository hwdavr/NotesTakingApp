package com.example.notesapp.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.ui.editor.mapper.noteContentPreview
import com.example.notesapp.ui.home.model.FolderUiModel
import com.example.notesapp.ui.home.model.HomeUiState
import com.example.notesapp.ui.notes.model.NoteUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
open class HomeViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {
    private val selectedFolderId = MutableStateFlow("all_notes")
    private val isRefreshing = MutableStateFlow(false)
    private val initialLoadComplete = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            try {
                folderRepository.sync()
                noteRepository.sync()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                initialLoadComplete.value = true
            }
        }
    }
    fun selectFolder(id: String) {
        selectedFolderId.value = id
    }
    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            try {
                folderRepository.sync()
                noteRepository.sync()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isRefreshing.value = false
            }
        }
    }
    fun renameNote(note: Note, newName: String) {
        viewModelScope.launch {
            noteRepository.save(note.copy(title = newName, updatedAt = System.currentTimeMillis()))
        }
    }
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.delete(note)
        }
    }
    fun addNoteToFavorites(note: Note) {
        viewModelScope.launch {
            noteRepository.toggleFavorite(note)
        }
    }
    private data class HomeInputs(
        val notes: List<Note>,
        val shared: List<Note>,
        val folders: List<Folder>,
        val selectedId: String,
        val refreshing: Boolean
    )

    open val uiState: StateFlow<HomeUiState> = combine(
        noteRepository.getActiveNotes(),
        noteRepository.getSharedNotes(),
        folderRepository.getFolders(),
        selectedFolderId,
        isRefreshing
    ) { notes, shared, folders, selectedId, refreshing ->
        HomeInputs(notes, shared, folders, selectedId, refreshing)
    }.combine(initialLoadComplete) { inputs, loadComplete ->
        val filteredNotes = when (inputs.selectedId) {
            "all_notes" -> inputs.notes + inputs.shared
            "shared" -> inputs.shared
            "favorites" -> {
                inputs.notes.filter { it.isFavorite }
            }
            else -> inputs.notes.filter { it.folderId == inputs.selectedId }
        }
        val noteCountsByFolder = (inputs.notes + inputs.shared)
            .mapNotNull { note -> note.folderId }
            .groupingBy { it }
            .eachCount()
        HomeUiState(
            recentNotes = filteredNotes.map { note ->
                NoteUiModel(
                    id = note.id,
                    title = note.title,
                    preview = noteContentPreview(note.content),
                    colorIndex = note.id.hashCode().mod(4).let { if (it < 0) it + 4 else it },
                    isFavorite = note.isFavorite,
                    isShared = note.isShared
                )
            },
            noteActions = (inputs.notes + inputs.shared).associateBy { it.id },
            recentFolders = listOf(
                FolderUiModel(
                    id = "all_notes",
                    name = "All Notes",
                    noteCount = inputs.notes.size,
                    isPrimary = false,
                    isShared = false
                ),
                FolderUiModel(
                    id = "shared",
                    name = "Shared",
                    noteCount = inputs.shared.size,
                    isPrimary = false,
                    isShared = true
                )
            ) + inputs.folders.map { folder ->
                FolderUiModel(
                    id = folder.id,
                    name = folder.name,
                    noteCount = noteCountsByFolder[folder.id] ?: 0,
                    isPrimary = folder.name.equals("Favorites", ignoreCase = true),
                    isShared = folder.isShared
                )
            },
            selectedFolderId = inputs.selectedId,
            isLoading = !loadComplete,
            isRefreshing = inputs.refreshing
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(15_000),
            initialValue = HomeUiState(isLoading = true)
        )
}
