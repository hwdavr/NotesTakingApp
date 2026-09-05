# Sprint Contract — Formatting Toolbar Completion

## 🏃 Sprint Overview

* **Sprint:** `P09-03`
* **Feature:** Formatting Toolbar Completion
* **Duration:** 4 sequential implementation sessions
* **Plan revision:** Re-sliced on 2026-09-03 after the collapsed-cursor, formula deletion, formula preview, formula action, link cleanup, and IME-toolbar clarifications; amended on 2026-09-04 to preserve current line formatting across Enter/new-line creation.

## 🎯 Scope

### In Scope

* [ ] Insert, edit, persist, export, and atomically delete rendered inline LaTex formulas.
* [ ] Reset selected text with Body and apply or remove Bold, Italic, Underline, Strikethrough, and Code.
* [ ] Carry selected inline marks into exactly the following typed text at a focused collapsed cursor.
* [ ] Preserve the current block style and active inline marks when Enter creates a new line at a focused collapsed cursor in a text block while leaving the existing line unchanged.
* [ ] Keep the editor formatting toolbar above the IME and make long formula previews horizontally scrollable within the available sheet height.
* [ ] Use one consistent text-only Insert or Update action in every formula-sheet state.
* [ ] Search, create, replace, remove, resolve, and export internal note links with retained labels and stable target IDs.
* [ ] Remove the complete linked label when its target note is deleted and retain readable plain fallback for unresolved annotations.
* [ ] Preserve read-only, accessibility, localization, navigation, privacy, autosave, reload, Markdown, and PDF contracts.

### Out of Scope

* New toolbar buttons, external URLs, backlinks, previews, outside-app deep links, and folders as link destinations.
* Multi-block selections, rich formula blocks, formula history, handwriting, collaboration, or new server endpoints.
* Changes to the established Basic Blocks insertion behavior or the existing standalone code-block feature.
* A renderer-only abstraction or a visual redesign of the existing editor shell.

## Platform Capability & Environment Contract

`feature_list.json` declares `platform_validation.required: false` with `unsupported_environment_policy: "fail_loudly"`. This feature uses the existing Android application, local Room data, Navigation Compose, and an offline renderer; it does not cross an Android service, permission, hardware, locale, device-model, or platform-model boundary. No `platform-capability-matrix.md` is generated. The normal connected UI tests still remain required for user-visible Compose and navigation behavior.

## Rule Applicability Contract

| Rule ID | Rule document | Decision | Slice evidence |
|---|---|---|---|
| ARCH | `android-architecture.md` | Required | US-1 through US-4 keep UI stateless, put state and mutations in ViewModels and domain/data boundaries, and are reviewed against the architecture rule. |
| IMPL | `implementation-rules.md` | Required | Every toolbar callback, formula validation/deletion branch, link-resolution fallback, and export path is exercised by the slice tests; no stubs or suppression comments. |
| TEST | `testing-strategy.md` | Required | Each story has JVM and/or production-entry instrumented tests; US-4 owns the final in-test visual captures. |
| SUI | `compose-rules.md` | Required | Stable test tags, stateless Compose content, accessibility, IME insets, and the approved 2026-09-03 editor-toolbar-above-IME exception are verified in US-2 through US-4. |
| L10N | `localization-rules.md` | Required | Picker, formula validation, empty/error states, actions, content descriptions, and No folder copy use string resources and are checked in US-1 and US-4. |
| NAV | `navigation-rules.md` | Required | US-4 verifies the typed picker route, primitive caller ID, saved-state target result, cancellation, and target-note navigation. |
| API | `api-contract-rules.md` | Not applicable — no endpoint, DTO, schema, or OpenAPI change; document JSON remains local note content. | `sharedContracts/openapi.yaml` remains unchanged; every acceptance row declares `N/A — no API`. |
| OBS | `observability.md` | Required | Persistence and renderer failures use safe diagnostics; US-4 verifies that note content, titles, IDs, labels, and formula source are not logged. |
| ANL | `analytics-rules.md` | Not applicable — analytics: none; no product-approved event or funnel is requested. | No analytics event or dependency is added. |

## Spec Coverage Matrix

