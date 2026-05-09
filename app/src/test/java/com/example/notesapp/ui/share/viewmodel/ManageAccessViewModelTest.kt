package com.example.notesapp.ui.share.viewmodel

import com.example.notesapp.R
import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.share.NoteShare
import com.example.notesapp.domain.share.NoteShareAccessRole
import com.example.notesapp.domain.share.NoteShareRepository
import com.example.notesapp.domain.share.NoteShareStatus
import com.example.notesapp.ui.share.model.ManageAccessPermission
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ManageAccessViewModelTest : BaseViewModelTest() {
    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val noteShareRepository: NoteShareRepository = mockk(relaxed = true)
    private lateinit var viewModel: ManageAccessViewModel

    private val noteId = "note1"
    private val sharesFlow = MutableStateFlow(
        listOf(
            NoteShare(
                id = "share1",
                noteId = noteId,
                userId = "user1",
                email = "ben@example.com",
                displayName = "Ben Lee",
                accessRole = NoteShareAccessRole.FULL_ACCESS,
                status = NoteShareStatus.ACTIVE,
                invitedByUserId = "owner",
                createdAt = 0L,
                updatedAt = 0L
            ),
            NoteShare(
                id = "share2",
                noteId = noteId,
                userId = "user2",
                email = "clara@example.com",
                displayName = "Clara Wong",
                accessRole = NoteShareAccessRole.READ_ONLY,
                status = NoteShareStatus.ACTIVE,
                invitedByUserId = "owner",
                createdAt = 0L,
                updatedAt = 0L
            )
        )
    )

    @Before
    fun setup() {
        every { noteShareRepository.observeNoteShares(noteId) } returns sharesFlow
        coEvery { noteRepository.getNoteById(noteId) } returns Note(
            id = noteId,
            title = "Force update strategy",
            content = "",
            createdAt = 0L,
            updatedAt = 0L
        )

        viewModel = ManageAccessViewModel(noteRepository, noteShareRepository)
    }

    @Test
    fun `load maps note and share data into state`() = runTest {
        viewModel.load(noteId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(noteId, state.noteId)
        assertEquals("Force update strategy", state.noteTitle)
        assertEquals(2, state.users.size)
        assertEquals(ManageAccessPermission.EDITOR, state.users[0].selectedPermission)
        assertEquals(ManageAccessPermission.VIEWER, state.users[1].selectedPermission)
    }

    @Test
    fun `onPermissionSelected enables confirm when a change exists`() = runTest {
        viewModel.load(noteId)
        advanceUntilIdle()

        viewModel.onPermissionSelected("share1", ManageAccessPermission.VIEWER)

        val state = viewModel.uiState.value
        assertEquals(
            ManageAccessPermission.VIEWER,
            state.users.first { it.id == "share1" }.selectedPermission
        )
        assertTrue(state.isConfirmEnabled)
    }

    @Test
    fun `confirmChanges updates roles and deletes marked users`() = runTest(UnconfinedTestDispatcher()) {
        viewModel.load(noteId)
        advanceUntilIdle()
        viewModel.onPermissionSelected("share1", ManageAccessPermission.VIEWER)
        viewModel.onPermissionSelected("share2", ManageAccessPermission.DELETE)

        val events = mutableListOf<ManageAccessEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.confirmChanges()
        advanceUntilIdle()

        coVerify {
            noteShareRepository.updateNoteShareRole(noteId, "share1", NoteShareAccessRole.READ_ONLY)
        }
        coVerify {
            noteShareRepository.deleteNoteShare(noteId, "share2")
        }
        assertEquals(listOf(ManageAccessEvent.ConfirmSucceeded), events)
        assertFalse(viewModel.uiState.value.isSubmitting)
        job.cancel()
    }

    @Test
    fun `confirmChanges failure shows error`() = runTest {
        coEvery {
            noteShareRepository.updateNoteShareRole(noteId, "share1", NoteShareAccessRole.READ_ONLY)
        } throws Exception("failed")

        viewModel.load(noteId)
        advanceUntilIdle()
        viewModel.onPermissionSelected("share1", ManageAccessPermission.VIEWER)

        viewModel.confirmChanges()
        advanceUntilIdle()

        assertEquals(R.string.manage_access_error, viewModel.uiState.value.errorMessageRes)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }
}
