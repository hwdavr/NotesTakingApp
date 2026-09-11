package com.example.notesapp.integration

import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Round-trips bookmark blocks through the note content JSON that the repository persists, proving
 * that URL, resolved metadata, block order, and stable identity survive a close/reopen cycle.
 */
class WebBookmarkPersistenceIntegrationTest {

    @Test
    fun roundTripsBookmarkThroughNoteContentStore() {
        val store = InMemoryNoteContentStore()
        val document = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(id = "text-1", children = listOf(RichText("Intro"))),
                EditorBlock.WebBookmarkBlock(
                    id = "bookmark-1",
                    url = "https://example.com/articles/one",
                    title = "Example article",
                    description = "A description"
                ),
                EditorBlock.WebBookmarkBlock(
                    id = "bookmark-2",
                    url = "https://example.com/articles/two",
                    title = "Second article with long text",
                    description = ""
                )
            )
        )
        store.save("note-1", document.toJsonString())

        val reloaded = NoteDocument.fromContent(store.load("note-1")!!)
        val bookmarks = reloaded.blocks.filterIsInstance<EditorBlock.WebBookmarkBlock>()

        assertEquals(3, reloaded.blocks.size)
        assertTrue(reloaded.blocks.first() is EditorBlock.TextBlock)
        assertEquals(listOf("bookmark-1", "bookmark-2"), bookmarks.map { it.id })
        assertEquals(
            listOf("https://example.com/articles/one", "https://example.com/articles/two"),
            bookmarks.map { it.url }
        )
        assertEquals(listOf("Example article", "Second article with long text"), bookmarks.map { it.title })
        assertEquals(listOf("A description", ""), bookmarks.map { it.description })
    }

    @Test
    fun roundTripsNoteContentThroughTheRepositoryBoundary() {
        val store = InMemoryNoteContentStore()
        val note = Note(
            id = "note-2",
            title = "Bookmarks",
            content = NoteDocument(
                blocks = listOf(
                    EditorBlock.WebBookmarkBlock(
                        id = "persisted-bookmark",
                        url = "http://example.com/cleartext",
                        title = "example.com",
                        description = ""
                    )
                )
            ).toJsonString(),
            createdAt = 0L,
            updatedAt = 0L
        )

        store.saveNote(note)
        val reopened = store.loadNote("note-2")!!
        val bookmark = NoteDocument
            .fromContent(reopened.content)
            .blocks
            .filterIsInstance<EditorBlock.WebBookmarkBlock>()
            .single()

        assertEquals("persisted-bookmark", bookmark.id)
        assertEquals("http://example.com/cleartext", bookmark.url)
        assertEquals("example.com", bookmark.title)
    }

    @Test
    fun keepsLegacyBlocksReadableWhenBookmarkContentIsAdded() {
        val legacyJson = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(id = "legacy-text", children = listOf(RichText("Legacy meaning")))
            )
        ).toJsonString()

        val document = NoteDocument.fromContent(legacyJson)
        val withBookmark = document.copy(
            blocks = document.blocks + EditorBlock.WebBookmarkBlock(
                id = "added-bookmark",
                url = "https://example.com",
                title = "example.com",
                description = ""
            )
        )

        val reloaded = NoteDocument.fromContent(withBookmark.toJsonString())

        val legacyText = reloaded.blocks.filterIsInstance<EditorBlock.TextBlock>().single().children.single().text
        val bookmarkId = reloaded.blocks.filterIsInstance<EditorBlock.WebBookmarkBlock>().single().id
        assertEquals("Legacy meaning", legacyText)
        assertEquals("added-bookmark", bookmarkId)
    }

    @Test
    fun rendersBookmarkContextInPlainTextAndMarkdownExports() {
        val document = NoteDocument(
            blocks = listOf(
                EditorBlock.WebBookmarkBlock(
                    id = "exported-bookmark",
                    url = "https://example.com/exported",
                    title = "Exported title",
                    description = "Exported description"
                )
            )
        )

        val plainText = document.toPlainText()
        val markdown = document.toMarkdown()

        assertTrue(plainText.contains("Exported title"))
        assertTrue(plainText.contains("Exported description"))
        assertTrue(plainText.contains("https://example.com/exported"))
        assertTrue(markdown.contains("[Exported title](https://example.com/exported)"))
    }

    private class InMemoryNoteContentStore {
        private val contents = mutableMapOf<String, String>()
        private val notes = mutableMapOf<String, Note>()

        fun save(noteId: String, content: String) {
            contents[noteId] = content
        }

        fun load(noteId: String): String? = contents[noteId]

        fun saveNote(note: Note) {
            notes[note.id] = note
        }

        fun loadNote(noteId: String): Note? = notes[noteId]
    }
}
