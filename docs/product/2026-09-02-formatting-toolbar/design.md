# Feature Design — Formatting Toolbar Completion

**Date**: 2026-09-02
**Status**: Approved for slice planning; awaiting implementation approval
**Source request**: Plan the remaining Note Editor formatting-toolbar controls.
**Related spec**: `spec.md`
**Project design system**: `docs/product/design_system.md`
**Approved design-system exceptions**: User-approved on 2026-09-03: when the Note Editor IME is visible, keep the formatting toolbar visible above the keyboard; it must not overlap the keyboard or focused input. This overrides the default IME toolbar-dismissal guidance for this editor state.

## Conditional Keyboard-Visible Mockup Contract

The Note Editor contains text input and a bottom formatting toolbar; while the editor IME is visible, that toolbar remains visible above the keyboard. The formula bottom sheet contains text input; it stays open and expands above the IME. See `design/mockup_editor_formatting_keyboard.png` and `design/mockup_formula_sheet_keyboard.png`.

## Screens Covered

| # | Screen / Surface | Status |
|---|---|---|
| 1 | Note Editor formatting toolbar | Updated |
| 2 | Link to note picker | New destination |
| 3 | Inline formula sheet | New overlay |

## Screen 1 — Note Editor formatting toolbar

### Purpose

Provide compact, discoverable rich-text actions without changing the existing document-first editor or flat 56dp bottom-toolbar family.

### UX Principles

- Preserve the existing editor's dense, horizontally scrollable, flat white toolbar and its Material outlined-icon language.
- Make selection-sensitive operations explicit through selected text/active-style state, while keeping controls enabled in editable no-selection state as specified.

### Entry And Exit

- **Entry points**: Existing format-toggle control in the Note Editor; Formula opens its sheet and Link opens the picker.
- **Primary success exit**: Body reset and inline marks apply in place; Link returns from picker; formula sheet dismisses after valid insertion/update.
- **Cancel/back behavior**: Hide control collapses the toolbar; picker back and formula cancel make no document change.
- **Failure recovery**: Formula validation stays in its sheet; Body remains a no-op at a collapsed cursor while inline-format controls change only the pending typing state; read-only controls are disabled/inert.

### Information Architecture

1. **Editor content**: Editable rich-text blocks; selection is visually apparent. Valid internal links look tappable and formulas render as math rather than raw source.
2. **Formatting toolbar**: 56dp white surface, `surface` token, 4dp horizontal padding, 2dp item gap; Body, bold, italic, underline, strikethrough, Link, Code, Formula, then Hide.
3. **Body action**: A direct reset action; it has no menu and returns the selected text to plain Paragraph formatting.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|---|---|---|---|
| Formatting toolbar | Existing editor action rail | editable, read-only disabled, IME hidden/visible | `editor_formatting_bottom_bar` |
| Body action | Remove all formatting from selected text | enabled/non-empty selection, collapsed-cursor no-op, read-only disabled | `editor_body_action` |
| Bold action | Toggle bold on selected text or following typed text | active/inactive, collapsed-cursor typing state, read-only disabled | `editor_bold_action` |
| Italic action | Toggle italic on selected text or following typed text | active/inactive, collapsed-cursor typing state, read-only disabled | `editor_italic_action` |
| Underline action | Toggle underline on selected text or following typed text | active/inactive, collapsed-cursor typing state, read-only disabled | `editor_underline_action` |
| Strikethrough action | Toggle strikethrough on selected text or following typed text | active/inactive, collapsed-cursor typing state, read-only disabled | `editor_strikethrough_action` |
| Link action | Open/modify/remove internal note link | editable, read-only disabled | `editor_link_action` |
| Inline code action | Toggle code mark and code/monospace styling on selected or following text | active, inactive, collapsed-cursor typing state, read-only disabled | `editor_code_action` |
| Formula action | Open formula sheet | insert, edit, read-only disabled | `editor_formula_action` |
| Rendered formula | Expose editable/deletable formula as tappable math | default, focused/editable, whole-object deletion, read-only | `editor_inline_formula_<stable-inline-id>` |
| Internal note link | Accessible in-app navigation target | valid primary-color underlined/tappable label, deleted-label removal, unresolved/plain fallback, read-only | `editor_note_link_<stable-inline-id>` |
| Hide action | Close the formatting toolbar | enabled/read-only disabled | `editor_hide_formatting` |

`<storage-type>` comes from the fixed supported type catalog. `<stable-inline-id>` is a persisted document-owned ID, never selection text or a transient index.

