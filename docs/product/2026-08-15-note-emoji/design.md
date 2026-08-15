# Feature Design — Note Emoji

**Date**: 2026-08-15  
**Status**: Updated — user-approved fix-pass refinement
**Source request**: Add emoji to notes through the existing Note Editor toolbar control.  
**Related spec**: `spec.md`  
**Project design system**: `docs/product/design_system.md`  
**Approved design-system exceptions**: The user-approved fix pass makes the picker a compact
one-third-screen sheet, removes the extra title top inset, and expands the bundled catalog while
preserving the existing component tokens and 48dp targets.

---

## Screens Covered

| # | Screen / Surface | Status |
|---|-----------------|--------|
| 1 | Note Editor — Emoji Picker bottom sheet | Updated |

## Screen 1 — Note Editor — Emoji Picker Bottom Sheet

### Purpose

Extend the existing Note Editor with an expressive text-insertion tool while preserving the editor’s document-first layout and existing toolbar. The picker makes discovery, search, and repeated insertion quick without leaving the editor or changing the note title.

### UX Principles

- Preserve current editor context: opening, choosing, and dismissing the picker never navigates away from the note.
- Keep frequent actions close: Recent is the default category, search is immediately available, and selected emoji insert without an additional confirmation.
- Make state obvious: disabled editing, empty results, the active category, and selected skin tone are communicated with labels/iconography and semantic state—not color alone.
- Reuse the editor’s existing `LocalAppColors`, Material 3 sheet, touch sizing, and flat toolbar patterns. No new accent color, gradient, or component family is introduced.

### Entry And Exit

- **Entry points**: The existing `Insert emoticon` icon in the default Note Editor bottom bar, only while `isEditable` is true.
- **Primary success exit**: No route transition. A selected emoji is inserted at the focused body-text cursor and the picker remains open.
- **Cancel/back behavior**: The sheet’s close icon, tap outside/scrim, Android Back, or predictive Back dismisses the sheet without inserting an emoji; focus returns to the editor’s prior body target when one exists.
- **Failure exit or recovery**: Empty search and Recent states are handled in the sheet. Users can clear search, change category, or dismiss; a local recents read failure falls back to an empty Recent list.

### Information Architecture

