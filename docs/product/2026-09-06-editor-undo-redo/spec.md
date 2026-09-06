# Feature Spec — Note Editor Undo & Redo

**Date**: 2026-09-06
**Status**: Draft — for specification approval
**Related design**: `design.md`

## Objective

Wire the Note Editor's existing bottom-toolbar Undo and Redo controls to a real, session-scoped document history. While an editable note is open, every change to the note's document (body text, block structure, rich formatting, tables, charts, code, mermaid, formulas, links, emoji, voice blocks) becomes reversible one step at a time and replayable, without persisting any history to disk. This turns currently inert toolbar buttons into a dependable safety net for editing mistakes.

## User Goal

As a person editing a note, I want to reverse my most recent document change (Undo) and re-apply a change I reversed (Redo) so that a mistaken edit never permanently damages my note while I am working on it.

## Scope

### In Scope

- One in-memory undo/redo history for the open note's **document** (`NoteDocument` block list), created when an editable note is opened and cleared when the editor closes or the process ends.
- Undo/redo coverage of every document edit made while the note is open: typed body text (paragraphs, headings, lists, to-dos, toggles, callouts, quotes, voice transcripts), Enter block splits, block type conversions, block insertions/deletions (basic blocks, code, mermaid, chart, table, image, voice), checkbox and toggle-expansion flips, inline Bold/Italic/Underline/Strikethrough/Code marks over a selection, emoji insertion, note-link insert/remove/replace, inline formula insert/update/remove, table row/column operations and table cell text, chart title/data-cell/options edits, code language/code text edits, mermaid title/code edits, and image URL/caption edits.
- Continuous typing coalesced into a single undo step per typing run; every discrete action is its own step.
- Toolbar Undo/Redo controls with enabled/disabled visual and semantic states driven by `canUndo`/`canRedo`.
- Hardware-keyboard shortcuts on the whole editor screen while editing an editable note: **Ctrl+Z** = undo, **Ctrl+Shift+Z** and **Ctrl+Y** = redo.
- Caret/selection restoration after undo/redo to the block and offset where the reverted/re-applied change happened; when the previously focused block no longer exists after an undo, focus falls back to the nearest preceding block.
- Read-only/protected notes keep no undo/redo surface: the read-only bottom bar does not render the controls and shortcuts are inert.

### Out Of Scope

- Title-field undo/redo (the note title is **never** part of the history, per user decision).
- Persisting history across editor sessions, note reopen, or process death; history is not written to Room, DataStore, or any file.
- Undo/redo of note-level operations that are not document edits: favorite/pin toggles, folder moves, note delete, rename dialog outcomes (the rename dialog edits the title), export, categorization, sharing.
- A settings toggle, custom history depth UI, multi-document/global history, collaborative undo, or gesture-based undo.
- Changing the position, ordering, or iconography of the existing toolbar buttons; only their behavior and enabled/disabled presentation change.
- New user-visible copy: the existing `editor_undo_description` / `editor_redo_description` strings are reused unchanged.

## Rule Applicability

