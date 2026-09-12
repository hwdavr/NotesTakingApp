@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.auth.AuthManager
import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.comment.model.NoteBlockComment
import com.example.notesapp.domain.comment.repository.NoteCommentRepository
import com.example.notesapp.domain.folder.Folder
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
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DiscussionViewModelTest : BaseViewModelTest() {

    private val commentRepository: NoteCommentRepository = mockk(relaxed = true)
    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val noteShareRepository: NoteShareRepository = mockk(relaxed = true)
    private val authManager: AuthManager = mockk(relaxed = true)
    private lateinit var viewModel: DiscussionViewModel

    private val activeNote = Note(
        id = "note-1",
        title = "Current note",
        content = "Current content",
        folderId = "folder-1",
        createdAt = 1L,
        updatedAt = 1L
    )
    private val otherNote = Note(
        id = "note-2",
        title = "Roadmap",
        content = "Roadmap content",
        folderId = "folder-1",
        createdAt = 1L,
        updatedAt = 1L
    )

    @Before
    fun setUp() {
        every { authManager.profileEmail } returns MutableStateFlow("owner@example.com")
        every { noteRepository.getActiveNotes() } returns flowOf(listOf(activeNote, otherNote))
        every { noteShareRepository.observeNoteShares("note-1") } returns flowOf(
            listOf(
                NoteShare(
                    id = "share-active",
                    noteId = "note-1",
                    userId = "user-2",
                    email = "alice@example.com",
                    displayName = "Alice",
                    accessRole = NoteShareAccessRole.EDITOR,
                    status = NoteShareStatus.ACTIVE,
                    invitedByUserId = "owner",
                    createdAt = 1L,
                    updatedAt = 1L
                ),
                NoteShare(
                    id = "share-pending",
                    noteId = "note-1",
                    userId = null,
                    email = "pending@example.com",
                    displayName = "Pending",
                    accessRole = NoteShareAccessRole.VIEWER,
                    status = NoteShareStatus.PENDING,
                    invitedByUserId = "owner",
                    createdAt = 1L,
                    updatedAt = 1L
                )
            )
        )
        every { commentRepository.observeComments("note-1", "block-1") } returns flowOf(emptyList())
        coEvery { noteShareRepository.refreshNoteShares("note-1") } returns Unit
        coEvery { commentRepository.refreshComments("note-1", "block-1") } returns Unit
        viewModel = DiscussionViewModel(
            commentRepository = commentRepository,
            noteRepository = noteRepository,
            noteShareRepository = noteShareRepository,
            authManager = authManager,
            clock = Clock.fixed(
                Instant.parse("2026-09-12T12:00:00Z"),
                ZoneId.of("Asia/Singapore")
            )
        )
    }

    @Test
    fun `opening discussion shows dates active collaborators and other notes`() = runTest {
        viewModel.open(
            noteId = "note-1",
            blockId = "block-1",
            focusedBlockText = "Current paragraph",
            folders = listOf(Folder(id = "folder-1", name = "Planning", createdAt = 1L, updatedAt = 1L))
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isVisible)
        assertEquals("@", state.commentText)
        assertEquals("Current paragraph", state.focusedBlockText)
        assertEquals(
            listOf("Today", "Tomorrow", "Next Tuesday 3pm"),
            state.mentionDates.map { it.insertText.removePrefix("@") }
        )
        assertEquals(listOf("owner@example.com", "alice@example.com"), state.mentionUsers.map { it.email })
        assertEquals(listOf("Roadmap"), state.mentionNotes.map { it.title })
        assertFalse(state.mentionUsers.any { it.email == "pending@example.com" })
    }

    @Test
    fun `mention completion replaces only the active at query and preserves surrounding text`() = runTest {
        viewModel.open("note-1", "block-1", "Current paragraph", emptyList())
        viewModel.onCommentValueChange("See @Road", 9, 9)

        viewModel.applyMentionCompletion("@Roadmap")

        assertEquals("See @Roadmap ", viewModel.uiState.value.commentText)
        assertEquals(13, viewModel.uiState.value.selectionStart)
        assertEquals(13, viewModel.uiState.value.selectionEnd)
    }

    @Test
    fun `sending a comment delegates trimmed text and clears the composer`() = runTest {
        viewModel.open("note-1", "block-1", "Current paragraph", emptyList())
        val savedComment = NoteBlockComment(
            id = "comment-1",
            noteId = "note-1",
            blockId = "block-1",
            authorUserId = "owner",
            authorDisplayName = "Owner",
            authorEmail = "owner@example.com",
            body = "A comment",
            createdAt = 1L,
            updatedAt = 1L
        )
        coEvery { commentRepository.addComment("note-1", "block-1", "A comment") } returns savedComment
        viewModel.onCommentValueChange("  A comment  ", 12, 12)

        viewModel.sendComment()
        advanceUntilIdle()

        coVerify { commentRepository.addComment("note-1", "block-1", "A comment") }
        assertEquals("", viewModel.uiState.value.commentText)
        assertFalse(viewModel.uiState.value.isMentionSuggestionsVisible)
    }
}
