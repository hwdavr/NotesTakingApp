# Feature Spec — Note Editor Basic Blocks Panel

**Date**: 2026-08-16
**Status**: Approved
**Related design**: design.md

---

## Objective

Add an embedded Basic blocks panel directly below the existing Note Editor toolbar so people can choose a document block type instead of adding only a paragraph. The inline panel keeps the editor, toolbar, and block catalog in one continuous layout without a modal overlay.

## User Goal

As a person editing a note, I want to choose a basic content block from the toolbar so that I can add structured note content at the point where I am working without leaving or obscuring my note.

## Scope

### In Scope

- Replace the editable Note Editor toolbar plus action's direct paragraph insertion with an inline Basic blocks panel directly beneath the toolbar.
- Present a two-column panel with exactly these 11 block types: Text, Heading 1, Heading 2, Heading 3, Heading 4, Bulleted list, Numbered list, To-do list, Toggle list, Callout, and Quote.
- Insert the selected empty block immediately after the focused document block, focus it for editing, collapse the inline panel, and use the existing auto-save path.
- When no body block is focused, append the selected block to the end of the current note.
- Persist and render the selected block type, including the new heading levels, numbered list, toggle list, callout, and quote types.
- Create a Toggle list as an empty, expanded item.
- Keep the toolbar trigger visible but disabled for read-only notes.
- Let the plus control toggle the inline panel. Android Back collapses an open panel before the editor performs its normal Back behavior.
- Keep the existing 56 dp editor toolbar unchanged while limiting the embedded Basic blocks panel to a compact height of min(280 dp, 40% of usable editor height).
- Use 48 dp baseline block tiles in a vertically scrollable grid so the remaining block types are reachable without enlarging the panel.
- Use the supplied reference as the catalog-layout source while applying the project Material 3 design system.

### Out Of Scope

- Modal, overlay, or swipe-dismiss surfaces for Basic blocks.
- Page blocks, child notes, or navigation to a newly created page. The Page tile is explicitly removed from this feature.
- Images, tables, voice notes, links, mentions, undo/redo, and other existing toolbar actions.
- Nested lists, drag-and-drop ordering, slash commands, block search, favorites, recents, or custom block templates.
- Multi-level toggle nesting and moving existing blocks under a toggle.
- Changes to Room schema, network APIs, permissions, or external services.

## Technical Spec

### Libraries & Dependencies

| Library / SDK | Version | Purpose |
|---------------|---------|---------|
| Jetpack Compose Material 3 | Existing project version | Render the embedded panel, block tiles, and semantics. |
| Kotlin coroutines / StateFlow | Existing project version | Keep document updates in unidirectional presentation state. |
| Room | Existing project version | Persist the existing Note content field through the established auto-save path. |
| Hilt | Existing project version | Retain existing Note Editor dependency wiring. |

No new dependency is required.

### Key Technical Decisions

- **Toolbar trigger**: The existing editable toolbar plus action becomes the Basic blocks panel trigger. Its former direct paragraph behavior moves to the Text tile.
- **Panel state and placement**: The screen owns transient isBasicBlocksPanelVisible UI state. When visible, the panel is composed as a normal sibling immediately after the unchanged 56 dp editor toolbar, not as an overlay. Its content region is capped at min(280 dp, 40% of usable editor height), so the editor content regains the remaining vertical space above it.
- **Density and scrolling**: The block collection uses a vertically scrollable LazyVerticalGrid. Tiles have a 48 dp baseline height and compact spacing; a tile may grow only when font scaling requires it, while the bounded panel remains scrollable.
- **Dismissal**: Tapping the plus control toggles the panel. An inner BackHandler consumes Android Back to collapse the open panel before the existing editor Back handler runs. There is no scrim tap, drag handle, swipe-to-dismiss behavior, or modal navigation state.
- **Block representation**: Extend the existing JSON-backed EditorBlock / NoteDocument representation with stable basic-block type identifiers and any needed toggle-expanded state. Existing paragraph, heading, bulleted, and checkbox documents remain readable; existing generic heading content renders as Heading 1.
- **Insertion rule**: The ViewModel resolves the focused block's index and inserts the new block at index + 1. If no focused body block exists, it appends the block. The newly inserted block becomes focused with a collapsed zero selection.
- **New block defaults**: Text, headings, lists, callout, and quote begin with an empty editable text value. To-do begins unchecked. Toggle list begins empty and expanded.
- **Type semantics**: Heading 1–4 retain distinct hierarchy; Bulleted and Numbered lists retain their list marker; To-do retains a functional checked state; Toggle list has a functional expanded/collapsed state; Callout and Quote retain their visual semantic type after save and reload.
- **Compatibility**: JSON decoding must tolerate older editor documents and unknown values without losing existing note content. The existing Note.content field remains the sole persistence location.
- **Localization and testing**: All new visible copy, content descriptions, and state descriptions are string resources. Every interactive trigger and tile has a stable test tag.

