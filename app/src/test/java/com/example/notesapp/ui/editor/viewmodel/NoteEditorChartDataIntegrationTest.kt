package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.folder.usecase.CategorizeNoteUseCase
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.summary.NoteSummaryResult
import com.example.notesapp.domain.summary.usecase.SummarizeNoteUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteAudioUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteBlockUseCase
import com.example.notesapp.ui.editor.mapper.ChartType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.normalized
import com.example.notesapp.ui.editor.model.ChartTableAction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorChartDataIntegrationTest : BaseViewModelTest() {
    private lateinit var noteRepository: NoteRepository
    private lateinit var folderRepository: FolderRepository
    private lateinit var viewModel: NoteEditorViewModel

    @Before
    fun setup() {
        noteRepository = mockk(relaxed = true)
        folderRepository = mockk(relaxed = true)
        every { folderRepository.getFolders() } returns flowOf(emptyList())
        viewModel = newViewModel()
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "chart-note",
            title = "Sales",
            document = NoteDocument(blocks = listOf(chartBlock())),
            isLoaded = true
        )
    }

    @Test
    fun testOptionsRowActionsAndProtectedColumnInvariants() {
        val chartId = chartBlock().id

        assertTrue(viewModel.addChartRow(chartId))
        assertEquals(3, currentChart().rows.size)

        assertTrue(viewModel.addChartColumn(chartId))
        assertEquals(3, currentChart().columnIds.size)
        assertEquals("c_column_3", currentChart().columnIds.last())
        assertEquals("c_value", currentChart().selectedColumnId)

        assertFalse(viewModel.deleteChartColumn(chartId, 0))
        assertTrue(viewModel.deleteChartColumn(chartId, 1))
        assertEquals(2, currentChart().columnIds.size)
        assertEquals("c_column_3", currentChart().selectedColumnId)
        assertFalse(viewModel.deleteChartColumn(chartId, 1))
    }

    @Test
    fun testChartEditsAndSelectionReloadFromAutoSave() = runTest {
        var savedNote: Note? = null
        coEvery { noteRepository.save(any()) } answers { savedNote = firstArg() }

        viewModel.updateChartCell(
            blockId = "chart-data",
            rowIndex = 1,
            columnIndex = 1,
            value = "125"
        )
        assertTrue(viewModel.addChartColumn("chart-data"))
        viewModel.updateChartCell("chart-data", rowIndex = 0, columnIndex = 2, value = "Tax")
        viewModel.updateChart("chart-data", title = "Updated sales", selectedColumnId = "c_column_3")

        advanceTimeBy(2_001)
        advanceUntilIdle()

        coVerify { noteRepository.save(any()) }
        val persisted = savedNote ?: error("The chart note was not persisted")
        val persistedChart = NoteDocument.fromContent(persisted.content)
            .blocks.single() as EditorBlock.ChartBlock
        assertEquals("125", cellText(persistedChart.rows[1][1]))
        assertEquals("Tax", cellText(persistedChart.rows[0][2]))
        assertEquals("c_column_3", persistedChart.selectedColumnId)
        assertEquals("Updated sales", persistedChart.title)

        coEvery { noteRepository.getNoteById("chart-note") } returns persisted
        val reloaded = newViewModel()
        reloaded.load("chart-note")
        advanceUntilIdle()

        val restored = reloaded.uiState.value.document.blocks.filterIsInstance<EditorBlock.ChartBlock>().single()
        assertEquals("c_column_3", restored.selectedColumnId)
        assertEquals("125", cellText(restored.rows[1][1]))
        assertEquals("Tax", cellText(restored.rows[0][2]))
        assertEquals("Updated sales", restored.title)
    }

    @Test
    fun testChartTableActionDispatcherCoversRowAndColumnOperations() {
        val chartId = chartBlock().id

        viewModel.onChartTableAction(ChartTableAction.InsertColumnLeft(chartId, 1))
        assertEquals(3, currentChart().columnIds.size)
        viewModel.onChartTableAction(ChartTableAction.InsertColumnRight(chartId, 1))
        assertEquals(4, currentChart().columnIds.size)
        viewModel.onChartTableAction(ChartTableAction.ClearColumn(chartId, 2))
        assertTrue(currentChart().rows.all { row -> cellText(row[2]).isEmpty() })
        viewModel.onChartTableAction(ChartTableAction.DeleteColumn(chartId, 1))
        assertEquals(3, currentChart().columnIds.size)

        viewModel.onChartTableAction(ChartTableAction.InsertRowAbove(chartId, 0))
        viewModel.onChartTableAction(ChartTableAction.InsertRowBelow(chartId, 1))
        assertEquals(4, currentChart().rows.size)
        viewModel.onChartTableAction(ChartTableAction.ClearRow(chartId, 1))
        assertTrue(currentChart().rows[1].all { cellText(it).isEmpty() })
        viewModel.onChartTableAction(ChartTableAction.DeleteRow(chartId, 1))
        assertEquals(3, currentChart().rows.size)
    }

    @Test
    fun testInvalidChartSelectionAndCellCoordinatesPreserveChartData() {
        val before = currentChart().normalized()

        viewModel.updateChart("chart-data", selectedColumnId = "missing-column")
        assertEquals(before.selectedColumnId, currentChart().selectedColumnId)

        viewModel.updateChartCell(
            blockId = "chart-data",
            rowIndex = 99,
            columnIndex = 99,
            value = "ignored"
        )
        assertEquals(before.rows, currentChart().rows)

        viewModel.updateChartCell(
            blockId = "chart-data",
            rowIndex = 1,
            columnIndex = 1,
            value = "line\nbreak"
        )
        assertEquals("line break", cellText(currentChart().rows[1][1]))

        val beforeNonChart = viewModel.uiState.value.document
        viewModel.updateChart("missing-block", title = "ignored")
        viewModel.updateChartCell("missing-block", 0, 0, "ignored")
        assertEquals(beforeNonChart, viewModel.uiState.value.document)
    }

    private fun newViewModel(): NoteEditorViewModel {
        val summarizeNoteUseCase = mockk<SummarizeNoteUseCase>(relaxed = true)
        coEvery { summarizeNoteUseCase(any(), any()) } returns NoteSummaryResult.Empty
        return NoteEditorViewModel(
            noteRepository = noteRepository,
            folderRepository = folderRepository,
            summarizeNoteUseCase = summarizeNoteUseCase,
            categorizeNoteUseCase = mockk<CategorizeNoteUseCase>(relaxed = true),
            deleteVoiceNoteAudioUseCase = mockk<DeleteVoiceNoteAudioUseCase>(relaxed = true),
            deleteVoiceNoteBlockUseCase = mockk<DeleteVoiceNoteBlockUseCase>(relaxed = true)
        )
    }

    private fun currentChart(): EditorBlock.ChartBlock =
        viewModel.uiState.value.document.blocks.single() as EditorBlock.ChartBlock

    private fun chartBlock(): EditorBlock.ChartBlock = EditorBlock.ChartBlock(
        id = "chart-data",
        chartType = ChartType.BAR,
        title = "Sales",
        columnIds = listOf("c_category", "c_value"),
        selectedColumnId = "c_value",
        rows = listOf(
            row("Category", "Revenue"),
            row("Jan", "100")
        )
    )

    private fun row(vararg values: String): List<List<RichText>> = values.map { value -> listOf(RichText(value)) }

    private fun cellText(cell: List<RichText>): String = cell.joinToString("") { it.text }
}
