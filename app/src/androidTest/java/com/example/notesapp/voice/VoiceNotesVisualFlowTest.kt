package com.example.notesapp.voice

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.screen.NoteEditorScreenContent
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import com.example.notesapp.ui.home.model.HomeUiState
import com.example.notesapp.ui.home.screen.HomeNotesScreenContent
import com.example.notesapp.ui.settings.screen.SettingsScreenContent
import com.example.notesapp.ui.settings.viewmodel.SettingsAudioFormat
import com.example.notesapp.ui.settings.viewmodel.VoiceStorageUiState
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import com.example.notesapp.ui.voice.model.VoiceRecorderStatus
import com.example.notesapp.ui.voice.model.VoiceRecorderUiState
import com.example.notesapp.ui.voice.screen.VoiceRecorderContent
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoiceNotesVisualFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var audioFile: File? = null

    @After
    fun tearDown() {
        audioFile?.takeIf { it.parentFile?.canWrite() == true }?.delete()
    }

    @Test
    fun recorderInProgressLightTheme() {
        composeRule.setContent {
            NotesTakingAppTheme {
                VoiceRecorderContent(
                    state = VoiceRecorderUiState(
                        status = VoiceRecorderStatus.Recording,
                        elapsedMs = 204_000L,
                        amplitudes = List(64) { index -> (index % 5 + 1) / 5f },
                        transcript = "Hey team, quick follow-up on this morning's sync."
                    ),
                    transcriptScrollState = rememberScrollState(),
                    onClose = {},
                    onDiscardRequest = {},
                    onDiscardConfirm = {},
                    onDiscardCancel = {},
                    onPauseResume = {},
                    onStop = {},
                    onBack = {},
                    onPermissionGrant = {}
                )
            }
        }

        composeRule.onNodeWithTag("recorder_status_pill").assertIsDisplayed()
        composeRule.onNodeWithTag("recorder_elapsed_timer").assertIsDisplayed()
        composeRule.onNodeWithTag("recorder_waveform").assertIsDisplayed()
        composeRule.onNodeWithTag("recorder_transcript_preview").assertIsDisplayed()
        composeRule.onNodeWithTag("recorder_toggle_record_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("recorder_stop_btn").assertIsDisplayed()
        captureVisualEvidence("recorder_in_progress_light")
    }

    @Test
    fun allTargetStatesAreReachableAndAsserted() {
        audioFile = visualAudioFixture("visual-flow-combined.wav")
        composeRule.setContent {
            NotesTakingAppTheme {
                Column {
                    Box(modifier = Modifier.height(720.dp)) {
                        HomeNotesScreenContent(
                            parentPadding = PaddingValues(0.dp),
                            state = HomeUiState(isLoading = false),
                            onAddNote = {},
                            onRecordNote = {},
                            onOpenNote = {},
                            onSelectFolder = {}
                        )
                    }
                    Box(modifier = Modifier.height(720.dp)) {
                        VoiceRecorderContent(
                            state = VoiceRecorderUiState(
                                status = VoiceRecorderStatus.Recording,
                                elapsedMs = 204_000L,
                                amplitudes = List(64) { 0.5f },
                                transcript = "Live transcript preview"
                            ),
                            transcriptScrollState = rememberScrollState(),
                            onClose = {},
                            onDiscardRequest = {},
                            onDiscardConfirm = {},
                            onDiscardCancel = {},
                            onPauseResume = {},
                            onStop = {},
                            onBack = {},
                            onPermissionGrant = {}
                        )
                    }
                    Box(modifier = Modifier.height(720.dp)) {
                        NoteEditorScreenContent(
                            parentPadding = PaddingValues(0.dp),
                            noteId = "visual-note",
                            state = NoteEditorUiState(
                                noteId = "visual-note",
                                title = "Voice note",
                                document = NoteDocument(
                                    blocks = listOf(
                                        EditorBlock.Voice(
                                            blockId = "combined-voice",
                                            audioFilePath = audioFile?.absolutePath,
                                            audioFormat = AudioFormat.AAC,
                                            durationMs = 10_000L,
                                            fileSizeBytes = 1_024L,
                                            sampleRateHertz = 44_100,
                                            channels = 1,
                                            createdAt = 1L,
                                            updatedAt = 1L
                                        ),
                                        EditorBlock.TextBlock(
                                            id = "combined-transcript",
                                            children = listOf(RichText("Editable transcript"))
                                        )
                                    )
                                ),
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
                            onImageChange = { _, _, _ -> },
                            onAddTable = {},
                            onTableCellChange = { _, _, _, _ -> },
                            onFolderSelected = {},
                            onToggleFormattingToolbar = {},
                            onBlockFocused = {},
                            onSelectionChange = { _, _ -> },
                            onDeleteBlock = {},
                            onDeleteVoiceAudio = {}
                        )
                    }
                    Box(modifier = Modifier.height(720.dp)) {
                        SettingsScreenContent(
                            parentPadding = PaddingValues(0.dp),
                            voiceAudioFormat = SettingsAudioFormat.AAC,
                            voiceStorage = VoiceStorageUiState(
                                totalBytes = 184_600_000L,
                                recordingCount = 12
                            ),
                            onVoiceAudioFormatSelected = {},
                            onLogout = {}
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithTag("home_add_fab", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("recorder_status_pill", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("voice_player_card", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("settings_voice_notes_section", useUnmergedTree = true).assertExists()
    }

    @Test
    fun homeFabSheetLightTheme() {
        composeRule.setContent {
            NotesTakingAppTheme {
                HomeNotesScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    state = HomeUiState(isLoading = false),
                    onAddNote = {},
                    onRecordNote = {},
                    onOpenNote = {},
                    onSelectFolder = {}
                )
            }
        }

        composeRule.onNodeWithTag("home_add_fab").performClick()
        composeRule.onNodeWithTag("home_fab_sheet_title").assertIsDisplayed()
        composeRule.onNodeWithTag("home_fab_text_note").assertIsDisplayed()
        composeRule.onNodeWithTag("home_fab_record_note").assertIsDisplayed()
        captureVisualEvidence("home_fab_sheet_light")
    }

    @Test
    fun editorVoiceBlockLightTheme() {
        audioFile = visualAudioFixture("visual-flow.wav")
        val voiceBlock = EditorBlock.Voice(
            blockId = "visual-voice",
            audioFilePath = audioFile?.absolutePath,
            audioFormat = AudioFormat.AAC,
            durationMs = 272_000L,
            fileSizeBytes = 2_400_000L,
            sampleRateHertz = 44_100,
            channels = 1,
            createdAt = 1L,
            updatedAt = 1L
        )
        composeRule.setContent {
            NotesTakingAppTheme {
                NoteEditorScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    noteId = "visual-note",
                    state = NoteEditorUiState(
                        noteId = "visual-note",
                        title = "Product Sync — Meeting Notes",
                        document = NoteDocument(
                            blocks = listOf(
                                voiceBlock,
                                EditorBlock.TextBlock(
                                    id = "visual-transcript",
                                    children = listOf(RichText("Hey team, quick recap of this morning's action items."))
                                )
                            )
                        ),
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
                    onImageChange = { _, _, _ -> },
                    onAddTable = {},
                    onTableCellChange = { _, _, _, _ -> },
                    onFolderSelected = {},
                    onToggleFormattingToolbar = {},
                    onBlockFocused = {},
                    onSelectionChange = { _, _ -> },
                    onDeleteBlock = {},
                    onDeleteVoiceAudio = {}
                )
            }
        }

        composeRule.onNodeWithTag("editor_default_bottom_bar", useUnmergedTree = true)
            .performTouchInput { swipeLeft() }
        composeRule.onNodeWithTag("editor_mic_btn", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("voice_player_card", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("voice_seek_slider", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("voice_file_size_label", useUnmergedTree = true).assertExists()
        captureVisualEvidence("editor_voice_block_light")
    }

    @Test
    fun settingsVoiceNotesLightTheme() {
        composeRule.setContent {
            NotesTakingAppTheme {
                SettingsScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    voiceAudioFormat = SettingsAudioFormat.AAC,
                    voiceStorage = VoiceStorageUiState(
                        totalBytes = 184_600_000L,
                        recordingCount = 12
                    ),
                    onVoiceAudioFormatSelected = {},
                    onLogout = {}
                )
            }
        }

        composeRule.onNodeWithTag("settings_voice_notes_section")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("settings_voice_storage_value").assertExists()
        composeRule.onNodeWithTag("settings_voice_format_toggle").assertExists()
        composeRule.onNodeWithTag("settings_voice_format_aac").assertExists()
        composeRule.onNodeWithTag("settings_voice_format_opus").assertExists()
        captureVisualEvidence("settings_voice_notes_light")
    }

    private fun captureVisualEvidence(name: String) {
        composeRule.waitForIdle()
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            ?: error("Could not capture visual evidence: $name")
        val directory = InstrumentationRegistry.getInstrumentation().targetContext
            .getExternalFilesDir("visual_evidence")
            ?: error("External files directory unavailable")
        directory.mkdirs()
        File(directory, "$name.png").outputStream().use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                "Could not write visual evidence: $name"
            }
        }
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand(
                "cp ${File(directory, "$name.png").absolutePath} " +
                    "/sdcard/Download/notesapp_visual_$name.png"
            ).use { }
    }

    private fun visualAudioFixture(name: String): File {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = context.getExternalFilesDir("visual_audio")
            ?: error("External files directory unavailable")
        directory.mkdirs()
        val sampleCount = 8_000
        val dataSize = sampleCount * 2
        val bytes = ByteArray(44 + dataSize)
        bytes[0] = 'R'.code.toByte()
        bytes[1] = 'I'.code.toByte()
        bytes[2] = 'F'.code.toByte()
        bytes[3] = 'F'.code.toByte()
        ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(36 + dataSize)
        bytes[8] = 'W'.code.toByte()
        bytes[9] = 'A'.code.toByte()
        bytes[10] = 'V'.code.toByte()
        bytes[11] = 'E'.code.toByte()
        bytes[12] = 'f'.code.toByte()
        bytes[13] = 'm'.code.toByte()
        bytes[14] = 't'.code.toByte()
        bytes[15] = ' '.code.toByte()
        ByteBuffer.wrap(bytes, 16, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(16)
        ByteBuffer.wrap(bytes, 20, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(1.toShort())
        ByteBuffer.wrap(bytes, 22, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(1.toShort())
        ByteBuffer.wrap(bytes, 24, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(8_000)
        ByteBuffer.wrap(bytes, 28, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(16_000)
        ByteBuffer.wrap(bytes, 32, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(2.toShort())
        ByteBuffer.wrap(bytes, 34, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(16.toShort())
        bytes[36] = 'd'.code.toByte()
        bytes[37] = 'a'.code.toByte()
        bytes[38] = 't'.code.toByte()
        bytes[39] = 'a'.code.toByte()
        ByteBuffer.wrap(bytes, 40, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(dataSize)
        return File(directory, name).apply {
            outputStream().use { output -> output.write(bytes) }
        }
    }
}
