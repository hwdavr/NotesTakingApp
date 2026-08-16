# Feature Design — Note Editor Basic Blocks Panel

**Date**: 2026-08-16
**Status**: Approved
**Source request**: Show Basic blocks as an embedded view under the Note Editor toolbar, not as a bottom sheet.
**Related spec**: spec.md
**Project design system**: docs/product/design_system.md
**Approved design-system exceptions**: None. The supplied reference establishes the block-grid concept; all color, typography, surface, spacing, and component decisions use the project design system.

---

## Conditional Keyboard-Visible Mockup Contract

Not applicable. This feature adds an embedded tappable-action panel only; it contains no typing, search, or filtering control and does not independently invoke or resize for the IME.

## Screens Covered

| # | Screen / Surface | Status |
|---|---|---|
| 1 | Note Editor toolbar plus trigger | Updated |
| 2 | Embedded Basic blocks panel | New |

## Screen 1 — Note Editor Toolbar Plus Trigger

### Purpose

Give an editable-note user a single, discoverable way to reveal or hide the Basic blocks panel. The control replaces the former direct paragraph insertion action without changing the rest of the editor toolbar.

### UX Principles

- **Progressive disclosure**: The toolbar stays compact until the user asks to add a block.
- **Continuity**: The catalog expands as part of the Note Editor instead of covering it with a new surface.
- **Safe editing**: Read-only users can see the capability but cannot accidentally invoke a mutation.

### Component Inventory

| Component | Purpose | Required states | Test tag |
|---|---|---|---|
| Basic blocks toolbar trigger | Toggles the inline block panel for editable notes | enabled/closed, enabled/open, pressed, disabled | editor_basic_blocks_trigger |

### Visual States

| State | User sees | User can do |
|---|---|---|
| Editable / closed | Existing toolbar with the plus icon in textPrimary. | Open Basic blocks. |
| Editable / open | Plus icon has primary emphasis; the panel is visibly attached below the toolbar. | Select a block or tap plus to collapse. |
| Read-only | The plus remains visible at 38% opacity with disabled state description. | Understand that block insertion is unavailable; it cannot open the panel. |

### Interaction Rules

- Tap the enabled plus trigger to expand the inline panel; tap it again to collapse.
- The former immediate Add paragraph action is no longer assigned to this trigger; Text in the panel performs that action.
- Android Back collapses the open panel before the existing Note Editor Back action runs.
- Do not use a long press, swipe, or double tap gesture.
- In read-only mode, suppress click handling and expose disabled semantics.

### Accessibility

- Content description: localized Basic blocks action text, such as “Show basic blocks” or “Hide basic blocks” according to the current state.
- Disabled state description: localized text such as “Basic blocks are unavailable in a read-only note”.
- Expanded/collapsed semantics are exposed as a state description.
- Minimum target: 48 by 48 dp.
- Traversal: the plus trigger follows the existing editor toolbar order.

## Screen 2 — Embedded Basic Blocks Panel

### Purpose

Let people select a common empty document block without leaving or overlaying the current note. The panel lives immediately below the editor toolbar; selecting a tile inserts the block after the focused block, focuses it, auto-saves it, and collapses the panel.

### UX Principles

- **Embedded, not modal**: The panel is a normal editor-layout region with no scrim, overlay, elevation, rounded modal edge, drag handle, or swipe dismissal.
- **Reference-informed, product-native**: Keep the supplied reference's two-column block grid and labeled tile affordances, expressed with the app's Material 3 palette and typography.
- **Scannable choice**: Each option pairs a semantic icon with a written label; users do not need to infer meaning from an icon.
- **Immediate commitment**: A tile tap has one clear outcome: insert one empty block and collapse the panel.
- **No Page affordance**: Page is intentionally absent; the feature never implies child-note navigation.

### Entry And Exit

- **Entry**: Tap the enabled Basic blocks toolbar trigger in the Note Editor.
- **Success exit**: Tap a tile. The selected block appears after the focused block (or is appended when no body block is focused), receives focus, and the panel collapses.
- **Cancel / back**: Tap the plus trigger again or press Android Back to collapse the panel without a document change.
- **Read-only behavior**: The panel cannot be expanded.
- **Failure / recovery**: There is no network, permission, loading, or error state. Existing document parsing fallback protects legacy content.

### Information Architecture

1. **Toolbar divider**: A one dp divider separates the existing 56 dp toolbar from the embedded panel.
2. **Section label**: Basic blocks, in the project title/section hierarchy and textPrimary.
3. **Two-column tile grid**: Scrollable collection of actions attached below the section label.
4. **Tile order**:

