# Feature Spec — Code Block in Note Editor

**Date**: 2026-08-18  
**Status**: Approved  
**Related design**: `design.md`  

---

## Objective

Introduce a rich, interactive **Code Block** component to the Note Editor with language selection, lightweight syntax highlighting, line numbers, one-tap code copying, auto-save persistence, and seamless Markdown/PDF export. Reorganize the Basic Blocks catalog with dedicated "Basic" and "Advanced" section headers to house basic text structures and advanced modular blocks (Code Block and Mermaid Diagram).

## User Goal

As a developer, technical writer, student, or note creator, I want to write, edit, and organize code snippets with syntax coloring, line numbers, and language tags directly in my notes so that I can maintain clean technical documentation and copy code easily on my mobile device without formatting loss.

## Scope

### In Scope

- **Document Model & Schema Persistence**:
  - Introduce `EditorBlock.CodeBlock(id: String, language: String, code: String)` into `NoteDocument`.
  - JSON serialization/deserialization format (`type: "code"`, `language: String`, `code: String`) with backward/forward compatibility.
  - Default starter block: `language = "Plain Text"` (or auto-detected/selected), `code = ""`.
  - Plain text, Markdown, and PDF export formatting.
- **Basic Blocks Panel Reorganization (Basic & Advanced Sections)**:
  - Update `BasicBlocksPanel` to display two clear section headers:
    - **Basic**: Text (Paragraph), Heading 1, Heading 2, Heading 3, Heading 4, Bullet list, Number list, To-do list, Toggle list, Callout, Quote.
    - **Advanced**: Code (`BasicBlockType.CODE`), Mermaid Diagram (`BasicBlockType.MERMAID`).
  - Insertion logic: Inserts empty Code Block after the currently focused block or appends to document if no focus.
  - Auto-collapses the panel on tile selection and auto-saves the document.
- **Code Block Card UI & Monospace Editor**:
  - Elevated Material 3 card container (`#FFFFFF` surface in Light mode, `#1E1E1E` in Dark mode, `#E7E3F6` border stroke, 12dp rounded corners).
  - Card Header:
    - Language selector dropdown badge/chip displaying current language (e.g. "Kotlin", "Python", "JavaScript", "JSON", "Plain Text", etc.) with dropdown menu for language selection.
    - "Copy Code" button: one-tap action that copies the code block content to Android `ClipboardManager` with immediate visual feedback.
    - Delete/options action for removing the block.
  - Code Editor Body:
    - Left gutter displaying line numbers (1, 2, 3...) styled with `textSecondary` / `textTertiary`.
    - Monospace code editing surface (`FontFamily.Monospace`, 13sp/14sp font size, 20sp line height).
    - Lightweight, real-time client-side syntax highlighting for popular languages (Kotlin, Java, Python, JavaScript, TypeScript, HTML, CSS, JSON, SQL, Shell/Bash, C/C++, Rust, Go, Plain Text) via Compose `VisualTransformation` / `AnnotatedString`.
- **Read-Only Mode Support**:
  - In read-only notes (`isEditable = false`), the Code Block renders with syntax highlighting, line numbers, and active "Copy Code" action, but editing is disabled.
- **Export & Serialization**:
  - Markdown (.md) export formats Code Blocks as standard fenced code blocks:
    ````markdown
    ```kotlin
    fun main() {
        println("Hello")
    }
    ```
    ````
  - PDF export formats Code Blocks in a dedicated styled monospace container with language header.
  - Plain text export outputs raw indented or tagged code lines.

### Out Of Scope

- Full-blown IDE features (code completion, intellisense, linter diagnostics, debugger execution).
- Remote compilation or cloud execution sandbox (all editing and highlighting is 100% on-device).
- File import/export as separate standalone source code files (code blocks are part of note documents).

---

## Technical Spec

### Libraries & Dependencies

