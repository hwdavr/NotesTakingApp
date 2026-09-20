package com.example.notesapp.ui.editor.screen

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FullscreenDiagramViewerTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

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
    fun mermaidViewer_keepsChartBehindTitleBar() {
        val block = EditorBlock.MermaidBlock(
            id = "fs-title-bar",
            code = "graph TD\n  A[Start] --> B{Decision}\n  B -->|Yes| C[Result 1]\n  B -->|No| D[Result 2]",
            title = "Mermaid Diagram"
        )

        composeTestRule.setContent {
            NotesTakingAppTheme {
                FullscreenDiagramViewerDialog(
                    block = block,
                    onDismiss = {},
                    modifier = Modifier.testTag("fullscreen_diagram_root")
                )
            }
        }

        composeTestRule.onNodeWithTag("fullscreen_diagram_top_bar")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("fullscreen_diagram_canvas")
            .assertIsDisplayed()
        composeTestRule.waitForIdle()

        val rootNode = composeTestRule.onNodeWithTag("fullscreen_diagram_root")
        val titleBarHeight = composeTestRule.onNodeWithTag("fullscreen_diagram_top_bar")
            .captureToImage()
            .height
        val beforePan = rootNode.captureToImage().asAndroidBitmap()

        composeTestRule.onNodeWithTag("fullscreen_diagram_canvas").performTouchInput {
            swipeUp()
        }
        composeTestRule.waitForIdle()
        val afterPan = rootNode.captureToImage().asAndroidBitmap()

        assertEquals(
            "Panning the Mermaid chart must not change title-bar pixels",
            0,
            differingPixelCount(beforePan, afterPan, titleBarHeight)
        )
    }

    private fun differingPixelCount(first: Bitmap, second: Bitmap, top: Int): Int {
        val width = minOf(first.width, second.width)
        val bottom = minOf(top, first.height, second.height)
        var differences = 0
        for (y in 0 until bottom) {
            for (x in 0 until width) {
                if (first.getPixel(x, y) != second.getPixel(x, y)) {
                    differences++
                }
            }
        }
        return differences
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
