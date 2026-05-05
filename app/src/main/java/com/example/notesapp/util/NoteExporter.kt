package com.example.notesapp.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.editor.document.NoteDocument
import java.io.OutputStream

class NoteExporter(private val context: Context) {

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
        
        // Simple PDF generation
        // A4 size: 595 x 842 points
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        
        var y = 50f
        
        // Title
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText(note.title, 50f, y, paint)
        y += 40f
        
        // Content
        paint.textSize = 12f
        paint.isFakeBoldText = false
        
        val lines = document.toPlainText().lines()
        for (line in lines) {
            if (y > 800) break // Simple pagination limit for now
            canvas.drawText(line, 50f, y, paint)
            y += 20f
        }
        
        pdfDocument.finishPage(page)
        
        try {
            pdfDocument.writeTo(outputStream)
        } finally {
            pdfDocument.close()
            outputStream.close()
        }
    }
}