| Library / SDK | Version | Purpose |
|---|---|---|
| Jetpack Compose Material 3 | Existing project version | Card surfaces, dropdown menus, icon buttons, and typography. |
| Jetpack Compose UI Text | Existing project version | Monospace `AnnotatedString` syntax tokenizer and `VisualTransformation`. |
| Android ClipboardManager | Android SDK (minSdk 24) | On-device clipboard copying for "Copy Code" action. |
| Room & Kotlin / org.json | Existing project version | Document block JSON persistence and schema migration. |

### Key Technical Decisions

- **Client-Side Regex / Tokenizer Syntax Highlighting**: Lightweight lexical highlighter running on the UI thread using cached regex token rules (Keywords, Strings, Comments, Numbers, Builtin Types, Operators) for supported languages. Zero external heavy binary dependencies or webviews required, ensuring instant typing responsiveness (<16ms frame time).
- **Synchronized Line Numbers Gutter**: Line numbers are computed dynamically based on the newline count of the code string (`code.lines().size`) and rendered in a side gutter aligned with the code text lines.
- **Unidirectional State Flow**: Code and language changes dispatch through `NoteEditorViewModel` (`updateCodeBlock(id, language, code)`), triggering reactive document updates and debounced auto-save.

### External APIs / Services

- None. 100% on-device local execution and storage.

### Platform & Compatibility Constraints

- **Min SDK**: Android 7.0 (API 24).
- **Permissions required**: None.
- **Network**: Fully offline; no network permissions needed.

---

## Functional Requirements

- **FR-001**: The system MUST add `EditorBlock.CodeBlock(id, language, code)` to `NoteDocument` supporting backward-compatible JSON serialization (`type: "code"`, `language`, `code`).
- **FR-002**: The `BasicBlocksPanel` MUST be organized into two distinct sections with localized headers: "Basic" (Text, Headings 1–4, Bulleted, Numbered, Todo, Toggle, Callout, Quote) and "Advanced" (Code, Mermaid Diagram).
- **FR-003**: The Basic Blocks panel MUST include a "Code" tile under "Advanced" that inserts an empty Code Block after the currently focused block or at the end of the note.
- **FR-004**: The Code Block MUST be rendered as an elevated card container with `#FFFFFF` surface (Light theme) / `#1E1E1E` (Dark theme), `#E7E3F6` border, and 12dp rounded corners.
- **FR-005**: The Code Block card header MUST include a language selector badge that displays the active language and opens a dropdown menu to select from supported languages (Kotlin, Java, Python, JavaScript, TypeScript, HTML, CSS, JSON, SQL, Shell, C/C++, Rust, Go, Plain Text).
- **FR-006**: The Code Block card header MUST include a "Copy Code" button that copies the full code string to the Android system clipboard with instant feedback.
- **FR-007**: The Code Block MUST render line numbers (1, 2, 3...) in a dedicated left gutter synchronized with the code line count.
- **FR-008**: The Code Block editor body MUST support multi-line text editing with `FontFamily.Monospace` and real-time syntax highlighting for the selected language.
- **FR-009**: In read-only notes (`isEditable = false`), the Code Block MUST allow code selection and copying, while disabling text editing and language switching.
- **FR-010**: Note export MUST format Code Blocks as fenced code blocks ```` ```<language>\n<code>\n``` ```` in Markdown (.md) and as structured monospace boxes in PDF export.
- **FR-011**: The Code Block card header MUST provide a "Delete" icon button (visible when `isEditable = true`) that allows the user to remove the Code Block from the note document, auto-saving the updated document.

---

## Acceptance Criteria

