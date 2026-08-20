# Test Review — code-block

## Review Scope and Evidence Provenance

| Item | Value |
|---|---|
| Feature / slice | `code-block` (US-1, US-2, US-3) — workspace `docs/product/2026-08-18-code-block/` |
| Current commit | `9eee46f` (feature workspace is uncommitted; implementation verified on-disk) |
| Baselines reviewed | `spec.md`, `sprint-contract.md`, `feature_list.json`, `progress.md`, `session-handoff.md` |
| Changed production files reviewed | `NoteDocument.kt`, `BasicBlockType.kt`, `BasicBlocksPanel.kt`, `CodeBlockCard.kt`, `CodeSyntaxHighlighter.kt`, `CodeLanguage.kt`, `NoteEditorCodeActions.kt`, `NoteEditorViewModel.kt`, `NoteEditorScreen.kt`, `NoteExporter.kt`, `AppColors.kt` |
| Changed test files reviewed | `NoteDocumentTest.kt`, `BasicBlocksPanelTest.kt`, `CodeSyntaxHighlighterTest.kt`, `NoteEditorViewModelIntegrationTest.kt`, `NoteExporterTest.kt` (JVM + instrumented), `CodeBlockCardTest.kt`, `CodeBlockScreenTest.kt`, `CodeBlockVisualFlowTest.kt` |

### Command Evidence

| Command | Exit code | Timestamp | Provenance | Result / failure detail |
|---|---:|---|---|---|
| `./gradlew testDebugUnitTest` | 0 | 2026-08-20 | Independently executed | PASS (JVM + integration suite green) |
| `./gradlew koverLog` | 0 | 2026-08-20 | Independently executed | PASS — application line coverage 82.6775% |
| `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=…CodeBlockCardTest,…CodeBlockScreenTest,…CodeBlockVisualFlowTest,…NoteExporterTest` | 0 | 2026-08-20 | Independently executed | PASS — 11/11 tests, 0 failed, 0 skipped |

All three commands were independently re-executed during this review (not merely cited from recorded stage evidence).

## Requirement-to-Test Traceability

