package com.example.notesapp.ui.editor.screen

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FullscreenDiagramViewerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testOpenFullscreenViewerAndNavigateBack() {
        var dismissed = false
        val block = EditorBlock.MermaidBlock(
            id = "fs1",
            code = "graph TD\n  A-->B",
            title = "Fullscreen Flowchart"
        )

        composeTestRule.setContent {
            NotesTakingAppTheme {
                FullscreenDiagramViewerContent(
                    block = block,
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("fullscreen_diagram_top_bar")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("fullscreen_diagram_canvas")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("fullscreen_top_back_btn")
            .performClick()

        assertTrue(dismissed)
    }

    @Test
    fun testZoomControlsAndUpdateScale() {
        val block = EditorBlock.MermaidBlock(
            id = "fs2",
            code = "graph TD\n  A-->B",
            title = "Zoom Flowchart"
        )

        composeTestRule.setContent {
            NotesTakingAppTheme {
                FullscreenDiagramViewerContent(
                    block = block,
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("fullscreen_zoom_controls")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("fullscreen_zoom_in_btn")
            .performClick()

        composeTestRule.onNodeWithTag("fullscreen_zoom_controls")
            .assertTextContains("125%")

        composeTestRule.onNodeWithTag("fullscreen_fit_to_screen_btn")
            .performClick()

        composeTestRule.onNodeWithTag("fullscreen_zoom_controls")
            .assertTextContains("100%")
    }

    @Test
    fun testCopyCodeToClipboard() {
        val testCode = "graph TD\n  A[Start] --> B[Result]"
        val block = EditorBlock.MermaidBlock(
            id = "fs3",
            code = testCode,
            title = "Clipboard Test"
        )

        composeTestRule.setContent {
            NotesTakingAppTheme {
                FullscreenDiagramViewerContent(
                    block = block,
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("fullscreen_copy_code_btn")
            .performClick()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()

        assertEquals(testCode, clipText)
    }
}
