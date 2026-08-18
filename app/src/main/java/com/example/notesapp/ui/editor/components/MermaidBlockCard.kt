package com.example.notesapp.ui.editor.components

import android.webkit.WebView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.notesapp.R
import com.example.notesapp.ui.editor.mapper.DEFAULT_MERMAID_CODE
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.theme.LocalAppColors
import kotlin.math.roundToInt

@Composable
fun MermaidBlockCard(
    block: EditorBlock.MermaidBlock,
    isEditable: Boolean,
    onUpdateTitle: (String) -> Unit,
    onUpdateCode: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpenFullscreen: (EditorBlock.MermaidBlock) -> Unit = {}
) {
    var cardMode by rememberSaveable(block.id) { mutableStateOf(MermaidCardMode.PREVIEW) }
    val colors = LocalAppColors.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("editor_mermaid_block_${block.id}")
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MermaidCardHeader(
                block = block,
                isEditable = isEditable,
                cardMode = cardMode,
                onToggleMode = {
                    cardMode = if (cardMode == MermaidCardMode.PREVIEW) {
                        MermaidCardMode.CODE
                    } else {
                        MermaidCardMode.PREVIEW
                    }
                },
                onUpdateTitle = onUpdateTitle,
                onOpenFullscreen = onOpenFullscreen
            )

            if (cardMode == MermaidCardMode.CODE && isEditable) {
                MermaidCodeEditorContent(
                    block = block,
                    onUpdateCode = onUpdateCode
                )
            } else {
                MermaidPreviewContent(
                    block = block
                )
            }
        }
    }
}

enum class MermaidCardMode {
    PREVIEW,
    CODE
}