| Source requirement | Requirement summary | Primary user story | Primary acceptance test | Handling |
|---|---|---|---|---|
| FR-001 | Body resets only a non-empty focused selection to plain Paragraph formatting and preserves the formatted prefix and suffix. | US-2 | TC-US-2-01 | In scope |
| FR-002 | Code toggles a selected range or pending collapsed-cursor mark, renders monospace, exports backticks, and no-ops without a valid focused block. | US-2 | TC-US-2-03 | In scope; collapsed and invalid-context outcomes are separately decomposed below. |
| FR-008 | Bold toggles only the selected range or pending typing mark and preserves other marks. | US-2 | TC-US-2-04 | In scope |
| FR-009 | Italic toggles only the selected range or pending typing mark and preserves other marks. | US-2 | TC-US-2-05 | In scope |
| FR-010 | Underline toggles only the selected range or pending typing mark and preserves other marks. | US-2 | TC-US-2-06 | In scope |
| FR-011 | Strikethrough toggles only the selected range or pending typing mark and preserves other marks. | US-2 | TC-US-2-07 | In scope |
| FR-012 | The toolbar exposes pending marks and newly typed text inherits exactly them; Body, Link, and Formula remain direct actions. | US-2 | TC-US-2-08 | In scope |
| FR-013 | Deleting any part of a formula removes the complete formula object, source annotation, and rendered output. | US-1 | TC-US-1-06 | In scope |
| FR-014 | Enter-created lines at a focused collapsed cursor inherit the current block style and effective inline marks at the caret without changing the existing line. | US-2 | TC-US-2-10 | In scope |
| FR-003 | Link opens a searchable internal-note picker, supports target lifecycle, cancellation, and valid-link styling. | US-4 | TC-US-4-01 | In scope; picker, label, lifecycle, and fallback outcomes are decomposed below. |
| FR-004 | Formula supports selection/cursor/append insertion, rendering, editing, atomic deletion, and a usable long preview. | US-1 | TC-US-1-01 | In scope; responsive preview is an independently observable follow-up in US-3. |
| FR-005 | Invalid formula drafts remain editable in the open sheet and do not mutate the document. | US-1 | TC-US-1-04 | In scope |
| FR-006 | Supported formatting annotations persist and export; deleted targets remove labels and unknown annotations fall back readably. | US-4 | TC-US-4-08 | In scope; deleted-target and fallback outcomes have dedicated tests below. |
| FR-007 | Formatting controls are visible, disabled, and inert in read-only notes; editable no-selection rules remain enabled as specified. | US-4 | TC-US-4-09 | In scope |
| AC-001 | Body removes all formatting from only the selected range. | US-2 | TC-US-2-01 | In scope |
| AC-002 | Body is unchanged for no selection or a cross-block selection. | US-2 | TC-US-2-02 | In scope |
| AC-003 | Code toggles selected text, supports pending cursor typing, and exports backticks. | US-2 | TC-US-2-03 | In scope; pending typing is decomposed as AC-018 and TC-US-2-08. |
| AC-012 | Bold changes only the selected range. | US-2 | TC-US-2-04 | In scope |
| AC-013 | Italic changes only the selected range. | US-2 | TC-US-2-05 | In scope |
| AC-014 | Underline changes only the selected range. | US-2 | TC-US-2-06 | In scope |
| AC-015 | Strikethrough changes only the selected range. | US-2 | TC-US-2-07 | In scope |
| AC-018 | Collapsed-cursor inline buttons expose active state and exact following-text inheritance. | US-2 | TC-US-2-08 | In scope |
| AC-019 | Formula deletion removes the whole object and annotation. | US-1 | TC-US-1-06 | In scope |
| AC-020 | Enter-created lines preserve the current formatting context and apply its marks to subsequent text. | US-2 | TC-US-2-10 | In scope |
| AC-004 | Picker is full-screen/searchable, excludes the current note, and shows folder context. | US-4 | TC-US-4-01 | In scope |
| AC-005 | Selected labels are retained and no-selection insertion uses the target title. | US-4 | TC-US-4-02 | In scope; no-selection insertion is separately decomposed as AC-005 cursor outcome and TC-US-4-03. |
| AC-016 | Valid links are primary-color underlined tappable labels and target resolution follows the specified deletion and fallback rules. | US-4 | TC-US-4-04 | In scope; deletion and unresolved outcomes are separately decomposed below. |
| AC-006 | Remove, replace, and cancel produce the specified link outcomes. | US-4 | TC-US-4-05 | In scope |
| AC-007 | Valid formula selection replacement renders inline and no-selection insertion uses cursor or paragraph fallback. | US-1 | TC-US-1-01 | In scope; cursor and no-focused-block outcomes are separately decomposed below. |
| AC-017 | Long previews remain horizontally scrollable and actions remain reachable above the IME. | US-3 | TC-US-3-01 | In scope |
| AC-008 | Tapping a formula edits its source and invalid source stays open without document mutation. | US-1 | TC-US-1-04 | In scope; edit and invalid outcomes are separately decomposed below. |
| AC-009 | The editor toolbar remains above the IME and the formula sheet remains usable above it. | US-2 | TC-US-2-09 | In scope; the formula-sheet outcome is separately owned by US-3. |
| AC-010 | Read-only formatting controls are visible, disabled, and inert. | US-4 | TC-US-4-09 | In scope |
| AC-011 | Save, reload, Markdown, and PDF preserve the complete formatting and fallback contract. | US-4 | TC-US-4-08 | In scope |
| FR-004 outcome: cursor insertion | A valid formula inserts at a focused collapsed cursor without replacing adjacent text. | US-1 | TC-US-1-02 | In scope planning decomposition |
| FR-004 outcome: no focused block | A valid formula creates and focuses an append paragraph when no text block is focused. | US-1 | TC-US-1-03 | In scope planning decomposition |
| FR-004 outcome: edit | Tapping a rendered formula restores its source and valid update persists. | US-1 | TC-US-1-05 | In scope planning decomposition |
| FR-004 outcome: long preview | A long rendered formula is bounded and horizontally scrollable above the IME. | US-3 | TC-US-3-01 | In scope planning decomposition |
| FR-005 outcome: validation recovery | Invalid or unsupported LaTex remains editable with localized feedback and no document write. | US-1 | TC-US-1-04 | In scope planning decomposition |
| FR-003 outcome: no-selection title | A selected note title is inserted when the editor has no selected text or focused block. | US-4 | TC-US-4-03 | In scope planning decomposition |
| FR-003 outcome: cancellation | Picker back/cancel leaves the editor document and pending selection unchanged. | US-4 | TC-US-4-05 | In scope planning decomposition |
| FR-006 outcome: deleted target | Deleting a linked target removes the whole linked label/title from document and exports. | US-4 | TC-US-4-06 | In scope planning decomposition |
| FR-006 outcome: unknown annotation | Unknown or malformed annotation remains readable, non-clickable, and plain in export. | US-4 | TC-US-4-07 | In scope planning decomposition |
| FR-007 outcome: editable collapsed cursor | Editable inline controls are enabled and follow pending typing semantics without turning direct actions into modes. | US-2 | TC-US-2-08 | In scope planning decomposition |
| AC-005 outcome: no-selection title | Choosing a target with no selected text inserts its title as the link label. | US-4 | TC-US-4-03 | In scope planning decomposition |
| AC-008 outcome: formula edit | Tapping a rendered formula opens the source in edit mode and a valid update replaces the atom. | US-1 | TC-US-1-05 | In scope planning decomposition |
| AC-008 outcome: invalid draft | Invalid draft remains in the open sheet and does not change the saved document. | US-1 | TC-US-1-04 | In scope planning decomposition |
| AC-009 outcome: formula sheet IME | Formula sheet source, preview, and actions stay usable above the IME and the editor rail is absent behind the sheet. | US-3 | TC-US-3-02 | In scope planning decomposition |
| AC-016 outcome: deleted target | A deleted target removes the entire linked label/title rather than retaining a plain label. | US-4 | TC-US-4-06 | In scope planning decomposition |
| AC-016 outcome: unresolved annotation | An otherwise unresolved annotation retains readable non-clickable plain text. | US-4 | TC-US-4-07 | In scope planning decomposition |
| Edge case: collapsed cursor | Body is a no-op; each inline mark toggles pending typing; Link and Formula remain direct actions. | US-2 | TC-US-2-08 | In scope |
| Edge case: no focused block or cross-block selection | Body and inline marks do not mutate document or typing state; insertion fallbacks are tested separately. | US-2 | TC-US-2-02 | In scope |
| Edge case: root-level target | Picker uses localized No folder subtitle. | US-4 | TC-US-4-01 | In scope |
| Edge case: empty/search-empty picker | Picker displays localized empty state and permits no selection. | US-4 | TC-US-4-01 | In scope |
| Edge case: current note | Current note is absent from candidate results. | US-4 | TC-US-4-01 | In scope |
| Edge case: invalid or unsupported LaTex | Formula draft stays open and the pre-existing document is preserved. | US-1 | TC-US-1-04 | In scope |
| Edge case: formula deletion | Any deletion touching a formula removes the whole formula atom. | US-1 | TC-US-1-06 | In scope |
| Edge case: unknown or legacy annotation | Visible text is not erased or crashed and exports as readable fallback. | US-4 | TC-US-4-07 | In scope |
| Edge case: read-only access change | All controls reject mutation and navigation while remaining visible and disabled. | US-4 | TC-US-4-09 | In scope |
| Data: backward-compatible document JSON | Optional link and formula fields preserve old documents and stable inline identity. | US-1 | TC-US-1-07 | In scope |
| Data: autosave and reload | Formatting mutations reuse autosave and survive reload. | US-4 | TC-US-4-08 | In scope |
| NFR: API 24 and offline renderer | Concrete renderer fixtures work offline on API 24-compatible code and in light/dark themes. | US-1 | TC-US-1-08 | In scope |
| NFR: privacy and observability | Diagnostics never include note content, titles, labels, IDs, or formula source. | US-4 | TC-US-4-08 | In scope |
| NFR: accessibility | Controls, links, picker rows, formula source, validation, and read-only states expose localized semantics and 48dp targets. | US-4 | TC-US-4-09 | In scope |
| NFR: localization | User-visible action, error, empty, folder, and accessibility copy comes from resources. | US-4 | TC-US-4-01 | In scope |
| NFR: responsive IME | Editor and formula sheet respect insets and remain usable above the keyboard. | US-3 | TC-US-3-02 | In scope |
| Verification: JVM | Mapper, reducer, renderer, persistence, fallback, and export contracts have deterministic JVM coverage. | US-1 | TC-US-1-07 | In scope |
| Verification: integration | Local notes and folders feed link resolution, autosave/reload, and exporters without endpoint use. | US-4 | TC-US-4-08 | In scope |
| Verification: instrumented UI | Production toolbar, picker, formula sheet, selection, keyboard, navigation, and disabled semantics are exercised. | US-4 | TC-US-4-09 | In scope |
| Verification: visual | Final production states are captured inside dedicated VisualFlowTest methods and compared against the approved design assets. | US-4 | TC-US-4-VIS-001 | In scope |
| Design: editor toolbar rail | Preserve flat 56dp toolbar, stable tags, selected states, horizontal accessibility scrolling, and all eight requested controls. | US-2 | TC-US-2-09 | In scope |
| Design: editor toolbar above IME | Keep the formatting rail visible above the keyboard with no overlap. | US-2 | TC-US-2-09 | In scope |
| Design: formula preview | Bound long preview horizontally without wrapping or clipping and keep actions reachable. | US-3 | TC-US-3-01 | In scope |
| Design: formula sheet IME | Keep the sheet expanded and usable above the IME; do not render the editor rail behind it. | US-3 | TC-US-3-02 | In scope |
| Design: formula submit action | Use one consistent text-only Insert or Update action with no extra plus icon. | US-3 | TC-US-3-03 | In scope |
| Design: picker hierarchy | Use full-screen back/title, search, 16dp rhythm, 48dp rows, folder subtitle, and no self-link. | US-4 | TC-US-4-01 | In scope |
| Design: valid link rendering | Retained labels use primary-color underlined tappable styling and stable semantic identity. | US-4 | TC-US-4-04 | In scope |
| Design: atomic formula | Rendered formula has one stable editable/deletable identity and never exposes raw fragments after deletion. | US-1 | TC-US-1-06 | In scope |
| Design: final toolbar visual | Selected editor state matches the approved toolbar mockup and measured anchor contract. | US-4 | TC-US-4-VIS-001 | In scope |
| Design: final keyboard visual | Bold-selected pending typing state shows toolbar above the IME and inherited formatting. | US-4 | TC-US-4-VIS-002 | In scope |
| Design: final picker visual | Picker rows and folder subtitles match the approved picker mockup. | US-4 | TC-US-4-VIS-003 | In scope |
| Design: final formula visuals | Formula default, invalid, keyboard, and dark-theme states match the approved formula-sheet mockups and responsive contract. | US-4 | TC-US-4-VIS-004 | In scope |

