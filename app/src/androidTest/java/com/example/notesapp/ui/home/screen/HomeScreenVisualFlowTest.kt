package com.example.notesapp.ui.home.screen

import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.home.model.HomeUiState
import com.example.notesapp.ui.notes.model.NoteUiModel
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import java.io.File
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenVisualFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun capturesFixedHeightHomeCards() {
        val longNote = note("visual-long", "Tricky Technical Experience")
        val shortNote = note("visual-short", "Another note")
        val state = HomeUiState(
            recentNotes = listOf(
                NoteUiModel(
                    id = longNote.id,
                    title = longNote.title,
                    preview = listOf(
                        "# Architect",
                        "Clean architect",
                        "kotlin flow",
                        "Split the projects into 3 modules",
                        "Additional content that must be clipped"
                    ).joinToString("\n"),
                    colorIndex = 2
                ),
                NoteUiModel(
                    id = shortNote.id,
                    title = shortNote.title,
                    preview = "Meeting with the team tomorrow\nPrepare the demo environment",
                    colorIndex = 2
                )
            ),
            noteActions = mapOf(longNote.id to longNote, shortNote.id to shortNote)
        )

        composeRule.setContent {
            NotesTakingAppTheme {
                HomeNotesScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    state = state,
                    onAddNote = {},
                    onRecordNote = {},
                    onOpenNote = {},
                    onSelectFolder = {}
                )
            }
        }
        composeRule.waitUntil(5000) {
            composeRule.onAllNodesWithTag("home_note_card", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.waitForIdle()
        val firstCardHeight = composeRule
            .onAllNodesWithTag("home_note_card", useUnmergedTree = true)
            .fetchSemanticsNodes()
            .first()
            .boundsInRoot
            .height
        val firstCardFrame = composeRule
            .onAllNodesWithTag("home_note_card", useUnmergedTree = true)
            .fetchSemanticsNodes()
            .first()
            .boundsInRoot
        val firstMoreActionFrame = composeRule
            .onAllNodesWithTag("home_note_more_actions", useUnmergedTree = true)
            .fetchSemanticsNodes()
            .first()
            .boundsInRoot
        saveActiveWindowCapture("home_fixed_card_initial")
        composeRule.onNodeWithTag("home_notes_list").performScrollToIndex(1)
        composeRule.waitUntil(5000) {
            composeRule.onAllNodesWithTag("home_note_card", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.waitForIdle()

        val secondCardHeight = composeRule
            .onAllNodesWithTag("home_note_card", useUnmergedTree = true)
            .fetchSemanticsNodes()
            .last()
            .boundsInRoot
            .height
        val secondCardFrame = composeRule
            .onAllNodesWithTag("home_note_card", useUnmergedTree = true)
            .fetchSemanticsNodes()
            .last()
            .boundsInRoot
        val secondMoreActionFrame = composeRule
            .onAllNodesWithTag("home_note_more_actions", useUnmergedTree = true)
            .fetchSemanticsNodes()
            .last()
            .boundsInRoot
        val expectedHeight = 220f * composeRule.density.density
        assertTrue(abs(firstCardHeight - expectedHeight) <= 1f)
        assertTrue(abs(secondCardHeight - expectedHeight) <= 1f)

        saveActiveWindowCapture("home_fixed_card_scrolled")
        saveRuntimeEvidence(
            firstCardFrame,
            secondCardFrame,
            firstMoreActionFrame,
            secondMoreActionFrame
        )
    }

    private fun saveActiveWindowCapture(fileName: String) {
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            ?: error("Could not capture visual evidence: $fileName")
        try {
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
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .executeShellCommand("cp ${screenshot.absolutePath} /sdcard/Download/$fileName.png")
                .use { }
        } finally {
            bitmap.recycle()
        }
    }

    private fun saveRuntimeEvidence(
        firstFrame: Rect,
        secondFrame: Rect,
        firstMoreActionFrame: Rect,
        secondMoreActionFrame: Rect
    ) {
        val directory = InstrumentationRegistry.getInstrumentation().targetContext
            .getExternalFilesDir("visual_evidence")
            ?: error("External files directory unavailable")
        directory.mkdirs()
        val density = composeRule.density.density
        val evidence = """
            {
              "version": 1,
              "producer": { "kind": "ComposeUiTest", "test_name": "HomeScreenVisualFlowTest#capturesFixedHeightHomeCards" },
              "coordinate_space": { "unit": "dp" },
              "normalization": { "theme": "light", "font_scale": 1.0, "locale": "en-US" },
              "screens": [
                {
                  "name": "home_fixed_card_initial",
                  "screenshot": "evidence/home_fixed_card_initial.png",
                  "elements": {
                    "home_note_card": ${frameJson(firstFrame, density)},
                    "home_note_more_actions": ${frameJson(firstMoreActionFrame, density)}
                  }
                },
                {
                  "name": "home_fixed_card_scrolled",
                  "screenshot": "evidence/home_fixed_card_scrolled.png",
                  "elements": {
                    "home_note_card": ${frameJson(secondFrame, density)},
                    "home_note_more_actions": ${frameJson(secondMoreActionFrame, density)}
                  }
                }
              ]
            }
        """.trimIndent()
        val evidenceFile = File(directory, "home_fixed_card_runtime_evidence.json")
        evidenceFile.writeText(evidence)
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand(
                "cp ${evidenceFile.absolutePath} /sdcard/Download/home_fixed_card_runtime_evidence.json"
            )
            .use { }
    }

    private fun frameJson(frame: Rect, density: Float): String = buildString {
        append("{\"x\":${frame.left / density}")
        append(",\"y\":${frame.top / density}")
        append(",\"width\":${frame.width / density}")
        append(",\"height\":${frame.height / density}}")
    }

    private fun note(id: String, title: String): Note = Note(
        id = id,
        title = title,
        content = "Visual fixture",
        createdAt = 0,
        updatedAt = 0
    )
}