@Composable
private fun MermaidCardHeader(
    block: EditorBlock.MermaidBlock,
    isEditable: Boolean,
    cardMode: MermaidCardMode,
    onToggleMode: () -> Unit,
    onUpdateTitle: (String) -> Unit,
    onOpenFullscreen: (EditorBlock.MermaidBlock) -> Unit
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.AccountTree,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (isEditable) {
                BasicTextField(
                    value = block.title,
                    onValueChange = onUpdateTitle,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .testTag("editor_mermaid_title_${block.id}")
                        .fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (block.title.isEmpty()) {
                            Text(
                                text = stringResource(R.string.mermaid_default_title),
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textTertiary
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            } else {
                Text(
                    text = block.title.ifBlank { stringResource(R.string.mermaid_default_title) },
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    ),
                    modifier = Modifier.testTag("editor_mermaid_title_${block.id}")
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isEditable) {
                val isPreview = cardMode == MermaidCardMode.PREVIEW
                Surface(
                    onClick = onToggleMode,
                    shape = RoundedCornerShape(16.dp),
                    color = colors.primary.copy(alpha = 0.12f),
                    modifier = Modifier.testTag("editor_mermaid_toggle_mode_${block.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPreview) Icons.Default.Code else Icons.Default.RemoveRedEye,
                            contentDescription = stringResource(
                                R.string.mermaid_toggle_mode_content_description
                            ),
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPreview) {
                                stringResource(R.string.mermaid_edit_code)
                            } else {
                                stringResource(R.string.mermaid_view_chart)
                            },
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.primary
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            IconButton(
                onClick = { onOpenFullscreen(block) },
                modifier = Modifier.testTag("editor_mermaid_fullscreen_btn_${block.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = stringResource(R.string.mermaid_fullscreen_content_description),
                    tint = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun MermaidCodeEditorContent(block: EditorBlock.MermaidBlock, onUpdateCode: (String) -> Unit) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.mermaid_quick_templates),
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        val templates = listOf(
            "Flowchart" to R.string.mermaid_template_flowchart,
            "Sequence" to R.string.mermaid_template_sequence,
            "Class" to R.string.mermaid_template_class,
            "State" to R.string.mermaid_template_state
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            items(templates) { (name, labelRes) ->
                val label = stringResource(labelRes)
                val templateCode = when (name) {
                    "Flowchart" -> """
                        graph TD
                            A[Start] --> B{Decision}
                            B -->|Yes| C[Result 1]
                            B -->|No| D[Result 2]
                    """.trimIndent()
                    "Sequence" -> """
                        sequenceDiagram
                            autonumber
                            Alice->>Bob: Hello Bob, how are you?
                            Bob-->>Alice: Great!
                    """.trimIndent()
                    "Class" -> """
                        classDiagram
                            class Animal {
                                +String name
                                +makeSound()
                            }
                            class Dog {
                                +bark()
                            }
                            Animal <|-- Dog
                    """.trimIndent()
                    "State" -> """
                        stateDiagram-v2
                            [*] --> Still
                            Still --> [*]
                            Still --> Moving
                            Moving --> Still
                            Moving --> Crash
                            Crash --> [*]
                    """.trimIndent()
                    else -> DEFAULT_MERMAID_CODE
                }

                AssistChip(
                    onClick = { onUpdateCode(templateCode) },
                    label = {
                        Text(
                            text = label,
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        )
                    },
                    modifier = Modifier.testTag("editor_mermaid_template_chip_${name.lowercase()}")
                )
            }
        }

        OutlinedTextField(
            value = block.code,
            onValueChange = onUpdateCode,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp, max = 280.dp)
                .testTag("editor_mermaid_code_editor_${block.id}"),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = colors.textPrimary
            ),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border,
                focusedContainerColor = colors.background.copy(alpha = 0.5f),
                unfocusedContainerColor = colors.background.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        val isDark = isSystemInDarkTheme()
        val validationResult = remember(block.code, isDark) {
            MermaidRenderer.renderSvg(block.code, isDarkTheme = isDark)
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("editor_mermaid_syntax_badge_${block.id}"),
            shape = RoundedCornerShape(6.dp),
            color = if (validationResult is RenderResult.Success) {
                colors.accentMint.copy(alpha = 0.2f)
            } else {
                colors.error.copy(alpha = 0.12f)
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (validationResult is RenderResult.Success) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.Error
                    },
                    contentDescription = null,
                    tint = if (validationResult is RenderResult.Success) {
                        colors.primary
                    } else {
                        colors.error
                    },
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (validationResult is RenderResult.Success) {
                        stringResource(R.string.mermaid_valid_syntax)
                    } else {
                        (validationResult as RenderResult.Error).message
                    },
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (validationResult is RenderResult.Success) {
                            colors.textPrimary
                        } else {
                            colors.error
                        }
                    )
                )
            }
        }
    }
}

@Composable
private fun MermaidPreviewContent(block: EditorBlock.MermaidBlock) {
    val colors = LocalAppColors.current
    if (block.code.trim().isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(16.dp)
                .testTag("editor_mermaid_preview_canvas_${block.id}"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.mermaid_empty_placeholder),
                style = TextStyle(
                    fontSize = 14.sp,
                    color = colors.textTertiary,
                    textAlign = TextAlign.Center
                )
            )
        }
    } else {
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        val isDark = isSystemInDarkTheme()
        val renderResult = remember(block.code, isDark) {
            MermaidRenderer.renderSvg(block.code, isDarkTheme = isDark)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (renderResult is RenderResult.Error) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("editor_mermaid_syntax_badge_${block.id}"),
                    shape = RoundedCornerShape(8.dp),
                    color = colors.error.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, colors.error.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = colors.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = renderResult.message,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = colors.error,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.background)
                    .testTag("editor_mermaid_preview_canvas_${block.id}")
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 4f)
                            offset = Offset(offset.x + pan.x, offset.y + pan.y)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val svgString = if (renderResult is RenderResult.Success) renderResult.svg else ""

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                ) {
                    MermaidSvgView(
                        svgString = svgString,
                        isDark = isDark,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (scale != 1f || offset != Offset.Zero) {
                    SmallFloatingActionButton(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        containerColor = colors.surface,
                        contentColor = colors.primary
                    ) {
                        Text(
                            text = "${(scale * 100).roundToInt()}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun MermaidSvgView(svgString: String, isDark: Boolean, modifier: Modifier = Modifier) {
    if (svgString.isEmpty()) return

    val htmlData = remember(svgString, isDark) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                html, body {
                    width: 100%;
                    height: 100%;
                    background-color: transparent;
                    overflow: hidden;
                }
                .wrapper {
                    width: 100%;
                    height: 100%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    padding: 8px;
                }
                svg {
                    width: 100%;
                    height: 100%;
                    max-width: 100%;
                    max-height: 100%;
                }
            </style>
        </head>
        <body>
            <div class="wrapper">
                $svgString
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    blockNetworkLoads = true
                    loadWithOverviewMode = true
                    useWideViewPort = false
                }
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                "file:///android_asset/mermaid/",
                htmlData,
                "text/html; charset=utf-8",
                "UTF-8",
                null
            )
        },
        modifier = modifier.fillMaxSize()
    )
}
