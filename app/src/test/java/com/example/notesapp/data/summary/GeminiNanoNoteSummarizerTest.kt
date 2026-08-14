package com.example.notesapp.data.summary

import android.content.Context
import com.example.notesapp.domain.summary.NoteSummaryUnavailableException
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizationResult
import com.google.mlkit.genai.summarization.Summarizer
import com.google.mlkit.genai.summarization.SummarizerOptions
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GeminiNanoNoteSummarizerTest {

    private val context: Context = mockk(relaxed = true)
    private val clientMock: Summarizer = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        mockkStatic(Summarization::class)
        every { Summarization.getClient(any<SummarizerOptions>()) } returns clientMock
    }

    @After
    fun tearDown() {
        unmockkStatic(Summarization::class)
    }

    @Test
    fun `given feature status available when summarize then returns summary`() = runTest(testDispatcher) {
        val availableFuture: ListenableFuture<Int> =
            Futures.immediateFuture(FeatureStatus.AVAILABLE)
        val resultMock: SummarizationResult = mockk()
        every { resultMock.getSummary() } returns "This is a summary."
        val inferenceFuture: ListenableFuture<SummarizationResult> =
            Futures.immediateFuture(resultMock)

        every { clientMock.checkFeatureStatus() } returns availableFuture
        every { clientMock.prepareInferenceEngine() } returns Futures.immediateFuture(null)
        every { clientMock.runInference(any()) } returns inferenceFuture

        val summarizer = GeminiNanoNoteSummarizer(
            context = context,
            config = GeminiNanoSummaryConfig(),
            ioDispatcher = testDispatcher
        )

        val result = summarizer.summarize("Title", "Note content for testing AI summarization.")

        assertEquals("This is a summary.", result.text)
    }

    @Test
    fun `given feature status downloading when summarize then throws exception`() = runTest(testDispatcher) {
        val downloadingFuture: ListenableFuture<Int> =
            Futures.immediateFuture(FeatureStatus.DOWNLOADING)
        every { clientMock.checkFeatureStatus() } returns downloadingFuture

        val summarizer = GeminiNanoNoteSummarizer(
            context = context,
            config = GeminiNanoSummaryConfig(),
            ioDispatcher = testDispatcher
        )

        val exception = assertThrows(NoteSummaryUnavailableException::class.java) {
            runTest(testDispatcher) {
                summarizer.summarize("Title", "Note content for testing AI summarization.")
            }
        }

        assertEquals("Gemini Nano model is still downloading.", exception.message)
    }

    @Test
    fun `given feature status downloadable when summarize then initiates download and throws exception`() =
        runTest(testDispatcher) {
            val downloadableFuture: ListenableFuture<Int> =
                Futures.immediateFuture(FeatureStatus.DOWNLOADABLE)
            val downloadFuture: ListenableFuture<Void> =
                Futures.immediateFuture(null)
            every { clientMock.checkFeatureStatus() } returns downloadableFuture
            every { clientMock.downloadFeature(any()) } returns downloadFuture

            val summarizer = GeminiNanoNoteSummarizer(
                context = context,
                config = GeminiNanoSummaryConfig(),
                ioDispatcher = testDispatcher
            )

            val exception = assertThrows(NoteSummaryUnavailableException::class.java) {
                runTest(testDispatcher) {
                    summarizer.summarize("Title", "Note content for testing AI summarization.")
                }
            }

            assertEquals("Gemini Nano model download initiated.", exception.message)
        }

    @Test
    fun `given feature status unavailable when summarize then throws exception`() = runTest(testDispatcher) {
        val unavailableFuture: ListenableFuture<Int> =
            Futures.immediateFuture(FeatureStatus.UNAVAILABLE)
        every { clientMock.checkFeatureStatus() } returns unavailableFuture

        val summarizer = GeminiNanoNoteSummarizer(
            context = context,
            config = GeminiNanoSummaryConfig(),
            ioDispatcher = testDispatcher
        )

        val exception = assertThrows(NoteSummaryUnavailableException::class.java) {
            runTest(testDispatcher) {
                summarizer.summarize("Title", "Note content for testing AI summarization.")
            }
        }

        assertEquals("Gemini Nano summarization is unavailable on this device.", exception.message)
    }
}
