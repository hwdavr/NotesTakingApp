package com.example.notesapp.ui.editor.screen

import androidx.compose.ui.unit.dp
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TableLayoutTest {
    @Test
    fun fitToWidthUsesEqualColumnWeights() {
        val table = tableBlock(fitToWidth = true)

        assertEquals(listOf(1f, 1f, 1f), table.tableColumnWeights())
    }

    @Test
    fun defaultSizingReflectsContentAndRestoresAfterFitToggle() {
        val defaultTable = tableBlock(fitToWidth = false)
        val defaultWeights = defaultTable.tableColumnWeights()
        val fitWeights = defaultTable.copy(fitToWidth = true).tableColumnWeights()
        val restoredWeights = defaultTable.copy(fitToWidth = false).tableColumnWeights()

        assertNotEquals(defaultWeights[0], defaultWeights[1])
        assertEquals(listOf(1f, 1f, 1f), fitWeights)
        assertEquals(defaultWeights, restoredWeights)
    }

    @Test
    fun comfortableColumnsKeepViewportWidth() {
        val widths = tableColumnWidths(listOf(1f, 1f), 320.dp)

        assertEquals(2, widths.size)
        assertEquals(320.dp, widths[0] + widths[1])
        assertTrue(widths.all { it >= TableColumnMinWidth })
    }

    @Test
    fun narrowColumnsExpandToMinimumAndOverflowViewport() {
        val viewport = 320.dp
        val widths = tableColumnWidths(List(8) { 1f }, viewport)

        assertTrue(widths.all { it >= TableColumnMinWidth })
        val totalWidth = widths.fold(0.dp) { total, width -> total + width }
        assertTrue(totalWidth > viewport)
    }

    @Test
    fun emptyWeightsProduceEmptyWidths() {
        assertTrue(tableColumnWidths(emptyList(), 320.dp).isEmpty())
    }

    private fun tableBlock(fitToWidth: Boolean): EditorBlock.TableBlock = EditorBlock.TableBlock(
        id = "layout-table",
        fitToWidth = fitToWidth,
        rows = listOf(
            listOf(
                listOf(RichText("Very long first column content")),
                listOf(RichText("B")),
                listOf(RichText("C"))
            ),
            listOf(
                listOf(RichText("Another long value")),
                listOf(RichText("D")),
                listOf(RichText("E"))
            )
        )
    )
}
