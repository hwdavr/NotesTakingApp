# Evaluator Rubric — note-emoji

Date: 2026-08-15
Evaluator: Evaluator agent
Commit: `a23887f4dd50e7216a72eb6253d4d42559c50b53`
Workspace: `docs/product/2026-08-15-note-emoji/`

## Evidence Basis

Stages 1–4 were completed in order. The evaluator read the workflow, sprint contract, feature list, specification, progress log, session handoff, platform capability matrix, design system, feature design, harness rules, review templates, and required review skills. The lifecycle and platform-evidence checks passed.

Fresh Stage 4 evidence on `emulator-5554` / `sdk_gphone64_arm64` / API 33:

- `./gradlew testDebugUnitTest --rerun-tasks` — exit 0; fresh JVM/unit and integration suite passed.
- `./gradlew koverLog` — exit 0; application line coverage `82.5978%`.
- `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest` — exit 0; 85/85 tests passed, 0 skipped/failed.
- The declared `EmojiPickerPlatformTest#unicodeEmojiHasGlyphOnAndroidRuntime` — exit 0; 1/1 real Android boundary test passed.
- The three declared visual flows — exit 0; fresh non-empty PNGs were pulled into `visual_evidence/`.
- `assembleDebug`, `ktlintCheck`, `detekt`, `lintDebug`, Compose/localization/architecture checks, suppression audit, and `git diff --check` — all independently passed.

The detailed findings and requirement traceability are in `code_review_note-emoji.md` and `test_review_note-emoji.md`. Passing runtime gates establish the normal and platform paths; they do not prove the missing failure, lifecycle, downstream, and production callback-chain cases.

## Category Scores

| Category | Question | Score (0-5) | Evidence-based notes |
| --- | --- | ---: | --- |
| Correctness | Does the implemented behavior match the requested feature? | 3.5 | Normal insertion, catalog browsing/search, skin-tone mapping, Recent persistence, read-only behavior, and visual states are implemented and runtime-verified. Material defects remain: five production callback parameters default to silent no-ops, and catalog failure state is not rendered; share/PDF and full production variant wiring are not proven. |
| Verification | Did the required checks actually run, with evidence? | 3.5 | Fresh JVM, coverage, 85-test connected suite, real `Paint.hasGlyph` boundary, three visual captures, static gates, and lifecycle/platform scripts pass. Required behavior coverage is incomplete where tests substitute test-owned callbacks or omit Recent read failure, lifecycle recreation/dismissal, share/PDF, and missing-glyph preservation. |
| Scope discipline | Did the session stay inside the chosen feature scope? | 4.5 | The change is focused on Note Emoji, reuses existing note serialization, adds no API/Room/schema/permission surface, and has no unrelated production changes. The generic tag convention deviates from the approved feature design but remains feature-local. |
| Reliability | Does the result survive restart or rerun without repair? | 3.5 | Fresh reruns are deterministic, DataStore MRU persistence is covered, and the real runtime is available. Error recovery and lifecycle behavior are not fully demonstrated; a catalog error can produce a blank results area and Recent read failure has no injected test. |
| Maintainability | Is the code and documentation clear enough for the next session? | 3.5 | Data/domain/presentation boundaries, localized resources, semantic colors, stable keys, and no new suppressions are good. Silent callback defaults, unused/unrendered state, generic item tags, and stale “no unresolved issue” handoff claims require cleanup. |
| Handoff readiness | Can a fresh session continue work from repo artifacts only? | 3.5 | The workspace contains the contract, spec, feature list, progress, handoff, clean-state checklist, visual evidence, and both detailed review reports. The handoff and checklist overstate completeness and contain stale coverage/downstream claims, so a fresh session needs the evaluator reports to identify the actual next work. |
| Code & Test Review | Do code quality checks and comprehensive reviews pass? | 3.0 | Ktlint, Detekt, Android Lint, Compose/localization/architecture scripts, build, coverage threshold, and connected tests pass. The code review has three required production findings and the test review has multiple required evidence gaps, so this is not merge-ready. |

### Overall: 3.6 / 5

