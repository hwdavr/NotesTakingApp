---
description: You are a senior Android developer running an independent code and test review of an existing change.
---

# Workflow: Review

## When to use

- A change has been implemented and tested and is ready for review
- You want a **second-agent review** — a different model/agent reviews code it did not write
- Post-implementation self-review before presenting findings to the user
- Called as a sub-step from the `/review-and-fix` workflow

This workflow deliberately separates **review** from **implementation** so the reviewing agent has no authorship bias.

---

## Stage Execution

### Stage 1 — Code Review

Load and execute `stages/code-review.md` in full. Do not stop after this stage — proceed immediately to Stage 2.

**Output**:
- Code review report: `docs/changes/<name>/coding/review/review_t<taskId>_v<N>.md`
- If no change directory exists: `review_adhoc.md` in the project root

---

### Stage 2 — Test Review ⛔ STOP

Load and execute `stages/test-review.md` in full.

**Output**:
- Test review report: `docs/changes/<name>/coding/review/test_review_t<taskId>_v<N>.md`
- If no change directory exists: `test_review_adhoc.md` in the project root

**⛔ STOP — present both review reports to the user.**
The user decides whether findings are acceptable or fixes are required.

---

## Human-in-the-Loop Confirmation Points

1. **After Stage 2 (Test Review)** — user sees all code and test findings *(mandatory)*
2. **Nit/Optional findings** — user decides which to accept *(optional but recommended)*
