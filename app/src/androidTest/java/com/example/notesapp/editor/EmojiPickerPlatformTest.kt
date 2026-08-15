package com.example.notesapp.editor

import android.graphics.Paint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.R
import com.example.notesapp.data.emoji.BundledEmojiCatalogRepository
import com.example.notesapp.domain.emoji.EmojiCategory
import com.example.notesapp.domain.emoji.usecase.FindEmojiCatalogUseCase
import com.example.notesapp.ui.editor.components.EmojiPickerBottomSheet
import com.example.notesapp.ui.editor.mapper.EmojiPickerUiMapper
import com.example.notesapp.ui.editor.model.EmojiPickerUiState
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmojiPickerPlatformTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun unicodeEmojiHasGlyphOnAndroidRuntime() {
        val selectedEmoji = mutableListOf<String>()
        val pickerState = mutableStateOf(peopleBodyPickerState())
        composeRule.setContent {
            NotesTakingAppTheme {
                EmojiPickerBottomSheet(
                    uiState = pickerState.value,
                    onDismiss = {},
                    onQueryChange = {},
                    onClearQuery = {},
                    onCategorySelected = {},
                    onEmojiSelected = selectedEmoji::add,
                    onSkinToneRequested = { itemId ->
                        pickerState.value = pickerState.value.copy(activeSkinToneItemId = itemId)
                    },
                    onSkinToneDismissed = {
                        pickerState.value = pickerState.value.copy(activeSkinToneItemId = null)
                    }
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("emoji_picker_item_thumbs_up", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag("emoji_picker_item_thumbs_up", useUnmergedTree = true)
            .performTouchInput { longClick() }
        composeRule.onNodeWithContentDescription(
            text(
                R.string.emoji_picker_skin_tone_selector_description,
                text(R.string.emoji_name_thumbs_up)
            )
        ).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(
            text(
                R.string.emoji_picker_skin_tone_option_description,
                text(R.string.emoji_name_thumbs_up),
                text(R.string.emoji_picker_skin_tone_medium)
            )
        ).performClick()
        composeRule.waitForIdle()

        assertEquals(listOf("👍", "👍🏽"), selectedEmoji)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 48f }
        selectedEmoji.forEach { emoji ->
            assertTrue("Android font does not support $emoji", paint.hasGlyph(emoji))
        }
    }

    private fun peopleBodyPickerState(): EmojiPickerUiState {
        val catalog = BundledEmojiCatalogRepository()
        val items = FindEmojiCatalogUseCase(catalog)(EmojiCategory.PEOPLE_BODY)
        return EmojiPickerUiState(
            selectedCategory = EmojiCategory.PEOPLE_BODY,
            items = EmojiPickerUiMapper.mapItems(items)
        )
    }

    private fun text(resId: Int, vararg formatArgs: String): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId, *formatArgs)
}