### Reference Anchor Contract

| Reference relationship | Visual bounds node(s) | Required runtime measurement |
|---|---|---|
| Formatting toolbar remains a 56dp flat rail at the editor bottom when IME is hidden | `editor_formatting_bottom_bar`, editor content anchor | Toolbar height = 56dp ± 2dp; its top meets/abuts editor bottom divider. |
| IME-visible editor keeps formatting rail above keyboard | `editor_formatting_bottom_bar`, focused editor input | Toolbar remains visible above the IME, does not overlap the keyboard or focused input, and retains 56dp ± 2dp height. |

### Visual States

| State | User Sees | User Can Do |
|---|---|---|
| Loading | Existing editor loading behavior; toolbar does not expose unready mutations. | Wait. |
| Content with selection | Existing toolbar plus Body, Bold, Italic, Underline, Strikethrough, Link, Code, and Formula actions; active format uses `primary` plus non-color state treatment. | Reset selected formatting, apply inline marks, choose target, open formula sheet. |
| Content without selection | With a focused collapsed cursor, Body remains a no-op, inline-format buttons expose their pending typing state, Enter preserves the current line formatting on the new line, Link picker inserts title, and Formula sheet inserts at cursor. | Toggle an inline format, type, press Enter at the collapsed cursor, and continue typing; use Link/Formula. |
| IME-visible editor | Formatting toolbar remains visible in the editor between content and the keyboard. | Continue editing and use any formatting action without dismissing the toolbar. |
| Read-only | Formatting controls are visible at 38% content alpha with disabled semantics. | Inspect only; no mutation or navigation. |

### Interaction Rules

- Body makes no change unless the focused selection is non-empty and within one `TextBlock`.
- Body removes all block and inline formatting from the selected range and leaves unselected text formatting unchanged. Each basic inline action toggles only its own mark and preserves the other marks.
- At a collapsed cursor in a focused `TextBlock`, Bold, Italic, Underline, Strikethrough, and Code toggle only their own pending typing mark. The toolbar selected state reflects that pending mark, and every subsequently typed character inherits exactly the active marks until toggled off or the editing context changes.
- When Enter creates a new line at a focused collapsed cursor in a `TextBlock`, the new line inherits the current block style and effective inline marks at the caret, including active pending Bold, Italic, Underline, Strikethrough, and Code marks. Existing line content and formatting remain unchanged.
- Code toggles only the code mark on the selected range or pending typing state, renders marked text with code/monospace styling, and emits Markdown backticks.
- Link opens the picker. A selected label remains the label; no selection inserts the selected target title. An already linked selected label exposes **Remove link** in the picker.
- After insertion, a valid internal-link label uses primary-color underlined styling and is tappable. If its target note is deleted, remove the entire linked label/title; if the annotation is otherwise unavailable or malformed, retain ordinary non-clickable readable text.
- Formula opens the bottom sheet. Valid source replaces the selection or inserts at cursor/appended paragraph; tapping rendered math reopens source editing; deleting any part of rendered math removes the whole formula atom and leaves no raw source fragment.
- Formula control and sheet use descriptive, localized text, errors, roles, and state descriptions; all targets are at least 48×48dp.

### Copy Requirements

| Element | Copy |
|---|---|
| Body label | Body |
| Link action description | Link to note |
| Code action description | Inline code |
| Formula action description | Insert formula |
| Disabled description | Formatting is unavailable in a read-only note. |

### Accessibility

- All icons have localized content descriptions and all controls have stable tags.
- Active state combines violet `primary` with a visible indicator/selected semantics; color is never the only indicator.
- Read-only controls expose disabled semantics. Formula/link expose semantic action and descriptive label; formula source remains available in the edit sheet.
- Support font scaling using LazyRow scrolling without clipped controls.

### Responsive And Configuration Behavior

- Preserve 56dp toolbar rail and horizontal scrolling on phones/tablets; use safe drawing/navigation insets.
- Pending link selection is `SavedStateHandle`-backed; Body reset and formula draft/validation recover across configuration change.
- Editor keyboard uses IME insets so the formatting toolbar remains visible above the keyboard without covering the focused input.

### Design Assets

- **Generated mockup**: `design/mockup_editor_formatting.png` — selection and flat formatting toolbar reference.
- **Keyboard-visible mockup**: `design/mockup_editor_formatting_keyboard.png` — editor typing state with Bold selected, demonstrating that the formatting toolbar remains visible above the keyboard and subsequent typed text inherits the active mark.

### Out Of Scope For This Design

