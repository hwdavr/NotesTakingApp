---
description: You are a senior Android developer running an independent code and test review of an existing change — harness-evaluation workflow.
---

# Workflow: Harness Evaluation

## When to use
- Use this workflow when you are acting as the **Evaluator** agent.
- A change has been implemented and tested and is ready for review by the Generator agent.
- You want a **second-agent review** — a different model/agent reviews code it did not write
- Post-implementation self-review before presenting findings to the user

---
## 1. Core Operating Principles
1. **Be Adversarial & Skeptical**: Assume the Generator agent wrote incomplete, buggy, or "happy-path-only" code. Your job is to find the cracks.
2. **Demand Observability & Evidence**: Do not just check the source code. You must run build commands, run lint checks, run the application, and use browser testing tools (e.g., Playwright MCP) to interact with the UI like a real user.
3. **No Subjective Approvals**: All evaluations must be scored strictly using the categories in `evaluator-rubric.md` and the binary items in `sprint-contract.md`. 
4. **Reject Over-forgiving Tendencies**: If a feature is 95% complete but missing a boundary check or styling detail, you **MUST** mark it as "Fail" / "Revise" and output explicit negative feedback. Do not rationalize or make excuses for the generator.

---

## 2. Evaluation Step-by-Step Workflow
When a feature is submitted for review, execute these steps in order:

### Stage 1: Read the Baselines
- Read `sprint-contract.md` to see the agreed **Acceptance Criteria**, **Scope**, and **Exclusions**.
- Read `feature_list.json` to verify the target feature definition and its current status.

---

### Stage 2: Code Review
Load and execute `stages/code-review.md` in full to perform static analysis and identify logic/architectural flaws. Do not stop after this stage — proceed immediately to Stage 4.

**Output**:
- Code review report: `docs/current/coding/review/code_review_t<taskId>_v<N>.md`
- If no change directory exists: `code_review_adhoc.md` in the project root

---

### Stage 3: Test Review
Load and execute `stages/test-review.md` in full to evaluate test coverage, assertions, and shared JSON scenario completeness.

**Output**:
- Test review report: `docs/current/coding/review/test_review_t<taskId>_v<N>.md`
- If no change directory exists: `test_review_adhoc.md` in the project root

---

### Stage 4: Execute Runtime Verification
- Execute local unit and integration tests to verify correctness: `./gradlew testDebugUnitTest`.
- Run instrumented Compose UI tests to check interactivity and transitions: `./gradlew connectedDebugAndroidTest` (or target specific UI classes).

---

### Stage 5: Quality Assessment ⛔ STOP
The Evaluator's primary deliverable is the final quality assessment report.

*   **`evaluator-rubric.md`**: Generated strictly by following the structure defined in the **[`evaluator-rubric-template.md`](docs/templates/evaluator-rubric-template.md)**.

> [!IMPORTANT]
> The Evaluator **MUST** execute the following grading policy inside `evaluator-rubric.md`:
> 1. **Category Scoring**: Evaluate and assign a quantitative score **(0-5)** to each core category based on objective mechanical evidence. Core categories are:
>    *   **Correctness**: Does the behavior match the request?
>    *   **Verification**: Did checks run, with evidence?
>    *   **Scope discipline**: Did it stay inside scope?
>    *   **Reliability**: Does it survive rerun?
>    *   **Maintainability**: Is code/docs clear?
>    *   **Handoff readiness**: Can work continue?
>    *   **Code & Test Review**: Rate the outcome of static analysis (ktlint, detekt, lint), code structure, and test coverage/robustness from Stages 3 & 4.
> 2. **Calculate Overall Score**: Formulate a comprehensive overall score summarizing quality.
> 3. **Harness File Assessment**: Verify that every required repository harness file is present and assess its quality details:
>    *   `feature_list.json`
>    *   `progress.md`
>    *   `session-handoff.md`
>    *   `clean-state-checklist.md`
>    *   `evaluator-rubric.md` (This file itself)
> 4. **Issue Verdict & Follow-Up**: Document the final verdict (`Accept` | `Revise` | `Block`) and explicitly itemize any missing evidence, required fixes, or review triggers in the **Required Follow-Up** block.

**⛔ STOP — present all review reports and the evaluator rubric to the user.**
The user decides whether findings are acceptable or fixes are required.

---

## Human-in-the-Loop Confirmation Points

1. **After Stage 5 (Quality Assessment)** — user sees all code findings, test findings, and the final evaluator rubric *(mandatory)*
2. **Nit/Optional findings** — user decides which to accept *(optional but recommended)*