| Rule ID | Rule document | Default | Decision for this change | Trigger / rationale | Planned evidence |
|---|---|---|---|---|---|
| ARCH | `android-architecture.md` | Always | Required | History state and commit/coalescing logic live in the editor presentation layer (`NoteEditorViewModel` + pure reducer/history helpers); the Composable only renders `canUndo`/`canRedo` and dispatches events; no business logic in UI. | Architecture review; ViewModel and reducer unit tests under `viewmodel/` (Kover-covered). |
| IMPL | `implementation-rules.md` | Always | Required | The two currently no-op toolbar handlers (`handleToolbarClick {}`) become real undo/redo actions; every coalescing branch, fallback-focus branch, shortcut mapping, and disabled-state path must implement the specified behavior. | Acceptance tests and code review showing no stub/no-op handlers remain on the undo/redo controls. |
| TEST | `testing-strategy.md` | Always | Required | History manager semantics (baseline, undo/redo pointer, redo clearing, coalescing, cap eviction), ViewModel state transitions, caret restoration, read-only guards, keyboard-shortcut mapping, and instrumented editor behavior need deterministic coverage. | JVM unit tests plus instrumented UI tests and one editor journey through the production entry point. |
| SUI | `compose-rules.md` | Conditional | Required | Existing editor bottom-bar controls gain enabled/disabled states, test tags, content descriptions, and keyboard-visible behavior. Approved IME exception reused (2026-09-03, formatting-toolbar): the editor bottom toolbar remains visible above the keyboard and never sits behind it or overlaps the focused input. | Stateless-content UI tests, stable tags, keyboard-visible mockup, Compose-rule checker, review of the exception. |
| L10N | `localization-rules.md` | Conditional | Not applicable — no new or changed user-visible copy; the existing localized `editor_undo_description` ("Undo") and `editor_redo_description` ("Redo") content descriptions are reused as-is. | Review confirms zero string-resource additions or edits; localization checker. |
| NAV | `navigation-rules.md` | Conditional | Not applicable — no route, destination, deep link, tab, or back-stack change; Undo is not a navigation action and Android system back remains navigation-only. | Review confirms navigation graph is untouched. |
| API | `api-contract-rules.md` | Conditional | Not applicable — no endpoint, DTO, schema, or OpenAPI change; note content remains local document JSON and history is never serialized. | Review confirms `sharedContracts/openapi.yaml` and content JSON schema are unchanged. |
| OBS | `observability.md` | Conditional | Not applicable — in-memory undo/redo adds no async, network, persistence, or error boundary and reuses the existing autosave path unchanged; no logger changes or diagnostics are introduced. | Review confirms no new log statements and no content/title/PII logged anywhere in the new code. |
| ANL | `analytics-rules.md` | Conditional | Not applicable — analytics: none; no product-approved impression, action, funnel, or error event is requested for undo/redo. | Review confirms no analytics dependency or event is added. |

## Technical Spec

### Libraries & Dependencies

| Library / SDK | Version | Purpose |
|---|---|---|
| Existing Kotlin/Compose/Room stack | Project-managed | Editor state, immutable document model, UI, and autosave. |
| New dependencies | None | History is implemented with project language/stdlib constructs (lists, immutable data classes). |

### Key Technical Decisions

- **Snapshot-based linear history**: The undo history is a list of immutable document snapshots plus a pointer. Each snapshot stores the `NoteDocument` and the editing context that existed when that document state became current (`focusedBlockId`, `selectionStart`, `selectionEnd`, `focusedTableCells`). Undo moves the pointer back and applies the previous snapshot; redo moves it forward. The baseline snapshot is the document exactly as loaded (or the empty document for a new note), so the first undo returns the note to its state when opened.
- **Single commit funnel**: Every document mutation already flows through `NoteEditorViewModel` action functions that reassign `uiStateInternal.value`. The history is captured by a single internal commit boundary all document mutations pass through, so block, text, formatting, table, chart, code, mermaid, formula, link, emoji, voice, and image edits are recorded uniformly without per-action bookkeeping. Mutations that only touch non-document state (favorite, folder, title, dialog visibility, selection bookkeeping, pending-typing-mark toggles without committed text) do not create history entries.
- **Typing coalescing**: Text commits into the same target (one `TextBlock`, one table/chart cell, one code/mermaid editor field) merge into the open top entry while they arrive without a pause longer than the coalescing window (~1 s) and without an intervening discrete action. Enter (block split), a mark/formatting application over text, emoji/link/formula insertion, structural operations, block focus change to a different target, or a longer pause closes the run and starts a new step. Coalescing is implemented as a pure, JVM-testable helper; the window is a single named constant.
- **Redo invalidation**: Any new document edit made after an undo truncates the redo tail; the new edit becomes the next entry on the live pointer. This is standard linear-history behavior and is deterministic by construction of the commit funnel.
- **Caret restoration**: Because each snapshot carries the editing context recorded when it became current, restoring it on undo/redo also restores the natural focus and caret/selection position for that state. If the snapshot's `focusedBlockId` is absent from the restored document (e.g., undoing a block insertion), focus moves to the nearest preceding block; if none exists, focus is cleared. Pending typing marks are intentionally **not** carried across undo/redo: after an undo/redo the caret lands at the restored position with the toolbar typing marks cleared.
- **Bound the stack**: History is capped (default 100 entries) by dropping the oldest snapshots so memory stays bounded for long sessions; the cap is a named constant and never causes a crash or truncated current state.
- **No persistence**: Room autosave continues to persist only the currently visible document. Undoing to an earlier state and then navigating away/autosaving therefore persists the undone state, matching the expected editing mental model. History lives in the ViewModel, so it survives configuration change and is lost only when the editor (and its ViewModel) is destroyed.
- **Keyboard shortcuts**: Hardware-keyboard combos are intercepted at the editor screen level (key event pre-processing on the focused editor content) and map to the same `undo()`/`redo()` ViewModel actions as the toolbar buttons. Combos are consumed only when an action is actually available (editable note with history); otherwise they are not consumed and do nothing.
- **Toolbar presentation**: The existing `EditorBarButton` already supports an `enabled` flag; the two controls gain `canUndo`/`canRedo`-driven enabled state, 38% content alpha when disabled (per design system), stable test tags, and reuse of existing localized content descriptions.

