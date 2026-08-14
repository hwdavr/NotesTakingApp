package com.example.notesapp.data.summary

import android.content.Context
import android.os.Build
import com.example.notesapp.di.IoDispatcher
import com.example.notesapp.domain.summary.NoteSummarizer
import com.example.notesapp.domain.summary.NoteSummary
import com.example.notesapp.domain.summary.NoteSummaryUnavailableException
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizationRequest
import com.google.mlkit.genai.summarization.SummarizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Singleton
class GeminiNanoNoteSummarizer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val config: GeminiNanoSummaryConfig,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NoteSummarizer {
    override suspend fun summarize(title: String, noteText: String): NoteSummary {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            throw NoteSummaryUnavailableException("Gemini Nano summarization requires API 26 or higher.")
        }

        val options = SummarizerOptions.builder(context)
            .setInputType(SummarizerOptions.InputType.ARTICLE)
            .setOutputType(SummarizerOptions.OutputType.ONE_BULLET)
            .setLanguage(SummarizerOptions.Language.ENGLISH)
            .build()
        val summarizer = Summarization.getClient(options)

        return try {
            val status = summarizer.checkFeatureStatus().awaitFuture()
            android.util.Log.d(TAG, "Gemini Nano summarization checkFeatureStatus=$status")
            when (status) {
                FeatureStatus.AVAILABLE -> {
                    android.util.Log.d(TAG, "Gemini Nano model is AVAILABLE")
                }
                FeatureStatus.DOWNLOADING -> {
                    android.util.Log.d(TAG, "Gemini Nano model is DOWNLOADING")
                    throw NoteSummaryUnavailableException("Gemini Nano model is still downloading.")
                }
                FeatureStatus.DOWNLOADABLE -> {
                    android.util.Log.d(TAG, "Gemini Nano model is DOWNLOADABLE; initiating download...")
                    runCatching {
                        summarizer.downloadFeature(object : com.google.mlkit.genai.common.DownloadCallback {
                            override fun onDownloadStarted(bytesToDownload: Long) {
                                android.util.Log.d(TAG, "Gemini Nano download started; bytes=$bytesToDownload")
                            }
                            override fun onDownloadFailed(e: com.google.mlkit.genai.common.GenAiException) {
                                android.util.Log.w(TAG, "Gemini Nano download failed", e)
                            }
                            override fun onDownloadProgress(totalBytesDownloaded: Long) {
                                android.util.Log.d(TAG, "Gemini Nano download progress; bytes=$totalBytesDownloaded")
                            }
                            override fun onDownloadCompleted() {
                                android.util.Log.d(TAG, "Gemini Nano download completed")
                            }
                        }).awaitFuture()
                    }
                    throw NoteSummaryUnavailableException("Gemini Nano model download initiated.")
                }
                else -> {
                    android.util.Log.w(TAG, "Gemini Nano model status UNAVAILABLE (status=$status)")
                    throw NoteSummaryUnavailableException("Gemini Nano summarization is unavailable on this device.")
                }
            }

            val inputText = if (title.isNotBlank()) {
                "$title\n\n$noteText"
            } else {
                noteText
            }
            val request = SummarizationRequest.builder(inputText.take(config.inputCharacterLimit)).build()
            val result = withContext(ioDispatcher) {
                kotlinx.coroutines.withTimeout(30_000L) {
                    android.util.Log.d(TAG, "Gemini Nano preparing inference engine...")
                    summarizer.prepareInferenceEngine().awaitFuture()
                    android.util.Log.d(TAG, "Gemini Nano inference engine prepared; running inference...")
                    summarizer.runInference(request).awaitFuture()
                }
            }
            android.util.Log.d(TAG, "Gemini Nano inference succeeded")
            NoteSummary(result.getSummary())
        } catch (exception: NoteSummaryUnavailableException) {
            android.util.Log.w(TAG, "Gemini Nano summarization unavailable: ${exception.message}")
            throw exception
        } catch (exception: Exception) {
            android.util.Log.e(TAG, "Gemini Nano summarization failed with exception", exception)
            throw NoteSummaryUnavailableException("Gemini Nano summarization failed.", exception)
        } finally {
            summarizer.close()
        }
    }

    private companion object {
        const val TAG = "NotesApp/GeminiNanoNoteSummarizer"
    }
}

private suspend fun <T> ListenableFuture<T>.awaitFuture(): T = suspendCancellableCoroutine { continuation ->
    addListener(
        {
            runCatching { get() }
                .onSuccess { result -> continuation.resume(result) }
                .onFailure { exception -> continuation.resumeWithException(exception) }
        },
        DirectExecutor
    )
    continuation.invokeOnCancellation {
        cancel(true)
    }
}

private object DirectExecutor : Executor {
    override fun execute(command: Runnable) {
        command.run()
    }
}
