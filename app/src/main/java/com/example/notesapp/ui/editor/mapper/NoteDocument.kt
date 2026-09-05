package com.example.notesapp.ui.editor.mapper

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
const val DEFAULT_CHART_TITLE = "Chart"

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
            is EditorBlock.TextBlock -> block.children.joinToString("") { it.readableText() }
            is EditorBlock.ImageBlock -> listOf(block.url, block.caption).filter { it.isNotBlank() }.joinToString(" ")
            is EditorBlock.TableBlock -> block.rows.joinToString("\n") { row ->
                row.joinToString("\t") { cell -> cell.joinToString("") { it.text } }
            }
            is EditorBlock.MermaidBlock -> listOf(block.title, block.code).filter { it.isNotBlank() }.joinToString("\n")
            is EditorBlock.CodeBlock -> block.code
            is EditorBlock.ChartBlock -> block.rows.joinToString("\n") { row ->
                row.joinToString("\t") { cell -> cell.joinToString("") { it.text } }
            }
            is EditorBlock.Voice -> ""
        }
    }.trim()
    fun toMarkdown(
        chartImageAvailability: Map<String, Boolean> = emptyMap(),
        chartImageFailureMessage: String = ""
    ): String = blocks.joinToString("\n\n") { block ->
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
                    var text = richText.formulaSource?.let { "\$${it}\$" } ?: richText.text
                    if ("bold" in richText.marks) text = "**$text**"
                    if ("italic" in richText.marks) text = "*$text*"
                    if ("code" in richText.marks) text = "`$text`"
                    richText.linkTargetId?.takeIf { it.isNotBlank() }?.let { targetId ->
                        text = "[$text](notesapp://note/$targetId)"
                    }
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
            is EditorBlock.ChartBlock -> block.toMarkdown(
                includeImage = chartImageAvailability[block.id] != false,
                imageFailureMessage = chartImageFailureMessage
            )
            is EditorBlock.Voice -> ""
        }
    }.trim()
    fun ensureEditableTextBlock(): NoteDocument {
        if (blocks.any { it is EditorBlock.TextBlock }) return this
        return copy(blocks = listOf(EditorBlock.TextBlock()) + blocks)
    }

    fun resolveLinks(activeTargetIds: Set<String>, deletedTargetIds: Set<String>): NoteDocument = copy(
        blocks = blocks.map { block ->
            when (block) {
                is EditorBlock.TextBlock -> block.copy(
                    children = block.children.resolveLinks(activeTargetIds, deletedTargetIds)
                )
                is EditorBlock.TableBlock -> block.copy(
                    rows = block.rows.map { row ->
                        row.map { cell ->
                            cell.resolveLinks(activeTargetIds, deletedTargetIds)
                        }
                    }
                )
                else -> block
            }
        }
    )

    fun hasLinkAnnotations(): Boolean = blocks.any { block ->
        when (block) {
            is EditorBlock.TextBlock -> block.children.any { !it.linkTargetId.isNullOrBlank() }
            is EditorBlock.TableBlock -> block.rows.any { row ->
                row.any { cell -> cell.any { !it.linkTargetId.isNullOrBlank() } }
            }
            else -> false
        }
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
    data class ChartBlock(
        override val id: String = newBlockId(),
        val chartType: ChartType = ChartType.BAR,
        val title: String = DEFAULT_CHART_TITLE,
        val rows: List<List<List<RichText>>> = defaultChartRows(),
        val columnIds: List<String> = defaultChartColumnIds(),
        val selectedColumnId: String = defaultChartColumnIds().last()
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
    val marks: List<String> = emptyList(),
    val linkTargetId: String? = null,
    val formulaSource: String? = null,
    val inlineId: String? = null
) {
    val isFormula: Boolean
        get() = !formulaSource.isNullOrBlank()
}
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
    is EditorBlock.ChartBlock -> JSONObject()
        .put("id", id)
        .put("type", "chart")
        .put("chartType", chartType.storageValue)
        .put("title", title)
        .put("columnIds", JSONArray().also { ids -> columnIds.forEach(ids::put) })
        .put("selectedColumnId", selectedColumnId)
        .put("rows", rows.toRowsJson())
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
        "chart" -> {
            val rows = optJSONArray("rows").toRows()
            val columnIds = optJSONArray("columnIds").toStringList()
            val normalizedColumnIds = normalizeChartColumnIds(columnIds, rows)
            EditorBlock.ChartBlock(
                id = id,
                chartType = ChartType.fromStorageValue(optString("chartType")),
                title = optString("title", DEFAULT_CHART_TITLE).ifBlank { DEFAULT_CHART_TITLE },
                rows = normalizeChartRows(rows, normalizedColumnIds.size),
                columnIds = normalizedColumnIds,
                selectedColumnId = resolveSelectedChartColumn(
                    selectedColumnId = optString("selectedColumnId"),
                    columnIds = normalizedColumnIds
                )
            )
        }
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
                .also { json ->
                    richText.linkTargetId?.takeIf { it.isNotBlank() }?.let { targetId ->
                        json.put("linkTargetId", targetId)
                    }
                    richText.formulaSource?.takeIf { it.isNotBlank() }?.let { source ->
                        json.put("formulaSource", source)
                    }
                    richText.inlineId?.takeIf { it.isNotBlank() }?.let { inlineId ->
                        json.put("inlineId", inlineId)
                    }
                }
        )
    }
}
private fun JSONArray?.toRichTextList(): List<RichText> {
    if (this == null || length() == 0) return listOf(RichText(""))
    return (0 until length()).mapNotNull { index ->
        optJSONObject(index)?.let { json ->
            RichText(
                text = json.optString("text"),
                marks = json.optJSONArray("marks").toStringList(),
                linkTargetId = json.optString("linkTargetId").takeIf { it.isNotBlank() },
                formulaSource = json.optString("formulaSource").takeIf { it.isNotBlank() },
                inlineId = json.optString("inlineId").takeIf { it.isNotBlank() }
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

private fun defaultChartRows(): List<List<List<RichText>>> = listOf(
    listOf(listOf(RichText("Category")), listOf(RichText("Value")))
)

private fun defaultChartColumnIds(): List<String> = listOf("c_category", "c_value")

private fun normalizeChartColumnIds(columnIds: List<String>, rows: List<List<List<RichText>>>): List<String> {
    val columnCount = maxOf(2, columnIds.size, rows.maxOfOrNull { it.size } ?: 0)
    val existing = columnIds.mapIndexed { index, value ->
        value.ifBlank { "c_column_${index + 1}" }
    }
    return (0 until columnCount).map { index ->
        existing.getOrNull(index) ?: if (index == 0) "c_category" else "c_column_${index + 1}"
    }.distinct().let { ids ->
        if (ids.size >= columnCount) {
            ids.take(
                columnCount
            )
        } else {
            ids + (ids.size until columnCount).map { "c_column_${it + 1}" }
        }
    }
}

private fun normalizeChartRows(rows: List<List<List<RichText>>>, columnCount: Int): List<List<List<RichText>>> {
    val sourceRows = rows.ifEmpty { defaultChartRows() }
    return sourceRows.map { row ->
        (0 until columnCount).map { index -> row.getOrNull(index) ?: listOf(RichText("")) }
    }
}

private fun resolveSelectedChartColumn(selectedColumnId: String, columnIds: List<String>): String {
    return selectedColumnId.takeIf { it in columnIds.drop(1) } ?: columnIds.getOrElse(1) { "c_value" }
}

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

fun List<RichText>.applyLinkToRange(start: Int, end: Int, targetId: String?): List<RichText> {
    val textLength = sumOf { it.text.length }
    val rangeStart = minOf(start, end).coerceIn(0, textLength)
    val rangeEnd = maxOf(start, end).coerceIn(0, textLength)
    if (rangeStart == rangeEnd) return this

    val splitChildren = splitAtOffsets(listOf(rangeStart, rangeEnd))
    var currentOffset = 0
    return splitChildren.map { child ->
        val childStart = currentOffset
        val childEnd = childStart + child.text.length
        currentOffset = childEnd
        if (childStart >= rangeStart && childEnd <= rangeEnd) {
            child.copy(linkTargetId = targetId?.takeIf { it.isNotBlank() })
        } else {
            child
        }
    }.mergeAdjacentWithSameMarks()
}

fun List<RichText>.hasLinkInRange(start: Int, end: Int): Boolean {
    val textLength = sumOf { it.text.length }
    val rangeStart = minOf(start, end).coerceIn(0, textLength)
    val rangeEnd = maxOf(start, end).coerceIn(0, textLength)
    var currentOffset = 0
    return any { child ->
        val childStart = currentOffset
        val childEnd = childStart + child.text.length
        currentOffset = childEnd
        val overlaps = if (rangeStart == rangeEnd) {
            rangeStart in childStart until childEnd ||
                (rangeStart == textLength && childEnd == textLength)
        } else {
            childStart < rangeEnd && childEnd > rangeStart
        }
        overlaps && !child.linkTargetId.isNullOrBlank()
    }
}

fun List<RichText>.insertLinkedText(offset: Int, text: String, targetId: String): List<RichText> {
    if (text.isEmpty() || targetId.isBlank()) return this
    val insertionOffset = offset.coerceIn(0, sumOf { it.text.length })
    val updatedChildren = mutableListOf<RichText>()
    var currentOffset = 0
    var inserted = false

    for (child in this) {
        val childStart = currentOffset
        val childEnd = childStart + child.text.length
        if (!inserted && insertionOffset in childStart..childEnd) {
            val relativeOffset = (insertionOffset - childStart).coerceIn(0, child.text.length)
            if (relativeOffset > 0) {
                updatedChildren += child.copy(text = child.text.substring(0, relativeOffset))
            }
            updatedChildren += RichText(text = text, linkTargetId = targetId)
            if (relativeOffset < child.text.length) {
                updatedChildren += child.copy(text = child.text.substring(relativeOffset))
            }
            inserted = true
        } else {
            updatedChildren += child
        }
        currentOffset = childEnd
    }

    if (!inserted) {
        updatedChildren += RichText(text = text, linkTargetId = targetId)
    }
    return updatedChildren.mergeAdjacentWithSameMarks()
}

fun List<RichText>.removeLinkAtOffset(offset: Int): List<RichText> {
    if (isEmpty()) return this
    val textLength = sumOf { it.text.length }
    val targetOffset = offset.coerceIn(0, textLength)
    var currentOffset = 0
    return map { child ->
        val childStart = currentOffset
        val childEnd = childStart + child.text.length
        currentOffset = childEnd
        val containsOffset = targetOffset in childStart until childEnd ||
            (targetOffset == textLength && childEnd == textLength)
        if (containsOffset) child.copy(linkTargetId = null) else child
    }.mergeAdjacentWithSameMarks()
}

private fun List<RichText>.resolveLinks(activeTargetIds: Set<String>, deletedTargetIds: Set<String>): List<RichText> =
    flatMap { child ->
        val targetId = child.linkTargetId
        when {
            targetId.isNullOrBlank() -> listOf(child)
            targetId in deletedTargetIds -> emptyList()
            targetId in activeTargetIds -> listOf(child)
            else -> listOf(child.copy(linkTargetId = null))
        }
    }.mergeAdjacentWithSameMarks()
