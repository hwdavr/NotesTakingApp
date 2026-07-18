---
description: You are a senior Android developer clarifying requirements and planning a complex feature by breaking it into deliverable vertical slices — harness-planning workflow.
---

# Workflow: Harness Planning

## When to use
- A feature is complex enough to touch multiple areas of the codebase.
- The requirement is large and needs to be scoped and broken down into smaller, independently deliverable features before implementation begins.
- You want to define the feature order, verification steps, and scope boundaries **before** implementation.
- The user has a broad product idea (new screen, enhancement, or logic change) that needs clarification before slicing.

---

## Core Principle

Do not guess at scope. Clarify requirements first, decompose into structured features second, and implement later.
Every open question must be answered and approved by the user before slice planning begins.
Each planned feature in the list must represent a clean, end-to-end user-visible behavior with specific verification steps.

---

## Stage Execution

### Stage 1 — Clarify & Specify ⛔ STOP FOR APPROVAL
**INVOKE** the `feature-specification` skill via the Skill tool (name: `feature-specification`). Reading the SKILL.md manually is not a substitute — the Skill tool is the required mechanism.

Objective:
- Classify the task type (new screen, enhancement, or logic-only change).
- Ask targeted clarifying questions in chat until every material ambiguity is resolved.
- Write `docs/current/spec.md` (always) and `docs/current/design.md` (for new screens or UI enhancements/flows).

Output: `docs/current/spec.md` + `docs/current/design.md` (if the change includes UI modifications)
Gate: Run `bash scripts/check-stage-artifacts.sh harness-planning feature-specification` — must exit 0. **STOP — present artifacts to user. Do not proceed until user explicitly approves.**

### Stage 2 — Slice Planning ⛔ STOP FOR APPROVAL
**INVOKE** the `slice-planning` skill via the Skill tool (name: `slice-planning`). Reading the SKILL.md manually is not a substitute — the Skill tool is the required mechanism.
Input: `docs/current/spec.md` (+ `docs/current/design.md` if present)
Output: `docs/current/feature_list.json`, `docs/current/progress.md`, `docs/current/sprint-contract.md`
Gate: Run `bash scripts/check-stage-artifacts.sh harness-planning slice-planning` — must exit 0. **STOP — present `feature_list.json` and `sprint-contract.md` to user. Do not proceed until user explicitly approves the task breakdown and sprint contract.**

---

## Human-in-the-Loop Confirmation Points

1. **After Clarify & Specify** — User approves `spec.md` (and `design.md` if produced). All questions are answered, no open assumptions remain.
2. **After Slice Planning** — User approves the sliced features list, priorities, and `sprint-contract.md` before any implementation work begins.
