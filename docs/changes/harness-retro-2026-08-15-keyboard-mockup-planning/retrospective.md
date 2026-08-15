# Harness Retrospective — Keyboard-Visible Planning Mockups

**Date**: 2026-08-15
**Affected stage**: `harness-planning/feature-specification` (and the repeated slice-planning artifact gate)
**Scope**: Planning artifacts and validation only; no application source or product behavior changed.

## Incident

The planning workflow required a `design.md` and visual mockup for UI work, but it did not require a second mockup for a bottom sheet containing text input when the keyboard was shown. The pre-fix command below accepted the active feature after checking only the specification, design-system link, and file presence:

```text
bash scripts/check-stage-artifacts.sh harness-planning feature-specification docs/product/2026-08-15-note-emoji
Stage 'harness-planning/feature-specification' artifacts present.
```

The missing invariant was exposed by the user’s keyboard-visible bottom-sheet design request.

## Classification and root cause

**Primary classification: `WORKFLOW_GAP`**

The desired planning rule did not exist in the authoritative workflow gate, template, or validator. The existing workflow only stated “visual mockup images” in `.agents/workflows/harness-planning.md`; `scripts/check-stage-artifacts.sh` did not inspect keyboard-dependent states or referenced mockup assets.

## Invariant added

> A planning design that contains a bottom sheet and a textbox, text field, search field, or other text input must describe a keyboard-visible state and reference distinct, non-empty base and keyboard-visible mockup assets.

## Harness changes

- Added `scripts/check-keyboard-mockup-contract.sh`, which detects the bottom-sheet/text-input combination, rejects a missing keyboard-visible state, requires one keyboard/IME-specific asset plus one non-keyboard asset, and verifies every referenced design asset is non-empty.
- Wired the validator into `scripts/check-stage-artifacts.sh` for both `harness-planning/feature-specification` and `harness-planning/slice-planning`. The feature-specification gate now also treats an existing `design.md` as UI planning evidence rather than relying only on the spec heading or a working-tree UI diff.
- Updated `.agents/workflows/harness-planning.md` with the required alternate-state output and gate behavior.
- Updated `docs/templates/feature-design-template.md` with the conditional keyboard-visible mockup contract and per-screen asset placeholder.
- Updated `.agents/gates/ci-checks.md` with the planning validator command.
- Added `scripts/tests/keyboard-mockup-contract-test.sh`. It covers a valid pair, a non-applicable bottom sheet, missing keyboard asset, missing state text, missing referenced file, and the actual feature-specification stage gate.

## Verification

All relevant harness checks passed:

```text
bash scripts/tests/keyboard-mockup-contract-test.sh
PASS: keyboard mockup validator accepts the required pair and rejects missing state, asset, and file cases.

bash scripts/check-stage-artifacts.sh harness-planning feature-specification docs/product/2026-08-15-note-emoji
PASS: bottom-sheet text-input design includes distinct base and keyboard-visible mockups.
Stage 'harness-planning/feature-specification' artifacts present.

bash scripts/check-stage-artifacts.sh harness-planning slice-planning docs/product/2026-08-15-note-emoji
PASS: bottom-sheet text-input design includes distinct base and keyboard-visible mockups.
Stage 'harness-planning/slice-planning' artifacts present.

bash scripts/tests/visual-evidence-contract-test.sh
PASS: visual evidence validator aligns visual methods, sprint-contract rows, acceptance IDs, and successful evidence.

bash scripts/check-feature-lifecycle.sh
Feature lifecycle tracker valid: 2 feature(s), 0 in progress.

bash -n scripts/check-keyboard-mockup-contract.sh scripts/tests/keyboard-mockup-contract-test.sh scripts/check-stage-artifacts.sh
PASS

git diff --check
PASS
```

The current feature already contains the required artifacts: `docs/product/2026-08-15-note-emoji/design.md` references `design/mockup_note_editor_emoji_picker.png` and `design/mockup_note_editor_emoji_picker_keyboard.png`, and its visual-state table defines “Keyboard-visible content”.

No Android build, unit, coverage, lint, or instrumented gates were rerun because this retrospective changes only harness scripts, workflow guidance, templates, and audit documentation; application source and tests are unchanged.

## Routed items

- The keyboard-visible picker implementation and its runtime behavior remain owned by the existing `note-emoji` feature work and were not changed here.
- No lifecycle transition, slice selection, implementation-plan regeneration, or tracker mutation was performed.
- The callable Skill tool was not exposed in this session; the complete local `harness-retrospective` skill instructions were read and followed, with the workflow limitation preserved here for reviewer visibility.

## Remaining risk

The validator intentionally uses the design document’s standard terminology and backticked `design/` image references. Designs using uncommon terms or nonstandard asset-link syntax may need the contract vocabulary expanded, but they cannot silently pass when they use the documented template form.
