package com.example.notesapp.editor

import com.example.notesapp.base.BaseViewModelIntegrationTest
import com.example.notesapp.data.local.NoteEntity
import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.text
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteEmojiPersistenceIntegrationTest : BaseViewModelIntegrationTest() {
    @Test
    fun unicodeEmojiSurvivesSaveReloadSyncShareAndExport() = runTest {
        val defaultEmoji = "😀"
        val skinToneEmoji = "👍🏽"
        val document = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(
                    id = "body",
                    children = listOf(RichText("Launch $defaultEmoji with $skinToneEmoji"))
                )
            )
        )
        val note = Note(
            id = "emoji-note",
            title = "Emoji launch",
            content = document.toJsonString(),
            sortKey = "1",
            version = 1L,
            deviceId = "test_device",
            lastSyncedVersion = 1L,
            createdAt = 1L,
            updatedAt = 2L
        )
        fakeNoteDao.insert(
            NoteEntity(
                id = note.id,
                title = note.title,
                content = NoteDocument.empty().toJsonString(),
                sortKey = note.sortKey,
                version = note.version,
                deviceId = note.deviceId,
                lastSyncedVersion = note.lastSyncedVersion,
                createdAt = note.createdAt,
                updatedAt = note.createdAt
            )
        )
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        noteRepository.save(note)

        val syncRequest = mockWebServer.takeRequest()
        val syncedContent = JSONObject(syncRequest.body.readUtf8()).getString("content")
        assertEquals(document.toJsonString(), syncedContent)
        assertTrue(syncedContent.contains(defaultEmoji))
        assertTrue(syncedContent.contains(skinToneEmoji))

        val locallyReloadedNote = fakeNoteDao.getNoteById(note.id)
        val reloadedDocument = NoteDocument.fromContent(locallyReloadedNote?.content.orEmpty())
        val reloadedText = (reloadedDocument.blocks.single() as EditorBlock.TextBlock).text()
        assertEquals("Launch $defaultEmoji with $skinToneEmoji", reloadedText)
        assertEquals("Launch $defaultEmoji with $skinToneEmoji", reloadedDocument.toPlainText())
        assertTrue(reloadedDocument.toMarkdown().contains(defaultEmoji))
        assertTrue(reloadedDocument.toMarkdown().contains(skinToneEmoji))
    }
}
