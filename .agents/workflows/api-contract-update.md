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
**INVOKE** the `requirement-analysis` skill via the Skill tool (name: `requirement-analysis`). Reading the SKILL.md manually is not a substitute — the Skill tool is the required mechanism.

Adapt:
- API impact classification
- Design DTO and Domain model changes
- Run `bash scripts/check-stage-artifacts.sh api-contract-update requirement-analysis` — must exit 0.

### Stage 2 — Implementation Plan ⛔ STOP ✅ Always
**INVOKE** the `implementation-plan` skill via the Skill tool (name: `implementation-plan`). Reading the SKILL.md manually is not a substitute — the Skill tool is the required mechanism.

Run `bash scripts/check-stage-artifacts.sh api-contract-update implementation-plan` — must exit 0.
**Stop and present the plan. Do not proceed until approved.**

### Stage 3 — Data Layer ✅ Always
**INVOKE** the `android-data-layer` skill via the Skill tool (name: `android-data-layer`). Reading the SKILL.md manually is not a substitute — the Skill tool is the required mechanism.

### Stage 4 — Domain Layer ✅ Always
**INVOKE** the `android-domain-layer` skill via the Skill tool (name: `android-domain-layer`). Reading the SKILL.md manually is not a substitute — the Skill tool is the required mechanism.

### Stage 5 — UI Layer ⏭️ Skip if no UI changes
**INVOKE** the `android-ui-layer` skill via the Skill tool (name: `android-ui-layer`). Reading the SKILL.md manually is not a substitute — the Skill tool is the required mechanism.

Only run this stage if the contract change surfaces in the UI (new fields displayed, new screens, changed error states).

### Stage 6 — Testing ✅ Always
**INVOKE** the `android-testing` skill via the Skill tool (name: `android-testing`). Reading the SKILL.md manually is not a substitute — the Skill tool is the required mechanism.

Mandatory: at least one integration test per changed API endpoint using shared JSON scenarios. See `testing-strategy.md`.

### Stage 7 — Code Quality Fix ⚠️ Lightweight if Data & Domain only
**INVOKE** the `code-quality-fix` skill via the Skill tool (name: `code-quality-fix`). Reading the SKILL.md manually is not a substitute — the Skill tool is the required mechanism.

Run the code-quality-fix stage to verify complete baseline correctness.

Scope guidance:
- Code Quality: run static analysis checks (Ktlint, Detekt, Lint).
- Skip UI-related scripts or rules if Stage 5 was skipped.

### Stage 8 — Knowledge Capture ⏭️ Skip unless change is non-obvious
**INVOKE** the `knowledge-capture` skill via the Skill tool (name: `knowledge-capture`). Reading the SKILL.md manually is not a substitute — the Skill tool is the required mechanism.

Only run if the contract change involves a tricky mapping, a breaking change, a non-standard pattern, or a decision future agents need to understand.