## Dependency Order and Risk

The dependency order is linear: US-1 establishes the formula atom and concrete renderer, US-2 establishes the shared inline-mark and pending-typing model, US-3 hardens the formula sheet layout against the IME, and US-4 adds cross-note navigation plus the final persistence, read-only, and visual contract. The highest technical risk is US-1 because renderer licensing/API-24 compatibility and atomic rich-text deletion must be proven through the existing editor before later UI polish is accepted. Every slice remains observable through an already-reachable Note Editor toolbar action.

## User Scenarios & Testing

### US-1: Insert, edit, and atomically delete inline formulas (Priority: P1)

An editor opens the existing Formula action, enters valid LaTex, and sees it rendered inline. The editor can insert at a selection, cursor, or fallback paragraph, tap the rendered object to edit its source, save and export it, and delete the whole object without leaving source fragments. Invalid input stays in the sheet and never overwrites the note.

**Why this priority**: The offline renderer and atomic document representation are the highest-risk dependencies. Delivering them first gives the later responsive sheet and final visual flow a real production consumer.

**Independent Test**: Use the existing Formula toolbar action in a production editor for selection, cursor, and no-focused-block insertion, edit and delete a rendered formula, then reload/export and run the renderer fixtures.

**Acceptance Criterion**:

1. **AC-US-1-01 Given** a non-empty selection in a focused text block, **When** a valid formula is submitted, **Then** the selection is replaced by one rendered inline formula and surrounding text remains ordered.
2. **AC-US-1-02 Given** a focused collapsed cursor, **When** a valid formula is submitted, **Then** it is inserted at the cursor without changing adjacent text.
3. **AC-US-1-03 Given** no text block is focused, **When** a valid formula is submitted, **Then** a paragraph is appended and focused before the formula is inserted.
4. **AC-US-1-04 Given** invalid or unsupported source, **When** the draft is edited or submitted, **Then** the sheet remains open with localized feedback and the saved document is unchanged.
5. **AC-US-1-05 Given** a rendered formula, **When** it is tapped and valid source is updated, **Then** the source is restored in edit mode and the updated formula replaces the same atom.
6. **AC-US-1-06 Given** a cursor deletion touches any part of a rendered formula, **When** deletion is issued, **Then** the complete formula object and annotation disappear with no raw LaTex fragment.
7. **AC-US-1-07 Given** a formula document is saved and reloaded, **When** Markdown and PDF are exported, **Then** the formula source and rendered/export behavior remain correct and old JSON remains readable.
8. **AC-US-1-08 Given** fixed valid and invalid LaTex fixtures, **When** the concrete renderer is invoked offline in light and dark themes, **Then** output is deterministic, invalid input is explicit, and no network is required.

