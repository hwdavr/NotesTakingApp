# Stage — Feature Orient

> **Routing**: When this stage is complete, proceed to the next stage defined in the calling workflow.

## Purpose

Gather complete session, requirement, and git context, establishing a single source of truth for the active feature scope.

---

## Load
- `skills/spec-driven-development/SKILL.md`
- `rules/android-architecture.md`
- `rules/api-contract-rules.md`
- `rules/navigation-rules.md`
- `rules/testing-strategy.md`

---

## Execute

Before making any changes or planning code, gather complete session and git context:

1. **Read `sprint-contract.md`** for scope and acceptance criteria.
2. **Read `evaluator-rubric.md`** for final quality evidence and issues that require follow-up.
3. **Read active logs** in `docs/current/progress.md` (or the session logs).
4. **Run recent git history analysis** (`git log -n 5 --oneline`).
5. **Select the next task & initialize summary**:
   - Review `docs/current/feature_list.json` and select the highest-priority incomplete task (status `not_started`). Do not work on multiple tasks in parallel.
   - Update its status in `docs/current/feature_list.json` to `in_progress`.
   - Generate `docs/current/summary_{feature_id}.md` (where `{feature_id}` is the selected task's ID) following the template below. Refer to `requirement-summary.md` and `sprint-contract.md` to document baseline goals, scope, and acceptance criteria. Follow the **Guidelines for Files Changed** below to determine the affected files, UI state, and navigation.

**`summary_{feature_id}.md` Template:**
```markdown
# Change Summary — {name}

**Type**: feature / bugfix / api / refactor
**Started**: YYYY-MM-DD HH:MM
**Status**: In Progress / Complete

## Stage Progress

| Stage | Status | Timestamp | Notes |
|-------|--------|-----------|-------|
| Orient | | | |
| Setup | | | |
| Verify Baseline | | | |
| Implement | | | |
| Test | | | |
| Fix | | | |
| Update State | | | |
| Clean Exit | | | |

## Key Decisions
<major decisions made during this change>

## Files Changed
<summary list of files created, modified, or deleted>

## Knowledge Artifacts
<ADRs, past-bug entries, or pitfall entries produced>

## Open Items
<anything deferred or unresolved>
```

---

## Guidelines for Files Changed

To accurately identify the **Files Changed** and design the necessary updates, perform codebase searches and adhere to the following:

### 1. Impact Analysis
1. **Search the codebase** for all affected files (Screens, ViewModels, UseCases, Repos, DTOs, Tests).
2. **Classify changes** (`modify`, `extend`, `new`, `delete`).
3. **API & Contract Check**:
   - Classify API changes and state force update requirement.
   - **Identify needed APIs**: List all existing or new endpoints that must be called to fulfill the requirement.

### 2. UI State & Navigation Design
1. **Design UiState**: For any new or modified screen, define all possible states (Loading, Success, Empty, Error).
   - Prefer a single immutable `data class`.
2. **Design Navigation**: If navigation is affected, define routes, serializable arguments, and back-stack behavior.
3. **DI Scope**: Identify the required Hilt scope for new components (`@Singleton`, `@ViewModelScoped`).
