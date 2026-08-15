package com.example.notesapp.ui.editor.viewmodel

import androidx.lifecycle.ViewModel
import com.example.notesapp.domain.emoji.EmojiCategory
import com.example.notesapp.domain.emoji.usecase.FindEmojiCatalogUseCase
import com.example.notesapp.ui.editor.mapper.EmojiPickerUiMapper
import com.example.notesapp.ui.editor.model.EmojiPickerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class EmojiPickerViewModel @Inject constructor(
    private val findEmojiCatalogUseCase: FindEmojiCatalogUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(EmojiPickerUiState.empty())
    val uiState: StateFlow<EmojiPickerUiState> = _uiState.asStateFlow()

    init {
        refreshItems()
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(
            query = query,
            activeSkinToneItemId = null,
            isLoading = true
        )
        refreshItems()
    }

    fun onClearQuery() {
        onQueryChange("")
    }

    fun onCategorySelected(category: EmojiCategory) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            query = "",
            activeSkinToneItemId = null,
            isLoading = true
        )
        refreshItems()
    }

    fun onSkinToneRequested(itemId: String) {
        val item = _uiState.value.items.firstOrNull { it.id == itemId } ?: return
        if (item.variants.isEmpty()) return
        _uiState.value = _uiState.value.copy(activeSkinToneItemId = itemId)
    }

    fun onSkinToneDismissed() {
        _uiState.value = _uiState.value.copy(activeSkinToneItemId = null)
    }

    private fun refreshItems() {
        val current = _uiState.value
        val result = runCatching {
            findEmojiCatalogUseCase(
                category = current.selectedCategory,
                query = current.query
            )
        }
        _uiState.value = result.fold(
            onSuccess = { items ->
                current.copy(
                    items = EmojiPickerUiMapper.mapItems(items),
                    isLoading = false,
                    hasCatalogError = false
                )
            },
            onFailure = {
                current.copy(
                    items = emptyList(),
                    isLoading = false,
                    hasCatalogError = true
                )
            }
        )
    }
}