**Acceptance Test Cases**:

| Test ID | Covers AC | Test layer | Test file and method | Shared scenario(s) | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|---|
| TC-US-1-01 | AC-US-1-01 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorFormulaSheetTest.kt#formulaActionReplacesSelectionWithRenderedFormula` | N/A — no API | Render the production editor with a selected range, open Formula, enter valid source, and submit. | Selected text is replaced by one rendered formula atom, prefix and suffix remain ordered, and the editor remains reachable. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorFormulaSheetTest#formulaActionReplacesSelectionWithRenderedFormula` |
| TC-US-1-02 | AC-US-1-02 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorFormulaSheetTest.kt#formulaActionInsertsAtCollapsedCursor` | N/A — no API | Focus a real text block at a collapsed cursor, open Formula, and submit valid source. | Formula is inserted at the cursor, neighboring text is preserved, and selection/focus state is valid. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorFormulaSheetTest#formulaActionInsertsAtCollapsedCursor` |
| TC-US-1-03 | AC-US-1-03 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorFormulaSheetTest.kt#formulaActionAppendsAndFocusesWhenNoTextBlockIsFocused` | N/A — no API | Render a production editor with no focused text block, open Formula, and submit valid source. | A text paragraph is appended and focused, the formula is inserted there, and the document autosaves. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorFormulaSheetTest#formulaActionAppendsAndFocusesWhenNoTextBlockIsFocused` |
| TC-US-1-04 | AC-US-1-04 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorFormulaSheetTest.kt#invalidFormulaStaysEditableWithoutChangingDocument` | N/A — no API | Open the production formula sheet, enter invalid source, and attempt to submit. | Source remains editable, localized error and safe preview fallback are visible, submit does not write, and the prior document state is unchanged. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorFormulaSheetTest#invalidFormulaStaysEditableWithoutChangingDocument` |
| TC-US-1-05 | AC-US-1-05 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorFormulaSheetTest.kt#tappingFormulaReopensSourceAndValidUpdatePersists` | N/A — no API | Insert a formula through the production flow, tap its rendered node, change the source, and submit. | Source is prefilled, update replaces the same stable atom, rendered output changes, and reload returns the updated source. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorFormulaSheetTest#tappingFormulaReopensSourceAndValidUpdatePersists` |
| TC-US-1-06 | AC-US-1-06 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorFormulaSheetTest.kt#deletingInlineFormulaRemovesWholeFormulaAtom` | N/A — no API | Insert a rendered formula in production content, place the caret within its visual range, and issue backspace or delete. | The complete formula node and source annotation are removed as one unit, surrounding text remains, and no raw LaTex fragment is present. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorFormulaSheetTest#deletingInlineFormulaRemovesWholeFormulaAtom` |
| TC-US-1-07 | AC-US-1-07 | JVM integration | `app/src/test/java/com/example/notesapp/ui/editor/mapper/NoteDocumentTest.kt#formulaDocumentRoundTripAndMarkdownExport` | N/A — no API | Serialize a document containing formula annotations, reload it, and invoke Markdown and PDF exporters. | Optional fields round-trip, old JSON remains readable, formula source exports as `$source$`, PDF receives the rendered formula contract, and autosave data is stable. | `env ./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.mapper.NoteDocumentTest"` |
| TC-US-1-08 | AC-US-1-08 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/components/InlineFormulaRendererTest.kt#supportedFixturesRenderOfflineInLightAndDarkThemes` | N/A — no API | Run fixed valid and invalid LaTex fixtures through the selected concrete adapter without network access. | Valid output is deterministic in light and dark themes, invalid output is explicit, API-24-compatible code paths are used, and no network call occurs. | `env ./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.components.InlineFormulaRendererTest"` |

### US-2: Reset selected text and inherit inline marks while typing (Priority: P2)

An editor uses Body as a direct reset action and uses Bold, Italic, Underline, Strikethrough, and Code on selected text or at a focused collapsed cursor. Selected operations affect only the intended range. A pending mark is visible in the toolbar and applies exactly to subsequent typed text. When Enter creates a new line at a focused collapsed cursor, the new line preserves the current block style and effective inline marks at the caret while the existing line remains unchanged; the editor rail remains available above the keyboard.

