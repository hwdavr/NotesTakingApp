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

Do not fix symptoms first. Do not guess — prove it with a failing test.
**The reproduction test must be RED before the Fix Plan is written.**
**The Fix Plan must be approved before any fix code is written.**

Pipeline: Bug Context & Root Cause → Bug Reproduction (TDD) → Fix Plan → [User Approval] → Implementation → Code Review → Testing → Test Review → Knowledge

---

## Stage Execution

### Stage — Bug Context, Localization & Root Cause
Load: `stages/requirement-analysis.md`

Adapt for bugs:
- Bug description, expected vs. actual behavior
- Fault localization (UI → VM → UC → Repo → API)
- Root cause statement (triggered when <cond>, causing <behavior>)
- Design the fix (UiState changes if needed)

Output: `docs/changes/<name>/request_analysis/spec.md` with bug context, fault area, and root cause
Gate: root cause is specific enough that a reproduction test can be written

---

### Stage — Bug Reproduction (TDD) ⛔ STOP
Load: `stages/bug-reproduction.md`

Write a failing test that mechanically proves the root cause before any fix is written.

Output: Failing reproduction test file + `spec.md` updated with Reproduction Test section
Gate: test exits RED (non-zero), failure message matches root cause, no application code modified
**STOP — if root cause cannot be reproduced by a test, surface to user before continuing.**

---

### Stage — Fix Plan ⛔ STOP
Load: `stages/implementation-plan.md`

Adapt — the plan must include:
- Root cause (reference the reproduction test as evidence)
- Proposed fix (minimal)
- Which `@Ignore` annotation to remove once the fix is applied

Output: `docs/changes/<name>/coding/implementation_plan.md`
Gate: **STOP — present fix plan to user. Do not proceed until user explicitly approves.**

---

### Stage — Implementation (Data + Domain + UI as needed)
Load: `stages/implementation.md`

Adapt — only implement the layers the bug fix touches. Skip layers that are unaffected.
Gate: `./gradlew assembleDebug` passes, all affected layer rules satisfied

---

### Stage — Code Review
Load: `stages/code-review.md`

Verify:
- Minimal fix — no unrelated changes
- Logic matches root cause
- Architecture rules followed

Output: `docs/changes/<name>/coding/review/code_review_v<N>.md`
Gate: build passes, static analysis passes, architecture rules followed

---

### Stage — Testing
Load: `stages/testing.md`

Output: Unit tests, integration tests, shared JSON scenarios
Gate: tests pass, coverage targets met

---

### Stage — Test Review
Load: `stages/test-review.md`

Verify the reproduction test (now passing) and any additional tests written during Implementation:
- Remove any `@Ignore` annotation added in the Bug Reproduction stage
- Confirm the reproduction test is GREEN after the fix
- Confirm no regressions in the full suite

Gate: reproduction test passes, full suite exits 0, coverage requirements met

---

### Stage — Knowledge Capture
Load: `stages/knowledge-capture.md`

**Always** record the bug in `docs/knowledge/past-bugs/`.

---

## Human-in-the-Loop Confirmation Points

1. **After Bug Context, Localization & Root Cause** — if root cause is uncertain, ask user
2. **After Bug Reproduction** — if the bug cannot be reproduced by a test, surface to user *(mandatory stop)*
3. **After Fix Plan** — user approves fix plan *(mandatory always)*
4. **After Test Review** — user confirms the fix before it is merged