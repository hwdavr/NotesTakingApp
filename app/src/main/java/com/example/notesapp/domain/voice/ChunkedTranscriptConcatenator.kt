package com.example.notesapp.domain.voice

import kotlin.math.min

class ChunkedTranscriptConcatenator {
    private val committedChunks = linkedMapOf<Int, String>()
    private var partialChunkIndex: Int? = null
    private var partialText: String = ""

    fun appendPartial(chunkIndex: Int, text: String): String {
        if (chunkIndex < (committedChunks.keys.maxOrNull() ?: -1)) {
            return preview()
        }
        partialChunkIndex = chunkIndex
        partialText = normalize(text)
        return preview()
    }

    fun appendFinal(chunkIndex: Int, text: String): String {
        if (chunkIndex < (committedChunks.keys.maxOrNull() ?: -1)) {
            return preview()
        }
        committedChunks[chunkIndex] = normalize(text)
        if (partialChunkIndex == chunkIndex) {
            partialChunkIndex = null
            partialText = ""
        }
        return preview()
    }

    fun currentText(): String = committedText()

    fun commitPartial(): String {
        val chunkIndex = partialChunkIndex
        val normalizedPartial = normalize(partialText)
        if (chunkIndex != null && normalizedPartial.isNotBlank()) {
            committedChunks[chunkIndex] = normalizedPartial
        }
        partialChunkIndex = null
        partialText = ""
        return committedText()
    }

    fun currentPartialText(): String = partialText

    fun previewText(): String = preview()

    fun reset() {
        committedChunks.clear()
        partialChunkIndex = null
        partialText = ""
    }

    private fun preview(): String {
        val committed = committedText()
        return joinText(committed, partialText)
    }

    private fun committedText(): String {
        var result = ""
        committedChunks.toSortedMap().values.forEach { chunk ->
            result = appendWithOverlap(result, chunk)
        }
        return result
    }

    private fun appendWithOverlap(existing: String, next: String): String {
        val normalizedExisting = normalize(existing)
        val normalizedNext = normalize(next)
        return when {
            normalizedExisting.isBlank() -> normalizedNext
            normalizedNext.isBlank() -> normalizedExisting
            else -> {
                val existingTokens = normalizedExisting.split(WHITESPACE)
                val nextTokens = normalizedNext.split(WHITESPACE)
                val overlap = (min(MAX_OVERLAP_WORDS, min(existingTokens.size, nextTokens.size)) downTo 1)
                    .firstOrNull { count ->
                        existingTokens.takeLast(count).zip(nextTokens.take(count)).all { (left, right) ->
                            left.equals(right, ignoreCase = true)
                        }
                    }

                when {
                    overlap != null -> joinText(
                        normalizedExisting,
                        nextTokens.drop(overlap).joinToString(" ")
                    )
                    isBoundaryFragment(existingTokens.last(), nextTokens.first()) -> joinText(
                        existingTokens.dropLast(1).joinToString(" "),
                        normalizedNext
                    )
                    else -> joinText(normalizedExisting, normalizedNext)
                }
            }
        }
    }

    private fun isBoundaryFragment(existing: String, next: String): Boolean {
        if (existing.length < MIN_FRAGMENT_LENGTH || next.length < MIN_FRAGMENT_LENGTH) return false
        val left = existing.lowercase()
        val right = next.lowercase()
        return left.startsWith(right) || right.startsWith(left)
    }

    private fun normalize(text: String): String = text.trim().replace(WHITESPACE, " ")

    private fun joinText(left: String, right: String): String = when {
        left.isBlank() -> right
        right.isBlank() -> left
        else -> "$left $right"
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
        const val MAX_OVERLAP_WORDS = 12
        const val MIN_FRAGMENT_LENGTH = 3
    }
}
