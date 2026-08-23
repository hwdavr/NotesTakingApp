# Evaluator Rubric

| Category | Question | Score (0-5) | Notes |
| --- | --- | ---: | --- |
| Correctness | Does the implemented behavior match the requested feature? | 2.5 | Core creation, editing, persistence, interaction, export, and API33 runtime flows execute, but the fresh Data screenshot shows missing row values, the renderer has no negative zero baseline, and production callback defaults can drop/throw chart actions. |
| Verification | Did the required checks actually run, with evidence? | 3.5 | JVM, Kover, static checks, 168 connected tests, real platform boundary, five active screenshot pulls, and both evidence contracts ran. The visual comparison failed major findings; API24/API34 runtime boundaries and a `ui_verification.json` artifact are absent; several tests do not assert complete outcomes. |
| Scope discipline | Did the session stay inside the chosen feature scope? | 4.5 | The implementation and evaluator artifacts stay within chart-block/editor/export scope. The only gate failure outside the diff is the pre-existing dynamic-tag issue in unchanged legacy cards. |
| Reliability | Does the result survive restart or rerun without repair? | 2.5 | Full JVM and connected reruns are green, and the real Android boundary is exercised. Negative values, large tables, rotation, complete persistence, synchronous bitmap work, and the visually broken Data grid remain reliability risks. |
| Maintainability | Is the code and documentation clear enough for the next session? | 2.5 | Workspace documentation and handoff are detailed, but `ChartBlockCard.kt` is 1,190 lines, parsing/render work occurs in the Composable, chart tags are not ID-scoped, and new screen callback defaults violate the no-dummy-code rule. |
| Handoff readiness | Can a fresh session continue work from repo artifacts only? | 4.0 | Required contract, feature list, progress, handoff, checklist, code review, test review, visual review, and this rubric are present. Some generator checklist/evidence notes overstate the quality-gate and visual outcome, and the active slice lacks a machine-checked UI verification JSON. |
| Code & Test Review | Do the code quality checks and comprehensive test reviews pass? | 2.5 | Assemble, ktlint, Detekt, Android Lint, localization, architecture, and assertion-quality checks pass. The Compose gate exits 1 on 22 unchanged legacy dynamic tags; ViewModel line coverage is 80.5% (<90%), renderer JVM coverage is 0%, and the test/code reviews identify material unverified paths. |

### Overall: 3.1 / 5

The arithmetic mean of the seven category scores is 3.14, reported to one decimal place. This is below the perfect-score threshold, so the verdict is `Revise` and the feature must transition to `To be fixed`.

### Platform Hard Gate

- Platform capability matrix present and linked from `feature_list.json`: Yes
- Minimum, target, and important API boundaries explicitly tested: No — the real runtime evidence is API 33; API 24 minimum and API 34 target runtime boundaries are not independently exercised.
- Unsupported environment policy is `fail_loudly`: Yes
- Real instrumented platform-boundary test passed: Yes — `ChartPlatformBoundaryTest#testProductionCanvasBitmapAndPdfDocumentBoundary` passed on `emulator-5554` API 33.
- Fake-only or JVM-only evidence used as the sole platform proof: No

The platform matrix and real boundary test are valid, but the API boundary evidence is not complete; this independently prevents a 5.0 score.

### Visual Verification Hard Gate

- Dedicated `ChartVisualFlowTest.kt` exists and captures screenshots in-test via `takeScreenshot()` during `waitForIdle()`: Yes
- No post-test CLI screencaps (`&& adb exec-out screencap`) in `feature_list.json` verification or evidence commands: Yes
- `ui_verification.json` present and passes `check-ui-verification-artifact.sh`: No — no active chart-workspace UI verification JSON is present; the evaluator outcome is recorded in `visual_evidence/evaluator-visual-verification.md`.
- `reference-anchor-verification.md` references `ChartVisualFlowTest` methods in Runtime proof column: Yes
- `check-visual-evidence-contract.sh` exits 0: Yes

The evidence contract is mechanically valid, but the direct visual review found major unresolved Data-grid and tooltip issues and incomplete first-level Options screenshot proof. Verification therefore remains below 5.0.

### Harness File Assessment

