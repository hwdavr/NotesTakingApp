---
description: You are a senior Android developer diagnosing and fixing a bug.
---

# Workflow: Bug Fixing

## When to use
- A defect, crash, ANR, or production issue
- A regression or test failure
- Unexpected app behavior

This workflow prioritises root-cause analysis over quick patching.

---

## Core Principle

Do not fix symptoms first. Do not guess — prove it with a failing test.
**The reproduction test must be RED before the Fix Plan is written.**
**The Fix Plan must be approved before any fix code is written.**

Pipeline: Bug Context & Root Cause → Bug Reproduction (TDD) → Fix Plan → [User Approval] → Implementation → Testing → Review → Knowledge

---

## Stage Execution

### Stage 1 — Bug Context, Localization & Root Cause
Load: `stages/requirement-analysis.md`

Adapt for bugs:
- Bug description, expected vs. actual behavior
- Fault localization (UI → VM → UC → Repo → API)
- Root cause statement (triggered when \<cond\>, causing \<behavior\>)
- Design the fix (UiState changes if needed)

Output: `docs/current/spec_v<N>.md` created; `docs/current/summary_v<N>.md` updated with bug context, fault area, and root cause.
Gate: root cause is specific enough that a reproduction test can be written.

---

### Stage 2 — Bug Reproduction (TDD) ⛔ STOP
Load: `stages/bug-reproduction.md`

Write a failing test that mechanically proves the root cause before any fix is written.

Output: Failing reproduction test file created; `docs/current/spec_v<N>.md` updated with a Reproduction Test section; `docs/current/summary_v<N>.md` updated.
Gate: test exits RED (non-zero), failure message matches root cause, no application code modified.
**STOP — if root cause cannot be reproduced by a test, surface to user before continuing.**

---

### Stage 3 — Fix Plan ⛔ STOP
Load: `stages/implementation-plan.md`

Adapt — the plan must include:
- Root cause (reference the reproduction test as evidence)
- Proposed fix (minimal)
- Which `@Ignore` annotation to remove once the fix is applied

Output: `docs/current/implementation_plan_v<N>.md` created; `docs/current/summary_v<N>.md` updated.
Gate: **STOP — present fix plan to user. Do not proceed until user explicitly approves.**

---

### Stage 4 — Implementation (Data + Domain + UI as needed)
Load: `stages/implementation.md`

Adapt — only implement the layers the bug fix touches. Skip layers that are unaffected.

Output: `docs/current/summary_v<N>.md` updated with Implementation stage marked complete.
Gate: `./gradlew assembleDebug` passes, all affected layer rules satisfied.

---

### Stage 5 — Testing
Load: `stages/testing.md`

Output: Unit tests, integration tests, and shared JSON scenarios created or updated; `docs/current/summary_v<N>.md` updated with test count and coverage.
Gate: tests pass, coverage targets met.

---

### Stage 6 — Code Quality Fix
Load: `stages/code-quality-fix.md`

Run the code-quality-fix stage to verify complete baseline correctness.

For bug fixes, additionally verify:
- Any `@Ignore` annotation added in the Bug Reproduction stage has been removed
- The reproduction test is GREEN after the fix
- No regressions in the full suite
- The minimal-fix constraint: no unrelated changes slipped in

Output: `docs/current/summary_v<N>.md` updated with code quality results.
Gate:
- All conditions in `stages/code-quality-fix.md` pass
- The reproduction test is GREEN after the fix

---

### Stage 7 — Knowledge Capture
Load: `stages/knowledge-capture.md`

Output: `docs/current/summary_v<N>.md` updated with Knowledge Capture stage marked complete.

---

## Human-in-the-Loop Confirmation Points

1. **After Bug Context, Localization & Root Cause** — if root cause is uncertain, ask user
2. **After Bug Reproduction** — if the bug cannot be reproduced by a test, surface to user *(mandatory stop)*
3. **After Fix Plan** — user approves fix plan *(mandatory always)*