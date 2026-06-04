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
- `docs/templates/feature_list_template.json`
- `docs/templates/progress-template.md`
- `docs/templates/sprint-contract-template.md`
- **Requirement input** — read the path from the active workflow's `Input:` line:
  - harness-planning workflow → `docs/current/requirement-summary.md`
  - Other workflows → `request_analysis/spec_t<taskId>.md`

---

## Execute

### 1. Assess Complexity

Review the requirement input document (path defined by the calling workflow) and answer:
- How many distinct areas of the codebase are touched?
- Is there a natural dependency order (e.g. DB schema must exist before the ViewModel can query it)?
- Which part carries the highest technical risk or most uncertainty?

### 2. Compile Sprint Contract

Decompose the high-level requirement into a detailed scope, acceptance criteria, and verification plan. Strictly follow the structure in `docs/templates/sprint-contract-template.md` to generate `sprint-contract.md`. Fill in the Sprint ID, Feature Name, In Scope, Out of Scope, Roles, Acceptance Criteria, and the Verification Plan mapping to the acceptance criteria.

### 3. Choose a Slicing Strategy

**Strongly Prefer Vertical Slices**: Always default to the **Vertical Slices** strategy to ensure that each slice delivers an end-to-end, testable user value and leaves the codebase in a clean, shippable state. 

**Calibrate Granularity**: Each feature item should be scoped to "completable in one session." Too broad and it won't finish; too narrow and the management overhead grows.
- *Good granularity*: "User can add items to cart"
- *Too broad*: "Implement the shopping cart"
- *Too narrow*: "Create the name field on the Cart model"

### 4. Define Features inside `feature_list.json`

For each slice, you must populate the `features` list in the `feature_list.json` schema. Define each task completely, ensuring that each field is explained and adheres to the following definitions:

- **`id`**: A unique alphanumeric identifier for the feature slice (e.g. `comments-001`).
- **`priority`**: Integer priority indicating delivery order (lower number = higher priority).
- **`area`**: The codebase component or feature area (e.g., `comments`, `folders`, `editor`).
- **`title`**: A short, readable title summarizing the slice.
- **`description`**: A comprehensive detailed instruction mapping the precise code-level logic, domain model changes, and database structures required. **This field specifically tells the generator agent exactly what to do** at a technical execution level.
- **`ui_design`**: A file path to a layout asset/mockup or a reference name from an external design tool (e.g. Figma or Pencil.dev) depicting the UI specifications for the feature.
- **`user_visible_behavior`**: A clear explanation of what observable UI elements, texts, behavior, or default flows are affected by this task.
- **`status`**: The progress status (`not_started`, `in_progress`, `blocked`, or `passing`).
- **`verification`**: An array of specific, step-by-step proof required for that feature. A high-quality verification is defined as a set of instrumented tests or integration tests that can be executed directly in the shell to provide PASS or FAIL results.
- **`evidence`**: Terminal output, test reports, or screenshots verifying task completion.
- **`notes`**: Additional technical context or design considerations.

Each task must change **one logical thing** and do it completely, ensuring the codebase is never left in a broken or non-compiling state.

### 5. Order Tasks

Place the riskiest or most foundational slice first.
Express the dependency order explicitly (linear or branching).


---

## Output

Write the **feature list** following `docs/templates/feature_list_template.json`.
Write the **progress file** following `docs/templates/progress-template.md`.
Write the **sprint contract** following `docs/templates/sprint-contract-template.md`.

Output paths are defined by the calling workflow:
- **harness-planning workflow** → `docs/current/feature_list.json`, `docs/current/progress.md`, and `docs/current/sprint-contract.md`
- **Other workflows** → `request_analysis/feature_list.json`, `coding/progress.md`, and `request_analysis/sprint-contract.md`

Pre-populate the task list with all slices.
Set Feature 1 to `in_progress`, all others to `not_started`.

---

## Gate — ⛔ MANDATORY STOP

**Present the feature list to the user.**

The user must confirm:
- [ ] The slice boundaries make sense
- [ ] The dependency order is correct
- [ ] No feature is too large (completable in a single session)
- [ ] Feature descriptions are clear, instructing exactly what to do
- [ ] Verification steps are concrete, machine-executable shell commands (returning binary PASS/FAIL)
- [ ] The sprint-contract is compiled with explicit acceptance criteria and a corresponding verification plan

**APPROVED by user →** Return to the active workflow file and proceed to the next stage.

