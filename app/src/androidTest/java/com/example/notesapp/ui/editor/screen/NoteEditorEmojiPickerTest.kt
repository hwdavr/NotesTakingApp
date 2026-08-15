package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.R
import com.example.notesapp.data.emoji.BundledEmojiCatalogRepository
import com.example.notesapp.domain.emoji.EmojiCategory
import com.example.notesapp.domain.emoji.repository.RecentEmojiRepository
import com.example.notesapp.domain.emoji.usecase.FindEmojiCatalogUseCase
import com.example.notesapp.domain.emoji.usecase.ObserveRecentEmojiUseCase
import com.example.notesapp.domain.emoji.usecase.RecordRecentEmojiUseCase
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.folder.usecase.CategorizeNoteUseCase
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.summary.NoteSummaryResult
import com.example.notesapp.domain.summary.usecase.SummarizeNoteUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteAudioUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteBlockUseCase
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.EmojiPickerUiMapper
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.text
import com.example.notesapp.ui.editor.model.EmojiPickerUiState
import com.example.notesapp.ui.editor.viewmodel.EmojiPickerViewModel
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorEmojiPickerTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun editableToolbarOpensPickerWithRecentSelected() {
        val title = "Emoji note"
        val screenState = mutableStateOf(editableState(title = title))
        composeRule.setContent {
            EmojiPickerTestContent(state = screenState, onEmojiSelected = {})
        }

        openEmojiPicker()

        composeRule.onNodeWithTag("emoji_picker_sheet").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(text(R.string.emoji_picker_recent_category)).assertIsSelected()
        assertEquals(title, screenState.value.title)
    }

    @Test
    fun readOnlyToolbarIsDisabledAndDoesNotOpenPicker() {
        val screenState = mutableStateOf(editableState(title = "Shared note").copy(isEditable = false))
        composeRule.setContent {
            EmojiPickerTestContent(state = screenState, onEmojiSelected = {})
        }

        composeRule.onNodeWithTag("editor_insert_emoji")
            .assertIsDisplayed()
            .assertIsNotEnabled()
            .assertContentDescriptionEquals("Emoji insertion is unavailable in a read-only note.")
        assertTrue(composeRule.onAllNodesWithTag("emoji_picker_sheet").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun selectionInsertsEmojiAndKeepsPickerOpen() {
        val body = EditorBlock.TextBlock(id = "body", children = listOf(RichText("Plan today")))
        val screenState = mutableStateOf(
            editableState(title = "Title", block = body).copy(
                focusedBlockId = body.id,
                selectionStart = 5,
                selectionEnd = 10
            )
        )
        composeRule.setContent {
            EmojiPickerTestContent(
                state = screenState,
                emojiPickerState = pickerState(EmojiCategory.SMILEYS_EMOTION),
                onEmojiSelected = { emoji ->
                    val selectedBlock = screenState.value.document.blocks.single() as EditorBlock.TextBlock
                    screenState.value = screenState.value.copy(
                        document = NoteDocument(
                            blocks = listOf(
                                selectedBlock.copy(children = listOf(RichText("Plan $emoji")))
                            )
                        ),
                        selectionStart = 5 + emoji.length,
                        selectionEnd = 5 + emoji.length
                    )
                }
            )
        }

        openEmojiPicker()
        composeRule.onNodeWithContentDescription(text(R.string.emoji_name_grinning_face)).performClick()
        composeRule.waitForIdle()

        val updatedBlock = screenState.value.document.blocks.single() as EditorBlock.TextBlock
        assertEquals("Plan 😀", updatedBlock.text())
        assertEquals(7, screenState.value.selectionStart)
        assertEquals("Title", screenState.value.title)
        composeRule.onNodeWithTag("emoji_picker_sheet").assertIsDisplayed()
    }

    @Test
    fun categoryRailShowsApprovedResultsAndEmptyRecent() {
        val screenState = mutableStateOf(editableState(title = "Category note"))
        val pickerState = mutableStateOf(EmojiPickerUiState.empty())
        composeRule.setContent {
            EmojiPickerTestContent(
                state = screenState,
                emojiPickerState = pickerState.value,
                onCategorySelected = { category ->
                    pickerState.value = pickerState(category = category)
                }
            )
        }

        openEmojiPicker()

        composeRule.onNodeWithTag("emoji_picker_recent_empty").assertIsDisplayed()
        listOf(
            EmojiCategory.SMILEYS_EMOTION to R.string.emoji_name_grinning_face,
            EmojiCategory.PEOPLE_BODY to R.string.emoji_name_thumbs_up,
            EmojiCategory.ANIMALS_NATURE to R.string.emoji_name_dog,
            EmojiCategory.FOOD_DRINK to R.string.emoji_name_pizza,
            EmojiCategory.ACTIVITIES to R.string.emoji_name_soccer_ball,
            EmojiCategory.TRAVEL_PLACES to R.string.emoji_name_rocket,
            EmojiCategory.OBJECTS to R.string.emoji_name_light_bulb,
            EmojiCategory.SYMBOLS to R.string.emoji_name_star,
            EmojiCategory.FLAGS to R.string.emoji_name_flag_singapore
        ).forEach { (category, itemNameRes) ->
            composeRule.onNodeWithContentDescription(text(category.labelRes()))
                .performScrollTo()
                .performClick()
            composeRule.waitForIdle()
            val itemDescription = if (category == EmojiCategory.PEOPLE_BODY) {
                text(
                    R.string.emoji_picker_item_accessibility_description,
                    text(itemNameRes),
                    text(R.string.emoji_picker_item_skin_tone_hint)
                )
            } else {
                text(itemNameRes)
            }
            composeRule.onNodeWithContentDescription(itemDescription).assertExists()
        }
    }

    @Test
    fun catalogFailureShowsRecoverableStateAndStableElementTags() {
        val screenState = mutableStateOf(editableState(title = "Catalog failure"))
        val pickerState = mutableStateOf(EmojiPickerUiState(hasCatalogError = true))
        composeRule.setContent {
            EmojiPickerTestContent(
                state = screenState,
                emojiPickerState = pickerState.value,
                onCategorySelected = { category ->
                    pickerState.value = pickerState(category)
                }
            )
        }

        openEmojiPicker()

        composeRule.onNodeWithTag("emoji_picker_catalog_error").assertIsDisplayed()
        composeRule.onNodeWithTag("emoji_category_smileys_emotion").performClick()
        composeRule.onNodeWithTag("emoji_picker_item_grinning_face").assertIsDisplayed()
    }

    @Test
    fun searchShowsMatchesAndClearableEmptyState() {
        val screenState = mutableStateOf(editableState(title = "Search note"))
        val pickerState = mutableStateOf(EmojiPickerUiState.empty())
        composeRule.setContent {
            EmojiPickerTestContent(
                state = screenState,
                emojiPickerState = pickerState.value,
                onEmojiQueryChange = { query ->
                    pickerState.value = pickerState(
                        category = pickerState.value.selectedCategory,
                        query = query
                    )
                },
                onEmojiClearQuery = {
                    pickerState.value = pickerState(category = pickerState.value.selectedCategory)
                }
            )
        }

        openEmojiPicker()
        composeRule.onNodeWithTag("emoji_picker_search").performTextInput("launch")
        closeSoftKeyboard()
        composeRule.onNodeWithContentDescription(text(R.string.emoji_name_rocket))
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithTag("emoji_picker_clear_search").performClick()
        composeRule.onNodeWithTag("emoji_picker_recent_empty").assertIsDisplayed()

        composeRule.onNodeWithTag("emoji_picker_search").performTextInput("no matching emoji")
        composeRule.onNodeWithTag("emoji_picker_search_empty").assertIsDisplayed()
        composeRule.onNodeWithTag("emoji_picker_clear_search_empty").performClick()
        composeRule.onNodeWithTag("emoji_picker_recent_empty").assertIsDisplayed()
    }

    @Test
    fun skinToneChoiceInsertsExactVariantAndKeepsSheetOpen() {
        val screenState = mutableStateOf(editableState(title = "Variant note"))
        val pickerState = mutableStateOf(pickerState(EmojiCategory.PEOPLE_BODY))
        var selectedEmoji = ""
        composeRule.setContent {
            EmojiPickerTestContent(
                state = screenState,
                emojiPickerState = pickerState.value,
                onEmojiSelected = { selectedEmoji = it },
                onSkinToneRequested = { itemId ->
                    pickerState.value = pickerState.value.copy(activeSkinToneItemId = itemId)
                },
                onSkinToneDismissed = {
                    pickerState.value = pickerState.value.copy(activeSkinToneItemId = null)
                }
            )
        }

        openEmojiPicker()
        composeRule.onNodeWithContentDescription(
            text(
                R.string.emoji_picker_item_accessibility_description,
                text(R.string.emoji_name_thumbs_up),
                text(R.string.emoji_picker_item_skin_tone_hint)
            )
        ).performTouchInput { longClick() }
        composeRule.onNodeWithContentDescription(
            text(
                R.string.emoji_picker_skin_tone_selector_description,
                text(R.string.emoji_name_thumbs_up)
            )
        )
            .assertIsDisplayed()
        composeRule.onNodeWithTag("emoji_picker_item_thumbs_up").assertIsDisplayed()
        composeRule.onNodeWithTag("emoji_skin_tone_selector_thumbs_up").assertIsDisplayed()
        composeRule.onNodeWithTag("emoji_skin_tone_variant_thumbs_up_medium").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(
            text(
                R.string.emoji_picker_skin_tone_option_description,
                text(R.string.emoji_name_thumbs_up),
                text(R.string.emoji_picker_skin_tone_medium)
            )
        ).performClick()

        assertEquals("👍🏽", selectedEmoji)
        composeRule.onNodeWithTag("emoji_skin_tone_variant_thumbs_up_medium").assertDoesNotExist()
        composeRule.onNodeWithTag("emoji_picker_sheet").assertIsDisplayed()
    }

    @Test
    fun productionScreenWiringInsertsDefaultAndSkinToneAndRecordsRecent() {
        val body = EditorBlock.TextBlock(id = "body", children = listOf(RichText("Body")))
        val note = Note(
            id = "note-1",
            title = "Production wiring",
            content = NoteDocument(blocks = listOf(body)).toJsonString(),
            createdAt = 1L,
            updatedAt = 1L
        )
        val noteRepository = mockk<NoteRepository>(relaxed = true)
        val folderRepository = mockk<FolderRepository>(relaxed = true)
        val summarizeNoteUseCase = mockk<SummarizeNoteUseCase>()
        val editorViewModel = NoteEditorViewModel(
            noteRepository = noteRepository,
            folderRepository = folderRepository,
            summarizeNoteUseCase = summarizeNoteUseCase,
            categorizeNoteUseCase = mockk<CategorizeNoteUseCase>(relaxed = true),
            deleteVoiceNoteAudioUseCase = mockk<DeleteVoiceNoteAudioUseCase>(relaxed = true),
            deleteVoiceNoteBlockUseCase = mockk<DeleteVoiceNoteBlockUseCase>(relaxed = true)
        )
        coEvery { noteRepository.getNoteById("note-1") } returns note
        every { folderRepository.getFolders() } returns flowOf(emptyList())
        coEvery { summarizeNoteUseCase(any(), any()) } returns NoteSummaryResult.Empty
        val recentRepository = RecordingRecentEmojiRepository()
        val emojiPickerViewModel = EmojiPickerViewModel(
            findEmojiCatalogUseCase = FindEmojiCatalogUseCase(BundledEmojiCatalogRepository()),
            observeRecentEmojiUseCase = ObserveRecentEmojiUseCase(recentRepository),
            recordRecentEmojiUseCase = RecordRecentEmojiUseCase(recentRepository)
        )

        emojiPickerViewModel.onCategorySelected(EmojiCategory.PEOPLE_BODY)

        composeRule.setContent {
            NoteEditorScreen(
                parentPadding = PaddingValues(0.dp),
                noteId = "note-1",
                onBack = {},
                onShareNote = {},
                onMoveNote = {},
                onExportNote = {},
                onOpenVoiceRecorder = { _, _ -> },
                viewModel = editorViewModel,
                emojiPickerViewModel = emojiPickerViewModel
            )
        }

        composeRule.waitUntil { editorViewModel.uiState.value.isLoaded }
        editorViewModel.setFocusedBlock("body")
        editorViewModel.updateSelection(4, 4)
        composeRule.waitForIdle()
        openEmojiPicker()
        composeRule.onNodeWithTag("emoji_picker_item_thumbs_up").performClick()
        composeRule.onNodeWithTag("emoji_picker_item_thumbs_up").performTouchInput { longClick() }
        composeRule.onNodeWithTag("emoji_skin_tone_variant_thumbs_up_medium").performClick()
        composeRule.waitUntil { recentRepository.recordedEmoji == listOf("👍", "👍🏽") }

        val updatedText = (editorViewModel.uiState.value.document.blocks.single() as EditorBlock.TextBlock).text()
        assertEquals("Body👍👍🏽", updatedText)
        assertEquals("Production wiring", editorViewModel.uiState.value.title)
        assertEquals(listOf("👍", "👍🏽"), recentRepository.recordedEmoji)
        composeRule.onNodeWithTag("emoji_picker_sheet").assertIsDisplayed()
    }

    private fun openEmojiPicker() {
        composeRule.onNodeWithTag("editor_default_bottom_bar").performTouchInput { swipeLeft() }
        composeRule.onNodeWithTag("editor_insert_emoji").performClick()
        composeRule.waitForIdle()
    }

    private fun text(resId: Int, vararg formatArgs: String): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId, *formatArgs)
}

