package com.example.notesapp.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteExporterTest {
    @Test
    fun testExportToPdfWithTableAndImage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val noteExporter = NoteExporter(context)
        val content = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(type = "heading", children = listOf(RichText("Test Heading"))),
                EditorBlock.TextBlock(
                    type = "paragraph",
                    children = listOf(RichText("This is a test paragraph with some text."))
                ),
                EditorBlock.TableBlock(
                    rows = listOf(
                        listOf(listOf(RichText("Header 1")), listOf(RichText("Header 2"))),
                        listOf(listOf(RichText("Cell 1")), listOf(RichText("Cell 2")))
                    )
                ),
                EditorBlock.ImageBlock(
                    url = "android.resource://com.example.notesapp/drawable/ic_launcher_foreground",
                    caption = "Test Image"
                )
            )
        ).toJsonString()
        val note = Note(
            id = "test-id",
            title = "Test Note",
            content = content,
            folderId = "folder-id",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val outputFile = File(context.cacheDir, "test_export.pdf")
        val outputStream = FileOutputStream(outputFile)
        noteExporter.exportToPdf(note, outputStream)
        assertTrue("PDF file should exist", outputFile.exists())
        assertTrue("PDF file should not be empty", outputFile.length() > 0)
        // Cleanup
        outputFile.delete()
    }
}
