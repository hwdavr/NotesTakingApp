# Feature Spec — Formatting Toolbar Completion

**Date**: 2026-09-02
**Status**: Approved for slice planning; awaiting implementation approval
**Related design**: `design.md`

## Objective

Complete the existing Note Editor formatting toolbar controls: **Body**, **Bold**, **Italic**, **Underline**, **Strikethrough**, **Link**, **Code**, and **Formula**. The feature lets an editor reset selected text to plain body formatting, apply inline styles, add internal note links, and write rendered inline LaTex formulas without losing document, export, accessibility, or read-only guarantees.

## User Goal

As a person editing a note, I want to format selected text, link it to another note, and insert readable formulas so that my notes remain structured, connected, and useful after save, reload, and export.

## Scope

### In Scope

- Complete the existing Body, Bold, Italic, Underline, Strikethrough, Link, Code, and Formula formatting-toolbar controls.
- Reset a non-empty selection in the focused text block to an unformatted Paragraph, removing all block and inline formatting from the selected text while preserving unselected text and its formatting in order.
- Apply or remove Bold, Italic, Underline, and Strikethrough inline marks on a non-empty selection in the focused text block without changing other marks or unselected text; at a collapsed cursor, each control toggles its mark for following typed text.
- Preserve the current line formatting when Enter creates a new line at a focused collapsed cursor, including the effective block style and active inline marks at the caret.
- Apply/remove inline code to a non-empty selection in the focused text block; selected code text renders with code/monospace styling and exports as Markdown backticks. At a collapsed cursor, Code toggles code/monospace formatting for following typed text.
- Pick an existing note (other than the note being edited) as an internal link destination, showing its parent folder as the picker-row subtitle.
- Insert a selected target note's title when no editor text is selected; retain selected editor text as the link label when it is selected.
- Remove or replace an existing internal note link from the Link picker.
- Insert, render, validate, edit, delete, persist, and export inline LaTex formulas; deleting a rendered formula removes the complete inline formula object.
- Preserve existing autosave/reload behavior and emit the approved Markdown/PDF forms.

### Out Of Scope

- New formatting-toolbar buttons, external web URLs, backlinks, link previews, deep links from outside the app, rich formula blocks, multi-block selections, collaborative editing, and new server endpoints.
- Changing the established Basic Blocks insertion behavior or the existing code-block feature.

## Rule Applicability

| Rule ID | Rule document | Default | Decision for this change | Trigger / rationale | Planned evidence |
|---|---|---|---|---|---|
| ARCH | `android-architecture.md` | Always | Required | UI controls dispatch presentation events; formatter/document changes remain in the editor ViewModel/mapper boundary; picker reads through domain repositories. | Architecture review; ViewModel/mapper unit tests. |
| IMPL | `implementation-rules.md` | Always | Required | Every toolbar callback, deletion fallback, validation branch, and renderer failure path must implement the specified behavior. | Acceptance tests and code review without stubs/no-op handlers. |
| TEST | `testing-strategy.md` | Always | Required | Rich-text serialization, block splitting, link resolution, formula validation, ViewModel transitions, exports, and Compose/navigation flows need deterministic coverage. | JVM unit/integration tests plus targeted instrumented UI and visual-flow tests. |
| SUI | `compose-rules.md` | Conditional | Required | Existing Compose editor changes, new destination/sheet, accessibility, selection states, and IME behavior are in scope. Explicit user-approved exception on 2026-09-03: the editor formatting toolbar remains visible above the IME and must not overlap the keyboard or focused field. | Stateless-content UI tests, stable tags, keyboard visual evidence, Compose-rule checker, and review of the approved exception. |
| L10N | `localization-rules.md` | Conditional | Required | Picker, menu, sheet, validation, empty state, and accessibility copy are user-visible. | String-resource review and localization-rule checker. |
| NAV | `navigation-rules.md` | Conditional | Required | Link selection introduces a full-screen picker and valid inline links open the existing Editor destination. | Typed route/result contract and instrumented back-stack tests. |
| API | `api-contract-rules.md` | Conditional | Not applicable — no endpoint, DTO, schema, or OpenAPI change; document JSON is local note content. | Review confirms `sharedContracts/openapi.yaml` is unchanged. |
| OBS | `observability.md` | Conditional | Required | This changes document persistence and recoverable formula-validation/link-resolution boundaries. No note content, formula source, or note title may be logged. | Review verifies reuse of existing persistence diagnostics and safe error handling. |
| ANL | `analytics-rules.md` | Conditional | Not applicable — analytics: none; no product-approved event or funnel is requested. | Review confirms no analytics dependency/event is added. |

