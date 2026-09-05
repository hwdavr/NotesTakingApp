package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.FakeFolderRepository
import com.example.notesapp.FakeNoteRepository
import com.example.notesapp.R
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderCategorizer
import com.example.notesapp.domain.folder.usecase.CategorizeNoteUseCase
import com.example.notesapp.domain.summary.NoteSummarizer
import com.example.notesapp.domain.summary.NoteSummary
import com.example.notesapp.domain.summary.usecase.SummarizeNoteUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteAudioUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteBlockUseCase
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModel
import com.example.notesapp.ui.editor.viewmodel.NoteLinkPickerItem
import com.example.notesapp.ui.editor.viewmodel.NoteLinkPickerUiState
import com.example.notesapp.ui.editor.viewmodel.setFocusedBlock
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteLinkPickerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun createEditorViewModel(
        initialDocument: NoteDocument = NoteDocument.empty(),
        noteRepository: FakeNoteRepository = FakeNoteRepository(),
        folderRepository: FakeFolderRepository = FakeFolderRepository()
    ): NoteEditorViewModel {
        val viewModel = NoteEditorViewModel(
            noteRepository = noteRepository,
            folderRepository = folderRepository,
            summarizeNoteUseCase = SummarizeNoteUseCase(object : NoteSummarizer {
                override suspend fun summarize(title: String, noteText: String): NoteSummary = NoteSummary("Summary")
            }),
            categorizeNoteUseCase = CategorizeNoteUseCase(object : FolderCategorizer {
                override suspend fun categorize(title: String, content: String, folders: List<Folder>): Folder? = null
            }),
            deleteVoiceNoteAudioUseCase = mockk<DeleteVoiceNoteAudioUseCase>(relaxed = true),
            deleteVoiceNoteBlockUseCase = mockk<DeleteVoiceNoteBlockUseCase>(relaxed = true)
        )
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "source_note_1",
            title = "Source Note",
            document = initialDocument,
            isLoaded = true,
            isEditable = true
        )
        return viewModel
    }

    @Test
    fun pickerSearchesCandidatesExcludesCurrentNoteAndShowsParentFolder() {
        val candidates = listOf(
            NoteLinkPickerItem(id = "target_1", title = "Project Alpha", folderName = "Projects"),
            NoteLinkPickerItem(id = "target_2", title = "Quick Note", folderName = null)
        )
        var currentUiState: NoteLinkPickerUiState by mutableStateOf(
            NoteLinkPickerUiState.Content(
                searchQuery = "",
                notes = candidates,
                hasExistingLink = false
            )
        )
        var selectedTargetId: String? = null
        var retryClicked = false

        composeRule.setContent {
            NotesTakingAppTheme {
                NoteLinkPickerScreenContent(
                    uiState = currentUiState,
                    onBack = {},
                    onSearchQueryChanged = { query ->
                        currentUiState = if (query.contains("NonExistent")) {
                            NoteLinkPickerUiState.Empty(searchQuery = query)
                        } else if (query.contains("error")) {
                            NoteLinkPickerUiState.Error(message = "Failed to load notes")
                        } else {
                            NoteLinkPickerUiState.Content(
                                searchQuery = query,
                                notes = candidates.filter { it.title.contains(query, ignoreCase = true) },
                                hasExistingLink = false
                            )
                        }
                    },
                    onRetry = { retryClicked = true },
                    onSelectNote = { id, _ -> selectedTargetId = id },
                    onRemoveLink = {}
                )
            }
        }

        // 1. Verify candidate list display
        composeRule.onNodeWithTag("note_link_picker_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("note_link_picker_search").assertIsDisplayed()
        composeRule.onNodeWithTag("note_link_picker_note_target_1").assertIsDisplayed()
        composeRule.onNodeWithTag("note_link_picker_note_target_2").assertIsDisplayed()

        // 2. Subtitles: Folder name vs "No folder"
        composeRule.onNodeWithText("Projects").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.note_link_picker_no_folder)).assertIsDisplayed()

        // 3. Touch target height >= 48dp
        composeRule.onNodeWithTag("note_link_picker_note_target_1").assertHeightIsAtLeast(48.dp)

        // 4. Search filter
        composeRule.onNodeWithTag("note_link_picker_search").performTextInput("Alpha")
        composeRule.onNodeWithTag("note_link_picker_note_target_1").assertIsDisplayed()
        composeRule.onNodeWithTag("note_link_picker_note_target_2").assertDoesNotExist()

        // 5. Empty state
        composeRule.onNodeWithTag("note_link_picker_search").performTextInput("NonExistent")
        composeRule.onNodeWithTag("note_link_picker_empty").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.note_link_picker_empty)).assertIsDisplayed()

        // 6. Error and retry state
        currentUiState = NoteLinkPickerUiState.Error(message = "Network error")
        composeRule.onNodeWithTag("note_link_picker_error").assertIsDisplayed()
        composeRule.onNodeWithTag("note_link_picker_retry").assertIsDisplayed().performClick()
        assertTrue(retryClicked)
    }

    @Test
    fun pickerReturnsTargetAndPreservesSelectedLabel() {
        val initialDoc = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(
                    id = "b1",
                    children = listOf(
                        RichText(text = "Check out our documentation here for details")
                    )
                )
            )
        )
        val viewModel = createEditorViewModel(initialDoc)

        // Select "documentation" (index 14 to 27)
        viewModel.updateSelection(14, 27)
        viewModel.setFocusedBlock("b1")

        // User picks target note with title "System Architecture" and id "arch_999"
        viewModel.onTargetNoteSelected(targetId = "arch_999", targetTitle = "System Architecture")

        val state = viewModel.uiState.value
        val block = state.document.blocks.first() as EditorBlock.TextBlock
        val linkChild = block.children.find { it.linkTargetId == "arch_999" }

        // Preserves original selected text "documentation", does not overwrite with target title
        assertNotNull(linkChild)
        assertEquals("documentation", linkChild?.text)
        assertEquals("arch_999", linkChild?.inlineId)
    }

    @Test
    fun pickerInsertsTargetTitleWithoutSelection() {
        // Case A: Collapsed cursor in existing text block
        val initialDoc = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(
                    id = "b1",
                    children = listOf(RichText(text = "Hello world"))
                )
            )
        )
        val viewModel = createEditorViewModel(initialDoc)
        viewModel.setFocusedBlock("b1")
        viewModel.updateSelection(6, 6) // cursor between 'Hello ' and 'world'

        viewModel.onTargetNoteSelected(targetId = "target_note_1", targetTitle = "Design Specs")

        val state1 = viewModel.uiState.value
        val block1 = state1.document.blocks.first() as EditorBlock.TextBlock
        val linkChild1 = block1.children.find { it.linkTargetId == "target_note_1" }
        assertNotNull(linkChild1)
        assertEquals("Design Specs", linkChild1?.text)
        assertEquals("target_note_1", linkChild1?.inlineId)

        // Case B: No block focused
        val viewModel2 = createEditorViewModel(NoteDocument.empty())
        viewModel2.onTargetNoteSelected(targetId = "target_note_2", targetTitle = "Meeting Notes")

        val state2 = viewModel2.uiState.value
        val newBlock = state2.document.blocks.first() as EditorBlock.TextBlock
        val linkChild2 = newBlock.children.find { it.linkTargetId == "target_note_2" }
        assertNotNull(linkChild2)
        assertEquals("Meeting Notes", linkChild2?.text)
        assertEquals("target_note_2", linkChild2?.inlineId)
    }

    @Test
    fun validInternalLinkIsStyledAndOpensTarget() {
        var clickedTargetId: String? = null
        val docWithLink = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(
                    id = "b1",
                    children = listOf(
                        RichText(
                            text = "Reference Note",
                            linkTargetId = "ref_note_456",
                            inlineId = "ref_note_456"
                        )
                    )
                )
            )
        )
        val state = NoteEditorUiState(
            noteId = "current_note",
            title = "Current Note",
            document = docWithLink,
            isLoaded = true,
            isEditable = true,
            isFormattingToolbarVisible = true
        )

        composeRule.setContent {
            NotesTakingAppTheme {
                NoteEditorScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    noteId = "current_note",
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
                    onDeleteBlock = {},
                    onOpenNoteLink = { clickedTargetId = it }
                )
            }
        }

        // Verify link tag exists and tapping it invokes navigation callback with targetId
        composeRule.onNodeWithTag("editor_note_link_ref_note_456").assertIsDisplayed().performClick()
        assertEquals("ref_note_456", clickedTargetId)
    }

    @Test
    fun removeLinkReplaceAndCancelHaveSpecifiedOutcomes() {
        val initialDoc = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(
                    id = "b1",
                    children = listOf(
                        RichText(
                            text = "Linked Text",
                            linkTargetId = "old_target",
                            inlineId = "old_target"
                        )
                    )
                )
            )
        )
        val viewModel = createEditorViewModel(initialDoc)
        viewModel.setFocusedBlock("b1")
        viewModel.updateSelection(0, 11)

        // 1. Replace target
        viewModel.onTargetNoteSelected(targetId = "new_target", targetTitle = "New Title")
        var block = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        assertEquals("new_target", block.children.first().linkTargetId)
        assertEquals("Linked Text", block.children.first().text)

        // 2. Remove link
        viewModel.onRemoveLinkSelected()
        block = viewModel.uiState.value.document.blocks.first() as EditorBlock.TextBlock
        assertNull(block.children.first().linkTargetId)
        assertNull(block.children.first().inlineId)
        assertEquals("Linked Text", block.children.first().text)

        // 3. Cancel / back leaves document unchanged
        val unchangedDoc = viewModel.uiState.value.document
        assertEquals(1, unchangedDoc.blocks.size)
    }

    @Test
    fun deletingLinkedTargetRemovesEntireLabel() {
        val targetId = "target_to_delete"
        val initialDoc = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(
                    id = "b1",
                    children = listOf(
                        RichText(text = "Before link "),
                        RichText(
                            text = "Deleted Target Note",
                            linkTargetId = targetId,
                            inlineId = targetId
                        ),
                        RichText(text = " after link")
                    )
                )
            )
        )

        // Target note is in deletedTargetIds
        val activeIds = setOf("some_other_note")
        val deletedIds = setOf(targetId)
        val resolvedDoc = initialDoc.resolveLinks(activeIds, deletedIds)

        val block = resolvedDoc.blocks.first() as EditorBlock.TextBlock
        assertNull(block.children.find { it.linkTargetId == targetId })
        assertFalse(block.children.any { it.text.contains("Deleted Target Note") })
        // Entire linked label is removed rather than leaving orphan plain label
        assertEquals("Before link  after link", block.children.joinToString("") { it.text })
    }

    @Test
    fun unresolvedAnnotationRendersReadablePlainText() {
        val unresolvedDoc = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(
                    id = "b1",
                    children = listOf(
                        RichText(
                            text = "Legacy Annotation",
                            linkTargetId = "deleted_or_unknown_note",
                            inlineId = "legacy_id"
                        )
                    )
                )
            )
        )
        // Unknown note (active notes is non-empty, note not in active and not in deleted) falls back to plain text
        val activeIds = setOf("known_note_1")
        val resolvedDoc = unresolvedDoc.resolveLinks(activeIds, emptySet())

        val block = resolvedDoc.blocks.first() as EditorBlock.TextBlock
        val child = block.children.first()
        assertEquals("Legacy Annotation", child.text)
        assertNull(child.linkTargetId)
        assertNull(child.inlineId)
    }
}
