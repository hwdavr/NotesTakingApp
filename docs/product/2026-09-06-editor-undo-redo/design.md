# Feature Design — Note Editor Undo & Redo

**Date**: 2026-09-06
**Status**: Revised draft — mockups regenerated for design review
**Source request**: Review and redo the Note Editor Undo/Redo design and regenerate the mockups.
**Related spec**: `spec.md`
**Project design system**: `docs/product/design_system.md`
**Approved design-system exceptions**: Reuse of the user-approved IME exception recorded for the Note Editor formatting toolbar on 2026-09-03 (formatting-toolbar design): while the editor IME is visible, the editor's bottom toolbar remains visible **above** the keyboard and must never sit behind it or overlap the focused input. The Undo/Redo controls live in that same editor bottom toolbar, so this exception governs their keyboard-visible state and is re-confirmed by the user at this gate. No other exceptions; no new colors, shapes, or components are introduced.

## Conditional Keyboard-Visible Mockup Contract

The Note Editor screen contains text input (title field, body text blocks, and inline table/chart/code/mermaid fields) and a bottom toolbar that hosts the Undo/Redo controls. While the editor IME is visible and the user types, the 56dp bottom toolbar is raised above the keyboard by the editor's `imePadding()` so it is **never hidden behind the keyboard** and never overlaps the focused field; it is not dismissed — per the approved IME exception the toolbar (including Undo/Redo) stays reachable while typing so an editor can undo or redo mid-composition. See `design/mockup_note_editor_undo_redo.png` (base state) and `design/mockup_note_editor_undo_redo_keyboard.png` (keyboard-visible state).

## Screens Covered

| # | Screen / Surface | Status |
|---|---|---|
| 1 | Note Editor — default bottom toolbar with Undo/Redo | Updated |

## Review Findings And Revision

The first mockup pass was reviewed against `NoteEditorScreen.kt` and the project design system. The revised reference makes these corrections:

- The editor top bar now uses the real back, share, and more actions; it does not invent a centered title or add action.
- The document title remains in the body as the existing 24sp headline field, and the folder context stays a compact 8dp-radius surface using the existing border token.
- Enabled Undo/Redo controls use the existing flat toolbar treatment and `textPrimary` tint. They do not receive a filled selected background because Undo/Redo are availability actions, not toggle selections.
- Disabled controls use `textSecondary` at 38% alpha. The redo-enabled state is shown separately so the two availability states are reviewable.
- The keyboard-visible reference uses an Android-style IME treatment and keeps the 56dp editor rail above it, matching the approved IME exception.

No new colors, shapes, toolbar positions, icons, or user-visible copy are introduced.

## Screen 1 — Note Editor (Undo/Redo in the default bottom toolbar)

### Purpose

Turn the existing inert Undo and Redo icons in the Note Editor's default bottom toolbar into working, state-aware document history controls so editors can reverse and replay body/document changes during an editing session without leaving the editor or losing their place.

### UX Principles

- Preserve the existing editor surface: dense, horizontally scrollable, flat white 56dp bottom-toolbar family and Material outlined-icon language — only behavior and enablement change.
- Make availability explicit: controls appear disabled at the documented 38% content alpha whenever the matching history action cannot run, so the state is obvious without tapping.
- Never lose the user's place: undo/redo return the caret to the block and offset where the reverted or re-applied change happened.

### Entry And Exit

- **Entry points**: The Note Editor already shows Undo/Redo in the editable default bottom toolbar between the Emoji control and the camera control; no new entry point.
- **Primary success exit**: Undo/redo apply in place and the user keeps editing; no navigation occurs.
- **Cancel/back behavior**: Android system back and the top-bar back action remain navigation-only and never trigger undo.
- **Failure exit or recovery**: In-memory history cannot fail to apply; when an undo removes the focused block, focus falls back to the nearest preceding block (or clears when none exists).

### Information Architecture

1. **Status & top bar**: system insets; the existing 56dp top action bar on `surface` with back, share, and more actions (unchanged).
2. **Folder context & title**: existing compact breadcrumb folder selector and editable 24sp title field (unchanged; the title is **not** part of undo history).
3. **Document body**: vertically scrollable rich-text blocks (paragraphs, headings, lists, to-do, tables, charts, code, mermaid, formulas, links, images, voice) on `background` (unchanged).
4. **Default bottom toolbar**: unchanged 56dp flat rail on `surface` above the navigation/IME insets holding, in order: Basic-blocks add, Format toggle (F), checkbox, link, mention, emoji, **Undo**, **Redo**, camera, image, mic, table, and hide-keyboard actions. Only the Undo and Redo items gain functional, state-driven behavior in this feature; the rail remains horizontally scrollable on narrow widths.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|---|---|---|---|
| Default bottom toolbar | Existing editor action rail containing Undo/Redo | editable; read-only variant without Undo/Redo; IME hidden/visible | `editor_default_bottom_bar` (read-only: `editor_read_only_bottom_bar`) |
| Undo action | Revert the most recent document change one step | enabled (history below pointer), disabled at 38% content alpha (baseline), IME-visible, read-only not rendered | `editor_undo_action` |
| Redo action | Re-apply a document change that was undone | enabled (redo tail exists), disabled at 38% content alpha, IME-visible, read-only not rendered | `editor_redo_action` |
| Undo/Redo icon | AutoMirrored Outlined Undo / Redo glyphs | enabled tint `textPrimary`; disabled tint `textSecondary` at 38% alpha | (icon inherits the action button tag) |