## Technical Spec

### Libraries & Dependencies

| Library / SDK | Version | Purpose |
|---|---|---|
| Existing Kotlin/Compose/Room/Navigation stack | Project-managed | Editor state, local note data, destination navigation, and UI. |
| Offline LaTex renderer | To be selected in US-1 after API-24, license, rendering fidelity, and dark-theme validation | Render a deterministic inline formula preview and editor display without network access. |

### Key Technical Decisions

- **Document compatibility**: Extend the local `RichText` JSON representation only with optional fields for internal-link destination and formula source/stable identity. Existing documents with no new fields deserialize unchanged; unknown/missing annotations retain readable plain text.
- **Body reset**: Selection is constrained to the focused `TextBlock`. The formatter splits its children at selection offsets as needed, replaces the selected range with a plain Paragraph, and removes every inline mark from that range; unselected prefix/suffix text retains its content and formatting.
- **Inline marks**: Bold, Italic, Underline, Strikethrough, and Code toggle only their own mark over a non-empty focused selection. At a collapsed cursor in a focused `TextBlock`, tapping one of these controls toggles its pending typing mark; every subsequently typed character inherits the active marks exactly until the user toggles them off or changes the editing context. Existing other marks, text order, and unselected content remain unchanged; Body, Link, and Formula remain direct actions rather than persistent typing modes.
- **New-line formatting**: When Enter creates a new line at a focused collapsed cursor in a `TextBlock`, the new line inherits the current block style and the effective inline marks at the caret, including pending Bold, Italic, Underline, Strikethrough, and Code marks. Existing line content and formatting remain unchanged.
- **Internal note links**: Persist the stable target note ID separately from the retained label. Resolve it locally at render/open time; deleting a linked target note removes the entire linked label/title from the document, while malformed or otherwise unresolvable annotations retain readable plain text.
- **Picker navigation**: Define a typed `NoteLinkPicker` route with only primitive source note ID arguments. Keep the pending selection context in the editor `SavedStateHandle`; return only the selected target ID through the back-stack saved-state result before popping.
- **Formula representation**: Store the raw LaTex source in an optional rich-text annotation and render it inline as one atomic formula object. Deleting any part of a rendered formula removes the complete formula annotation and visible formula; it never leaves a raw LaTex fragment. Invalid source stays solely in the bottom-sheet draft with a localized error; it never changes the document.
- **Export**: Markdown emits `[label](notesapp://note/<stable-id>)` for resolvable internal links and `$<LaTex source>$` for formulas. PDF keeps links clickable when supported by the exporter and renders formulas as math; a deleted target contributes no label because the linked text is removed from the document.
- **Privacy/diagnostics**: Reuse existing persistence error handling. Never add logs or analytics containing note content, titles, link labels, IDs, or formula source.

### External APIs / Services

- None. The picker queries existing local note/folder data and formula rendering is offline.

### Platform & Compatibility Constraints

- **Min SDK**: 24 (project default).
- **Permissions required**: None.
- **Other constraints**: The renderer selected in US-1 must work offline, on API 24+, and in light/dark themes. This is not platform-bound because it does not depend on a device service, permission, or hardware capability.

## Functional Requirements

