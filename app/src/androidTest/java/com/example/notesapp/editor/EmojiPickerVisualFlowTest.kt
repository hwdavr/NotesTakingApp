package com.example.notesapp.editor

import android.graphics.Bitmap
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.R
import com.example.notesapp.data.emoji.BundledEmojiCatalogRepository
import com.example.notesapp.domain.emoji.EmojiCategory
import com.example.notesapp.domain.emoji.usecase.FindEmojiCatalogUseCase
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.EmojiPickerUiMapper
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.text
import com.example.notesapp.ui.editor.model.EmojiPickerUiState
import com.example.notesapp.ui.editor.screen.NoteEditorScreenContent
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmojiPickerVisualFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun dismissKeyboardBeforeVisualScenario() {
        closeSoftKeyboard()
    }

    @Test
    fun emojiPickerContentLightTheme() {
        composeRule.setContent {
            NotesTakingAppTheme {
                editorContent(
                    state = editorState(),
                    emojiPickerState = recentPickerState()
                )
            }
        }

        openEmojiPicker()

        composeRule.onNodeWithTag("emoji_picker_sheet", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("emoji_picker_search", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("emoji_picker_categories", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("emoji_picker_grid", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription(
            InstrumentationRegistry.getInstrumentation().targetContext.getString(
                R.string.emoji_picker_item_accessibility_description,
                InstrumentationRegistry.getInstrumentation().targetContext
                    .getString(R.string.emoji_name_thumbs_up),
                InstrumentationRegistry.getInstrumentation().targetContext
                    .getString(R.string.emoji_picker_item_skin_tone_hint)
            )
        ).onFirst().assertIsDisplayed()
        val rootHeight = composeRule.onAllNodes(isRoot(), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .maxOf { node -> node.boundsInRoot.height }
        val sheetHeight = composeRule.onNodeWithTag(
            "emoji_picker_sheet",
            useUnmergedTree = true
        ).fetchSemanticsNode().boundsInRoot.height
        assertEquals(rootHeight * 2f / 5f, sheetHeight, 12f)
        captureVisualEvidence("notesapp_emoji_picker_content_light")
    }

    @Test
    @OptIn(ExperimentalLayoutApi::class)
    fun emojiPickerExpandsToAvailableHeightWhenKeyboardIsVisible() {
        var imeVisible = false
        composeRule.setContent {
            NotesTakingAppTheme {
                val isImeVisible = WindowInsets.isImeVisible
                val pickerState = remember {
                    mutableStateOf(recentPickerState().copy(activeSkinToneItemId = null))
                }
                SideEffect {
                    imeVisible = isImeVisible
                }
                editorContent(
                    state = editorState(),
                    emojiPickerState = pickerState.value,
                    onEmojiQueryChange = { query ->
                        val current = pickerState.value
                        val items = FindEmojiCatalogUseCase(BundledEmojiCatalogRepository())(
                            category = current.selectedCategory,
                            query = query,
                            recentEmoji = current.recentEmoji
                        )
                        pickerState.value = current.copy(
                            query = query,
                            items = EmojiPickerUiMapper.mapItems(items)
                        )
                    }
                )
            }
        }

        openEmojiPicker()
        composeRule.onNodeWithTag("emoji_picker_search", useUnmergedTree = true)
            .performTextInput("launch")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            val rootHeight = composeRule.onAllNodes(isRoot(), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .maxOfOrNull { node -> node.boundsInRoot.height }
            val sheetHeight = composeRule.onAllNodesWithTag(
                "emoji_picker_sheet",
                useUnmergedTree = true
            ).fetchSemanticsNodes().firstOrNull()?.boundsInRoot?.height
            imeVisible && rootHeight != null && sheetHeight != null &&
                sheetHeight > rootHeight * 2f / 5f
        }

        val rootHeight = composeRule.onAllNodes(isRoot(), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .maxOf { node -> node.boundsInRoot.height }
        val sheet = composeRule.onNodeWithTag(
            "emoji_picker_sheet",
            useUnmergedTree = true
        ).fetchSemanticsNode()
        assertTrue(sheet.boundsInRoot.height > rootHeight * 2f / 5f)
        assertTrue(sheet.boundsInRoot.top <= 12f)
        composeRule.onNodeWithTag("emoji_picker_item_rocket", useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule.onNodeWithTag("emoji_picker_grid", useUnmergedTree = true).assertIsDisplayed()
        captureVisualEvidence("notesapp_emoji_picker_keyboard_light")
        closeSoftKeyboard()
    }

    @Test
    fun readOnlyEmojiControlLightTheme() {
        composeRule.setContent {
            NotesTakingAppTheme {
                editorContent(
                    state = editorState().copy(isEditable = false),
                    emojiPickerState = EmojiPickerUiState.empty()
                )
            }
        }

        composeRule.onNodeWithTag("editor_insert_emoji")
            .assertIsDisplayed()
            .assertIsNotEnabled()
            .assertContentDescriptionEquals(
                InstrumentationRegistry.getInstrumentation().targetContext
                    .getString(R.string.emoji_picker_read_only_description)
            )
        captureVisualEvidence("notesapp_emoji_read_only_light")
    }

    @Test
    fun emptySearchEmojiPickerLightTheme() {
        composeRule.setContent {
            NotesTakingAppTheme {
                editorContent(
                    state = editorState(),
                    emojiPickerState = EmojiPickerUiState(
                        selectedCategory = EmojiCategory.RECENT,
                        query = "no matching emoji"
                    )
                )
            }
        }

        openEmojiPicker()

        composeRule.onNodeWithTag("emoji_picker_search_empty", useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule.onNodeWithTag("emoji_picker_clear_search_empty", useUnmergedTree = true)
            .assertIsDisplayed()
        captureVisualEvidence("notesapp_emoji_empty_search_light")
    }

    @Test
    fun pickerSupportsRtlTraversalAndLargeFontScale() {
        composeRule.setContent {
            NotesTakingAppTheme {
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl,
                    LocalDensity provides Density(density = 1f, fontScale = 1.5f)
                ) {
                    editorContent(
                        state = editorState(),
                        emojiPickerState = peopleBodyPickerState()
                    )
                }
            }
        }

        openEmojiPicker()

        composeRule.onNodeWithTag("emoji_picker_search", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("emoji_picker_categories", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("emoji_category_people_body", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("emoji_picker_grid", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("emoji_picker_item_thumbs_up", useUnmergedTree = true).assertIsDisplayed()
    }

    private fun openEmojiPicker() {
        composeRule.onNodeWithTag("editor_default_bottom_bar", useUnmergedTree = true)
            .performTouchInput { swipeLeft() }
        composeRule.onNodeWithTag("editor_insert_emoji", useUnmergedTree = true).performClick()
        composeRule.waitForIdle()
    }

    @Composable
    private fun editorContent(
        state: NoteEditorUiState,
        emojiPickerState: EmojiPickerUiState,
        onEmojiQueryChange: (String) -> Unit = {}
    ) {
        NoteEditorScreenContent(
            parentPadding = PaddingValues(0.dp),
            noteId = state.noteId,
            state = state,
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
            emojiPickerState = emojiPickerState,
            onEmojiQueryChange = onEmojiQueryChange,
            onEmojiClearQuery = {},
            onEmojiCategorySelected = {},
            onEmojiSkinToneRequested = {},
            onEmojiSkinToneDismissed = {},
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

    private fun editorState(): NoteEditorUiState {
        val block = EditorBlock.TextBlock(
            id = "visual-body",
            children = listOf(RichText("Emoji visual verification"))
        )
        return NoteEditorUiState(
            noteId = "visual-note",
            title = "Emoji visual verification",
            document = NoteDocument(blocks = listOf(block)),
            isLoaded = true,
            focusedBlockId = block.id,
            selectionStart = block.text().length,
            selectionEnd = block.text().length
        )
    }

    private fun recentPickerState(): EmojiPickerUiState {
        val catalog = BundledEmojiCatalogRepository()
        val recent = buildList {
            add("👍🏽")
            catalog.getCatalog().forEach { item ->
                if (item.unicode !in this) add(item.unicode)
            }
        }
        val items = FindEmojiCatalogUseCase(catalog)(
            category = EmojiCategory.RECENT,
            recentEmoji = recent
        )
        val mappedItems = EmojiPickerUiMapper.mapItems(items)
        return EmojiPickerUiState(
            selectedCategory = EmojiCategory.RECENT,
            recentEmoji = recent,
            items = mappedItems,
            activeSkinToneItemId = mappedItems.first { it.unicode == "👍🏽" }.id
        )
    }

    private fun peopleBodyPickerState(): EmojiPickerUiState {
        val catalog = BundledEmojiCatalogRepository()
        val items = FindEmojiCatalogUseCase(catalog)(EmojiCategory.PEOPLE_BODY)
        return EmojiPickerUiState(
            selectedCategory = EmojiCategory.PEOPLE_BODY,
            items = EmojiPickerUiMapper.mapItems(items)
        )
    }

    private fun captureVisualEvidence(fileName: String) {
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
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("cp ${screenshot.absolutePath} /sdcard/Download/$fileName.png")
            .use { }
    }
}
