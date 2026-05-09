package com.example.notesapp.ui.share.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.R
import com.example.notesapp.domain.share.NoteShareAccessRole
import com.example.notesapp.domain.share.NoteShareRepository
import com.example.notesapp.ui.share.model.isValidInviteEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ShareInviteUiState(
    val noteId: String = "",
    val email: String = "",
    val selectedRole: NoteShareAccessRole = NoteShareAccessRole.EDITOR,
    val isSubmitting: Boolean = false,
    val errorMessageRes: Int? = null
) {
    val isInviteEnabled: Boolean
        get() = !isSubmitting && isValidInviteEmail(email)
}
sealed interface ShareInviteEvent {
    data object InviteSucceeded : ShareInviteEvent
}
@HiltViewModel
class ShareInviteViewModel @Inject constructor(
    private val noteShareRepository: NoteShareRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShareInviteUiState())
    val uiState: StateFlow<ShareInviteUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<ShareInviteEvent>()
    val events: SharedFlow<ShareInviteEvent> = _events.asSharedFlow()
    fun load(noteId: String) {
        if (_uiState.value.noteId == noteId) return
        _uiState.value = ShareInviteUiState(noteId = noteId)
    }
    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessageRes = null) }
    }
    fun onRoleSelected(role: NoteShareAccessRole) {
        _uiState.update { it.copy(selectedRole = role, errorMessageRes = null) }
    }
    fun invite() {
        val state = _uiState.value
        if (!isValidInviteEmail(state.email)) {
            _uiState.update { it.copy(errorMessageRes = R.string.share_invite_invalid_email_error) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessageRes = null) }
            try {
                noteShareRepository.inviteNoteShare(
                    noteId = state.noteId,
                    email = state.email.trim(),
                    accessRole = state.selectedRole
                )
                _uiState.update { it.copy(isSubmitting = false) }
                _events.emit(ShareInviteEvent.InviteSucceeded)
            } catch (exception: Exception) {
                val errorRes = when ((exception as? HttpException)?.code()) {
                    409 -> R.string.share_invite_duplicate_error
                    else -> R.string.share_invite_generic_error
                }
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessageRes = errorRes
                    )
                }
            }
        }
    }
}
