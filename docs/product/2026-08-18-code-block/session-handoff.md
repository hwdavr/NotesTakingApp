# Session Handoff

## Verified Now

- What is currently working: `EditorBlock.CodeBlock` model with `type: "code"` JSON round-trip, `BasicBlockType.CODE`, Basic Blocks panel "Basic"/"Advanced" sections with a Code tile under Advanced, `insertBasicBlock(BasicBlockType.CODE)` focus-aware insertion with auto-save, Markdown fenced-block export, PDF monospace code box, and a monospace `CodeBlockCard` editor wired into the note editor.
- What verification actually ran: TC-US-1-01..04 (all exit 0), `check-platform-evidence.sh --slice "US-1"` (exit 0), `connectedDebugAndroidTest` on `com.example.notesapp.util.NoteExporterTest` (2/2), `ktlintCheck` and `detekt` (PASS), full `testDebugUnitTest` after `./gradlew clean` (389 tests green), `assembleDebug` (green), `installDebug` (installed on emulator-5554).

## Changed This Session

- Code or behavior added: CodeBlock document block + persistence, BasicBlocksPanel section reorg + Code tile, CodeBlockCard composable, `updateCodeBlock` ViewModel extension, Markdown/PDF export, strings, and US-1 tests (JVM + one instrumented PDF export test).
- Infrastructure or harness changes: `./gradlew clean` removed stale `app/build` artifacts left by the pre-reset state (they produced phantom `CodeSyntaxHighlighterTest` NoClassDefFoundError failures).

## Broken Or Unverified

- Known defect: None for US-1.
- Unverified path: `CodeSyntaxHighlighter` and line-number engine are intentionally not delivered yet (US-2). `CodeBlockCard` has no syntax highlighting, line-number gutter, language dropdown, copy button, or delete button yet.
- Risk for the next session: Do not trust stale `app/build` output; a clean build is required after the reset. `PdfDocument` cannot run under Robolectric — PDF export must be verified with the instrumented `NoteExporterTest`.

## Next Best Step

- Highest-priority unfinished feature: US-2 (Client-Side Syntax Highlighter & Line Number Engine).
- Why it is next: US-1 is passing; US-2 is the next `not_started` slice and provides the lexical engine + line numbering that US-3/US-4 render and verify.
- What counts as passing: TC-US-2-01..03 (syntax highlighting for supported languages, dynamic line numbering, plain-text/empty fallback) exit 0, plus ktlint/detekt clean and platform-evidence check.
- What must not change during that step: the `type: "code"` JSON schema, the `BasicBlockType.CODE` storage value `"code"`, and the Basic/Advanced panel section structure.

## Commands

- Startup: `docs/product/2026-08-18-code-block/`
- Verification: `./gradlew testDebugUnitTest && ./gradlew connectedDebugAndroidTest`
- Focused debug command: `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.components.CodeSyntaxHighlighterTest"`
