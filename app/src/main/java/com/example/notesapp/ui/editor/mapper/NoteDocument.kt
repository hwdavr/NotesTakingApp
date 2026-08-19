package com.example.notesapp.ui.editor.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.example.notesapp.domain.voice.AudioFormat
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

const val DEFAULT_MERMAID_TITLE = "Mermaid Diagram"
const val DEFAULT_MERMAID_CODE = """graph TD
    A[Start] --> B{Decision}
    B -->|Yes| C[Result 1]
    B -->|No| D[Result 2]"""
const val DEFAULT_CODE_BLOCK_LANGUAGE = "Plain Text"

private const val DOCUMENT_VERSION = 1
data class NoteDocument(
    val version: Int = DOCUMENT_VERSION,
    val blocks: List<EditorBlock> = listOf(EditorBlock.TextBlock())
) {
    fun toJsonString(): String {
        val json = JSONObject()
            .put("version", version)
            .put(
                "blocks",
                JSONArray().also { array ->
                    blocks.forEach { array.put(it.toJson()) }
                }
            )
        return json.toString()
    }
    fun toPlainText(): String = blocks.joinToString("\n") { block ->
        when (block) {
            is EditorBlock.TextBlock -> block.children.joinToString("") { it.text }
            is EditorBlock.ImageBlock -> listOf(block.url, block.caption).filter { it.isNotBlank() }.joinToString(" ")
            is EditorBlock.TableBlock -> block.rows.joinToString("\n") { row ->
                row.joinToString("\t") { cell -> cell.joinToString("") { it.text } }
            }
            is EditorBlock.MermaidBlock -> listOf(block.title, block.code).filter { it.isNotBlank() }.joinToString("\n")
            is EditorBlock.CodeBlock -> block.code
            is EditorBlock.Voice -> ""
        }
    }.trim()
    fun toMarkdown(): String = blocks.joinToString("\n\n") { block ->
        when (block) {
            is EditorBlock.TextBlock -> {
                val blockType = block.basicBlockType()
                val prefix = when (blockType) {
                    BasicBlockType.HEADING_1 -> "# "
                    BasicBlockType.HEADING_2 -> "## "
                    BasicBlockType.HEADING_3 -> "### "
                    BasicBlockType.HEADING_4 -> "#### "
                    BasicBlockType.BULLETED_LIST -> "- "
                    BasicBlockType.NUMBERED_LIST -> "1. "
                    BasicBlockType.TODO_LIST -> if (block.checked) "- [x] " else "- [ ] "
                    BasicBlockType.CALLOUT -> "> [!NOTE] "
                    BasicBlockType.QUOTE -> "> "
                    else -> ""
                }
                val content = block.children.joinToString("") { richText ->
                    var text = richText.text
                    if ("bold" in richText.marks) text = "**$text**"
                    if ("italic" in richText.marks) text = "*$text*"
                    if ("code" in richText.marks) text = "`$text`"
                    text
                }
                if (blockType == BasicBlockType.TOGGLE_LIST) {
                    val openAttribute = if (block.isExpanded) " open" else ""
                    "<details$openAttribute>\n<summary>$content</summary>\n</details>"
                } else {
                    "$prefix$content"
                }
            }
            is EditorBlock.ImageBlock -> {
                "![${block.caption}](${block.url})"
            }
            is EditorBlock.TableBlock -> {
                val header = block.rows.firstOrNull()?.joinToString(" | ") { cell ->
                    cell.joinToString("") { it.text }
                }.orEmpty()
                val divider = block.rows.firstOrNull()?.joinToString(" | ") { "---" }.orEmpty()
                val body = block.rows.drop(1).joinToString("\n") { row ->
                    "| " + row.joinToString(" | ") { cell -> cell.joinToString("") { it.text } } + " |"
                }
                "| $header |\n| $divider |\n$body"
            }
            is EditorBlock.MermaidBlock -> {
                "```mermaid\n${block.code}\n```"
            }
            is EditorBlock.CodeBlock -> {
                "```${codeBlockLanguageSlug(block.language)}\n${block.code}\n```"
            }
            is EditorBlock.Voice -> ""
        }
    }.trim()
    fun ensureEditableTextBlock(): NoteDocument {
        if (blocks.any { it is EditorBlock.TextBlock }) return this
        return copy(blocks = listOf(EditorBlock.TextBlock()) + blocks)
    }
    companion object {
        fun empty(): NoteDocument = NoteDocument()
        fun fromContent(content: String): NoteDocument {
            if (content.isBlank()) return empty()
            return runCatching {
                val json = JSONObject(content)
                val blocks = json.optJSONArray("blocks")?.let { blockArray ->
                    (0 until blockArray.length()).mapNotNull { index ->
                        blockArray.optJSONObject(index)?.toEditorBlock()
                    }
                }.orEmpty()
                NoteDocument(
                    version = json.optInt("version", DOCUMENT_VERSION),
                    blocks = blocks.ifEmpty { listOf(EditorBlock.TextBlock()) }
                )
            }.getOrElse {
                fromPlainText(content)
            }
        }
        fun fromPlainText(content: String): NoteDocument {
            val blocks = content.lines().ifEmpty { listOf("") }.map { line ->
                parseMarkdownTextBlock(text = line)
            }
            return NoteDocument(blocks = blocks)
        }
    }
}
sealed class EditorBlock {
    abstract val id: String
    data class TextBlock(
        override val id: String = newBlockId(),
        val type: String = "paragraph",
        val children: List<RichText> = listOf(RichText("")),
        val checked: Boolean = false,
        val isExpanded: Boolean = true
    ) : EditorBlock()
    data class ImageBlock(
        override val id: String = newBlockId(),
        val url: String = "",
        val caption: String = ""
    ) : EditorBlock()
    data class TableBlock(
        override val id: String = newBlockId(),
        val rows: List<List<List<RichText>>> = defaultTableRows(),
        val fitToWidth: Boolean = false
    ) : EditorBlock()
    data class MermaidBlock(
        override val id: String = newBlockId(),
        val code: String = DEFAULT_MERMAID_CODE,
        val title: String = DEFAULT_MERMAID_TITLE
    ) : EditorBlock()
    data class CodeBlock(
        override val id: String = newBlockId(),
        val language: String = DEFAULT_CODE_BLOCK_LANGUAGE,
        val code: String = ""
    ) : EditorBlock()
    data class Voice(
        val blockId: String,
        val audioFilePath: String?,
        val audioFormat: AudioFormat,
        val durationMs: Long,
        val fileSizeBytes: Long,
        val sampleRateHertz: Int,
        val channels: Int,
        val createdAt: Long,
        val updatedAt: Long
    ) : EditorBlock() {
        override val id: String
            get() = blockId
    }
}
data class RichText(
    val text: String,
    val marks: List<String> = emptyList()
)
fun noteContentPreview(content: String): String = NoteDocument.fromContent(content).toPlainText().ifBlank { content }
fun codeBlockLanguageSlug(language: String): String = when (language.trim().lowercase()) {
    "" -> ""
    DEFAULT_CODE_BLOCK_LANGUAGE.lowercase() -> ""
    else -> language.trim().lowercase().replace(" ", "")
}
fun newBlockId(): String = "b_${UUID.randomUUID()}"
fun parseMarkdownTextBlock(id: String = newBlockId(), text: String): EditorBlock.TextBlock {
    val trimmed = text.trimStart()
    val (type, checked, body) = when {
        trimmed.startsWith("- [ ] ") -> Triple(BasicBlockType.TODO_LIST, false, trimmed.removePrefix("- [ ] "))
        trimmed.startsWith("- [x] ") -> Triple(BasicBlockType.TODO_LIST, true, trimmed.removePrefix("- [x] "))
        trimmed.startsWith("#### ") -> Triple(BasicBlockType.HEADING_4, false, trimmed.removePrefix("#### "))
        trimmed.startsWith("### ") -> Triple(BasicBlockType.HEADING_3, false, trimmed.removePrefix("### "))
        trimmed.startsWith("## ") -> Triple(BasicBlockType.HEADING_2, false, trimmed.removePrefix("## "))
        trimmed.startsWith("# ") -> Triple(BasicBlockType.HEADING_1, false, trimmed.removePrefix("# "))
        trimmed.startsWith("1. ") -> Triple(BasicBlockType.NUMBERED_LIST, false, trimmed.removePrefix("1. "))
        trimmed.startsWith("> [!NOTE] ") -> Triple(BasicBlockType.CALLOUT, false, trimmed.removePrefix("> [!NOTE] "))
        trimmed.startsWith("> ") -> Triple(BasicBlockType.QUOTE, false, trimmed.removePrefix("> "))
        trimmed.startsWith("- ") -> Triple(BasicBlockType.BULLETED_LIST, false, trimmed.removePrefix("- "))
        else -> Triple(BasicBlockType.PARAGRAPH, false, text)
    }
    return EditorBlock.TextBlock(
        id = id,
        type = type.storageValue,
        children = parseInlineMarkdown(body),
        checked = checked
    )
}
fun parseInlineMarkdown(text: String): List<RichText> {
    if (text.isEmpty()) return listOf(RichText(""))
    val result = mutableListOf<RichText>()
    var index = 0
    while (index < text.length) {
        val boldEnd = if (text.startsWith("**", index)) text.indexOf("**", index + 2) else -1
        val codeEnd = if (text[index] == '`') text.indexOf('`', index + 1) else -1
        val italicEnd = if (text[index] == '*' && !text.startsWith("**", index)) text.indexOf('*', index + 1) else -1
        when {
            boldEnd > index -> {
                result += RichText(text.substring(index + 2, boldEnd), listOf("bold"))
                index = boldEnd + 2
            }
            codeEnd > index -> {
                result += RichText(text.substring(index + 1, codeEnd), listOf("code"))
                index = codeEnd + 1
            }
            italicEnd > index -> {
                result += RichText(text.substring(index + 1, italicEnd), listOf("italic"))
                index = italicEnd + 1
            }
            else -> {
                val nextMarker = listOf(
                    text.indexOf("**", index).takeIf { it >= 0 },
                    text.indexOf('`', index).takeIf { it >= 0 },
                    text.indexOf('*', index).takeIf { it >= 0 }
                ).filterNotNull().minOrNull() ?: text.length
                val next = if (nextMarker == index) index + 1 else nextMarker
                result += RichText(text.substring(index, next))
                index = next
            }
        }
    }
    return result.filterNot { it.text.isEmpty() }.ifEmpty { listOf(RichText("")) }
}
fun EditorBlock.TextBlock.text(): String = children.joinToString("") { it.text }

