@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.notesapp.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesapp.R
import com.example.notesapp.domain.emoji.EmojiCategory
import com.example.notesapp.ui.editor.model.EmojiCategoryUiModel
import com.example.notesapp.ui.editor.model.EmojiPickerItemUiModel
import com.example.notesapp.ui.editor.model.EmojiPickerUiState
import com.example.notesapp.ui.editor.model.EmojiVariantUiModel
import com.example.notesapp.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPickerBottomSheet(
    uiState: EmojiPickerUiState,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onCategorySelected: (EmojiCategory) -> Unit,
    onEmojiSelected: (String) -> Unit,
    onSkinToneRequested: (String) -> Unit,
    onSkinToneDismissed: () -> Unit
) {
    val colors = LocalAppColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxHeight(1f / 3f)
            .testTag("emoji_picker_sheet"),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            EmojiPickerHeader(onDismiss = onDismiss)
            EmojiPickerSearchField(
                query = uiState.query,
                onQueryChange = onQueryChange,
                onClearQuery = onClearQuery
            )
            EmojiPickerCategoryRail(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = onCategorySelected
            )
            EmojiPickerResults(
                uiState = uiState,
                onEmojiSelected = onEmojiSelected,
                onSkinToneRequested = onSkinToneRequested,
                onSkinToneDismissed = onSkinToneDismissed,
                onClearQuery = onClearQuery,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EmojiPickerHeader(onDismiss: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.emoji_picker_title),
            color = colors.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(48.dp)
                .testTag("emoji_picker_close")
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.emoji_picker_close_description),
                tint = colors.textPrimary
            )
        }
    }
}

@Composable
private fun EmojiPickerSearchField(query: String, onQueryChange: (String) -> Unit, onClearQuery: () -> Unit) {
    val colors = LocalAppColors.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("emoji_picker_search"),
        singleLine = true,
        placeholder = { Text(stringResource(R.string.emoji_picker_search_placeholder)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = colors.searchIcon
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(
                    onClick = onClearQuery,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("emoji_picker_clear_search")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.emoji_picker_clear_search),
                        tint = colors.textSecondary
                    )
                }
            }
        } else {
            null
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.searchBackground,
            unfocusedContainerColor = colors.searchBackground,
            focusedBorderColor = colors.border,
            unfocusedBorderColor = colors.border,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            focusedPlaceholderColor = colors.textSecondary,
            unfocusedPlaceholderColor = colors.textSecondary
        )
    )
}

@Composable
private fun EmojiPickerCategoryRail(
    categories: List<EmojiCategoryUiModel>,
    selectedCategory: EmojiCategory,
    onCategorySelected: (EmojiCategory) -> Unit
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .testTag("emoji_picker_categories"),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category.id == selectedCategory
            val categoryLabel = stringResource(category.labelRes)
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) colors.highlight else colors.surface)
                    .combinedClickable(onClick = { onCategorySelected(category.id) })
                    .semantics(mergeDescendants = true) {
                        contentDescription = categoryLabel
                        selected = isSelected
                        role = Role.Tab
                    }
                    .testTag("emoji_category_${category.id.storageKey}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = categoryLabel,
                    color = if (isSelected) colors.primary else colors.textPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun EmojiPickerResults(
    uiState: EmojiPickerUiState,
    onEmojiSelected: (String) -> Unit,
    onSkinToneRequested: (String) -> Unit,
    onSkinToneDismissed: () -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .testTag("emoji_picker_loading"),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.emoji_picker_loading), color = LocalAppColors.current.textSecondary)
            }
        }
        uiState.hasCatalogError -> {
            EmojiPickerEmptyState(
                message = stringResource(R.string.emoji_picker_catalog_error),
                testTag = "emoji_picker_catalog_error",
                modifier = modifier
            )
        }
        uiState.isEmptyRecent -> {
            EmojiPickerEmptyState(
                message = stringResource(R.string.emoji_picker_recent_empty),
                testTag = "emoji_picker_recent_empty",
                modifier = modifier
            )
        }
        uiState.isEmptySearch -> {
            EmojiPickerEmptyState(
                message = stringResource(R.string.emoji_picker_no_results),
                testTag = "emoji_picker_search_empty",
                modifier = modifier,
                action = {
                    TextButton(
                        onClick = onClearQuery,
                        modifier = Modifier.testTag("emoji_picker_clear_search_empty")
                    ) {
                        Text(stringResource(R.string.emoji_picker_clear_search))
                    }
                }
            )
        }
        uiState.isEmptyCategory -> {
            EmojiPickerEmptyState(
                message = stringResource(R.string.emoji_picker_category_empty),
                testTag = "emoji_picker_category_empty",
                modifier = modifier
            )
        }
        else -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 48.dp),
                modifier = modifier
                    .fillMaxWidth()
                    .testTag("emoji_picker_grid"),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                gridItems(uiState.items, key = { item -> item.id }) { item ->
                    EmojiPickerItem(
                        item = item,
                        isSkinToneSelectorVisible = item.id == uiState.activeSkinToneItemId,
                        onEmojiSelected = onEmojiSelected,
                        onSkinToneRequested = onSkinToneRequested,
                        onSkinToneDismissed = onSkinToneDismissed
                    )
                }
            }
        }
    }
}

