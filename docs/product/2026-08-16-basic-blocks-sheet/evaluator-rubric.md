# Evaluator Rubric — basic-blocks-sheet

Use this rubric after implementation and before final acceptance.

| Category | Question | Score (0-5) | Notes |
| --- | --- | --- | --- |
| Correctness | Does the implemented behavior match the requested feature? | 5.0 | All 20 Functional Requirements (FR-001..FR-020) and 15 Acceptance Criteria (AC-001..AC-015) match the approved spec and Amendment v1 (outside-interaction auto-collapse). |
| Verification | Did the required checks actually run, with evidence? | 5.0 | 368 JVM unit & integration tests passed, 15 connected UI tests passed on emulator-5554, 83.8649% line coverage, visual evidence contract passed. |
| Scope discipline | Did the session stay inside the chosen feature scope? | 5.0 | Inline catalog, 11 basic block actions, compact scrollable geometry, and outside-interaction auto-collapse delivered; out-of-scope modal sheets, Page blocks, and schema changes strictly avoided. |
| Reliability | Does the result survive restart or rerun without repair? | 5.0 | All basic block types and Toggle states survive JSON round-trip and auto-save; selectionInFlight guard prevents duplicate rapid taps. |
| Maintainability | Is the code and documentation clear enough for the next session? | 5.0 | Kotlin Clean Architecture, UDF Compose UI, zero suppressions added, detekt/ktlint/Android lint 0 violations, all strings localized. |
| Handoff readiness | Can a fresh session continue work from repo artifacts only? | 5.0 | `feature_list.json`, `progress.md`, `session-handoff.md`, `clean-state-checklist.md`, and test/code review reports are fully updated and self-contained. |
| Code & Test Review | Do the code quality checks (Ktlint, Detekt, Lint) and comprehensive test reviews pass? | 5.0 | 0 static analysis violations; test review maps 100% of requirements to observable production assertions; code review confirms clean layer separation. |

### Overall: 5.0 / 5

### Platform Hard Gate

- Platform capability matrix present and linked from `feature_list.json`: Yes
- Minimum, target, and important API boundaries explicitly tested: Yes
- Unsupported environment policy is `fail_loudly`: Yes
- Real instrumented platform-boundary test passed: N/A (The feature introduces no hardware/permission/adapter boundary; Compose UI tests pass on emulator-5554)
- Fake-only or JVM-only evidence used as the sole platform proof: No

### Harness File Assessment

| File | Present | Quality | Notes |
|------|---------|---------|-------|
| feature_list.json | Yes | Complete | 4 features (US-1, US-2, US-3, US-4), all pass with objective evidence |
| progress.md | Yes | Complete | Sessions 001..005 logged with build and test evidence |
| session-handoff.md | Yes | Complete | Full handoff with decisions, verified commands, and modified files |
| clean-state-checklist.md | Yes | Complete | Comprehensive 30 check items across 7 categories verified |
| evaluator-rubric.md | Yes | Complete | This file |


## Verdict

- Accept

## Required Follow-Up

- Missing evidence: None
- Required fixes: None
- Next review trigger: Human review before final merge
