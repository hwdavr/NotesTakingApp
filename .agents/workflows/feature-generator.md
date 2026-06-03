---
description: You are a senior Android developer implementing features step-by-step using the feature-generator pipeline.
---

# Workflow: Feature Generator

## When to use
Use this workflow when you are acting as the **Generator** (Implementer) agent. This workflow ensures that you are properly oriented, verify safety baselines, implement features surgical-by-surgical, test continuously on runtime, and commit clean states back to the repository.

---

## 🔄 Stage Execution Pipeline

### Stage 1 — Orient
Before making any changes or planning code, gather complete session and git context.
*   **Action**: Load and execute **`stages/feature-orient.md`**.
*   **Objective**: Reconstruct exactly what was done in the previous session, establish the single source of truth (`summary_{feature_id}.md`), and identify the current active feature context.

### Stage 2 — Setup
Verify target emulator/device runtime environment readiness.
*   **Action**:
    1. Run command line tools to check for active ADB devices:
        ```bash
        adb devices
        ```
    2. **Update `summary_{feature_id}.md`** to mark the **Setup** stage status to completed (✅) with notes and current timestamp.
*   **Objective**: Confirm if a physical device or emulator is connected for any instrumented UI or manual runtime testing checks, and register progress in the summary.

### Stage 3 — Verify Baseline
Ensure that the existing codebase compiles and all tests pass before making any changes. The previous session or developer may have introduced bugs or broken tests.
*   **Action**:
    1. Run full static checks and JVM test suites:
        ```bash
        ./gradlew assembleDebug
        ./gradlew testDebugUnitTest
        ```
    2. **Update `summary_{feature_id}.md`** to mark the **Verify Baseline** stage status to completed (✅) with notes and current timestamp.
*   **Objective**: Confirm the repository is in a perfectly stable, compilable, and green state. If the baseline is broken, stop and fix existing regressions first! Register status in `summary_{feature_id}.md`.

### Stage 4 — Select One Task
Focus on a single, isolated slice to avoid scope creep and cognitive overload.
*   **Action**:
    1. Review the prioritized list in `docs/current/feature_list.json` and pick the highest-priority incomplete item (status `not_started`).
    2. Update the feature status to `in_progress` in the JSON file. Do not work on multiple features in parallel.
    3. **Update `summary_{feature_id}.md`** to mark the **Select One Task** stage status to completed (✅), and set the active slice/feature under development.
*   **Objective**: Ensure the team works on exactly one prioritized slice at a time, documenting it clearly in task logs and the summary.

### Stage 5 — Implement
Load and execute **`stages/implementation.md`** to build out the selected feature across the necessary layers.
*   **Action**:
    1. Load the implementation stage and perform surgical coding using your *Generator* standards (Data, Domain, and UI layers).
    2. **Update `summary_{feature_id}.md`** to mark the **Implement** stage status to completed (✅) with list of created/modified files.
*   **Objective**: All layers successfully implemented, `./gradlew assembleDebug` compiles cleanly, and progress is logged in the summary.

### Stage 6 — Test
Load and execute **`stages/testing.md`** to verify the correctness of the implemented behavior visually and logically.
*   **Action**:
    1. Load the testing stage and implement matching unit, integration, and UI test suites. Verify through the actual UI/API and meet code coverage targets (overall project **≥ 80%**, ViewModel & Use Case **≥ 90%**).
    2. **Update `summary_{feature_id}.md`** to mark the **Test** stage status to completed (✅) detailing coverage percentages and passed test counts.
*   **Objective**: All local tests pass cleanly, coverage targets are fully met, and verification evidence is documented in the summary.


### Stage 7 — Code Quality Fix
Load and execute **`stages/code-quality-fix.md`** to run all static check suites, lint rules, and custom compliance rules, and resolve all violations.
*   **Action**: Load and execute **`stages/code-quality-fix.md`**.
*   **Objective**: Diagnose and resolve all formatting, quality, localization, and architectural style guidelines issues, logging check success in `summary_{feature_id}.md`.

### Stage 8 — Update State
Update repository history and project task logs to reflect completion.

> [!IMPORTANT]
> **Strict Verification Gate**: You **CANNOT** directly or arbitrarily change a feature's status to `passing` in `feature_list.json`. Transitioning a feature to `passing` is a gate controlled exclusively by executing successful verification commands.
>
> **Gate Check Policy**:
> 1. **Identify Gate Criteria**: Look at the active feature in `feature_list.json` and locate its `"verification"` field. This contains the exact command(s) that must be run.
> 2. **Execute the Command**: Run the exact verification command (e.g., `./gradlew testDebugUnitTest` or specific test runner script).
> 3. **Validate & Attach Evidence**:
>    *   The status can **ONLY** transition to `passing` if the verification command executes successfully (exit code `0`).
>    *   You **MUST** attach objective evidence (e.g., the test output summary, exit verification status, or the commit hash) inside the `"evidence"` field of the active feature object.
>    *   If verification fails (exit code `non-zero`), the status must be marked as `blocked` or returned to `in_progress`.

*   **Action**:
    1. Once verification passes and evidence is attached, update `docs/current/feature_list.json` and commit the progress:
        ```bash
        git add docs/current/feature_list.json
        git commit -m "feat(<area>): <short description of implemented feature>"
        ```
    2. **Update `summary_{feature_id}.md`** to mark the **Update State** stage status to completed (✅), logging the commit hash and verification execution outcome.
*   **Objective**: Ensure all state updates are backed by mechanical, verifiable evidence.

### Stage 9 — Clean Exit
Ensure that the final repository state is clean, verified, and fully prepared for the next developer or agent session.

> [!IMPORTANT]
> **Checklist & Handoff Policy**:
> 1. **Run Clean State Checklist**: Execute and verify every single item in the **[`clean-state-checklist-template.md`](../../docs/templates/clean-state-checklist-template.md)**. All checklist checks (Build, Architecture, Runtime, Testing, Observability, Cleanliness, Documentation) **MUST** pass before making the final session commits.
> 2. **Produce Session Handoff**: Create or update the **`docs/current/session-handoff.md`** file by strictly following the format and fields defined in **[`session-handoff-template.md`](../../docs/templates/session-handoff-template.md)**. Detail what is working, what changed, unverified paths, risks, and next steps.
> 3. **Commit Handoff Artifacts**: Once the checklist passes and the handoff file is written, commit these files to register the successful session exit:
>    ```bash
>    git add docs/current/session-handoff.md
>    git commit -m "docs: finalize session handoff and clean state verification"
>    ```

*   **Action**:
    1. Execute the verification command one last time to ensure no regression was introduced, verify all checklist criteria, write `docs/current/session-handoff.md`, and perform the final commit.
    2. **Update `summary_{feature_id}.md`** to mark the **Clean Exit** stage status to completed (✅), transitions overall status to Complete, and documents key outcomes, open items, and handoff decisions.
*   **Objective**: Leave the repository in a completely green, stable, and self-documenting state that a fresh session can immediately pick up and resume.
