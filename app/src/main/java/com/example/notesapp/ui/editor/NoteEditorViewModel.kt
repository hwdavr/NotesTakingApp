package com.example.notesapp.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class NoteEditorUiState(
    val noteId: String? = null,
    val title: String = "",
    val content: String = "",
    val folderId: String? = null,
    val availableFolders: List<Folder> = emptyList(),
    val createdAt: Long = 0L,
    val isLoaded: Boolean = false
)

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private var autoSaveJob: Job? = null

    fun load(noteId: String?, folderId: String? = null) {
        viewModelScope.launch {
            folderRepository.sync()
            val folders = folderRepository.getFolders().first()
            if (noteId.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    noteId = "note_${UUID.randomUUID()}",
                    availableFolders = folders,
                    folderId = folderId,
                    isLoaded = true
                )
                return@launch
            }

            val note = noteRepository.getNoteById(noteId)
            _uiState.value = if (note != null) {
                NoteEditorUiState(
                    noteId = note.id,
                    title = note.title,
                    content = note.content,
                    folderId = note.folderId,
                    availableFolders = folders,
                    createdAt = note.createdAt,
                    isLoaded = true
                )
            } else {
                NoteEditorUiState(
                    noteId = "note_${UUID.randomUUID()}",
                    availableFolders = folders,
                    isLoaded = true
                )
            }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
        scheduleAutoSave()
    }

    fun onContentChange(value: String) {
        _uiState.value = _uiState.value.copy(content = value)
        scheduleAutoSave()
    }

    fun onFolderSelected(folderId: String?) {
        _uiState.value = _uiState.value.copy(folderId = folderId)
        scheduleAutoSave()
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(2000)
            saveInternally()
        }
    }

    private suspend fun saveInternally() {
        val current = _uiState.value
        // Don't auto-save if both title and content are empty
        if (current.title.isBlank() && current.content.isBlank()) return

        val now = System.currentTimeMillis()
        val noteId = current.noteId ?: "note_${UUID.randomUUID()}"
        val note = Note(
            id = noteId,
            title = current.title.ifBlank { "Untitled note" },
            content = current.content,
            folderId = current.folderId,
            sortKey = now.toString(),
            deviceId = "",
            createdAt = if (current.createdAt == 0L) now else current.createdAt,
            updatedAt = now
        )
        noteRepository.save(note)
        
        // Update state with generated ID and createdAt to avoid duplicate creations
        _uiState.value = _uiState.value.copy(
            noteId = noteId,
            createdAt = note.createdAt
        )
    }

    fun save(onDone: () -> Unit) {
        autoSaveJob?.cancel()
        viewModelScope.launch {
            saveInternally()
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val current = _uiState.value
        // If not saved yet, just finish
        if (current.createdAt == 0L) {
            onDone()
            return
        }

        viewModelScope.launch {
            noteRepository.delete(
                Note(
                    id = current.noteId.orEmpty(),
                    title = current.title,
                    content = current.content,
                    folderId = current.folderId,
                    sortKey = "",
                    version = 0,
                    deviceId = "",
                    createdAt = current.createdAt,
                    updatedAt = System.currentTimeMillis()
                )
            )
            onDone()
        }
    }
}
