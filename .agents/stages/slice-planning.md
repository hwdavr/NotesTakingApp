# Stage — Slice Planning

> **Routing**: When this stage is complete and approved, return to the active **workflow** file to determine the next stage.

## Purpose

Decompose a complex requirement into independently deliverable vertical slices before any code is written.
Each slice must be end-to-end (Data → Domain → UI → Tests) and leave the codebase in a working, shippable state.

This stage only applies when the feature touches **more than one logical area** or **more than ~3 files**.
For trivially small changes, skip this stage and go directly to the Implementation Plan stage.

---

## Load

- `skills/incremental-implementation/SKILL.md`
- `docs/templates/task-list-template.md`
- `docs/templates/progress-template.md`
- **Requirement input** — read the path from the active workflow's `Input:` line:
  - feature-planning workflow → `docs/current/requirement-summary.md`
  - Other workflows → `request_analysis/spec.md`

---

## Execute

### 1. Assess Complexity

Review the requirement input document (path defined by the calling workflow) and answer:
- How many distinct areas of the codebase are touched?
- Is there a natural dependency order (e.g. DB schema must exist before the ViewModel can query it)?
- Which part carries the highest technical risk or most uncertainty?

### 2. Choose a Slicing Strategy

**Strongly Prefer Vertical Slices**: Always default to the **Vertical Slices** strategy to ensure that each slice delivers an end-to-end, testable user value and leaves the codebase in a clean, shippable state. Only choose another strategy if there is a compelling structural blocker.

| Strategy | When to use |
|----------|------------|
| **Vertical slices** | **Preferred**. Build one complete, end-to-end user-visible flow per slice (Data → Domain → UI → Tests). |
| **Contract-first** | When the API contract is highly uncertain. Define the contract in Slice 0, implement against mocks in parallel. |
| **Risk-first** | When one specific architectural part carries high risk or extreme technical uncertainty. Spike that piece first. |

### 3. Define Tasks

For each slice:
- Give it a short name and clear Objective.
- Describe User-visible behavior including what should change and what must remain unchanged.
- Identify the Scope by listing potential components, classes, or files it **may touch**.
- Define the Required tests in a clean table (Given/When/Then + Type).
- Specify clear Done criteria to bound completeness and prevent scope creep.
- Verify the build and full test suite pass at the end of each slice — no exceptions.
- Each task changes **one logical thing** and does it completely.
- No task should leave the codebase broken or a test failing.
- A task may use a feature flag if it merges UI that is not yet user-visible.
- Touch only what the task requires — do not clean up adjacent code.

### 4. Order Tasks

Place the riskiest or most foundational slice first.
Express the dependency order explicitly (linear or branching).

---

## Output

Write the **task list** following `docs/templates/task-list-template.md`.
Write the **progress file** following `docs/templates/progress-template.md`.

Output paths are defined by the calling workflow:
- **feature-planning workflow** → `docs/current/task-list.md` and `docs/current/progress.md`
- **Other workflows** → `request_analysis/task-list.md` and `coding/progress.md`

Pre-populate the Task Progress table with all tasks.
Set Task 1 to ⏳ In Progress, all others to ⬜ Not Started.

---

## Gate — ⛔ MANDATORY STOP

**Present the task list to the user.**

The user must confirm:
- [ ] The slice boundaries make sense
- [ ] The dependency order is correct
- [ ] No task is too large (a task that takes more than one session is too large — split it)
- [ ] Expected behaviors and required tests are complete and testable

**APPROVED by user →** Return to the active workflow file and proceed to the next stage.

