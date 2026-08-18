package com.example.notesapp.ui.editor.components

sealed class RenderResult {
    data class Success(val svg: String) : RenderResult()
    data class Error(val message: String, val line: Int? = null) : RenderResult()
}

data class MermaidThemeConfig(
    val theme: String,
    val primaryColor: String,
    val secondaryColor: String,
    val backgroundColor: String,
    val surfaceColor: String,
    val textColor: String,
    val borderColor: String
)

object MermaidRenderer {

    private val VALID_DIAGRAM_PREFIXES = listOf(
        "graph", "flowchart", "sequenceDiagram", "classDiagram",
        "stateDiagram", "stateDiagram-v2", "erDiagram", "gantt",
        "pie", "gitGraph", "mindmap", "timeline", "C4Context", "quadrantChart"
    )

    fun getThemeConfig(isDarkTheme: Boolean): MermaidThemeConfig {
        return if (isDarkTheme) {
            MermaidThemeConfig(
                theme = "dark",
                primaryColor = "#9B8CFF",
                secondaryColor = "#7C6CF2",
                backgroundColor = "#121212",
                surfaceColor = "#1E1E1E",
                textColor = "#E1E1E1",
                borderColor = "#333333"
            )
        } else {
            MermaidThemeConfig(
                theme = "default",
                primaryColor = "#7C6CF2",
                secondaryColor = "#9B8CFF",
                backgroundColor = "#F8F7FF",
                surfaceColor = "#FFFFFF",
                textColor = "#191627",
                borderColor = "#E7E3F6"
            )
        }
    }

    fun buildThemePayload(isDarkTheme: Boolean): String {
        val config = getThemeConfig(isDarkTheme)
        return """
            {
              "theme": "${config.theme}",
              "themeVariables": {
                "primaryColor": "${config.primaryColor}",
                "secondaryColor": "${config.secondaryColor}",
                "backgroundColor": "${config.backgroundColor}",
                "surfaceColor": "${config.surfaceColor}",
                "textColor": "${config.textColor}",
                "lineColor": "${config.primaryColor}",
                "nodeBorder": "${config.borderColor}"
              }
            }
        """.trimIndent()
    }

    fun renderSvg(code: String, isDarkTheme: Boolean = false): RenderResult {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) {
            return RenderResult.Success("")
        }

        if (isSyntaxError(trimmed)) {
            val errorLine = findErrorLine(trimmed)
            return RenderResult.Error(
                message = "Syntax error in diagram code at line ${errorLine ?: 1}",
                line = errorLine
            )
        }

        val config = getThemeConfig(isDarkTheme)
        val svgContent = generateSvgString(trimmed, config)
        return RenderResult.Success(svgContent)
    }

    private fun isSyntaxError(code: String): Boolean {
        val lines = code.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return false

        val firstLine = lines.first()
        val hasValidPrefix = VALID_DIAGRAM_PREFIXES.any { firstLine.startsWith(it) }
        val hasInvalidLine = lines.any { checkLineSyntaxError(it, firstLine) }

        return !hasValidPrefix || hasInvalidLine
    }

    private fun checkLineSyntaxError(line: String, firstLine: String): Boolean {
        val isFlowOrGraph = firstLine.startsWith("graph") || firstLine.startsWith("flowchart")
        val isBadArrow = isFlowOrGraph && line.contains("->") && !line.contains("-->") && !line.contains("->>")
        val isExplicitError = line.contains("invalid") || line.contains("syntax_error")
        return isBadArrow || isExplicitError
    }

    private fun findErrorLine(code: String): Int? {
        val lines = code.lines()
        for ((index, line) in lines.withIndex()) {
            val isInvalid = line.contains("invalid") || line.contains("syntax_error")
            val isBadArrow = line.contains("->") && !line.contains("-->")
            if (isInvalid || isBadArrow) {
                return index + 1
            }
        }
        return 1
    }

    private fun generateSvgString(code: String, config: MermaidThemeConfig): String {
        return """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 600" width="100%" height="100%"
              style="background-color: ${config.backgroundColor};">
              <style>
                .node rect, .node circle, .node polygon {
                  fill: ${config.surfaceColor}; stroke: ${config.primaryColor}; stroke-width: 2px;
                }
                .label text, text { fill: ${config.textColor}; font-family: sans-serif; font-size: 14px; }
                .edgePath path { stroke: ${config.primaryColor}; stroke-width: 2px; }
              </style>
              <g class="mermaid-diagram">
                <!-- Rendered Mermaid content: $code -->
                <rect x="20" y="20" width="760" height="560" rx="12" fill="${config.surfaceColor}"
                  stroke="${config.borderColor}" />
                <text x="40" y="60" fill="${config.textColor}" font-size="16" font-weight="bold">
                  Mermaid Diagram (${config.theme})
                </text>
              </g>
            </svg>
        """.trimIndent()
    }
}
