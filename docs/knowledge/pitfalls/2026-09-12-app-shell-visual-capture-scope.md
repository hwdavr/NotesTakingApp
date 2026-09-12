# App-Shell Claims Need an App-Shell Capture

## Symptom

A full-page visual comparison looks correct even though global tabs, a system bar, or other
application chrome is present or absent incorrectly at runtime.

## Cause

A content Composable can be rendered directly in a `VisualFlowTest`. That bypasses the production
root such as `AppNavHost` and its `Scaffold`, which owns global navigation and other shell chrome.
A screenshot of the child therefore cannot prove the full screen.

## What To Do Instead

For a visual acceptance row that mentions global navigation, system bars, or a full-page shell,
declare `Capture scope: app-shell; production root: <ComposableOrActivity>.` in the sprint
contract. Capture from a dedicated `VisualFlowTest` that invokes that root and records the same
scope and root in the reference-anchor proof. Use `Capture scope: component` only when the shell
is explicitly outside the claim.

Run:

~~~bash
bash harness/scripts/tests/visual-evidence-contract-test.sh
bash harness/scripts/check-visual-evidence-contract.sh "$FEATURE_DIR" --planning
~~~

## Reference

- docs/changes/harness-retro-2026-09-12-web-bookmark-visual-evidence/retrospective.md
- harness/scripts/check-visual-evidence-contract.sh
- harness/templates/visual-reference-anchor-verification-template.md