- **FR-001**: The system MUST reset only a non-empty selection within the focused text block to a plain Paragraph, removing all block and inline formatting from the selected text while preserving surrounding text and its existing formatting.
- **FR-002**: The system MUST apply/remove the existing inline `code` mark only to a non-empty focused selection within one `TextBlock`; selected code text MUST render with code/monospace styling and emit Markdown backticks. At a collapsed cursor in a focused `TextBlock`, Code MUST toggle the pending code mark so subsequently typed text uses code/monospace styling and emits backticks. With no focused text block or a cross-block selection, Code MUST make no document or typing-state change.
- **FR-008**: The system MUST apply/remove the inline `bold` mark only to a non-empty focused selection within one `TextBlock`, preserving all other marks and unselected text. At a collapsed cursor in a focused `TextBlock`, Bold MUST toggle the pending bold mark so subsequently typed text is bold; with no focused text block or a cross-block selection, Bold MUST make no document or typing-state change.
- **FR-009**: The system MUST apply/remove the inline `italic` mark only to a non-empty focused selection within one `TextBlock`, preserving all other marks and unselected text. At a collapsed cursor in a focused `TextBlock`, Italic MUST toggle the pending italic mark so subsequently typed text is italic; with no focused text block or a cross-block selection, Italic MUST make no document or typing-state change.
- **FR-010**: The system MUST apply/remove the inline `underline` mark only to a non-empty focused selection within one `TextBlock`, preserving all other marks and unselected text. At a collapsed cursor in a focused `TextBlock`, Underline MUST toggle the pending underline mark so subsequently typed text is underlined; with no focused text block or a cross-block selection, Underline MUST make no document or typing-state change.
- **FR-011**: The system MUST apply/remove the inline `strikethrough` mark only to a non-empty focused selection within one `TextBlock`, preserving all other marks and unselected text. At a collapsed cursor in a focused `TextBlock`, Strikethrough MUST toggle the pending strikethrough mark so subsequently typed text is struck through; with no focused text block or a cross-block selection, Strikethrough MUST make no document or typing-state change.
- **FR-012**: The system MUST expose the pending inline typing marks at the collapsed cursor through the toolbar's selected states, and newly typed text MUST inherit exactly those active marks without changing adjacent existing text; Body, Link, and Formula MUST remain non-persistent direct actions.
- **FR-013**: When deletion targets any part of an inline formula, the system MUST remove the complete formula object, its source annotation, and its rendered output from the document without leaving raw LaTex text.
- **FR-014**: When Enter creates a new line at a focused collapsed cursor in a `TextBlock`, the system MUST preserve the current line's formatting context on the new line: the current block style and the effective inline marks at the caret, including active pending Bold, Italic, Underline, Strikethrough, and Code marks. Existing line content and formatting MUST remain unchanged.
- **FR-003**: The system MUST open a full-screen, searchable internal-note picker from Link, exclude the current note, show each candidate note's parent folder subtitle, apply/replace/remove a target link, and preserve the editor when the picker is cancelled. A valid inserted link MUST display its retained label as a visually distinct tappable inline link using the app's primary color and underline.
- **FR-004**: The system MUST insert an inline formula by replacing a non-empty selection or inserting at the cursor (or appending/focusing a paragraph when no text block is focused), render valid LaTex, reopen the formula sheet when a rendered formula is tapped, and treat the rendered formula as one atomic editable/deletable object. Long previews MUST remain usable in a bounded horizontally scrollable area.
- **FR-005**: The system MUST keep invalid formula drafts editable in the open sheet with localized validation feedback and MUST NOT mutate the document until a valid formula is inserted/updated.
- **FR-006**: The system MUST preserve Body, inline-mark, link, code, and formula data through autosave, reload, Markdown export, and PDF export; deleting a linked target MUST remove its entire linked label/title, while unknown or malformed annotations use graceful readable fallbacks.
- **FR-007**: The system MUST expose all formatting controls as visible disabled/inert controls for read-only notes; editable controls remain enabled without a selection according to FR-002/FR-003/FR-004/FR-008–FR-012.

## Acceptance Criteria