- **AC-001**: Given an open Basic Blocks panel, when viewed, then the panel displays two section headers: "Basic" and "Advanced", with the "Code" tile and "Mermaid Diagram" tile positioned under "Advanced".
- **AC-002**: Given an editable note, when the user taps the "Code" tile in the Basic Blocks panel, then a new Code Block is inserted, the panel collapses, and the note document is auto-saved.
- **AC-003**: Given a Code Block in the editor, when the user selects a different language (e.g. "Python") from the language dropdown, then the language badge updates, syntax highlighting re-applies for Python syntax, and the change is persisted.
- **AC-004**: Given a Code Block with text, when the user types code, then line numbers update dynamically in the left gutter and syntax tokens (keywords, strings, comments) are highlighted in real-time.
- **AC-005**: Given a Code Block containing code, when the user taps the "Copy" button, then the exact code content is placed into the system clipboard.
- **AC-006**: Given a note containing a Code Block, when exported to Markdown, then the output includes the fenced code block ```` ```<language>\n<code>\n``` ```` with the selected language identifier.
- **AC-007**: Given a read-only note, when viewed, then the Code Block displays the syntax-highlighted code with line numbers and allows copying, but hides or disables editing controls.
- **AC-008**: Given an editable note with a Code Block, when the user taps the "Delete" button on the Code Block card header, then the block is immediately removed from the document, focus returns to the adjacent block, and the document is auto-saved.

---

## Data And Persistence

- Persisted in `Note.content` as part of `NoteDocument` JSON:
  ```json
  {
    "id": "b_code_12345",
    "type": "code",
    "language": "kotlin",
    "code": "fun main() {\n    println(\"Hello, Kotlin!\")\n}"
  }
  ```
- Graceful deserialization: Unknown or missing fields fall back to `language = "Plain Text"` and empty code without data loss or crashes.

---

## Edge Cases

- **Empty Code Block**: When code is empty, display placeholder text `"// Enter code here..."` and a single line number `1`.
- **Very Long Lines**: The monospace editor soft-wraps long lines (Compose `BasicTextField` default wrapping) so the code never breaks the card boundaries. Verified by `CodeSyntaxHighlighterTest#testVeryLongLineHandling`.
- **Large Code Snippets (1000+ lines)**: The syntax highlighter is a single-pass lexical scanner (no regex backtracking) that tokenizes in linear time without blocking the main thread for note-sized snippets. Verified by `CodeSyntaxHighlighterTest#testLargeCodeSnippetTokenization`.
- **Clipboard Permissions / Failures**: Copy uses the standard Compose `LocalClipboardManager`, which is permission-free and non-throwing for foreground apps on API 24+. No explicit try/catch fallback is required (documented non-goal — a foreground clipboard write has no practical failure mode on the supported API range).
- **Language Switching**: Switching language immediately updates token styling without modifying or corrupting the underlying code string.
- **Orientation / Recomposition**: Documented known limitation (non-goal) — the code block does not preserve cursor/selection/scroll state across device rotation; this mirrors the existing note-editor behavior and is outside this feature's scope.

---

## Explicit Assumptions

