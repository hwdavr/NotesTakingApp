# Sprint Contract — Note Editor Mermaid Chart & Preview

## 🏃 Sprint Overview

*   **Sprint:** P06-01
*   **Feature:** Mermaid Chart & Interactive Preview in Note Editor
*   **Duration:** 1 sprint

---

## 🎯 Scope

### In Scope

*   [ ] Add `EditorBlock.MermaidBlock(id, code, title)` to `NoteDocument` with JSON persistence (`type: "mermaid"`) and auto-save.
*   [ ] Add "Mermaid Diagram" tile to the Basic Blocks panel inserting a default starter flowchart.
*   [ ] Local offline Mermaid rendering engine executing bundled `mermaid.min.js` in a local `WebView` sandbox (100% on-device, zero network requests).
*   [ ] Automatic theme synchronization with `AppColors` tokens (Light / Dark mode).
*   [ ] Mermaid Diagram Card component with "Edit Code" / "View Chart" header toggle, title editing, and syntax error alerts.
*   [ ] Quick template chips ("Flowchart", "Sequence", "Class", "State") for instant boilerplate insertion.
*   [ ] Inline two-finger pinch-to-zoom and one-finger pan gestures inside the diagram card.
*   [ ] Fullscreen Diagram Viewer screen/dialog with edge-to-edge canvas, zoom controls (+, -, 100%, Fit to Screen), code copy, and SVG export.
*   [ ] Markdown (` ```mermaid `) and PDF export mappings.
*   [ ] Read-only note behavior (renders diagram preview, hides edit controls).

### Out of Scope

*   *   Online/cloud-based diagram rendering services.
*   *   Real-time multi-user collaborative diagram editing.
*   *   Drag-and-drop WYSIWYG graphical diagram builder.
*   *   Non-Mermaid diagram engines (PlantUML, Graphviz).

---

## Platform Capability & Environment Contract

Link to matrix artifact: [`platform-capability-matrix.md`](platform-capability-matrix.md).

This feature uses the local Android System `WebView` asset sandbox to execute offline JavaScript. Minimum API: 24. Target API: 34. The failure policy is `fail_loudly`: missing emulators, test runners, or visual capture tools fail non-zero and cannot be marked passing.

---

## Spec Coverage Matrix

| Source requirement | Requirement summary | Primary user story | Primary acceptance test | Handling |
|---|---|---|---|---|
| FR-001 | Add `EditorBlock.MermaidBlock` to `NoteDocument` with JSON serialization (`type: "mermaid"`) | US-1 | TC-US-1-01 | In scope |
| FR-002 | Basic Blocks panel "Mermaid Diagram" tile inserts starter block | US-1 | TC-US-1-02 | In scope |
| FR-003 | Default to Diagram Preview mode displaying rendered SVG chart | US-3 | TC-US-3-01 | In scope |
| FR-004 | Card header "Edit Code" / "View Chart" mode toggle button | US-3 | TC-US-3-02 | In scope |
| FR-005 | Monospace code editor with starter template chips | US-3 | TC-US-3-03 | In scope |
| FR-006 | Local offline Mermaid rendering via bundled JS | US-2 | TC-US-2-01 | In scope |
| FR-007 | Theme-aware diagram styling matching Light/Dark tokens | US-2 | TC-US-2-02 | In scope |
| FR-008 | Non-crashing inline syntax error indicator | US-2 | TC-US-2-03 | In scope |
| FR-009 | Inline pinch-to-zoom, pan gestures, and fullscreen expand action | US-3 | TC-US-3-04 | In scope |
| FR-010 | Fullscreen Diagram Viewer with zoom controls, code copy, and SVG export | US-4 | TC-US-4-01 | In scope |
| FR-011 | Read-only notes display diagram in preview mode and hide edit controls | US-3 | TC-US-3-05 | In scope |
| FR-012 | Markdown (` ```mermaid `) and PDF export | US-1 | TC-US-1-03 | In scope |
| AC-001 | Insert Mermaid block from Basic Blocks panel with default starter template | US-1 | TC-US-1-02 | In scope |
| AC-002 | Tap "Edit Code" transitions card to Code Editor mode | US-3 | TC-US-3-02 | In scope |
| AC-003 | Modifying Mermaid code auto-saves to note document | US-3 | TC-US-3-03 | In scope |
| AC-004 | Render valid Mermaid code as SVG matching app theme | US-2 | TC-US-2-01 | In scope |
| AC-005 | Render inline error banner for invalid syntax without crashing | US-2 | TC-US-2-03 | In scope |
| AC-006 | Inline pinch-to-zoom and pan within diagram card viewport | US-3 | TC-US-3-04 | In scope |
| AC-007 | Tap Fullscreen opens Fullscreen Diagram Viewer with navigation controls | US-4 | TC-US-4-01 | In scope |
| AC-008 | Export note with Mermaid block to Markdown containing ` ```mermaid ` block | US-1 | TC-US-1-03 | In scope |
| AC-009 | Read-only note renders diagram preview with hidden edit controls | US-3 | TC-US-3-05 | In scope |
| Edge case: Empty code | Show empty-state card encouraging user to tap Edit Code | US-3 | TC-US-3-06 | In scope |
| Edge case: Large diagram | Virtualized scaling and bounded touch viewport | US-4 | TC-US-4-02 | In scope |
| NFR: 100% Offline | Zero network calls or permissions required | US-2 | TC-US-2-01 | In scope |
| Design: Design system alignment | Reuse exact M3 tokens from `design_system.md` | US-4 | TC-US-4-VIS-01 | In scope |

---

## User Scenarios & Testing

### US-1: Document Block Model, Persistence & Basic Blocks Panel Insertion (Priority: P1)

Define the `EditorBlock.MermaidBlock` data structure in `NoteDocument.kt`, support backward-compatible JSON serialization (`type: "mermaid"`), implement auto-save/reload in the repository, add the "Mermaid Diagram" tile to the Basic Blocks panel, and map Markdown/PDF export.

**Why this priority**: Foundational data model and entry point required before rendering and interactive card components can function.

**Independent Test**: Can be tested independently via document serialization unit tests, exporter tests, and Basic Blocks panel insertion integration tests.

**Acceptance Criterion**:

1. **AC-US-1-01 Given** a `NoteDocument` with a `MermaidBlock`, **When** serialized to JSON and deserialized back, **Then** the block preserves its ID, title, and Mermaid code verbatim.
2. **AC-US-1-02 Given** an editable note in the editor, **When** the user taps the "Mermaid Diagram" tile in the Basic Blocks panel, **Then** a new `MermaidBlock` with starter flowchart code is inserted after the active block and the panel collapses.
3. **AC-US-1-03 Given** a note containing a `MermaidBlock`, **When** exported to Markdown, **Then** the output contains the ```` ```mermaid ```` code block.