### External APIs / Services

None. This is an on-device editor and persistence change.

### Platform & Compatibility Constraints

- **Min SDK**: Project default, API 24.
- **Permissions required**: None.
- **Compatibility**: Light and dark app themes, font scaling, narrow phones, landscape, tablets, TalkBack, keyboard focus, and Android Back navigation must remain supported.

## Functional Requirements

- **FR-001**: In an editable Note Editor, tapping the existing toolbar plus control MUST expand or collapse the embedded Basic blocks panel instead of immediately adding a paragraph.
- **FR-002**: When expanded, the Basic blocks panel MUST be a normal, non-modal editor-layout region immediately below the unchanged 56 dp toolbar; it MUST NOT use an overlay, dimming scrim, rounded modal surface, drag handle, or swipe-dismiss affordance.
- **FR-003**: The panel MUST use a two-column grid and show exactly these localized tiles in reading order: Text, Heading 1, Heading 2, Heading 3, Heading 4, Bulleted list, Numbered list, To-do list, Toggle list, Callout, and Quote.
- **FR-004**: The panel MUST NOT show a Page tile or create, navigate to, or link any page or child note.
- **FR-005**: Each Basic blocks tile MUST be an accessible, independently tappable action with a localized label, content description, stable test tag, and at least a 48 by 48 dp touch target.
- **FR-006**: When a user selects a Basic blocks tile while a document block is focused, the ViewModel MUST insert an empty block of the selected type immediately after that focused block.
- **FR-007**: When a user selects a Basic blocks tile with no body block focused, the ViewModel MUST append an empty block of the selected type to the end of the document.
- **FR-008**: After a successful insertion, the new block MUST receive editor focus, use a collapsed zero-length selection, update the visible document immediately, schedule the existing auto-save, and collapse the Basic blocks panel.
- **FR-009**: The Text tile MUST insert a regular editable paragraph; Heading 1–4 MUST insert four distinguishable heading levels; Bulleted list and Numbered list MUST insert their respective list-item types; To-do list MUST insert an unchecked task item.
- **FR-010**: The Toggle list tile MUST insert an empty, expanded toggle item. Its expansion control MUST expose expanded/collapsed semantics and preserve its state when the note is saved and reloaded.
- **FR-011**: The Callout and Quote tiles MUST insert editable blocks that retain their visual and semantic type after auto-save and reload.
- **FR-012**: When the panel is open, a second plus-control tap or Android Back MUST collapse it without changing the document. Android Back after it is closed follows the existing editor behavior.
- **FR-013**: In a read-only note, the plus control MUST remain visible, use disabled appearance and semantics, and MUST NOT expand the panel or mutate the note.
- **FR-014**: The panel, toolbar trigger, and tiles MUST follow docs/product/design_system.md: semantic colors, app typography, spacing, a flat surface and divider, safe drawing insets, and dark-theme equivalents.
- **FR-015**: The panel content region MUST be capped at min(280 dp, 40% of usable editor height), with the existing toolbar remaining 56 dp tall.
- **FR-016**: Tiles MUST use a 48 dp baseline height, 8 dp grid spacing, and vertically scrollable grid content. The grid MUST expose all 11 tile actions without increasing the compact panel cap.
- **FR-017**: The panel MUST remain usable at increased font scale, on narrow phones, in landscape, and on tablets; its grid content MUST scroll rather than clip. A tile may expand beyond its baseline only to prevent text clipping at the user's font scale.
- **FR-018**: Existing notes and existing editor block types MUST continue to load, edit, export, and persist without data loss.
- **FR-019**: The Basic blocks panel MUST contain no typing, search, or filtering control; it does not independently invoke the IME.

## Acceptance Criteria