@Composable
private fun EmojiPickerEmptyState(
    message: String,
    testTag: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = colors.textSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        action?.invoke()
    }
}

@Composable
private fun EmojiPickerItem(
    item: EmojiPickerItemUiModel,
    isSkinToneSelectorVisible: Boolean,
    onEmojiSelected: (String) -> Unit,
    onSkinToneRequested: (String) -> Unit,
    onSkinToneDismissed: () -> Unit
) {
    val colors = LocalAppColors.current
    val itemName = stringResource(item.nameRes)
    val hasVariants = item.variants.size > 1
    val itemDescription = if (hasVariants) {
        stringResource(
            R.string.emoji_picker_item_accessibility_description,
            itemName,
            stringResource(R.string.emoji_picker_item_skin_tone_hint)
        )
    } else {
        stringResource(R.string.emoji_picker_item_name_description, itemName)
    }
    Box {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface)
                .combinedClickable(
                    onClick = { onEmojiSelected(item.unicode) },
                    onLongClick = if (hasVariants) {
                        { onSkinToneRequested(item.id) }
                    } else {
                        null
                    }
                )
                .semantics(mergeDescendants = true) {
                    contentDescription = itemDescription
                    role = Role.Button
                }
                .testTag("emoji_picker_item_${item.id}"),
            contentAlignment = Alignment.Center
        ) {
            Text(text = item.unicode, fontSize = 28.sp)
        }
        if (isSkinToneSelectorVisible) {
            EmojiSkinToneSelector(
                item = item,
                onEmojiSelected = onEmojiSelected,
                onDismiss = onSkinToneDismissed
            )
        }
    }
}

@Composable
private fun EmojiSkinToneSelector(
    item: EmojiPickerItemUiModel,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val selectorDescription = stringResource(
        R.string.emoji_picker_skin_tone_selector_description,
        stringResource(item.nameRes)
    )
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .testTag("emoji_skin_tone_selector_${item.id}")
            .semantics { contentDescription = selectorDescription }
    ) {
        item.variants.forEach { variant ->
            EmojiSkinToneOption(
                item = item,
                variant = variant,
                onClick = {
                    onEmojiSelected(variant.unicode)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun EmojiSkinToneOption(item: EmojiPickerItemUiModel, variant: EmojiVariantUiModel, onClick: () -> Unit) {
    val itemName = stringResource(item.nameRes)
    val toneName = stringResource(variant.labelRes)
    val optionDescription = stringResource(
        R.string.emoji_picker_skin_tone_option_description,
        itemName,
        toneName
    )
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = variant.unicode, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(variant.labelRes))
            }
        },
        onClick = onClick,
        modifier = Modifier
            .testTag("emoji_skin_tone_variant_${item.id}_${variant.tone.storageKey}")
            .semantics {
                contentDescription = optionDescription
                stateDescription = toneName
            }
    )
}