**Acceptance Test Cases**:

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-1-01 | AC-US-1-01 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/mapper/NoteDocumentTest.kt#testMermaidBlockSerializationAndDeserialization` | Given `EditorBlock.MermaidBlock` with custom code and title, when converted to JSON and back | Assert `NoteDocument.fromContent(json)` recovers identical block | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.mapper.NoteDocumentTest.testMermaidBlockSerializationAndDeserialization"` |
| TC-US-1-02 | AC-US-1-02 | JVM integration | `app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorViewModelIntegrationTest.kt#testInsertMermaidBlockFromBasicBlocksPanel` | Given active editor note, when `insertBasicBlock(BasicBlockType.MERMAID)` is invoked | Assert document contains new `MermaidBlock` with starter template and auto-save is triggered | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelIntegrationTest.testInsertMermaidBlockFromBasicBlocksPanel"` |
| TC-US-1-03 | AC-US-1-03 | JVM unit | `app/src/test/java/com/example/notesapp/util/NoteExporterTest.kt#testExportMermaidBlockToMarkdown` | Given `NoteDocument` containing a `MermaidBlock`, when exported via `NoteExporter.toMarkdown` | Assert exported string contains ```` ```mermaid\n<code>\n``` ```` | `./gradlew testDebugUnitTest --tests "com.example.notesapp.util.NoteExporterTest.testExportMermaidBlockToMarkdown"` |

