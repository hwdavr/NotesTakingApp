package com.example.notesapp.ui.editor.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.FakeFolderRepository
import com.example.notesapp.FakeNoteRepository
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderCategorizer
import com.example.notesapp.domain.folder.usecase.CategorizeNoteUseCase
import com.example.notesapp.domain.summary.NoteSummarizer
import com.example.notesapp.domain.summary.NoteSummary
import com.example.notesapp.domain.summary.usecase.SummarizeNoteUseCase
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModel
import com.example.notesapp.ui.editor.viewmodel.addImageBlock
import com.example.notesapp.ui.editor.viewmodel.addTableBlock
import com.example.notesapp.ui.editor.viewmodel.onTableAction
import com.example.notesapp.ui.editor.viewmodel.setFocusedBlock
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorImageTableInsertionReproductionTest {
    @get:Rule
    val activityRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun givenActiveBlockInPopulatedNote_whenImageTapped_thenImageIsVisibleAfterActiveBlock() {
        val viewModel = createViewModel()
        val activeBlockId = "text-1"
        setEditorContent(viewModel, activeBlockId)

        activityRule.onNodeWithTag("editor_default_bottom_bar")
            .performScrollToNode(hasTestTag("editor_add_image"))
        activityRule.onNodeWithTag("editor_add_image").performClick()
        activityRule.waitForIdle()

        val blocks = viewModel.uiState.value.document.blocks
        assertEquals(
            "Image must be inserted immediately after the active block; " +
                "focused=${viewModel.uiState.value.focusedBlockId}, " +
                "blocks=${blocks.map { it::class.simpleName }}",
            EditorBlock.ImageBlock::class,
            blocks[1]::class
        )
        activityRule.onNodeWithTag("editor_image_block").assertIsDisplayed()
    }

    @Test
    fun givenActiveBlockInPopulatedNote_whenTableTapped_thenTableIsVisibleAfterActiveBlock() {
        val viewModel = createViewModel()
        val activeBlockId = "text-1"
        setEditorContent(viewModel, activeBlockId)

        activityRule.onNodeWithTag("editor_default_bottom_bar")
            .performScrollToNode(hasTestTag("editor_add_table"))
        activityRule.onNodeWithTag("editor_add_table").performClick()
        activityRule.waitForIdle()

        val blocks = viewModel.uiState.value.document.blocks
        assertEquals(
            "Table must be inserted immediately after the active block; " +
                "focused=${viewModel.uiState.value.focusedBlockId}, " +
                "blocks=${blocks.map { it::class.simpleName }}",
            EditorBlock.TableBlock::class,
            blocks[1]::class
        )
        activityRule.onNodeWithTag("editor_table_block").assertIsDisplayed()
    }

    @Composable
    private fun EditorContent(viewModel: NoteEditorViewModel) {
        val state by viewModel.uiState.collectAsState()
        NotesTakingAppTheme {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note-1",
                state = state,
                onBack = {},
                onShareRequested = {},
                onDelete = {},
                onTitleChange = viewModel::onTitleChange,
                onRename = viewModel::rename,
                onToggleFavorite = viewModel::toggleFavorite,
                onMoveNote = {},
                onExportNote = {},
                onOpenVoiceRecorder = { _, _ -> },
                onTextBlockChange = viewModel::onTextBlockChange,
                onToggleCheckbox = viewModel::toggleCheckbox,
                onToggleCheckboxChecked = viewModel::toggleCheckboxChecked,
                onToggleMark = viewModel::toggleBlockMark,
                onAddImage = viewModel::addImageBlock,
                onEmojiSelected = {},
                onEmojiQueryChange = {},
                onEmojiClearQuery = {},
                onEmojiCategorySelected = {},
                onEmojiSkinToneRequested = {},
                onEmojiSkinToneDismissed = {},
                onImageChange = viewModel::updateImageBlock,
                onAddTable = viewModel::addTableBlock,
                onTableCellChange = viewModel::updateTableCell,
                onFolderSelected = {},
                onToggleFormattingToolbar = viewModel::toggleFormattingToolbar,
                onBlockFocused = viewModel::setFocusedBlock,
                onSelectionChange = viewModel::updateSelection,
                onDeleteBlock = viewModel::deleteBlock,
                onTableAction = viewModel::onTableAction
            )
        }
    }

    private fun setEditorContent(viewModel: NoteEditorViewModel, activeBlockId: String) {
        val blocks = buildList {
            add(EditorBlock.TextBlock(id = activeBlockId, children = listOf(RichText("Active"))))
            repeat(16) { index ->
                add(
                    EditorBlock.TextBlock(
                        id = "text-${index + 2}",
                        children = listOf(RichText("Existing block ${index + 2}"))
                    )
                )
            }
        }
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "note-1",
            title = "Insertion test",
            document = NoteDocument(blocks = blocks),
            focusedBlockId = activeBlockId,
            isLoaded = true,
            isEditable = true
        )
        activityRule.setContent { EditorContent(viewModel) }
        activityRule.waitForIdle()
    }

    private fun createViewModel(): NoteEditorViewModel = NoteEditorViewModel(
        noteRepository = FakeNoteRepository(),
        folderRepository = FakeFolderRepository(),
        summarizeNoteUseCase = SummarizeNoteUseCase(
            object : NoteSummarizer {
                override suspend fun summarize(title: String, noteText: String): NoteSummary =
                    NoteSummary("Insertion test summary")
            }
        ),
        categorizeNoteUseCase = CategorizeNoteUseCase(
            object : FolderCategorizer {
                override suspend fun categorize(title: String, content: String, folders: List<Folder>): Folder? = null
            }
        ),
        deleteVoiceNoteAudioUseCase = mockk(relaxed = true),
        deleteVoiceNoteBlockUseCase = mockk(relaxed = true)
    )
}