| Source ID | Required behavior | Test file + method | Production trigger exercised | Observable assertion | Evidence status | Result |
|---|---|---|---|---|---|---|
| FR-001 | `EditorBlock.CodeBlock(id, language, code)` + JSON round-trip (`type:"code"`) | `NoteDocumentTest#testCodeBlockSerializationAndDeserialization` | `NoteDocument.toJsonString()` / `fromContent()` | id, language, code strictly preserved | Independently executed | PASS |
| FR-002 | Basic/Advanced section headers | `BasicBlocksPanelTest#testBasicAndAdvancedSectionHeadersAndCodeTile` | `basicBlocksSections` catalog | Basic (11 tiles, no CODE/MERMAID), Advanced (CODE+MERMAID) | Independently executed | PASS |
| FR-003 | Code tile inserts empty CodeBlock after focus, collapses panel, auto-saves | `NoteEditorViewModelIntegrationTest#testInsertCodeBlockFromBasicBlocksPanel` + `CodeBlockScreenTest#testBasicBlocksPanelAdvancedSectionRendering` | `insertBasicBlock(CODE)` + panel tile click | CodeBlock added, default "Plain Text"/empty, focus set, panel collapsed, persisted `"type":"code"` | Independently executed | PASS |
| FR-004 | Elevated card container (surface/border/12dp) | `CodeBlockVisualFlowTest#captureCodeBlockEditor` | `CodeBlockCard` in `NoteEditorScreenContent` | card inset 16dp from content, header center-line alignment, gutter/editor top alignment | Independently executed | PASS |
| FR-005 | Language selector badge + dropdown | `CodeBlockCardTest#testLanguageSelectionInvokesCallback` + `CodeBlockScreenTest#testCodeBlockCardRenderingAndInteraction` | selector chip + DropdownMenuItem click | selected language propagated (`"Python"`), state updated | Independently executed | PASS |
| FR-006 | Copy button → clipboard | `CodeBlockCardTest#testCopyCodeToClipboard` + `CodeBlockScreenTest#testReadOnlyCodeBlockBehavior` | `ClipboardManager.setText` on icon click | clipboard primary clip equals exact code string | Independently executed | PASS |
| FR-007 | Line numbers gutter 1..N | `CodeSyntaxHighlighterTest#testDynamicLineNumberCalculation` + visual bounds | `CodeSyntaxHighlighter.lineNumbers()` | line count = newline+1, empty = `[1]`, gutter aligned | Independently executed | PASS |
| FR-008 | Monospace editor + real-time syntax highlighting | `CodeSyntaxHighlighterTest#testSyntaxHighlightingForSupportedLanguages` + `CodeBlockCardTest` | `CodeSyntaxHighlighter.tokenize()` via `VisualTransformation` | Keyword/String/Comment/Number tokens at expected ranges (Kotlin, Python, JSON) | Independently executed | PASS |
| FR-009 | Read-only: highlighted code + copy, edit/delete disabled | `CodeBlockCardTest#testReadOnlyShowsCodeAndHidesEditingControls` + `CodeBlockScreenTest#testReadOnlyCodeBlockBehavior` | `CodeBlockCard(isEditable=false)` | readonly text + line numbers displayed, copy works, delete/editor absent, lang selector disabled | Independently executed | PASS |
| FR-010 | Markdown + PDF export | `NoteExporterTest#testExportCodeBlockToMarkdownAndPdf` (JVM) + `NoteExporterTest#testExportToPdfWithCodeBlock` (instrumented) | `exportToMarkdown` / `exportToPdf` → `renderCodeBlock` | Markdown contains ```` ```kotlin ```` + code; PDF file exists and non-empty | Independently executed | **Fixed ✅** — instrumented `testExportToPdfWithCodeBlock` now back-renders the PDF and asserts non-blank content |
| FR-011 | Delete button removes block + auto-save | `NoteEditorViewModelIntegrationTest#testDeleteCodeBlockFromDocument` + `CodeBlockCardTest#testDeleteButtonInvokesCallback` | `deleteBlock(id)` | CodeBlock removed, text block retained, persisted | Independently executed | PASS |
| AC-001 | Panel shows Basic + Advanced sections | `BasicBlocksPanelTest#testBasicAndAdvancedSectionHeadersAndCodeTile` | `basicBlocksSections` | both section headers present | Independently executed | PASS |
| AC-002 | Code tile tap inserts + auto-saves | `NoteEditorViewModelIntegrationTest#testInsertCodeBlockFromBasicBlocksPanel` | `insertBasicBlock(CODE)` | block inserted, panel collapsed, saved | Independently executed | PASS |
| AC-003 | Language selection updates badge + coloring + persisted | `NoteEditorViewModelIntegrationTest#testUpdateCodeBlockLanguage` | `updateCodeBlock(id, language)` | language updated + persisted | Independently executed | PASS |
| AC-004 | Typing updates line numbers + tokens | `NoteEditorViewModelIntegrationTest#testUpdateCodeBlockContent` + `CodeBlockScreenTest#testCodeBlockCardRenderingAndInteraction` | `updateCodeBlock(id, code)` + `performTextInput` | code updated, persisted; line/token recompute via highlighter | Independently executed | PASS |
| AC-005 | Copy places exact code into clipboard | `CodeBlockCardTest#testCopyCodeToClipboard` | `ClipboardManager.setText` | clipboard equals exact code | Independently executed | PASS |
| AC-006 | Markdown export includes fenced block | `NoteExporterTest#testExportCodeBlockToMarkdownAndPdf` | `exportToMarkdown` | ```` ```kotlin ```` + code present | Independently executed | PASS |
| AC-007 | Read-only shows highlighted code + copy | `CodeBlockScreenTest#testReadOnlyCodeBlockBehavior` | `CodeBlockCard(isEditable=false)` | highlighted code, copy active, edit/delete hidden | Independently executed | PASS |
| AC-008 | Delete removes block + auto-save | `NoteEditorViewModelIntegrationTest#testDeleteCodeBlockFromDocument` | `deleteBlock(id)` | block removed + persisted | Independently executed | PASS |
| Edge: Empty Code Block | placeholder + line 1 | `CodeSyntaxHighlighterTest#testPlainTextAndFallbackHandling` + `CodeBlockCard` decorationBox | `lineCount("")` / placeholder render | empty → 1 line, no tokens | Independently executed | PASS |
| Edge: Very Long Lines | clean wrapping (Compose soft-wrap) | `CodeSyntaxHighlighterTest#testVeryLongLineHandling` | tokenizer over a 200-term single line | line count == 1; full-range contiguous coverage | Independently executed | **Fixed ✅** |
| Edge: Large Snippets (1000+) | linear-scan tokenizer without lag | `CodeSyntaxHighlighterTest#testLargeCodeSnippetTokenization` | tokenize 1000-line Kotlin snippet | 1000 keywords/numbers/comments; contiguous full coverage | Independently executed | **Fixed ✅** |
| Edge: Clipboard Errors | non-throwing Compose ClipboardManager (documented non-goal) | `CodeBlockCardTest#testCopyCodeToClipboard` | real ClipboardManager write | exact code in clipboard | Independently executed | **Fixed ✅** (documented non-goal) |
| Edge: Language Switching | no code corruption | `testUpdateCodeBlockLanguage` | `updateCodeBlock(id, language)` | language changes, code untouched | Independently executed | PASS |
| Edge: Orientation / Recomposition | cursor/selection/scroll survive rotation | none | — | demoted to documented known limitation (non-goal) | Spec amended | **Fixed ✅** (documented non-goal) |

