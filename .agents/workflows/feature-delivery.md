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
Load: `stages/requirement-analysis.md`
Output: `docs/current/spec_adhoc.md`, `docs/current/summary_adhoc.md`
Gate: requirements clear, impacted files identified, API classified, UiState/Navigation designed

### Stage 2 — Implementation Plan ⛔ STOP
Load: `stages/implementation-plan.md`
Output: `docs/current/implementation_plan_adhoc.md`, `docs/current/test_plan_adhoc.md`
Gate: **STOP — present plan to user. Do not proceed until user explicitly approves.**

### Stage 3 — Implementation (Data + Domain + UI)
Load: `stages/implementation.md`
Output: All source files across Data, Domain, and UI layers.
Gate: `./gradlew assembleDebug` passes, all layer rules satisfied

### Stage 4 — Testing
Load: `stages/testing.md`
Output: Unit tests, integration tests, shared JSON scenarios.
Gate: tests pass, coverage targets met

### Stage 5 — Code Quality Fix
Load: `stages/code-quality-fix.md`
Output: All violations resolved, `summary_adhoc.md` updated.
Gate: `ktlintCheck`, `detekt`, `lintDebug`, and all custom check scripts exit with code 0

### Stage 6 — Knowledge Capture
Load: `stages/knowledge-capture.md`
Output: ADRs, past-bugs, pitfalls, finalized `summary_adhoc.md`, updated `docs/current/progress.md` (marking the task as Complete)
Gate: all knowledge artifacts produced, task marked Complete in progress file (if active)

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