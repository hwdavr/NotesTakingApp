package com.example.notesapp.ui.editor.mapper

fun List<RichText>.splitAtOffsets(offsets: List<Int>): List<RichText> {
    val result = mutableListOf<RichText>()
    var currentOffset = 0
    val sortedOffsets = offsets.filter { it > 0 }.distinct().sorted()
    var offsetIndex = 0

    for (child in this) {
        val childLength = child.text.length
        var childStart = 0

        while (offsetIndex < sortedOffsets.size && sortedOffsets[offsetIndex] < currentOffset + childLength) {
            val splitOffset = sortedOffsets[offsetIndex]
            val relativeSplit = splitOffset - currentOffset
            if (relativeSplit > childStart) {
                result.add(
                    child.copy(
                        text = child.text.substring(childStart, relativeSplit),
                        formulaSource = null,
                        inlineId = null
                    )
                )
                childStart = relativeSplit
            }
            offsetIndex++
        }

        if (childStart < childLength) {
            result.add(child.copy(text = child.text.substring(childStart)))
        }
        currentOffset += childLength
    }
    return result
}

fun List<RichText>.mergeAdjacentWithSameMarks(): List<RichText> {
    if (isEmpty()) return this
    val result = mutableListOf<RichText>()
    var current = first()
    for (i in 1 until size) {
        val next = get(i)
        val mergeable = !current.isFormula && !next.isFormula &&
            current.marks.sorted() == next.marks.sorted() &&
            current.linkTargetId == next.linkTargetId
        if (mergeable) {
            current = RichText(
                text = current.text + next.text,
                marks = current.marks,
                linkTargetId = current.linkTargetId
            )
        } else {
            result.add(current)
            current = next
        }
    }
    result.add(current)
    return result.filterNot { it.text.isEmpty() }.ifEmpty { listOf(RichText("")) }
}

fun EditorBlock.TextBlock.marksAtOffset(offset: Int): List<String> {
    var cur = 0
    for (child in children) {
        val childEnd = cur + child.text.length
        if (offset in cur..childEnd && child.text.isNotEmpty()) {
            return child.marks
        }
        cur = childEnd
    }
    return children.lastOrNull()?.marks ?: emptyList()
}

fun List<RichText>.applyTextDiff(newText: String, pendingMarks: Set<String>? = null): List<RichText> {
    val oldText = joinToString("") { it.text }
    if (oldText == newText) return this
    if (newText.isEmpty()) return listOf(RichText(""))

    var prefixLen = 0
    while (prefixLen < oldText.length && prefixLen < newText.length && oldText[prefixLen] == newText[prefixLen]) {
        prefixLen++
    }

    var oldSuffixLen = 0
    while (oldSuffixLen < (oldText.length - prefixLen) &&
        oldSuffixLen < (newText.length - prefixLen) &&
        oldText[oldText.length - 1 - oldSuffixLen] == newText[newText.length - 1 - oldSuffixLen]
    ) {
        oldSuffixLen++
    }

    val deleteStart = prefixLen
    val deleteEnd = oldText.length - oldSuffixLen
    val insertedText = newText.substring(prefixLen, newText.length - oldSuffixLen)

    val split = splitAtOffsets(listOf(deleteStart, deleteEnd))
    var currentOffset = 0
    val result = mutableListOf<RichText>()
    var inserted = false

    for (child in split) {
        val childStart = currentOffset
        val childEnd = currentOffset + child.text.length
        currentOffset = childEnd

        if (childEnd <= deleteStart) {
            result.add(child)
        } else if (childStart >= deleteEnd) {
            if (!inserted && insertedText.isNotEmpty()) {
                val marks = if (!pendingMarks.isNullOrEmpty()) {
                    pendingMarks.toList()
                } else {
                    result.lastOrNull()?.marks ?: emptyList()
                }
                result.add(RichText(text = insertedText, marks = marks))
                inserted = true
            }
            result.add(child)
        } else {
            if (!inserted && insertedText.isNotEmpty()) {
                val marks = if (!pendingMarks.isNullOrEmpty()) {
                    pendingMarks.toList()
                } else {
                    child.marks
                }
                result.add(RichText(text = insertedText, marks = marks))
                inserted = true
            }
        }
    }

    if (!inserted && insertedText.isNotEmpty()) {
        val marks = if (!pendingMarks.isNullOrEmpty()) {
            pendingMarks.toList()
        } else {
            result.lastOrNull()?.marks ?: emptyList()
        }
        result.add(RichText(text = insertedText, marks = marks))
    }

    return result.filterNot {
        it.text.isEmpty() && !it.isFormula
    }.ifEmpty { listOf(RichText("")) }.mergeAdjacentWithSameMarks()
}
