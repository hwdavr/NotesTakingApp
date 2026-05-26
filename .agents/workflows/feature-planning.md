---
description: You are a senior Android developer planning a complex feature by breaking it into deliverable vertical slices.
---

# Workflow: Feature Planning

## When to use
- A feature is complex enough to touch multiple areas of the codebase
- The requirement is large and needs to be broken into smaller, independently deliverable pieces before implementation begins
- You want to define the task order and scope **before** running `/feature-delivery`

Use `/feature-delivery` for each individual task once this workflow produces an approved task list.

---

## Core Principle

Do not guess at scope. Clarify first, decompose second, implement later.
Every open question must be answered before slice planning begins.
Each task produced by this workflow must be a thin vertical slice — end-to-end (Data → Domain → UI → Tests) and independently shippable.

---

## Stage Execution

### Stage 1 — Feature Requirement Capture ⛔ STOP
Load: `stages/feature-requirement-capture.md`
Output: `docs/current/requirement-summary.md`
Gate: **STOP — present `requirement-summary.md` to user. Every open question must be ✅ Answered before proceeding.**

### Stage 2 — Slice Planning ⛔ STOP
Load: `stages/slice-planning.md`
Input: `docs/current/requirement-summary.md`
Output: `docs/current/task-list.md`, `docs/current/progress.md`
Gate: **STOP — present `task-list.md` to user. Do not proceed until user explicitly approves the task breakdown.**

---

## After This Workflow

Once the task list is approved:
1. Pick **Task 1** from `docs/current/task-list.md`.
2. Run `/feature-delivery` — it will implement, review, and test that single task.
3. After Task 1 is complete, update `docs/current/progress.md` and repeat for the next task.

Continue until all tasks in `task-list.md` are marked ✅ Complete.

---

## Human-in-the-Loop Confirmation Points

1. **After Feature Requirement Capture** — user confirms the requirement is complete and all questions are answered *(mandatory — do not slice until approved)*
2. **After Slice Planning** — user approves the task breakdown *(mandatory always)*
