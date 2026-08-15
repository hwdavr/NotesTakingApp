package com.example.notesapp.ui.editor.model

import androidx.annotation.StringRes
import com.example.notesapp.domain.emoji.EmojiCategory
import com.example.notesapp.domain.emoji.SkinTone
import com.example.notesapp.ui.editor.mapper.EmojiPickerUiMapper

data class EmojiCategoryUiModel(
    val id: EmojiCategory,
    @StringRes val labelRes: Int
)

data class EmojiVariantUiModel(
    val tone: SkinTone,
    val unicode: String,
    @StringRes val labelRes: Int
)

data class EmojiPickerItemUiModel(
    val id: String,
    val unicode: String,
    @StringRes val nameRes: Int,
    val variants: List<EmojiVariantUiModel>
)

data class EmojiPickerUiState(
    val selectedCategory: EmojiCategory = EmojiCategory.RECENT,
    val query: String = "",
    val categories: List<EmojiCategoryUiModel> = EmojiPickerUiMapper.categoryModels(),
    val recentEmoji: List<String> = emptyList(),
    val items: List<EmojiPickerItemUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val hasCatalogError: Boolean = false,
    val activeSkinToneItemId: String? = null
) {
    val isEmptyRecent: Boolean
        get() = selectedCategory == EmojiCategory.RECENT && query.isBlank() && items.isEmpty()

    val isEmptySearch: Boolean
        get() = query.isNotBlank() && items.isEmpty()

    val isEmptyCategory: Boolean
        get() = selectedCategory != EmojiCategory.RECENT && query.isBlank() && items.isEmpty()

    companion object {
        fun empty(): EmojiPickerUiState = EmojiPickerUiState()
    }
}
