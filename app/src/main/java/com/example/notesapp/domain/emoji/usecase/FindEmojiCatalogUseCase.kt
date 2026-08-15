package com.example.notesapp.domain.emoji.usecase

import com.example.notesapp.domain.emoji.EmojiCatalogItem
import com.example.notesapp.domain.emoji.EmojiCatalogRepository
import com.example.notesapp.domain.emoji.EmojiCategory
import javax.inject.Inject

class FindEmojiCatalogUseCase @Inject constructor(
    private val repository: EmojiCatalogRepository
) {
    operator fun invoke(
        category: EmojiCategory,
        query: String = "",
        recentEmoji: List<String> = emptyList()
    ): List<EmojiCatalogItem> {
        val catalog = repository.getCatalog()
        val source = if (query.isBlank()) {
            if (category == EmojiCategory.RECENT) {
                recentEmoji.mapNotNull { recentUnicode ->
                    catalog.firstOrNull { item ->
                        item.unicode == recentUnicode || item.variants.any { it.unicode == recentUnicode }
                    }?.copy(unicode = recentUnicode)
                }
            } else {
                catalog.filter { it.category == category }
            }
        } else {
            catalog
        }
        return source.filter { it.matchesQuery(query) }
    }
}
