package com.example.notesapp.ui.editor.screen

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.ui.editor.mapper.BasicBlockType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CodeBlockScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun testCodeBlockCardRenderingAndInteraction() {
        val initialCode = "fun greet() {\n    println(\"Hello\")\n}"
        var state by mutableStateOf(
            NoteEditorUiState(
                noteId = "note-code",
                title = "Code note",
                document = NoteDocument(
                    blocks = listOf(
                        EditorBlock.CodeBlock(
                            id = "code-1",
                            language = "Kotlin",
                            code = initialCode
                        )
                    )
                ),
                isLoaded = true,
                isEditable = true
            )
        )
        var deletedBlockId: String? = null

        setEditorContent(
            stateProvider = { state },
            onUpdateCodeBlockCode = { blockId, code ->
                state = state.updateCodeBlock(blockId) { block -> block.copy(code = code) }
            },
            onUpdateCodeBlockLanguage = { blockId, language ->
                state = state.updateCodeBlock(blockId) { block -> block.copy(language = language) }
            },
            onDeleteBlock = { blockId ->
                deletedBlockId = blockId
                state = state.copy(
                    document = state.document.copy(
                        blocks = state.document.blocks.filterNot { it.id == blockId }
                    )
                )
            }
        )

        onCodeNode("editor_code_block_code-1")
            .performScrollTo()
            .assertIsDisplayed()
        onCodeNode("editor_code_line_numbers_code-1").assertIsDisplayed()
        onCodeNode("editor_code_lang_selector_code-1").performClick()
        onCodeNode("editor_code_lang_item_python").performClick()
        composeRule.waitForIdle()
        assertEquals("Python", state.codeBlock("code-1")?.language)

        onCodeNode("editor_code_editor_code-1")
            .performTextInput("\nprint(\"connected\")")
        composeRule.waitForIdle()
        assertTrue(state.codeBlock("code-1")?.code?.contains("connected") == true)

        onCodeNode("editor_code_copy_btn_code-1").performClick()
        val clipboard = InstrumentationRegistry.getInstrumentation().targetContext
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        assertEquals(
            state.codeBlock("code-1")?.code,
            clipboard.primaryClip?.getItemAt(0)?.text?.toString()
        )

        onCodeNode("editor_code_delete_btn_code-1").performClick()
        composeRule.waitForIdle()
        onCodeNode("editor_code_block_code-1").assertDoesNotExist()
        assertEquals("code-1", deletedBlockId)
    }

    @Test
    fun testBasicBlocksPanelAdvancedSectionRendering() {
        val state = NoteEditorUiState(
            noteId = "note-panel",
            title = "Panel note",
            document = NoteDocument(
                blocks = listOf(
                    EditorBlock.TextBlock(
                        id = "text-1",
                        children = listOf(RichText("Open the block catalog"))
                    )
                )
            ),
            isLoaded = true,
            isEditable = true
        )
        var insertedType: BasicBlockType? = null

        setEditorContent(
            stateProvider = { state },
            onInsertBasicBlock = { type ->
                insertedType = type
                true
            }
        )

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()
        composeRule.onNodeWithTag("basic_blocks_section_basic").assertIsDisplayed()
        composeRule.onNodeWithTag("basic_blocks_grid")
            .performScrollToNode(hasTestTag("basic_blocks_code"))
        composeRule.onNodeWithTag("basic_blocks_section_advanced").assertIsDisplayed()
        composeRule.onNodeWithTag("basic_blocks_code")
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()
        composeRule.waitForIdle()

        assertEquals(BasicBlockType.CODE, insertedType)
        composeRule.onNodeWithTag("basic_blocks_panel").assertDoesNotExist()
    }

    @Test
    fun testReadOnlyCodeBlockBehavior() {
        val code = "fun readOnly() {\n    return 42\n}"
        val state = NoteEditorUiState(
            noteId = "note-read-only",
            title = "Read-only code",
            document = NoteDocument(
                blocks = listOf(
                    EditorBlock.CodeBlock(
                        id = "code-read-only",
                        language = "Kotlin",
                        code = code
                    )
                )
            ),
            isLoaded = true,
            isEditable = false
        )

        setEditorContent(stateProvider = { state })

        onCodeNode("editor_code_block_code-read-only")
            .performScrollTo()
            .assertIsDisplayed()
        onCodeNode("editor_code_readonly_code-read-only").assertIsDisplayed()
        onCodeNode("editor_code_line_numbers_code-read-only").assertIsDisplayed()
        onCodeNode("editor_code_copy_btn_code-read-only")
            .assertIsDisplayed()
            .performClick()
        onCodeNode("editor_code_lang_selector_code-read-only").assertIsNotEnabled()
        onCodeNode("editor_code_delete_btn_code-read-only").assertDoesNotExist()
        onCodeNode("editor_code_editor_code-read-only").assertDoesNotExist()

        val clipboard = InstrumentationRegistry.getInstrumentation().targetContext
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        assertEquals(code, clipboard.primaryClip?.getItemAt(0)?.text?.toString())
    }

    private fun onCodeNode(testTag: String) = composeRule.onNodeWithTag(testTag, useUnmergedTree = true)

    private fun setEditorContent(
        stateProvider: () -> NoteEditorUiState,
        onInsertBasicBlock: (BasicBlockType) -> Boolean = { false },
        onUpdateCodeBlockCode: (String, String) -> Unit = { _, _ -> },
        onUpdateCodeBlockLanguage: (String, String) -> Unit = { _, _ -> },
        onDeleteBlock: (String) -> Unit = {}
    ) {
        composeRule.setContent {
            NotesTakingAppTheme {
                NoteEditorScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    noteId = stateProvider().noteId,
                    state = stateProvider(),
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
                    onInsertBasicBlock = onInsertBasicBlock,
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
                    onDeleteBlock = onDeleteBlock,
                    onUpdateCodeBlockCode = onUpdateCodeBlockCode,
                    onUpdateCodeBlockLanguage = onUpdateCodeBlockLanguage
                )
            }
        }
    }
}

private fun NoteEditorUiState.updateCodeBlock(
    blockId: String,
    transform: (EditorBlock.CodeBlock) -> EditorBlock.CodeBlock
): NoteEditorUiState = copy(
    document = document.copy(
        blocks = document.blocks.map { block ->
            if (block is EditorBlock.CodeBlock && block.id == blockId) {
                transform(block)
            } else {
                block
            }
        }
    )
)

private fun NoteEditorUiState.codeBlock(blockId: String): EditorBlock.CodeBlock? =
    document.blocks.filterIsInstance<EditorBlock.CodeBlock>().firstOrNull { it.id == blockId }
