package com.example.notesapp.ui.editor.document

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteDocumentTest {

    @Test
    fun `plain text content converts to paragraph blocks`() {
        val document = NoteDocument.fromContent("Hello\n# Heading")

        assertEquals(2, document.blocks.size)
        assertEquals("Hello\nHeading", document.toPlainText())
        assertEquals("heading", (document.blocks[1] as EditorBlock.TextBlock).type)
    }

    @Test
    fun `markdown inline syntax converts to marks`() {
        val block = parseMarkdownTextBlock(text = "Hello **world** and *friends*")

        assertEquals("Hello world and friends", block.text())
        assertTrue(block.children.any { it.text == "world" && "bold" in it.marks })
        assertTrue(block.children.any { it.text == "friends" && "italic" in it.marks })
    }

    @Test
    fun `unmatched markdown markers remain plain text`() {
        val block = parseMarkdownTextBlock(text = "Keep * and ` as text")

        assertEquals("Keep * and ` as text", block.text())
    }

    @Test
    fun `document serializes image and table blocks as structured json`() {
        val document = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(id = "b1", children = listOf(RichText("Hello"))),
                EditorBlock.ImageBlock(id = "b2", url = "https://cdn.example.com/image.png", caption = "My image"),
                EditorBlock.TableBlock(
                    id = "b3",
                    rows = listOf(
                        listOf(listOf(RichText("Name")), listOf(RichText("Age"))),
                        listOf(listOf(RichText("Alice")), listOf(RichText("10")))
                    )
                )
            )
        )

        val json = JSONObject(document.toJsonString())
        val blocks = json.getJSONArray("blocks")

        assertEquals(1, json.getInt("version"))
        assertEquals("paragraph", blocks.getJSONObject(0).getString("type"))
        assertEquals("image", blocks.getJSONObject(1).getString("type"))
        assertEquals("table", blocks.getJSONObject(2).getString("type"))
        assertEquals(
            "Alice",
            blocks.getJSONObject(
                2
            ).getJSONArray("rows").getJSONArray(1).getJSONArray(0).getJSONObject(0).getString("text")
        )
    }
}