internal fun EditorBlock.TextBlock.toAnnotatedString(
    codeBackground: Color,
    transparentBackground: Color
): AnnotatedString {
    return buildAnnotatedString {
        children.forEach { child ->
            withStyle(
                SpanStyle(
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
                    textDecoration = when {
                        "underline" in child.marks -> TextDecoration.Underline
                        "strikethrough" in child.marks -> TextDecoration.LineThrough
                        else -> null
                    },
                    background =
                    if ("code" in child.marks) {
                        codeBackground
                    } else {
                        transparentBackground
                    }
                )
            ) { append(child.text) }
        }
    }
}

private fun EditorBlock.toJson(): JSONObject = when (this) {
    is EditorBlock.TextBlock -> JSONObject()
        .put("id", id)
        .put("type", canonicalStorageType())
        .put("children", children.toRichTextJson())
        .put("checked", checked)
        .also { json ->
            if (basicBlockType() == BasicBlockType.TOGGLE_LIST) {
                json.put("expanded", isExpanded)
            }
        }
    is EditorBlock.ImageBlock -> JSONObject()
        .put("id", id)
        .put("type", "image")
        .put("url", url)
        .put("caption", caption)
    is EditorBlock.TableBlock -> JSONObject()
        .put("id", id)
        .put("type", "table")
        .put("rows", rows.toRowsJson())
        .put("fitToWidth", fitToWidth)
    is EditorBlock.MermaidBlock -> JSONObject()
        .put("id", id)
        .put("type", "mermaid")
        .put("title", title)
        .put("code", code)
    is EditorBlock.CodeBlock -> JSONObject()
        .put("id", id)
        .put("type", "code")
        .put("language", language)
        .put("code", code)
    is EditorBlock.Voice -> JSONObject()
        .put("id", id)
        .put("blockId", blockId)
        .put("type", "voice")
        .put("audioFilePath", audioFilePath ?: JSONObject.NULL)
        .put("audioFormat", audioFormat.storageValue)
        .put("durationMs", durationMs)
        .put("fileSizeBytes", fileSizeBytes)
        .put("sampleRateHertz", sampleRateHertz)
        .put("channels", channels)
        .put("createdAt", createdAt)
        .put("updatedAt", updatedAt)
}
private fun JSONObject.toEditorBlock(): EditorBlock? {
    val id = optString("id").ifBlank { newBlockId() }
    return when (optString("type", BasicBlockType.PARAGRAPH.storageValue)) {
        "image" -> EditorBlock.ImageBlock(
            id = id,
            url = optString("url"),
            caption = optString("caption")
        )
        "table" -> EditorBlock.TableBlock(
            id = id,
            rows = optJSONArray("rows").toRows(),
            fitToWidth = optBoolean("fitToWidth", false)
        )
        "mermaid" -> EditorBlock.MermaidBlock(
            id = id,
            title = optString("title", DEFAULT_MERMAID_TITLE),
            code = optString("code", DEFAULT_MERMAID_CODE)
        )
        "code" -> EditorBlock.CodeBlock(
            id = id,
            language = optString("language", DEFAULT_CODE_BLOCK_LANGUAGE).ifBlank { DEFAULT_CODE_BLOCK_LANGUAGE },
            code = optString("code", "")
        )
        "voice" -> EditorBlock.Voice(
            blockId = optString("blockId", id),
            audioFilePath = if (isNull("audioFilePath")) null else optString("audioFilePath").ifBlank { null },
            audioFormat = AudioFormat.fromStorageValue(optString("audioFormat")),
            durationMs = optLong("durationMs", 0L),
            fileSizeBytes = optLong("fileSizeBytes", 0L),
            sampleRateHertz = optInt("sampleRateHertz", 44_100),
            channels = optInt("channels", 1),
            createdAt = optLong("createdAt", 0L),
            updatedAt = optLong("updatedAt", 0L)
        )
        else -> toTextBlockOrFallback(id)
    }
}

