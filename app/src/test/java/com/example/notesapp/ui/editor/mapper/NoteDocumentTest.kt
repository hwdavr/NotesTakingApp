package com.example.notesapp.ui.editor.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteDocumentTest {

    @Test
    fun `toPlainText joins blocks correctly`() {
        val doc = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(children = listOf(RichText("Hello"))),
                EditorBlock.TextBlock(children = listOf(RichText("World")))
            )
        )
        assertEquals("Hello\nWorld", doc.toPlainText())
    }

    @Test
    fun `toMarkdown handles heading and bulleted list`() {
        val doc = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(type = "heading", children = listOf(RichText("Title"))),
                EditorBlock.TextBlock(type = "bulleted", children = listOf(RichText("Item 1"))),
                EditorBlock.TextBlock(type = "paragraph", children = listOf(RichText("Normal text")))
            )
        )
        val expected = "# Title\n\n- Item 1\n\nNormal text"
        assertEquals(expected, doc.toMarkdown())
    }

    @Test
    fun `toMarkdown handles inline marks`() {
        val doc = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(
                    children = listOf(
                        RichText("Bold", listOf("bold")),
                        RichText(" and "),
                        RichText("Italic", listOf("italic")),
                        RichText(" and "),
                        RichText("Code", listOf("code"))
                    )
                )
            )
        )
        assertEquals("**Bold** and *Italic* and `Code`", doc.toMarkdown())
    }

    @Test
    fun `toMarkdown handles table correctly`() {
        val doc = NoteDocument(
            blocks = listOf(
                EditorBlock.TableBlock(
                    rows = listOf(
                        listOf(listOf(RichText("H1")), listOf(RichText("H2"))),
                        listOf(listOf(RichText("C1")), listOf(RichText("C2")))
                    )
                )
            )
        )
        val expected = "| H1 | H2 |\n| --- | --- |\n| C1 | C2 |"
        assertEquals(expected, doc.toMarkdown())
    }

    @Test
    fun `fromContent parses json correctly`() {
        val json = """
            {
              "version": 1,
              "blocks": [
                { "id": "b1", "type": "heading", "children": [{ "text": "Hello" }] }
              ]
            }
        """.trimIndent()
        val doc = NoteDocument.fromContent(json)
        assertEquals(1, doc.version)
        assertEquals(1, doc.blocks.size)
        val block = doc.blocks[0] as EditorBlock.TextBlock
        assertEquals("heading", block.type)
        assertEquals("Hello", block.children[0].text)
    }

    @Test
    fun `fromContent falls back to plain text for invalid json`() {
        val doc = NoteDocument.fromContent("Just some plain text")
        assertEquals(1, doc.blocks.size)
        val block = doc.blocks[0] as EditorBlock.TextBlock
        assertEquals("Just some plain text", block.children[0].text)
    }

    @Test
    fun `parseInlineMarkdown handles various combinations`() {
        val text = "Normal **Bold** *Italic* `Code` Mix"
        val result = parseInlineMarkdown(text)

        assertEquals(7, result.size)
        assertEquals("Normal ", result[0].text)
        assertEquals("Bold", result[1].text)
        assertTrue("bold" in result[1].marks)
        assertEquals(" ", result[2].text)
        assertEquals("Italic", result[3].text)
        assertTrue("italic" in result[3].marks)
        assertEquals(" ", result[4].text)
        assertEquals("Code", result[5].text)
        assertTrue("code" in result[5].marks)
        assertEquals(" Mix", result[6].text)
    }

    @Test
    fun `parseMarkdownTextBlock identifies types correctly`() {
        val h = parseMarkdownTextBlock(text = "# Heading")
        assertEquals("heading", h.type)
        assertEquals("Heading", h.children[0].text)

        val b = parseMarkdownTextBlock(text = "- Item")
        assertEquals("bulleted", b.type)
        assertEquals("Item", b.children[0].text)

        val p = parseMarkdownTextBlock(text = "Paragraph")
        assertEquals("paragraph", p.type)
        assertEquals("Paragraph", p.children[0].text)
    }

    @Test
    fun `toJsonString and fromContent are symmetric`() {
        val original = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(type = "heading", children = listOf(RichText("Hello"))),
                EditorBlock.ImageBlock(url = "url", caption = "cap"),
                EditorBlock.TableBlock()
            )
        )
        val json = original.toJsonString()
        val restored = NoteDocument.fromContent(json)

        assertEquals(original.blocks.size, restored.blocks.size)
        assertEquals(
            (original.blocks[0] as EditorBlock.TextBlock).type,
            (restored.blocks[0] as EditorBlock.TextBlock).type
        )
        assertEquals(
            (original.blocks[1] as EditorBlock.ImageBlock).url,
            (restored.blocks[1] as EditorBlock.ImageBlock).url
        )
        assertEquals(
            (original.blocks[2] as EditorBlock.TableBlock).rows.size,
            (restored.blocks[2] as EditorBlock.TableBlock).rows.size
        )
    }
}
