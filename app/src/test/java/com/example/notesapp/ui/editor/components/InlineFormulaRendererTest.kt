package com.example.notesapp.ui.editor.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InlineFormulaRendererTest {

    @Test
    fun supportedFixturesRenderOfflineInLightAndDarkThemes() {
        // Arrange valid fixtures
        val validFixtures = mapOf(
            "E = mc^2" to "E = mc²",
            "\\frac{a}{b}" to "a⁄b",
            "\\sqrt{x}" to "√x",
            "\\alpha + \\beta = \\gamma" to "α + β = γ",
            "x_1 + y_2" to "x₁ + y₂",
            "\\sum_{i=1}^n x_i" to "∑₍ᵢ₌₁₎ⁿ xᵢ",
            "a \\cdot b \\times c \\div d" to "a · b × c ÷ d",
            "x \\leq y \\geq z \\neq w" to "x ≤ y ≥ z ≠ w"
        )

        // Act & Assert valid fixtures
        for ((source, expectedDisplay) in validFixtures) {
            val result = InlineFormulaRenderer.render(source)
            assertTrue("Expected valid result for '$source'", result.isValid)
            assertEquals("Rendered display mismatch for '$source'", expectedDisplay, result.displayText)
            assertEquals("Source should be preserved", source, result.source)

            // Repeat to guarantee offline determinism
            val repeatResult = InlineFormulaRenderer.render(source)
            assertEquals("Rendering must be deterministic", result, repeatResult)
        }

        // Arrange & Assert invalid / unsupported fixtures
        val emptyResult = InlineFormulaRenderer.render("   ")
        assertFalse("Empty source should be invalid", emptyResult.isValid)
        assertEquals(FormulaRenderError.EMPTY, emptyResult.error)

        val unbalancedResult = InlineFormulaRenderer.render("\\frac{a}{b")
        assertFalse("Unbalanced delimiters should be invalid", unbalancedResult.isValid)
        assertEquals(FormulaRenderError.UNBALANCED_DELIMITER, unbalancedResult.error)

        val unsupportedResult = InlineFormulaRenderer.render("\\unsupportedCommand{x}")
        assertFalse("Unsupported command should be invalid", unsupportedResult.isValid)
        assertEquals(FormulaRenderError.UNSUPPORTED_COMMAND, unsupportedResult.error)
        assertNotNull(unsupportedResult.displayText)
    }

    @Test
    fun malformedExpressionsReturnStructuredError() {
        val malformed = InlineFormulaRenderer.render("}")
        assertFalse(malformed.isValid)
        assertEquals(FormulaRenderError.UNBALANCED_DELIMITER, malformed.error)
    }
}