**Why this priority**: This slice establishes the shared rich-text selection and typing-state model used by the visible toolbar, with no new navigation or renderer dependency.

**Independent Test**: In the production editor, select marked text and invoke each action, then place a collapsed cursor, toggle marks in combinations, type, press Enter, continue typing, toggle them off, and inspect the resulting spans and exports.

**Acceptance Criterion**:

1. **AC-US-2-01 Given** a non-empty formatted selection, **When** Body is tapped, **Then** only the selected range becomes plain Paragraph text and surrounding formatting stays unchanged.
2. **AC-US-2-02 Given** no selection or a cross-block selection, **When** Body is tapped, **Then** document, selection, and typing state remain unchanged.
3. **AC-US-2-03 Given** selected text or a collapsed cursor, **When** Code is toggled, **Then** only the selected range or following typed text receives code styling and Markdown backticks.
4. **AC-US-2-04 Given** a non-empty selected range, **When** Bold is toggled, **Then** only that range changes bold state.
5. **AC-US-2-05 Given** a non-empty selected range, **When** Italic is toggled, **Then** only that range changes italic state.
6. **AC-US-2-06 Given** a non-empty selected range, **When** Underline is toggled, **Then** only that range changes underline state.
7. **AC-US-2-07 Given** a non-empty selected range, **When** Strikethrough is toggled, **Then** only that range changes strikethrough state.
8. **AC-US-2-08 Given** a focused collapsed cursor, **When** any inline-format button is selected, **Then** the selected state and subsequently typed spans match exactly the active marks and direct Body, Link, and Formula actions do not become typing modes.
9. **AC-US-2-09 Given** an editable focused editor with the IME visible, **When** the editor is laid out, **Then** the 56dp formatting rail stays visible above the keyboard without covering the input.
10. **AC-US-2-10 Given** a focused editable `TextBlock` with a collapsed cursor and active formatting at the caret, **When** the user presses Enter, **Then** the new line inherits the current block style and effective inline marks, text typed after the break keeps those marks, and the existing line remains unchanged.

**Acceptance Test Cases**:

| Test ID | Covers AC | Test layer | Test file and method | Shared scenario(s) | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|---|
| TC-US-2-01 | AC-US-2-01 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorSelectionFormattingTest.kt#bodyActionRemovesAllFormattingFromSelectedText` | N/A — no API | Render production content with mixed block and inline marks, select one range, and tap Body. | Selected range is plain Paragraph text with no inline marks; prefix, suffix, text order, autosave, reload, and export remain correct. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorSelectionFormattingTest#bodyActionRemovesAllFormattingFromSelectedText` |
| TC-US-2-02 | AC-US-2-02 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorViewModelTest.kt#bodyResetWithNoOrCrossBlockSelectionLeavesDocumentUnchanged` | N/A — no API | Apply the Body reducer to collapsed, unfocused, and cross-block selection fixtures. | Document, selection, pending typing marks, and autosave invocation remain unchanged. | `env ./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelTest"` |
| TC-US-2-03 | AC-US-2-03 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorSelectionFormattingTest.kt#codeActionChangesOnlyTheSelectedRange` | N/A — no API | Select a range in the production editor and tap Code, then verify a collapsed-cursor typing run. | Selected code toggles only its range, renders monospace, following typed text receives code, and Markdown uses backticks. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorSelectionFormattingTest#codeActionChangesOnlyTheSelectedRange` |
| TC-US-2-04 | AC-US-2-04 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorSelectionFormattingTest.kt#boldActionChangesOnlyTheSelectedRange` | N/A — no API | Select text with neighboring marks in the production editor and tap Bold. | Only the selected range toggles bold; other marks, neighboring text, and selection are unchanged. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorSelectionFormattingTest#boldActionChangesOnlyTheSelectedRange` |
| TC-US-2-05 | AC-US-2-05 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorSelectionFormattingTest.kt#italicActionChangesOnlyTheSelectedRange` | N/A — no API | Select text with neighboring marks in the production editor and tap Italic. | Only the selected range toggles italic; other marks and neighboring text are unchanged. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorSelectionFormattingTest#italicActionChangesOnlyTheSelectedRange` |
| TC-US-2-06 | AC-US-2-06 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorSelectionFormattingTest.kt#underlineActionChangesOnlyTheSelectedRange` | N/A — no API | Select text with neighboring marks in the production editor and tap Underline. | Only the selected range toggles underline; other marks and neighboring text are unchanged. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorSelectionFormattingTest#underlineActionChangesOnlyTheSelectedRange` |
| TC-US-2-07 | AC-US-2-07 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorSelectionFormattingTest.kt#strikethroughActionChangesOnlyTheSelectedRange` | N/A — no API | Select text with neighboring marks in the production editor and tap Strikethrough. | Only the selected range toggles strikethrough; other marks and neighboring text are unchanged. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorSelectionFormattingTest#strikethroughActionChangesOnlyTheSelectedRange` |
| TC-US-2-08 | AC-US-2-08 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorSelectionFormattingTest.kt#inlineMarksApplyToFollowingTypedTextAtCollapsedCursor` | N/A — no API | Place a real collapsed cursor, toggle each inline format independently and in combination, type, toggle off, and invoke Body, Link, and Formula actions. | Toolbar selected state equals pending marks; newly typed text inherits exactly Bold, Italic, Underline, Strikethrough, and Code; adjacent text is unchanged and direct actions do not persist as marks. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorSelectionFormattingTest#inlineMarksApplyToFollowingTypedTextAtCollapsedCursor` |
| TC-US-2-09 | AC-US-2-09 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorSelectionFormattingTest.kt#formattingToolbarRemainsVisibleAboveIme` | N/A — no API | Focus the production editor input, open the IME, and measure the toolbar and focused field. | Toolbar exists above the IME, retains 56dp height, does not overlap the keyboard or focused input, and remains horizontally scrollable for all controls. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorSelectionFormattingTest#formattingToolbarRemainsVisibleAboveIme` |
| TC-US-2-10 | AC-US-2-10 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorSelectionFormattingTest.kt#newLinePreservesCurrentFormatting` | N/A — no API | Render a focused production text block with active inline marks, place a collapsed cursor, press Enter, and type on the new line. | New line and newly typed text retain the current block style and active marks; the original line remains unchanged. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorSelectionFormattingTest#newLinePreservesCurrentFormatting` |

