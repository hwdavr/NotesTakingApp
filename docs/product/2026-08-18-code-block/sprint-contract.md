# Sprint Contract — Code Block in Note Editor

## 🏃 Sprint Overview

*   **Sprint:** P06-01
*   **Feature:** Code Block in Note Editor
*   **Duration:** 1 sprint

---

## 🎯 Scope

### In Scope

- [ ] **Document Block Model & Schema Persistence (US-1)**: Add `EditorBlock.CodeBlock(id, language, code)` to `NoteDocument.kt` with JSON serialization/deserialization (`type: "code"`), auto-save persistence, `BasicBlockType.CODE` mapping, and Markdown/PDF export formatting in `NoteExporter.kt`.
- [ ] **Basic Blocks Panel Reorganization (US-1)**: Update `BasicBlocksPanel.kt` to display two clear section headers: "Basic" (Text, Headings 1–4, Lists, Toggle, Callout, Quote) and "Advanced" (Code, Mermaid Diagram), and wire focus-aware insertion.
- [ ] **Code Block Card, Syntax Highlighting, Line Numbers & Actions (US-2)**: Implement `CodeBlockCard.kt` elevated container with a synchronized line-number gutter, a lightweight real-time regex-based lexical tokenizer for Kotlin, Java, Python, JavaScript, TypeScript, HTML, CSS, JSON, SQL, Shell, C/C++, Rust, Go, and Plain Text, a language selector chip with DropdownMenu, a "Copy" icon button with ClipboardManager integration, and a "Delete" icon button with auto-save.
- [ ] **Read-Only Mode, Connected UI Flows & Visual Verification (US-3)**: Deliver connected screen tests and visual-flow tests covering the full user journey and read-only behavior, with in-test screenshot capture for reference-anchor verification against approved mockups.

### Out of Scope

- IDE execution / code running sandbox (client-side viewing and editing only).
- Remote compilation or cloud syntax analysis.
- Live collaborative multi-user editing.

---

## Platform Capability & Environment Contract *(required)*

Link the feature workspace artifact: `platform-capability-matrix.md`.

The matrix declares minimum API 24, target API 34, single resource ownership under `NoteEditorViewModel`, and loud failure policy `fail_loudly`. Platform validation is explicitly not required because the feature operates strictly on standard Android Jetpack Compose, Room persistence, and `ClipboardManager` APIs.

---

## Spec Coverage Matrix *(required)*

| Source requirement | Requirement summary | Primary user story | Primary acceptance test | Handling |
|---|---|---|---|---|
| FR-001 | CodeBlock model & JSON serialization | US-1 | TC-US-1-01 | In scope |
| FR-002 | BasicBlocksPanel Basic and Advanced section headers | US-1 | TC-US-1-02 | In scope |
| FR-003 | Code tile insertion in BasicBlocksPanel | US-1 | TC-US-1-03 | In scope |
| FR-004 | Elevated Code Block card container | US-2 | TC-US-3-01 | In scope |
| FR-005 | Language selector chip & dropdown menu | US-2 | TC-US-2-04 | In scope |
| FR-006 | Copy Code button with clipboard integration | US-2 | TC-US-2-06 | In scope |
| FR-007 | Synchronized line numbers gutter | US-2 | TC-US-2-02 | In scope |
| FR-008 | Monospace editor & real-time syntax highlighting | US-2 | TC-US-2-01 | In scope |
| FR-009 | Read-only mode support | US-3 | TC-US-3-03 | In scope |
| FR-010 | Markdown and PDF export formatting | US-1 | TC-US-1-04 | In scope |
| FR-011 | Delete Code Block button & removal | US-2 | TC-US-2-07 | In scope |
| AC-001 | Basic Blocks panel shows Basic and Advanced sections | US-1 | TC-US-1-02 | In scope |
| AC-002 | Tapping Code tile inserts block & auto-saves | US-1 | TC-US-1-03 | In scope |
| AC-003 | Selecting language updates badge & syntax coloring | US-2 | TC-US-2-04 | In scope |
| AC-004 | Code editing updates line numbers & syntax tokens | US-2 | TC-US-2-05 | In scope |
| AC-005 | Tapping Copy places code into clipboard | US-2 | TC-US-2-06 | In scope |
| AC-006 | Markdown export includes fenced code block | US-1 | TC-US-1-04 | In scope |
| AC-007 | Read-only note displays highlighted code & copy action | US-3 | TC-US-3-03 | In scope |
| AC-008 | Tapping Delete removes block & auto-saves | US-2 | TC-US-2-07 | In scope |
| Edge case: Empty Code Block | Displays placeholder and line 1 | US-2 | TC-US-2-03 | In scope |
| Edge case: Very Long Lines | Horizontal scroll or clean wrapping | US-2 | TC-US-2-05 | In scope |
| Edge case: Large Code Snippets | Lightweight regex evaluation without lag | US-2 | TC-US-2-01 | In scope |
| Edge case: Clipboard Errors | Safe fallback handling for clipboard operations | US-2 | TC-US-2-06 | In scope |
| Design: Code Block Card Container | Elevated container with header and gutter | US-2 | TC-US-3-01 | In scope |
| Design: Basic Blocks Panel Advanced Section | 2-column horizontal pill tiles under Basic/Advanced | US-1 | TC-US-1-02 | In scope |

