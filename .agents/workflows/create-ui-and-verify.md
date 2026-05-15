---
description: Implement or update the Android UI to match the provided screenshot.
---

# Workflow: Create UI and Verify

## When to use
Use this workflow when:
- Implementing a new screen from a design screenshot
- Updating an existing screen to match a revised design

---

## Stages

### Stage 1 — Requirement, Impact & Design Analysis
Run: `stages/01-requirement-analysis.md`

Adapt:
- Identify target components
- Design UiState and navigation

### Stage 2 — Implementation Plan ⛔ STOP
Run: `stages/02-implementation-plan.md`

**Stop and present the plan before implementing.**

### Stage 3–05 — Implementation
Run: `stages/03-data-layer.md`, `stages/04-domain-layer.md`, `stages/05-ui-layer.md`

### Stage 6 — Code Review (UI verification)
Run: `stages/06-code-review.md`

Perform:
- Build and Static Quality Checks
- Architecture & Design Validation (match against designs in Stage 01)
- Visual UI verification (screenshots/texts)

### Stage 7 — Testing
Run: `stages/07-testing.md`

### Stage 8 — Test Review
Run: `stages/08-test-review.md`

### Stage 9 — Knowledge Capture
Run: `stages/09-knowledge-capture.md`