### US-3: Keep long formula previews usable above the keyboard (Priority: P3)

An editor uses the Formula action delivered by US-1 on a phone-sized viewport with the IME visible. The sheet stays open and bounded above the keyboard, a long preview scrolls horizontally without wrapping or clipping, actions remain reachable, and every formula state has one text-only Insert or Update action.

**Why this priority**: It hardens the highest-risk visual interaction after formula semantics work, while remaining independently observable through the existing Formula entry point.

**Independent Test**: Open Formula from the production editor, enter a long formula with the keyboard visible, assert bounds and horizontal scroll semantics, then verify the sheet action count and formula-sheet toolbar isolation.

**Acceptance Criterion**:

1. **AC-US-3-01 Given** a formula preview wider than the viewport, **When** the sheet is open, **Then** the preview is bounded and horizontally scrollable without wrapping or clipping.
2. **AC-US-3-02 Given** the formula source input owns focus with the IME visible, **When** the sheet is laid out, **Then** the sheet, source, preview, Cancel, and Insert or Update action remain reachable above the IME and the editor toolbar is absent behind it.
3. **AC-US-3-03 Given** any formula-sheet mode, **When** the actions render, **Then** exactly one text-only Insert or Update action is shown and no extra plus-icon insert action is present.

**Acceptance Test Cases**:

| Test ID | Covers AC | Test layer | Test file and method | Shared scenario(s) | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|---|
| TC-US-3-01 | AC-US-3-01 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorFormulaSheetResponsiveTest.kt#longFormulaPreviewScrollsHorizontallyWithoutWrapping` | N/A — no API | Open the real Formula sheet, enter a long valid source, and inspect the preview viewport. | Preview is horizontally scrollable, content does not wrap or clip, sheet height is bounded by available space, and actions remain in the visible sheet. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorFormulaSheetResponsiveTest#longFormulaPreviewScrollsHorizontallyWithoutWrapping` |
| TC-US-3-02 | AC-US-3-02 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorFormulaSheetResponsiveTest.kt#formulaSheetRemainsOpenAndActionsReachableAboveIme` | N/A — no API | Focus the production formula source field and await the IME. | Formula sheet remains open above the IME, source/preview/Cancel/submit are visible or reachable through bounded scrolling, and the editor formatting rail is not behind the sheet. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorFormulaSheetResponsiveTest#formulaSheetRemainsOpenAndActionsReachableAboveIme` |
| TC-US-3-03 | AC-US-3-03 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorFormulaSheetResponsiveTest.kt#formulaSheetUsesSingleTextOnlyInsertAction` | N/A — no API | Render insert and edit formula-sheet modes through the production Formula action. | Each mode has one localized text-only Insert or Update action, no plus-icon duplicate is exposed, and both actions meet the target-size contract. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorFormulaSheetResponsiveTest#formulaSheetUsesSingleTextOnlyInsertAction` |

### US-4: Link text to existing notes and protect the completed toolbar contract (Priority: P4)

An editor opens the existing Link action, searches local notes, and chooses a destination other than the current note. The selected label is retained or the target title is inserted when no text is selected, valid links are tappable primary-color underlined labels, and remove, replace, cancel, deletion cleanup, fallback, persistence, export, read-only, and final visual states follow the approved contract.

**Why this priority**: Links add the only new full-screen navigation flow. They depend on the stable rich-text representation and completed editor controls, and this slice is the final user-reachable visual-verification owner.

**Independent Test**: Start from the production editor Link action, exercise picker loading/search/empty/error, selected and no-selection insertion, target navigation, remove/replace/cancel, delete-target cleanup, fallback, reload/export, and read-only rendering; then run all dedicated in-test visual captures.

**Acceptance Criterion**:

1. **AC-US-4-01 Given** the Link action is opened, **When** local candidates load or search changes, **Then** the full-screen picker excludes the current note, shows title plus parent-folder or No folder subtitle, and exposes loading, empty, and recoverable error states.
2. **AC-US-4-02 Given** editor text is selected, **When** a target note is chosen, **Then** the selected text remains the link label and the stable target ID is stored separately.
3. **AC-US-4-03 Given** no editor text is selected or no text block is focused, **When** a target note is chosen, **Then** its title is inserted as the link label at the cursor or in an appended focused paragraph.
4. **AC-US-4-04 Given** a valid internal link exists, **When** it is rendered and tapped, **Then** it is primary-color, underlined, tappable, and opens the target Editor route.
5. **AC-US-4-05 Given** an existing link or a pending picker, **When** Remove link, replacement, or cancel/back is used, **Then** the label becomes plain, the target changes, or the document remains unchanged respectively.
6. **AC-US-4-06 Given** a linked target note is deleted, **When** the source note resolves, **Then** the complete linked label/title and annotation are removed from document and exports.
7. **AC-US-4-07 Given** an unknown or malformed annotation cannot resolve, **When** the document renders or exports, **Then** readable plain non-clickable text remains and no crash occurs.
8. **AC-US-4-08 Given** the complete annotated document is saved, reloaded, and exported, **When** diagnostics are produced, **Then** all supported marks, links, and formulas retain their contract, Markdown/PDF use the specified forms, and sensitive user content is absent from logs.
9. **AC-US-4-09 Given** a read-only note is displayed, **When** all completed formatting controls render, **Then** they are visible, semantically disabled, and cannot mutate or navigate.
10. **AC-US-4-10 Given** the completed toolbar is rendered with a selection, **When** the dedicated visual test captures it, **Then** the production chrome matches the approved selection mockup and anchor contract.
11. **AC-US-4-11 Given** the editor IME is visible with Bold pending, **When** the dedicated visual test captures it, **Then** the toolbar remains above the keyboard and following text visibly inherits Bold.
12. **AC-US-4-12 Given** the production Link picker contains candidates, **When** the dedicated visual test captures it, **Then** title, search, folder subtitles, spacing, and self-link exclusion are reviewable.
13. **AC-US-4-13 Given** the production formula sheet is in its default state, **When** the dedicated visual test captures it, **Then** source, preview, and one text-only submit action are reviewable.
14. **AC-US-4-14 Given** the production formula sheet has invalid source, **When** the dedicated visual test captures it, **Then** localized validation and safe preview fallback are reviewable without a document mutation.
15. **AC-US-4-15 Given** the production formula sheet is above the IME with a long preview, **When** the dedicated visual test captures it, **Then** bounded scrolling and reachable actions are reviewable.
16. **AC-US-4-16 Given** the production formula sheet is rendered in dark theme, **When** the dedicated visual test captures it, **Then** renderer and sheet contrast remain compliant with the design system.

