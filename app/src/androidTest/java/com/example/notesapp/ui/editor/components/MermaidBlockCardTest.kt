package com.example.notesapp.ui.editor.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MermaidBlockCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testMermaidCardDefaultsToPreviewMode() {
        val block = EditorBlock.MermaidBlock(
            id = "m1",
            code = "graph TD\n A-->B",
            title = "Test Flowchart"
        )

        composeTestRule.setContent {
            NotesTakingAppTheme {
                MermaidBlockCard(
                    block = block,
                    isEditable = true,
                    onUpdateTitle = {},
                    onUpdateCode = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("editor_mermaid_preview_canvas_m1")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("editor_mermaid_toggle_mode_m1")
            .assertIsDisplayed()
            .assertTextContains("Edit Code")
    }

    @Test
    fun testToggleBetweenPreviewAndCodeEditor() {
        val block = EditorBlock.MermaidBlock(
            id = "m2",
            code = "graph TD\n A-->B",
            title = "Toggle Test"
        )

        composeTestRule.setContent {
            NotesTakingAppTheme {
                MermaidBlockCard(
                    block = block,
                    isEditable = true,
                    onUpdateTitle = {},
                    onUpdateCode = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("editor_mermaid_toggle_mode_m2")
            .performClick()

        composeTestRule.onNodeWithTag("editor_mermaid_code_editor_m2")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("editor_mermaid_toggle_mode_m2")
            .performClick()

        composeTestRule.onNodeWithTag("editor_mermaid_preview_canvas_m2")
            .assertIsDisplayed()
    }

    @Test
    fun testTemplateChipInsertion() {
        var updatedCode = ""
        val block = EditorBlock.MermaidBlock(
            id = "m3",
            code = "graph TD\n A-->B",
            title = "Template Test"
        )

        composeTestRule.setContent {
            NotesTakingAppTheme {
                MermaidBlockCard(
                    block = block,
                    isEditable = true,
                    onUpdateTitle = {},
                    onUpdateCode = { updatedCode = it }
                )
            }
        }

        composeTestRule.onNodeWithTag("editor_mermaid_toggle_mode_m3")
            .performClick()

        composeTestRule.onNodeWithTag("editor_mermaid_template_chip_sequence")
            .performClick()

        assertTrue(updatedCode.contains("sequenceDiagram"))
    }

    @Test
    fun testPinchZoomWithinCard() {
        val block = EditorBlock.MermaidBlock(
            id = "m4",
            code = "graph TD\n A-->B",
            title = "Zoom Test"
        )

        composeTestRule.setContent {
            NotesTakingAppTheme {
                MermaidBlockCard(
                    block = block,
                    isEditable = true,
                    onUpdateTitle = {},
                    onUpdateCode = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("editor_mermaid_preview_canvas_m4")
            .assertIsDisplayed()
            .performTouchInput {
                pinch(
                    start0 = Offset(100f, 100f),
                    end0 = Offset(20f, 20f),
                    start1 = Offset(200f, 200f),
                    end1 = Offset(300f, 300f)
                )
            }
    }

    @Test
    fun testReadOnlyHidesEditControls() {
        val block = EditorBlock.MermaidBlock(
            id = "m5",
            code = "graph TD\n A-->B",
            title = "ReadOnly Test"
        )

        composeTestRule.setContent {
            NotesTakingAppTheme {
                MermaidBlockCard(
                    block = block,
                    isEditable = false,
                    onUpdateTitle = {},
                    onUpdateCode = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("editor_mermaid_preview_canvas_m5")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("editor_mermaid_toggle_mode_m5")
            .assertDoesNotExist()
    }

    @Test
    fun testEmptyDiagramShowsPlaceholder() {
        val block = EditorBlock.MermaidBlock(
            id = "m6",
            code = "",
            title = "Empty Test"
        )

        composeTestRule.setContent {
            NotesTakingAppTheme {
                MermaidBlockCard(
                    block = block,
                    isEditable = true,
                    onUpdateTitle = {},
                    onUpdateCode = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Tap Edit Code to create a diagram")
            .assertIsDisplayed()
    }
}
