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

**Sliced Plan Pickup & Routing**:
- **If the user prompt outlines a specific, clear requirement**: Execute and perform analysis based directly on the user's explicit requirement.
- **If the user prompt asks to "execute the next task" (or if no explicit requirement is specified but an active sliced plan exists in `docs/current/progress.md`)**: Check the task progress in `docs/current/progress.md`, identify the next uncompleted task in `docs/current/task-list.md`, pick it up (set its progress status to `⏳ In Progress` in `docs/current/progress.md`), and read its objective, behavior, and scope details. Fulfill this pipeline specifically for that task.

Pipeline: Requirement, Impact & Design → Plan → [User Approval] → Implementation → Testing → [Human runs /review-and-fix] → Knowledge

---

## Stage Execution

### Stage 1 — Requirement, Impact & Design Analysis
Load: `stages/requirement-analysis.md`
Output: `docs/changes/<name>/request_analysis/spec_t<taskId>.md`, `summary_t<taskId>.md`
Gate: requirements clear, impacted files identified, API classified, UiState/Navigation designed

### Stage 2 — Implementation Plan ⛔ STOP
Load: `stages/implementation-plan.md`
Output: `docs/changes/<name>/coding/implementation_plan_t<taskId>.md`, `coding/test_plan_t<taskId>.md`
Gate: **STOP — present plan to user. Do not proceed until user explicitly approves.**

### Stage 3 — Implementation (Data + Domain + UI)
Load: `stages/implementation.md`
Output: All source files across Data, Domain, and UI layers. `coding/coding_report_t<taskId>_v<N>.md`
Gate: `./gradlew assembleDebug` passes, all layer rules satisfied

### Stage 4 — Testing
Load: `stages/testing.md`
Output: Unit tests, integration tests, shared JSON scenarios. `unit_test/test_report_t<taskId>_v<N>.md`
Gate: tests pass, coverage targets met

### Stage 5 — Code + Test Review ⛔ STOP
Do not run this stage yourself.

**STOP — inform the user that Stage 4 (Testing) is complete and instruct them to run the `/review-and-fix` workflow to perform the code and test review. Do not proceed to Stage 6 until the user explicitly confirms that the review is done and approved.**

### Stage 6 — Knowledge Capture
Load: `stages/knowledge-capture.md`
Output: ADRs, past-bugs, pitfalls, finalized `summary_t<taskId>.md`, updated `docs/current/progress.md` (marking the task as Complete)
Gate: all knowledge artifacts produced, task marked Complete in progress file (if active)

---

## Rollback Routes

| Failure | Return to |
|---------|-----------|
| Compilation error | Implementation (Data + Domain + UI) |
| Test failure or Coverage gap | Testing (fix implementation if needed, then re-test) |
| Architecture/rule violation found in Review | Implementation (Data + Domain + UI) |
| Test quality issue found in Review | Testing |
| Requirement ambiguity or Plan rejection | Requirement, Impact & Design Analysis |

---

## Human-in-the-Loop Confirmation Points

1. **After Requirement, Impact & Design Analysis** — user confirms assumptions and designs
2. **After Implementation Plan** — user approves implementation plan *(mandatory always)*
3. **After Testing** — agent stops and asks user to run `/review-and-fix`. Agent does NOT perform the review itself. *(mandatory always)*
4. **After `/review-and-fix` completes** — user explicitly confirms review is approved before agent proceeds to Knowledge Capture