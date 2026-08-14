package com.example.notesapp.domain.summary

import com.example.notesapp.domain.summary.usecase.SummarizeNoteUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SummarizeNoteUseCaseTest {
    @Test
    fun `blank note returns empty without invoking summarizer`() = runTest {
        val summarizer = RecordingNoteSummarizer()
        val useCase = SummarizeNoteUseCase(summarizer)

        val result = useCase("", "")

        assertEquals(NoteSummaryResult.Empty, result)
        assertTrue(summarizer.inputs.isEmpty())
    }

    @Test
    fun `non empty note returns summary from summarizer`() = runTest {
        val summarizer = RecordingNoteSummarizer(summaryText = "A concise note summary.")
        val useCase = SummarizeNoteUseCase(summarizer)

        val result = useCase("Test Note Title", longNoteText())

        assertEquals(
            NoteSummaryResult.Success(NoteSummary("A concise note summary.")),
            result
        )
    }

    @Test
    fun `long note input is capped before summarizer call`() = runTest {
        val summarizer = RecordingNoteSummarizer()
        val useCase = SummarizeNoteUseCase(summarizer)

        useCase("Test Title", "A".repeat(13_000))

        assertEquals(12_000, summarizer.inputs.single().second.length)
    }

    @Test
    fun `summarizer failure returns unavailable`() = runTest {
        val summarizer = RecordingNoteSummarizer(failure = NoteSummaryUnavailableException())
        val useCase = SummarizeNoteUseCase(summarizer)

        val result = useCase("Test Note Title", longNoteText())

        assertEquals(NoteSummaryResult.Unavailable, result)
    }

    private class RecordingNoteSummarizer(
        private val summaryText: String = "Summary",
        private val failure: Throwable? = null
    ) : NoteSummarizer {
        val inputs = mutableListOf<Pair<String, String>>()

        override suspend fun summarize(title: String, noteText: String): NoteSummary {
            inputs += title to noteText
            failure?.let { throw it }
            return NoteSummary(summaryText)
        }
    }

    private fun longNoteText(): String = List(80) { index ->
        "Paragraph $index captures planning notes, decisions, follow ups, and context for the editor summary feature."
    }.joinToString(separator = " ")
}
