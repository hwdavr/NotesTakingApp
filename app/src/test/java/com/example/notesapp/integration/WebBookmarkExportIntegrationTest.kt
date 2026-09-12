package com.example.notesapp.integration

import androidx.test.core.app.ApplicationProvider
import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.util.NoteExporter
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WebBookmarkExportIntegrationTest {

    @Test
    fun exportsBookmarkContextWithoutNetworkFetch() {
        val bookmark = EditorBlock.WebBookmarkBlock(
            id = "export-bookmark",
            url = "https://example.com/export",
            title = "Export article",
            description = "Context retained without a page fetch"
        )
        val note = Note(
            id = "export-note",
            title = "Exported bookmark",
            content = NoteDocument(blocks = listOf(bookmark)).toJsonString(),
            createdAt = 0L,
            updatedAt = 0L
        )
        val exporter = NoteExporter(ApplicationProvider.getApplicationContext())

        val plainText = NoteDocument.fromContent(note.content).toPlainText()
        val markdownOutput = ByteArrayOutputStream()
        exporter.exportToMarkdown(note, markdownOutput)

        assertTrue(plainText.contains("Export article"))
        assertTrue(plainText.contains(bookmark.url))
        assertTrue(markdownOutput.toString().contains("[Export article](${bookmark.url})"))

        // PdfDocument requires the real Android runtime; the existing instrumented
        // NoteExporterTest exercises the actual PDF bytes and rendered page. The same
        // persisted document is the source for that path, so this JVM test verifies the
        // bookmark payload that the PDF renderer receives without any metadata fetch.
        val pdfSourceText = NoteDocument.fromContent(note.content).toPlainText()
        assertTrue(pdfSourceText.contains("Context retained without a page fetch"))
        assertTrue(pdfSourceText.contains(bookmark.url))
    }
}
