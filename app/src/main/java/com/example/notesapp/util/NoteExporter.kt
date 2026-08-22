package com.example.notesapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.notesapp.R
import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.editor.chart.ChartBitmapColors
import com.example.notesapp.ui.editor.chart.ChartBitmapRenderer
import com.example.notesapp.ui.editor.mapper.BasicBlockType
import com.example.notesapp.ui.editor.mapper.ChartTableParser
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.basicBlockType
import com.example.notesapp.ui.editor.mapper.chartAssetPath
import com.example.notesapp.ui.editor.mapper.headingLevel
import com.example.notesapp.ui.editor.mapper.normalized
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class NoteExporter(private val context: Context) {
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 50f
    private val contentWidth = pageWidth - 2 * margin
    fun exportToMarkdown(note: Note, outputStream: OutputStream) {
        val document = NoteDocument.fromContent(note.content)
        val chartBlocks = document.blocks.filterIsInstance<EditorBlock.ChartBlock>()
        if (chartBlocks.isEmpty()) {
            val markdown = "# ${note.title}\n\n${document.toMarkdown()}"
            outputStream.use { stream ->
                stream.write(markdown.toByteArray(StandardCharsets.UTF_8))
            }
            return
        }

        val renderedAssets = chartBlocks.associate { block ->
            block.id to chartBitmapForExport(block, width = contentWidth.toInt(), height = 240)?.let(::encodePng)
        }
        val imageAvailability = renderedAssets.mapValues { (_, bytes) -> bytes != null }
        val markdown = "# ${note.title}\n\n${document.toMarkdown(
            chartImageAvailability = imageAvailability,
            chartImageFailureMessage = context.getString(R.string.chart_export_fallback_message)
        )}"
        ZipOutputStream(outputStream).use { archive ->
            writeZipEntry(archive, "note.md", markdown.toByteArray(StandardCharsets.UTF_8))
            chartBlocks.forEach { block ->
                renderedAssets[block.id]?.let { bytes ->
                    writeZipEntry(archive, chartAssetPath(block.id), bytes)
                }
            }
        }
    }
    fun exportToPdf(note: Note, outputStream: OutputStream) {
        val document = NoteDocument.fromContent(note.content)
        val pdfDocument = PdfDocument()
        val renderer = PdfRenderer(context, pdfDocument, pageWidth, pageHeight, margin)
        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }
        val textPaint = TextPaint(paint)
        val borderPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        // Title
        textPaint.textSize = 24f
        textPaint.isFakeBoldText = true
        val titleLayout = StaticLayout.Builder.obtain(note.title, 0, note.title.length, textPaint, contentWidth.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()
        renderer.ensureSpace(titleLayout.height.toFloat())
        renderer.canvas.save()
        renderer.canvas.translate(margin, renderer.currentY)
        titleLayout.draw(renderer.canvas)
        renderer.canvas.restore()
        renderer.currentY += titleLayout.height + 20f
        // Blocks
        for (block in document.blocks) {
            when (block) {
                is EditorBlock.TextBlock -> renderTextBlock(block, renderer, textPaint)
                is EditorBlock.ImageBlock -> {
                    val bitmap = try {
                        loadBitmap(block.url)
                    } catch (e: Exception) {
                        android.util.Log.e("NoteExporter", "Failed to load image: ${block.url}", e)
                        null
                    }
                    if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
                        val scale = contentWidth / bitmap.width
                        val h = bitmap.height * scale
                        renderer.ensureSpace(h + 20f)
                        val destRect = RectF(margin, renderer.currentY, margin + contentWidth, renderer.currentY + h)
                        renderer.canvas.drawBitmap(bitmap, null, destRect, paint)
                        renderer.currentY += h + 10f
                        if (block.caption.isNotBlank()) {
                            textPaint.textSize = 10f
                            textPaint.isFakeBoldText = false
                            textPaint.color = Color.GRAY
                            val captionLayout = StaticLayout.Builder.obtain(
                                block.caption,
                                0,
                                block.caption.length,
                                textPaint,
                                contentWidth.toInt()
                            )
                                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                                .build()
                            renderer.ensureSpace(captionLayout.height.toFloat())
                            renderer.canvas.save()
                            renderer.canvas.translate(margin, renderer.currentY)
                            captionLayout.draw(renderer.canvas)
                            renderer.canvas.restore()
                            renderer.currentY += captionLayout.height + 10f
                            textPaint.color = Color.BLACK
                        }
                    } else {
                        // Draw error placeholder
                        renderer.ensureSpace(60f)
                        val errorPaint = Paint().apply {
                            color = Color.RED
                            textSize = 10f
                            isAntiAlias = true
                        }
                        renderer.canvas.drawRect(
                            margin,
                            renderer.currentY,
                            margin + contentWidth,
                            renderer.currentY + 50f,
                            borderPaint
                        )
                        renderer.canvas.drawText(
                            "Image Load Error: ${if (block.url.isBlank()) "Empty URL" else "Invalid image or format"}",
                            margin + 10f,
                            renderer.currentY + 20f,
                            errorPaint
                        )
                        renderer.canvas.drawText(
                            "URL: ${block.url.take(60)}${if (block.url.length > 60) "..." else ""}",
                            margin + 10f,
                            renderer.currentY + 40f,
                            errorPaint
                        )
                        renderer.currentY += 60f
                    }
                }
                is EditorBlock.TableBlock -> {
                    renderTable(block, renderer, textPaint, borderPaint)
                }
                is EditorBlock.MermaidBlock -> {
                    renderMermaidBlock(block, renderer, textPaint)
                }
                is EditorBlock.CodeBlock -> {
                    renderCodeBlock(block, renderer, textPaint, borderPaint)
                }
                is EditorBlock.ChartBlock -> {
                    renderChartBlock(block, renderer, textPaint, borderPaint)
                }
                is EditorBlock.Voice -> {
                    renderer.currentY += 10f
                }
            }
        }
        pdfDocument.finishPage(renderer.currentPage)
        try {
            pdfDocument.writeTo(outputStream)
        } finally {
            pdfDocument.close()
            outputStream.close()
        }
    }

    private fun renderCodeBlock(
        block: EditorBlock.CodeBlock,
        renderer: PdfRenderer,
        textPaint: TextPaint,
        borderPaint: Paint
    ) {
        textPaint.textSize = 12f
        textPaint.isFakeBoldText = true
        val language = block.language.ifBlank { "Code" }
        val headerLayout = StaticLayout.Builder.obtain(
            language,
            0,
            language.length,
            textPaint,
            contentWidth.toInt()
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()
        renderer.ensureSpace(headerLayout.height + 10f)
        renderer.canvas.save()
        renderer.canvas.translate(margin, renderer.currentY)
        headerLayout.draw(renderer.canvas)
        renderer.canvas.restore()
        renderer.currentY += headerLayout.height + 6f

        textPaint.textSize = 10f
        textPaint.isFakeBoldText = false
        textPaint.typeface = Typeface.MONOSPACE
        val code = block.code.ifBlank { "// Enter code here..." }
        val codeLayout = StaticLayout.Builder.obtain(
            code,
            0,
            code.length,
            textPaint,
            contentWidth.toInt()
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()
        val boxHeight = codeLayout.height + 20f
        renderer.ensureSpace(boxHeight + 10f)
        renderer.canvas.drawRect(
            margin,
            renderer.currentY,
            margin + contentWidth,
            renderer.currentY + boxHeight,
            borderPaint
        )
        renderer.canvas.save()
        renderer.canvas.translate(margin + 10f, renderer.currentY + 10f)
        codeLayout.draw(renderer.canvas)
        renderer.canvas.restore()
        renderer.currentY += boxHeight + 10f
        textPaint.typeface = Typeface.DEFAULT
    }

    private fun renderMermaidBlock(block: EditorBlock.MermaidBlock, renderer: PdfRenderer, textPaint: TextPaint) {
        textPaint.textSize = 14f
        textPaint.isFakeBoldText = true
        val titleText = block.title.ifBlank { "Mermaid Diagram" }
        val titleLayout = StaticLayout.Builder.obtain(
            titleText,
            0,
            titleText.length,
            textPaint,
            contentWidth.toInt()
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()
        renderer.ensureSpace(titleLayout.height + 10f)
        renderer.canvas.save()
        renderer.canvas.translate(margin, renderer.currentY)
        titleLayout.draw(renderer.canvas)
        renderer.canvas.restore()
        renderer.currentY += titleLayout.height + 6f

        textPaint.textSize = 10f
        textPaint.isFakeBoldText = false
        val codeLayout = StaticLayout.Builder.obtain(
            block.code,
            0,
            block.code.length,
            textPaint,
            contentWidth.toInt()
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()
        renderer.ensureSpace(codeLayout.height + 15f)
        renderer.canvas.save()
        renderer.canvas.translate(margin, renderer.currentY)
        codeLayout.draw(renderer.canvas)
        renderer.canvas.restore()
        renderer.currentY += codeLayout.height + 15f
    }

    private fun renderChartBlock(
        block: EditorBlock.ChartBlock,
        renderer: PdfRenderer,
        textPaint: TextPaint,
        borderPaint: Paint
    ) {
        textPaint.textSize = 14f
        textPaint.isFakeBoldText = true
        val title = block.title.ifBlank { "Chart" }
        val titleLayout = StaticLayout.Builder.obtain(title, 0, title.length, textPaint, contentWidth.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()
        renderer.ensureSpace(titleLayout.height + 10f)
        renderer.canvas.save()
        renderer.canvas.translate(margin, renderer.currentY)
        titleLayout.draw(renderer.canvas)
        renderer.canvas.restore()
        renderer.currentY += titleLayout.height + 8f
        val bitmap = chartBitmapForExport(block, width = contentWidth.toInt(), height = 240)
        if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
            renderer.ensureSpace(bitmap.height.toFloat() + 16f)
            renderer.canvas.drawBitmap(
                bitmap,
                null,
                RectF(margin, renderer.currentY, margin + contentWidth, renderer.currentY + bitmap.height),
                Paint(Paint.ANTI_ALIAS_FLAG)
            )
            renderer.currentY += bitmap.height + 16f
        } else {
            renderChartFallbackMessage(renderer, textPaint)
            renderTable(
                EditorBlock.TableBlock(id = block.id, rows = block.normalized().rows),
                renderer,
                textPaint,
                borderPaint
            )
        }
    }

    private fun renderChartFallbackMessage(renderer: PdfRenderer, textPaint: TextPaint) {
        val message = context.getString(R.string.chart_export_fallback_message)
        textPaint.textSize = 11f
        textPaint.isFakeBoldText = false
        textPaint.color = Color.DKGRAY
        val layout = StaticLayout.Builder.obtain(
            message,
            0,
            message.length,
            textPaint,
            contentWidth.toInt()
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()
        renderer.ensureSpace(layout.height + 8f)
        renderer.canvas.save()
        renderer.canvas.translate(margin, renderer.currentY)
        layout.draw(renderer.canvas)
        renderer.canvas.restore()
        renderer.currentY += layout.height + 8f
        textPaint.color = Color.BLACK
    }

    internal fun chartBitmapForExport(block: EditorBlock.ChartBlock, width: Int, height: Int): Bitmap? {
        if (ChartTableParser.parse(block).points.isEmpty()) return null
        return runCatching {
            ChartBitmapRenderer.render(
                block = block,
                width = width.coerceAtLeast(1),
                height = height.coerceAtLeast(1),
                colors = ChartBitmapColors(
                    background = Color.WHITE,
                    primary = Color.rgb(124, 108, 242),
                    text = Color.BLACK,
                    grid = Color.LTGRAY
                )
            )
        }.getOrNull()
    }

    private fun encodePng(bitmap: Bitmap): ByteArray? {
        return ByteArrayOutputStream().use { buffer ->
            val compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, buffer)
            bitmap.recycle()
            if (compressed) buffer.toByteArray() else null
        }
    }

    private fun writeZipEntry(archive: ZipOutputStream, path: String, bytes: ByteArray) {
        archive.putNextEntry(ZipEntry(path))
        archive.write(bytes)
        archive.closeEntry()
    }

    private fun renderTextBlock(block: EditorBlock.TextBlock, renderer: PdfRenderer, textPaint: TextPaint) {
        val text = block.children.joinToString("") { it.text }
        if (text.isBlank()) {
            renderer.currentY += 10f
            return
        }
        val blockType = block.basicBlockType()
        textPaint.isFakeBoldText = blockType.headingLevel() != null
        textPaint.textSize = blockType.pdfTextSize()
        val fullText = block.pdfPrefix(blockType) + text
        val layout = StaticLayout.Builder.obtain(
            fullText,
            0,
            fullText.length,
            textPaint,
            contentWidth.toInt()
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()
        renderer.ensureSpace(layout.height.toFloat())
        renderer.canvas.save()
        renderer.canvas.translate(margin, renderer.currentY)
        layout.draw(renderer.canvas)
        renderer.canvas.restore()
        renderer.currentY += layout.height + 10f
    }

    private fun BasicBlockType.pdfTextSize(): Float = when (this) {
        BasicBlockType.HEADING_1 -> 24f
        BasicBlockType.HEADING_2 -> 20f
        BasicBlockType.HEADING_3 -> 18f
        BasicBlockType.HEADING_4 -> 16f
        else -> 12f
    }

    private fun EditorBlock.TextBlock.pdfPrefix(blockType: BasicBlockType): String = when (blockType) {
        BasicBlockType.BULLETED_LIST -> "• "
        BasicBlockType.NUMBERED_LIST -> "1. "
        BasicBlockType.TODO_LIST -> if (checked) "[x] " else "[ ] "
        BasicBlockType.TOGGLE_LIST -> if (isExpanded) "▼ " else "▶ "
        BasicBlockType.CALLOUT -> "! "
        BasicBlockType.QUOTE -> "| "
        else -> ""
    }

    private fun loadBitmap(url: String): Bitmap? {
        if (url.isBlank()) return null
        val uri = Uri.parse(url)
        val inputStream = try {
            when {
                uri.scheme?.startsWith("http") == true -> {
                    java.net.URL(url).openConnection().apply {
                        connectTimeout = 10000
                        readTimeout = 10000
                    }.getInputStream()
                }
                uri.scheme == "content" || uri.scheme == "android.resource" -> {
                    context.contentResolver.openInputStream(uri)
                }
                else -> {
                    // Try as file path or file URI
                    val path = if (url.startsWith("file://")) url.substring(7) else url
                    java.io.FileInputStream(path)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("NoteExporter", "Could not open stream for $url", e)
            null
        }
        return inputStream?.use {
            BitmapFactory.decodeStream(it)
        }
    }
    private fun renderTable(
        block: EditorBlock.TableBlock,
        renderer: PdfRenderer,
        textPaint: TextPaint,
        borderPaint: Paint
    ) {
        val rows = block.rows
        if (rows.isEmpty()) return
        val colCount = rows.maxOf { it.size }
        if (colCount == 0) return
        val colWidth = contentWidth / colCount
        val cellPadding = 5f
        textPaint.textSize = 10f
        textPaint.isFakeBoldText = false
        for (row in rows) {
            // Calculate row height
            val layouts = row.map { cell ->
                val cellText = cell.joinToString("") { it.text }
                StaticLayout.Builder.obtain(
                    cellText,
                    0,
                    cellText.length,
                    textPaint,
                    (colWidth - 2 * cellPadding).toInt()
                )
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .build()
            }
            val rowHeight = (layouts.maxOfOrNull { it.height } ?: 0) + 2 * cellPadding
            renderer.ensureSpace(rowHeight)
            // Draw row cells
            for ((index, layout) in layouts.withIndex()) {
                val x = margin + index * colWidth
                // Cell border
                renderer.canvas.drawRect(x, renderer.currentY, x + colWidth, renderer.currentY + rowHeight, borderPaint)
                // Cell text
                renderer.canvas.save()
                renderer.canvas.translate(x + cellPadding, renderer.currentY + cellPadding)
                layout.draw(renderer.canvas)
                renderer.canvas.restore()
            }
            renderer.currentY += rowHeight
        }
        renderer.currentY += 10f
    }
    private class PdfRenderer(
        val context: Context,
        val pdfDocument: PdfDocument,
        val pageWidth: Int,
        val pageHeight: Int,
        val margin: Float
    ) {
        var pageNumber = 1
        lateinit var currentPage: PdfDocument.Page
        lateinit var canvas: Canvas
        var currentY = margin
        val contentWidth = pageWidth - 2 * margin
        init {
            startNextPage()
        }
        fun startNextPage() {
            if (::currentPage.isInitialized) {
                pdfDocument.finishPage(currentPage)
            }
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
            currentPage = pdfDocument.startPage(pageInfo)
            canvas = currentPage.canvas
            currentY = margin
        }
        fun ensureSpace(height: Float) {
            if (currentY + height > pageHeight - margin) {
                startNextPage()
            }
        }
    }
}