| Row | Left tile | Right tile |
|---|---|---|
| 1 | Text | Heading 1 |
| 2 | Heading 2 | Heading 3 |
| 3 | Heading 4 | Bulleted list |
| 4 | Numbered list | To-do list |
| 5 | Toggle list | Callout |
| 6 | Quote, spanning both columns | — |

No Page row or blank selectable tile is present.

### Component Inventory

| Component | Purpose | Required states | Test tag |
|---|---|---|---|
| Basic blocks panel | Hosts the attached, height-capped selection grid | hidden, open, collapsed | basic_blocks_panel |
| Panel-toolbar divider | Visual boundary between toolbar and panel | visible when panel is open | basic_blocks_panel_divider |
| Panel title | Announces the selection region | visible when panel is open | basic_blocks_panel_title |
| Scrollable block grid | Reveals all block tiles within the compact panel | top, scrolled, end | basic_blocks_grid |
| Text tile | Inserts paragraph | default, pressed | basic_blocks_text |
| Heading 1 tile | Inserts level-1 heading | default, pressed | basic_blocks_heading_1 |
| Heading 2 tile | Inserts level-2 heading | default, pressed | basic_blocks_heading_2 |
| Heading 3 tile | Inserts level-3 heading | default, pressed | basic_blocks_heading_3 |
| Heading 4 tile | Inserts level-4 heading | default, pressed | basic_blocks_heading_4 |
| Bulleted list tile | Inserts bulleted list item | default, pressed | basic_blocks_bulleted_list |
| Numbered list tile | Inserts numbered list item | default, pressed | basic_blocks_numbered_list |
| To-do list tile | Inserts unchecked task item | default, pressed | basic_blocks_todo_list |
| Toggle list tile | Inserts empty expanded toggle item | default, pressed | basic_blocks_toggle_list |
| Callout tile | Inserts callout | default, pressed | basic_blocks_callout |
| Quote tile | Inserts quote | default, pressed | basic_blocks_quote |

### Reference Anchor Contract

| Reference relationship | Visual bounds node(s) | Required runtime measurement |
|------------------------|-----------------------|------------------------------|
| Panel begins immediately below the editor toolbar | editor_default_bottom_bar, basic_blocks_panel_divider, basic_blocks_panel | dividerBounds.top == toolbarBounds.bottom ± 2dp; panelBounds.top == dividerBounds.bottom ± 2dp |
| Two-column tile grid is inset evenly from panel edges | basic_blocks_panel, basic_blocks_text, basic_blocks_heading_1 | leftTileBounds.left - panelBounds.left == panelBounds.right - rightTileBounds.right ± 2dp |
| Quote is a full-width final tile | basic_blocks_panel, basic_blocks_quote | quoteBounds.left/right align to the grid's left/right bounds ± 2dp |
| Compact panel preserves editor height | editor_default_bottom_bar, basic_blocks_panel | panelBounds.height <= min(280dp, 40% of usable editor height) ± 2dp |
| Tile density reaches the accessibility minimum | basic_blocks_text, basic_blocks_heading_1 | tileBounds.height == 48dp ± 2dp at default font scale |

### Visual Specification

- Compose the panel as a flat layout sibling below the toolbar. Do not use ModalBottomSheet or an overlay container.
- Use the design-system Light Theme baseline: background #F8F7FF for the main editor area, surface #FFFFFF for the toolbar, panel, and tiles, textPrimary #191627 for labels, textSecondary #7B7694 for supporting icon tint, border #E7E3F6 for tile outlines, divider #E7EBF0 at the toolbar boundary, and primary #7C6CF2 for the active toolbar trigger and pressed feedback.
- Keep the existing editor toolbar at 56 dp. The Basic blocks region alone is compact: its content height is capped at min(280 dp, 40% of usable editor height), including its section label and internal vertical padding.
- Use existing platform sans-serif Material typography: compact panel label at 14 sp semibold; tile label 14 sp semibold; icon approximately 20 dp.
- Use 16 dp horizontal panel padding, 8 dp vertical padding, 8 dp grid spacing, 8 dp tile corner radius, 6 dp tile internal spacing, and a 48 dp tile baseline. At increased font scale, a tile may grow only enough to avoid clipping its label.
- Give Quote a full-width final row, rather than a fake blank partner tile.
- Use a LazyVerticalGrid tagged basic_blocks_grid for the bounded tile region. The grid scrolls vertically to Numbered list, To-do list, Toggle list, Callout, and Quote; its top state visibly clips the next row or gives another standard scroll affordance rather than expanding the panel.
- The compact panel is flush with the toolbar and lower screen edge; it never floats over editor content. The editor body receives the recovered vertical space above the unchanged toolbar.
- Dark Theme uses the corresponding semantic LocalAppColors values; no raw colors are introduced in Compose.

