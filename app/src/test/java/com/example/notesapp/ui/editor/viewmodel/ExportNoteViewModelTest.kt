package com.example.notesapp.ui.editor.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.util.NoteExporter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExportNoteViewModelTest {

    private val context: Context = mockk()
    private val noteRepository: NoteRepository = mockk()
    private val noteExporter: NoteExporter = mockk()
    private val contentResolver: ContentResolver = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: ExportNoteViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { context.contentResolver } returns contentResolver
        viewModel = ExportNoteViewModel(context, noteRepository, noteExporter, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNote updates uiState with note`() = runTest {
        val note =
            Note(id = "n1", title = "Title", content = "Content", folderId = "f1", createdAt = 0L, updatedAt = 0L)
        coEvery { noteRepository.getNoteById("n1") } returns note

        viewModel.loadNote("n1")

        assertEquals(note, viewModel.uiState.value.note)
    }

    @Test
    fun `loadNote marks chart notes for package export`() = runTest {
        val note = Note(
            id = "chart-note",
            title = "Chart note",
            content = NoteDocument(
                blocks = listOf(EditorBlock.ChartBlock(id = "chart-1"))
            ).toJsonString(),
            createdAt = 0L,
            updatedAt = 0L
        )
        coEvery { noteRepository.getNoteById("chart-note") } returns note

        viewModel.loadNote("chart-note")

        assertTrue(viewModel.uiState.value.hasChart)
    }

    @Test
    fun `selectFormat updates uiState`() {
        viewModel.selectFormat(ExportFormat.PDF)
        assertEquals(ExportFormat.PDF, viewModel.uiState.value.selectedFormat)
    }

    @Test
    fun `exportToUri calls exporter and updates success state`() = runTest {
        val note =
            Note(id = "n1", title = "Title", content = "Content", folderId = "f1", createdAt = 0L, updatedAt = 0L)
        coEvery { noteRepository.getNoteById("n1") } returns note
        viewModel.loadNote("n1")

        val uri: Uri = mockk()
        val outputStream: OutputStream = mockk(relaxed = true)
        every { contentResolver.openOutputStream(uri) } returns outputStream
        coEvery { noteExporter.exportToMarkdown(any(), any()) } returns Unit

        viewModel.exportToUri(uri)

        // Since ExportNoteViewModel uses Dispatchers.IO, we might need to wait or use a more robust way to test.
        // But with UnconfinedTestDispatcher and runTest, it might just work if we use advanceUntilIdle().
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.exportSuccess)
        coVerify { noteExporter.exportToMarkdown(note, any()) }
    }

    @Test
    fun `exportToUri calls exporter for PDF and updates success state`() = runTest {
        val note =
            Note(id = "n1", title = "Title", content = "Content", folderId = "f1", createdAt = 0L, updatedAt = 0L)
        coEvery { noteRepository.getNoteById("n1") } returns note
        viewModel.loadNote("n1")
        viewModel.selectFormat(ExportFormat.PDF)

        val uri: Uri = mockk()
        val outputStream: OutputStream = mockk(relaxed = true)
        every { contentResolver.openOutputStream(uri) } returns outputStream
        coEvery { noteExporter.exportToPdf(any(), any()) } returns Unit

        viewModel.exportToUri(uri)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.exportSuccess)
        coVerify { noteExporter.exportToPdf(note, any()) }
    }

    @Test
    fun `exportToUri handles exception and updates error state`() = runTest {
        val note =
            Note(id = "n1", title = "Title", content = "Content", folderId = "f1", createdAt = 0L, updatedAt = 0L)
        coEvery { noteRepository.getNoteById("n1") } returns note
        viewModel.loadNote("n1")

        val uri: Uri = mockk()
        every { contentResolver.openOutputStream(uri) } throws Exception("Export failed")

        viewModel.exportToUri(uri)
        advanceUntilIdle()

        assertEquals("Export failed", viewModel.uiState.value.error)
        assertTrue(!viewModel.uiState.value.exportSuccess)
    }

    @Test
    fun `resetStatus clears success and error state`() {
        val uri: Uri = mockk()
        every { contentResolver.openOutputStream(uri) } throws Exception("Export failed")

        val note =
            Note(id = "n1", title = "Title", content = "Content", folderId = "f1", createdAt = 0L, updatedAt = 0L)
        coEvery { noteRepository.getNoteById("n1") } returns note
        viewModel.loadNote("n1")

        viewModel.exportToUri(uri)
        assertEquals("Export failed", viewModel.uiState.value.error)

        viewModel.resetStatus()
        assertEquals(null, viewModel.uiState.value.error)
        assertTrue(!viewModel.uiState.value.exportSuccess)
    }
}
