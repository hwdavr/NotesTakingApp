package com.example.notesapp.ui.editor.screen

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.ui.editor.components.MermaidBlockCard
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MermaidVisualFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun captureMermaidCardPreviewState() {
        val block = EditorBlock.MermaidBlock(
            id = "m1",
            code = "graph TD\n  A[Start] --> B[Process]\n  B --> C[End]",
            title = "System Architecture"
        )

        composeRule.setContent {
            NotesTakingAppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    MermaidBlockCard(
                        block = block,
                        isEditable = true,
                        onUpdateTitle = {},
                        onUpdateCode = {}
                    )
                }
            }
        }

        composeRule.onNodeWithTag("editor_mermaid_preview_canvas_m1")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("editor_mermaid_toggle_mode_m1")
            .assertIsDisplayed()

        captureVisualEvidence("mermaid_card_preview")
    }

    @Test
    fun captureMermaidCardCodeEditorState() {
        val block = EditorBlock.MermaidBlock(
            id = "m2",
            code = "graph TD\n  A[Client] --> B[Server]",
            title = "Workflow Diagram"
        )

        composeRule.setContent {
            NotesTakingAppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    MermaidBlockCard(
                        block = block,
                        isEditable = true,
                        onUpdateTitle = {},
                        onUpdateCode = {}
                    )
                }
            }
        }

        composeRule.onNodeWithTag("editor_mermaid_toggle_mode_m2")
            .performClick()

        composeRule.onNodeWithTag("editor_mermaid_code_editor_m2")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("editor_mermaid_template_chip_flowchart")
            .assertIsDisplayed()

        captureVisualEvidence("mermaid_card_code_editor")
    }

    @Test
    fun captureFullscreenDiagramViewerState() {
        val block = EditorBlock.MermaidBlock(
            id = "fs1",
            code = "graph TD\n  A[Client] --> B[API Gateway]\n  B --> C[Auth Service]\n  B --> D[Data Service]",
            title = "Architecture Diagram"
        )

        composeRule.setContent {
            NotesTakingAppTheme {
                FullscreenDiagramViewerContent(
                    block = block,
                    onDismiss = {}
                )
            }
        }

        composeRule.onNodeWithTag("fullscreen_diagram_top_bar")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("fullscreen_diagram_canvas")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("fullscreen_zoom_controls")
            .assertIsDisplayed()

        captureVisualEvidence("mermaid_fullscreen_viewer")
    }

    private fun captureVisualEvidence(fileName: String) {
        composeRule.waitForIdle()
        Thread.sleep(1500) // Allow WebView to finish loading and rendering SVG
        composeRule.waitForIdle()
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            ?: error("Could not capture visual evidence: $fileName")
        val directory = InstrumentationRegistry.getInstrumentation().targetContext
            .getExternalFilesDir("visual_evidence")
            ?: error("External files directory unavailable")
        directory.mkdirs()
        val screenshot = File(directory, "$fileName.png")
        screenshot.outputStream().use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                "Could not write visual evidence: $fileName"
            }
        }
        val pfd = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("cp ${screenshot.absolutePath} /sdcard/Download/$fileName.png")
        pfd.use {
            java.io.FileInputStream(it.fileDescriptor).use { stream -> stream.readBytes() }
        }
    }
}
