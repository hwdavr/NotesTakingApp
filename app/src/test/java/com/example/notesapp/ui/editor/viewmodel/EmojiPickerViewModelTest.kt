package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.data.emoji.BundledEmojiCatalogRepository
import com.example.notesapp.domain.emoji.EmojiCatalogRepository
import com.example.notesapp.domain.emoji.EmojiCategory
import com.example.notesapp.domain.emoji.usecase.FindEmojiCatalogUseCase
import com.example.notesapp.domain.emoji.usecase.ObserveRecentEmojiUseCase
import com.example.notesapp.domain.emoji.usecase.RecordRecentEmojiUseCase
import com.example.notesapp.domain.emoji.repository.RecentEmojiRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
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
        val viewModel = createViewModel(
            catalogRepository = failingRepository
        )

        assertTrue(viewModel.uiState.value.hasCatalogError)
        assertTrue(viewModel.uiState.value.items.isEmpty())
        assertTrue(viewModel.uiState.value.isEmptyRecent)
    }

    @Test
    fun loadsExactPersistedRecentUnicodeInMostRecentOrder() {
        val viewModel = createViewModel(recentEmoji = listOf("👍🏽", "🚀"))

        assertEquals(listOf("👍🏽", "🚀"), viewModel.uiState.value.recentEmoji)
        assertEquals(listOf("👍🏽", "🚀"), viewModel.uiState.value.items.map { it.unicode })
    }

    @Test
    fun recordsExactSelectedUnicodeThroughTheUseCase() = runTest {
        val recentRepository = FakeRecentEmojiRepository(emptyList())
        val viewModel = createViewModel(recentRepository = recentRepository)

        viewModel.onEmojiSelected("👍🏽")
        advanceUntilIdle()

        assertEquals("👍🏽", recentRepository.recordedEmoji)
    }

    private fun createViewModel(
        catalogRepository: EmojiCatalogRepository = BundledEmojiCatalogRepository(),
        recentEmoji: List<String> = emptyList(),
        recentRepository: FakeRecentEmojiRepository = FakeRecentEmojiRepository(recentEmoji)
    ): EmojiPickerViewModel = EmojiPickerViewModel(
        findEmojiCatalogUseCase = FindEmojiCatalogUseCase(catalogRepository),
        observeRecentEmojiUseCase = ObserveRecentEmojiUseCase(recentRepository),
        recordRecentEmojiUseCase = RecordRecentEmojiUseCase(recentRepository)
    )

    private class FakeRecentEmojiRepository(
        private val storedEmoji: List<String>
    ) : RecentEmojiRepository {
        var recordedEmoji: String? = null
        override val recentEmoji: Flow<List<String>> = flowOf(storedEmoji)

        override suspend fun recordSelectedEmoji(emoji: String) {
            recordedEmoji = emoji
        }
    }
}
