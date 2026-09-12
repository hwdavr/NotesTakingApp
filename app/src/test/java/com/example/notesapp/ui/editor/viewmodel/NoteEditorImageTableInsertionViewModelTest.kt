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
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NoteEditorImageTableInsertionViewModelTest : BaseViewModelTest() {
    private lateinit var viewModel: NoteEditorViewModel

    @Before
    fun setup() {
        viewModel = NoteEditorViewModel(
            noteRepository = mockk<NoteRepository>(relaxed = true),
            folderRepository = mockk<FolderRepository>(relaxed = true),
            summarizeNoteUseCase = mockk<SummarizeNoteUseCase>(relaxed = true),
            categorizeNoteUseCase = mockk<CategorizeNoteUseCase>(relaxed = true),
            deleteVoiceNoteAudioUseCase = mockk<DeleteVoiceNoteAudioUseCase>(relaxed = true),
            deleteVoiceNoteBlockUseCase = mockk<DeleteVoiceNoteBlockUseCase>(relaxed = true)
        )
    }

    @Test
    fun `addImageBlock inserts after focused block and requests reveal`() {
        val activeBlock = EditorBlock.TextBlock(id = "active", children = listOf(RichText("Active")))
        val trailingBlock = EditorBlock.TextBlock(id = "trailing", children = listOf(RichText("Trailing")))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "n1",
            document = NoteDocument(blocks = listOf(activeBlock, trailingBlock)),
            focusedBlockId = activeBlock.id,
            isLoaded = true,
            isEditable = true
        )

        viewModel.addImageBlock()

        val blocks = viewModel.uiState.value.document.blocks
        assertEquals(activeBlock.id, blocks[0].id)
        assertTrue(blocks[1] is EditorBlock.ImageBlock)
        assertEquals(trailingBlock.id, blocks[2].id)
        assertEquals(activeBlock.id, viewModel.uiState.value.focusedBlockId)
        assertEquals(blocks[1].id, viewModel.uiState.value.blockIdToReveal)
    }

    @Test
    fun `addTableBlock inserts after focused block and requests reveal`() {
        val activeBlock = EditorBlock.TextBlock(id = "active", children = listOf(RichText("Active")))
        val trailingBlock = EditorBlock.TextBlock(id = "trailing", children = listOf(RichText("Trailing")))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "n1",
            document = NoteDocument(blocks = listOf(activeBlock, trailingBlock)),
            focusedBlockId = activeBlock.id,
            isLoaded = true,
            isEditable = true
        )

        viewModel.addTableBlock()

        val blocks = viewModel.uiState.value.document.blocks
        assertEquals(activeBlock.id, blocks[0].id)
        assertTrue(blocks[1] is EditorBlock.TableBlock)
        assertEquals(trailingBlock.id, blocks[2].id)
        assertEquals(activeBlock.id, viewModel.uiState.value.focusedBlockId)
        assertEquals(blocks[1].id, viewModel.uiState.value.blockIdToReveal)
    }

    @Test
    fun `block insertion without focus uses the last text block as fallback`() {
        val firstBlock = EditorBlock.TextBlock(id = "first", children = listOf(RichText("First")))
        val secondBlock = EditorBlock.TextBlock(id = "second", children = listOf(RichText("Second")))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "n1",
            document = NoteDocument(blocks = listOf(firstBlock, secondBlock)),
            isLoaded = true,
            isEditable = true
        )

        viewModel.addTableBlock()

        val blocks = viewModel.uiState.value.document.blocks
        assertEquals(secondBlock.id, blocks[1].id)
        assertTrue(blocks[2] is EditorBlock.TableBlock)
        assertEquals(blocks[2].id, viewModel.uiState.value.blockIdToReveal)
    }
}