- **AC-001**: Given a non-empty selection in the focused text block, when the user taps Body, then only the selected text becomes an unformatted Paragraph with all inline marks removed, while unselected text and its formatting remain in order.
- **AC-002**: Given no selection or a selection spanning more than one block, when the user chooses Body, then the document remains unchanged; multi-block selection is out of scope.
- **AC-003**: Given a non-empty selected range, when Code is tapped, then only that range gains/removes the inline code mark and renders with code/monospace styling; given a collapsed cursor in a focused text block, Code toggles the mark for subsequently typed text; Markdown exports marked text with backticks.
- **AC-012**: Given a non-empty selected range, when Bold is tapped, then only that range gains/removes bold while other marks and unselected text remain unchanged; given a collapsed cursor in a focused text block, Bold toggles bold for subsequently typed text.
- **AC-013**: Given a non-empty selected range, when Italic is tapped, then only that range gains/removes italic while other marks and unselected text remain unchanged; given a collapsed cursor in a focused text block, Italic toggles italic for subsequently typed text.
- **AC-014**: Given a non-empty selected range, when Underline is tapped, then only that range gains/removes underline while other marks and unselected text remain unchanged; given a collapsed cursor in a focused text block, Underline toggles underline for subsequently typed text.
- **AC-015**: Given a non-empty selected range, when Strikethrough is tapped, then only that range gains/removes strikethrough while other marks and unselected text remain unchanged; given a collapsed cursor in a focused text block, Strikethrough toggles strikethrough for subsequently typed text.
- **AC-018**: Given a collapsed cursor in a focused text block, when any inline-format button is selected, then the button exposes its active typing state and all subsequently typed text inherits exactly the active Bold, Italic, Underline, Strikethrough, and/or Code marks until toggled off; Body, Link, and Formula do not become persistent typing modes.
- **AC-019**: Given a rendered inline formula, when the user deletes any part of it, then the complete formula object and annotation are removed and no raw LaTex fragment remains.
- **AC-020**: Given a focused editable `TextBlock` with a collapsed cursor, a current block style, and effective inline marks at the caret, when the user presses Enter, then the new line inherits that formatting context and text typed immediately after the break inherits the active marks; the existing line content and formatting remain unchanged.
- **AC-004**: Given the user taps Link, when the picker opens, then it is a full-screen searchable destination, excludes the current note, and each result presents its title plus parent-folder subtitle (or localized No folder subtitle).
- **AC-005**: Given selected editor text, when a target note is chosen, then the selection remains the clickable label linked by target ID; given no selection, then the target title is inserted as the clickable label at cursor/appended paragraph.
- **AC-016**: Given a valid inserted internal link, then its retained label is visibly distinct with the app's primary color and underline, remains tappable, and opens the target note; deleting the target removes the entire linked label/title, while an otherwise unresolvable annotation renders as ordinary non-clickable text.
- **AC-006**: Given a selected existing link, when Link opens, then Remove link makes the label plain text and choosing another note replaces the destination; cancelling/back makes no editor change.
- **AC-007**: Given a valid selected formula source, when Formula is inserted, then it replaces the selected text and renders inline; with no selection it inserts at cursor or appends/focuses a paragraph if needed.
- **AC-017**: Given a formula whose rendered preview exceeds the available width or height, then the dialog expands only within the available space above the IME, the preview remains horizontally scrollable without wrapping or clipping, and Cancel/Insert remain reachable.
- **AC-008**: Given a rendered formula, when tapped, then the sheet opens with its source; given invalid draft source, then the sheet stays open with localized error/preview fallback and no document mutation.
- **AC-009**: Given an editable note and an IME-visible editor state, when text input is focused, then the editor formatting bar remains visible above the keyboard without overlap; in the formula-sheet state, the sheet remains open, expanded, and usable above the IME.
- **AC-010**: Given a read-only note, when formatting controls are shown, then all are visible, disabled in semantics, and cannot mutate or navigate.
- **AC-011**: Given an edited note is saved/reloaded/exported, then Body, bold, italic, underline, strikethrough, code, collapsed-cursor typing marks, resolvable note links, deleted-target label removal, and the full formula lifecycle retain the specified behavior; Markdown/PDF use the defined forms and unknown annotations remain readable text.

