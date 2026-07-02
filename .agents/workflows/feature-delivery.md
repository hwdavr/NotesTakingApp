---
description: You are a senior Android developer delivering a new feature end-to-end.
---

# Workflow: Feature Delivery

## When to use
- Use this workflow when you are acting as the **Generator** (Implementer) agent.
- Implementing a new feature
- Enhancing an existing feature
- Integrating a backend or API change

This workflow is for production-grade delivery — not quick prototyping.

---

## Core Principle

Do not jump directly into coding.
**The Implementation Plan must be approved before any code is written.**

Pipeline: Requirement, Impact & Design → Plan → [User Approval] → Implementation → Testing → Code Quality Fix → Knowledge

---

## Stage Execution

### Stage 1 — Requirement, Impact & Design Analysis
Load: `skills/requirement-analysis/SKILL.md`

Output: `docs/current/spec_v<N>.md` created; `docs/current/summary_v<N>.md` updated with requirements, impacted files, API classification, and UiState/Navigation design.
Gate: requirements clear, impacted files identified, API classified, UiState/Navigation designed.

---

### Stage 2 — Implementation Plan ⛔ STOP
Load: `skills/implementation-plan/SKILL.md`

Output: `docs/current/implementation_plan_v<N>.md` created; `docs/current/test_plan_v<N>.md` created; `docs/current/summary_v<N>.md` updated.
Gate: **STOP — present plan to user. Do not proceed until user explicitly approves.**

---

### Stage 3 — Implementation (Data + Domain + UI)
Load: `skills/android-implementation/SKILL.md`

Output: All source files across Data, Domain, and UI layers created or modified; `docs/current/summary_v<N>.md` updated with Implementation stage marked complete.
Gate: `./gradlew assembleDebug` passes, all layer rules satisfied.

---

### Stage 4 — Testing
Load: `skills/android-testing/SKILL.md`

Output: Unit tests, integration tests, and shared JSON scenarios created or updated; `docs/current/summary_v<N>.md` updated with test count and coverage.
Gate: tests pass, coverage targets met.

---

### Stage 5 — Code Quality Fix
Load: `skills/code-quality-fix/SKILL.md`

Output: All violations resolved; `docs/current/summary_v<N>.md` updated with code quality results.
Gate: `ktlintCheck`, `detekt`, `lintDebug`, and all custom check scripts exit with code 0.

---

### Stage 6 — Knowledge Capture
Load: `skills/knowledge-capture/SKILL.md`

Output: ADRs, past-bugs, and pitfalls recorded; `docs/current/summary_v<N>.md` finalised; `docs/current/progress.md` updated marking the task as Complete.
Gate: all knowledge artefacts produced, task marked Complete in progress file.

---

## Rollback Routes

| Failure | Return to |
|---------|-----------|
| Requirement ambiguity or Plan rejection | Requirement, Impact & Design Analysis |
| Compilation error | Implementation (Data + Domain + UI) |
| Test failure or Coverage gap | Testing (fix implementation if needed, then re-test) |
| Quality check violation | Code Quality Fix (fix root cause, re-run checks) |

---

## Human-in-the-Loop Confirmation Points

1. **After Requirement, Impact & Design Analysis** — user confirms assumptions and designs
2. **After Implementation Plan** — user approves implementation plan *(mandatory always)*