---

### US-2: Local Offline Mermaid Rendering Engine & Theme Synchronization (Priority: P2)

Implement the on-device `MermaidRenderer` component backed by a local `WebView` and bundled `mermaid.min.js` asset (`file:///android_asset/mermaid/index.html`). Evaluates Mermaid code offline, injects theme tokens (Light / Dark mode), produces SVG output strings, and catches syntax errors gracefully.

**Why this priority**: Core engine that powers visual diagram rendering and SVG generation for both card and fullscreen views.

**Independent Test**: Can be tested independently via renderer unit tests and offline SVG generation tests with sample diagrams and syntax error fixtures.

**Acceptance Criterion**:

1. **AC-US-2-01 Given** valid Mermaid code and Light theme tokens, **When** rendered by `MermaidRenderer`, **Then** it produces an SVG string without network requests using matching theme colors.
2. **AC-US-2-02 Given** dark theme mode, **When** rendering a diagram, **Then** dark theme styling tokens are passed into the Mermaid configuration.
3. **AC-US-2-03 Given** invalid Mermaid syntax, **When** rendered, **Then** `MermaidRenderer` returns a structured syntax error state with line details rather than crashing.

**Acceptance Test Cases**:

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-2-01 | AC-US-2-01 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/components/MermaidRendererTest.kt#testRenderValidFlowchartProducesSvg` | Given valid flowchart code, when `MermaidRenderer.renderSvg` is called | Assert result is `RenderResult.Success` containing valid SVG payload | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.components.MermaidRendererTest.testRenderValidFlowchartProducesSvg"` |
| TC-US-2-02 | AC-US-2-02 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/components/MermaidRendererTest.kt#testDarkThemeTokenInjection` | Given dark theme is active, when configuring Mermaid JS payload | Assert theme payload contains dark theme background and stroke tokens | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.components.MermaidRendererTest.testDarkThemeTokenInjection"` |
| TC-US-2-03 | AC-US-2-03 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/components/MermaidRendererTest.kt#testInvalidSyntaxReturnsStructuredError` | Given invalid Mermaid syntax `graph ZZ -> invalid`, when evaluated | Assert result is `RenderResult.Error` with non-empty error message | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.components.MermaidRendererTest.testInvalidSyntaxReturnsStructuredError"` |

---

### US-3: Mermaid Diagram Card with Mode Toggle & Quick Template Chips (Priority: P3)

Implement the `MermaidBlockCard` Compose component inside `NoteEditorScreen`. Features an elevated card surface, title editing, "Edit Code" / "View Chart" header toggle button, quick template chips ("Flowchart", "Sequence", "Class", "State"), monospace code editor, inline pinch-to-zoom/pan viewport, and read-only mode protection.

**Why this priority**: Delivers the primary in-editor user interface for creating, editing, and previewing diagrams inside notes.

**Independent Test**: Can be tested independently via Composable tests verifying mode switching, template insertion, zoom gesture handling, and read-only behavior.

**Acceptance Criterion**:

1. **AC-US-3-01 Given** a note with a `MermaidBlock`, **When** displayed in the editor, **Then** it defaults to Diagram Preview mode displaying the rendered SVG diagram.
2. **AC-US-3-02 Given** a `MermaidBlock` in Preview mode, **When** user taps "Edit Code", **Then** the card switches to Code Editor mode displaying the monospace code editor and template chips.
3. **AC-US-3-03 Given** a `MermaidBlock` in Code Editor mode, **When** user selects a template chip (e.g. "Sequence") or types code, **Then** the code updates and persists to the document.
4. **AC-US-3-04 Given** a rendered diagram in the card, **When** the user pinches or drags inside the preview area, **Then** the diagram scales and pans within the card bounds.
5. **AC-US-3-05 Given** a read-only note, **When** viewed, **Then** the diagram renders in preview mode and the "Edit Code" button is hidden.
6. **AC-US-3-06 Given** an empty Mermaid block, **When** rendered in preview mode, **Then** an empty-state prompt encourages the user to tap "Edit Code".