1. **Existing bottom tool rail**: Retains the outlined `InsertEmoticon` icon. Enabled state uses `textPrimary`; disabled uses 38% alpha and disabled semantics. It gains `editor_insert_emoji` as its stable test tag.
2. **Emoji picker sheet header**: Standard M3 `ModalBottomSheet` surface (`surface` #FFFFFF in light mode) with 16dp rounded top corners and a compact one-third-screen height. The title “Emoji” starts at the sheet content edge without an additional top inset; a 48dp close action labeled “Close emoji picker” sits beside it.
3. **Search**: Full-width search field using `searchBackground` #EEEFF1 and 12dp corners, a search icon with `searchIcon` #8E959B, localized placeholder “Search emoji”, and a 48dp clear action when text is present.
4. **Category rail**: Horizontally scrollable M3 tab/chip rail in the approved category order: Recent, Smileys & Emotion, People & Body, Animals & Nature, Food & Drink, Activities, Travel & Places, Objects, Symbols, and Flags. The selected category uses `primary` #7C6CF2 plus an explicit selected indicator/semantics.
5. **Emoji results grid**: A virtualized adaptive grid of 48dp minimum emoji cells using the expanded bundled catalog (at least three additional entries in every browse category). The grid scrolls within the compact sheet’s remaining results region. Each cell shows one Unicode emoji and a localized accessible name; eligible items expose a visible/semantically announced skin-tone affordance. A long press opens the compact skin-tone selector; ordinary tap inserts the default variant. The selected variant is inserted exactly as shown.
6. **Skin-tone selector**: Compact anchored selector above the emoji cell with Default plus the five standard modifier choices. It uses `surface`, `border` #E7E3F6, 8dp corners, 48dp targets, selected `primary`, and accessible labels such as “Thumbs up: medium skin tone”.
7. **State panel**: In the results region, an empty state uses `textSecondary` #7B7694 with the exact localized message appropriate to an empty Recent list or no search result. Its clear-search button uses `primary` and a 48dp target.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|-----------|---------|-----------------|----------|
| Existing emoticon toolbar action | Opens picker for editable notes; remains visible but disabled for read-only notes | enabled, pressed, disabled | `editor_insert_emoji` |
| Emoji picker bottom sheet | Contains discovery and selection UI | open, dismissed, editable | `emoji_picker_sheet` |
| Close action | Dismisses without editing | enabled | `emoji_picker_close` |
| Search field | Finds emoji by name/keyword | empty, populated, focused, no results | `emoji_picker_search` |
| Clear-search action | Removes active query | visible only with query | `emoji_picker_clear_search` |
| Category rail | Selects an approved catalog category | selected, unselected, scrollable | `emoji_picker_categories` |
| Category tab | Chooses one category | selected, unselected | `emoji_category_<category-id>` |
| Results grid | Displays current local results efficiently | content, empty Recent, empty search | `emoji_picker_grid` |
| Emoji cell | Inserts the default Unicode emoji | enabled, pressed, skin-tone eligible | `emoji_picker_item_<emoji-id>` |
| Skin-tone selector | Chooses an eligible emoji’s exact variant | collapsed, expanded, selected | `emoji_skin_tone_selector_<emoji-id>` |
| Recent empty panel | Explains that selected emoji appear here | empty | `emoji_picker_recent_empty` |
| Search empty panel | Reports no matching result and offers recovery | empty | `emoji_picker_search_empty` |

Dynamic tag IDs use only immutable catalog identifiers: category tags use the
`EmojiCategory.storageKey`, emoji cells use the bundled catalog item ID (with a
Unicode-derived suffix when a Recent list contains the same catalog item more
than once), and skin-tone options append the immutable tone storage key. They
never use list indexes, timestamps, random IDs, or user-entered text.

### Visual States

| State | User Sees | User Can Do |
|-------|-----------|-------------|
| Editable/closed | Existing toolbar control in `textPrimary`; no added editor chrome. | Open the picker. |
| Read-only/closed | Existing toolbar control remains visible at 38% opacity with disabled semantics. | Cannot open the picker. |
| Recent/content | Sheet with title, search, category rail, selected Recent tab, and emoji grid. | Search, select category, choose emoji, open skin tone, insert repeatedly, dismiss. |
| Category/content | Same sheet with selected category indicator and virtualized emoji grid. | Search, choose emoji/variant, change category, dismiss. |
| Empty Recent | “Your recently used emoji will appear here.” with category rail/search still visible. | Choose another category or search. |
| Empty Search | “No emoji found” and “Clear search” action; category rail remains visible. | Clear query, edit query, change category, dismiss. |
| Skin-tone selector | Compact surface adjacent to the selected eligible cell, with named variants. | Choose a variant or dismiss the selector. |
| Recents fallback | Same as Empty Recent, with catalog fully usable. | Browse, search, insert, and dismiss. |

### Interaction Rules

- **Open picker**: Tapping `editor_insert_emoji` opens a M3 `ModalBottomSheet` only for an editable note. The control has a 48×48dp target and visible ripple/press feedback.
- **Search and category filtering**: Typing filters locally by display name and common keyword. While a query is non-empty, it has precedence over category contents; category selection clears the query and shows that category.
- **Emoji selection**: A normal tap inserts the default Unicode emoji at the current body-text selection/cursor. The picker stays open, focus returns to the same body text block, and the cursor moves after the inserted sequence. The title never receives picker input.
- **No focused block**: An emoji selection appends and focuses a new paragraph, then inserts its Unicode string. No new screen or confirmation is shown.
- **Skin tone**: Long-pressing an eligible cell opens its selector. Choosing a variant inserts the exact selected sequence, keeps the picker open, and records that exact sequence in Recent. A normal tap still inserts the default emoji.
- **Recent**: Each successful selection moves the exact Unicode sequence to the front of Recent and saves it locally. Recent is never written to note content unless the emoji is actually inserted.
- **Dismissal**: Close, scrim, or system Back dismisses the sheet without content change. There are no destructive actions and no confirmation dialog.

### Copy Requirements

| Element | Copy |
|---------|------|
| Picker title | Emoji |
| Search placeholder | Search emoji |
| Recent category | Recent |
| Recent empty state | Your recently used emoji will appear here. |
| Search empty state | No emoji found |
| Clear-search action | Clear search |
| Close content description | Close emoji picker |
| Skin-tone selector content description | Choose skin tone for %1$s |
| Disabled toolbar state description | Emoji insertion is unavailable in a read-only note. |

### Accessibility

- Every interactive item has a stable test tag, a localized content description, 48×48dp minimum target, keyboard-focus treatment, and Material ripple feedback.
- TalkBack order is: close action, title, search, clear search when visible, category tabs, results/empty state, skin-tone selector. Emoji cells announce their localized name and whether skin-tone variants are available; tabs announce selected state.
- `editor_insert_emoji` exposes disabled semantics and the localized state description for read-only notes; it is not focusable as an actionable control in that state.
- The grid and tab rail support RTL traversal, localized copy, system font scaling, and dark-theme semantic tokens. Text labels use `textPrimary` #191627 or `textSecondary` #7B7694 in light mode and their `LocalAppColors` dark counterparts.
- Color is never the only selected/disabled/empty-state cue. The UI uses the selected indicator, state semantics, text labels, and disabled semantics alongside color.

### Responsive And Configuration Behavior

- On phones in portrait, the sheet occupies one-third of the available screen height, respects `WindowInsets.safeDrawing`/gesture insets, and leaves the editor context visible above it. The results grid scrolls when the expanded catalog exceeds the compact results region.
- In landscape and on tablets, the sheet remains width-constrained and the adaptive grid increases columns rather than shrinking below 48dp targets. The horizontally scrollable category rail retains all categories.
- The current query, selected category, sheet visibility, and active skin-tone selector survive configuration changes through screen/presentation state. Persisted Recent reloads after process recreation; no note document mutation occurs until a selection.

### Design Assets

- **Generated mockup**: `design/mockup_note_editor_emoji_picker.png` — AI-generated visual mockup reflecting the existing editor with the new picker open.

### Out Of Scope For This Design

- Custom packs, GIFs/stickers, reactions, note-cover emoji, title insertion, and any new navigation destination.