**Acceptance Test Cases**:

| Test ID | Covers AC | Test layer | Test file and method | Shared scenario(s) | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|---|
| TC-US-4-01 | AC-US-4-01 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteLinkPickerScreenTest.kt#pickerSearchesCandidatesExcludesCurrentNoteAndShowsParentFolder` | N/A — no API | Seed local notes and folders including caller, root, matching, empty, and error fixtures; open Link and search. | Full-screen picker, current-note exclusion, title/folder or No folder subtitles, loading/empty/error/retry states, and 48dp row targets are correct. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteLinkPickerScreenTest#pickerSearchesCandidatesExcludesCurrentNoteAndShowsParentFolder` |
| TC-US-4-02 | AC-US-4-02 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteLinkPickerScreenTest.kt#pickerReturnsTargetAndPreservesSelectedLabel` | N/A — no API | Select a real editor label, open Link, choose a candidate, and return through the production route. | Label text is retained, target ID is persisted separately, selection context survives return, and autosave receives the link. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteLinkPickerScreenTest#pickerReturnsTargetAndPreservesSelectedLabel` |
| TC-US-4-03 | AC-US-4-03 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteLinkPickerScreenTest.kt#pickerInsertsTargetTitleWithoutSelection` | N/A — no API | Open Link with a collapsed cursor and with no focused block, choose a target, and return. | Target title is inserted at the cursor or in an appended focused paragraph, receives a stable target ID, and remains editable. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteLinkPickerScreenTest#pickerInsertsTargetTitleWithoutSelection` |
| TC-US-4-04 | AC-US-4-04 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteLinkPickerScreenTest.kt#validInternalLinkIsStyledAndOpensTarget` | N/A — no API | Render a valid inserted link in the production editor and tap its semantic link node. | Label is primary-color, underlined, tappable, stable-tagged, and dispatches selection callback with the target note ID. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteLinkPickerScreenTest#validInternalLinkIsStyledAndOpensTarget` |
| TC-US-4-05 | AC-US-4-05 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteLinkPickerScreenTest.kt#removeLinkReplaceAndCancelHaveSpecifiedOutcomes` | N/A — no API | Open Link on an existing link, run Remove link, replace with another target, and repeat with cancel/back. | Remove strips only the target, replacement changes the target ID, and cancel/back preserves the original document and selection. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteLinkPickerScreenTest#removeLinkReplaceAndCancelHaveSpecifiedOutcomes` |
| TC-US-4-06 | AC-US-4-06 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteLinkPickerScreenTest.kt#deletingLinkedTargetRemovesEntireLabel` | N/A — no API | Create a source note with a linked label, delete its target through the production note flow, and reload the source. | Entire linked label/title and annotation are gone from the document, Markdown, and PDF; no orphan label remains. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteLinkPickerScreenTest#deletingLinkedTargetRemovesEntireLabel` |
| TC-US-4-07 | AC-US-4-07 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteLinkPickerScreenTest.kt#unresolvedAnnotationRendersReadablePlainText` | N/A — no API | Load legacy, malformed, and otherwise unresolved annotations through the production editor. | Readable label remains non-clickable plain text, export is plain, and the renderer does not crash or erase content. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteLinkPickerScreenTest#unresolvedAnnotationRendersReadablePlainText` |
| TC-US-4-08 | AC-US-4-08 | JVM integration | `app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorNoteLinkTest.kt#allFormattingAnnotationsRoundTripAndExportSafely` | N/A — no API | Save and reload a combined document with all marks, code, link, and formula annotations, then invoke Markdown/PDF export and safe diagnostics. | Optional JSON fields, autosave/reload, `notesapp://note/<id>` links, formula `$source$`, clickable PDF links when supported, deleted-label cleanup, unknown fallback, and privacy constraints all hold. | `env ./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.viewmodel.NoteEditorNoteLinkTest"` |
| TC-US-4-09 | AC-US-4-09 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorFormattingReadOnlyTest.kt#formattingControlsAreVisibleDisabledAndInert` | N/A — no API | Render the production read-only editor with every completed formatting control and a link/formula fixture. | All controls have visible disabled semantics and localized descriptions, no document mutation occurs, and picker/sheet/navigation do not open. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorFormattingReadOnlyTest#formattingControlsAreVisibleDisabledAndInert` |
| TC-US-4-VIS-001 | AC-US-4-10 | Visual verification | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/FormattingToolbarVisualFlowTest.kt#captureToolbarSelection` | N/A — no API | Navigate through the production editor to a selected-text state, wait for idle, and capture inside the active test window. | Non-empty in-test capture is saved to `visual_evidence/formatting_toolbar_selection.png` and shows the flat 56dp rail, requested controls, selected state, and design-system chrome. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.FormattingToolbarVisualFlowTest#captureToolbarSelection && adb -s emulator-5554 pull /sdcard/Download/formatting_toolbar_selection.png "$FEATURE_DIR/visual_evidence/formatting_toolbar_selection.png" && test -s "$FEATURE_DIR/visual_evidence/formatting_toolbar_selection.png"` |
| TC-US-4-VIS-002 | AC-US-4-11 | Visual verification | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/FormattingToolbarVisualFlowTest.kt#captureEditorKeyboard` | N/A — no API | Navigate through the production editor, focus input, select Bold, await the IME, wait for idle, and capture inside the active test window. | Non-empty in-test capture is saved to `visual_evidence/formatting_toolbar_keyboard.png` and shows the toolbar above the keyboard plus visibly inherited Bold text. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.FormattingToolbarVisualFlowTest#captureEditorKeyboard && adb -s emulator-5554 pull /sdcard/Download/formatting_toolbar_keyboard.png "$FEATURE_DIR/visual_evidence/formatting_toolbar_keyboard.png" && test -s "$FEATURE_DIR/visual_evidence/formatting_toolbar_keyboard.png"` |
| TC-US-4-VIS-003 | AC-US-4-12 | Visual verification | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/FormattingToolbarVisualFlowTest.kt#captureLinkPicker` | N/A — no API | Navigate through the production Link action to populated picker content, wait for idle, and capture inside the active test window. | Non-empty in-test capture is saved to `visual_evidence/note_link_picker.png` and shows title, search, candidate title/folder subtitle, spacing, and no current note. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.FormattingToolbarVisualFlowTest#captureLinkPicker && adb -s emulator-5554 pull /sdcard/Download/note_link_picker.png "$FEATURE_DIR/visual_evidence/note_link_picker.png" && test -s "$FEATURE_DIR/visual_evidence/note_link_picker.png"` |
| TC-US-4-VIS-004 | AC-US-4-13 | Visual verification | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/FormattingToolbarVisualFlowTest.kt#captureFormulaDefault` | N/A — no API | Navigate through the production Formula action to its default sheet state, wait for idle, and capture inside the active test window. | Non-empty in-test capture is saved to `visual_evidence/formula_sheet_default.png` and shows source, preview, Cancel, and one text-only Insert action. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.FormattingToolbarVisualFlowTest#captureFormulaDefault && adb -s emulator-5554 pull /sdcard/Download/formula_sheet_default.png "$FEATURE_DIR/visual_evidence/formula_sheet_default.png" && test -s "$FEATURE_DIR/visual_evidence/formula_sheet_default.png"` |
| TC-US-4-VIS-005 | AC-US-4-14 | Visual verification | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/FormattingToolbarVisualFlowTest.kt#captureFormulaInvalid` | N/A — no API | Navigate through the production Formula action, enter invalid source, wait for validation, and capture inside the active test window. | Non-empty in-test capture is saved to `visual_evidence/formula_sheet_invalid.png` and shows localized error, safe preview fallback, unchanged editor, and reachable actions. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.FormattingToolbarVisualFlowTest#captureFormulaInvalid && adb -s emulator-5554 pull /sdcard/Download/formula_sheet_invalid.png "$FEATURE_DIR/visual_evidence/formula_sheet_invalid.png" && test -s "$FEATURE_DIR/visual_evidence/formula_sheet_invalid.png"` |
| TC-US-4-VIS-006 | AC-US-4-15 | Visual verification | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/FormattingToolbarVisualFlowTest.kt#captureFormulaSheetKeyboard` | N/A — no API | Navigate through the production Formula action, focus source, enter a long formula, await the IME, wait for idle, and capture inside the active test window. | Non-empty in-test capture is saved to `visual_evidence/formula_sheet_keyboard.png` and shows the sheet above the IME, bounded horizontal preview, and reachable Cancel/Insert actions. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.FormattingToolbarVisualFlowTest#captureFormulaSheetKeyboard && adb -s emulator-5554 pull /sdcard/Download/formula_sheet_keyboard.png "$FEATURE_DIR/visual_evidence/formula_sheet_keyboard.png" && test -s "$FEATURE_DIR/visual_evidence/formula_sheet_keyboard.png"` |
| TC-US-4-VIS-007 | AC-US-4-16 | Visual verification | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/FormattingToolbarVisualFlowTest.kt#captureFormulaSheetDarkTheme` | N/A — no API | Navigate through the production Formula action, switch the production theme to dark, wait for idle, and capture inside the active test window. | Non-empty in-test capture is saved to `visual_evidence/formula_sheet_dark_theme.png` and shows readable renderer output, surface contrast, and compliant controls. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.FormattingToolbarVisualFlowTest#captureFormulaSheetDarkTheme && adb -s emulator-5554 pull /sdcard/Download/formula_sheet_dark_theme.png "$FEATURE_DIR/visual_evidence/formula_sheet_dark_theme.png" && test -s "$FEATURE_DIR/visual_evidence/formula_sheet_dark_theme.png"` |

