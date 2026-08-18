package com.example.notesapp.ui.editor.components

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MermaidRendererTest {

    @Test
    fun testRenderValidFlowchartProducesSvg() {
        // Arrange
        val validCode = "graph TD\n    A[Start] --> B[Result 1]"

        // Act
        val result = MermaidRenderer.renderSvg(validCode, isDarkTheme = false)

        // Assert
        assertTrue("Result should be Success", result is RenderResult.Success)
        val successResult = result as RenderResult.Success
        assertTrue("SVG should contain <svg tag", successResult.svg.contains("<svg"))
        assertTrue("SVG should contain </svg> closing tag", successResult.svg.contains("</svg>"))
    }

    @Test
    fun testDarkThemeTokenInjection() {
        // Arrange & Act
        val darkPayload = MermaidRenderer.buildThemePayload(isDarkTheme = true)
        val darkResult = MermaidRenderer.renderSvg("graph TD\n    A --> B", isDarkTheme = true)

        // Assert
        assertTrue("Payload should contain dark theme tag", darkPayload.contains("\"theme\": \"dark\""))
        assertTrue("Payload should contain dark primary color token", darkPayload.contains("#9B8CFF"))
        assertTrue("Dark result should be Success", darkResult is RenderResult.Success)
        val svg = (darkResult as RenderResult.Success).svg
        assertTrue("SVG should contain dark background token", svg.contains("#121212") || svg.contains("dark"))
    }

    @Test
    fun testInvalidSyntaxReturnsStructuredError() {
        // Arrange
        val invalidCode = "graph ZZ -> invalid"

        // Act
        val result = MermaidRenderer.renderSvg(invalidCode, isDarkTheme = false)

        // Assert
        assertTrue("Result should be Error", result is RenderResult.Error)
        val errorResult = result as RenderResult.Error
        assertNotNull("Error message should not be null", errorResult.message)
        assertTrue("Error message should indicate syntax error", errorResult.message.contains("Syntax error"))
    }
}
