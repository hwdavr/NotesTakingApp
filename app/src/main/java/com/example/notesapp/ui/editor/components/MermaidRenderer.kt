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
        val lines = code.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val firstLine = lines.firstOrNull() ?: ""

        return when {
            firstLine.startsWith("sequenceDiagram") -> generateSequenceSvg(lines, config)
            firstLine.startsWith("classDiagram") -> generateClassSvg(lines, config)
            firstLine.startsWith("stateDiagram") -> generateStateSvg(lines, config)
            else -> generateFlowchartSvg(lines, config)
        }
    }

    private data class ParsedNode(
        val id: String,
        val label: String,
        val shape: NodeShape = NodeShape.RECT
    )

    private enum class NodeShape { RECT, ROUNDED, DIAMOND, CIRCLE }

    private data class ParsedEdge(
        val fromId: String,
        val toId: String,
        val label: String? = null,
        val isDotted: Boolean = false
    )

    private data class FlowchartParseResult(
        val nodes: Map<String, ParsedNode>,
        val edges: List<ParsedEdge>,
        val isHorizontal: Boolean
    )

    private fun parseFlowchart(lines: List<String>): FlowchartParseResult {
        val nodesMap = linkedMapOf<String, ParsedNode>()
        val edges = mutableListOf<ParsedEdge>()
        val isHorizontal = lines.firstOrNull()?.contains("LR") == true || lines.firstOrNull()?.contains("RL") == true
        val nodeRegex = Regex("""([a-zA-Z0-9_]+)(\[([^\]]+)\]|\(([^\)]+)\)|\{([^\}]+)\})?""")

        for (line in lines.drop(1)) {
            if (line.startsWith("%%") || line.startsWith("subgraph") || line == "end") continue

            val arrowSplit = parseArrowSplit(line)
            if (arrowSplit != null) {
                val fromNode = extractNode(arrowSplit.first, nodeRegex)
                val toNode = extractNode(arrowSplit.third, nodeRegex)
                nodesMap[fromNode.id] = mergeNode(nodesMap[fromNode.id], fromNode)
                nodesMap[toNode.id] = mergeNode(nodesMap[toNode.id], toNode)
                edges.add(ParsedEdge(fromNode.id, toNode.id, arrowSplit.second, line.contains("-.->")))
            } else {
                val singleNode = extractNode(line, nodeRegex)
                if (singleNode.id.isNotBlank()) {
                    nodesMap[singleNode.id] = mergeNode(nodesMap[singleNode.id], singleNode)
                }
            }
        }

        if (nodesMap.isEmpty()) {
            nodesMap["A"] = ParsedNode("A", "Start", NodeShape.ROUNDED)
            nodesMap["B"] = ParsedNode("B", "End", NodeShape.ROUNDED)
            edges.add(ParsedEdge("A", "B", null))
        }

        return FlowchartParseResult(nodesMap, edges, isHorizontal)
    }

    private fun parseArrowSplit(line: String): Triple<String, String?, String>? {
        return when {
            line.contains("-->|") -> {
                val parts = line.split("-->|")
                if (parts.size == 2) {
                    val labelAndTarget = parts[1].split("|")
                    if (labelAndTarget.size == 2) {
                        Triple(parts[0].trim(), labelAndTarget[0].trim(), labelAndTarget[1].trim())
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
            line.contains("-->") -> {
                val parts = line.split("-->")
                if (parts.size == 2) Triple(parts[0].trim(), null, parts[1].trim()) else null
            }
            line.contains("-.->") -> {
                val parts = line.split("-.->")
                if (parts.size == 2) Triple(parts[0].trim(), null, parts[1].trim()) else null
            }
            line.contains("---") -> {
                val parts = line.split("---")
                if (parts.size == 2) Triple(parts[0].trim(), null, parts[1].trim()) else null
            }
            else -> null
        }
    }

    private fun generateFlowchartSvg(lines: List<String>, config: MermaidThemeConfig): String {
        val parsed = parseFlowchart(lines)
        val nodeList = parsed.nodes.values.toList()
        val nodeCoords = mutableMapOf<String, Pair<Float, Float>>()
        val nodeWidth = 140f
        val nodeHeight = 50f

        val (svgWidth, svgHeight) = computeFlowchartDimensions(parsed, nodeList, nodeCoords)

        val sb = StringBuilder()
        sb.append("""<svg xmlns="http://www.w3.org/2000/svg" """)
        sb.append("""viewBox="0 0 ${svgWidth.toInt()} ${svgHeight.toInt()}" """)
        sb.append("""width="100%" height="100%" """)
        sb.append("""style="background-color: ${config.backgroundColor};">""")
        sb.append("<defs>")
        sb.append("""<marker id="arrowhead" markerWidth="8" markerHeight="6" """)
        sb.append("""refX="7" refY="3" orient="auto">""")
        sb.append("""<polygon points="0 0, 8 3, 0 6" fill="${config.primaryColor}"/>""")
        sb.append("</marker>")
        sb.append("</defs>")

        parsed.edges.forEach { edge ->
            val from = nodeCoords[edge.fromId]
            val to = nodeCoords[edge.toId]
            if (from != null && to != null) {
                renderFlowchartEdge(sb, edge, from, to, parsed.isHorizontal, nodeWidth, nodeHeight, config)
            }
        }

        nodeList.forEach { node ->
            val coords = nodeCoords[node.id]
            if (coords != null) {
                renderFlowchartNode(sb, node, coords, nodeWidth, nodeHeight, config)
            }
        }

        sb.append("</svg>")
        return sb.toString()
    }

    private fun computeFlowchartDimensions(
        parsed: FlowchartParseResult,
        nodeList: List<ParsedNode>,
        nodeCoords: MutableMap<String, Pair<Float, Float>>
    ): Pair<Float, Float> {
        return if (parsed.isHorizontal) {
            val width = maxOf(600f, (nodeList.size * 200f) + 100f)
            val height = 300f
            val startX = 80f
            val centerY = height / 2f
            for ((index, node) in nodeList.withIndex()) {
                nodeCoords[node.id] = Pair(startX + index * 180f, centerY)
            }
            Pair(width, height)
        } else {
            val levels = calculateLevels(nodeList, parsed.edges)
            val maxNodesInLevel = levels.values.groupBy { it }.maxOfOrNull { it.value.size } ?: 1
            val width = maxOf(600f, (maxNodesInLevel * 180f) + 120f)
            val maxLevel = levels.values.maxOrNull() ?: 0
            val height = maxOf(400f, (maxLevel + 1) * 110f + 100f)

            val levelGroups = nodeList.groupBy { levels[it.id] ?: 0 }
            for ((level, nodes) in levelGroups) {
                val y = 70f + level * 100f
                val totalWidth = nodes.size * 170f
                val startX = (width - totalWidth) / 2f + 85f
                for ((index, node) in nodes.withIndex()) {
                    nodeCoords[node.id] = Pair(startX + index * 170f, y)
                }
            }
            Pair(width, height)
        }
    }

    private fun renderFlowchartEdge(
        sb: StringBuilder,
        edge: ParsedEdge,
        from: Pair<Float, Float>,
        to: Pair<Float, Float>,
        isHorizontal: Boolean,
        nodeWidth: Float,
        nodeHeight: Float,
        config: MermaidThemeConfig
    ) {
        val strokeDash = if (edge.isDotted) """stroke-dasharray="4,4"""" else ""
        if (isHorizontal) {
            val startX = from.first + nodeWidth / 2f
            val endX = to.first - nodeWidth / 2f
            sb.append("""<line x1="$startX" y1="${from.second}" x2="$endX" y2="${to.second}" """)
            sb.append("""stroke="${config.primaryColor}" stroke-width="2" """)
            sb.append("""marker-end="url(#arrowhead)" $strokeDash />""")
        } else {
            val startY = from.second + nodeHeight / 2f
            val endY = to.second - nodeHeight / 2f
            sb.append("""<line x1="${from.first}" y1="$startY" x2="${to.first}" y2="$endY" """)
            sb.append("""stroke="${config.primaryColor}" stroke-width="2" """)
            sb.append("""marker-end="url(#arrowhead)" $strokeDash />""")
        }

        if (!edge.label.isNullOrBlank()) {
            val midX = (from.first + to.first) / 2f
            val midY = (from.second + to.second) / 2f - 8f
            sb.append("""<text x="$midX" y="$midY" fill="${config.textColor}" """)
            sb.append("""font-size="11" font-family="sans-serif" text-anchor="middle" """)
            sb.append("""font-weight="500">${escapeXml(edge.label)}</text>""")
        }
    }

    private fun renderFlowchartNode(
        sb: StringBuilder,
        node: ParsedNode,
        coords: Pair<Float, Float>,
        nodeWidth: Float,
        nodeHeight: Float,
        config: MermaidThemeConfig
    ) {
        val left = coords.first - nodeWidth / 2f
        val top = coords.second - nodeHeight / 2f

        when (node.shape) {
            NodeShape.DIAMOND -> {
                val p1 = "${coords.first},${top - 5f}"
                val p2 = "${left + nodeWidth + 10f},${coords.second}"
                val p3 = "${coords.first},${top + nodeHeight + 5f}"
                val p4 = "${left - 10f},${coords.second}"
                sb.append("""<polygon points="$p1 $p2 $p3 $p4" fill="${config.surfaceColor}" """)
                sb.append("""stroke="${config.primaryColor}" stroke-width="2" />""")
            }
            NodeShape.ROUNDED -> {
                sb.append("""<rect x="$left" y="$top" width="$nodeWidth" height="$nodeHeight" """)
                sb.append("""rx="20" ry="20" fill="${config.surfaceColor}" """)
                sb.append("""stroke="${config.primaryColor}" stroke-width="2" />""")
            }
            else -> {
                sb.append("""<rect x="$left" y="$top" width="$nodeWidth" height="$nodeHeight" """)
                sb.append("""rx="8" ry="8" fill="${config.surfaceColor}" """)
                sb.append("""stroke="${config.primaryColor}" stroke-width="2" />""")
            }
        }

        val textY = coords.second + 5f
        sb.append("""<text x="${coords.first}" y="$textY" fill="${config.textColor}" """)
        sb.append("""font-size="13" font-family="sans-serif" font-weight="600" """)
        sb.append("""text-anchor="middle">${escapeXml(node.label)}</text>""")
    }

    private fun extractNode(raw: String, regex: Regex): ParsedNode {
        val trimmed = raw.trim()
        val match = regex.find(trimmed) ?: return ParsedNode(trimmed, trimmed)
        val id = match.groupValues[1]
        val rectText = match.groupValues.getOrNull(3)?.takeIf { it.isNotEmpty() }
        val roundText = match.groupValues.getOrNull(4)?.takeIf { it.isNotEmpty() }
        val diamondText = match.groupValues.getOrNull(5)?.takeIf { it.isNotEmpty() }

        return when {
            diamondText != null -> ParsedNode(id, diamondText, NodeShape.DIAMOND)
            roundText != null -> ParsedNode(id, roundText, NodeShape.ROUNDED)
            rectText != null -> ParsedNode(id, rectText, NodeShape.RECT)
            else -> ParsedNode(id, id)
        }
    }

    private fun mergeNode(existing: ParsedNode?, new: ParsedNode): ParsedNode {
        if (existing == null) return new
        if (new.label != new.id) return new
        return existing
    }

    private fun calculateLevels(nodes: List<ParsedNode>, edges: List<ParsedEdge>): Map<String, Int> {
        val levels = mutableMapOf<String, Int>()
        val incoming = mutableMapOf<String, Int>()

        for (node in nodes) {
            incoming[node.id] = 0
        }
        for (edge in edges) {
            incoming[edge.toId] = (incoming[edge.toId] ?: 0) + 1
        }

        val queue = ArrayDeque<Pair<String, Int>>()
        for (node in nodes) {
            if ((incoming[node.id] ?: 0) == 0) {
                queue.add(Pair(node.id, 0))
                levels[node.id] = 0
            }
        }

        if (queue.isEmpty() && nodes.isNotEmpty()) {
            queue.add(Pair(nodes.first().id, 0))
            levels[nodes.first().id] = 0
        }

        while (queue.isNotEmpty()) {
            val (current, level) = queue.removeFirst()
            for (edge in edges.filter { it.fromId == current }) {
                val nextLevel = level + 1
                if ((levels[edge.toId] ?: -1) < nextLevel) {
                    levels[edge.toId] = nextLevel
                    queue.add(Pair(edge.toId, nextLevel))
                }
            }
        }

        for (node in nodes) {
            if (!levels.containsKey(node.id)) {
                levels[node.id] = (levels.values.maxOrNull() ?: 0)
            }
        }

        return levels
    }

    private fun generateSequenceSvg(lines: List<String>, config: MermaidThemeConfig): String {
        val participants = linkedSetOf<String>()
        val messages = mutableListOf<Triple<String, String, String>>()

        for (line in lines.drop(1)) {
            when {
                line.startsWith("participant ") -> {
                    val name = line.removePrefix("participant ").trim().split(" as ").last().trim()
                    participants.add(name)
                }
                line.startsWith("actor ") -> {
                    val name = line.removePrefix("actor ").trim().split(" as ").last().trim()
                    participants.add(name)
                }
                line.contains("->>") || line.contains("-->>") -> {
                    val isDotted = line.contains("-->>")
                    val sep = if (isDotted) "-->>" else "->>"
                    val parts = line.split(sep)
                    if (parts.size == 2) {
                        val from = parts[0].trim()
                        val toAndMsg = parts[1].split(":")
                        val to = toAndMsg[0].trim()
                        val msg = if (toAndMsg.size > 1) toAndMsg[1].trim() else ""
                        participants.add(from)
                        participants.add(to)
                        messages.add(Triple(from, to, msg))
                    }
                }
            }
        }

        if (participants.isEmpty()) {
            participants.add("Alice")
            participants.add("Bob")
            messages.add(Triple("Alice", "Bob", "Hello"))
        }

        val partList = participants.toList()
        val partWidth = 110f
        val partHeight = 40f
        val svgWidth = maxOf(600f, (partList.size * 180f) + 80f)
        val svgHeight = maxOf(400f, (messages.size * 60f) + 160f)

        val partCoords = mutableMapOf<String, Float>()
        val spacing = svgWidth / (partList.size + 1)
        for ((index, part) in partList.withIndex()) {
            partCoords[part] = spacing * (index + 1)
        }

        val sb = StringBuilder()
        sb.append("""<svg xmlns="http://www.w3.org/2000/svg" """)
        sb.append("""viewBox="0 0 ${svgWidth.toInt()} ${svgHeight.toInt()}" """)
        sb.append("""width="100%" height="100%" style="background-color: ${config.backgroundColor};">""")
        sb.append("<defs>")
        sb.append("""<marker id="seqarrow" markerWidth="8" markerHeight="6" """)
        sb.append("""refX="7" refY="3" orient="auto">""")
        sb.append("""<polygon points="0 0, 8 3, 0 6" fill="${config.primaryColor}"/>""")
        sb.append("</marker>")
        sb.append("</defs>")

        val topY = 40f
        val bottomY = svgHeight - 40f

        partList.forEach { part ->
            val cx = partCoords[part]
            if (cx != null) {
                renderSequenceParticipant(sb, part, cx, topY, bottomY, partWidth, partHeight, config)
            }
        }

        messages.forEachIndexed { index, msg ->
            val y = topY + partHeight + 40f + index * 55f
            val fromX = partCoords[msg.first]
            val toX = partCoords[msg.second]
            if (fromX != null && toX != null) {
                renderSequenceMessage(sb, msg.third, fromX, toX, y, config)
            }
        }

        sb.append("</svg>")
        return sb.toString()
    }

    private fun renderSequenceParticipant(
        sb: StringBuilder,
        part: String,
        cx: Float,
        topY: Float,
        bottomY: Float,
        partWidth: Float,
        partHeight: Float,
        config: MermaidThemeConfig
    ) {
        sb.append("""<line x1="$cx" y1="${topY + partHeight}" x2="$cx" y2="$bottomY" """)
        sb.append("""stroke="${config.borderColor}" stroke-width="1.5" stroke-dasharray="4,4" />""")

        val left = cx - partWidth / 2f
        sb.append("""<rect x="$left" y="$topY" width="$partWidth" height="$partHeight" rx="6" """)
        sb.append("""fill="${config.surfaceColor}" stroke="${config.primaryColor}" stroke-width="2" />""")
        sb.append("""<text x="$cx" y="${topY + 25f}" fill="${config.textColor}" """)
        sb.append("""font-size="13" font-family="sans-serif" font-weight="600" """)
        sb.append("""text-anchor="middle">${escapeXml(part)}</text>""")

        sb.append("""<rect x="$left" y="${bottomY - 10f}" width="$partWidth" height="$partHeight" rx="6" """)
        sb.append("""fill="${config.surfaceColor}" stroke="${config.primaryColor}" stroke-width="2" />""")
        sb.append("""<text x="$cx" y="${bottomY + 15f}" fill="${config.textColor}" """)
        sb.append("""font-size="13" font-family="sans-serif" font-weight="600" """)
        sb.append("""text-anchor="middle">${escapeXml(part)}</text>""")
    }

    private fun renderSequenceMessage(
        sb: StringBuilder,
        msgText: String,
        fromX: Float,
        toX: Float,
        y: Float,
        config: MermaidThemeConfig
    ) {
        val isRight = toX >= fromX
        val endX = if (isRight) toX - 4f else toX + 4f

        sb.append("""<line x1="$fromX" y1="$y" x2="$endX" y2="$y" """)
        sb.append("""stroke="${config.primaryColor}" stroke-width="2" marker-end="url(#seqarrow)" />""")

        val midX = (fromX + toX) / 2f
        sb.append("""<text x="$midX" y="${y - 8f}" fill="${config.textColor}" """)
        sb.append("""font-size="12" font-family="sans-serif" font-weight="500" """)
        sb.append("""text-anchor="middle">${escapeXml(msgText)}</text>""")
    }

    private fun generateClassSvg(lines: List<String>, config: MermaidThemeConfig): String {
        val classes = mutableListOf<String>()
        for (line in lines.drop(1)) {
            if (line.startsWith("class ")) {
                val className = line.removePrefix("class ").substringBefore("{").trim()
                if (className.isNotEmpty()) classes.add(className)
            } else if (line.contains("<|--")) {
                val parts = line.split("<|--")
                parts.forEach { part ->
                    val name = part.trim()
                    if (name.isNotEmpty() && !classes.contains(name)) classes.add(name)
                }
            }
        }
        if (classes.isEmpty()) classes.addAll(listOf("Animal", "Dog"))

        val svgWidth = maxOf(600f, classes.size * 180f + 60f)
        val svgHeight = 350f
        val boxWidth = 140f
        val boxHeight = 90f

        val sb = StringBuilder()
        sb.append("""<svg xmlns="http://www.w3.org/2000/svg" """)
        sb.append("""viewBox="0 0 ${svgWidth.toInt()} ${svgHeight.toInt()}" """)
        sb.append("""width="100%" height="100%" style="background-color: ${config.backgroundColor};">""")

        for ((index, cls) in classes.withIndex()) {
            val left = 50f + index * 180f
            val top = 80f
            sb.append("""<rect x="$left" y="$top" width="$boxWidth" height="$boxHeight" rx="6" """)
            sb.append("""fill="${config.surfaceColor}" stroke="${config.primaryColor}" stroke-width="2" />""")
            sb.append("""<line x1="$left" y1="${top + 30f}" x2="${left + boxWidth}" y2="${top + 30f}" """)
            sb.append("""stroke="${config.borderColor}" stroke-width="1" />""")
            sb.append("""<text x="${left + boxWidth / 2f}" y="${top + 20f}" fill="${config.textColor}" """)
            sb.append("""font-size="13" font-family="sans-serif" font-weight="bold" """)
            sb.append("""text-anchor="middle">${escapeXml(cls)}</text>""")
            sb.append("""<text x="${left + 12f}" y="${top + 50f}" fill="${config.textColor}" """)
            sb.append("""font-size="11" font-family="monospace">+name: String</text>""")
            sb.append("""<text x="${left + 12f}" y="${top + 70f}" fill="${config.textColor}" """)
            sb.append("""font-size="11" font-family="monospace">+execute()</text>""")
        }

        sb.append("</svg>")
        return sb.toString()
    }

    private fun generateStateSvg(lines: List<String>, config: MermaidThemeConfig): String {
        val states = mutableListOf<String>()
        for (line in lines.drop(1)) {
            if (line.contains("-->")) {
                val parts = line.split("-->")
                parts.forEach {
                    val s = it.trim().substringBefore(":")
                    if (s.isNotEmpty() && s != "[*]" && !states.contains(s)) {
                        states.add(s)
                    }
                }
            }
        }
        if (states.isEmpty()) states.addAll(listOf("Still", "Moving", "Crash"))

        val svgWidth = maxOf(600f, (states.size + 2) * 140f + 60f)
        val svgHeight = 280f
        val centerY = svgHeight / 2f

        val sb = StringBuilder()
        sb.append("""<svg xmlns="http://www.w3.org/2000/svg" """)
        sb.append("""viewBox="0 0 ${svgWidth.toInt()} ${svgHeight.toInt()}" """)
        sb.append("""width="100%" height="100%" style="background-color: ${config.backgroundColor};">""")
        sb.append("<defs>")
        sb.append("""<marker id="statearrow" markerWidth="8" markerHeight="6" """)
        sb.append("""refX="7" refY="3" orient="auto">""")
        sb.append("""<polygon points="0 0, 8 3, 0 6" fill="${config.primaryColor}"/>""")
        sb.append("</marker>")
        sb.append("</defs>")

        sb.append("""<circle cx="50" cy="$centerY" r="12" fill="${config.primaryColor}" />""")

        var prevX = 50f
        for ((index, state) in states.withIndex()) {
            val cx = 150f + index * 140f
            val left = cx - 50f
            val top = centerY - 25f

            sb.append("""<line x1="${prevX + 15f}" y1="$centerY" x2="${left - 5f}" y2="$centerY" """)
            sb.append("""stroke="${config.primaryColor}" stroke-width="2" marker-end="url(#statearrow)" />""")
            sb.append("""<rect x="$left" y="$top" width="100" height="50" rx="16" """)
            sb.append("""fill="${config.surfaceColor}" stroke="${config.primaryColor}" stroke-width="2" />""")
            sb.append("""<text x="$cx" y="${centerY + 5f}" fill="${config.textColor}" """)
            sb.append("""font-size="12" font-family="sans-serif" font-weight="600" """)
            sb.append("""text-anchor="middle">${escapeXml(state)}</text>""")

            prevX = left + 100f
        }

        val endX = prevX + 60f
        sb.append("""<line x1="${prevX + 5f}" y1="$centerY" x2="${endX - 16f}" y2="$centerY" """)
        sb.append("""stroke="${config.primaryColor}" stroke-width="2" marker-end="url(#statearrow)" />""")
        sb.append("""<circle cx="$endX" cy="$centerY" r="14" fill="none" """)
        sb.append("""stroke="${config.primaryColor}" stroke-width="2" />""")
        sb.append("""<circle cx="$endX" cy="$centerY" r="8" fill="${config.primaryColor}" />""")

        sb.append("</svg>")
        return sb.toString()
    }

    private fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