## Implementation Order and Stage Gates

1. Implement and verify US-1. Do not start US-2 until its acceptance evidence is recorded and the slice-scoped traceability and platform planning/evaluation checks pass.
2. Implement and verify US-2, including the approved editor IME-toolbar exception.
3. Implement and verify US-3 using the formula entry point already shipped by US-1.
4. Implement and verify US-4, then capture all seven visual states inside `FormattingToolbarVisualFlowTest` and create the required reference-anchor report before final evaluation.

Every slice must keep `evidence: []` until its declared command exits 0 and its result is recorded. No slice may be marked `passing` from a manual claim or a post-test external screenshot.

## 📊 Sprint Log

| Phase | Agent | Target / Outcome | Notes & Core Decisions |
| :--- | :--- | :--- | :--- |
| **Planning** | Planner | `sprint-contract.md` recompiled | Four vertical slices are ordered by risk and dependency; US-4 is the only visual-verification owner. |
| **Implementation** | Generator | Awaiting user approval | Start with US-1 only after the user approves this feature list and sprint contract. |
| **Review 1** | Evaluator | Pending | Review architecture, tests, quality, accessibility, navigation, localization, observability, and visual evidence after implementation. |
| **Revision 1** | Generator | Pending | Resolve all evaluator findings without suppression or stale evidence. |
| **Final Review** | Evaluator | Pending | Evaluate the complete toolbar, picker, formula states, exports, and visual anchor report. |
