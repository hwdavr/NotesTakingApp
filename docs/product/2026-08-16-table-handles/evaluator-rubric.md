# Evaluator Rubric

| Category | Question | Score (0-5) | Notes |
| --- | --- | ---: | --- |
| Correctness | Does the implemented behavior match the requested feature? | 3.5 | Core table transformations, handles, sheets, persistence, read-only behavior, target retention, and Delete-last ordering work in the tested flow. The production `onTableAction` default is a silent no-op, and the design-required configuration-change focus preservation is not implemented. |
| Verification | Did the required checks actually run, with evidence? | 4.0 | Fresh unit (356 tests), coverage (83.9635%), static gates, connected UI (16/16), and four sequential visual tests pass. Several required behaviors still lack direct evidence: clear-all UI, all-action dismissal, rendered fit-to-width, wide tables, accessibility bounds/descriptions, and rotation. |
| Scope discipline | Did the session stay inside the chosen feature scope? | 5.0 | Production changes are limited to table document state, editor handles/sheets, serialization, resources, and tests. No API, Room migration, navigation, or out-of-scope table features were added. |
| Reliability | Does the result survive restart or rerun without repair? | 3.5 | Full JVM/UI/visual reruns pass and row insertion survives editor reload. Rotation state is held in composable `remember` state, and the no-op callback path can silently lose actions. |
| Maintainability | Is the code and documentation clear enough for the next session? | 4.0 | Typed action dispatch, localized resources, focused sheet components, and detailed workspace reports are strong. The screen remains very large, new resource keys are inconsistently suffixed, the callback default is unsafe, and the generic clean-state checklist is stale relative to US-3. |
| Handoff readiness | Can a fresh session continue work from repo artifacts only? | 4.0 | Feature baselines, evidence, platform matrix, progress, handoff, test review, and code review are present. The current evaluator report exposes all follow-up work, but the required `clean-state-checklist.md` path is a US-1 artifact and the environment lacks a callable Skill endpoint. |
| Code & Test Review | Do the code quality checks and comprehensive test reviews pass? | 3.5 | Assemble, ktlint, Detekt, lint, Compose, localization, architecture, suppression, unit, Kover, connected, and visual gates pass. The code review has two required production findings and the test review has multiple revision-required traceability gaps. |

### Overall: 3.9 / 5

The score is the rounded mean of the seven category scores (27.5 / 7 = 3.93). It is below perfect because the review found real production and evidence gaps, despite all currently declared generator commands passing.

### Platform Hard Gate

- Platform capability matrix present and linked from `feature_list.json`: Yes
- Minimum, target, and important API boundaries explicitly tested: No — the feature is classified as not platform-bound, and the fresh runtime evidence is on API 33 only; no API 24/34 matrix run was declared.
- Unsupported environment policy is `fail_loudly`: Yes
- Real instrumented platform-boundary test passed: N/A — no special hardware, permission, model, network, or external Android service boundary is introduced.
- Fake-only or JVM-only evidence used as the sole platform proof: No — production-backed Compose tests pass on `emulator-5554`.

No special platform-boundary hard gate is triggered because `platform_validation.required` is explicitly false, but the missing API-boundary breadth is retained as a non-perfect verification note.

### Harness File Assessment

| File | Present | Quality | Notes |
|------|---------|---------|-------|
| `feature_list.json` | Yes | Complete | Three slices are marked passing with acceptance IDs, commands, and evidence; US-3 owns four visual evidence rows. |
| `progress.md` | Yes | Complete | Four implementation sessions record commands, scope decisions, risks, and handoff state. |
| `session-handoff.md` | Yes | Complete | Current feature state, commands, artifacts, device, and next owner are documented. It predates this independent evaluation and therefore does not include evaluator findings. |
| `clean-state-checklist.md` | Yes | Partial | The required path contains the US-1 checklist at commit `aa95f95`; the final US-3 checklist exists separately as `clean-state-checklist_US-3.md`. Consolidate or clearly index the final checklist at the required path. |
| `evaluator-rubric.md` | Yes | Complete | This report records category scores, platform gate, harness assessment, verdict, and follow-up. |

## Verdict

- Accept
- Revise
- Block

**Revise** — score-based lifecycle transition required.

## Required Follow-Up

- Missing evidence:
  - Production `Clear entire table` click with UI/document assertion.
  - Sheet dismissal assertions for delete and table-level actions, not only representative insert/clear actions.
  - Rendered equal-width fit-to-width assertion and toggle-back/default sizing coverage.
  - Large/wide-table handle boundary test.
  - Direct content-description and 48dp bounds assertions for handles/action rows.
  - Configuration-change/recreation test for focused-cell preservation, or an explicitly approved design change removing that requirement.
- Required fixes:
  - Remove the default `{}` from `NoteEditorScreenContent.onTableAction` or make the production callback mandatory/fail-fast; update rendering-only test callers with an explicit callback.
  - Persist focused table target state through the approved ViewModel/state path (or obtain and document a design exception); do not rely on non-saveable `remember` for a required configuration-preserved state.
  - Consolidate the final clean-state checklist under the required `clean-state-checklist.md` path.
  - Generator must update finding status in both `test_review_table-handles.md` and `code_review_table-handles.md` after fixes.
- Next review trigger: Run `.agents/workflows/harness-fix.md`, rerun all Stage 4 commands and gates, then request a new Evaluator pass. The feature must remain `To be fixed` until every finding is resolved and evidenced.

## Generator Fix-Pass Follow-Up — 2026-08-16

The Generator completed Fix-Stages 1–8. All 9 code-review findings and all 8 test-review findings are marked `Fixed ✅`; source/test commit `a0c9533` contains the implementation and test changes. Fresh Fix-Stage 5 evidence reports 359 JVM tests with 0 failures/errors, 84.027% application line coverage, 20/20 production table UI tests, four sequential visual captures, and passing quality/platform/visual/lifecycle gates. The tracker is now `To be human reviewed`; the original 3.9/5 score above remains the historical pre-fix evaluation and requires a new human/evaluator review.
