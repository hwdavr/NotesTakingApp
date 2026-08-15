package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.folder.usecase.CategorizeNoteUseCase
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.summary.usecase.SummarizeNoteUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteAudioUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteBlockUseCase
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.text
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorViewModelEmojiTest : BaseViewModelTest() {
    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val folderRepository: FolderRepository = mockk(relaxed = true)
    private lateinit var viewModel: NoteEditorViewModel

    @Before
    fun setUp() {
        viewModel = NoteEditorViewModel(
            noteRepository = noteRepository,
            folderRepository = folderRepository,
            summarizeNoteUseCase = mockk<SummarizeNoteUseCase>(relaxed = true),
            categorizeNoteUseCase = mockk<CategorizeNoteUseCase>(relaxed = true),
            deleteVoiceNoteAudioUseCase = mockk<DeleteVoiceNoteAudioUseCase>(relaxed = true),
            deleteVoiceNoteBlockUseCase = mockk<DeleteVoiceNoteBlockUseCase>(relaxed = true)
        )
    }

    @Test
    fun insertsUnicodeAtCursorAndReplacesSelection() = runTest {
        val block = EditorBlock.TextBlock(
            id = "body",
            children = listOf(RichText("Plan today"))
        )
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "note-1",
            title = "Unchanged title",
            document = NoteDocument(blocks = listOf(block)),
            focusedBlockId = block.id,
            selectionStart = 5,
            selectionEnd = 10
        )

        viewModel.insertEmoji("😀")

        val state = viewModel.uiState.value
        val updatedBlock = state.document.blocks.single() as EditorBlock.TextBlock
        assertEquals("Plan 😀", updatedBlock.text())
        assertEquals(7, state.selectionStart)
        assertEquals(7, state.selectionEnd)
        assertEquals("Unchanged title", state.title)

        advanceTimeBy(2_001)

        coVerify { noteRepository.save(match { saved -> saved.content.contains("😀") }) }
    }

    @Test
    fun insertsIntoNewFocusedParagraphWhenNoBodyBlockIsFocused() = runTest {
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "note-1",
            title = "Title remains unchanged",
            document = NoteDocument(blocks = listOf(EditorBlock.ImageBlock(id = "image"))),
            focusedBlockId = null
        )

        viewModel.insertEmoji("👍🏽")

        val state = viewModel.uiState.value
        val appendedBlock = state.document.blocks.last() as EditorBlock.TextBlock
        assertEquals("👍🏽", appendedBlock.text())
        assertEquals(appendedBlock.id, state.focusedBlockId)
        assertEquals("👍🏽".length, state.selectionStart)
        assertEquals("Title remains unchanged", state.title)
    }

    @Test
    fun doesNotMutateReadOnlyDocument() = runTest {
        val block = EditorBlock.TextBlock(id = "body", children = listOf(RichText("Read only")))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            document = NoteDocument(blocks = listOf(block)),
            focusedBlockId = block.id,
            isEditable = false
        )

        viewModel.insertEmoji("😀")

        assertEquals("Read only", (viewModel.uiState.value.document.blocks.single() as EditorBlock.TextBlock).text())
        assertTrue(viewModel.uiState.value.document.blocks.single() is EditorBlock.TextBlock)
    }
}