private fun JSONObject.toTextBlockOrFallback(id: String): EditorBlock.TextBlock? {
    val blockType = BasicBlockType.fromStorageValue(optString("type", BasicBlockType.PARAGRAPH.storageValue))
    return if (blockType != BasicBlockType.UNKNOWN) {
        EditorBlock.TextBlock(
            id = id,
            type = blockType.storageValue,
            children = optJSONArray("children").toRichTextList(),
            checked = optBoolean("checked", false),
            isExpanded = if (blockType == BasicBlockType.TOGGLE_LIST) optBoolean("expanded", true) else true
        )
    } else {
        unknownTextBlock(id)
    }
}

private fun JSONObject.unknownTextBlock(id: String): EditorBlock.TextBlock? {
    val children = when {
        has("children") -> optJSONArray("children").toRichTextList()
        has("text") -> listOf(RichText(optString("text")))
        else -> return null
    }
    return EditorBlock.TextBlock(
        id = id,
        type = BasicBlockType.PARAGRAPH.storageValue,
        children = children
    )
}
private fun List<RichText>.toRichTextJson(): JSONArray = JSONArray().also { array ->
    forEach { richText ->
        array.put(
            JSONObject()
                .put("text", richText.text)
                .put("marks", JSONArray().also { marks -> richText.marks.forEach(marks::put) })
        )
    }
}
private fun JSONArray?.toRichTextList(): List<RichText> {
    if (this == null || length() == 0) return listOf(RichText(""))
    return (0 until length()).mapNotNull { index ->
        optJSONObject(index)?.let { json ->
            RichText(
                text = json.optString("text"),
                marks = json.optJSONArray("marks").toStringList()
            )
        }
    }.ifEmpty { listOf(RichText("")) }
}
private fun List<List<List<RichText>>>.toRowsJson(): JSONArray = JSONArray().also { rowsArray ->
    forEach { row ->
        rowsArray.put(
            JSONArray().also { rowArray ->
                row.forEach { cell -> rowArray.put(cell.toRichTextJson()) }
            }
        )
    }
}
private fun JSONArray?.toRows(): List<List<List<RichText>>> {
    if (this == null || length() == 0) return defaultTableRows()
    return (0 until length()).map { rowIndex ->
        val row = optJSONArray(rowIndex) ?: JSONArray()
        (0 until row.length()).map { cellIndex ->
            row.optJSONArray(cellIndex).toRichTextList()
        }
    }.ifEmpty { defaultTableRows() }
}
private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return (0 until length()).mapNotNull { index -> optString(index).takeIf { it.isNotBlank() } }
}
private fun defaultTableRows(): List<List<List<RichText>>> = listOf(
    listOf(listOf(RichText("Name")), listOf(RichText("Age"))),
    listOf(listOf(RichText("")), listOf(RichText("")))
)

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
                result.add(RichText(child.text.substring(childStart, relativeSplit), child.marks))
                childStart = relativeSplit
            }
            offsetIndex++
        }

        if (childStart < childLength) {
            result.add(RichText(child.text.substring(childStart), child.marks))
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
        if (current.marks.sorted() == next.marks.sorted()) {
            current = RichText(current.text + next.text, current.marks)
        } else {
            result.add(current)
            current = next
        }
    }
    result.add(current)
    return result.filterNot { it.text.isEmpty() }.ifEmpty { listOf(RichText("")) }
}
