package com.example.notesapp.data.summary

interface FolderTextEmbeddingClient {
    fun similarity(firstText: String, secondText: String): Double
}
