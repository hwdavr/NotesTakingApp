package com.example.notesapp.ui.editor.platform

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.R
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.screen.NoteEditorScreenContent
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebBookmarkPlatformBoundaryTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun opensValidatedUrlWithRealActionViewBrowser() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val launcher = WebBookmarkBrowserLauncher(context)
        val bookmark = bookmark()
        var openResult: WebBookmarkOpenResult? = null
        val actionViewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(bookmark.url))
        assertEquals(Intent.ACTION_VIEW, actionViewIntent.action)
        assertTrue(actionViewIntent.resolveActivity(context.packageManager) != null)

        composeRule.setContent {
            com.example.notesapp.ui.theme.NotesTakingAppTheme {
                com.example.notesapp.ui.editor.components.WebBookmarkBlockCard(
                    block = bookmark,
                    isEditable = true,
                    onOpen = { openResult = launcher.open(bookmark.url) },
                    onMore = {}
                )
            }
        }

        composeRule.onNodeWithTag("editor_web_bookmark_open_${bookmark.id}").performClick()
        assertEquals(WebBookmarkOpenResult.Opened, openResult)
    }

    @Test
    fun reportsMissingBrowserHandlerWithoutMutation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val launcher = WebBookmarkBrowserLauncher(context, packageNameOverride = NO_BROWSER_PACKAGE)
        val bookmark = bookmark()
        val initialDocument = NoteDocument(blocks = listOf(bookmark))
        var document = initialDocument

        composeRule.setContent {
            com.example.notesapp.ui.theme.NotesTakingAppTheme {
                NoteEditorScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    noteId = "note-1",
                    state = NoteEditorUiState(
                        noteId = "note-1",
                        title = "Bookmark note",
                        document = document,
                        isLoaded = true,
                        isEditable = true
                    ),
                    onBack = {},
                    onShareRequested = {},
                    onDelete = {},
                    onTitleChange = {},
                    onRename = {},
                    onToggleFavorite = {},
                    onMoveNote = {},
                    onExportNote = {},
                    onOpenVoiceRecorder = { _, _ -> },
                    onTextBlockChange = { _, _ -> },
                    onToggleCheckbox = {},
                    onToggleCheckboxChecked = {},
                    onToggleMark = { _, _ -> },
                    onAddImage = {},
                    onEmojiSelected = {},
                    onEmojiQueryChange = {},
                    onEmojiClearQuery = {},
                    onEmojiCategorySelected = {},
                    onEmojiSkinToneRequested = {},
                    onEmojiSkinToneDismissed = {},
                    onImageChange = { _, _, _ -> },
                    onAddTable = {},
                    onTableCellChange = { _, _, _, _ -> },
                    onFolderSelected = {},
                    onToggleFormattingToolbar = {},
                    onBlockFocused = {},
                    onSelectionChange = { _, _ -> },
                    onDeleteBlock = {},
                    onOpenWebBookmark = { url -> launcher.open(url) is WebBookmarkOpenResult.Opened }
                )
            }
        }

        composeRule.onNodeWithTag("editor_web_bookmark_open_${bookmark.id}").performClick()
        composeRule.onNodeWithText(
            context.getString(R.string.web_bookmark_browser_error)
        ).assertIsDisplayed()
        assertEquals(initialDocument, document)
        assertTrue(launcher.open(bookmark.url) is WebBookmarkOpenResult.NoHandler)
    }

    private fun bookmark() = EditorBlock.WebBookmarkBlock(
        id = "bookmark-platform-1",
        url = "https://example.com/platform",
        title = "Platform article",
        description = "Browser boundary fixture"
    )

    private companion object {
        const val NO_BROWSER_PACKAGE = "com.example.notesapp.test.no.browser.handler"
    }
}
