package com.example.notesapp.domain.emoji

import com.example.notesapp.data.emoji.BundledEmojiCatalogRepository
import com.example.notesapp.domain.emoji.usecase.FindEmojiCatalogUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FindEmojiCatalogUseCaseTest {
    private val useCase = FindEmojiCatalogUseCase(BundledEmojiCatalogRepository())

    @Test
    fun returnsEveryApprovedCategory() {
        val resultsByCategory = EmojiCategory.approvedBrowseCategories.associateWith { category ->
            useCase(category)
        }

        assertEquals(9, resultsByCategory.size)
        resultsByCategory.values.forEach { results ->
            assertTrue(results.isNotEmpty())
        }
    }

    @Test
    fun matchesEmojiNamesAndKeywords() {
        val nameMatch = useCase(EmojiCategory.RECENT, query = "rocket")
        val caseInsensitiveNameMatch = useCase(EmojiCategory.RECENT, query = "EUROPE")
        val keywordMatch = useCase(EmojiCategory.RECENT, query = "launch")

        assertEquals(listOf("rocket"), nameMatch.map { it.id })
        assertEquals(listOf("globe_showing_europe_africa"), caseInsensitiveNameMatch.map { it.id })
        assertEquals(listOf("rocket"), keywordMatch.map { it.id })
    }

    @Test
    fun recentCategoryUsesExactPersistedUnicodeOrder() {
        val recents = listOf("👍🏽", "🚀")

        val result = useCase(EmojiCategory.RECENT, recentEmoji = recents)

        assertEquals(listOf("thumbs_up", "rocket"), result.map { it.id })
        assertEquals("👍🏽", result.first().unicodeFor(SkinTone.MEDIUM))
    }
}