- New toolbar controls, external URL entry, multi-block selection, formula block cards, and visual redesign of the editor shell.

## Screen 2 — Link to note picker

### Purpose

Let an editor select an existing note as an internal destination without leaving the editor flow or allowing self-links.

### UX Principles

- Use a predictable full-screen destination with a top back action and clear search, matching existing searchable app surfaces.
- Show enough context to disambiguate notes: title plus parent-folder subtitle.

### Entry And Exit

- **Entry points**: Note Editor Link toolbar action.
- **Primary success exit**: Tap a single note result; destination ID returns to the caller and picker pops.
- **Cancel/back behavior**: Top/system back pops with no editor change.
- **Failure exit or recovery**: Empty/search failure stays in picker; user clears search or retries. No selection is written.

### Information Architecture

1. **Top app bar**: Back icon and title **Link to note**.
2. **Search field**: Existing `searchBackground` pattern, search icon, localized placeholder **Search notes**, and clear action.
3. **Result list**: Lazy list of selectable note rows, keyed by note ID. Each row has note icon, title, and parent folder name or **No folder** subtitle.
4. **Selected-link affordance**: When changing an existing selected link, a localized **Remove link** action precedes/results alongside candidates.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|---|---|---|---|
| Picker screen | Hosts picker state | loading, content, empty, error | `note_link_picker_screen` |
| Back action | Cancel selection | enabled | `note_link_picker_back` |
| Search input | Filter candidate titles | empty, focused, query, clear | `note_link_picker_search` |
| Result list | Render candidates excluding caller note | content/empty/error | `note_link_picker_results` |
| Note row | Select a stable note destination | default, pressed | `note_link_picker_note_<note-id>` |
| Remove link | Strip target from selected label | visible only for existing selected link | `note_link_picker_remove_link` |
| Empty state | Explain no candidates/results | all-notes empty, query empty | `note_link_picker_empty` |
| Error/retry | Recover local-loading failure | error/retrying | `note_link_picker_retry` |

`<note-id>` is the immutable domain-owned note ID and is permitted as documented dynamic test-tag data.

### Reference Anchor Contract

| Reference relationship | Visual bounds node(s) | Required runtime measurement |
|---|---|---|
| Search field and first result maintain 16dp content rhythm | `note_link_picker_search`, `note_link_picker_results` | First result top is 16dp ± 2dp below search-field bottom. |
| Every result has an accessible row target | `note_link_picker_note_<note-id>` | Each rendered candidate is at least 48dp high. |

### Visual States

| State | User Sees | User Can Do |
|---|---|---|
| Loading | Local loading indicator, result actions disabled. | Go back. |
| Content | Search and note rows with folder subtitles; current note absent. | Search, choose a note, remove existing link. |
| Empty | Localized no-notes/no-results message. | Clear search or go back. |
| Error | Human-readable localized recovery message and Retry. | Retry, clear search, or go back. |

### Interaction Rules

- Query filters title/folder context locally; candidate selection is single-tap and returns target ID only.
- Existing selection text remains the link label. If there is no editor selection, choosing a target inserts its title.
- Current note is always excluded; target rows are not multi-select.
- `Remove link` turns the selected label plain immediately and pops; cancellation does nothing.

### Copy Requirements

| Element | Copy |
|---|---|
| Title | Link to note |
| Search placeholder | Search notes |
| Root-folder subtitle | No folder |
| Remove action | Remove link |
| Empty state | No notes found |

### Accessibility

- Back/search/clear/remove controls have localized descriptions and 48dp minimum targets.
- Each row merges title and folder subtitle for screen-reader announcement and exposes a selectable role.
- Empty/error state and retry have stable tags and spoken labels; dynamic note titles are rendered content, not tags.

### Responsive And Configuration Behavior

- Full-screen route respects safe insets and shows the keyboard naturally without an editor bottom toolbar.
- Query and pending caller context survive configuration change; cancellation leaves the caller document untouched.

### Design Assets

- **Generated mockup**: `design/mockup_note_link_picker.png` — full-screen searchable candidate picker.
- **Keyboard-visible mockup**: Not applicable — this screen has no bottom toolbar; IME behavior follows normal full-screen search accessibility.

### Out Of Scope For This Design

- Folders as link destinations, external URLs, multi-select, target previews, and backlinks.

## Screen 3 — Inline formula sheet

### Purpose

Provide an edit-safe, preview-first way to write inline LaTex source and see its rendered math before it changes the note.

### UX Principles

