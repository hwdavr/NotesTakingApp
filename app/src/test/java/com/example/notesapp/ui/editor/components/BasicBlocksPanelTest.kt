package com.example.notesapp.ui.editor.components

import com.example.notesapp.ui.editor.mapper.BasicBlockType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BasicBlocksPanelTest {

    @Test
    fun approvedTilesContainsExactlyElevenBasicBlockTypesInReadingOrder() {
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
            "basic_blocks_mermaid"
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
            BasicBlockType.MERMAID
        )

        assertEquals(12, approvedBasicBlockTiles.size)
        assertEquals(expectedTags, approvedBasicBlockTiles.map { it.testTag })
        assertEquals(expectedTypes, approvedBasicBlockTiles.map { it.type })
    }

    @Test
    fun pageBlockTypeIsExcludedFromCatalog() {
        assertTrue(approvedBasicBlockTiles.none { it.type == BasicBlockType.UNKNOWN })
    }
}
