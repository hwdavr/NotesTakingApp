package com.example.notesapp.ui.share.screen

import com.example.notesapp.domain.share.NoteShare
import com.example.notesapp.domain.share.NoteShareAccessRole
import com.example.notesapp.domain.share.NoteShareStatus
import com.example.notesapp.ui.share.model.AccessRole
import com.example.notesapp.ui.share.model.buildSharedUserUiModels
import com.example.notesapp.ui.share.model.isValidInviteEmail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareUiModelsTest {
    @Test
    fun `buildSharedUserUiModels prepends owner and maps collaborator roles`() {
        val users = buildSharedUserUiModels(
            ownerEmail = "owner@example.com",
            shares = listOf(
                NoteShare(
                    id = "share_1",
                    noteId = "note_1",
                    userId = "auth0|user1",
                    email = "editor@example.com",
                    displayName = "Editor User",
                    accessRole = NoteShareAccessRole.EDITOR,
                    status = NoteShareStatus.ACTIVE,
                    invitedByUserId = "auth0|owner",
                    createdAt = 1L,
                    updatedAt = 1L
                ),
                NoteShare(
                    id = "share_2",
                    noteId = "note_1",
                    userId = null,
                    email = "viewer@example.com",
                    displayName = null,
                    accessRole = NoteShareAccessRole.VIEWER,
                    status = NoteShareStatus.PENDING,
                    invitedByUserId = "auth0|owner",
                    createdAt = 2L,
                    updatedAt = 2L
                )
            )
        )
        assertEquals(3, users.size)
        assertEquals(AccessRole.OWNER, users[0].role)
        assertEquals("owner@example.com", users[0].email)
        assertEquals(AccessRole.EDITOR, users[1].role)
        assertEquals(AccessRole.VIEWER, users[2].role)
        assertTrue(users[2].isPending)
    }

    @Test
    fun `isValidInviteEmail accepts standard email and rejects invalid values`() {
        assertTrue(isValidInviteEmail("invitee@example.com"))
        assertFalse(isValidInviteEmail("invitee"))
        assertFalse(isValidInviteEmail("invitee@"))
        assertFalse(isValidInviteEmail(" "))
    }
}
