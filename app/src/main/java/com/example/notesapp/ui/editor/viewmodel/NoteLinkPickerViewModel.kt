package com.example.notesapp.ui.editor.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class NoteLinkPickerItem(
    val id: String,
    val title: String,
    val folderName: String?
)

sealed interface NoteLinkPickerUiState {
    data object Loading : NoteLinkPickerUiState
    data class Content(
        val notes: List<NoteLinkPickerItem>,
        val searchQuery: String = "",
        val hasExistingLink: Boolean = false
    ) : NoteLinkPickerUiState
    data class Empty(
        val searchQuery: String = "",
        val hasExistingLink: Boolean = false
    ) : NoteLinkPickerUiState
    data class Error(
        val message: String = "",
        val hasExistingLink: Boolean = false
    ) : NoteLinkPickerUiState
}

@HiltViewModel
class NoteLinkPickerViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val callerNoteId: String = savedStateHandle.get<String>("callerNoteId").orEmpty()
    val hasExistingLink: Boolean = savedStateHandle.get<Boolean>("hasExistingLink") ?: false

    private val searchQuery = MutableStateFlow("")
    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<NoteLinkPickerUiState> = combine(
        noteRepository.getActiveNotes(),
        folderRepository.getFolders(),
        searchQuery,
        retryTrigger
    ) { activeNotes, folders, query, _ ->
        val folderMap = folders.associateBy { it.id }
        val candidateItems = activeNotes
            .filter { it.id != callerNoteId }
            .map { note ->
                NoteLinkPickerItem(
                    id = note.id,
                    title = note.title,
                    folderName = note.folderId?.let { folderMap[it]?.name }
                )
            }

        val filteredItems = if (query.isBlank()) {
            candidateItems
        } else {
            val normalized = query.trim().lowercase()
            candidateItems.filter { item ->
                item.title.lowercase().contains(normalized) ||
                    (item.folderName != null && item.folderName.lowercase().contains(normalized))
            }
        }

        if (filteredItems.isEmpty()) {
            NoteLinkPickerUiState.Empty(
                searchQuery = query,
                hasExistingLink = hasExistingLink
            )
        } else {
            NoteLinkPickerUiState.Content(
                notes = filteredItems,
                searchQuery = query,
                hasExistingLink = hasExistingLink
            )
        }
    }.catch { error ->
        emit(
            NoteLinkPickerUiState.Error(
                message = error.message.orEmpty(),
                hasExistingLink = hasExistingLink
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NoteLinkPickerUiState.Loading
    )

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun retry() {
        retryTrigger.value++
    }
}
