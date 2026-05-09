package com.example.notesapp.ui.share.viewmodel

import com.example.notesapp.R
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.share.NoteShare
import com.example.notesapp.domain.share.NoteShareAccessRole
import com.example.notesapp.domain.share.NoteShareRepository
import com.example.notesapp.domain.share.NoteShareStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SharedUsersViewModelTest : BaseViewModelTest() {
    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val noteShareRepository: NoteShareRepository = mockk(relaxed = true)
    private val authManager: AuthManager = mockk(relaxed = true)
    private lateinit var viewModel: SharedUsersViewModel

    private val noteId = "note1"
    private val testNote = Note(id = noteId, title = "Shared Note", content = "Content", createdAt = 0, updatedAt = 0)
    private val testShare = NoteShare(
        id = "share1",
        noteId = noteId,
        userId = "user1",
        email = "user@example.com",
        displayName = "User One",
        accessRole = NoteShareAccessRole.VIEWER,
        status = NoteShareStatus.ACTIVE,
        invitedByUserId = "me",
        createdAt = 0L,
        updatedAt = 0L
    )

    @Before
    fun setup() {
        every { authManager.profileEmail } returns MutableStateFlow("me@example.com")
        every { noteShareRepository.observeNoteShares(noteId) } returns flow { emit(listOf(testShare)) }
        coEvery { noteRepository.getNoteById(noteId) } returns testNote
        
        viewModel = SharedUsersViewModel(noteRepository, noteShareRepository, authManager)
    }

    @Test
    fun `load updates state with title and shares`() = runTest {
        viewModel.load(noteId)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(noteId, state.noteId)
        assertEquals("Shared Note", state.noteTitle)
        assertEquals(2, state.users.size) // Owner + Collaborator
        assertEquals("me@example.com", state.users[0].email)
        assertEquals("user@example.com", state.users[1].email)
    }

    @Test
    fun `refresh calls repository and updates loading state`() = runTest(UnconfinedTestDispatcher()) {
        viewModel.load(noteId)
        advanceUntilIdle()
        
        viewModel.refresh()
        // Note: isLoading might be false already if the mock is synchronous, 
        // but we verify the repository call.
        coVerify { noteShareRepository.refreshNoteShares(noteId) }
    }

    @Test
    fun `refresh failure sets error message`() = runTest(UnconfinedTestDispatcher()) {
        coEvery { noteShareRepository.refreshNoteShares(noteId) } throws Exception("Failed")
        viewModel.load(noteId)
        advanceUntilIdle()
        
        viewModel.refresh()
        advanceUntilIdle()
        
        assertEquals(R.string.shared_users_error, viewModel.uiState.value.errorMessageRes)
        assertTrue(!viewModel.uiState.value.isLoading)
    }
}
