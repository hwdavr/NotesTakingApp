---
description: You are a senior Android developer running an independent review of an existing change and fixing all findings before merge.
---

# Workflow: Review & Fix

## When to use

- A change has been implemented (and optionally tested) but not yet reviewed
- You want a **second-agent review** — a different model/agent reviews code it did not write
- Post-implementation self-review before presenting to the user
- Findings from a previous review exist and need to be resolved

This workflow deliberately separates **review** from **implementation** so the reviewing agent has no authorship bias.

---

## Core Principle

The reviewer and the fixer can be the same agent, but they must operate in sequence — **review first, fix second**. Never fix while reviewing: finishing the review with a complete finding list produces better fixes than interleaving them.

**The fix loop runs until all Critical and Required findings are resolved, or the user explicitly accepts a known deviation.**

---

## Stage Execution

### Stage 1 — Review ⛔ STOP

Run the `/review` workflow in full (Code Review → Test Review). Do not proceed until the `/review` workflow has completed and both reports are presented to the user.

**Output** (produced by `/review`):
- Code review report: `docs/changes/<name>/coding/review/review_t<taskId>_v<N>.md`
- Test review report: `docs/changes/<name>/coding/review/test_review_t<taskId>_v<N>.md`

**⛔ STOP — the `/review` workflow stops after Test Review and presents both reports.**
The user decides whether to proceed to Stage 2 (Fix) or accept findings as-is.

---

### Stage 2 — Fix

The fixing agent (may be the same instance or a new one):
1. Reads both review reports produced in Stage 1.
2. States a fix plan — one bullet per Critical or Required finding, in dependency order.
3. Implements fixes surgically:
   - One logical fix at a time
   - Do not refactor unrelated code
   - Do not change test scope or coverage approach unless the review explicitly flagged it
4. After all fixes are applied, re-runs the scripted checks to confirm no regressions:
   ```bash
   ./gradlew assembleDebug
   ./gradlew ktlintCheck
   ./gradlew detekt
   ./gradlew lintDebug
   bash scripts/check-compose-rules.sh
   ./gradlew testDebugUnitTest
   ```
5. Appends the resolution to each finding in the review reports (e.g. `→ Fixed in <file>:<line>`).

**Output**: Updated source files. Review reports updated with resolutions.

Gate: All Critical and Required findings resolved or explicitly accepted by user.

---

### Stage 3 — Re-Review ⛔ STOP

After fixes, run a targeted re-review:
1. Re-read only the files changed in Stage 2.
2. Confirm each Critical/Required finding from Stage 1 is resolved.
3. Confirm no new violations were introduced by the fixes.
4. Update the review report verdict to **APPROVED** or **REVISION REQUIRED (round N+1)**.

**⛔ STOP — present the updated review report to the user.**

If verdict is **APPROVED** → workflow complete.  
If **REVISION REQUIRED** again → loop back to Stage 2 (max 2 fix rounds; if still unresolved, surface to user for a decision).

---

## Fix Discipline

| Rule | Rationale |
|------|-----------|
| Fix only what the review flagged | Scope creep in the fix round introduces new risk |
| Re-run scripts after every fix group | Catch regressions immediately, not at the end |
| Nit/Optional findings are opt-in | Do not fix without user acknowledgement |
| Do not alter test intent | If tests need changing, flag it — don't silently rewrite assertions |

---

## Rollback Routes

| Situation | Action |
|-----------|--------|
| Fix introduces a compilation error | Revert the fix, diagnose separately |
| Fix causes a test failure | Revert, re-examine the finding; may indicate a deeper design issue |
| Critical finding cannot be fixed without a larger refactor | Surface to user — do not proceed |
| Round 2 re-review still shows Required violations | Surface to user for explicit acceptance or escalation |

---

## Human-in-the-Loop Confirmation Points

1. **After Stage 1 (Review)** — user sees all code and test findings before any code is changed *(mandatory)*
2. **After Stage 3 (Re-Review)** — user confirms the resolved change is acceptable *(mandatory)*
3. **Nit/Optional findings** — user decides which to accept *(optional but recommended)*
