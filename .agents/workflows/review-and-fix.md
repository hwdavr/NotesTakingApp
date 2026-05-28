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

Load all rule files relevant to the diff:

| If the diff touches… | Load |
|---|---|
| Any Composable or UI file | `rules/compose-rules.md` |
| Any user-visible text | `rules/localization-rules.md` |
| Any layer boundary | `rules/android-architecture.md` |
| Any navigation code | `rules/navigation-rules.md` |
| Any API / data layer | `rules/api-contract-rules.md` |
| Any analytics event | `rules/analytics-rules.md` |

The reviewing agent:
1. Runs all scripted checks:
   ```bash
   ./gradlew assembleDebug
   ./gradlew ktlintCheck
   ./gradlew detekt
   ./gradlew lintDebug
   bash scripts/check-compose-rules.sh
   ```
2. Evaluates all Evaluator-category rules by reading the changed source files.
3. Fills in the review report using `docs/templates/review-template.md`:
   - Build & Test Results table (including `check-compose-rules.sh` row)
   - Compose Rules Enforcement table — **every row completed, no blanks**
   - Layer Violations, Security, Release Risk, Remaining Risks sections
4. Categorises every finding by severity:
   - **Critical** — blocks merge (data loss, security, broken functionality)
   - **Required** — must fix before merge
   - **Nit / Optional** — author may choose to skip

**Output**: Review report saved to a location of your choice (e.g. a scratch file or `docs/changes/<name>/coding/review/review_t<taskId>_v<N>.md`). If no change directory exists, save to the project root as `review_adhoc.md`.

**⛔ STOP — present the review report to the user before any fixes are made.**
The user decides whether to proceed to Stage 2 (fix) or accept findings as-is.

---

### Stage 2 — Fix

The fixing agent (may be the same instance or a new one):
1. Reads the review report produced in Stage 1.
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
5. Appends the resolution to each finding in the review report (e.g. `→ Fixed in <file>:<line>`).

**Output**: Updated source files. Review report updated with resolutions.

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

1. **After Stage 1 (Review)** — user sees all findings before any code is changed *(mandatory)*
2. **After Stage 3 (Re-Review)** — user confirms the resolved change is acceptable *(mandatory)*
3. **Nit/Optional findings** — user decides which to accept *(optional but recommended)*