---

## User Scenarios & Testing *(mandatory)*

### US-1: Document Block Model, Persistence & Basic Blocks Panel Insertion (Priority: P1)

Users can store and load Code Blocks within note documents, access the newly structured Basic Blocks panel with "Basic" and "Advanced" sections, insert Code Blocks seamlessly, and export them to Markdown and PDF formats.

**Why this priority**: Foundational data model, schema persistence, catalog reorganization, and export mechanisms required by all downstream editing features.

**Independent Test**: Can be fully tested via Unit and Integration tests verifying serialization, panel tile rendering, insertion logic, and export output.

**Acceptance Criterion**:

1. **AC-US-1-01 Given** a note document, **When** a CodeBlock is serialized to JSON and deserialized back, **Then** all properties (`id`, `language`, `code`) are strictly preserved.
2. **AC-US-1-02 Given** an open Basic Blocks panel, **When** viewed, **Then** it renders "Basic" and "Advanced" section headers, with "Code" and "Mermaid Diagram" tiles under "Advanced".
3. **AC-US-1-03 Given** an editable note, **When** the user taps the "Code" tile, **Then** a new CodeBlock is inserted after the focused block, the panel collapses, and the note is auto-saved.
4. **AC-US-1-04 Given** a note containing a CodeBlock, **When** exported to Markdown or PDF, **Then** the output contains the standard fenced code block ```` ```<language>\n<code>\n``` ```` in Markdown and a styled box in PDF.

