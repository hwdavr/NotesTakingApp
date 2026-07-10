package com.example.notesapp.ui.folderdescription.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.R
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class FolderDescriptionUiState(
    val isLoading: Boolean = true,
    val folderName: String = "",
    val description: String = "",
    val isSaving: Boolean = false,
    val errorMessageRes: Int? = null,
    val canSave: Boolean = false
)

sealed interface FolderDescriptionEvent {
    data object NavigateBack : FolderDescriptionEvent
}

@HiltViewModel
class FolderDescriptionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val folderRepository: FolderRepository
) : ViewModel() {
    private val folderId: String = savedStateHandle["folderId"] ?: ""
    private val _uiState = MutableStateFlow(FolderDescriptionUiState())
    val uiState: StateFlow<FolderDescriptionUiState> = _uiState.asStateFlow()

    private val events = Channel<FolderDescriptionEvent>(Channel.BUFFERED)
    val navigationEvents = events.receiveAsFlow()

    private var currentFolder: Folder? = null
    private var originalDescription = ""

    init {
        loadFolder()
    }

    fun onDescriptionChanged(description: String) {
        _uiState.value = _uiState.value.copy(
            description = description,
            canSave = description.trim() != originalDescription
        )
    }

    fun save() {
        val folder = currentFolder ?: return
        val description = _uiState.value.description.trim()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessageRes = null)
            try {
                folderRepository.updateDescription(folder, description)
                Log.d(TAG, "Folder description saved; descriptionLength=${description.length}")
                events.trySend(FolderDescriptionEvent.NavigateBack)
            } catch (e: Exception) {
                Log.w(TAG, "Folder description save failed; cause=${e.javaClass.simpleName}")
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessageRes = R.string.folder_description_save_error
                )
            }
        }
    }

    fun cancel() {
        events.trySend(FolderDescriptionEvent.NavigateBack)
    }

    private fun loadFolder() {
        if (folderId.isBlank()) {
            _uiState.value = FolderDescriptionUiState(
                isLoading = false,
                errorMessageRes = R.string.folder_description_missing_error
            )
            return
        }
        viewModelScope.launch {
            val folder = folderRepository.getFolder(folderId).first()
            if (folder == null) {
                currentFolder = null
                _uiState.value = FolderDescriptionUiState(
                    isLoading = false,
                    errorMessageRes = R.string.folder_description_missing_error
                )
            } else {
                currentFolder = folder
                originalDescription = folder.description
                _uiState.value = FolderDescriptionUiState(
                    isLoading = false,
                    folderName = folder.name,
                    description = folder.description,
                    canSave = false
                )
            }
        }
    }

    private companion object {
        const val TAG = "NotesApp/FolderDescriptionViewModel"
    }
}
