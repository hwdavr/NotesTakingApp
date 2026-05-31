# Stage — Feature Orient

> **Routing**: When this stage is complete, proceed to the next stage defined in the calling workflow.

## Purpose

Gather complete session, requirement, and git context, establishing a single source of truth for the active feature scope.

---

## Execute

Before making any changes or planning code, gather complete session and git context:

1. **Read `sprint-contract.md`** for scope and acceptance criteria.
2. **Read `evaluator-rubric.md`** for final quality evidence and issues that require follow-up.
3. **Read active logs** in `docs/current/progress.md` (or the session logs).
4. **Examine the active planned slices** in `docs/current/feature_list.json`.
5. **Run recent git history analysis** (`git log -n 5 --oneline`).
6. **Generate a `summary_{feature_id}.md` file** (where `{feature_id}` is the ID of the active slice, e.g. `summary_comments-001.md`) in the active folder (e.g. `docs/current/summary_{feature_id}.md`) by strictly following the template defined below. Refer to the details in `requirement-summary.md` and `sprint-contract.md` to establish the baseline goals, scope, and acceptance criteria.

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
| Select One Task | | | |
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
