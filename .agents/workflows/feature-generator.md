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
*   **Action**:
    1. Read `sprint-contract.md` for scope and acceptance criteria.
    2. Read `evaluator-rubric.md` for final quality evidence and issues requires follow-up.
    3. Read active logs in `docs/current/progress.md` (or the session logs).
    4. Examine the active planned slices in `docs/current/feature_list.json`.
    5. Run recent git history analysis (`git log -n 5 --oneline`).
*   **Objective**: Reconstruct exactly what was done in the previous session and identify the current active feature context.

### Stage 2 — Setup
Verify target emulator/device runtime environment readiness.
*   **Action**: Run command line tools to check for active ADB devices:
    ```bash
    adb devices
    ```
*   **Objective**: Confirm if a physical device or emulator is connected for any instrumented UI or manual runtime testing checks.

### Stage 3 — Verify Baseline
Ensure that the existing codebase compiles and all tests pass before making any changes. The previous session or developer may have introduced bugs or broken tests.
*   **Action**: Run full static checks and JVM test suites:
    ```bash
    ./gradlew assembleDebug
    ./gradlew testDebugUnitTest
    ```
*   **Objective**: Confirm the repository is in a perfectly stable, compilable, and green state. If the baseline is broken, stop and fix existing regressions first!

### Stage 4 — Select One Task
Focus on a single, isolated slice to avoid scope creep and cognitive overload.
*   **Action**: Review the prioritized list in `docs/current/feature_list.json` and pick the highest-priority incomplete item (status `not_started`).
*   **Objective**: Update the feature status to `in_progress` in the JSON file. Do not work on multiple features in parallel.

### Stage 5 — Implement
Load and execute **`stages/implementation.md`** to build out the selected feature across the necessary layers.
*   **Action**: Load the implementation stage and perform surgical coding using your *Generator* standards (Data, Domain, and UI layers).
*   **Objective**: All layers successfully implemented and `./gradlew assembleDebug` compiles cleanly.

### Stage 6 — Test
Load and execute **`stages/testing.md`** to verify the correctness of the implemented behavior visually and logically.
*   **Action**: Load the testing stage and implement matching unit, integration, and UI test suites. Verify through the actual UI/API and meet code coverage targets (overall project **≥ 80%**, ViewModel & Use Case **≥ 90%**).
*   **Objective**: All local tests pass cleanly and coverage targets are fully met.


### Stage 7 — Fix
Run all static check suites, lint rules, and custom compliance rules. Resolve and fix all reported violations before proceeding.

*   **Action**: Execute the following set of checks to verify complete quality baseline correctness:
    ```bash
    ./gradlew assembleDebug
    ./gradlew ktlintCheck
    ./gradlew detekt
    ./gradlew lintDebug
    bash scripts/check-compose-rules.sh
    bash scripts/check-localization-rules.sh
    bash scripts/check-architecture-rules.sh
    ```
*   **Objective**: Diagnose and resolve all formatting, quality, localization, and architectural style guidelines issues.

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

*   **Action**: Once verification passes and evidence is attached, update `docs/current/feature_list.json` and commit the progress:
    ```bash
    git add docs/current/feature_list.json
    git commit -m "feat(<area>): <short description of implemented feature>"
    ```
*   **Objective**: Ensure all state updates are backed by mechanical, verifiable evidence.

### Stage 9 — Clean Exit
Ensure that the final repository state is clean, verified, and fully prepared for the next developer or agent session.

> [!IMPORTANT]
> **Checklist & Handoff Policy**:
> 1. **Run Clean State Checklist**: Execute and verify every single item in the **[`clean-state-checklist-template.md`](file:///mnt/data/Projects/NotesApp/NotesTakingApp/docs/templates/clean-state-checklist-template.md)**. All checklist checks (Build, Architecture, Runtime, Testing, Observability, Cleanliness, Documentation) **MUST** pass before making the final session commits.
> 2. **Produce Session Handoff**: Create or update the **`docs/current/session-handoff.md`** file by strictly following the format and fields defined in **[`session-handoff-template.md`](file:///mnt/data/Projects/NotesApp/NotesTakingApp/docs/templates/session-handoff-template.md)**. Detail what is working, what changed, unverified paths, risks, and next steps.
> 3. **Commit Handoff Artifacts**: Once the checklist passes and the handoff file is written, commit these files to register the successful session exit:
>    ```bash
>    git add docs/current/session-handoff.md
>    git commit -m "docs: finalize session handoff and clean state verification"
>    ```

*   **Action**: Execute the verification command one last time to ensure no regression was introduced, verify all checklist criteria, write `docs/current/session-handoff.md`, and perform the final commit.
*   **Objective**: Leave the repository in a completely green, stable, and self-documenting state that a fresh session can immediately pick up and resume.
