# Harness Retrospective — Slice Platform Gate

## Incident

- **Trigger**: Stage 5 for `docs/product/2026-08-15-note-emoji/` US-1 ran `bash scripts/check-platform-evidence.sh docs/product/2026-08-15-note-emoji --evaluate`.
- **Observed evidence**: the command exited 1 with `FAIL: real platform test TC-US-3-REAL-UNICODE has no successful connected-test evidence`.
- **Affected stage**: `harness-generator` Stages 5 and 7 prevented US-1 from passing before US-3, despite the approved delivery order requiring US-1 to pass before US-2 and US-3.

## Classification And Root Cause

**Classification**: `WORKFLOW_GAP`.

`platform_validation` is feature-wide, while the real boundary test is explicitly assigned to US-3 (`TC-US-3-REAL-UNICODE`). The generator applied the feature-wide evaluation gate to every slice, contradicting the slice dependency order.

## Invariant

A slice that does not own a declared real platform-boundary test validates the capability contract without requiring evidence owned by a later slice; boundary-owning slices and final feature evaluation still require successful real-runtime evidence.

## Harness Change

- `scripts/check-platform-evidence.sh` now accepts `--slice <slice-id>` and defers runtime evidence only when that slice has no matching real-boundary test in `acceptance_test_ids`.
- `.agents/workflows/harness-generator.md` now uses the slice-scoped command during implementation and reserves the full-feature command for the boundary owner and final evaluation.
- `docs/templates/sprint-contract-template.md` records the same ownership rule for future plans.
- `scripts/tests/platform-evidence-contract-test.sh` proves both sides of the invariant: a non-owner defers correctly, while a boundary owner without evidence is rejected.

## Verification

| Command | Result |
|---|---|
| `bash -n scripts/check-platform-evidence.sh scripts/tests/platform-evidence-contract-test.sh` | Exit 0 |
| `bash scripts/tests/platform-evidence-contract-test.sh` | Exit 0 — `PASS: platform evidence validator defers non-owning slices and rejects ... boundary owners without evidence.` |
| `bash scripts/check-feature-lifecycle.sh` | Exit 0 — `Feature lifecycle tracker valid: 2 feature(s), 1 in progress.` |
| `bash scripts/check-platform-evidence.sh docs/product/2026-08-15-note-emoji --planning` | Exit 0 |
| `bash scripts/check-platform-evidence.sh docs/product/2026-08-15-note-emoji --evaluate --slice US-1` | Exit 0 — `slice US-1 does not own a declared real platform boundary test; full-feature evidence is deferred.` |
| `git diff --check` | Exit 0 |

## Routed Items And Remaining Risk

- US-3 still owns `TC-US-3-REAL-UNICODE` and must run the real `Paint.hasGlyph` test before it can pass or the feature can be evaluated. This repair does not weaken that gate.
- No product requirements, application source behavior, tracker status, slice selection, or implementation authorization were changed by this retrospective.