**Acceptance Test Cases**:

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-3-01 | AC-US-3-01 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/components/MermaidBlockCardTest.kt#testMermaidCardDefaultsToPreviewMode` | Given note with Mermaid block, when screen renders | Assert `editor_mermaid_preview_canvas_{id}` is visible and `editor_mermaid_toggle_mode_{id}` shows "Edit Code" | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.components.MermaidBlockCardTest#testMermaidCardDefaultsToPreviewMode` |
| TC-US-3-02 | AC-US-3-02 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/components/MermaidBlockCardTest.kt#testToggleBetweenPreviewAndCodeEditor` | Given card in preview mode, when tapping "Edit Code" | Assert `editor_mermaid_code_editor_{id}` is displayed; tapping "View Chart" returns to preview | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.components.MermaidBlockCardTest#testToggleBetweenPreviewAndCodeEditor` |
| TC-US-3-03 | AC-US-3-03 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/components/MermaidBlockCardTest.kt#testTemplateChipInsertion` | Given card in code editor mode, when tapping "Sequence" template chip | Assert code editor text is replaced with sequence diagram template | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.components.MermaidBlockCardTest#testTemplateChipInsertion` |
| TC-US-3-04 | AC-US-3-04 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/components/MermaidBlockCardTest.kt#testPinchZoomWithinCard` | Given card in preview mode, when pinch gesture is performed | Assert diagram scale factor increases without scrolling parent note | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.components.MermaidBlockCardTest#testPinchZoomWithinCard` |
| TC-US-3-05 | AC-US-3-05 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/components/MermaidBlockCardTest.kt#testReadOnlyHidesEditControls` | Given read-only note with Mermaid block, when rendered | Assert diagram canvas is displayed and `editor_mermaid_toggle_mode_{id}` is not present | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.components.MermaidBlockCardTest#testReadOnlyHidesEditControls` |
| TC-US-3-06 | AC-US-3-06 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/components/MermaidBlockCardTest.kt#testEmptyDiagramShowsPlaceholder` | Given Mermaid block with empty code, when rendered | Assert empty placeholder text is visible | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.components.MermaidBlockCardTest#testEmptyDiagramShowsPlaceholder` |

---

### US-4: Fullscreen Interactive Diagram Viewer & Visual Verification (Priority: P4)

Implement the `FullscreenDiagramViewerDialog` screen reachable via the expand action on any Mermaid card header. Provides an edge-to-edge interactive SVG canvas with pan, zoom controls (Zoom In, Zoom Out, Reset, Fit to Screen), code copy to clipboard, and SVG export/sharing. Owns the final visual verification contract and screenshot captures.

**Why this priority**: Completes the end-to-end user experience by enabling detailed inspection and export of complex diagrams, and serves as the sole visual verification owner.

**Independent Test**: Can be tested independently via fullscreen viewer tests asserting canvas interaction, zoom controls, clipboard copy, and visual captures.

**Acceptance Criterion**:

1. **AC-US-4-01 Given** a Mermaid card, **When** the user taps the Fullscreen button, **Then** the Fullscreen Diagram Viewer opens displaying the diagram centered on the full screen with zoom controls and top action bar.
2. **AC-US-4-02 Given** the Fullscreen Diagram Viewer, **When** user taps Zoom In (+), Zoom Out (-), or Fit to Screen, **Then** the diagram scale updates accordingly.
3. **AC-US-4-03 Given** the Fullscreen Diagram Viewer, **When** user taps "Copy Code", **Then** the Mermaid DSL code is copied to the Android clipboard.

