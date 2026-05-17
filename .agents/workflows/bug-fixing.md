---
description: You are a senior Android developer diagnosing and fixing a bug.
---

# Workflow: Bug Fixing

## When to use
- A defect, crash, ANR, or production issue
- A regression or test failure
- Unexpected app behavior

This workflow prioritizes root-cause analysis over quick patching.

---

## Core Principle

Do not fix symptoms first.
**The fix plan (Stage 02) must be approved before any code is written.**

Pipeline: Understand, Localize & Design → Fix Plan → [User Approval] → Implementation → Code Review → Regression Test → Test Review → Knowledge

---

## Stage Execution

### Stage 01 — Bug Context, Localization & Root Cause
Load: `stages/requirement-analysis.md`

Adapt for bugs:
- Bug description, expected vs. actual behavior
- Fault localization (UI → VM → UC → Repo → API)
- Root cause statement (triggered when <cond>, causing <behavior>)
- Design the fix (UiState changes if needed)

Output: `docs/changes/<name>/request_analysis/spec.md` with bug context, fault area, and root cause
Gate: root cause is specific enough that a test can be written to reproduce it

---

### Stage 02 — Fix Plan ⛔ STOP
Load: `stages/implementation-plan.md`

Adapt — the plan must include:
- Root cause
- Proposed fix (minimal)
- Regression tests to add

Output: `docs/changes/<name>/coding/implementation_plan.md`
Gate: **STOP — present fix plan to user. Do not proceed until user explicitly approves.**

---

### Stage 03 — Implementation (Data + Domain + UI as needed)
Load: `stages/implementation.md`

Adapt — only implement the layers the bug fix touches. Skip layers that are unaffected.
Gate: `./gradlew assembleDebug` passes, all affected layer rules satisfied

---

### Stage 04 — Code Review
Load: `stages/code-review.md`

Verify:
- Minimal fix — no unrelated changes
- Logic matches root cause
- Architecture rules followed

Output: `docs/changes/<name>/coding/review/code_review_v<N>.md`
Gate: build passes, static analysis passes, architecture rules followed

---

### Stage 05 — Regression Test (Testing — write test first)
Load: `stages/testing.md`

**Write the failing regression test before touching the application code** where feasible.
Gate: regression test fails before fix AND passes after fix

---

### Stage 06 — Test Review
Load: `stages/test-review.md`

Verify regression test stability and fix coverage.

---

### Stage 07 — Knowledge Capture
Load: `stages/knowledge-capture.md`

**Always** record the bug in `docs/knowledge/past-bugs/`.

---

## Human-in-the-Loop Confirmation Points

1. **After Stage 01** — if root cause is uncertain, ask user
2. **After Stage 02** — user approves fix plan *(mandatory always)*
3. **After Stage 05** — user confirms the fix before it is merged