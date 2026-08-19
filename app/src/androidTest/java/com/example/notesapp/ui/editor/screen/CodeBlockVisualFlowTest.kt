package com.example.notesapp.ui.editor.screen

import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.ui.editor.mapper.BasicBlockType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CodeBlockVisualFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun captureCodeBlockEditor() {
        val blockId = "visual-code-1"
        val state = NoteEditorUiState(
            noteId = "visual-code-note",
            title = "Code examples",
            document = NoteDocument(
                blocks = listOf(
                    EditorBlock.CodeBlock(
                        id = blockId,
                        language = "Kotlin",
                        code = "fun main() {\n    println(\"Hello\")\n}"
                    )
                )
            ),
            isLoaded = true,
            isEditable = true
        )

        setEditorContent(state)
        onCodeNode("editor_code_block_$blockId")
            .performScrollTo()
            .assertIsDisplayed()

        val cardBounds = onCodeNode("editor_code_block_$blockId")
            .getUnclippedBoundsInRoot()
        val contentBounds = composeRule.onNodeWithTag("note_editor_content")
            .getUnclippedBoundsInRoot()
        assertEquals(16.dp.value, cardBounds.left.value - contentBounds.left.value, 2.0f)
        assertEquals(16.dp.value, contentBounds.right.value - cardBounds.right.value, 2.0f)

        val languageBounds = onCodeNode("editor_code_lang_selector_$blockId")
            .getUnclippedBoundsInRoot()
        val copyBounds = onCodeNode("editor_code_copy_btn_$blockId")
            .getUnclippedBoundsInRoot()
        val deleteBounds = onCodeNode("editor_code_delete_btn_$blockId")
            .getUnclippedBoundsInRoot()
        val languageCenter = languageBounds.top.value + languageBounds.height.value / 2
        val copyCenter = copyBounds.top.value + copyBounds.height.value / 2
        val deleteCenter = deleteBounds.top.value + deleteBounds.height.value / 2
        assertEquals(languageCenter, copyCenter, 2.0f)
        assertEquals(copyCenter, deleteCenter, 2.0f)

        val lineNumbersBounds = onCodeNode("editor_code_line_numbers_$blockId")
            .getUnclippedBoundsInRoot()
        val editorBounds = onCodeNode("editor_code_editor_$blockId")
            .getUnclippedBoundsInRoot()
        assertEquals(lineNumbersBounds.top.value, editorBounds.top.value, 2.0f)

        captureVisualEvidence("code_block_editor")
    }

    @Test
    fun captureBasicBlocksPanelAdvanced() {
        val state = NoteEditorUiState(
            noteId = "visual-panel-note",
            title = "Block catalog",
            document = NoteDocument(
                blocks = listOf(
                    EditorBlock.TextBlock(
                        id = "visual-text-1",
                        children = listOf(RichText("Choose an advanced block"))
                    )
                )
            ),
            isLoaded = true,
            isEditable = true
        )

        setEditorContent(state)
        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()
        composeRule.onNodeWithTag("basic_blocks_grid")
            .performScrollToNode(hasTestTag("basic_blocks_code"))
        composeRule.onNodeWithTag("basic_blocks_section_advanced").assertIsDisplayed()
        composeRule.onNodeWithTag("basic_blocks_code")
            .assertIsDisplayed()
            .assertHasClickAction()

        val panelBounds = composeRule.onNodeWithTag("basic_blocks_panel")
            .getUnclippedBoundsInRoot()
        val dividerBounds = composeRule.onNodeWithTag("basic_blocks_panel_divider")
            .getUnclippedBoundsInRoot()
        assertEquals(dividerBounds.bottom.value, panelBounds.top.value, 2.0f)
        val advancedHeaderBounds = composeRule.onNodeWithTag("basic_blocks_section_advanced")
            .getUnclippedBoundsInRoot()
        val codeTileBounds = composeRule.onNodeWithTag("basic_blocks_code")
            .getUnclippedBoundsInRoot()
        assertTrue(advancedHeaderBounds.bottom.value <= codeTileBounds.top.value)

        captureVisualEvidence("basic_blocks_panel_advanced")
    }

    private fun onCodeNode(testTag: String) = composeRule.onNodeWithTag(testTag, useUnmergedTree = true)

    private fun setEditorContent(state: NoteEditorUiState) {
        composeRule.setContent {
            NotesTakingAppTheme {
                NoteEditorScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    noteId = state.noteId,
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
                    onInsertBasicBlock = { type -> type == BasicBlockType.CODE },
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
    }

    private fun captureVisualEvidence(fileName: String) {
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
