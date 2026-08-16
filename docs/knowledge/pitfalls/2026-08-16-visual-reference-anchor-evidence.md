# Visual Reference Alignment Needs Visual-Bounds Evidence

## Symptom

A screenshot and a passing Compose test can both look reassuring while a visible handle, pill, overlay, or icon is still offset from the design line it is intended to meet.

## Cause

Touch targets are often deliberately larger than the visible shape. Testing only the target or an internal layout container does not prove the actual visual edge is aligned to the table, grid, sheet, or reference boundary. A report checkbox saying that a screenshot “matches” provides no traceable measurement.

## What To Do Instead

When an approved reference encodes a meaningful spatial relationship, give the visible shape a stable visual-bounds testTag and assert the relationship with Compose semantics bounds and an explicit density-derived tolerance. Record the approved asset, test method, visual tag, measured relation, and actual screenshot in the appropriate anchor-verification report. Run:

~~~bash
# Complex feature visual evidence
bash scripts/check-visual-evidence-contract.sh "$FEATURE_DIR"

# Ad-hoc UI workflow evidence
bash scripts/check-stage-artifacts.sh create-ui-and-verify ui-verification docs/current
~~~

Do not substitute the outer 48dp target for a smaller visual pill/icon when the reference concerns the visual's edge, center, or height.

## Reference

- docs/templates/visual-reference-anchor-verification-template.md
- docs/templates/ui-verification-template.md
- scripts/check-visual-evidence-contract.sh
- scripts/check-ui-verification-artifact.sh
- docs/product/2026-08-16-table-handles/visual_evidence/reference-anchor-verification.md
