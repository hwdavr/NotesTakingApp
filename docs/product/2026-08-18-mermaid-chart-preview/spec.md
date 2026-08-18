# Feature Spec — Mermaid Chart & Preview in Note Editor

**Date**: 2026-08-18  
**Status**: Approved  
**Related design**: `design.md`  

---

## Objective

Add rich Mermaid chart diagramming and interactive visual preview capabilities directly inside the Note Editor. Users can insert a Mermaid Diagram block from the Basic Blocks catalog, write Mermaid code in a focused monospace editor with instant syntax feedback, view high-quality rendered SVG diagrams inline with pan/zoom support, inspect diagrams in a full-screen interactive canvas, and seamlessly export them to Markdown and PDF.

## User Goal

As a note creator, technical writer, or designer, I want to create and view Mermaid diagrams (flowcharts, sequence diagrams, architecture flows, class diagrams, etc.) directly within my notes so that I can document structured visual models alongside my rich text without relying on external diagramming tools or internet services.

## Scope

### In Scope

- **Block Model & Document Schema**:
  - Introduce `EditorBlock.MermaidBlock(id: String, code: String, title: String)` into `NoteDocument`.
  - Full backward-compatible JSON serialization (`type: "mermaid"`) preserving existing auto-save, reload, and document ordering.
  - Default starter flowchart template on block creation:
    ```mermaid
    graph TD
        A[Start] --> B{Decision}
        B -->|Yes| C[Result 1]
        B -->|No| D[Result 2]
    ```
- **Basic Blocks Catalog Insertion**:
  - Add a dedicated **Mermaid Diagram** tile to the Basic Blocks panel.
  - Focus-aware insertion: inserts immediately after the currently focused block or appends to the end if no block is focused.
  - Auto-collapses the Basic Blocks panel upon insertion and auto-saves the document.
- **Card UI & Editor Modes**:
  - Render Mermaid blocks as elevated Material 3 cards (`#FFFFFF` background in light theme, `#E7E3F6` border stroke, 12dp rounded corners).
  - Default view: **Rendered Diagram Preview** with diagram title, "Edit Code" toggle action, and Fullscreen icon.
  - Toggled view: **Code Editor** with "View Chart" toggle action, quick diagram template chips (Flowchart, Sequence, Class, State), a monospace code editor, and live syntax validation status.
- **100% On-Device Offline Rendering Engine**:
  - Bundled local `mermaid.min.js` loaded inside a local offline `WebView` (zero internet permission, zero external network requests).
  - Generates interactive SVG output locally on-device.
  - Theme-aware rendering: automatically applies Light Theme (`#191627` text, `#7C6CF2` primary accents, `#FFFFFF` surfaces) or Dark Theme matching `AppColors`.
- **Interactive Canvas & Fullscreen Viewer**:
  - Inline pinch-to-zoom and pan within the note editor diagram preview card.
  - Fullscreen Diagram Viewer dialog/screen with pan, zoom controls (Zoom In, Zoom Out, Reset 100%, Fit to Screen), code copy, and SVG sharing/export.
- **Syntax Error Handling**:
  - Non-crashing inline error card when Mermaid syntax is invalid, showing the line error hint while keeping the code editor fully interactive.
- **Export & Read-Only Handling**:
  - Markdown export: formats Mermaid blocks as ```` ```mermaid\n<code>\n``` ````.
  - PDF export: formats Mermaid blocks with diagram title and code/diagram structure.
  - Read-only mode: renders diagrams in preview mode with zoom/fullscreen enabled; disables/hides code editing actions.

### Out Of Scope

- Online cloud-based rendering or external API dependencies (strictly 100% on-device local rendering).
- Live collaborative multi-user editing of diagrams.
- Real-time WYSIWYG graphical drag-and-drop node positioning editor (Mermaid code-driven syntax is the source of truth).
- Non-Mermaid diagramming engines (e.g., PlantUML, Graphviz).

---

## Technical Spec

### Libraries & Dependencies

| Library / SDK | Version | Purpose |
|---|---|---|
| Jetpack Compose Material 3 | Existing project version | UI card components, toggle buttons, toolbars, and fullscreen dialog. |
| Android `WebView` (Local Asset) | Android SDK (minSdk 24) | On-device sandbox for executing bundled `mermaid.min.js` to produce SVGs without network access. |
| Bundled `mermaid.min.js` | 10.9.1 / 11.x (Local Asset) | Offline JavaScript diagram rendering engine. |
| Room & Kotlin Serialization / org.json | Existing project version | Document block persistence and JSON schema mappings. |
| Kotlin Coroutines / StateFlow | Existing project version | Reactive state handling between code input and SVG generation. |

### Key Technical Decisions

