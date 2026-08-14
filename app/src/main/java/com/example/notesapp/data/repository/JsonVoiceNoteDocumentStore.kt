package com.example.notesapp.data.repository

import com.example.notesapp.domain.voice.VoiceNoteBlock
import com.example.notesapp.domain.voice.VoiceNoteDocumentInsertion
import com.example.notesapp.domain.voice.VoiceNoteDocumentStore
import java.util.UUID
import javax.inject.Inject
import org.json.JSONArray
import org.json.JSONObject

class JsonVoiceNoteDocumentStore @Inject constructor() : VoiceNoteDocumentStore {
    override fun insertVoiceNote(content: String, insertion: VoiceNoteDocumentInsertion): String {
        val root = documentRoot(content)
        val blocks = root.optJSONArray("blocks") ?: JSONArray()
        val insertionIndex = insertion.focusedBlockId
            ?.let { focusedId ->
                (0 until blocks.length()).firstOrNull { index ->
                    blocks.optJSONObject(index)?.optString("id") == focusedId
                }?.plus(1)
            }
            ?: 0
        val newBlocks = JSONArray()
        for (index in 0 until blocks.length()) {
            if (index == insertionIndex) {
                newBlocks.put(insertion.block.toJson())
                newBlocks.put(transcriptToJson(insertion.transcript))
            }
            newBlocks.put(blocks.optJSONObject(index))
        }
        if (insertionIndex >= blocks.length()) {
            newBlocks.put(insertion.block.toJson())
            newBlocks.put(transcriptToJson(insertion.transcript))
        }
        return root.put("blocks", newBlocks).toString()
    }

    override fun updateAudioFilePath(content: String, blockId: String, audioFilePath: String?): String {
        val root = documentRoot(content)
        val blocks = root.optJSONArray("blocks") ?: JSONArray()
        val updatedBlocks = JSONArray()
        for (index in 0 until blocks.length()) {
            val block = blocks.optJSONObject(index)
            if (block?.optString("id") == blockId || block?.optString("blockId") == blockId) {
                block.put("audioFilePath", audioFilePath ?: JSONObject.NULL)
            }
            updatedBlocks.put(block)
        }
        return root.put("blocks", updatedBlocks).toString()
    }

    private fun documentRoot(content: String): JSONObject {
        val parsed = runCatching { JSONObject(content) }.getOrNull()
        if (parsed?.has("blocks") == true) return parsed
        val textBlocks = JSONArray()
        if (content.isNotBlank()) {
            textBlocks.put(
                JSONObject()
                    .put("id", "b_${UUID.randomUUID()}")
                    .put("type", "paragraph")
                    .put("children", JSONArray().put(JSONObject().put("text", content).put("marks", JSONArray())))
                    .put("checked", false)
            )
        }
        return JSONObject().put("version", 1).put("blocks", textBlocks)
    }

    private fun transcriptToJson(transcript: String): JSONObject = JSONObject()
        .put("id", "b_${UUID.randomUUID()}")
        .put("type", "paragraph")
        .put(
            "children",
            JSONArray().put(JSONObject().put("text", transcript).put("marks", JSONArray()))
        )
        .put("checked", false)
}

private fun VoiceNoteBlock.toJson(): JSONObject = JSONObject()
    .put("id", blockId)
    .put("blockId", blockId)
    .put("type", "voice")
    .put("audioFilePath", audioFilePath ?: JSONObject.NULL)
    .put("audioFormat", audioFormat.storageValue)
    .put("durationMs", durationMs)
    .put("fileSizeBytes", fileSizeBytes)
    .put("sampleRateHertz", sampleRateHertz)
    .put("channels", channels)
    .put("createdAt", createdAt)
    .put("updatedAt", updatedAt)
