---
description: You are a senior Android developer delivering a new feature end-to-end.
---

# Workflow: Feature Delivery

## When to use
- Implementing a new feature
- Enhancing an existing feature
- Integrating a backend or API change

This workflow is for production-grade delivery — not quick prototyping.

---

## Core Principle

Do not jump directly into coding.
**The implementation plan (Stage 02) must be approved before any code is written.**

Pipeline: Requirement, Impact & Design → Plan → [User Approval] → Implementation → Code Review → Testing → Test Review → Knowledge

---

## Stage Execution

### Stage 01 — Requirement, Impact & Design Analysis
Load: `stages/requirement-analysis.md`
Output: `docs/changes/<name>/request_analysis/spec.md`, `tasks.md`, `summary.md`
Gate: requirements clear, impacted files identified, API classified, UiState/Navigation designed

### Stage 02 — Implementation Plan ⛔ STOP
Load: `stages/implementation-plan.md`
Output: `docs/changes/<name>/coding/implementation_plan.md`
Gate: **STOP — present plan to user. Do not proceed until user explicitly approves.**

### Stage 03 — Implementation (Data + Domain + UI)
Load: `stages/implementation.md`
Output: All source files across Data, Domain, and UI layers. `coding/coding_report_v<N>.md`
Gate: `./gradlew assembleDebug` passes, all layer rules satisfied

### Stage 04 — Code Review
Load: `stages/code-review.md`
Output: `docs/changes/<name>/coding/review/code_review_v<N>.md`
Gate: ktlint/detekt passes, architecture and design compliance verified

### Stage 05 — Testing
Load: `stages/testing.md`
Output: Unit tests, integration tests, shared JSON scenarios
Gate: tests pass, coverage targets met

### Stage 06 — Test Review
Load: `stages/test-review.md`
Output: `docs/changes/<name>/coding/review/test_review_v<N>.md`
Gate: overall coverage ≥ 80%, shared scenarios used, regressions verified

### Stage 07 — Knowledge Capture
Load: `stages/knowledge-capture.md`
Output: ADRs, past-bugs, pitfalls, finalized `summary.md`
Gate: all knowledge artifacts produced

---

## Rollback Routes

| Failure | Return to |
|---------|-----------|
| Compilation error | Stage 03 (Implementation) |
| Code quality/Architecture violation | Stage 03(Implementation)
| Test failure/Coverage gap | Stage 05 (Testing) |
| User rejects plan | Stage 01 (Requirement, Impact & Design Analysis) |
| Requirement ambiguity | Stage 01 (Requirement, Impact & Design Analysis) |

---

## Human-in-the-Loop Confirmation Points

1. **After Stage 01** — user confirms assumptions and designs
2. **After Stage 02** — user approves implementation plan *(mandatory always)*
3. **After Stage 04** — user reviews the code before testing
4. **After Stage 06** — user reviews the full change before merge