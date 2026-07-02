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
Load: `skills/requirement-analysis/SKILL.md`

Adapt:
- API impact classification
- Design DTO and Domain model changes

### Stage 2 — Implementation Plan ⛔ STOP ✅ Always
Load: `skills/implementation-plan/SKILL.md`

**Stop and present the plan. Do not proceed until approved.**

### Stage 3 — Data Layer ✅ Always
Load: `skills/android-data-layer/SKILL.md`

### Stage 4 — Domain Layer ✅ Always
Load: `skills/android-domain-layer/SKILL.md`

### Stage 5 — UI Layer ⏭️ Skip if no UI changes
Load: `skills/android-ui-layer/SKILL.md`

Only run this stage if the contract change surfaces in the UI (new fields displayed, new screens, changed error states).

### Stage 6 — Testing ✅ Always
Load: `skills/android-testing/SKILL.md`

Mandatory: at least one integration test per changed API endpoint using shared JSON scenarios. See `testing-strategy.md`.

### Stage 7 — Code Quality Fix ⚠️ Lightweight if Data & Domain only
Load: `skills/code-quality-fix/SKILL.md`

Run the code-quality-fix stage to verify complete baseline correctness.

Scope guidance:
- Code Quality: run static analysis checks (Ktlint, Detekt, Lint).
- Skip UI-related scripts or rules if Stage 5 was skipped.

### Stage 8 — Knowledge Capture ⏭️ Skip unless change is non-obvious
Load: `skills/knowledge-capture/SKILL.md`

Only run if the contract change involves a tricky mapping, a breaking change, a non-standard pattern, or a decision future agents need to understand.
