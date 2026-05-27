---
description: You are a senior Android developer handling an API contract change.
---

# Workflow: API Contract Update

## When to use
Use this workflow when:
- A backend API changes its request or response contract

---

## Scope

Before starting, identify your scope:

| Scope | Stages to run |
|-------|--------------|
| **Full** (contract + repo + UI + tests) | All stages |
| **Data & Domain only** (contract + repo, no UI changes) | 1 → 2 → 3 → 4 → 6 → 7 (lightweight). Skip 5, 8. |

---

## Stages

### Stage 1 — Requirement, Impact & Design Analysis ✅ Always
Load: `stages/requirement-analysis.md`

Adapt:
- API impact classification
- Design DTO and Domain model changes

### Stage 2 — Implementation Plan ⛔ STOP ✅ Always
Load: `stages/implementation-plan.md`

**Stop and present the plan. Do not proceed until approved.**

### Stage 3 — Data Layer ✅ Always
Load: `stages/data-layer.md`

### Stage 4 — Domain Layer ✅ Always
Load: `stages/domain-layer.md`

### Stage 5 — UI Layer ⏭️ Skip if no UI changes
Load: `stages/ui-layer.md`

Only run this stage if the contract change surfaces in the UI (new fields displayed, new screens, changed error states).

### Stage 6 — Testing ✅ Always
Load: `stages/testing.md`

Mandatory: at least one integration test per changed API endpoint using shared JSON scenarios. See `testing-strategy.md`.

### Stage 7 — Code + Test Review ⚠️ Lightweight if Data & Domain only
Load: `stages/review.md`

- Part A (Code Review): run `android-code-quality-checks` (Ktlint, Detekt, Lint) — always required. Reasoning review focuses on DTO mapping, null safety, and error handling. Skip UI axes if Stage 5 was skipped.
- Part B (Test Review): verify tests assert the correct layer (`expected.domain` for repo/use case tests). Skip UI test review if Stage 5 was skipped.

### Stage 8 — Knowledge Capture ⏭️ Skip unless change is non-obvious
Load: `stages/knowledge-capture.md`

Only run if the contract change involves a tricky mapping, a breaking change, a non-standard pattern, or a decision future agents need to understand.
