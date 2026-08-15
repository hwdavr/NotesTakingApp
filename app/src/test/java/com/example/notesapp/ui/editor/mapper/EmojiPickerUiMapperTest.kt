package com.example.notesapp.ui.editor.mapper

import com.example.notesapp.R
import com.example.notesapp.data.emoji.BundledEmojiCatalogRepository
import com.example.notesapp.domain.emoji.EmojiCategory
import com.example.notesapp.domain.emoji.SkinTone
import com.example.notesapp.domain.emoji.usecase.FindEmojiCatalogUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiPickerUiMapperTest {
    private val catalog = BundledEmojiCatalogRepository().getCatalog()

    @Test
    fun mapsExactSkinToneVariants() {
        val thumbsUp = catalog.first { it.id == "thumbs_up" }

        val mapped = EmojiPickerUiMapper.mapItems(listOf(thumbsUp)).single()

        assertEquals(R.string.emoji_name_thumbs_up, mapped.nameRes)
        assertEquals(6, mapped.variants.size)
        assertEquals("👍🏽", mapped.variants.first { it.tone == SkinTone.MEDIUM }.unicode)
    }

    @Test
    fun mapsAllApprovedCategoriesWithRecentFirst() {
        val categories = EmojiPickerUiMapper.categoryModels()

        assertEquals(EmojiCategory.values().toList(), categories.map { it.id })
    }

    @Test
    fun mapsEveryBundledEmojiNameAndSkinToneLabel() {
        val mapped = EmojiPickerUiMapper.mapItems(catalog)

        assertEquals(catalog.map { it.id }, mapped.map { it.id })
        assertEquals(catalog.map { it.unicode }, mapped.map { it.unicode })
        assertTrue(mapped.all { item -> item.variants.size == catalog.first { it.id == item.id }.variants.size })
    }

    @Test
    fun mappedSearchResultKeepsTheExactCatalogUnicode() {
        val result = FindEmojiCatalogUseCase(BundledEmojiCatalogRepository())(
            category = EmojiCategory.RECENT,
            query = "launch"
        )

        val mapped = EmojiPickerUiMapper.mapItems(result).single()

        assertEquals("🚀", mapped.unicode)
    }
}