| # | Assumption | Risk if Wrong |
|---|---|---|
| A1 | Android ClipboardManager is available on all supported API levels (API 24+) without additional permissions. | Low. ClipboardManager is standard Android SDK foundation. |
| A2 | Client-side Compose VisualTransformation provides sufficient syntax highlighting performance for note snippets. | Low. Pure-Kotlin regex lexical tokenization runs synchronously in microseconds for note-sized code blocks. |
| A3 | Markdown fenced code blocks with language identifiers (e.g. ```` ```kotlin ````) are standard across markdown viewers. | Low. Standard CommonMark and GFM specification. |

---

## Open Questions

All questions must be ✅ Answered before this document is approved.

| # | Question | Status | Answer |
|---|---|---|---|
| Q1 | How should users insert a Code Block? | ✅ Answered | Via a "Code" tile in the Basic Blocks panel under a new "Advanced" section header. |
| Q2 | What language selection and syntax highlighting should be supported? | ✅ Answered | Language selector dropdown supporting popular languages (Kotlin, Java, Python, JS/TS, JSON, HTML/CSS, SQL, Shell, C/C++, Rust, Go, Plain Text) with lightweight real-time syntax highlighting. |
| Q3 | What controls and layout should appear on the Code Block card? | ✅ Answered | Elevated card container with header (language selector chip, Copy button, delete action), line numbers gutter, and monospace code editor body. |
| Q4 | How should the Basic Blocks panel be organized? | ✅ Answered | Two clear section headers: "Basic" (Paragraph, Headings 1–4, Lists, Toggle, Callout, Quote) and "Advanced" (Code, Mermaid Diagram). |
| Q5 | How should Code Blocks export to Markdown and PDF? | ✅ Answered | Standard fenced code block ```` ```<language>\n<code>\n``` ```` in Markdown, and styled monospace box in PDF. |

---

## Screen States

| State | Requirement | Acceptance Criteria |
|---|---|---|
| Empty Code Block | Displays header with language chip, Copy button (disabled or inactive), line number `1`, and placeholder text. | AC-002, AC-004 |
| Editing Code Block | Monospace text field with active cursor, dynamic line numbering, and real-time syntax highlighting. | AC-003, AC-004 |
| Copied Feedback | Brief visual confirmation (checkmark icon / Toast) when Copy button is tapped. | AC-005 |
| Read-Only Code Block | Highlighted code with line numbers, active Copy button, text selection enabled, editing controls disabled. | AC-007 |

---

## Navigation

- **Entry**: Note Editor screen (`NoteEditorScreen`) -> Tap "+" / Basic Blocks icon on bottom toolbar -> Tap "Code" under "Advanced" section.
- **Back/dismiss**: Panel collapses on tile selection or outside tap; Code Block remains inline in document.
- **Success**: Code Block inserted, auto-saved, editable in document flow.
- **Error recovery**: Invalid or empty code handled gracefully without interrupting note editing.

---

## Traceability

| Requirement | Design Section | Acceptance Criteria |
|---|---|---|
| FR-001 | Data and Persistence | AC-002, AC-006 |
| FR-002 | Screen 2 — Basic Blocks Panel | AC-001 |
| FR-003 | Screen 2 — Basic Blocks Panel | AC-001, AC-002 |
| FR-004 | Screen 1 — Note Editor Code Block Component | AC-002, AC-004 |
| FR-005 | Screen 1 — Note Editor Code Block Component | AC-003 |
| FR-006 | Screen 1 — Note Editor Code Block Component | AC-005 |
| FR-007 | Screen 1 — Note Editor Code Block Component | AC-004 |
| FR-008 | Screen 1 — Note Editor Code Block Component | AC-004 |
| FR-009 | Screen 1 — Note Editor Code Block Component | AC-007 |
| FR-010 | Screen 1 — Note Editor Code Block Component | AC-006 |
| FR-011 | Screen 1 — Note Editor Code Block Component | AC-008 |

---

## Verification Expectations

- **Unit**:
  - `NoteDocumentTest`: CodeBlock JSON serialization, deserialization, plain text export, markdown export ```` ```<lang>\n<code>\n``` ````.
  - `SyntaxHighlighterTest`: Tokenizer and `AnnotatedString` transformations for Kotlin, Python, JSON, Java, JavaScript, etc.
  - `BasicBlockTypeTest`: `BasicBlockType.CODE` mapping, storage value `"code"`, and panel tile item catalog.
- **Integration**:
  - `NoteEditorViewModelIntegrationTest`: Inserting CodeBlock, updating language, modifying code, auto-saving document, and delete block action.
- **Instrumented UI**:
  - `CodeBlockCardTest`: Rendering card, tapping language selector, selecting language, typing code with line numbers, tapping Copy button.
  - `BasicBlocksPanelTest`: Verifying "Basic" and "Advanced" section headers, selecting "Code" tile.
- **Visual Verification**:
  - Runtime screenshots capturing Code Block card in editor and Basic Blocks panel with Advanced section.

---

## No Open Questions Gate

- [x] All requirements are specific and testable.
- [x] All non-goals are explicit.
- [x] No unresolved assumptions remain.
- [x] All visual states are defined in `design.md`.
- [x] All navigation outcomes are defined.
