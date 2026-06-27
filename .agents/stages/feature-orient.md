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

1. **Read `docs/current/sprint-contract.md`** for scope and acceptance criteria.
2. **Read `docs/current/evaluator-rubric.md`** for final quality evidence and issues that require follow-up.
3. **Read active logs** in `docs/current/progress.md` (or the session logs).
4. **Run recent git history analysis** (`git log -n 5 --oneline`).
5. **Review prior knowledge** in `docs/knowledge/`:
   - Scan `docs/knowledge/architecture-decisions/` for ADRs relevant to the feature area (e.g. navigation, scoping, data layer patterns).
   - Scan `docs/knowledge/past-bugs/` for bugs that affected the same area or similar functionality.
   - Scan `docs/knowledge/pitfalls/` for known gotchas that could impact implementation.
   - Record any relevant findings in the summary file's **Knowledge Artifacts** section so they are visible throughout the session.
6. **Select the next task & initialize summary**:
   - Review `docs/current/feature_list.json` and select the highest-priority incomplete task (status `not_started`). Do not work on multiple tasks in parallel.
   - Update its status in `docs/current/feature_list.json` to `in_progress`.
   - Generate `docs/current/summary_{feature_id}.md` (where `{feature_id}` is the selected task's ID) following the template below. Refer to `requirement-summary.md` and `sprint-contract.md` to document baseline goals, scope, and acceptance criteria. Include relevant findings from step 5.

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

## Knowledge Artifacts
<ADRs, past-bug entries, or pitfall entries produced>

## Open Items
<anything deferred or unresolved>
```

---