## Data And Persistence

- Existing Room note content storage remains the persistence boundary; no migration or API contract change is expected.
- Optional serialized fields retain link target ID and formula source/stable inline identity. Older JSON without fields remains valid.
- Formatting mutations use the existing editor autosave path. Picker pending state survives configuration change through `SavedStateHandle`; formula sheet draft survives configuration change with `rememberSaveable`/presentation state and is not persisted until insert/update.

## Edge Cases

- Collapsed cursor with a focused text block: Body remains a no-op; Bold, Italic, Underline, Strikethrough, and Code toggle pending typing marks for following text; Link opens the picker and inserts target title; Formula opens the sheet and inserts at cursor/appends as defined.
- No focused text block or cross-block selection: Body and inline-format actions make no document or typing-state change.
- Deleted link target: remove the entire linked label/title from the document and exports. Otherwise unresolved link annotation: retain readable label, remove interactive link behavior, and export plain label.
- Root-level target note: show localized **No folder** subtitle.
- Empty/no-search picker results: show localized empty message; no selection is possible.
- Current note: never appears as a candidate.
- Invalid/unsupported LaTex: keep draft source in open sheet, show localized error, and preserve the pre-existing document.
- Delete inline formula: remove the complete formula object and annotation, never only a visible/source fragment.
- New line at a focused collapsed cursor: inherit the current block style and effective inline marks at the caret while leaving the existing line unchanged.
- Unknown/legacy rich-text annotations: do not crash or erase visible text; render/export readable fallback text.
- Read-only/editor access change: controls are disabled and all mutations/navigation are rejected.

## Explicit Assumptions

| # | Assumption | Risk if Wrong |
|---|---|---|
| A1 | None. All product decisions required for this feature were confirmed by the user on 2026-09-02. | N/A |

## Open Questions

All questions are answered.

| # | Question | Status | Answer |
|---|---|---|---|
| Q1 | Which toolbar controls are in scope? | ✅ Answered | Body, Bold, Italic, Underline, Strikethrough, Link, Code, and Formula. |
| Q2 | What does Body reset and what is transformed? | ✅ Answered | Body has no style menu; it resets only the selected text to a plain Paragraph and removes all block and inline formatting from that selection. |
| Q3 | How do note links behave? | ✅ Answered | Full-screen searchable picker, current note excluded, parent-folder subtitle, stable target ID, retained selected label rendered in primary color with underline, deletion of a target removes its entire linked label/title, and remove/replace support. |
| Q4 | What are the inline-mark and Formula semantics? | ✅ Answered | Bold, Italic, Underline, Strikethrough, and Code toggle selected ranges or become pending typing marks at a collapsed cursor; Formula is editable rendered inline LaTex, `$...$` in Markdown. |
| Q5 | What happens without selection and in read-only mode? | ✅ Answered | At a focused collapsed cursor, inline-format buttons toggle the marks for following typed text; Body remains a no-op, Link opens picker and inserts title, Formula opens sheet/inserts at cursor, and read-only controls are visible and disabled. |
| Q6 | Must it persist and export? | ✅ Answered | Yes; Markdown links use `notesapp://note/<id>` and PDF keeps clickable links when possible. |
| Q7 | May formula renderer selection be an implementation slice? | ✅ Answered | Yes; it must validate offline API-24 compatibility, licensing, and dark theme. |
| Q8 | Should the editor formatting toolbar remain visible when the keyboard is shown? | ✅ Answered | Yes; it remains visible above the keyboard and must not overlap the keyboard or focused field. |
| Q9 | How are long formula previews and inserted links displayed? | ✅ Answered | Long formula previews use bounded horizontal scrolling while the sheet expands only within the space above the IME; inserted links retain their label and use tappable primary-color underlined styling, while deleting the target removes the entire linked label/title. |
| Q10 | What happens when a formula is deleted? | ✅ Answered | The inline formula is atomic: deleting any part removes the complete rendered formula and its annotation, with no raw LaTex fragment left behind. |
| Q11 | What happens when Enter creates a new line in formatted text? | ✅ Answered | At a focused collapsed cursor in a `TextBlock`, the new line inherits the current block style and effective inline marks at the caret, including active pending marks; the existing line remains unchanged. |

