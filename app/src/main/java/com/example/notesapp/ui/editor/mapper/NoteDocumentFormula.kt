package com.example.notesapp.ui.editor.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.example.notesapp.ui.editor.components.InlineFormulaRenderer
import java.util.UUID

const val INLINE_FORMULA_PLACEHOLDER = "\uFFFC"

fun newInlineFormulaId(): String = "inline_${UUID.randomUUID()}"

fun RichText.readableText(): String = formulaSource ?: text

internal fun EditorBlock.TextBlock.formulaAtCursor(cursorOffset: Int): RichText? {
    var currentOffset = 0
    return children.firstOrNull { child ->
        val childStart = currentOffset
        val childEnd = childStart + child.text.length
        currentOffset = childEnd
        child.isFormula && cursorOffset in childStart..childEnd
    }
}

internal data class TextBlockVisualText(
    val annotatedString: AnnotatedString,
    val offsetMapping: OffsetMapping
)

internal fun EditorBlock.TextBlock.toAnnotatedString(
    codeBackground: Color,
    transparentBackground: Color,
    linkColor: Color? = null
): AnnotatedString = toVisualText(codeBackground, transparentBackground, linkColor).annotatedString

internal fun EditorBlock.TextBlock.toVisualText(
    codeBackground: Color,
    transparentBackground: Color,
    linkColor: Color? = null
): TextBlockVisualText {
    val originalToTransformed = IntArray(text().length + 1)
    val transformedToOriginal = mutableListOf(0)
    var originalOffset = 0
    var transformedOffset = 0

    val annotated = buildAnnotatedString {
        children.forEach { child ->
            val displayText = child.formulaSource
                ?.let { InlineFormulaRenderer.render(it).displayText }
                ?: child.text
            originalToTransformed[originalOffset] = transformedOffset

            val decorations = buildList {
                if ("underline" in child.marks || !child.linkTargetId.isNullOrBlank()) {
                    add(TextDecoration.Underline)
                }
                if ("strikethrough" in child.marks) {
                    add(TextDecoration.LineThrough)
                }
            }
            val spanStyle = SpanStyle(
                fontWeight =
                if ("bold" in child.marks) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                },
                fontStyle =
                if ("italic" in child.marks) {
                    FontStyle.Italic
                } else {
                    FontStyle.Normal
                },
                textDecoration = decorations.takeIf { it.isNotEmpty() }?.let(TextDecoration::combine),
                fontFamily = if ("code" in child.marks) FontFamily.Monospace else null,
                background =
                if ("code" in child.marks) {
                    codeBackground
                } else {
                    transparentBackground
                }
            )
            val styledSpan = if (!child.linkTargetId.isNullOrBlank() && linkColor != null) {
                spanStyle.copy(color = linkColor)
            } else {
                spanStyle
            }
            withStyle(styledSpan) { append(displayText) }

            if (child.isFormula) {
                repeat(displayText.length) {
                    transformedToOriginal += originalOffset
                    transformedOffset++
                }
                originalOffset += child.text.length
                transformedToOriginal[transformedOffset] = originalOffset
            } else {
                repeat(child.text.length) {
                    originalOffset++
                    transformedOffset++
                    transformedToOriginal += originalOffset
                    originalToTransformed[originalOffset] = transformedOffset
                }
            }
            originalToTransformed[originalOffset] = transformedOffset
        }
    }
    return TextBlockVisualText(
        annotatedString = annotated,
        offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                originalToTransformed[offset.coerceIn(0, originalToTransformed.size - 1)]

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset >= transformedToOriginal.size) {
                    return transformedToOriginal.last()
                }
                return transformedToOriginal[offset]
            }
        }
    )
}

fun findWordBoundary(text: String, cursor: Int): TextRange {
    if (text.isEmpty()) return TextRange.Zero
    val clamped = cursor.coerceIn(0, text.length)
    val index = if (clamped == text.length && clamped > 0) clamped - 1 else clamped
    if (!text[index].isLetterOrDigit() && text[index] != '_') {
        if (index > 0 && (text[index - 1].isLetterOrDigit() || text[index - 1] == '_')) {
            val wordCharIndex = index - 1
            var start = wordCharIndex
            while (start > 0 && (text[start - 1].isLetterOrDigit() || text[start - 1] == '_')) {
                start--
            }
            var end = wordCharIndex + 1
            while (end < text.length && (text[end].isLetterOrDigit() || text[end] == '_')) {
                end++
            }
            return TextRange(start, end)
        }
        return TextRange(clamped, clamped)
    }
    var start = index
    while (start > 0 && (text[start - 1].isLetterOrDigit() || text[start - 1] == '_')) {
        start--
    }
    var end = index + 1
    while (end < text.length && (text[end].isLetterOrDigit() || text[end] == '_')) {
        end++
    }
    return TextRange(start, end)
}
