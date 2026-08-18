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

    @Test
    fun givenMermaidFlowchartCode_whenRenderingSvg_thenContainsRenderedNodeLabelsAndConnectors() {
        // Arrange
        val code = "graph TD\n    A[Client Mobile] --> B[API Gateway]\n    B --> C[Auth Service]"

        // Act
        val result = MermaidRenderer.renderSvg(code, isDarkTheme = false)

        // Assert
        assertTrue("Result should be Success", result is RenderResult.Success)
        val svg = (result as RenderResult.Success).svg
        // The SVG must contain rendered visible text nodes, not just a comment
        assertTrue(
            "SVG should contain visible text element for Client Mobile",
            svg.contains(">Client Mobile</text>") || svg.contains(">Client Mobile<")
        )
        assertTrue(
            "SVG should contain visible text element for API Gateway",
            svg.contains(">API Gateway</text>") || svg.contains(">API Gateway<")
        )
        assertTrue(
            "SVG should contain visible text element for Auth Service",
            svg.contains(">Auth Service</text>") || svg.contains(">Auth Service<")
        )
    }

    @Test
    fun givenSequenceDiagramCode_whenRenderingSvg_thenContainsParticipantsAndMessages() {
        // Arrange
        val code = "sequenceDiagram\n    autonumber\n    Alice->>Bob: Hello Bob\n    Bob-->>Alice: Hi Alice"

        // Act
        val result = MermaidRenderer.renderSvg(code, isDarkTheme = false)

        // Assert
        assertTrue("Result should be Success", result is RenderResult.Success)
        val svg = (result as RenderResult.Success).svg
        assertTrue("SVG should contain participant Alice", svg.contains(">Alice</text>"))
        assertTrue("SVG should contain participant Bob", svg.contains(">Bob</text>"))
        assertTrue("SVG should contain message Hello Bob", svg.contains(">Hello Bob</text>"))
    }

    @Test
    fun givenClassDiagramCode_whenRenderingSvg_thenContainsClassBoxes() {
        // Arrange
        val code = "classDiagram\n    class Animal {\n        +name: String\n    }\n    Animal <|-- Dog"

        // Act
        val result = MermaidRenderer.renderSvg(code, isDarkTheme = false)

        // Assert
        assertTrue("Result should be Success", result is RenderResult.Success)
        val svg = (result as RenderResult.Success).svg
        assertTrue("SVG should contain Animal class", svg.contains(">Animal</text>"))
        assertTrue("SVG should contain Dog class", svg.contains(">Dog</text>"))
    }

    @Test
    fun givenStateDiagramCode_whenRenderingSvg_thenContainsStateNodes() {
        // Arrange
        val code = "stateDiagram-v2\n    [*] --> Still\n    Still --> Moving"

        // Act
        val result = MermaidRenderer.renderSvg(code, isDarkTheme = false)

        // Assert
        assertTrue("Result should be Success", result is RenderResult.Success)
        val svg = (result as RenderResult.Success).svg
        assertTrue("SVG should contain Still state", svg.contains(">Still</text>"))
        assertTrue("SVG should contain Moving state", svg.contains(">Moving</text>"))
    }
}
