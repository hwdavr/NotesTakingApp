package com.example.notesapp.ui.share.viewmodel

import com.example.notesapp.R
import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.share.NoteShareAccessRole
import com.example.notesapp.domain.share.NoteShareRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShareInviteViewModelTest : BaseViewModelTest() {
    private val noteShareRepository: NoteShareRepository = mockk(relaxed = true)
    private lateinit var viewModel: ShareInviteViewModel

    @Before
    fun setup() {
        viewModel = ShareInviteViewModel(noteShareRepository)
    }

    @Test
    fun `load sets noteId`() {
        viewModel.load("note1")
        assertEquals("note1", viewModel.uiState.value.noteId)
    }

    @Test
    fun `onEmailChange updates state`() {
        viewModel.onEmailChange("test@example.com")
        assertEquals("test@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.errorMessageRes)
    }

    @Test
    fun `onRoleSelected updates state`() {
        viewModel.onRoleSelected(NoteShareAccessRole.READ_ONLY)
        assertEquals(NoteShareAccessRole.READ_ONLY, viewModel.uiState.value.selectedRole)
    }

    @Test
    fun `invite with invalid email sets error`() {
        viewModel.onEmailChange("invalid")
        viewModel.invite()
        assertEquals(R.string.share_invite_invalid_email_error, viewModel.uiState.value.errorMessageRes)
    }

    @Test
    fun `invite success emits event`() = runTest(UnconfinedTestDispatcher()) {
        viewModel.load("note1")
        viewModel.onEmailChange("valid@example.com")
        
        val events = mutableListOf<ShareInviteEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }
        
        viewModel.invite()
        advanceUntilIdle()
        
        assertEquals(1, events.size)
        assertEquals(ShareInviteEvent.InviteSucceeded, events[0])
        coVerify { noteShareRepository.inviteNoteShare("note1", "valid@example.com", NoteShareAccessRole.FULL_ACCESS) }
        assertFalse(viewModel.uiState.value.isSubmitting)
        job.cancel()
    }

    @Test
    fun `invite failure sets generic error`() = runTest {
        coEvery { noteShareRepository.inviteNoteShare(any(), any(), any()) } throws Exception("API Error")
        viewModel.load("note1")
        viewModel.onEmailChange("valid@example.com")
        
        viewModel.invite()
        advanceUntilIdle()
        
        assertEquals(R.string.share_invite_generic_error, viewModel.uiState.value.errorMessageRes)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }
}