### External APIs / Services

- None. Undo/redo operates entirely on in-memory state already loaded from the local Room note.

### Platform & Compatibility Constraints

- **Min SDK**: 24 (project default).
- **Permissions required**: None.
- **Other constraints**: Hardware-keyboard shortcuts depend only on standard `KeyEvent` handling available on API 24+. History is platform-agnostic JVM state; the feature is not platform-bound (no device service, permission, or hardware capability is used). The toolbar-visible-above-IME editor state follows the user-approved IME exception recorded for the formatting toolbar on 2026-09-03.

## Functional Requirements

- **FR-001**: When an editable note is opened in the Note Editor, the system MUST create an in-memory undo history whose baseline is the document state exactly as loaded (or the empty editable document for a brand-new note); when the note is read-only, the system MUST NOT create or expose an undo history.
- **FR-002**: The system MUST record every document edit made while the note is open as part of the undo history, including typed body text, Enter block splits, block insertions/deletions/conversions, checkbox and toggle flips, inline formatting marks over a selection, emoji insertion, note-link insert/remove/replace, formula insert/update/remove, table row/column operations and cell text, chart title/data/options edits, code language/code text edits, mermaid title/code edits, image caption edits, and voice-block insertion. Title edits and non-document operations MUST NOT be recorded.
- **FR-003**: Tapping Undo while the document differs from the baseline MUST restore the previous document state (formatting, marks, links, formulas, block identities, and content included) and move the history pointer back one step; tapping Undo at the baseline MUST make no change. Tapping Redo after an undo MUST re-apply the next forward state; Redo MUST be unavailable until at least one undo has occurred.
- **FR-004**: When the user makes any new document edit after undoing, the system MUST discard the redo tail so Redo becomes unavailable; the new edit continues from the current history position.
- **FR-005**: The system MUST coalesce continuous typing in the same block/cell/field into a single undo step when commits arrive within the coalescing window and no discrete action intervenes; a discrete action (Enter, mark application, emoji/link/formula insertion, structural operation, or focus moving to a different text target) or a longer pause MUST start a new undo step.
- **FR-006**: The system MUST expose `canUndo` and `canRedo` derived from the history pointer; the toolbar Undo/Redo controls MUST be enabled exactly when the corresponding action is available and MUST render disabled (38% content alpha, disabled semantics) otherwise. On read-only notes the controls MUST NOT be rendered at all.
- **FR-007**: On an editable note, Ctrl+Z MUST trigger Undo, Ctrl+Shift+Z and Ctrl+Y MUST trigger Redo, regardless of which field currently has focus; on read-only notes or when the matching action is unavailable the combos MUST be ignored.
- **FR-008**: After Undo or Redo, the system MUST focus the block/cell where the restored change occurred and place the caret/selection at the position recorded for that state; if that block no longer exists after an undo, focus MUST fall back to the nearest preceding block, or clear when the document has no blocks. Pending typing marks MUST be cleared after an undo/redo.
- **FR-009**: Undo/redo history MUST be bounded (maximum 100 steps, oldest entries dropped first) and MUST be held only in memory for the lifetime of the open editor; leaving the editor, closing the note, or process death MUST clear it, and the autosave path MUST keep persisting only the currently visible document.
- **FR-010**: Undo/redo MUST remain available while editing in any body surface whose text input keeps the bottom toolbar reachable (paragraphs, table cells, chart data cells, code field, mermaid code), and MUST behave consistently when an action is committed from an overlay (formula sheet, link picker, table/chart handle sheets, basic-blocks panel, emoji picker) by recording that committed change as the next undo step.

