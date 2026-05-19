package com.example.notesapp.ui.share.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.R
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.share.NoteShareRepository
import com.example.notesapp.ui.share.model.SharedUserUiModel
import com.example.notesapp.ui.share.model.buildSharedUserUiModels
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SharedUsersUiState(
    val noteId: String = "",
    val noteTitle: String = "",
    val isLoading: Boolean = true,
    val users: List<SharedUserUiModel> = emptyList(),
    val errorMessageRes: Int? = null
)

@HiltViewModel
class SharedUsersViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val noteShareRepository: NoteShareRepository,
    private val authManager: AuthManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(SharedUsersUiState())
    val uiState: StateFlow<SharedUsersUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null
    private var loadedNoteId: String? = null
    fun load(noteId: String) {
        if (loadedNoteId == noteId) return
        loadedNoteId = noteId
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            noteShareRepository.observeNoteShares(noteId).collect { shares ->
                _uiState.update { state ->
                    state.copy(
                        noteId = noteId,
                        users = buildSharedUserUiModels(authManager.profileEmail.value, shares),
                        isLoading = false
                    )
                }
            }
        }
        viewModelScope.launch {
            val title = noteRepository.getNoteById(noteId)?.title.orEmpty()
            _uiState.update { it.copy(noteId = noteId, noteTitle = title) }
            refresh()
        }
    }
    fun refresh() {
        val noteId = loadedNoteId ?: return
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessageRes = null) }
                noteShareRepository.refreshNoteShares(noteId)
                _uiState.update { it.copy(isLoading = false) }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessageRes = R.string.shared_users_error
                    )
                }
            }
        }
    }
}
