package com.example.notesapp.ui.editor.screen

import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