**Acceptance Test Cases** *(required for implementation authorization)*:

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-1-01 | AC-US-1-01 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/mapper/NoteDocumentTest.kt#testCodeBlockSerializationAndDeserialization` | Given CodeBlock model, when serialized to JSON and parsed back | Assert identical id, language, and code content | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.mapper.NoteDocumentTest.testCodeBlockSerializationAndDeserialization"` |
| TC-US-1-02 | AC-US-1-02 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/components/BasicBlocksPanelTest.kt#testBasicAndAdvancedSectionHeadersAndCodeTile` | Given BasicBlocksPanel model, when querying tile sections | Assert Basic and Advanced headers present with Code tile under Advanced | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.components.BasicBlocksPanelTest.testBasicAndAdvancedSectionHeadersAndCodeTile"` |
| TC-US-1-03 | AC-US-1-03 | JVM integration | `app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorViewModelIntegrationTest.kt#testInsertCodeBlockFromBasicBlocksPanel` | Given active editor note, when insertBasicBlock(BasicBlockType.CODE) is called | Assert CodeBlock is added to document blocks and auto-save is triggered | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelIntegrationTest.testInsertCodeBlockFromBasicBlocksPanel"` |
| TC-US-1-04 | AC-US-1-04 | JVM unit | `app/src/test/java/com/example/notesapp/util/NoteExporterTest.kt#testExportCodeBlockToMarkdownAndPdf` | Given NoteDocument with CodeBlock, when exportNoteToMarkdown and exportNoteToPdf are invoked | Assert Markdown contains fenced block ```` ```kotlin ```` and PDF contains formatted code section | `./gradlew testDebugUnitTest --tests "com.example.notesapp.util.NoteExporterTest.testExportCodeBlockToMarkdownAndPdf"` |

---

### US-2: Code Block Card UI, Syntax Highlighting, Line Numbers, Language Selection, Copy & Deletion (Priority: P2)

Users interact with the elevated Code Block card in the note editor: code is rendered in monospace with real-time syntax coloring and synchronized line numbers, the language is selectable from a dropdown, code is editable with auto-save, and the block can be copied or deleted.

**Why this priority**: The user-facing card and its lexical engine. The syntax highlighter and line-number engine are delivered together with the card that consumes them, so the slice is user-visible and independently demonstrable the moment it ships.

**Independent Test**: Tokenizer and line-count unit tests plus ViewModel integration tests for language/code/delete, and an instrumented card test for the clipboard boundary.

**Acceptance Criterion**:

1. **AC-US-2-01 Given** source code for a supported language (Kotlin, Python, JSON, Java, JavaScript, etc.), **When** passed through the syntax highlighter, **Then** keywords, strings, comments, numbers, and types are styled with appropriate theme tokens.
2. **AC-US-2-02 Given** multiline code text, **When** evaluated, **Then** line numbers (1, 2, 3...) are calculated dynamically matching the line count.
3. **AC-US-2-03 Given** empty text or Plain Text language, **When** processed, **Then** fallback monospace styling is returned without errors.
4. **AC-US-2-04 Given** a Code Block in the editor, **When** the user taps the language chip and selects a new language (e.g. "Python"), **Then** the badge updates, syntax highlighting re-applies, and the change is saved.
5. **AC-US-2-05 Given** a Code Block in the editor, **When** the user types code, **Then** line numbers update and the code content is auto-saved.
6. **AC-US-2-06 Given** a Code Block containing code, **When** the user taps the "Copy" button, **Then** the code is copied to the system clipboard and a visual confirmation is displayed.
7. **AC-US-2-07 Given** an editable note with a Code Block, **When** the user taps the "Delete" button on the card header, **Then** the block is removed from the document and auto-saved.

**Acceptance Test Cases** *(required for implementation authorization)*:

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-2-01 | AC-US-2-01 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/components/CodeSyntaxHighlighterTest.kt#testSyntaxHighlightingForSupportedLanguages` | Given Kotlin/Python/JSON code snippets, when syntax highlighter transforms text | Assert Keyword, String, Comment, and Number styles are applied to expected character ranges | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.components.CodeSyntaxHighlighterTest.testSyntaxHighlightingForSupportedLanguages"` |
| TC-US-2-02 | AC-US-2-02 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/components/CodeSyntaxHighlighterTest.kt#testDynamicLineNumberCalculation` | Given code string with N newline characters, when computing line count | Assert calculated line count equals N+1 and gutter lines sequence 1..N+1 | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.components.CodeSyntaxHighlighterTest.testDynamicLineNumberCalculation"` |
| TC-US-2-03 | AC-US-2-03 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/components/CodeSyntaxHighlighterTest.kt#testPlainTextAndFallbackHandling` | Given empty string or Plain Text language, when transformed | Assert default monospace styling returned and placeholder handled gracefully | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.components.CodeSyntaxHighlighterTest.testPlainTextAndFallbackHandling"` |
| TC-US-2-04 | AC-US-2-04 | JVM integration | `app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorViewModelIntegrationTest.kt#testUpdateCodeBlockLanguage` | Given note with CodeBlock, when updateCodeBlockLanguage(id, "python") is called | Assert document block language is updated to python and auto-saved | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelIntegrationTest.testUpdateCodeBlockLanguage"` |
| TC-US-2-05 | AC-US-2-05 | JVM integration | `app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorViewModelIntegrationTest.kt#testUpdateCodeBlockContent` | Given note with CodeBlock, when updateCodeBlockCode(id, newCode) is called | Assert document block code is updated and persisted | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelIntegrationTest.testUpdateCodeBlockContent"` |
| TC-US-2-06 | AC-US-2-06 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/components/CodeBlockCardTest.kt#testCopyCodeToClipboard` | Given CodeBlockCard with sample code rendered, when clicking copy button | Assert clipboard contains the exact code string and copy feedback is shown | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.components.CodeBlockCardTest#testCopyCodeToClipboard` |
| TC-US-2-07 | AC-US-2-07 | JVM integration | `app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorViewModelIntegrationTest.kt#testDeleteCodeBlockFromDocument` | Given note with CodeBlock, when deleteBlock(id) is invoked | Assert CodeBlock is removed from note document blocks and auto-saved | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelIntegrationTest.testDeleteCodeBlockFromDocument"` |

---

### US-3: Read-Only Mode, Connected UI Flows, Visual Verification & Acceptance Verification (Priority: P3)

Users experience a fully polished, accessible, and theme-adaptive Code Block and Basic Blocks panel experience across editable and read-only modes, with runtime visual proof matching approved design mockups.

**Why this priority**: Final end-to-end user-reachable slice that delivers read-only behavior, validates the complete visual flow, verifies reference-anchor bounds, and records runtime visual evidence.

