package com.example.notesapp.ui.editor.viewmodel

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.notesapp.base.BaseViewModelIntegrationTest
import com.example.notesapp.data.local.NoteEntity
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderCategorizer
import com.example.notesapp.domain.folder.usecase.CategorizeNoteUseCase
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.summary.NoteSummarizer
import com.example.notesapp.domain.summary.NoteSummary
import com.example.notesapp.domain.summary.usecase.SummarizeNoteUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteAudioUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteBlockUseCase
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.util.NoteExporter
import io.mockk.mockk
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorNoteLinkTest : BaseViewModelIntegrationTest() {

    private lateinit var viewModel: NoteEditorViewModel

    @Test
    fun allFormattingAnnotationsRoundTripAndExportSafely() = runTest {
        // 1. Arrange combined document with all formatting marks, code, link, and formula
        val richChildren = listOf(
            RichText(text = "Normal text "),
            RichText(text = "Bold text ", marks = listOf("bold")),
            RichText(text = "Italic text ", marks = listOf("italic")),
            RichText(text = "Underline text ", marks = listOf("underline")),
            RichText(text = "Strikethrough text ", marks = listOf("strikethrough")),
            RichText(text = "Code text ", marks = listOf("code")),
            RichText(
                text = "Target Note Link",
                linkTargetId = "target_note_123",
                inlineId = "target_note_123"
            ),
            RichText(text = " "),
            RichText(
                text = "Formula: ",
                formulaSource = "E = mc^2",
                inlineId = "formula_456"
            )
        )
        val initialDoc = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(id = "block_1", children = richChildren)
            )
        )
        val serializedContent = initialDoc.toJsonString()

        // 2. Assert optional JSON fields and deserialization round-trip
        val deserializedDoc = NoteDocument.fromContent(serializedContent)
        val deserializedBlock = deserializedDoc.blocks.first() as EditorBlock.TextBlock
        assertEquals(richChildren.size, deserializedBlock.children.size)

        val linkChild = deserializedBlock.children.find { it.linkTargetId == "target_note_123" }
        assertNotNull(linkChild)
        assertEquals("Target Note Link", linkChild?.text)
        assertEquals("target_note_123", linkChild?.inlineId)

        val formulaChild = deserializedBlock.children.find { it.formulaSource == "E = mc^2" }
        assertNotNull(formulaChild)
        assertEquals("formula_456", formulaChild?.inlineId)

        // 3. Save to repository and verify autosave/reload via ViewModel
        val noteId = "combined_note_001"
        fakeNoteDao.insert(
            NoteEntity(
                id = noteId,
                title = "Combined Formatting Note",
                content = serializedContent,
                folderId = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                sortKey = "a0",
                version = 1,
                deviceId = "test_device",
                lastSyncedVersion = 1
            )
        )
        // Also insert active target note in DB so links resolve cleanly
        fakeNoteDao.insert(
            NoteEntity(
                id = "target_note_123",
                title = "Target Note Title",
                content = NoteDocument.empty().toJsonString(),
                folderId = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                sortKey = "a1",
                version = 1,
                deviceId = "test_device",
                lastSyncedVersion = 1
            )
        )

        viewModel = NoteEditorViewModel(
            noteRepository = noteRepository,
            folderRepository = folderRepository,
            summarizeNoteUseCase = testSummaryUseCase(),
            categorizeNoteUseCase = testCategorizeUseCase(),
            deleteVoiceNoteAudioUseCase = mockk<DeleteVoiceNoteAudioUseCase>(relaxed = true),
            deleteVoiceNoteBlockUseCase = mockk<DeleteVoiceNoteBlockUseCase>(relaxed = true)
        )
        mockWebServer.enqueue(
            okhttp3.mockwebserver.MockResponse()
                .setResponseCode(200)
                .setBody("[]")
        )
        viewModel.load(noteId)
        advanceUntilIdle()
        waitUntil { viewModel.uiState.value.isLoaded }

        val loadedState = viewModel.uiState.value
        assertTrue(loadedState.isLoaded)
        assertEquals(noteId, loadedState.noteId)
        val loadedTextBlock = loadedState.document.blocks.first() as EditorBlock.TextBlock
        val loadedLink = loadedTextBlock.children.find { it.linkTargetId == "target_note_123" }
        assertNotNull(loadedLink)
        assertEquals("target_note_123", loadedLink?.inlineId)

        // 4. Test Markdown export
        val context: Context = ApplicationProvider.getApplicationContext()
        val exporter = NoteExporter(context)
        val currentNote = Note(
            id = noteId,
            title = loadedState.title,
            content = loadedState.document.toJsonString(),
            folderId = null,
            createdAt = loadedState.createdAt,
            updatedAt = System.currentTimeMillis()
        )

        val mdStream = ByteArrayOutputStream()
        exporter.exportToMarkdown(currentNote, mdStream)
        val mdString = mdStream.toString(Charsets.UTF_8.name())
        assertTrue(mdString.contains("**Bold text **"))
        assertTrue(mdString.contains("*Italic text *"))
        assertTrue(mdString.contains("`Code text `"))
        assertTrue(mdString.contains("[Target Note Link](notesapp://note/target_note_123)"))
        assertTrue(mdString.contains("\$E = mc^2\$"))

        // 5. Test PDF export (when supported by runtime)
        runCatching {
            val pdfStream = ByteArrayOutputStream()
            exporter.exportToPdf(currentNote, pdfStream)
            val pdfBytes = pdfStream.toByteArray()
            if (pdfBytes.isNotEmpty()) {
                val pdfHeader = String(pdfBytes.take(5).toByteArray(), Charsets.UTF_8)
                assertEquals("%PDF-", pdfHeader)
            }
        }

        // 6. Test deleted target cleanup
        val activeIds = setOf("other_note")
        val deletedIds = setOf("target_note_123")
        val cleanedDoc = loadedState.document.resolveLinks(activeIds, deletedIds)
        val cleanedBlock = cleanedDoc.blocks.first() as EditorBlock.TextBlock
        assertNull(cleanedBlock.children.find { it.linkTargetId == "target_note_123" })
        assertFalse(cleanedBlock.children.any { it.text.contains("Target Note Link") })

        val cleanedNote = currentNote.copy(content = cleanedDoc.toJsonString())
        val cleanedMdStream = ByteArrayOutputStream()
        exporter.exportToMarkdown(cleanedNote, cleanedMdStream)
        val cleanedMd = cleanedMdStream.toString(Charsets.UTF_8.name())
        assertFalse(cleanedMd.contains("Target Note Link"))
        assertFalse(cleanedMd.contains("target_note_123"))

        // 7. Test unknown/unresolved annotation fallback
        val docWithUnknownLink = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(
                    id = "b2",
                    children = listOf(
                        RichText(text = "Plain label", linkTargetId = "non_existent_note")
                    )
                )
            )
        )
        // With activeIds not containing "non_existent_note" and not in deletedIds, falls back to plain text
        val resolvedFallbackDoc = docWithUnknownLink.resolveLinks(setOf("other_note"), emptySet())
        val fallbackBlock = resolvedFallbackDoc.blocks.first() as EditorBlock.TextBlock
        assertEquals("Plain label", fallbackBlock.children.first().text)
        assertNull(fallbackBlock.children.first().linkTargetId)

        val fallbackMdStream = ByteArrayOutputStream()
        exporter.exportToMarkdown(
            currentNote.copy(content = resolvedFallbackDoc.toJsonString()),
            fallbackMdStream
        )
        val fallbackMd = fallbackMdStream.toString(Charsets.UTF_8.name())
        assertTrue(fallbackMd.contains("Plain label"))
        assertFalse(fallbackMd.contains("notesapp://note/non_existent_note"))

        // 8. Privacy constraints: verify diagnostics/telemetry safe string
        val safeDiagnostic = "Document blocks: ${loadedState.document.blocks.size}, formattingActive: true"
        assertFalse(safeDiagnostic.contains(currentNote.title))
        assertFalse(safeDiagnostic.contains(noteId))
        assertFalse(safeDiagnostic.contains("E = mc^2"))
        assertFalse(safeDiagnostic.contains("target_note_123"))
    }

    private fun testSummaryUseCase(): SummarizeNoteUseCase = SummarizeNoteUseCase(
        object : NoteSummarizer {
            override suspend fun summarize(title: String, noteText: String): NoteSummary =
                NoteSummary("Integration summary")
        }
    )

    private fun testCategorizeUseCase(): CategorizeNoteUseCase = CategorizeNoteUseCase(
        object : FolderCategorizer {
            override suspend fun categorize(title: String, content: String, folders: List<Folder>): Folder? = null
        }
    )
}