| File | Present | Quality | Notes |
|------|---------|---------|-------|
| `feature_list.json` | Yes | Substantive but needs correction | All four slices are marked passing with exact commands and platform/visual metadata, but several evidence claims exceed what their assertions prove. |
| `progress.md` | Yes | Detailed but stale in places | Records implementation sessions and coverage; it predates the evaluator’s findings and says the feature is ready for review. |
| `session-handoff.md` | Yes | Strong | Gives current commit, runtime, commands, evidence paths, and known API-level caveat; it also correctly notes the unavailable Skill tool. |
| `clean-state-checklist.md` | Yes | Broad but partially stale | Covers build, quality, platform, visual, and lifecycle checks, but records Compose checks as passing although the current repository-wide checker exits 1. |
| `evaluator-rubric.md` | Yes | Complete | This report includes quantitative scoring, platform/visual hard gates, harness assessment, verdict, and required follow-up. |

## Verdict

- Accept
- **Revise**
- Block

## Required Follow-Up

### Missing evidence

- Add an active-slice `ui_verification.json` (or update the workflow artifact contract) and make the visual verification result machine-checkable.
- Capture first-level Options and second-level Data column states separately; the current Options screenshot only shows the second-level sheet.
- Add runtime evidence at the API 24 minimum and API 34 target boundary, or explicitly document a supported-device matrix that makes the missing runtimes fail loudly.
- Assert actual PDF title/image placement and PDF table fallback content, not only non-empty page output.
- Raise the modified ViewModel scope to the project’s 90% target and add meaningful JVM coverage for `ChartBitmapRenderer` or an equivalent testable rendering seam.

### Required fixes

- Fix the Data view so normalized data rows render with their values and do not expand into an oversized blank grid; add visible row-text and scrolling assertions.
- Implement a zero baseline/domain for negative Bar and Line values.
- Replace production chart callback no-op/error defaults in `NoteEditorScreenContent` with required callbacks or real editor-backed behavior.
- Make chart test tags include stable persisted block/column IDs, tag every nested interactive control, and make the tooltip dismiss target at least 48 dp without overlap.
- Replace equal-width full-height datum overlays with hit targets that correspond to the rendered Bar/Line/Pie geometry, or document and verify an accessible alternative.
- Add tests for focused insertion, full ChartBlock round-trip fields, selected-column immutability, title/insertion/conversion auto-save, read-only ViewModel guards, all-zero/header-only/duplicate-header/large-table cases, and complete export fallback behavior.

### Next review trigger

The Generator should run the harness-fix workflow, resolve every finding in `code_review_chart-block.md`, `test_review_chart-block.md`, and `visual_evidence/evaluator-visual-verification.md`, update each finding’s status/evidence, rerun all required gates, and resubmit the feature as `To be reviewed`. A subsequent Evaluator run should confirm the fixes before human review.

## Generator Fix Pass — 2026-08-23

The required follow-up was completed in Harness Fix Mode. The original score and findings remain preserved above; this section records the re-verification outcome.

- Implementation/evidence commit: 7545d61.
- Code review reconciliation: 16/16 findings fixed, 0 unresolved.
- Test review reconciliation: all 17 functional requirements, 15 acceptance criteria, and revision edge/conditional rows have Fix Status entries; 0 unresolved.
- Visual reconciliation: VIS-01 through VIS-04 fixed; active-window screenshots and ui_verification.json pass their validators.
- Fresh clean coverage: 83.569% application line coverage; NoteEditorViewModel 96.5% line; ChartTableParser 96.2% line; ChartBitmapRenderer 95.1% line.
- Fresh regression: 437 JVM tests and 172 connected Android tests pass with zero failures/errors/skips; chart package is 16/16 on emulator-5554 API33.
- Fresh quality/evidence: assembleDebug, ktlintCheck, detekt, lint, Compose/localization/architecture/assertion-quality checks, lifecycle, platform evidence, visual evidence, and UI artifact checks all exit 0.
- Platform limitation: API24 and API34 direct runtime images are not provisioned in this workspace. The capability matrix explicitly records API33 verification, targetSdk34 build verification, and the required human-review environments under fail-loudly policy.

> **Fix Pass Verdict:** All evaluator findings are addressed and the feature is routed to human review; no new score is asserted before the next independent evaluator pass.
