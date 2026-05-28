# Changes Directory

This folder contains the complete Audit Trail for every change delivered through the harness.

Each change gets its own subdirectory. Nothing in this folder is ever deleted.

---

## Directory naming

```
{type}-{short-name}-{YYYYMMDD}/
```

Examples:
```
feature-note-sharing-20260515/
bugfix-room-schema-crash-20260510/
api-openapi-note-access-role-20260509/
```

Types: `feature` | `bugfix` | `api` | `refactor` | `config`

---

## Standard structure

```
{change-dir}/
├── summary.md                    # Single source of truth — updated at every stage
├── request_analysis/
│   ├── spec.md                   # Requirement + impact + architecture summary
│   ├── tasks.md                  # Task breakdown with acceptance criteria
│   └── review/
│       ├── spec_review_v1.md     # Expert review of spec (v increments, never deleted)
│       └── spec_review_v2.md
├── coding/
│   ├── implementation_plan.md    # Approved implementation plan
│   ├── coding_report_v1.md       # Coding report (v increments per revision round)
│   └── review/
│       └── code_review_v1.md     # Code review report
├── unit_test/
│   └── test_report_v1.md         # Test results and coverage
└── deployment/
    └── deploy_report.md          # Deployment verification (if applicable)
```

---

## summary.md — the single source of truth

`summary.md` is the most important file in each change directory.
It is updated at every stage and provides a one-page view of the entire change.

**It must be created at the beginning of the workflow (during Requirement Analysis or UI Implementation) and updated immediately after every stage completes.**

Template:
```markdown
# Change Summary — {name}

**Type**: feature / bugfix / api / refactor
**Started**: YYYY-MM-DD HH:MM
**Status**: In Progress / Complete

## Stage Progress

> *Agent Instruction: Generate this table dynamically based on the stages defined in the active workflow (e.g., `workflows/feature-delivery.md` or `workflows/bug-fixing.md`). List all the stages required by the workflow in order, without numbers, and update the status as you progress.*

| Stage | Status | Timestamp | Notes |
|-------|--------|-----------|-------|
| Requirement Analysis | ✅ / ⏳ / ❌ | YYYY-MM-DD HH:MM | |
| Implementation Plan | | | Approved by user: yes/no |
| Implementation | | | |
| Code Review | | | APPROVED / REVISION REQUIRED |
| Testing | | | N tests, X% coverage |
| Test Review | | | APPROVED / REVISION REQUIRED |
| Knowledge Capture | | | |
*(Adapt the rows above to match the exact stages in the active workflow)*

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

## Review versioning policy

Review files use an incrementing version suffix: `v1`, `v2`, `v3`, ...

**Old versions are never deleted.** The complete history of review rounds must be preserved.

This is the Audit Trail. Anyone — human or agent — must be able to reconstruct exactly what happened and why.
