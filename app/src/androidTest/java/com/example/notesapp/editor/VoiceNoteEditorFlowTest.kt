package com.example.notesapp.editor

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.screen.NoteEditorScreenContent
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoiceNoteEditorFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var audioFile: File? = null

    @After
    fun tearDown() {
        audioFile?.delete()
    }

    @Test
    fun savedDocumentRoundTripPlaysAndSeeksInlineVoicePlayer() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        audioFile = File(context.filesDir, "voice-notes/editor-flow.m4a").apply {
            parentFile?.mkdirs()
            createNewFile()
        }
        val block = EditorBlock.Voice(
            blockId = "voice-flow",
            audioFilePath = audioFile?.absolutePath,
            audioFormat = AudioFormat.AAC,
            durationMs = 272_000L,
            fileSizeBytes = 2_400_000L,
            sampleRateHertz = 44_100,
            channels = 1,
            createdAt = 1L,
            updatedAt = 1L
        )
        var deletedBlockId: String? = null

        val savedDocument = NoteDocument(
            blocks = listOf(
                block,
                EditorBlock.TextBlock(
                    id = "transcript-flow",
                    children = listOf(RichText("Transcript remains editable"))
                )
            )
        )
        val document = NoteDocument.fromContent(savedDocument.toJsonString())
        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note-flow",
                state = NoteEditorUiState(
                    noteId = "note-flow",
                    title = "Voice note",
                    document = document,
                    isLoaded = true
                ),
                onBack = {},
                onShareRequested = {},
                onDelete = {},
                onTitleChange = {},
                onRename = {},
                onToggleFavorite = {},
                onMoveNote = {},
                onExportNote = {},
                onOpenVoiceRecorder = { _, _ -> },
                onTextBlockChange = { _, _ -> },
                onToggleCheckbox = {},
                onToggleCheckboxChecked = {},
                onToggleMark = { _, _ -> },
                onAddParagraph = {},
                onAddImage = {},
                onEmojiSelected = {},
                onImageChange = { _, _, _ -> },
                onAddTable = {},
                onTableCellChange = { _, _, _, _ -> },
                onFolderSelected = {},
                onToggleFormattingToolbar = {},
                onBlockFocused = {},
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {},
                onDeleteVoiceAudio = { deletedBlockId = it }
            )
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Transcript remains editable").assertIsDisplayed()
        composeRule.onNodeWithTag("voice_player_card", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("editor_content_scrollable").performTouchInput {
            swipeUp()
        }
        composeRule.onNodeWithTag("voice_player_card", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("voice_play_pause_btn", useUnmergedTree = true).assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("voice_seek_slider", useUnmergedTree = true).assertIsDisplayed().performTouchInput {
            swipeRight()
        }
        composeRule.onNodeWithTag("voice_elapsed_label", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("voice_duration_label", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("voice_file_size_label", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("voice_delete_audio_btn", useUnmergedTree = true).performClick()
        composeRule.onNodeWithTag("voice_delete_audio_dialog", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("voice_delete_audio_confirm", useUnmergedTree = true).performClick()
        assertEquals("voice-flow", deletedBlockId)
    }
}