private fun EmojiCategory.labelRes(): Int = when (this) {
    EmojiCategory.RECENT -> R.string.emoji_picker_recent_category
    EmojiCategory.SMILEYS_EMOTION -> R.string.emoji_picker_category_smileys_emotion
    EmojiCategory.PEOPLE_BODY -> R.string.emoji_picker_category_people_body
    EmojiCategory.ANIMALS_NATURE -> R.string.emoji_picker_category_animals_nature
    EmojiCategory.FOOD_DRINK -> R.string.emoji_picker_category_food_drink
    EmojiCategory.ACTIVITIES -> R.string.emoji_picker_category_activities
    EmojiCategory.TRAVEL_PLACES -> R.string.emoji_picker_category_travel_places
    EmojiCategory.OBJECTS -> R.string.emoji_picker_category_objects
    EmojiCategory.SYMBOLS -> R.string.emoji_picker_category_symbols
    EmojiCategory.FLAGS -> R.string.emoji_picker_category_flags
}

@Composable
private fun EmojiPickerTestContent(
    state: MutableState<NoteEditorUiState>,
    emojiPickerState: EmojiPickerUiState = EmojiPickerUiState.empty(),
    onEmojiSelected: (String) -> Unit = {},
    onEmojiQueryChange: (String) -> Unit = {},
    onEmojiClearQuery: () -> Unit = {},
    onCategorySelected: (EmojiCategory) -> Unit = {},
    onSkinToneRequested: (String) -> Unit = {},
    onSkinToneDismissed: () -> Unit = {}
) {
    NoteEditorScreenContent(
        parentPadding = PaddingValues(0.dp),
        noteId = state.value.noteId,
        state = state.value,
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
        onEmojiSelected = onEmojiSelected,
        emojiPickerState = emojiPickerState,
        onEmojiQueryChange = onEmojiQueryChange,
        onEmojiClearQuery = onEmojiClearQuery,
        onEmojiCategorySelected = onCategorySelected,
        onEmojiSkinToneRequested = onSkinToneRequested,
        onEmojiSkinToneDismissed = onSkinToneDismissed,
        onImageChange = { _, _, _ -> },
        onAddTable = {},
        onTableCellChange = { _, _, _, _ -> },
        onFolderSelected = {},
        onToggleFormattingToolbar = {},
        onBlockFocused = {},
        onSelectionChange = { _, _ -> },
        onDeleteBlock = {}
    )
}

