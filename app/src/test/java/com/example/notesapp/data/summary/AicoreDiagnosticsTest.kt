package com.example.notesapp.data.summary

import org.junit.Assert.assertEquals
import org.junit.Test

class AicoreDiagnosticsTest {

    @Test
    fun `given known aicore error code when formatting diagnostics then returns named code`() {
        // Arrange / Act
        val result = AICORE_NOT_AVAILABLE.toAicoreErrorCodeName()

        // Assert
        assertEquals("NOT_AVAILABLE", result)
    }

    @Test
    fun `given unknown aicore error code when formatting diagnostics then returns unrecognized`() {
        // Arrange / Act
        val result = UNRECOGNIZED_AICORE_ERROR.toAicoreErrorCodeName()

        // Assert
        assertEquals("UNRECOGNIZED", result)
    }

    @Test
    fun `given normal exception when formatting diagnostics then includes type and normalized message`() {
        // Arrange
        val exception = IllegalStateException("AICore\nfailed\tbefore generation")

        // Act
        val result = exception.toAicoreDiagnosticMessage()

        // Assert
        assertEquals(
            "type=IllegalStateException; message=AICore failed before generation",
            result
        )
    }

    @Test
    fun `given blank exception message when formatting diagnostics then uses none`() {
        // Arrange
        val exception = IllegalStateException(" ")

        // Act
        val result = exception.toAicoreDiagnosticMessage()

        // Assert
        assertEquals(
            "type=IllegalStateException; message=none",
            result
        )
    }

    private companion object {
        const val AICORE_NOT_AVAILABLE = 8
        const val UNRECOGNIZED_AICORE_ERROR = 9_999
    }
}
