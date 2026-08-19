package com.example.notesapp.ui.editor.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.AutoAwesomeMosaic
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesapp.R
import com.example.notesapp.ui.editor.mapper.BasicBlockType
import com.example.notesapp.ui.theme.LocalAppColors

@Composable
fun BasicBlocksPanel(onTileSelected: (BasicBlockType) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .testTag("basic_blocks_panel")
            .background(colors.surface)
    ) {
        val maxPanelHeight = minOf(280.dp, maxHeight * 0.4f)
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.basic_blocks_panel_title),
                modifier = Modifier
                    .testTag("basic_blocks_panel_title")
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 4.dp),
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxPanelHeight - 28.dp)
                    .testTag("basic_blocks_grid"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                basicBlocksSections.forEach { section ->
                    item(
                        key = section.testTag,
                        span = { GridItemSpan(2) }
                    ) {
                        BasicBlocksSectionHeader(section)
                    }
                    items(
                        items = section.tiles,
                        key = { it.testTag },
                        span = { tile ->
                            if (tile.type == BasicBlockType.QUOTE) {
                                GridItemSpan(2)
                            } else {
                                GridItemSpan(1)
                            }
                        }
                    ) { tile ->
                        BasicBlockTile(
                            tile = tile,
                            onClick = { onTileSelected(tile.type) }
                        )
                    }
                }
            }
        }
    }
}

data class BasicBlockTileItem(
    val type: BasicBlockType,
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int,
    val testTag: String,
    val icon: ImageVector
)

private val basicBlockTiles = listOf(
    BasicBlockTileItem(
        type = BasicBlockType.PARAGRAPH,
        labelRes = R.string.basic_blocks_text_label,
        descriptionRes = R.string.basic_blocks_text_description,
        testTag = "basic_blocks_text",
        icon = Icons.AutoMirrored.Outlined.Notes
    ),
    BasicBlockTileItem(
        type = BasicBlockType.HEADING_1,
        labelRes = R.string.basic_blocks_heading_1_label,
        descriptionRes = R.string.basic_blocks_heading_1_description,
        testTag = "basic_blocks_heading_1",
        icon = Icons.Outlined.Title
    ),
    BasicBlockTileItem(
        type = BasicBlockType.HEADING_2,
        labelRes = R.string.basic_blocks_heading_2_label,
        descriptionRes = R.string.basic_blocks_heading_2_description,
        testTag = "basic_blocks_heading_2",
        icon = Icons.Outlined.Title
    ),
    BasicBlockTileItem(
        type = BasicBlockType.HEADING_3,
        labelRes = R.string.basic_blocks_heading_3_label,
        descriptionRes = R.string.basic_blocks_heading_3_description,
        testTag = "basic_blocks_heading_3",
        icon = Icons.Outlined.Title
    ),
    BasicBlockTileItem(
        type = BasicBlockType.HEADING_4,
        labelRes = R.string.basic_blocks_heading_4_label,
        descriptionRes = R.string.basic_blocks_heading_4_description,
        testTag = "basic_blocks_heading_4",
        icon = Icons.Outlined.Title
    ),
    BasicBlockTileItem(
        type = BasicBlockType.BULLETED_LIST,
        labelRes = R.string.basic_blocks_bulleted_list_label,
        descriptionRes = R.string.basic_blocks_bulleted_list_description,
        testTag = "basic_blocks_bulleted_list",
        icon = Icons.AutoMirrored.Outlined.FormatListBulleted
    ),
    BasicBlockTileItem(
        type = BasicBlockType.NUMBERED_LIST,
        labelRes = R.string.basic_blocks_numbered_list_label,
        descriptionRes = R.string.basic_blocks_numbered_list_description,
        testTag = "basic_blocks_numbered_list",
        icon = Icons.Outlined.FormatListNumbered
    ),
    BasicBlockTileItem(
        type = BasicBlockType.TODO_LIST,
        labelRes = R.string.basic_blocks_todo_list_label,
        descriptionRes = R.string.basic_blocks_todo_list_description,
        testTag = "basic_blocks_todo_list",
        icon = Icons.Outlined.CheckBoxOutlineBlank
    ),
    BasicBlockTileItem(
        type = BasicBlockType.TOGGLE_LIST,
        labelRes = R.string.basic_blocks_toggle_list_label,
        descriptionRes = R.string.basic_blocks_toggle_list_description,
        testTag = "basic_blocks_toggle_list",
        icon = Icons.AutoMirrored.Outlined.KeyboardArrowRight
    ),
    BasicBlockTileItem(
        type = BasicBlockType.CALLOUT,
        labelRes = R.string.basic_blocks_callout_label,
        descriptionRes = R.string.basic_blocks_callout_description,
        testTag = "basic_blocks_callout",
        icon = Icons.Outlined.Info
    ),
    BasicBlockTileItem(
        type = BasicBlockType.QUOTE,
        labelRes = R.string.basic_blocks_quote_label,
        descriptionRes = R.string.basic_blocks_quote_description,
        testTag = "basic_blocks_quote",
        icon = Icons.Outlined.FormatQuote
    )
)

private val advancedBlockTiles = listOf(
    BasicBlockTileItem(
        type = BasicBlockType.CODE,
        labelRes = R.string.basic_blocks_code_label,
        descriptionRes = R.string.basic_blocks_code_description,
        testTag = "basic_blocks_code",
        icon = Icons.Outlined.Code
    ),
    BasicBlockTileItem(
        type = BasicBlockType.MERMAID,
        labelRes = R.string.basic_blocks_mermaid_label,
        descriptionRes = R.string.basic_blocks_mermaid_description,
        testTag = "basic_blocks_mermaid",
        icon = Icons.Outlined.AutoAwesomeMosaic
    )
)

data class BasicBlockSection(
    @StringRes val titleRes: Int,
    val testTag: String,
    val tiles: List<BasicBlockTileItem>
)

val basicBlocksSections = listOf(
    BasicBlockSection(
        titleRes = R.string.basic_blocks_section_basic,
        testTag = "basic_blocks_section_basic",
        tiles = basicBlockTiles
    ),
    BasicBlockSection(
        titleRes = R.string.basic_blocks_section_advanced,
        testTag = "basic_blocks_section_advanced",
        tiles = advancedBlockTiles
    )
)

val approvedBasicBlockTiles: List<BasicBlockTileItem> = basicBlockTiles + advancedBlockTiles

@Composable
private fun BasicBlocksSectionHeader(section: BasicBlockSection) {
    val colors = LocalAppColors.current
    Text(
        text = stringResource(section.titleRes),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(section.testTag)
            .padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
        color = colors.textSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun BasicBlockTile(tile: BasicBlockTileItem, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val tileDescription = stringResource(tile.descriptionRes)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, colors.border), RoundedCornerShape(8.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = tileDescription
            }
            .testTag(tile.testTag),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = tile.icon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(tile.labelRes),
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
