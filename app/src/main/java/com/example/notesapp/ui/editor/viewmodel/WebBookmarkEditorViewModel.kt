package com.example.notesapp.ui.editor.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.bookmark.WebBookmarkMetadata
import com.example.notesapp.domain.bookmark.WebBookmarkMetadataSource
import com.example.notesapp.domain.bookmark.WebBookmarkUrlValidator
import com.example.notesapp.domain.bookmark.WebBookmarkValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WebBookmarkEditorUiState(
    val isEditMode: Boolean = false,
    val url: String = "",
    val isSaving: Boolean = false,
    val showInvalidUrlError: Boolean = false
) {
    private val validation: WebBookmarkValidationResult
        get() = WebBookmarkUrlValidator.validate(url)

    val isUrlValid: Boolean
        get() = validation is WebBookmarkValidationResult.Valid

    val isSaveEnabled: Boolean
        get() = isUrlValid && !isSaving

    /**
     * True when the draft is a valid HTTP URL: cleartext metadata enrichment is unavailable by
     * design, so the page explains the host/blank fallback before the user submits.
     */
    val showsMetadataFallbackStatus: Boolean
        get() = validation is WebBookmarkValidationResult.Valid &&
            !url.trim().startsWith("https://", ignoreCase = true)
}

/** Resolved bookmark produced by a successful full-page save; consumed by the originating editor. */
data class WebBookmarkSaveResult(
    val blockId: String,
    val url: String,
    val title: String,
    val description: String
)

@HiltViewModel
class WebBookmarkEditorViewModel @Inject constructor(
    private val metadataSource: WebBookmarkMetadataSource,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val editingBlockId: String = savedStateHandle.get<String>(ARG_BLOCK_ID).orEmpty()
    val isEditMode: Boolean = editingBlockId.isNotBlank()
    private val initialUrl = savedStateHandle.get<String>(ARG_INITIAL_URL).orEmpty()
    private val initialTitle = savedStateHandle.get<String>(ARG_INITIAL_TITLE).orEmpty()
    private val initialDescription = savedStateHandle.get<String>(ARG_INITIAL_DESCRIPTION).orEmpty()

    private val uiStateInternal = MutableStateFlow(
        WebBookmarkEditorUiState(
            isEditMode = isEditMode,
            url = savedStateHandle.get<String>(KEY_URL_DRAFT)
                ?: savedStateHandle.get<String>(ARG_INITIAL_URL).orEmpty()
        )
    )
    val uiState: StateFlow<WebBookmarkEditorUiState> = uiStateInternal.asStateFlow()

    private val saveResultChannel = Channel<WebBookmarkSaveResult>(Channel.BUFFERED)
    val saveResult: Flow<WebBookmarkSaveResult> = saveResultChannel.receiveAsFlow()

    fun onUrlChanged(value: String) {
        savedStateHandle[KEY_URL_DRAFT] = value
        uiStateInternal.update { it.copy(url = value, showInvalidUrlError = false) }
    }

    /**
     * Validates the draft, resolves metadata for eligible HTTPS URLs on submit (never per
     * keystroke), and emits one [WebBookmarkSaveResult]. Metadata failures fall back to
     * host/blank so a valid bookmark still saves; HTTP drafts skip retrieval entirely.
     */
    fun save() {
        val current = uiStateInternal.value
        if (current.isSaving) return
        when (val validated = WebBookmarkUrlValidator.validate(current.url)) {
            is WebBookmarkValidationResult.Invalid ->
                uiStateInternal.update { it.copy(showInvalidUrlError = true) }
            is WebBookmarkValidationResult.Valid -> viewModelScope.launch {
                uiStateInternal.update { it.copy(isSaving = true) }
                val metadata = if (isEditMode && validated.url == initialUrl.trim()) {
                    WebBookmarkMetadata(
                        title = initialTitle,
                        description = initialDescription
                    )
                } else if (validated.url.startsWith("http://", ignoreCase = true)) {
                    WebBookmarkMetadata.fallback(validated.url)
                } else {
                    metadataSource.fetch(validated.url)
                        ?: WebBookmarkMetadata.fallback(validated.url)
                }
                uiStateInternal.update { it.copy(isSaving = false) }
                saveResultChannel.send(
                    WebBookmarkSaveResult(
                        blockId = editingBlockId,
                        url = validated.url,
                        title = metadata.title,
                        description = metadata.description
                    )
                )
            }
        }
    }

    private companion object {
        const val ARG_BLOCK_ID = "blockId"
        const val ARG_INITIAL_URL = "initialUrl"
        const val ARG_INITIAL_TITLE = "initialTitle"
        const val ARG_INITIAL_DESCRIPTION = "initialDescription"
        const val KEY_URL_DRAFT = "web_bookmark_url_draft"
    }
}
