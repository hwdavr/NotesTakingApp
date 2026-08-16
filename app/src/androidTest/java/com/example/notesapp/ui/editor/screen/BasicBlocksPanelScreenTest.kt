package com.example.notesapp.ui.editor.screen

import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import com.example.notesapp.ui.theme.DarkAppColors
import com.example.notesapp.ui.theme.LightAppColors
import com.example.notesapp.ui.theme.LocalAppColors
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BasicBlocksPanelScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun basicBlocksPanelExposesAccessibleLabeledTilesAndTargetBounds() {
        val state by mutableStateOf(
            NoteEditorUiState(
                noteId = "note-1",
                isLoaded = true,
                isEditable = true
            )
        )

        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note-1",
                state = state,
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
                onDeleteBlock = {}
            )
        }

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()

        val tileTags = listOf(
            "basic_blocks_text",
            "basic_blocks_heading_1",
            "basic_blocks_heading_2",
            "basic_blocks_heading_3",
            "basic_blocks_heading_4",
            "basic_blocks_bulleted_list",
            "basic_blocks_numbered_list",
            "basic_blocks_todo_list",
            "basic_blocks_toggle_list",
            "basic_blocks_callout",
            "basic_blocks_quote"
        )

        for (tag in tileTags) {
            composeRule.onNodeWithTag("basic_blocks_grid").performScrollToNode(hasTestTag(tag))
            val node = composeRule.onNodeWithTag(tag)
            node.assertIsDisplayed()
            node.assertHasClickAction()
            val bounds = node.getUnclippedBoundsInRoot()
            assertTrue("Tile $tag height ${bounds.height} must be >= 48dp", bounds.height >= 48.dp)
        }
    }

    @Test
    fun basicBlocksPanelMatchesCompactGeometry() {
        val state by mutableStateOf(
            NoteEditorUiState(
                noteId = "note-1",
                isLoaded = true,
                isEditable = true
            )
        )

        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note-1",
                state = state,
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
                onDeleteBlock = {}
            )
        }

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()

        val toolbarBounds = composeRule.onNodeWithTag("editor_default_bottom_bar").getUnclippedBoundsInRoot()
        val dividerBounds = composeRule.onNodeWithTag("basic_blocks_panel_divider").getUnclippedBoundsInRoot()
        val panelBounds = composeRule.onNodeWithTag("basic_blocks_panel").getUnclippedBoundsInRoot()
        val tileBounds = composeRule.onNodeWithTag("basic_blocks_text").getUnclippedBoundsInRoot()

        assertEquals(56.dp.value, toolbarBounds.height.value, 2.0f)
        assertEquals(dividerBounds.top.value, toolbarBounds.bottom.value, 2.0f)
        assertEquals(panelBounds.top.value, dividerBounds.bottom.value, 2.0f)
        assertTrue("Panel height ${panelBounds.height} must be <= 282dp", panelBounds.height <= 282.dp)
        assertEquals(48.dp.value, tileBounds.height.value, 4.0f)
    }

    @Test
    fun basicBlocksGridScrollsToQuoteWithoutExpandingPanel() {
        val state by mutableStateOf(
            NoteEditorUiState(
                noteId = "note-1",
                isLoaded = true,
                isEditable = true
            )
        )

        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note-1",
                state = state,
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
                onDeleteBlock = {}
            )
        }

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()

        val initialPanelBounds = composeRule.onNodeWithTag("basic_blocks_panel").getUnclippedBoundsInRoot()
        composeRule.onNodeWithTag("basic_blocks_grid").performScrollToNode(hasTestTag("basic_blocks_quote"))
        composeRule.onNodeWithTag("basic_blocks_quote").assertIsDisplayed()

        val scrolledPanelBounds = composeRule.onNodeWithTag("basic_blocks_panel").getUnclippedBoundsInRoot()
        assertEquals(initialPanelBounds.height.value, scrolledPanelBounds.height.value, 2.0f)
    }

    @Test
    fun basicBlocksTriggerAndBackCollapseWithoutMutation() {
        var backCalled = false
        val state by mutableStateOf(
            NoteEditorUiState(
                noteId = "note-1",
                isLoaded = true,
                isEditable = true
            )
        )

        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note-1",
                state = state,
                onBack = { backCalled = true },
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
                onDeleteBlock = {}
            )
        }

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()

        // Second click collapses
        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertDoesNotExist()

        // Open again
        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()

        // Press back
        androidx.test.espresso.Espresso.pressBack()
        composeRule.onNodeWithTag("basic_blocks_panel").assertDoesNotExist()
        assertEquals(false, backCalled)
    }

    @Test
    fun readOnlyBasicBlocksTriggerIsVisibleDisabledAndSafe() {
        val state = NoteEditorUiState(
            noteId = "read-only-1",
            isLoaded = true,
            isEditable = false
        )

        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "read-only-1",
                state = state,
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
                onDeleteBlock = {}
            )
        }

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").assertIsDisplayed().assertIsNotEnabled()
        composeRule.onNodeWithTag("basic_blocks_panel").assertDoesNotExist()
    }

    @Test
    fun basicBlocksPanelSupportsLargeFontAndConstrainedViewport() {
        val state = NoteEditorUiState(
            noteId = "note-large-font",
            isLoaded = true,
            isEditable = true
        )

        composeRule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 2.0f, fontScale = 1.5f)
            ) {
                NoteEditorScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    noteId = "note-large-font",
                    state = state,
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
                    onDeleteBlock = {}
                )
            }
        }

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()
        composeRule.onNodeWithTag("basic_blocks_grid").performScrollToNode(hasTestTag("basic_blocks_quote"))
        composeRule.onNodeWithTag("basic_blocks_quote").assertIsDisplayed()
    }

    @Test
    fun basicBlocksPanelRendersInLightAndDarkThemes() {
        var isDarkTheme by mutableStateOf(false)
        val state = NoteEditorUiState(
            noteId = "theme-note",
            isLoaded = true,
            isEditable = true
        )

        composeRule.setContent {
            val colors = if (isDarkTheme) DarkAppColors else LightAppColors
            CompositionLocalProvider(LocalAppColors provides colors) {
                NoteEditorScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    noteId = "theme-note",
                    state = state,
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
                    onDeleteBlock = {}
                )
            }
        }

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()

        isDarkTheme = true
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()
    }

    @Test
    fun captureBasicBlocksPanelTopState() {
        val state = NoteEditorUiState(
            noteId = "vis-top",
            isLoaded = true,
            isEditable = true
        )

        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "vis-top",
                state = state,
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
                onDeleteBlock = {}
            )
        }

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()
        composeRule.onNodeWithTag("basic_blocks_panel_title").assertIsDisplayed()
        composeRule.onNodeWithTag("basic_blocks_grid").assertIsDisplayed()
        composeRule.onNodeWithTag("basic_blocks_text").assertIsDisplayed()

        saveScreenshot("notesapp_basic_blocks_panel_top")
    }

    @Test
    fun captureBasicBlocksPanelScrolledState() {
        val state = NoteEditorUiState(
            noteId = "vis-scrolled",
            isLoaded = true,
            isEditable = true
        )

        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "vis-scrolled",
                state = state,
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
                onDeleteBlock = {}
            )
        }

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_grid").performScrollToNode(hasTestTag("basic_blocks_quote"))
        composeRule.onNodeWithTag("basic_blocks_quote").assertIsDisplayed()

        saveScreenshot("notesapp_basic_blocks_panel_scrolled")
    }

    private fun saveScreenshot(fileName: String) {
        composeRule.waitForIdle()
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            ?: error("Could not capture visual evidence: $fileName")
        val directory = InstrumentationRegistry.getInstrumentation().targetContext
            .getExternalFilesDir("visual_evidence")
            ?: error("External files directory unavailable")
        directory.mkdirs()
        val screenshot = File(directory, "$fileName.png")
        screenshot.outputStream().use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                "Could not write visual evidence: $fileName"
            }
        }
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("cp ${screenshot.absolutePath} /sdcard/Download/$fileName.png")
            .use { }
    }
}