- **Local WebView SVG Renderer**: Mermaid is executed within an isolated, local `WebView` loaded with a local HTML asset (`file:///android_asset/mermaid/index.html`) containing bundled `mermaid.min.js`. The WebView disables network access, executing only local JavascriptInterface or `evaluateJavascript` calls. This ensures 100% offline privacy, zero latency, and zero external network calls.
- **Unidirectional Diagram State**: The Mermaid card maintains a rendering state pipeline: `Idle` → `Rendering` → `Rendered(svgContent)` or `SyntaxError(message)`. Debounced code changes prevent unnecessary re-renders while typing.
- **Card-Level Mode Switching**: The block is rendered by default in `Preview` mode so notes remain clean and visual. Tapping "Edit Code" switches to `Code` mode with monospace typography, quick template chips, and live validation.

### External APIs / Services

- None. 100% on-device local execution.

### Platform & Compatibility Constraints

- **Min SDK**: API 24 (Android 7.0) compatible.
- **Permissions required**: None.
- **Network**: Fully offline; no internet connection required.

---

## Functional Requirements

- **FR-001**: The system MUST add `EditorBlock.MermaidBlock` to `NoteDocument` supporting backward-compatible JSON serialization and deserialization (`type: "mermaid"`).
- **FR-002**: The Basic Blocks panel MUST include a "Mermaid Diagram" tile that inserts an empty starter Mermaid block after the focused block (or at the end of the note).
- **FR-003**: The Mermaid block MUST default to Diagram Preview mode displaying the rendered SVG chart.
- **FR-004**: The Mermaid block MUST provide an "Edit Code" button in the card header that toggles between Code Editor mode and Diagram Preview mode.
- **FR-005**: In Code Editor mode, the user MUST be able to edit the Mermaid DSL code with monospace font and select starter template chips (Flowchart, Sequence, Class, State).
- **FR-006**: The system MUST render Mermaid diagrams locally and offline using bundled `mermaid.min.js` without network access.
- **FR-007**: The system MUST adapt diagram styles and color tokens to match the active app theme (Light / Dark mode).
- **FR-008**: The system MUST display an inline non-crashing syntax error indicator when the Mermaid code has errors, without disrupting editor responsiveness.
- **FR-009**: The diagram preview MUST support inline touch gestures (pinch-to-zoom and pan) and an expand action that opens the Fullscreen Diagram Viewer.
- **FR-010**: The Fullscreen Diagram Viewer MUST provide pan/zoom controls (Zoom In, Zoom Out, Reset, Fit to Screen), code copy, and SVG sharing/export.
- **FR-011**: In read-only notes, the Mermaid block MUST display in Diagram Preview mode only, hiding code editing controls while preserving fullscreen inspection.
- **FR-012**: Note export MUST format Mermaid blocks as ```` ```mermaid\n<code>\n``` ```` in Markdown (.md) and as structured blocks in PDF/Text exports.

---

## Acceptance Criteria