**Acceptance Test Cases**:

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-4-01 | AC-US-4-01 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/FullscreenDiagramViewerTest.kt#testOpenFullscreenViewerAndNavigateBack` | Given note with Mermaid block, when tapping `editor_mermaid_fullscreen_btn_{id}` | Assert `fullscreen_diagram_canvas` is visible and tapping Back returns to editor | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.FullscreenDiagramViewerTest` |
| TC-US-4-02 | AC-US-4-02 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/FullscreenDiagramViewerTest.kt#testZoomControlsAndUpdateScale` | Given fullscreen viewer open, when tapping Zoom In and Fit to Screen buttons | Assert zoom percentage updates and canvas scales | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.FullscreenDiagramViewerTest` |
| TC-US-4-03 | AC-US-4-03 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/FullscreenDiagramViewerTest.kt#testCopyCodeToClipboard` | Given fullscreen viewer open, when tapping Copy Code button | Assert clipboard contains Mermaid DSL string | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.FullscreenDiagramViewerTest` |
| TC-US-4-VIS-01 | AC-US-3-01 | Visual verification | State-verifying screenshot capture: Note Editor Mermaid Diagram Preview | Given note editor with rendered Mermaid diagram preview asserted by `MermaidBlockCardTest#testMermaidCardDefaultsToPreviewMode` | Assert preview state passes and capture screenshot at `$FEATURE_DIR/visual_evidence/mermaid_card_preview.png` | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.components.MermaidBlockCardTest#testMermaidCardDefaultsToPreviewMode && mkdir -p docs/product/2026-08-18-mermaid-chart-preview/visual_evidence && adb exec-out screencap -p > docs/product/2026-08-18-mermaid-chart-preview/visual_evidence/mermaid_card_preview.png && test -s docs/product/2026-08-18-mermaid-chart-preview/visual_evidence/mermaid_card_preview.png` |
| TC-US-4-VIS-02 | AC-US-3-02 | Visual verification | State-verifying screenshot capture: Note Editor Mermaid Code Editor | Given note editor with Mermaid block in Code Editor mode asserted by `MermaidBlockCardTest#testToggleBetweenPreviewAndCodeEditor` | Assert code mode passes and capture screenshot at `$FEATURE_DIR/visual_evidence/mermaid_card_code_editor.png` | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.components.MermaidBlockCardTest#testToggleBetweenPreviewAndCodeEditor && mkdir -p docs/product/2026-08-18-mermaid-chart-preview/visual_evidence && adb exec-out screencap -p > docs/product/2026-08-18-mermaid-chart-preview/visual_evidence/mermaid_card_code_editor.png && test -s docs/product/2026-08-18-mermaid-chart-preview/visual_evidence/mermaid_card_code_editor.png` |
| TC-US-4-VIS-03 | AC-US-4-01 | Visual verification | State-verifying screenshot capture: Fullscreen Diagram Viewer | Given fullscreen diagram viewer asserted by `FullscreenDiagramViewerTest#testOpenFullscreenViewerAndNavigateBack` | Assert fullscreen state passes and capture screenshot at `$FEATURE_DIR/visual_evidence/mermaid_fullscreen_viewer.png` | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.FullscreenDiagramViewerTest#testOpenFullscreenViewerAndNavigateBack && mkdir -p docs/product/2026-08-18-mermaid-chart-preview/visual_evidence && adb exec-out screencap -p > docs/product/2026-08-18-mermaid-chart-preview/visual_evidence/mermaid_fullscreen_viewer.png && test -s docs/product/2026-08-18-mermaid-chart-preview/visual_evidence/mermaid_fullscreen_viewer.png` |

---

## 📊 Sprint Log

| Phase | Agent | Target / Outcome | Notes & Core Decisions |
| :--- | :--- | :--- | :--- |
| **Planning** | Planner | `spec.md`, `design.md`, `sprint-contract.md`, `feature_list.json` compiled | 4 vertical slices scoped; offline WebView Mermaid rendering confirmed; US-4 designated as visual verification owner. |
| **Implementation** | Generator | Pending user approval | |
| **Review 1** | Evaluator | Pending implementation | |
| **Revision 1** | Generator | Pending review | |
| **Final Review** | Evaluator | Pending review | |
