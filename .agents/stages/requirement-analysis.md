# Stage — Requirement, Impact & Design Analysis

> **Routing**: When this stage is complete, return to the active **workflow** file to determine the next stage.

## Purpose
Understand what is being built, identify affected files, and design the core UI state and navigation before implementation.
Do not write any code in this stage.

---

## Load
- `skills/spec-driven-development/SKILL.md`
- `rules/android-architecture.md`
- `rules/api-contract-rules.md`
- `rules/navigation-rules.md`
- `rules/testing-strategy.md`

---

## Execute

### 1. Requirement & Impact Analysis
0. **Task Pickup Check & Routing**:
   - **If the user prompt outlines a specific, clear requirement**: Perform the analysis and execute based directly on the user's explicit requirement.
   - **If the user prompt asks to "execute the next task" (or if no explicit requirement is specified but an active sliced plan exists in `docs/current/progress.md`)**: Check `docs/current/progress.md`, identify the next uncompleted task in `docs/current/task-list.md`, pick it up (set its progress status to `⏳ In Progress` in `docs/current/progress.md`), and read its objective, behavior, and scope details.
   - The `spec.md` you create/update in this stage will be dedicated specifically to the resolved target task or requirement.
1. Read the resolved requirement or picked-up task details in full (resolved in Step 0).
2. Search the codebase for all affected files (Screens, ViewModels, UseCases, Repos, DTOs, Tests).
3. Classify changes (`modify`, `extend`, `new`, `delete`).
4. **API & Contract Check**:
   - Classify API changes and state force update requirement.
   - **Identify needed APIs**: List all existing or new endpoints that must be called to fulfill the requirement.

### 2. UI State & Navigation Design
1. **Design UiState**: For any new or modified screen, define all possible states (Loading, Success, Empty, Error).
   - Prefer a single immutable `data class`.
2. **Design Navigation**: If navigation is affected, define routes, serializable arguments, and back-stack behavior.
3. **DI Scope**: Identify the required Hilt scope for new components (`@Singleton`, `@ViewModelScoped`).

---

## Output

Create `docs/changes/<type>-<name>-<YYYYMMDD>/` directory.

If the user provides a design screenshot or mockup, save it to **`request_analysis/design/`** so it can be referenced during UI Verification.

Produce **`docs/changes/<name>/summary.md`** — create this file **first**, before `spec.md`.
Use the template from `docs/changes/README.md`.
The Stage Progress table must list every stage from the active workflow in order:

| Stage | Status | Date | Notes |
|-------|--------|------|-------|
| Requirement Analysis | ⏳ In Progress | | |
| Implementation Plan | | | Approved by user: — |
| Implementation | | | |
| Code Review | | | APPROVED / REVISION REQUIRED |
| Testing | | | |
| Test Review | | | APPROVED / REVISION REQUIRED |
| Knowledge Capture | | | |

Mark the Requirement Analysis row as ✅ Complete when this stage's gate passes.

Produce **`request_analysis/spec.md`**:
```
## Requirement Summary
<description>

## Impact Analysis (Affected Files)
| File | Layer | Change Type | Notes |
|------|-------|-------------|-------|

## API Impact
- Classification: <backward compatible / risky / breaking / none>
- Force update: <yes / no / unknown>
- **APIs Needed**:
  - `METHOD /path` : <description>

## UI State Design
```kotlin
data class ExampleUiState(...)
```

## Navigation Design
- Route: <name>
- Arguments: <types>
- Back-stack: <behavior>

## Explicit Assumptions
1. <assumption>
```


---

## Gate

**Conditions to pass:**
- [ ] `docs/changes/<name>/summary.md` exists with the Stage Progress table filled in.
- [ ] `request_analysis/spec.md` exists with requirement, impact, and design sections filled.
- [ ] Every affected file is listed with a change type.
- [ ] UiState design covers all visual states.
- [ ] API change is classified.

**APPROVED →** Return to the active workflow file and proceed to the next stage defined there.
