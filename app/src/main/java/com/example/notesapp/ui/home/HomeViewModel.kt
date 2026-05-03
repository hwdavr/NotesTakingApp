package com.example.notesapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.ui.editor.document.noteContentPreview
import com.example.notesapp.ui.notes.NoteUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
open class HomeViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {

    private val selectedFolderId = MutableStateFlow("all_notes")

    init {
        viewModelScope.launch {
            folderRepository.sync()
        }
    }

    fun selectFolder(id: String) {
        selectedFolderId.value = id
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

    open val uiState: StateFlow<HomeUiState> = combine(
        noteRepository.getActiveNotes(),
        folderRepository.getFolders(),
        selectedFolderId
    ) { notes, folders, selectedId ->
        val filteredNotes = when (selectedId) {
            "all_notes" -> notes
            "favorites" -> {
                val favFolder = folders.find { it.name.equals("Favorites", ignoreCase = true) }
                if (favFolder != null) {
                    notes.filter { it.folderId == favFolder.id }
                } else {
                    emptyList()
                }
            }
            else -> notes.filter { it.folderId == selectedId }
        }

        val noteCountsByFolder = notes
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
                    isFavorite = note.isFavorite
                )
            },
            noteActions = filteredNotes.associateBy { it.id },
            recentFolders = folders.map { folder ->
                FolderUiModel(
                    id = folder.id,
                    name = folder.name,
                    noteCount = noteCountsByFolder[folder.id] ?: 0,
                    isPrimary = folder.name.equals("Favorites", ignoreCase = true)
                )
            },
            selectedFolderId = selectedId,
            isLoading = false
        )
    }
.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )
}