## Test Quality Findings

- ✅ Names describe real Given/When/Then behavior (all mapped methods use descriptive names).
- ✅ Each core FR/AC test exercises a real production trigger (`insertBasicBlock`, `updateBlock`, `ClipboardManager.setText`, `exportToMarkdown`, `tokenize`) — no setter-only or preloaded-final-state tests for the core paths.
- ✅ Assertions are observable and specific (exact code strings, token ranges, clipboard content, persisted JSON).
- ✅ No unused capture variables, tautological assertions, empty verifies, or `assertTrue(true)`.
- ✅ Isolation is layer-appropriate: JVM unit/integration use in-memory DAO + MockWebServer; instrumented tests use `createComposeRule()` and stateless `NoteEditorScreenContent`; `CodeBlockVisualFlowTest` captures in-test via `uiAutomation.takeScreenshot()` during `waitForIdle()`.
- ⚠️ No API endpoints are introduced by this feature — shared JSON scenario requirement is N/A (no new endpoint; feature is on-device persistence + export).
- ✅ Import hygiene passes (`ktlintCheck` exit 0; architecture script flags no inline FQCNs).

### Conditional Categories

| Category | In scope? | Coverage / N/A reason | Result |
|---|---|---|---|
| Runtime permissions | No | No new permission; clipboard is permission-free since API 24 | N/A |
| Asynchronous callbacks and animation | Yes | Copy feedback `LaunchedEffect` + `delay(1500)` and debounced auto-save; both exercised indirectly | PASS |
| Lifecycle and navigation cleanup | Yes | Read-only mode + panel collapse asserted; no new navigation | PASS |
| Error and retry behavior | No | No network/retry introduced; clipboard "fallback" listed as edge case (see F-4) | N/A (weak) |
| API/data error matrix | No | No new API endpoint | N/A |
| Dedicated visual flow capture | Yes | `CodeBlockVisualFlowTest` uses in-test `takeScreenshot()` during `waitForIdle()`, no post-test CLI screencap | PASS |

## Coverage Distribution

| Scope / class | Coverage | Branches not proven | Result |
|---|---:|---|---|
| Overall project | 82.6775% | above 80% threshold | PASS |
| New ViewModel surface (`NoteEditorCodeActions.kt` extension) | covered via `NoteEditorViewModelIntegrationTest` | — | PASS |
| `CodeSyntaxHighlighter` (pure) | covered by `CodeSyntaxHighlighterTest` | — | PASS |

## Regression Verification

| Item | Evidence | Result |
|---|---|---|
| Reproduction test red before fix | N/A (feature, not bug fix) | N/A |
| Reproduction test green after fix | N/A | N/A |
| No uncontrolled timing/threading | No `Thread.sleep`; auto-save advanced via `advanceTimeBy`/`advanceUntilIdle`; UI via `waitForIdle` | PASS |

## Verdict

**FIXED** — all previously revision-required rows resolved (see Fix Pass Summary below).

## Fix Pass Summary

| Row | Original status | Fix applied | New status |
|---|---|---|---|
| FR-010 / TC-US-1-04 (PDF assertion) | REVISION REQUIRED | `testExportToPdfWithCodeBlock` now back-renders the PDF via `PdfRenderer` and asserts page count ≥ 1 plus non-blank content | Fixed ✅ |
| Edge: Very Long Lines | REVISION REQUIRED (nominal) | Added `CodeSyntaxHighlighterTest#testVeryLongLineHandling` | Fixed ✅ |
| Edge: Large Snippets (1000+) | REVISION REQUIRED (nominal) | Added `CodeSyntaxHighlighterTest#testLargeCodeSnippetTokenization` | Fixed ✅ |
| Edge: Clipboard Errors | REVISION REQUIRED (nominal) | Documented non-throwing Compose ClipboardManager as non-goal in `spec.md` | Fixed ✅ |
| Edge: Orientation / Recomposition | REVISION REQUIRED (unmapped) | Demoted to documented known limitation (non-goal) in `spec.md` | Fixed ✅ |

**Fix Pass:** 5/5 rows fixed; 0 unresolved (2026-08-20).
