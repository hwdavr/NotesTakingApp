---
description: You are a senior Android developer running an independent code and test review of an existing change.
---

# Workflow: Feature Evaluation

## When to use
- Use this workflow when you are acting as the **Evaluator** agent.
- A change has been implemented and tested and is ready for review by the Generator agent.
- You want a **second-agent review** — a different model/agent reviews code it did not write
- Post-implementation self-review before presenting findings to the user

---

## Stage Execution

### Stage 1 — Code Review

Load and execute `stages/code-review.md` in full. Do not stop after this stage — proceed immediately to Stage 2.

**Output**:
- Code review report: `docs/changes/<name>/coding/review/review_t<taskId>_v<N>.md`
- If no change directory exists: `review_adhoc.md` in the project root

---

### Stage 2 — Test Review

Load and execute `stages/test-review.md` in full.

**Output**:
- Test review report: `docs/changes/<name>/coding/review/test_review_t<taskId>_v<N>.md`
- If no change directory exists: `test_review_adhoc.md` in the project root

### Stage 3 — Quality Assessment ⛔ STOP

The Evaluator's primary deliverable is the final quality assessment report.

*   **`evaluator-rubric.md`**: Generated strictly by following the structure defined in the **[`evaluator-rubric-template.md`](docs/templates/evaluator-rubric-template.md)**.

> [!IMPORTANT]
> The Evaluator **MUST** execute the following grading policy inside `evaluator-rubric.md`:
> 1. **Category Scoring**: Evaluate and assign a quantitative score **(0-5)** to each core category (Correctness, Verification, Scope discipline, Reliability, Maintainability, Handoff readiness) based on objective mechanical evidence.
> 2. **Calculate Overall Score**: Formulate a comprehensive overall score summarizing quality.
> 3. **Harness File Assessment**: Verify that every required repository harness file is present and assess its quality details:
>    *   `feature_list.json`
>    *   `progress.md`
>    *   `session-handoff.md`
>    *   `clean-state-checklist.md`
>    *   `evaluator-rubric.md` (This file itself)
> 4. **Issue Verdict & Follow-Up**: Document the final verdict (`Accept` | `Revise` | `Block`) and explicitly itemize any missing evidence, required fixes, or review triggers in the **Required Follow-Up** block.

**⛔ STOP — present both review reports and the evaluator rubric to the user.**
The user decides whether findings are acceptable or fixes are required.

---

## Human-in-the-Loop Confirmation Points

1. **After Stage 3 (Quality Assessment)** — user sees all code findings, test findings, and the final evaluator rubric *(mandatory)*
2. **Nit/Optional findings** — user decides which to accept *(optional but recommended)*
