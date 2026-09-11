package com.example.notesapp.ui.editor.screen

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.FakeWebBookmarkMetadataSource
import com.example.notesapp.domain.bookmark.WebBookmarkMetadata
import com.example.notesapp.ui.editor.viewmodel.WebBookmarkEditorUiState
import com.example.notesapp.ui.editor.viewmodel.WebBookmarkEditorViewModel
import com.example.notesapp.ui.editor.viewmodel.WebBookmarkSaveResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebBookmarkEditorScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val collectionScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @After
    fun tearDown() {
        collectionScope.cancel()
    }

    @Test
    fun showsUrlOnlyEditorWithBottomActions() {
        composeRule.setContent {
            WebBookmarkEditorScreenContent(
                uiState = WebBookmarkEditorUiState(isEditMode = false),
                onUrlChanged = {},
                onSave = {},
                onBack = {}
            )
        }

        composeRule.onNodeWithTag("web_bookmark_editor_page").assertIsDisplayed()
        composeRule.onNodeWithTag("web_bookmark_editor_page_title")
            .assertTextEquals("Add Web Bookmark")
        composeRule.onNodeWithTag("web_bookmark_url_field").assertIsDisplayed()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(1)
        composeRule.onNodeWithTag("web_bookmark_back_button").assertIsDisplayed()
        composeRule.onNodeWithTag("web_bookmark_bottom_actions").assertIsDisplayed()
        composeRule.onNodeWithTag("web_bookmark_save_button").assertIsDisplayed().assertIsNotEnabled()
        composeRule.onNodeWithTag("web_bookmark_cancel_button").assertIsDisplayed()
    }

    @Test
    fun invalidUrlShowsInlineErrorWithoutSaving() {
        val metadataSource = FakeWebBookmarkMetadataSource(
            WebBookmarkMetadata(title = "title", description = "description")
        )
        val viewModel = WebBookmarkEditorViewModel(
            metadataSource = metadataSource,
            savedStateHandle = SavedStateHandle()
        )
        val emittedResults = mutableListOf<WebBookmarkSaveResult>()
        collectionScope.launch { viewModel.saveResult.collect { emittedResults += it } }

        composeRule.setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            WebBookmarkEditorScreenContent(
                uiState = state,
                onUrlChanged = viewModel::onUrlChanged,
                onSave = viewModel::save,
                onBack = {}
            )
        }

        val invalidDrafts = listOf(
            "",
            "not a web address",
            "ftp://example.com/file",
            "https://user:password@example.com/private",
            "https:///hostless",
            "https://example.com/" + "a".repeat(3000)
        )

        invalidDrafts.forEach { draft ->
            composeRule.onNodeWithTag("web_bookmark_url_field").performTextReplacement(draft)
            composeRule.onNodeWithTag("web_bookmark_url_field").performImeAction()
            // The supporting error text is merged into the text field node, so it is only a
            // distinct node in the unmerged tree.
            composeRule.onNodeWithTag("web_bookmark_inline_error", useUnmergedTree = true)
                .assertIsDisplayed()
            assertEquals("No metadata request for '$draft'", 0, metadataSource.fetchCount)
        }

        assertTrue("Invalid drafts must not emit a save result", emittedResults.isEmpty())
    }

    @Test
    fun enablesSaveForHttpAndHttpsUrl() {
        val viewModel = WebBookmarkEditorViewModel(
            metadataSource = FakeWebBookmarkMetadataSource(null),
            savedStateHandle = SavedStateHandle()
        )

        composeRule.setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            WebBookmarkEditorScreenContent(
                uiState = state,
                onUrlChanged = viewModel::onUrlChanged,
                onSave = viewModel::save,
                onBack = {}
            )
        }

        composeRule.onNodeWithTag("web_bookmark_url_field").performTextReplacement("http://example.com/page")
        composeRule.onNodeWithTag("web_bookmark_save_button").assertIsEnabled()

        composeRule.onNodeWithTag("web_bookmark_url_field").performTextReplacement("https://example.com/page")
        composeRule.onNodeWithTag("web_bookmark_save_button").assertIsEnabled()
    }
}