**Independent Test**: Can be tested via connected Android instrumented UI tests capturing runtime evidence against design mockups.

**Acceptance Criterion**:

1. **AC-US-3-01 Given** a note with a Code Block card, **When** rendered in the editor, **Then** the card layout, language badge, line numbers gutter, and copy/delete actions match the approved design.
2. **AC-US-3-02 Given** the note editor, **When** the Basic Blocks panel is opened, **Then** "Basic" and "Advanced" section headers and the "Code" tile render matching the approved design.
3. **AC-US-3-03 Given** a read-only note, **When** viewed, **Then** the Code Block card renders syntax-highlighted code with line numbers and allows copying, but disables editing and hides the delete button.

**Acceptance Test Cases** *(required for implementation authorization)*:

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-3-01 | AC-US-3-01 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/CodeBlockScreenTest.kt#testCodeBlockCardRenderingAndInteraction` | Given active NoteEditorScreen with CodeBlock, when rendered | Assert CodeBlock card, language badge, line numbers, and action buttons are displayed | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.CodeBlockScreenTest#testCodeBlockCardRenderingAndInteraction` |
| TC-US-3-02 | AC-US-3-02 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/CodeBlockScreenTest.kt#testBasicBlocksPanelAdvancedSectionRendering` | Given active NoteEditorScreen, when BasicBlocksPanel is opened | Assert Basic and Advanced section headers and Code tile are visible and clickable | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.CodeBlockScreenTest#testBasicBlocksPanelAdvancedSectionRendering` |
| TC-US-3-03 | AC-US-3-03 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/CodeBlockScreenTest.kt#testReadOnlyCodeBlockBehavior` | Given NoteEditorScreen in read-only mode with CodeBlock, when rendered | Assert CodeBlock is displayed, Copy button is active, Delete button is hidden, and editor is not editable | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.CodeBlockScreenTest#testReadOnlyCodeBlockBehavior` |
| TC-US-3-VIS-01 | AC-US-3-01 | Visual verification | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/CodeBlockVisualFlowTest.kt#captureCodeBlockEditor` | Given CodeBlock card in NoteEditorScreen with Kotlin snippet, when `composeRule.waitForIdle()` completes and in-test screenshot captures active window | The in-test capture produces a non-empty PNG on device at `/sdcard/Download/code_block_editor.png`, pulled to `docs/product/2026-08-18-code-block/visual_evidence/code_block_editor.png` for review against `design/mockup_code_block_editor.png` | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.CodeBlockVisualFlowTest#captureCodeBlockEditor && adb -s emulator-5554 pull /sdcard/Download/code_block_editor.png "docs/product/2026-08-18-code-block/visual_evidence/code_block_editor.png" && test -s "docs/product/2026-08-18-code-block/visual_evidence/code_block_editor.png"` |
| TC-US-3-VIS-02 | AC-US-3-02 | Visual verification | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/CodeBlockVisualFlowTest.kt#captureBasicBlocksPanelAdvanced` | Given open BasicBlocksPanel in NoteEditorScreen, when `composeRule.waitForIdle()` completes and in-test screenshot captures active window | The in-test capture produces a non-empty PNG on device at `/sdcard/Download/basic_blocks_panel_advanced.png`, pulled to `docs/product/2026-08-18-code-block/visual_evidence/basic_blocks_panel_advanced.png` for review against `design/mockup_basic_blocks_panel_advanced.png` | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.CodeBlockVisualFlowTest#captureBasicBlocksPanelAdvanced && adb -s emulator-5554 pull /sdcard/Download/basic_blocks_panel_advanced.png "docs/product/2026-08-18-code-block/visual_evidence/basic_blocks_panel_advanced.png" && test -s "docs/product/2026-08-18-code-block/visual_evidence/basic_blocks_panel_advanced.png"` |

---

## 📊 Sprint Log

| Phase | Agent | Target / Outcome | Notes & Core Decisions |
| :--- | :--- | :--- | :--- |
| **Planning** | Planner | `sprint-contract.md` compiled | Slices US-1 through US-4 defined with 1:1 mapping, complete spec coverage matrix, and concrete test commands. |
| **Re-slicing** | Planner | `sprint-contract.md` + `feature_list.json` restructured to 3 slices | Merged the former US-2 (syntax highlighter & line-number engine) into the US-2 Code Block Card slice: the engine alone was a horizontal, engine-only slice with no user-visible consumer until the card existed. Read-only mode and visual verification remain in the final slice (US-3). |