- **AC-001**: Given an editable note, when the user selects "Mermaid Diagram" from the Basic Blocks panel, then a new Mermaid block is inserted with the default starter flowchart and the panel collapses.
- **AC-002**: Given a note with a Mermaid block in Preview mode, when the user taps "Edit Code", then the block smoothly transitions to Code Editor mode displaying the editable code and template chips.
- **AC-003**: Given a note with a Mermaid block in Code Editor mode, when the user modifies the Mermaid code, then the changes are preserved in the note document and auto-saved.
- **AC-004**: Given valid Mermaid code, when toggled to Preview mode or rendered inline, then the SVG diagram is displayed within the card matching the app's theme colors.
- **AC-005**: Given invalid Mermaid syntax, when rendered, then an inline error message is displayed inside the card and the app remains stable without crashing.
- **AC-006**: Given a rendered diagram in the editor, when the user pinches or drags on the diagram, then the diagram zooms and pans within the card viewport bounds.
- **AC-007**: Given a Mermaid block, when the user taps the Fullscreen action, then the Fullscreen Diagram Viewer opens with zoom in/out controls and full canvas navigation.
- **AC-008**: Given a note containing a Mermaid block, when exported to Markdown, then the output contains the exact ```` ```mermaid ```` code block.
- **AC-009**: Given a read-only note, when viewed, then the Mermaid diagram is rendered in preview mode and the "Edit Code" button is hidden.

---

## Data And Persistence

- Persisted inside `Note.content` as part of `NoteDocument` JSON:
  ```json
  {
    "id": "b_mermaid_12345",
    "type": "mermaid",
    "title": "Architecture Flow",
    "code": "graph TD\n    A[Start] --> B[End]"
  }
  ```
- Deserialization gracefully falls back to plain code text if parsing encountered unexpected data.

---

## Edge Cases

- **Empty Code**: When code is empty or whitespace, render an empty-state prompt in the preview card encouraging the user to "Tap Edit Code to create a diagram".
- **Syntax Errors**: Mermaid parser error events are caught and surfaced via an inline error banner with error line details; the user can fix the code immediately.
- **Device Rotation & Recomposition**: Zoom/pan state and mode selection survive configuration changes and screen rotations.
- **Theme Switching**: Toggling system light/dark mode re-renders the Mermaid SVG using updated theme tokens without data loss.
- **Very Large Diagrams**: Handled via virtualized SVG scaling and bounding boxes in both card preview and fullscreen viewer.

---

## Explicit Assumptions

| # | Assumption | Risk if Wrong |
|---|---|---|
| A1 | Android System WebView is available on all supported API 24+ devices to execute local bundled JS. | Fallback text/code rendering if WebView is unavailable or disabled by user. |
| A2 | Bundled `mermaid.min.js` runs completely offline without remote asset fetching. | Zero network risk; all fonts and symbols are inline SVG. |

---

## Open Questions

All questions have been clarified with the user.

| # | Question | Status | Answer |
|---|---|---|---|
| Q1 | How should users insert a new Mermaid chart block? | ✅ Answered | Insert via the Basic Blocks panel under a Diagram / Mermaid tile. |
| Q2 | How should the Mermaid chart block be displayed and interacted with in the editor? | ✅ Answered | Toggleable Card: Default to rendered visual diagram preview with an 'Edit Code' / 'View Chart' toggle button in the header. |
| Q3 | Should the rendered Mermaid diagram support zoom, pan, and full-screen preview? | ✅ Answered | Yes, support pinch-to-zoom/pan within the card and tap to open a full-screen interactive diagram viewer with export/share. |
| Q4 | How should diagram theming and styling behave? | ✅ Answered | Automatically adapt Mermaid theme to match app theme (Light / Dark mode tokens). |

---

## Screen States

| State | Requirement | Acceptance Criteria |
|---|---|---|
| Loading / Rendering | Show subtle progress placeholder while SVG is generated | AC-004 |
| Empty Code | Show empty placeholder card with "Tap to add diagram" | AC-001 |
| Content Preview | Show interactive rendered SVG diagram with title and header actions | AC-004, AC-006 |
| Code Editor | Show monospace code text field with template chips and validation | AC-002, AC-003 |
| Syntax Error | Show inline error card with details and link to edit code | AC-005 |
| Fullscreen Viewer | Full canvas diagram view with zoom controls and export actions | AC-007 |

---

## Navigation

- **Entry**: Tapping "Mermaid Diagram" tile in the Basic Blocks panel within `NoteEditorScreen`.
- **Fullscreen Viewer Entry**: Tapping the expand icon in the Mermaid card header.
- **Fullscreen Viewer Exit**: Tapping Back / Close button returns to `NoteEditorScreen` with scroll position preserved.

---

## Traceability

| Requirement | Design Section | Acceptance Criteria |
|---|---|---|
| FR-001 | Note Document Schema & Block Types | AC-001, AC-003 |
| FR-002 | Basic Blocks Panel Tile | AC-001 |
| FR-003 | Mermaid Diagram Card (Preview Mode) | AC-004, AC-006 |
| FR-004 | Mermaid Card Header Controls | AC-002, AC-004 |
| FR-005 | Mermaid Diagram Card (Code Editor Mode) | AC-002, AC-003 |
| FR-006 | Offline WebView Mermaid Engine | AC-004 |
| FR-007 | Theme Synchronization | AC-004 |
| FR-008 | Syntax Error Handling | AC-005 |
| FR-009 | Pan & Pinch-to-Zoom Interaction | AC-006 |
| FR-010 | Fullscreen Diagram Viewer Screen | AC-007 |
| FR-011 | Read-Only Note Rendering | AC-009 |
| FR-012 | Markdown & PDF Export | AC-008 |

---

## Verification Expectations

- **Unit**:
  - `NoteDocumentTest`: serialization and deserialization of `EditorBlock.MermaidBlock`.
  - `NoteExporterTest`: Markdown export of Mermaid code blocks and PDF rendering.
  - `MermaidBlockMapperTest`: mapping between domain note document and UI presentation models.
- **Integration**:
  - `NoteEditorViewModelIntegrationTest`: inserting, editing code, updating title, and auto-saving Mermaid blocks.
- **Instrumented UI**:
  - `MermaidBlockCardTest`: verifying card rendering, mode toggle, template insertion, error display, and accessibility tags.
  - `FullscreenDiagramViewerTest`: verifying fullscreen canvas controls, zoom in/out, and back navigation.
- **Manual / Visual**:
  - Verify Light and Dark theme SVG rendering, pinch zoom fluidity, and fullscreen viewer export.

---

## No Open Questions Gate

- [x] All requirements are specific and testable.
- [x] All non-goals are explicit.
- [x] No unresolved assumptions remain.
- [x] All visual states are defined in `design.md`.
- [x] All navigation outcomes are defined.
