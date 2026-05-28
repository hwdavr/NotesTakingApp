package com.example.notesapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import java.io.OutputStream

class NoteExporter(private val context: Context) {
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 50f
    private val contentWidth = pageWidth - 2 * margin
    fun exportToMarkdown(note: Note, outputStream: OutputStream) {
        val document = NoteDocument.fromContent(note.content)
        val markdown = "# ${note.title}\n\n${document.toMarkdown()}"
        outputStream.use {
            it.write(markdown.toByteArray())
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
                is EditorBlock.TextBlock -> {
                    val text = block.children.joinToString("") { it.text }
                    if (text.isBlank()) {
                        renderer.currentY += 10f
                        continue
                    }
                    textPaint.isFakeBoldText = block.type == "heading"
                    textPaint.textSize = if (block.type == "heading") 18f else 12f
                    val prefix = if (block.type == "bulleted") "• " else ""
                    val fullText = prefix + text
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