### Reference Anchor Contract

| Reference relationship | Visual bounds node(s) | Required runtime measurement |
|---|---|---|
| Default bottom toolbar stays a 56dp flat rail at the editor bottom when the IME is hidden | `editor_default_bottom_bar`, editor content anchor | Toolbar height = 56dp ± 2dp and it abuts the editor divider/content. |
| Undo and Redo buttons keep the existing 48dp-high, 40dp-wide editor rail target centered in the 56dp rail | `editor_undo_action`, `editor_redo_action` | Button height = 48dp ± 2dp, width = 40dp ± 2dp, and both are vertically centered in the toolbar. |
| IME-visible editor keeps the rail (with Undo/Redo) above the keyboard | `editor_default_bottom_bar`, focused editor input, IME region | Toolbar bottom meets the top of the visible IME region; toolbar never overlaps keyboard or focused input. |

### Visual States

| State | User Sees | User Can Do |
|---|---|---|
| Loading | Existing editor loading behavior; toolbar not interactive until the document is loaded. | Wait; no undo/redo interaction. |
| Content — history available | Undo enabled (`textPrimary`) after an edit; after ≥1 undo, Redo enabled (`textPrimary`) while the redo tail exists. Neither action has a selected fill. | Undo/redo document changes; caret returns to the restored location. |
| Content — baseline / no redo | Undo disabled at 38% alpha on the loaded state; Redo disabled when nothing was undone or a new edit cleared the redo tail. | Continue editing; undo/redo become available as soon as history exists. |
| Keyboard visible | Bottom toolbar remains visible above the IME with Undo/Redo interactive (approved exception). | Keep typing and undo/redo mid-composition; committed text unwinds correctly. |
| Read-only | Undo/Redo are not rendered in the read-only bottom bar. | Inspect only; shortcuts inert. |

### Interaction Rules

- **Primary action**: Tapping Undo reverts the most recent document step and restores the caret to that change's location; tapping Redo replays the next forward step. Both act on the single shared document history regardless of which body surface (paragraph, table cell, chart cell, code/mermaid field) holds focus.
- **Secondary actions**: Hardware-keyboard Ctrl+Z (undo), Ctrl+Shift+Z / Ctrl+Y (redo) work anywhere on the editable editor screen; combos are ignored on read-only notes and when the matching action is unavailable.
- **Validation**: Controls are disabled exactly when the action cannot run; no error messages are possible (in-memory deterministic history).
- **Destructive actions**: None — undo/redo only reverse/replay the user's own session edits; no confirmation is required or shown.
- **Gestures**: None; button and keyboard triggers only (system back stays navigation).
- **Disabled affordance**: disabled icon content uses `textSecondary` at 38% alpha plus disabled semantics (per design system), combining color with semantics — not color alone.
- **Availability state**: enabled Undo/Redo use `textPrimary` with no background container. A new edit after undo clears the redo tail; it does not leave a stale active-looking control.

### Copy Requirements

| Element | Copy |
|---|---|
| Undo content description | Undo (existing `editor_undo_description`) |
| Redo content description | Redo (existing `editor_redo_description`) |

No new user-visible copy is introduced by this feature.

### Accessibility

- Undo/Redo keep localized content descriptions and expose standard button semantics with an explicit disabled state when unavailable.
- Active vs disabled is never communicated by color alone: disabled controls also carry disabled semantics.
- Touch targets remain 48dp tall (bar) and ≥40dp wide, matching `EditorBarButton`; TalkBack announces each icon per its content description.
- No dynamic-text clipping: icons do not scale with font size; the horizontally scrollable rail keeps all actions reachable at large font scales.

### Responsive And Configuration Behavior

- Portrait phones/tablets: the toolbar rail scrolls horizontally to keep Undo/Redo reachable on narrow screens; safe-drawing and navigation insets preserved.
- Landscape: the rail remains at the bottom edge above insets with horizontal scrolling.
- Configuration change: history lives in the `NoteEditorViewModel` and survives rotation; buttons reflect the same `canUndo`/`canRedo` state after recreation.
- Dark theme: tokens (`textPrimary`, `textSecondary`, `surface`) already resolve through `LocalAppColors`; disabled state uses the same 38% alpha on the dark-theme secondary token. Light theme remains the mockup/visual baseline.

### Design Assets

- **Mockup image**: `design/mockup_note_editor_undo_redo.png` — Note Editor after a typed edit: Undo enabled (`textPrimary`) and Redo disabled (38% alpha), with the actual editor top bar and flat toolbar treatment.
- **Redo-enabled mockup**: `design/mockup_note_editor_undo_redo_redo_enabled.png` — Note Editor after Undo: Redo enabled (`textPrimary`) and Undo disabled (38% alpha), with no selected-fill treatment.
- **Keyboard-visible mockup**: `design/mockup_note_editor_undo_redo_keyboard.png` — the editor while typing with an Android-style IME visible: the 56dp bottom toolbar (Undo/Redo included) remains visible above the keyboard and never overlaps the focused input (approved IME exception).
- Additional reference screenshots/assets: None (code-derived baseline from `app/src/main/java/com/example/notesapp/ui/editor/screen/NoteEditorScreen.kt`).

### Out Of Scope For This Design

- Title-field undo, history persistence UI, changed toolbar ordering or iconography, new toolbar surfaces, bottom sheets, and any redesign of the editor shell.
