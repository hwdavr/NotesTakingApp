package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.data.emoji.BundledEmojiCatalogRepository
import com.example.notesapp.domain.emoji.EmojiCatalogRepository
import com.example.notesapp.domain.emoji.EmojiCategory
import com.example.notesapp.domain.emoji.usecase.FindEmojiCatalogUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiPickerViewModelTest : BaseViewModelTest() {
    @Test
    fun startsWithAnEmptyRecentState() {
        val viewModel = createViewModel()

        assertEquals(EmojiCategory.RECENT, viewModel.uiState.value.selectedCategory)
        assertTrue(viewModel.uiState.value.isEmptyRecent)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun selectingCategoryLoadsItsCatalogItems() {
        val viewModel = createViewModel()

        viewModel.onCategorySelected(EmojiCategory.FOOD_DRINK)

        assertEquals(EmojiCategory.FOOD_DRINK, viewModel.uiState.value.selectedCategory)
        assertEquals("pizza", viewModel.uiState.value.items.first().id)
        assertFalse(viewModel.uiState.value.isEmptyCategory)
    }

    @Test
    fun searchingAndClearingQueryUsesTheCatalogAndRestoresRecent() {
        val viewModel = createViewModel()

        viewModel.onQueryChange("launch")

        assertEquals("launch", viewModel.uiState.value.query)
        assertEquals(listOf("rocket"), viewModel.uiState.value.items.map { it.id })

        viewModel.onClearQuery()

        assertEquals("", viewModel.uiState.value.query)
        assertTrue(viewModel.uiState.value.isEmptyRecent)
    }

    @Test
    fun openingAndDismissingSkinToneSelectorUpdatesPresentationState() {
        val viewModel = createViewModel()
        viewModel.onCategorySelected(EmojiCategory.PEOPLE_BODY)

        viewModel.onSkinToneRequested("thumbs_up")
        assertEquals("thumbs_up", viewModel.uiState.value.activeSkinToneItemId)

        viewModel.onSkinToneDismissed()
        assertNull(viewModel.uiState.value.activeSkinToneItemId)
    }

    @Test
    fun skinToneSelectorIgnoresUnknownOrIneligibleItems() {
        val viewModel = createViewModel()
        viewModel.onCategorySelected(EmojiCategory.SMILEYS_EMOTION)

        viewModel.onSkinToneRequested("grinning_face")
        assertNull(viewModel.uiState.value.activeSkinToneItemId)

        viewModel.onSkinToneRequested("missing_item")
        assertNull(viewModel.uiState.value.activeSkinToneItemId)
    }

    @Test
    fun catalogFailureLeavesARecoverableEmptyState() {
        val failingRepository = object : EmojiCatalogRepository {
            override fun getCatalog() = error("catalog unavailable")
        }
        val viewModel = EmojiPickerViewModel(FindEmojiCatalogUseCase(failingRepository))

        assertTrue(viewModel.uiState.value.hasCatalogError)
        assertTrue(viewModel.uiState.value.items.isEmpty())
        assertTrue(viewModel.uiState.value.isEmptyRecent)
    }

    private fun createViewModel(): EmojiPickerViewModel =
        EmojiPickerViewModel(FindEmojiCatalogUseCase(BundledEmojiCatalogRepository()))
}