- Keep the user in the editor through a compact Material 3 sheet rather than a separate destination.
- Source is always visible and editable; invalid source cannot overwrite saved content.

### Entry And Exit

- **Entry points**: Formula toolbar action or tap on rendered inline formula.
- **Primary success exit**: Insert/Update applies valid source and dismisses sheet.
- **Cancel/back behavior**: Cancel, scrim tap, swipe down, and system back dismiss with no document change.
- **Failure exit or recovery**: Invalid source leaves sheet open with input, inline localized error, and safe preview fallback.

### Information Architecture

1. **Sheet header**: M3 drag handle and title **Insert formula** or **Edit formula**.
2. **Source input**: Labeled multiline `LaTex formula` field, focused automatically for insert/edit.
3. **Preview**: Rendered math preview for valid source; error text and no destructive write for invalid source.
4. **Actions**: Cancel and one text-only primary Insert/Update action, both 48dp targets; the formula sheet has no additional plus-icon insert action.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|---|---|---|---|
| Formula sheet | Host draft and actions | insert, edit, IME visible, invalid | `editor_formula_sheet` |
| Source input | Accept LaTex source | empty, focused, valid, invalid | `editor_formula_source_input` |
| Preview | Show render result | valid, unavailable/invalid fallback | `editor_formula_preview` |
| Validation message | Explain invalid source | hidden/visible | `editor_formula_validation_error` |
| Cancel | Close without mutation | enabled | `editor_formula_cancel` |
| Insert/update | Commit valid formula | enabled/disabled | `editor_formula_submit` |

### Reference Anchor Contract

| Reference relationship | Visual bounds node(s) | Required runtime measurement |
|---|---|---|
| Sheet remains open above the IME | `editor_formula_sheet`, `editor_formula_source_input` | Sheet exists while IME visible; source input and submit are above IME/in visible bounds. |
| Preview follows source field with consistent spacing | `editor_formula_source_input`, `editor_formula_preview` | Preview top is 16dp ± 2dp below source-input bottom or validation message. |
| Long preview remains usable | `editor_formula_preview` | Preview is bounded and horizontally scrollable; formula content does not wrap, clip, or force the sheet below the IME. |

### Visual States

| State | User Sees | User Can Do |
|---|---|---|
| Default insert | Empty/focused source field and inactive preview. | Type valid LaTex, cancel. |
| Valid preview | Rendered math and enabled Insert/Update. | Commit or keep editing. |
| Invalid source | Localized error plus safe preview fallback; sheet remains open. | Correct source, cancel. |
| IME visible | Expanded open sheet above keyboard with Cancel and Insert reachable. | Type, inspect preview, submit/cancel. |
| Long formula | Bounded preview with horizontal scrolling inside the expanded sheet. | Scroll the formula preview horizontally without dismissing the sheet or keyboard. |

### Interaction Rules

- Insert replaces editor selection or inserts at cursor/appends a focused paragraph when no text block is focused.
- Edit initializes the draft from the tapped formula source; only valid submit updates the document.
- Tapping the input never dismisses the sheet. Only explicit dismiss gestures/actions do.
- A long rendered formula scrolls horizontally within the preview container; it does not wrap or clip. The sheet expands only to the available height above the IME, and its actions remain reachable.
- Formula insertion uses the same single text-only Insert/Update affordance in every formula-sheet state; no variant may add a second plus-icon action.

### Copy Requirements

| Element | Copy |
|---|---|
| Insert title | Insert formula |
| Edit title | Edit formula |
| Input label | LaTex formula |
| Preview label | Preview |
| Validation | Enter a valid LaTex formula. |
| Submit | Insert / Update |

### Accessibility

- Labeled source input, live validation announcement, and semantic rendered-formula description include no unrendered technical source unless focus enters editing.
- Controls meet 48dp targets and the preview has a screen-reader text alternative.
- Error is represented by text/icon/semantics in addition to `error` color.

### Responsive And Configuration Behavior

- Sheet uses `imePadding()`, stays open and expands above keyboard, and keeps input/preview/actions reachable through scrolling.
- Draft source, mode, and invalid state survive configuration change but are not persisted into the note until a valid submit.

### Design Assets

- **Generated mockup**: `design/mockup_formula_sheet.png` — normal formula sheet with source and preview.
- **Keyboard-visible mockup**: `design/mockup_formula_sheet_keyboard.png` — open expanded sheet above keyboard with no editor toolbar behind it.

### Out Of Scope For This Design

- Multi-line display equations, formula toolbar, external rendering services, handwriting, and formula history.