private fun pickerState(category: EmojiCategory, query: String = ""): EmojiPickerUiState = EmojiPickerUiState(
    selectedCategory = category,
    query = query,
    items = EmojiPickerUiMapper.mapItems(findEmojiCatalogForTests(category, query))
)

private fun findEmojiCatalogForTests(category: EmojiCategory, query: String) =
    FindEmojiCatalogUseCase(BundledEmojiCatalogRepository())(category, query)

private fun editableState(
    title: String,
    block: EditorBlock.TextBlock = EditorBlock.TextBlock(id = "body", children = listOf(RichText("Body")))
): NoteEditorUiState = NoteEditorUiState(
    noteId = "note-1",
    title = title,
    document = NoteDocument(blocks = listOf(block)),
    isLoaded = true,
    focusedBlockId = block.id,
    selectionStart = block.text().length,
    selectionEnd = block.text().length
)

private class RecordingRecentEmojiRepository : RecentEmojiRepository {
    private val recent = MutableStateFlow<List<String>>(emptyList())
    val recordedEmoji = mutableListOf<String>()

    override val recentEmoji: Flow<List<String>> = recent

    override suspend fun recordSelectedEmoji(emoji: String) {
        recordedEmoji += emoji
        recent.value = listOf(emoji) + recent.value.filterNot { it == emoji }
    }
}
