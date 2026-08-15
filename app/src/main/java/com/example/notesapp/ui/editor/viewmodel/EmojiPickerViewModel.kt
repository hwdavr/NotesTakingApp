package com.example.notesapp.ui.editor.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.emoji.EmojiCategory
import com.example.notesapp.domain.emoji.usecase.FindEmojiCatalogUseCase
import com.example.notesapp.domain.emoji.usecase.ObserveRecentEmojiUseCase
import com.example.notesapp.domain.emoji.usecase.RecordRecentEmojiUseCase
import com.example.notesapp.ui.editor.mapper.EmojiPickerUiMapper
import com.example.notesapp.ui.editor.model.EmojiPickerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class EmojiPickerViewModel @Inject constructor(
    private val findEmojiCatalogUseCase: FindEmojiCatalogUseCase,
    private val observeRecentEmojiUseCase: ObserveRecentEmojiUseCase,
    private val recordRecentEmojiUseCase: RecordRecentEmojiUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(EmojiPickerUiState.empty())
    val uiState: StateFlow<EmojiPickerUiState> = _uiState.asStateFlow()

    init {
        refreshItems()
        observeRecentEmoji()
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

    fun onEmojiSelected(emoji: String) {
        if (emoji.isEmpty()) return

        viewModelScope.launch {
            try {
                recordRecentEmojiUseCase(emoji)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Throwable) {
                Log.w(TAG, "Unable to persist recent emoji selection", exception)
            }
        }
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
                query = current.query,
                recentEmoji = current.recentEmoji
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

    private fun observeRecentEmoji() {
        viewModelScope.launch {
            observeRecentEmojiUseCase()
                .catch { throwable ->
                    if (throwable is CancellationException) throw throwable
                    Log.w(TAG, "Unable to load recent emoji; using an empty list", throwable)
                    emit(emptyList())
                }
                .collect { recentEmoji ->
                    _uiState.value = _uiState.value.copy(recentEmoji = recentEmoji)
                    refreshItems()
                }
        }
    }

    private companion object {
        const val TAG = "NotesApp/EmojiPickerViewModel"
    }
}
