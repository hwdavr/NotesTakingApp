package com.example.notesapp.data.summary

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder.TextEmbedderOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaPipeFolderTextEmbeddingClient @Inject constructor(
    @ApplicationContext private val context: Context
) : FolderTextEmbeddingClient {

    private val lock = Any()
    private var textEmbedder: TextEmbedder? = null

    override fun similarity(firstText: String, secondText: String): Double {
        return synchronized(lock) {
            val embedder = textEmbedder ?: createTextEmbedder().also { createdEmbedder ->
                textEmbedder = createdEmbedder
            }
            val firstEmbedding = embedder.embed(firstText).embeddingResult().embeddings().first()
            val secondEmbedding = embedder.embed(secondText).embeddingResult().embeddings().first()
            TextEmbedder.cosineSimilarity(firstEmbedding, secondEmbedding)
        }
    }

    private fun createTextEmbedder(): TextEmbedder {
        Log.d(TAG, "Loading MediaPipe text embedder model")
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(MODEL_ASSET_PATH)
            .build()
        val options = TextEmbedderOptions.builder()
            .setBaseOptions(baseOptions)
            .build()
        return TextEmbedder.createFromOptions(context, options)
    }

    private companion object {
        const val TAG = "NotesApp/MediaPipeFolderTextEmbeddingClient"
        const val MODEL_ASSET_PATH = "universal_sentence_encoder.tflite"
    }
}
