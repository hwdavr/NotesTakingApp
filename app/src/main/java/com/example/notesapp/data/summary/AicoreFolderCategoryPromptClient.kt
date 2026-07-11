package com.example.notesapp.data.summary

import android.content.Context
import android.util.Log
import com.google.ai.edge.aicore.DownloadCallback
import com.google.ai.edge.aicore.DownloadConfig
import com.google.ai.edge.aicore.GenerationConfig
import com.google.ai.edge.aicore.GenerativeAIException
import com.google.ai.edge.aicore.GenerativeModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AicoreFolderCategoryPromptClient @Inject constructor(
    @ApplicationContext private val context: Context
) : GeminiNanoFolderCategoryPromptClient {
    override suspend fun generateFolderCategory(prompt: String): String? {
        val executor = Executors.newSingleThreadExecutor()
        val config = GenerationConfig.Builder()
            .apply {
                this.context = this@AicoreFolderCategoryPromptClient.context
                workerExecutor = executor
                callbackExecutor = executor
                temperature = MODEL_TEMPERATURE
                candidateCount = MODEL_CANDIDATE_COUNT
                maxOutputTokens = MODEL_MAX_OUTPUT_TOKENS
            }
            .build()
        val downloadConfig = DownloadConfig(AicoreDownloadLogger())

        return try {
            GenerativeModel(config, downloadConfig).use { model ->
                Log.d(TAG, "AICore preparing inference engine")
                model.prepareInferenceEngine()
                Log.d(TAG, "AICore inference engine prepared")
                model.generateContent(prompt).text
            }
        } catch (e: GenerativeAIException) {
            Log.w(TAG, "AICore folder category generation failed; ${e.toAicoreDiagnosticMessage()}", e)
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "AICore folder category generation failed; ${e.toAicoreDiagnosticMessage()}", e)
            throw e
        } finally {
            executor.shutdown()
        }
    }

    private class AicoreDownloadLogger : DownloadCallback {
        override fun onDownloadDidNotStart(e: GenerativeAIException) {
            Log.w(TAG, "AICore model download did not start; ${e.toAicoreDiagnosticMessage()}")
        }

        override fun onDownloadPending() {
            Log.d(TAG, "AICore model download pending")
        }

        override fun onDownloadStarted(bytesToDownload: Long) {
            Log.d(TAG, "AICore model download started; bytesToDownload=$bytesToDownload")
        }

        override fun onDownloadFailed(failureStatus: String, e: GenerativeAIException) {
            Log.w(
                TAG,
                "AICore model download failed; status=$failureStatus; ${e.toAicoreDiagnosticMessage()}"
            )
        }

        override fun onDownloadProgress(totalBytesDownloaded: Long) {
            Log.d(TAG, "AICore model download progress; totalBytesDownloaded=$totalBytesDownloaded")
        }

        override fun onDownloadCompleted() {
            Log.d(TAG, "AICore model download completed")
        }
    }

    private companion object {
        const val TAG = "NotesApp/AicoreFolderCategoryPromptClient"
        const val MODEL_TEMPERATURE = 0.0f
        const val MODEL_CANDIDATE_COUNT = 1
        const val MODEL_MAX_OUTPUT_TOKENS = 16
    }
}
