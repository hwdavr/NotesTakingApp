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

Pipeline: Requirement, Impact & Design → Plan → [User Approval] → Implementation (Data → Domain → UI) → Code Review → Testing → Test Review → Knowledge

---

## Stage Execution

### Stage 01 — Requirement, Impact & Design Analysis
Load: `stages/01-requirement-analysis.md`
Output: `docs/changes/<name>/request_analysis/spec.md`, `tasks.md`, `summary.md`
Gate: requirements clear, impacted files identified, API classified, UiState/Navigation designed

### Stage 02 — Implementation Plan ⛔ STOP
Load: `stages/02-implementation-plan.md`
Output: `docs/changes/<name>/coding/implementation_plan.md`
Gate: **STOP — present plan to user. Do not proceed until user explicitly approves.**

### Stage 03 — Data Layer
Load: `stages/03-data-layer.md`
Output: DTOs, entities, DAOs, mapper, repository implementation
Gate: `./gradlew assembleDebug` passes, no DTOs outside data layer

### Stage 04 — Domain Layer
Load: `stages/04-domain-layer.md`
Output: Domain models, use cases, repository interfaces
Gate: `./gradlew assembleDebug` passes, no Android imports in domain

### Stage 05 — UI Layer
Load: `stages/05-ui-layer.md`
Output: ViewModel, UiState, Composable screens, navigation wiring
Gate: `./gradlew assembleDebug` passes, testTags added, UiState complete

### Stage 06 — Code Review
Load: `stages/06-code-review.md`
Output: `docs/changes/<name>/coding/review/code_review_v<N>.md`
Gate: ktlint/detekt passes, architecture and design compliance verified

### Stage 07 — Testing
Load: `stages/07-testing.md`
Output: Unit tests, integration tests, shared JSON scenarios
Gate: tests pass, coverage targets met

### Stage 08 — Test Review
Load: `stages/08-test-review.md`
Output: `docs/changes/<name>/coding/review/test_review_v<N>.md`
Gate: overall coverage ≥ 80%, shared scenarios used, regressions verified

### Stage 09 — Knowledge Capture
Load: `stages/09-knowledge-capture.md`
Output: ADRs, past-bugs, pitfalls, finalized `summary.md`
Gate: all knowledge artifacts produced

---

## Rollback Routes

| Failure | Return to |
|---------|-----------|
| Compilation error | Stage that introduced it (03 / 04 / 05) |
| Code quality/Architecture violation | Stage 06 (Code Review) |
| Test failure/Coverage gap | Stage 07 (Testing) |
| User rejects plan | Stage 02 (revise plan) |
| Requirement ambiguity | Stage 01 |

---

## Human-in-the-Loop Confirmation Points

1. **After Stage 01** — user confirms assumptions and designs
2. **After Stage 02** — user approves implementation plan *(mandatory always)*
3. **After Stage 06** — user reviews the code before testing
4. **After Stage 08** — user reviews the full change before merge