- **AC-001**: Given an editable loaded note with the panel closed, when the user taps the toolbar plus control, then a Basic blocks panel expands directly beneath the toolbar, the editor content remains part of the same layout, and no overlay or dimming scrim appears.
- **AC-002**: Given the panel is open, when the user views its grid, then it contains exactly the 11 specified labels in two columns, with Quote spanning the final row, and does not include Page.
- **AC-003**: Given the panel is open, when TalkBack or keyboard focus traverses a tile, then the tile has a localized label, role, and clear selected-block action without relying on its icon alone.
- **AC-004**: Given a standard phone viewport, when the panel opens, then the toolbar remains 56 dp high, the panel does not exceed min(280 dp, 40% of usable editor height), and its tile baseline is 48 dp.
- **AC-005**: Given the panel opens at its compact height, when the user scrolls its grid, then every tile from Text through Quote becomes reachable without expanding the panel.
- **AC-006**: Given a focused text, image, table, or voice block, when the user selects Heading 2, then an empty Heading 2 block appears immediately after that block, becomes focused, and the panel collapses.
- **AC-007**: Given no body block is focused, when the user selects Text, then an empty paragraph is appended to the end of the note, becomes focused, and the panel collapses.
- **AC-008**: Given an editable note, when the user selects each supported basic block type, then the visible inserted block has its expected type and initial state: empty text; correct heading/list marker; unchecked to-do; expanded toggle; callout; or quote.
- **AC-009**: Given an expanded empty Toggle list, when the user toggles its expansion control, then the control exposes the resulting expanded/collapsed state and the state remains after save and editor reload.
- **AC-010**: Given a newly inserted block, when the existing auto-save completes and the note is reopened, then its block order, type, text, to-do state, and toggle expanded state are preserved.
- **AC-011**: Given the panel is open, when the user taps the plus trigger again or presses Android Back, then the panel collapses and no block is inserted.
- **AC-012**: Given a read-only note, when the editor renders, then the plus control is visible with disabled semantics; when it is tapped, then no panel expands and the document remains unchanged.
- **AC-013**: Given a user uses increased font scale, a narrow phone, landscape, or a tablet, when the panel expands, then all tile labels remain accessible without clipping and the grid can scroll.
- **AC-014**: Given an existing note made before this feature, when it loads, edits, saves, exports, and reloads, then its existing blocks retain their content and behavior.

## Data And Persistence

- The feature persists in the existing JSON document stored in Note.content. It does not add a Room column, entity, DAO, or migration.
- New basic-block types and Toggle list expanded/collapsed state are serialized into the existing NoteDocument representation.
- A newly selected block is initially empty and receives a new stable block identifier.
- Existing paragraph, heading, bulleted, checkbox, image, table, and voice blocks remain backward compatible. A legacy heading type maps to Heading 1 when rendered.
- Inline-panel visibility is transient UI state only and is never written to a note.

## Edge Cases

| Scenario | Required behavior |
|----------|-------------------|
| No focused body block | Append the selected block at the document end and focus it. |
| Focused non-text block | Insert after that focused block without replacing it. |
| Empty new note | Insert after the editor's initial empty paragraph unless focus has been cleared, in which case append. |
| Panel toggle | Plus opens a closed panel and closes an open panel without a document mutation. |
| Android Back while panel open | Collapse only the panel; do not leave the current note. |
| Compact viewport | Keep the toolbar at 56 dp; cap the panel at min(280 dp, 40% of usable height) and scroll its grid. |
| Read-only note | Render the plus control disabled; block selection is unavailable and no mutation occurs. |
| Rapid tile taps | Accept only the first successful selection while the panel is collapsing; do not duplicate inserts. |
| Unknown stored block type | Preserve readable content safely and do not crash; use the existing compatibility fallback. |
| Toggle state | Keep the user-visible expanded/collapsed state after auto-save and reload. |
| Small viewport / large text | Permit vertical scrolling; do not truncate a tile's label or make it unreachable. |

## Explicit Assumptions

| # | Assumption | Risk if Wrong |
|---|------------|---------------|
| A1 | A newly inserted block receives focus so the user can type without another tap. | Low; visible in this approval artifact. |
| A2 | Heading 1–4 are separate stored/rendered levels, with legacy generic headings presented as Heading 1. | Medium; protects compatibility while enabling the requested labels. |
| A3 | Toggle list is a collapsible single-item block in this increment; nested children are deliberately out of scope. | Medium; a later feature can add nested toggle content without changing the panel contract. |
| A4 | Android Back collapses the inline panel before normal editor navigation, preserving the prior dismissal expectation without restoring a modal surface. | Low; it matches the panel's transient UI state and is explicit in this revision. |
| A5 | “Bottom tool area” refers to the embedded Basic blocks panel; the pre-existing 56 dp editor toolbar remains unchanged to retain its existing touch targets. | Low; the compact mockup makes the scope visible for approval. |