## Acceptance Criteria

- **AC-001**: Given an editable note whose document differs from its loaded baseline, when Undo is tapped, then the document returns exactly to the previous state including marks, links, formulas, block identities, and content, the history pointer moves back, and the caret lands at the restored edit location.
- **AC-002**: Given an editable note with no document changes since opening, when Undo or Redo is tapped (or Ctrl+Z is pressed), then no document change occurs and the controls remain disabled.
- **AC-003**: Given a document history of at least two steps with the pointer not at the baseline, when Redo is tapped, then the next forward state is re-applied; when the pointer is at the current head, Redo makes no change.
- **AC-004**: Given one or more undo operations, when the user makes a new document edit, then the redo tail is discarded, Redo becomes disabled, and the new edit is the next undo step.
- **AC-005**: Given continuous typing without a pause longer than the coalescing window and with no discrete action, when the user taps Undo once, then the entire typing run is removed in one step; given the same run with a longer pause or an intervening discrete action, consecutive Undo taps remove the run in the matching number of steps.
- **AC-006**: Given an editable note with history available, then the Undo control is enabled; at the baseline the Undo control is disabled at 38% content alpha with disabled semantics. The Redo control is enabled only when at least one undo has occurred and no new edit has discarded the redo tail.
- **AC-007**: Given a read-only note open in the editor, then no Undo/Redo controls are rendered in the bottom bar, and Ctrl+Z/Ctrl+Shift+Z/Ctrl+Y produce no document change.
- **AC-008**: Given an editable note with a document history of two or more steps, when Ctrl+Z is pressed, then one undo occurs; when Ctrl+Shift+Z or Ctrl+Y is pressed after an undo, then one redo occurs; with no history, the key combos are ignored.
- **AC-009**: Given an undo that removes the block which previously held focus, when the undo completes, then focus moves to the nearest preceding block (or is cleared when the document has no blocks) and no crash or empty focus state is reachable.
- **AC-010**: Given a sequence of document edits that includes a discrete action (for example an emoji insertion or mark application) followed by more typing, when Undo is pressed repeatedly, then the document unwinds through exactly the same discrete steps that were recorded, restoring the exact pre-edit content at each step.
- **AC-011**: Given an undo/redo step whose caret target is a table cell, chart data cell, code field, or mermaid field, when the step is applied, then that cell/field regains focus with the recorded selection; title-field-only edits never affect `canUndo`/`canRedo`.
- **AC-012**: Given a Voice block, Image block, or note-link/formula annotation whose insertion is undone, then the block or annotation is removed from the document without deleting any underlying audio/image file and without leaving orphaned raw fragments; Redo restores the block or annotation with its original identifier and file path.
- **AC-013**: Given a note with an empty document history (new note), when the user types only in the title field and taps Undo, then no document change occurs and the Undo control stays disabled; title text is never part of the history.
- **AC-014**: Given an undo/redo action followed by autosave or navigation away from the note, then the persisted note content equals the visible document at the moment of save (the undone state when undo was last applied), and reopening the note starts a fresh history whose baseline is that saved content.

## Data And Persistence

