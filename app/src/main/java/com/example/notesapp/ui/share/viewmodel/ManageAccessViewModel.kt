package com.example.notesapp.ui.share.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.R
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.share.NoteShareRepository
import com.example.notesapp.ui.share.model.ManageAccessPermission
import com.example.notesapp.ui.share.model.ManageAccessUserUiModel
import com.example.notesapp.ui.share.model.buildManageAccessUserUiModels
import com.example.notesapp.ui.share.model.toNoteShareAccessRole
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManageAccessUiState(
    val noteId: String = "",
    val noteTitle: String = "",
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val users: List<ManageAccessUserUiModel> = emptyList(),
    val errorMessageRes: Int? = null
) {
    val isConfirmEnabled: Boolean
        get() = !isLoading && !isSubmitting && users.any { it.currentPermission != it.selectedPermission }
}

sealed interface ManageAccessEvent {
    data object ConfirmSucceeded : ManageAccessEvent
}

@HiltViewModel
class ManageAccessViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val noteShareRepository: NoteShareRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ManageAccessUiState())
    val uiState: StateFlow<ManageAccessUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ManageAccessEvent>()
    val events: SharedFlow<ManageAccessEvent> = _events.asSharedFlow()

    private var observeJob: Job? = null
    private var loadedNoteId: String? = null
    private var selectedPermissions: Map<String, ManageAccessPermission> = emptyMap()

    fun load(noteId: String) {
        if (loadedNoteId == noteId) return
        loadedNoteId = noteId
        selectedPermissions = emptyMap()
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            noteShareRepository.observeNoteShares(noteId).collect { shares ->
                _uiState.update { state ->
                    state.copy(
                        noteId = noteId,
                        users = buildManageAccessUserUiModels(shares, selectedPermissions),
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

    fun onPermissionSelected(userId: String, permission: ManageAccessPermission) {
        selectedPermissions = selectedPermissions.toMutableMap().apply {
            put(userId, permission)
        }
        _uiState.update { state ->
            state.copy(
                users = state.users.map { user ->
                    if (user.id == userId) {
                        user.copy(selectedPermission = permission)
                    } else {
                        user
                    }
                },
                errorMessageRes = null
            )
        }
    }

    fun confirmChanges() {
        val state = _uiState.value
        val noteId = state.noteId
        if (noteId.isBlank()) return
        val changedUsers = state.users.filter { it.currentPermission != it.selectedPermission }
        if (changedUsers.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessageRes = null) }
            try {
                changedUsers.forEach { user ->
                    if (user.selectedPermission == ManageAccessPermission.DELETE) {
                        noteShareRepository.deleteNoteShare(noteId, user.id)
                    } else {
                        noteShareRepository.updateNoteShareRole(
                            noteId = noteId,
                            shareId = user.id,
                            accessRole = user.selectedPermission.toNoteShareAccessRole()
                        )
                    }
                }
                selectedPermissions = emptyMap()
                _uiState.update { it.copy(isSubmitting = false) }
                _events.emit(ManageAccessEvent.ConfirmSucceeded)
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessageRes = R.string.manage_access_error
                    )
                }
            }
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
                        errorMessageRes = R.string.manage_access_error
                    )
                }
            }
        }
    }
}
