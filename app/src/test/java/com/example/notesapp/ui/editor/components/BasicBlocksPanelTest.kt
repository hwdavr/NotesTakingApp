package com.example.notesapp.ui.editor.components

import com.example.notesapp.ui.editor.mapper.BasicBlockType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BasicBlocksPanelTest {

    @Test
    fun approvedTilesContainsChartBlockTypesInReadingOrder() {
        val expectedTags = listOf(
            "basic_blocks_text",
            "basic_blocks_heading_1",
            "basic_blocks_heading_2",
            "basic_blocks_heading_3",
            "basic_blocks_heading_4",
            "basic_blocks_bulleted_list",
            "basic_blocks_numbered_list",
            "basic_blocks_todo_list",
            "basic_blocks_toggle_list",
            "basic_blocks_callout",
            "basic_blocks_quote",
            "basic_blocks_code",
            "basic_blocks_mermaid",
            "basic_blocks_bar_chart",
            "basic_blocks_line_chart",
            "basic_blocks_pie_chart"
        )
        val expectedTypes = listOf(
            BasicBlockType.PARAGRAPH,
            BasicBlockType.HEADING_1,
            BasicBlockType.HEADING_2,
            BasicBlockType.HEADING_3,
            BasicBlockType.HEADING_4,
            BasicBlockType.BULLETED_LIST,
            BasicBlockType.NUMBERED_LIST,
            BasicBlockType.TODO_LIST,
            BasicBlockType.TOGGLE_LIST,
            BasicBlockType.CALLOUT,
            BasicBlockType.QUOTE,
            BasicBlockType.CODE,
            BasicBlockType.MERMAID,
            BasicBlockType.BAR_CHART,
            BasicBlockType.LINE_CHART,
            BasicBlockType.PIE_CHART
        )

        assertEquals(16, approvedBasicBlockTiles.size)
        assertEquals(expectedTags, approvedBasicBlockTiles.map { it.testTag })
        assertEquals(expectedTypes, approvedBasicBlockTiles.map { it.type })
    }

    @Test
    fun testBasicAndAdvancedSectionHeadersAndCodeTile() {
        val basicSection = basicBlocksSections.first { it.testTag == "basic_blocks_section_basic" }
        val advancedSection = basicBlocksSections.first { it.testTag == "basic_blocks_section_advanced" }

        assertEquals(11, basicSection.tiles.size)
        assertTrue(basicSection.tiles.none { it.type == BasicBlockType.CODE })
        assertTrue(basicSection.tiles.none { it.type == BasicBlockType.MERMAID })

        assertEquals(
            listOf(
                BasicBlockType.CODE,
                BasicBlockType.MERMAID,
                BasicBlockType.BAR_CHART,
                BasicBlockType.LINE_CHART,
                BasicBlockType.PIE_CHART
            ),
            advancedSection.tiles.map { it.type }
        )
        assertTrue(advancedSection.tiles.any { it.testTag == "basic_blocks_code" })
    }

    @Test
    fun pageBlockTypeIsExcludedFromCatalog() {
        assertTrue(approvedBasicBlockTiles.none { it.type == BasicBlockType.UNKNOWN })
    }
}
