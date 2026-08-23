package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.folder.usecase.CategorizeNoteUseCase
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.summary.usecase.SummarizeNoteUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteAudioUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteBlockUseCase
import com.example.notesapp.ui.editor.mapper.BasicBlockType
import com.example.notesapp.ui.editor.mapper.ChartType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.model.ChartTableAction
import com.example.notesapp.ui.editor.model.TableHandleAction
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NoteEditorChartBlockIntegrationTest : BaseViewModelTest() {
    private lateinit var noteRepository: NoteRepository
    private lateinit var viewModel: NoteEditorViewModel

    @Before
    fun setup() {
        noteRepository = mockk(relaxed = true)
        viewModel = NoteEditorViewModel(
            noteRepository = noteRepository,
            folderRepository = mockk<FolderRepository>(relaxed = true),
            summarizeNoteUseCase = mockk<SummarizeNoteUseCase>(relaxed = true),
            categorizeNoteUseCase = mockk<CategorizeNoteUseCase>(relaxed = true),
            deleteVoiceNoteAudioUseCase = mockk<DeleteVoiceNoteAudioUseCase>(relaxed = true),
            deleteVoiceNoteBlockUseCase = mockk<DeleteVoiceNoteBlockUseCase>(relaxed = true)
        )
        viewModel.uiStateInternal.value = NoteEditorUiState(isLoaded = true)
    }

    @Test
    fun testInsertEachChartTypeUsesDefaultsAndFocusInsertion() = runTest {
        BasicBlockType.entries.filter {
            it in setOf(BasicBlockType.BAR_CHART, BasicBlockType.LINE_CHART, BasicBlockType.PIE_CHART)
        }
            .forEach { type ->
                assertTrue(viewModel.insertBasicBlock(type))
                val chart = viewModel.uiState.value.document.blocks.last() as EditorBlock.ChartBlock
                assertEquals(type.toChartType(), chart.chartType)
                assertEquals(listOf("c_category", "c_value"), chart.columnIds)
                assertEquals("c_value", chart.selectedColumnId)
            }
        advanceTimeBy(2_001)
    }

    @Test
    fun testFocusedInsertionClosesPanelAndPlacesChartAfterFocusedBlock() {
        val focused = EditorBlock.TextBlock(id = "focused", children = listOf(RichText("Focus")))
        val trailing = EditorBlock.TextBlock(id = "trailing", children = listOf(RichText("After")))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            isLoaded = true,
            showBasicBlocksPanel = true,
            focusedBlockId = focused.id,
            document = NoteDocument(blocks = listOf(focused, trailing))
        )

        assertTrue(viewModel.insertBasicBlock(BasicBlockType.LINE_CHART))

        val blocks = viewModel.uiState.value.document.blocks
        assertEquals(listOf("focused", "trailing"), listOf(blocks[0].id, blocks[2].id))
        assertTrue(blocks[1] is EditorBlock.ChartBlock)
        assertEquals(blocks[1].id, viewModel.uiState.value.focusedBlockId)
        assertFalse(viewModel.uiState.value.showBasicBlocksPanel)
    }

    @Test
    fun testTableOptionsConversionPreservesCompleteRowsColumnsAndOrder() {
        val table = EditorBlock.TableBlock(
            id = "table-1",
            rows = listOf(
                row("Category", "Revenue", "Cost"),
                row("Jan", "10", "8"),
                row("Feb", "12", "9")
            )
        )
        viewModel.uiStateInternal.value = NoteEditorUiState(
            isLoaded = true,
            document = NoteDocument(
                blocks = listOf(EditorBlock.TextBlock(id = "before"), table, EditorBlock.TextBlock(id = "after"))
            ),
            focusedBlockId = table.id
        )

        viewModel.onTableAction(TableHandleAction.ConvertToChart(table.id, ChartType.LINE))
        val blocks = viewModel.uiState.value.document.blocks
        val chart = blocks[1] as EditorBlock.ChartBlock
        assertEquals("before", blocks[0].id)
        assertEquals("after", blocks[2].id)
        assertEquals(ChartType.LINE, chart.chartType)
        assertEquals("10", chart.rows[1][1].joinToString("") { it.text })
        assertEquals("8", chart.rows[1][2].joinToString("") { it.text })
        assertEquals("12", chart.rows[2][1].joinToString("") { it.text })
        assertEquals("9", chart.rows[2][2].joinToString("") { it.text })
        assertEquals(listOf("c_category", "c_column_2", "c_column_3"), chart.columnIds)
        assertEquals("c_column_2", chart.selectedColumnId)
    }

    @Test
    fun testInsertionAndConversionAutoSaveCompleteChartState() = runTest {
        val focused = EditorBlock.TextBlock(id = "focused", children = listOf(RichText("Focus")))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "chart-save-note",
            title = "Chart note",
            document = NoteDocument(blocks = listOf(focused)),
            focusedBlockId = focused.id,
            isLoaded = true
        )

        assertTrue(viewModel.insertBasicBlock(BasicBlockType.BAR_CHART))
        advanceTimeBy(2_001)
        advanceUntilIdle()
        coVerify {
            noteRepository.save(
                match { note ->
                    val savedChart = NoteDocument.fromContent(note.content).blocks
                        .filterIsInstance<EditorBlock.ChartBlock>()
                        .single()
                    savedChart.id == viewModel.uiState.value.focusedBlockId &&
                        savedChart.chartType == ChartType.BAR &&
                        savedChart.title == "Chart"
                }
            )
        }

        val table = EditorBlock.TableBlock(
            id = "table-save",
            rows = listOf(row("Category", "Revenue"), row("January", "120"))
        )
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "chart-save-note",
            title = "Converted chart",
            document = NoteDocument(blocks = listOf(table)),
            isLoaded = true
        )
        viewModel.onTableAction(TableHandleAction.ConvertToChart(table.id, ChartType.LINE))
        advanceTimeBy(2_001)
        advanceUntilIdle()

        coVerify {
            noteRepository.save(
                match { note ->
                    val savedChart = NoteDocument.fromContent(note.content).blocks
                        .filterIsInstance<EditorBlock.ChartBlock>()
                        .single()
                    note.title == "Converted chart" &&
                        savedChart.chartType == ChartType.LINE &&
                        savedChart.rows[1][1].single().text == "120"
                }
            )
        }
    }

    @Test
    fun testReadOnlyChartAndTableGuardsDoNotMutateState() {
        val chart = EditorBlock.ChartBlock(
            id = "read-only-chart",
            rows = listOf(row("Category", "Value"), row("Jan", "10")),
            columnIds = listOf("category", "value"),
            selectedColumnId = "value"
        )
        val table = EditorBlock.TableBlock(id = "read-only-table", rows = listOf(row("A", "B")))
        viewModel.uiStateInternal.value = NoteEditorUiState(
            isLoaded = true,
            isEditable = false,
            document = NoteDocument(blocks = listOf(chart, table))
        )
        val before = viewModel.uiState.value.document

        assertFalse(viewModel.insertBasicBlock(BasicBlockType.BAR_CHART))
        assertFalse(viewModel.addChartRow(chart.id))
        assertFalse(viewModel.addChartColumn(chart.id))
        viewModel.updateChart(chart.id, title = "Blocked")
        viewModel.updateChartCell(chart.id, rowIndex = 1, columnIndex = 1, value = "99")
        viewModel.onChartTableAction(ChartTableAction.InsertRowBelow(chart.id, 0))
        assertFalse(viewModel.convertTableToChart(table.id, ChartType.BAR))

        assertEquals(before, viewModel.uiState.value.document)
    }

    private fun row(vararg values: String): List<List<RichText>> {
        return values.map { value -> listOf(RichText(value)) }
    }

    private fun BasicBlockType.toChartType(): ChartType = when (this) {
        BasicBlockType.BAR_CHART -> ChartType.BAR
        BasicBlockType.LINE_CHART -> ChartType.LINE
        BasicBlockType.PIE_CHART -> ChartType.PIE
        else -> error("Not a chart type")
    }
}
