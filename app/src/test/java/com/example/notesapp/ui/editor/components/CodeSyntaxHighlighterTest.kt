package com.example.notesapp.ui.editor.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeSyntaxHighlighterTest {

    @Test
    fun testSyntaxHighlightingForSupportedLanguages() {
        val kotlin = "fun main() {\n    val answer = 42\n    println(\"Hello\") // comment\n}"
        val kotlinTokens = CodeSyntaxHighlighter.tokenize(kotlin, "kotlin")

        val funToken = kotlinTokens.first {
            it.type == CodeTokenType.KEYWORD && kotlin.substring(it.start, it.endExclusive) == "fun"
        }
        assertEquals(0, funToken.start)
        assertEquals(3, funToken.endExclusive)
        assertEquals(listOf("fun", "val"), tokenTexts(kotlin, kotlinTokens, CodeTokenType.KEYWORD))
        assertEquals(listOf("\"Hello\""), tokenTexts(kotlin, kotlinTokens, CodeTokenType.STRING))
        assertEquals(listOf("42"), tokenTexts(kotlin, kotlinTokens, CodeTokenType.NUMBER))
        assertEquals(listOf("// comment"), tokenTexts(kotlin, kotlinTokens, CodeTokenType.COMMENT))
        val commentToken = kotlinTokens.first { it.type == CodeTokenType.COMMENT }
        assertEquals(kotlin.indexOf("// comment"), commentToken.start)
        assertEquals(kotlin.indexOf("// comment") + "// comment".length, commentToken.endExclusive)

        val python = "def greet(name):\n    # say hi\n    return \"Hello \" + name\n"
        val pythonTokens = CodeSyntaxHighlighter.tokenize(python, "python")
        assertEquals(listOf("def", "return"), tokenTexts(python, pythonTokens, CodeTokenType.KEYWORD))
        assertEquals(listOf("\"Hello \""), tokenTexts(python, pythonTokens, CodeTokenType.STRING))
        assertEquals(listOf("# say hi"), tokenTexts(python, pythonTokens, CodeTokenType.COMMENT))

        val json = "{\n  \"name\": \"Buffy\",\n  \"active\": true,\n  \"count\": 42\n}"
        val jsonTokens = CodeSyntaxHighlighter.tokenize(json, "json")
        val jsonStrings = tokenTexts(json, jsonTokens, CodeTokenType.STRING)
        assertTrue(jsonStrings.contains("\"name\""))
        assertTrue(jsonStrings.contains("\"Buffy\""))
        assertEquals(listOf("true"), tokenTexts(json, jsonTokens, CodeTokenType.KEYWORD))
        assertEquals(listOf("42"), tokenTexts(json, jsonTokens, CodeTokenType.NUMBER))
    }

    @Test
    fun testDynamicLineNumberCalculation() {
        assertEquals(1, CodeSyntaxHighlighter.lineCount(""))
        assertEquals(1, CodeSyntaxHighlighter.lineCount("single line"))
        assertEquals(3, CodeSyntaxHighlighter.lineCount("a\nb\nc"))
        assertEquals(4, CodeSyntaxHighlighter.lineCount("a\nb\nc\n"))
        assertEquals(listOf(1, 2, 3), CodeSyntaxHighlighter.lineNumbers("a\nb\nc").toList())
        assertEquals(listOf(1), CodeSyntaxHighlighter.lineNumbers("").toList())
    }

    @Test
    fun testPlainTextAndFallbackHandling() {
        assertEquals(emptyList<CodeToken>(), CodeSyntaxHighlighter.tokenize("", "kotlin"))

        val plain = CodeSyntaxHighlighter.tokenize("just text", "plaintext")
        assertEquals(1, plain.size)
        assertEquals(CodeTokenType.PLAIN_TEXT, plain.single().type)
        assertEquals(0, plain.single().start)
        assertEquals("just text".length, plain.single().endExclusive)

        val unknown = CodeSyntaxHighlighter.tokenize("abc", "brainfuck")
        assertEquals(listOf(CodeTokenType.PLAIN_TEXT), unknown.map { it.type })
        assertTrue(CodeSyntaxHighlighter.lineCount("abc") > 0)
    }

    private fun tokenTexts(code: String, tokens: List<CodeToken>, type: CodeTokenType): List<String> =
        tokens.filter { it.type == type }.map { code.substring(it.start, it.endExclusive) }
}
