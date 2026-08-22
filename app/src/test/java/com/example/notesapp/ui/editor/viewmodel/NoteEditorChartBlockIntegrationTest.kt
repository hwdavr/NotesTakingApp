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
import com.example.notesapp.ui.editor.mapper.RichText
import io.mockk.mockk
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NoteEditorChartBlockIntegrationTest : BaseViewModelTest() {
    private lateinit var viewModel: NoteEditorViewModel

    @Before
    fun setup() {
        viewModel = NoteEditorViewModel(
            noteRepository = mockk<NoteRepository>(relaxed = true),
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
    fun testConvertTableInPlacePreservesRowsAndOrder() {
        val table = EditorBlock.TableBlock(
            id = "table-1",
            rows = listOf(
                row("Category", "Revenue"),
                row("Jan", "10")
            )
        )
        viewModel.uiStateInternal.value = NoteEditorUiState(
            isLoaded = true,
            document = com.example.notesapp.ui.editor.mapper.NoteDocument(
                blocks = listOf(EditorBlock.TextBlock(id = "before"), table, EditorBlock.TextBlock(id = "after"))
            ),
            focusedBlockId = table.id
        )

        assertTrue(viewModel.convertTableToChart(table.id, ChartType.LINE))
        val blocks = viewModel.uiState.value.document.blocks
        val chart = blocks[1] as EditorBlock.ChartBlock
        assertEquals("before", blocks[0].id)
        assertEquals("after", blocks[2].id)
        assertEquals(ChartType.LINE, chart.chartType)
        assertEquals("10", chart.rows[1][1].joinToString("") { it.text })
        assertEquals("c_column_2", chart.selectedColumnId)
    }

    private fun row(category: String, value: String): List<List<RichText>> =
        listOf(listOf(RichText(category)), listOf(RichText(value)))

    private fun BasicBlockType.toChartType(): ChartType = when (this) {
        BasicBlockType.BAR_CHART -> ChartType.BAR
        BasicBlockType.LINE_CHART -> ChartType.LINE
        BasicBlockType.PIE_CHART -> ChartType.PIE
        else -> error("Not a chart type")
    }
}
