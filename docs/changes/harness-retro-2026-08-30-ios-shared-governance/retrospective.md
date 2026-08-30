# Harness Retrospective — iOS Shared Governance Migration

## Incident

The Android harness submodule was at 7a2ff5f while the sibling
NotesTakingAppiOS harness was at 07ea0d3. Their remotes are separate repositories,
so the iOS git commit could not be applied as an Android gitlink. The iOS harness
contained newer shared governance for rule applicability, strict gate halting,
method-scoped visual evidence, and weighted coverage enforcement.

## Classification and root cause

Classification: WORKFLOW_GAP.

Shared harness controls had been added to the iOS repository without corresponding
Android adaptations. The Android harness therefore had no single contract for all
nine rule decisions, no mechanical Kover XML threshold checker, and weaker checks
for visual test method ownership and artifact uniqueness.

## Invariant

Every Android requirement artifact carries nine explicit rule decisions; every
required gate stops on failure; visual evidence is produced by a dedicated
method-scoped VisualFlowTest; and coverage thresholds are calculated from
class-level Kover line counters without double-counting methods.

## Harness changes

- Added rule-applicability-template.md and requirement-summary-template.md, and
  attached the matrix to requirements, plans, review templates, workflows, and CI.
- Added rule-applicability-contract-test.sh and extended check-stage-artifacts.sh
  to reject missing or unsupported decisions.
- Added check-coverage.sh and coverage-contract-test.sh for weighted Kover XML
  coverage, package exclusions, and unique per-file thresholds.
- Tightened check-visual-evidence-contract.sh to require dedicated
  VisualFlowTest methods, method-scoped runner selectors, and unique screenshots;
  expanded its regression fixtures.
- Added gate-failure-stop-contract-test.sh and removed the remaining retry-policy
  language from the Android fix workflow.
- Updated the Android testing strategy, CI gate, and clean-state checklist to use
  the new coverage and navigation contracts.
- Preserved Android-specific platform evidence, localization, Compose, navigation,
  and Windows launcher implementations. iOS-only SwiftUI, Xcode, String Catalog,
  and Android-to-iOS migration files were intentionally not copied.

## Verification

- bash -n on all changed shell scripts — PASS.
- bash harness/scripts/tests/coverage-contract-test.sh — PASS.
- bash harness/scripts/tests/gate-failure-stop-contract-test.sh — PASS.
- bash harness/scripts/tests/rule-applicability-contract-test.sh — PASS.
- bash harness/scripts/tests/visual-evidence-contract-test.sh — PASS.
- bash harness/scripts/tests/keyboard-mockup-contract-test.sh — PASS after its
  fixture was updated with the newly required matrix.
- All existing Android harness contract tests — PASS.
- bash harness/scripts/check-coverage.sh app/build/reports/kover/reportDebug.xml
  — PASS, 83.57% (5142/6153).
- cmp -s AGENTS.md .harness/AGENTS.md — PASS.
- git diff --check — PASS.

## Remaining risk

No iOS product or harness files were modified. Android application source was not
changed, so the existing app build and test evidence remains valid, but the new
coverage checker should be run in CI after each Kover report generation.
