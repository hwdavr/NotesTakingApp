package com.example.notesapp.ui.editor.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.notesapp.MainDispatcherRule
import com.example.notesapp.domain.bookmark.WebBookmarkMetadata
import com.example.notesapp.domain.bookmark.WebBookmarkMetadataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WebBookmarkEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun addModeStartsEmptyAndDisablesSave() {
        val viewModel = viewModel(savedStateHandle = SavedStateHandle())

        val state = viewModel.uiState.value

        assertFalse(state.isEditMode)
        assertEquals("", state.url)
        assertFalse(state.isSaveEnabled)
        assertFalse(state.showInvalidUrlError)
    }

    @Test
    fun invalidDraftShowsInlineErrorWithoutRequestingMetadata() {
        val source = RecordingMetadataSource()
        val viewModel = viewModel(source)

        viewModel.onUrlChanged("ftp://example.com/file")
        viewModel.save()

        assertTrue(viewModel.uiState.value.showInvalidUrlError)
        assertEquals(0, source.requestedUrls.size)
    }

    @Test
    fun saveResolvesMetadataFromTheSource() = runBlocking {
        val source = RecordingMetadataSource(
            metadata = WebBookmarkMetadata(title = "Resolved title", description = "Resolved description")
        )
        val viewModel = viewModel(source)

        viewModel.onUrlChanged("https://example.com/page")
        viewModel.save()

        val result = withTimeout(5_000) { viewModel.saveResult.first() }
        assertEquals("", result.blockId)
        assertEquals("https://example.com/page", result.url)
        assertEquals("Resolved title", result.title)
        assertEquals("Resolved description", result.description)
        assertEquals(listOf("https://example.com/page"), source.requestedUrls)
    }

    @Test
    fun saveFallsBackToHostWhenMetadataIsUnavailable() = runBlocking {
        val source = RecordingMetadataSource(metadata = null)
        val viewModel = viewModel(source)

        viewModel.onUrlChanged("https://example.com/page")
        viewModel.save()

        val result = withTimeout(5_000) { viewModel.saveResult.first() }
        assertEquals("example.com", result.title)
        assertEquals("", result.description)
    }

    @Test
    fun httpDraftSavesWithHostFallback() = runBlocking {
        // The production HTTPS-only source resolves cleartext URLs to null, so the save falls back.
        val source = RecordingMetadataSource(
            metadata = WebBookmarkMetadata(title = "Should not be used", description = "unused")
        )
        val viewModel = viewModel(source)

        viewModel.onUrlChanged("http://example.com/cleartext")
        assertTrue(viewModel.uiState.value.showsMetadataFallbackStatus)
        viewModel.save()

        val result = withTimeout(5_000) { viewModel.saveResult.first() }
        assertEquals("example.com", result.title)
        assertEquals("", result.description)
    }

    @Test
    fun restoresUrlDraftFromSavedStateHandle() {
        val handle = SavedStateHandle()
        val viewModel = viewModel(savedStateHandle = handle)

        viewModel.onUrlChanged("https://example.com/draft")

        val restoredHandle = SavedStateHandle(mapOf("web_bookmark_url_draft" to "https://example.com/draft"))
        val restored = viewModel(savedStateHandle = restoredHandle)

        assertEquals("https://example.com/draft", restored.uiState.value.url)
        assertTrue(restored.uiState.value.isSaveEnabled)
    }

    @Test
    fun editModeCarriesBlockIdIntoTheSaveResult() = runBlocking {
        val source = RecordingMetadataSource(
            metadata = WebBookmarkMetadata(title = "Edited title", description = "")
        )
        val viewModel = viewModel(
            source = source,
            savedStateHandle = SavedStateHandle(mapOf("blockId" to "block-42"))
        )

        assertTrue(viewModel.uiState.value.isEditMode)
        viewModel.onUrlChanged("https://example.com/edited")
        viewModel.save()

        val result = withTimeout(5_000) { viewModel.saveResult.first() }
        assertEquals("block-42", result.blockId)
        assertEquals("Edited title", result.title)
    }

    private fun viewModel(
        source: WebBookmarkMetadataSource = RecordingMetadataSource(),
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ): WebBookmarkEditorViewModel = WebBookmarkEditorViewModel(
        metadataSource = source,
        savedStateHandle = savedStateHandle
    )

    private class RecordingMetadataSource(
        private val metadata: WebBookmarkMetadata? = null
    ) : WebBookmarkMetadataSource {
        val requestedUrls = mutableListOf<String>()

        override suspend fun fetch(url: String): WebBookmarkMetadata? {
            requestedUrls += url
            return if (url.startsWith("https://")) metadata else null
        }
    }
}
