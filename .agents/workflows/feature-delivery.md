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
**The Implementation Plan must be approved before any code is written.**

Pipeline: Requirement, Impact & Design → Plan → [User Approval] → Implementation → Code Review → Testing → Test Review → Knowledge

---

## Stage Execution

### Stage — Requirement, Impact & Design Analysis
Load: `stages/requirement-analysis.md`
Output: `docs/changes/<name>/request_analysis/spec.md`, `tasks.md`, `summary.md`
Gate: requirements clear, impacted files identified, API classified, UiState/Navigation designed

### Stage — Implementation Plan ⛔ STOP
Load: `stages/implementation-plan.md`
Output: `docs/changes/<name>/coding/implementation_plan.md`
Gate: **STOP — present plan to user. Do not proceed until user explicitly approves.**

### Stage — Implementation (Data + Domain + UI)
Load: `stages/implementation.md`
Output: All source files across Data, Domain, and UI layers. `coding/coding_report_v<N>.md`
Gate: `./gradlew assembleDebug` passes, all layer rules satisfied

### Stage — Code Review ⛔ STOP
Load: `stages/code-review.md`
Output: `docs/changes/<name>/coding/review/code_review_v<N>.md`
Gate: ktlint/detekt passes, architecture and design compliance verified
**STOP — present `code_review_v<N>.md` to user. Do not proceed to Testing until user explicitly approves.**

### Stage — Testing
Load: `stages/testing.md`
Output: Unit tests, integration tests, shared JSON scenarios
Gate: tests pass, coverage targets met

### Stage — Test Review ⛔ STOP
Load: `stages/test-review.md`
Output: `docs/changes/<name>/coding/review/test_review_v<N>.md`
Gate: overall coverage ≥ 80%, shared scenarios used, regressions verified
**STOP — present `test_review_v<N>.md` to user. Do not proceed to Knowledge Capture until user explicitly approves.**

### Stage — Knowledge Capture
Load: `stages/knowledge-capture.md`
Output: ADRs, past-bugs, pitfalls, finalized `summary.md`
Gate: all knowledge artifacts produced

---

## Rollback Routes

| Failure | Return to |
|---------|-----------|
| Compilation error or Code quality/Architecture violation | Implementation (Data + Domain + UI) |
| Test failure or Coverage gap | Testing |
| Requirement ambiguity or Plan rejection | Requirement, Impact & Design Analysis |

---

## Human-in-the-Loop Confirmation Points

1. **After Requirement, Impact & Design Analysis** — user confirms assumptions and designs
2. **After Implementation Plan** — user approves implementation plan *(mandatory always)*
3. **After Code Review** — user reviews the code before testing
4. **After Test Review** — user reviews the full change before merge