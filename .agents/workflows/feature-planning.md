---
description: You are a senior Android developer planning a complex feature by breaking it into deliverable vertical slices.
---

# Workflow: Feature Planning

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

The Planner captures all requirements and extracts them into a structured summary.

*   **Load**: `skills/spec-driven-development/SKILL.md`
*   **Template**: `docs/templates/requirement-summary-template.md`
*   **Output**: `docs/current/requirement-summary.md`
*   **Gate**: **STOP — present `requirement-summary.md` to user. Every open question must be ✅ Answered and approved before proceeding.**

### Stage 2 — Slice Planning ⛔ STOP

The Planner decomposes the approved requirement summary into a prioritized list of independent features.

*   **Load**: `skills/incremental-implementation/SKILL.md`
*   **Input**: `docs/current/requirement-summary.md`
*   **Template**: `docs/templates/feature_list_template.json`
*   **Output**: `docs/current/feature_list.json`
*   **Gate**: **STOP — present `feature_list.json` to user. Do not proceed until user explicitly approves the planned features list.**

---

## After This Workflow

Once the `feature_list.json` is approved:
1. Pick the first high-priority feature from `docs/current/feature_list.json`.
2. Hand it off to the **Generator** (Implementer) along with the sprint contract.
3. Update the feature status to `in_progress` in `docs/current/feature_list.json` during execution.
4. After implementation and verification pass, mark the feature status as `passing` and log the captured evidence.

---

## Human-in-the-Loop Confirmation Points

1. **After Feature Requirement Capture** — User confirms the requirements are complete, all questions are answered, and approves `requirement-summary.md`.
2. **After Slice Planning** — User approves the sliced features list and priorities in `feature_list.json` before any implementation work begins.