- Existing Room note-content storage remains the persistence boundary; there is no schema migration, DTO change, or new table.
- Undo/redo history is a private, in-memory, ViewModel-scoped structure (snapshot list + pointer + coalescing timer state). It survives configuration change because the ViewModel survives it; it is never written to disk and never survives editor destruction or process death.
- The existing 2-second autosave path is unchanged: it persists whatever document is current, including documents reached through undo/redo.
- No shared JSON scenarios or API fixtures are involved.

## Edge Cases

- **Freshly opened existing note**: history baseline equals the persisted document; the first undo restores that exact persisted state.
- **New blank note**: baseline is the empty single-paragraph document; Undo stays disabled until the first body edit.
- **Read-only/protected notes and mid-session access change**: no history is created/exposed; controls hidden or disabled and shortcuts inert; existing mutations already reject read-only access.
- **Undo during active IME composition**: the in-flight composition text is committed into the current typing run before the undo applies, so undo always operates on a consistent committed state.
- **Undo of block insertion/deletion while that block is focused**: the block disappears and focus falls back to the nearest preceding block (AC-009); redo restores the block and its focus context.
- **Undo of voice/image block insertion**: removes the document block only; the audio/image file is left intact and redo restores the block referencing it.
- **Undo of formula/link deletion or insertion**: restores the complete atomic annotation; no raw LaTex source or orphaned link label fragments remain at any pointer position.
- **Typing pauses and coalescing boundaries**: a pause longer than the window or any discrete action splits runs into separate undo steps; consecutive runs in different blocks/cells are always separate steps.
- **History cap**: the oldest entries are dropped beyond 100 steps; the current state and pointer remain valid and undo simply cannot reach beyond the retained window.
- **Table/chart/code/mermaid cell or field focus after undo/redo**: restored when the target still exists, matching AC-011; a cell removed by an undone table operation falls back per AC-009.
- **Configuration change (rotation)**: history and pointer live in the ViewModel and are preserved; no `rememberSaveable` of history is attempted.
- **Deep/edge selection states**: collapsed cursor steps carry the caret position recorded at commit; pending typing marks are cleared after undo/redo so subsequent typing starts from the restored text's own marks.

## Explicit Assumptions

| # | Assumption | Risk if Wrong |
|---|---|---|
| A1 | Title edits are intentionally excluded from undo/redo (user decision 2026-09-06); Ctrl+Z while the title field is focused acts on the shared document history, not the title. | If title undo is later wanted, history scope expands to the title field and snapshot shape changes. |
| A2 | Hardware-keyboard shortcuts are in scope (toolbar-plus-shortcuts was the recommended default; the question was skipped by the user). | If unwanted, shortcuts are dropped without affecting document history semantics; confirmed at this spec gate. |
| A3 | Undo/redo operate on the single open note's document only; no cross-note or multi-tab history. | Not applicable — editor opens one note at a time. |
| A4 | The Note Editor bottom toolbar remains visible above the IME while typing, per the user-approved 2026-09-03 exception; undo/redo stay reachable while the keyboard is open. | If revoked, undo/redo become reachable only when the keyboard is hidden; toolbar/IME layout changes. |
| A5 | Undo/redo do not create analytics events or new logs. | If product analytics are later requested, events are added per analytics rules. |
| A6 | Coalescing window ≈ 1 s, history cap = 100 steps. Both are named constants tuned during implementation review. | Different values only change step grouping/retention feel, not architecture. |

## Open Questions

All questions are answered.

| # | Question | Status | Answer |
|---|---|---|---|
| Q1 | How is this task classified? | ✅ Answered | Enhancement of the existing Note Editor screen with UI state changes; spec.md + design.md + mockups are produced. |
| Q2 | Which edits must Undo/Redo revert? | ✅ Answered | Every document edit: typing, blocks, formatting, tables, charts, code, mermaid, formulas, links, emoji, voice insertion — one shared document history. |
| Q3 | How long does history live? | ✅ Answered | Editor session only; in-memory, cleared on leaving the note/process death; autosave persists only the current state. |
| Q4 | What is one undo step while typing? | ✅ Answered | Coalesced typing runs (short pauses); discrete actions are individual steps. |
| Q5 | Are title edits undoable and where does the caret go? | ✅ Answered | Body-only history: title edits are never undoable; caret/selection restore applies to body edits. |
| Q6 | Where else is undo/redo reachable? | ✅ Answered | Recommended default adopted: toolbar buttons plus Ctrl+Z / Ctrl+Shift+Z / Ctrl+Y on editable notes (see A2). |
| Q7 | What visual delta is expected? | ✅ Answered | Only enabled/disabled presentation of the existing Undo/Redo buttons; position, icons, and copy unchanged. |