## Screen States

| State | Requirement | Acceptance Criteria |
|---|---|---|
| Editor content | Flat formatting toolbar, Body reset/basic inline-mark/formula/link triggers, selected and pending inline marks, formatted new-line inheritance, rendered inline formulas, disabled read-only presentation, and keyboard-visible toolbar state. | AC-001–003, AC-005–010, AC-012–015, AC-018–020 |
| Link-picker loading | Local progress state with results disabled until candidates resolve. | AC-004 |
| Link-picker content/empty/error | Searchable results with folder subtitles; localized no-result or recoverable error treatment. | AC-004, AC-006 |
| Formula sheet default/invalid/long | Open sheet with source, rendered preview, insert/update action, inline validation error, and horizontally scrollable long-preview state. | AC-007–009, AC-017 |

## Navigation

- **Entry**: Existing Note Editor formatting toolbar's Link control opens `NoteLinkPicker` with primitive caller note ID; valid inline link tap opens the existing Editor route for the target note.
- **Back/cancel**: System back or top back pops the picker; sheet dismiss/cancel preserves document. Neither changes an editor selection nor writes content.
- **Success**: Picker returns only target ID to the originating Editor's saved state and pops; Editor applies the pending selection. Formula insert/update dismisses its sheet and keeps the user in Editor.
- **Error recovery**: Empty/search error keeps picker open with retry/clear-search path. Invalid formula stays in the open sheet and does not write.

## Traceability

| Requirement | Design Section | Acceptance Criteria |
|---|---|---|
| FR-001, FR-002, FR-007, FR-008–FR-012 | Screen 1 — Note Editor formatting toolbar | AC-001–003, AC-009–010, AC-012–015, AC-018 |
| FR-014 | Screen 1 — Note Editor formatting toolbar | AC-020 |
| FR-003 | Screen 1/2 — Editor link rendering and note-link picker | AC-004–006, AC-016 |
| FR-004, FR-005, FR-013 | Screen 3 — Inline formula sheet and editor formula atom | AC-007–009, AC-017, AC-019 |
| FR-006 | All screens / persistence and export contracts | AC-011 |

## Verification Expectations

- **Unit**: Rich-text optional-field JSON compatibility; selection split/merge; Body, bold, italic, underline, strikethrough, code, collapsed-cursor typing marks, new-line formatting inheritance, link, and formula reducers; whole-formula deletion; deleted-target label removal; unknown-annotation fallback; Markdown emission; formula validation adapter; ViewModel state transitions.
- **Integration**: Existing local note/folder repositories feed picker candidates, resolve folder subtitle/deleted target, autosave/reload annotations, and produce PDF/Markdown exports without endpoint use.
- **Instrumented UI**: Stateless editor toolbar actions, selected-range/collapsed-cursor/new-line formatting behavior, disabled semantics, Body reset, picker search/back/result, internal-link navigation, formula edit/invalid/delete state, keyboard contract, and configuration restoration.
- **Manual/visual**: Capture production visual-flow evidence for toolbar selection, picker rows/subtitles, formula default/invalid/keyboard states, Light/Dark themes, and measured toolbar/IME anchors, including the formatting toolbar remaining visible above the keyboard.

## No Open Questions Gate

- [x] All requirements are specific and testable.
- [x] All non-goals are explicit.
- [x] No unresolved assumptions remain.
- [x] All visual states are defined in `design.md`.
- [x] All navigation outcomes are defined.
