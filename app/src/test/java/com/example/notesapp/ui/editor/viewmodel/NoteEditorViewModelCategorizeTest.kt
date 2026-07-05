package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.folder.CategorizeNoteUseCase
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.summary.SummarizeNoteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorViewModelCategorizeTest : BaseViewModelTest() {

    private lateinit var viewModel: NoteEditorViewModel
    private val noteRepository = mockk<NoteRepository>(relaxed = true)
    private val folderRepository = mockk<FolderRepository>(relaxed = true)
    private val summarizeNoteUseCase = mockk<SummarizeNoteUseCase>(relaxed = true)
    private val categorizeNoteUseCase = mockk<CategorizeNoteUseCase>()

    private val testFolders = listOf(
        Folder(id = "f1", name = "Work", createdAt = 0L, updatedAt = 0L),
        Folder(id = "f2", name = "Personal", createdAt = 0L, updatedAt = 0L)
    )

    @Before
    fun setup() {
        every { folderRepository.getFolders() } returns flowOf(testFolders)
        viewModel = NoteEditorViewModel(
            noteRepository = noteRepository,
            folderRepository = folderRepository,
            summarizeNoteUseCase = summarizeNoteUseCase,
            categorizeNoteUseCase = categorizeNoteUseCase
        )
    }

    @Test
    fun `given note has no folder and has text and AI matches folder when handleBackPress then shows dialog`() =
        runTest {
            // Arrange
            viewModel.load(noteId = null)
            advanceUntilIdle()

            viewModel.onTitleChange("Work note")
            viewModel.onContentChange("This is work related content")

            coEvery {
                categorizeNoteUseCase("Work note", any(), testFolders)
            } returns testFolders[0] // Returns f1: Work

            var navigated = false
            val onNavigateBack = { navigated = true }

            // Act
            viewModel.handleBackPress(onNavigateBack)
            advanceUntilIdle()

            // Assert
            val state = viewModel.uiState.value
            assertFalse(state.isCategorizing)
            assertTrue(state.showCategorizationDialog)
            assertNotNull(state.recommendedFolder)
            assertEquals("f1", state.recommendedFolder?.id)
            assertFalse(navigated) // Navigation is deferred
        }

    @Test
    fun `given note already has folder when handleBackPress then saves and navigates immediately`() = runTest {
        // Arrange
        viewModel.load(noteId = null, folderId = "f2") // Assigned folder f2
        advanceUntilIdle()

        viewModel.onTitleChange("Personal note")

        var navigated = false
        val onNavigateBack = { navigated = true }

        // Act
        viewModel.handleBackPress(onNavigateBack)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.showCategorizationDialog)
        assertTrue(navigated) // Navigates immediately

        // Verify repository save was called with the note having folderId = f2
        coVerify { noteRepository.save(match { it.folderId == "f2" }) }
    }

    @Test
    fun `given note is blank when handleBackPress then saves and navigates immediately`() = runTest {
        // Arrange
        viewModel.load(noteId = null)
        advanceUntilIdle()

        var navigated = false
        val onNavigateBack = { navigated = true }

        // Act
        viewModel.handleBackPress(onNavigateBack)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.showCategorizationDialog)
        assertTrue(navigated) // Navigates immediately
    }

    @Test
    fun `given dialog shown when confirmCategorization then updates folder and saves note`() = runTest {
        // Arrange
        viewModel.load(noteId = null)
        advanceUntilIdle()

        viewModel.onTitleChange("Work note")
        coEvery { categorizeNoteUseCase(any(), any(), any()) } returns testFolders[0]

        var wasCategorizingDuringSave = false
        coEvery { noteRepository.save(any()) } answers {
            wasCategorizingDuringSave = viewModel.uiState.value.isCategorizing
        }

        var navigated = false
        val onNavigateBack = { navigated = true }

        viewModel.handleBackPress(onNavigateBack)
        advanceUntilIdle()

        // Act
        viewModel.confirmCategorization(onNavigateBack)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.showCategorizationDialog)
        assertFalse(state.isCategorizing)
        assertEquals("f1", state.folderId)
        assertTrue(navigated)
        assertTrue("isCategorizing should be true during save API call", wasCategorizingDuringSave)

        // Verify repository save
        coVerify { noteRepository.save(match { it.folderId == "f1" }) }
    }

    @Test
    fun `given dialog shown when cancelCategorization then saves at root without folder`() = runTest {
        // Arrange
        viewModel.load(noteId = null)
        advanceUntilIdle()

        viewModel.onTitleChange("Work note")
        coEvery { categorizeNoteUseCase(any(), any(), any()) } returns testFolders[0]

        var navigated = false
        val onNavigateBack = { navigated = true }

        viewModel.handleBackPress(onNavigateBack)
        advanceUntilIdle()

        // Act
        viewModel.cancelCategorization(onNavigateBack)
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.showCategorizationDialog)
        assertNull(state.folderId)
        assertTrue(navigated)

        // Verify database save has no folderId
        coVerify { noteRepository.save(match { it.folderId == null }) }
    }
}