## Open Questions

All questions are answered before specification approval.

| # | Question | Status | Answer |
|---|----------|--------|--------|
| Q1 | What happens when a Basic blocks tile is tapped? | ✅ Answered | Insert the selected block and collapse the Basic blocks panel. |
| Q2 | Which tiles belong in the first picker? | ✅ Answered | Use the reference catalog, then remove Page: 11 tiles remain. |
| Q3 | Where is a selected block inserted? | ✅ Answered | Immediately after the focused block. |
| Q4 | What happens with no focused body block? | ✅ Answered | Append the selected block to the end of the note. |
| Q5 | How does the plus control behave in a read-only note? | ✅ Answered | It remains visible but disabled. |
| Q6 | How should the visual reference be applied? | ✅ Answered | Reuse its two-column grid concept with the app's Material 3 colors and typography. |
| Q7 | How is the selector dismissed? | ✅ Answered | The plus toggles the panel; Android Back collapses an open panel. There is no modal dismissal gesture. |
| Q8 | What should the Page tile do? | ✅ Answered | Remove it from this feature. |
| Q9 | What is the default Toggle list state? | ✅ Answered | Insert an empty, expanded toggle item. |
| Q10 | Should the selector be modal? | ✅ Answered | No. It is an embedded view directly under the editor toolbar. |
| Q11 | How should the embedded tool area be sized? | ✅ Answered | Make the Basic blocks panel compact, reduce tile height, and make the grid scrollable. |

## Screen States

| State | Requirement | Acceptance Criteria |
|-------|-------------|---------------------|
| Editable / panel closed | The enabled plus trigger is visible in the existing toolbar; no selector occupies editor space. | AC-001 |
| Editable / panel open | The Basic blocks grid is a compact, inline region directly below the toolbar; no modal or keyboard is shown. The grid scrolls to hidden rows. | AC-001 through AC-005, AC-013 |
| Selection committed | Exactly one requested block appears after the focused block (or at end), receives focus, persists, and panel collapses. | AC-006 through AC-010 |
| Panel collapsed | The panel returns to hidden state without a document mutation. | AC-011 |
| Read-only | The plus trigger remains visible but disabled and the panel cannot expand. | AC-012 |
| Legacy document | Existing block content remains available and unchanged after the new type support is introduced. | AC-014 |

## Navigation

- **Entry**: Existing Note Editor toolbar plus control.
- **Back/cancel**: Android Back collapses an open inline panel. Once it is closed, existing editor Back behavior applies.
- **Success**: A tile selection stays on the current Note Editor, focuses the new block, and collapses the panel.
- **Error recovery**: There is no network or permission failure path. A compatibility read failure uses the existing safe document fallback rather than crashing the editor.

## Design Traceability

The visual, interaction, accessibility, responsive, and test-tag requirements are defined in design.md. The current approval-candidate asset is design/mockup_basic_blocks_panel_compact.png; design/mockup_basic_blocks_panel.png and design/mockup_basic_blocks_sheet.png are retained only as superseded concepts; the unmodified supplied source reference is design/source-photo-1.jpg.

## Verification Expectations

- **JVM unit tests**: Cover insertion order, no-focus append behavior, every initial type/default, read-only rejection, toggle-state persistence, and old-document compatibility through the NoteEditorViewModel and NoteDocument.
- **Instrumented UI tests**: Exercise the shipped toolbar trigger, compact inline-panel bounds, grid scroll from Text through Quote, Page absence, tile action, plus/Back collapse paths, read-only disabled behavior, and accessibility/test-tag semantics in a real Android runtime.
- **Runtime visual verification**: Compare the compact embedded panel against design/mockup_basic_blocks_panel_compact.png in Light Theme, including toolbar/panel bounds, 48 dp tile baseline, clipped-next-row/scroll state, and large-font scrolling; verify semantic Dark Theme rendering.
- **Required commands before delivery**: ./gradlew testDebugUnitTest, ./gradlew connectedDebugAndroidTest, ./gradlew koverLog, ./gradlew ktlintCheck, and ./gradlew detekt.
