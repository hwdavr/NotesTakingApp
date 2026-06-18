---
description: You are a senior Android developer planning a complex feature by breaking it into deliverable vertical slices — harness-planning workflow.
---

# Workflow: Harness Planning

## When to use
- A feature is complex enough to touch multiple areas of the codebase.
- The requirement is large and needs to be scoped and broken down into smaller, independently deliverable features before implementation begins.
- You want to define the feature order, verification steps, and scope boundaries **before** implementation.

---

## Core Principle

Do not guess at scope. Clarify requirements first, decompose into structured features second, and implement later.
Every open question must be answered and approved by the user before slice planning begins.
Each planned feature in the list must represent a clean, end-to-end user-visible behavior with specific verification steps.

---

## Stage Execution

### Stage 1 — Feature Requirement Capture ⛔ STOP
Load: `stages/requirement-capture.md`
Output: `docs/current/requirement-summary.md`
Gate: **STOP — present `requirement-summary.md` to user. Every open question must be ✅ Answered before proceeding.**

### Stage 2 — Slice Planning
Load: `stages/slice-planning.md`
Input: `docs/current/requirement-summary.md`
Output: `docs/current/feature_list.json`, `docs/current/progress.md`, `docs/current/sprint-contract.md`
Gate: **STOP — present `feature_list.json` and `sprint-contract.md` to user. Do not proceed until user explicitly approves the task breakdown and sprint contract.**

---

## Human-in-the-Loop Confirmation Points

1. **After Feature Requirement Capture** — User confirms the requirements are complete, all questions are answered, and approves `requirement-summary.md`.
2. **After Slice Planning** — User approves the sliced features list, priorities, and `sprint-contract.md` before any implementation work begins.