### Block Presentation

| Tile | Initial document result | Visible treatment after insertion |
|---|---|---|
| Text | Empty paragraph | Standard editor body text. |
| Heading 1–4 | Empty, distinct heading level | Clearly stepped typography hierarchy. |
| Bulleted list | Empty bulleted item | Bulleted leading marker and editable text. |
| Numbered list | Empty numbered item | Numbered leading marker and editable text. |
| To-do list | Empty unchecked task item | Functional checkbox and editable text. |
| Toggle list | Empty, expanded toggle item | Disclosure affordance with exposed expanded/collapsed state. |
| Callout | Empty callout | Subtle app-semantic informational container with iconography as needed. |
| Quote | Empty quote | Quotation treatment with a non-color-only visual indicator, such as a leading rule or quote icon. |

### Interaction Rules

- A single tile tap starts exactly one insert action and collapses the panel once the ViewModel accepts it.
- The tile inserts after the focused document block; when no body block is focused, it appends at the end.
- The new block is focused immediately so typing can begin without an extra selection step.
- The plus control toggles the panel. Android Back collapses an open panel; no scrim, swipe, drag gesture, or close button exists.
- A vertical grid scroll is supported with TalkBack scroll actions and ordinary touch scrolling; it must reveal every tile, including the full-width Quote row, without changing the panel cap.
- Rapid repeated taps cannot insert duplicate blocks while the panel is collapsing.
- The panel does not contain search, filters, Page, nested-category navigation, or typing controls.

### Accessibility

- Each tile exposes a localized content description that includes its block name and an action such as “Insert Heading 2”.
- Tile semantics identify an interactive button/action, not a decorative card.
- The Basic blocks label is an announced heading. The panel exposes expanded/collapsed state through the toolbar trigger.
- Focus order is toolbar trigger, then panel title, then tiles in visual reading order, with Quote last.
- Baseline tap target is 48 by 48 dp; labels scale and wrap or the tile grows while the panel grid scrolls instead of clipping.
- State changes after insertion are announced through focus moving to the new editable block.
- Read-only trigger semantics state disabled availability rather than silently consuming a tap.

### Responsive And Configuration Behavior

- On phones down to the project's supported narrow width, retain two equal columns with a minimum touch target and allow labels to wrap when required.
- On landscape and smaller available height, use the lesser of 280 dp or 40% of usable editor height and retain panel scrolling instead of squeezing rows below the 48 dp baseline.
- On tablets, retain the attached editor-panel relationship and two-column grid; do not promote it to a full-screen or overlay menu.
- Panel visibility is transient. Its collapsed/expanded state may reset after a configuration change; document content, focus target, and persisted toggle state remain ViewModel-backed and survive normal editor restoration.

### Design Assets

- Unmodified user-provided source reference: design/source-photo-1.jpg. It contains a Page tile and overlay-style source treatment, so it is evidence for the requested catalog concept only.
- Current approval-candidate mockup: design/mockup_basic_blocks_panel_compact.png. It shows the compact, scrollable, Page-free panel attached directly below the toolbar; only the first three rows and part of the next row are visible at the initial scroll position.
- Superseded mockups: design/mockup_basic_blocks_panel.png and design/mockup_basic_blocks_sheet.png. They record earlier taller and modal concepts and are not approval targets.
- Mockup generation method: built-in image generation, using the prior inline-panel mockup as an edit target. The final prompt required the capped compact panel, 48 dp baseline tiles, a visible vertical scroll affordance, the exact block catalog, and the tokens in docs/product/design_system.md.
- Keyboard-visible mockup: Not applicable; the panel has no typing controls.

### Out Of Scope For This Design

- Modal or overlay selector behavior.
- Page / child-note creation and navigation.
- Search, recent blocks, favorites, categories, and sort controls.
- Block preview thumbnails or long descriptions.
- Nested toggle content, drag-to-reorder, slash commands, and multi-select block actions.
- New app colors, gradients, glassmorphism, or a component family that diverges from the existing editor.