## Screen States

| State | Requirement | Acceptance Criteria |
|---|---|---|
| Loading | Existing editor loading behavior; no undo/redo interaction until the note document is loaded. | AC-002 |
| Editable content — history available | Default bottom bar shows enabled Undo (and Redo after an undo); caret restoration active. | AC-001, AC-003–AC-005, AC-008–AC-012 |
| Editable content — no history | Undo/Redo rendered disabled (38% alpha) at the loaded/new-note baseline. | AC-002, AC-006, AC-013 |
| Keyboard (IME) visible | Bottom toolbar (with Undo/Redo) remains visible above the keyboard per the approved IME exception; buttons stay interactive while typing. | AC-005, AC-011 |
| Read-only | Undo/Redo controls not rendered; shortcuts inert. | AC-007 |

## Navigation

- **Entry**: Note Editor already open on an editable note; no new route or back-stack entry.
- **Back/cancel**: Android system back and the existing top-bar back action remain navigation-only; they never trigger undo.
- **Success**: Undo/redo apply in place; the user remains in the editor with the restored document and caret.
- **Error recovery**: In-memory history cannot fail on load or apply; if an undo target block is gone, focus falls back per AC-009. Navigation away persists the current visible document and clears the session history.

## Traceability

| Requirement | Design Section | Acceptance Criteria |
|---|---|---|
| FR-001, FR-002 | Screen 1 — Note Editor (Undo/Redo toolbar) — scope and history contract | AC-001, AC-002, AC-010, AC-013 |
| FR-003, FR-004, FR-005 | Screen 1 — history semantics and step granularity | AC-003–AC-005, AC-010 |
| FR-006 | Screen 1 — component inventory and visual states | AC-006, AC-007 |
| FR-007 | Screen 1 — interaction rules | AC-008 |
| FR-008 | Screen 1 — interaction rules (caret restoration) | AC-001, AC-009, AC-011 |
| FR-009 | Screen 1 — history lifetime and memory bound | AC-014 |
| FR-010 | Screen 1 — reachability across editor surfaces and overlays | AC-011, AC-012 |

## Verification Expectations

- **Unit**: Pure history-manager semantics (baseline seeding, undo/redo pointer movement, redo-tail truncation on new edit, coalescing window boundaries, cap eviction dropping oldest, disabled-state derivation); commit-funnel recording across document mutation kinds; caret/context snapshot capture and fallback-focus resolution; ViewModel transitions for undo/redo, title-exclusion, read-only guards, and pending-mark clearing.
- **Integration**: Autosave/back-navigation flows persist the visible (possibly undone) document; history is not written anywhere; note reload starts a fresh baseline. No API or shared-scenario involvement.
- **Instrumented UI**: Editable-note undo/redo via toolbar taps and hardware-keyboard combos; disabled rendering at baseline and after redo-tail truncation; read-only note with no controls; caret landing assertions; undo across typed text, marks, emoji/link/formula insertion, block insert/delete, and table cell edits through the production editor entry point.
- **Manual/visual**: Capture production visual evidence of Undo enabled + Redo disabled after a typed edit, both controls disabled at baseline, Redo enabled after undo, and the keyboard-visible editor with the bottom toolbar (including Undo/Redo) above the IME.

## No Open Questions Gate

- [x] All requirements are specific and testable.
- [x] All non-goals are explicit.
- [x] No unresolved assumptions remain.
- [x] All visual states are defined in `design.md`.
- [x] All navigation outcomes are defined.
