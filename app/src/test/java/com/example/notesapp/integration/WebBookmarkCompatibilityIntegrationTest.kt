package com.example.notesapp.integration

import com.example.notesapp.domain.bookmark.WebBookmarkUrlValidator
import com.example.notesapp.domain.bookmark.WebBookmarkValidationResult
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebBookmarkCompatibilityIntegrationTest {

    @Test
    fun preservesLegacyAndMalformedBookmarkContentSafely() {
        val legacy = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(id = "legacy-text", children = listOf(RichText("Legacy text")))
            )
        ).toJsonString()
        val legacyDocument = NoteDocument.fromContent(legacy)
        assertEquals("Legacy text", legacyDocument.toPlainText())

        val malformed = """
            {
              "blocks": [
                {"id":"bad-bookmark","type":"web_bookmark","url":42,"title":null,"description":{}},
                {"id":"unknown","type":"future_block","children":[{"text":"Still readable"}]}
              ]
            }
        """.trimIndent()
        val recovered = NoteDocument.fromContent(malformed)
        val bookmark = recovered.blocks.filterIsInstance<EditorBlock.WebBookmarkBlock>().single()

        assertEquals("bad-bookmark", bookmark.id)
        assertEquals("", bookmark.url)
        assertEquals("", bookmark.title)
        assertEquals("", bookmark.description)
        assertTrue(recovered.toPlainText().isNotBlank())
        assertTrue(
            WebBookmarkUrlValidator.validate(bookmark.url) is WebBookmarkValidationResult.Invalid
        )

        val roundTripped = NoteDocument.fromContent(recovered.toJsonString())
        assertEquals(recovered.toPlainText(), roundTripped.toPlainText())
    }
}
