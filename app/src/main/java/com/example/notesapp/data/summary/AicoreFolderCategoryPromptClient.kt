package com.example.notesapp.data.summary

import android.content.Context
import com.google.ai.edge.aicore.GenerationConfig
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

        return try {
            GenerativeModel(config).use { model ->
                model.generateContent(prompt).text
            }
        } finally {
            executor.shutdown()
        }
    }

    private companion object {
        const val MODEL_TEMPERATURE = 0.0f
        const val MODEL_CANDIDATE_COUNT = 1
        const val MODEL_MAX_OUTPUT_TOKENS = 16
    }
}