Calculation: `(3.5 + 3.5 + 4.5 + 3.5 + 3.5 + 3.5 + 3.0) / 7 = 3.57`, rounded to one decimal.

## Platform Hard Gate

- Platform capability matrix present and linked from `feature_list.json`: **Yes**
- Minimum, target, and important API boundaries explicitly tested: **Yes** — API 24 minimum/target 34 are declared, with the available API 33 runtime boundary executed.
- Unsupported environment policy is `fail_loudly`: **Yes**
- Real instrumented platform-boundary test passed: **Yes** — `Paint.hasGlyph` test passed on the emulator.
- Fake-only or JVM-only evidence used as the sole platform proof: **No**

The platform hard gate passes. The overall score remains below 5.0 because the feature has non-platform correctness and verification findings.

## Harness File Assessment

| File | Present | Quality | Notes |
|------|---------|---------|-------|
| `feature_list.json` | Yes | Substantial, needs correction | All three slices are marked passing with exit-0 acceptance evidence and the real platform contract. Some evidence descriptions overstate production event-chain and downstream share/PDF coverage; update them when fixes land. |
| `progress.md` | Yes | Detailed, needs correction | Provides a usable session history and command inventory, but records “no unresolved issue” despite the findings identified by this evaluation. |
| `session-handoff.md` | Yes | Detailed, needs correction | Identifies the next evaluator step and preserves important invariants, but claims no required path is unverified and repeats an overbroad persistence/export claim. |
| `clean-state-checklist.md` | Yes | Structured, stale in places | Covers build, architecture, runtime, testing, and documentation gates, but contains older coverage values and claims share/PDF behavior was proven by a test that does not invoke both paths. |
| `evaluator-rubric.md` | Yes | Complete | This rubric records the score, hard-gate decision, harness-file assessment, verdict, and follow-up. |

## Verdict

**Revise**

The feature is not blocked: the emulator and required build/test infrastructure are available, and the fixes are local to the repository. It is not acceptable for human review yet because the overall score is below 5.0 and the required findings are actionable.

## Required Follow-Up

### Required fixes

1. Remove the five production no-op callback defaults in `NoteEditorScreenContent`, or make the event boundary non-optional with explicit behavior at every call site.
2. Render a localized, recoverable catalog-error/empty-category state rather than allowing a failed category load to fall through to a blank grid.
3. Replace generic category/item/variant tags with a stable, unique convention that satisfies both the feature design and the repository’s tag rules; document an approved convention if the dynamic-tag rule needs a source-level adjustment.
4. Add a production-wiring test that selects a default and a skin-tone variant through the shipped screen, verifies `NoteEditorViewModel.insertEmoji`, and verifies Recent recording as one user flow.
5. Add injected DataStore read-failure/corrupt-preference coverage and assert empty Recent remains recoverable while catalog browsing/insertion remains usable.
6. Exercise the existing share payload mapper and PDF exporter in the Unicode persistence integration test, or explicitly narrow the acceptance contract with an approved requirement change.
7. Add sheet close, scrim/back dismissal, and configuration/process recreation coverage for the required lifecycle behavior.
8. Add a missing-glyph code-point preservation test or a documented, test-backed rationale proving the platform fallback behavior.
9. Update `feature_list.json`, `progress.md`, `session-handoff.md`, and `clean-state-checklist.md` so their claims match the resolved evidence, then rerun all quality/platform/runtime gates.

### Missing evidence

- Recent repository read failure/corrupt preference.
- Shipped screen → ViewModel insertion → Recent recording for default and skin-tone selection.
- Existing share payload and PDF export execution/assertions.
- Sheet dismissal/back/scrim and configuration/process recreation.
- Missing-glyph Unicode preservation.
- Unique per-element picker test tags as required by the approved feature design.

### Next review trigger

When all required fixes are implemented, the two detailed review reports are updated with resolved evidence, the harness artifacts no longer overclaim coverage, all gates pass, and the feature returns to `To be reviewed`, rerun `harness-evaluation` Stage 1 and continue through Stage 5.
