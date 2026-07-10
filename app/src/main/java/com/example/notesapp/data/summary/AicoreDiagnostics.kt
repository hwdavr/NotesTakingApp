package com.example.notesapp.data.summary

import com.google.ai.edge.aicore.GenerativeAIException

internal fun Throwable.toAicoreDiagnosticMessage(): String {
    val type = javaClass.simpleName.ifBlank { javaClass.name }
    val message = message.toSafeDiagnosticMessage()
    val rootAicoreCause = aicoreCauseChain()
        .drop(1)
        .firstOrNull { cause -> cause.errorCode != AICORE_ERROR_UNKNOWN }
    val baseMessage = if (this is GenerativeAIException) {
        "type=$type; errorCode=${errorCode.toAicoreErrorCodeName()}($errorCode); message=$message"
    } else {
        "type=$type; message=$message"
    }

    return if (rootAicoreCause == null) {
        baseMessage
    } else {
        val rootType = rootAicoreCause.javaClass.simpleName.ifBlank { rootAicoreCause.javaClass.name }
        "$baseMessage; rootType=$rootType; rootErrorCode=${rootAicoreCause.errorCode.toAicoreErrorCodeName()}" +
            "(${rootAicoreCause.errorCode}); rootMessage=${rootAicoreCause.message.toSafeDiagnosticMessage()}"
    }
}

private fun Throwable.aicoreCauseChain(): Sequence<GenerativeAIException> =
    generateSequence(this) { throwable -> throwable.cause }
        .filterIsInstance<GenerativeAIException>()

private fun String?.toSafeDiagnosticMessage(): String {
    val normalized = this
        ?.replace(Regex("\\s+"), " ")
        ?.trim()
        .orEmpty()
    return if (normalized.isBlank()) {
        "none"
    } else {
        normalized.take(MAX_DIAGNOSTIC_MESSAGE_LENGTH)
    }
}

internal fun Int.toAicoreErrorCodeName(): String = when (this) {
    AICORE_ERROR_UNKNOWN -> "UNKNOWN"
    AICORE_ERROR_BAD_DATA -> "BAD_DATA"
    AICORE_ERROR_BAD_REQUEST -> "BAD_REQUEST"
    AICORE_ERROR_REQUEST_PROCESSING -> "REQUEST_PROCESSING_ERROR"
    AICORE_ERROR_COMPUTE -> "COMPUTE_ERROR"
    AICORE_ERROR_IPC -> "IPC_ERROR"
    AICORE_ERROR_CANCELLED -> "CANCELLED"
    AICORE_ERROR_NOT_AVAILABLE -> "NOT_AVAILABLE"
    AICORE_ERROR_BUSY -> "BUSY"
    AICORE_ERROR_SERVICE_PROCESSING -> "SERVICE_PROCESSING_ERROR"
    AICORE_ERROR_RESPONSE_PROCESSING -> "RESPONSE_PROCESSING_ERROR"
    AICORE_ERROR_REQUEST_TOO_LARGE -> "REQUEST_TOO_LARGE"
    AICORE_ERROR_RESPONSE_GENERATION -> "RESPONSE_GENERATION_ERROR"
    AICORE_ERROR_NOT_ENOUGH_DISK_SPACE -> "NOT_ENOUGH_DISK_SPACE"
    AICORE_ERROR_BINDING_FAILURE -> "BINDING_FAILURE"
    AICORE_ERROR_SERVICE_DISCONNECTED -> "SERVICE_DISCONNECTED"
    AICORE_ERROR_BINDING_DIED -> "BINDING_DIED"
    AICORE_ERROR_NEEDS_SYSTEM_UPDATE -> "NEEDS_SYSTEM_UPDATE"
    AICORE_ERROR_NULL_BINDING -> "NULL_BINDING"
    else -> "UNRECOGNIZED"
}

private const val MAX_DIAGNOSTIC_MESSAGE_LENGTH = 180
private const val AICORE_ERROR_UNKNOWN = 0
private const val AICORE_ERROR_BAD_DATA = 2
private const val AICORE_ERROR_BAD_REQUEST = 3
private const val AICORE_ERROR_REQUEST_PROCESSING = 4
private const val AICORE_ERROR_COMPUTE = 5
private const val AICORE_ERROR_IPC = 6
private const val AICORE_ERROR_CANCELLED = 7
private const val AICORE_ERROR_NOT_AVAILABLE = 8
private const val AICORE_ERROR_BUSY = 9
private const val AICORE_ERROR_SERVICE_PROCESSING = 10
private const val AICORE_ERROR_RESPONSE_PROCESSING = 11
private const val AICORE_ERROR_REQUEST_TOO_LARGE = 12
private const val AICORE_ERROR_RESPONSE_GENERATION = 15
private const val AICORE_ERROR_NOT_ENOUGH_DISK_SPACE = 501
private const val AICORE_ERROR_BINDING_FAILURE = 601
private const val AICORE_ERROR_SERVICE_DISCONNECTED = 602
private const val AICORE_ERROR_BINDING_DIED = 603
private const val AICORE_ERROR_NEEDS_SYSTEM_UPDATE = 604
private const val AICORE_ERROR_NULL_BINDING = 605
