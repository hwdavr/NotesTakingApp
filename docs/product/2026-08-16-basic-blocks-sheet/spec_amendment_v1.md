# Spec Amendment v1 — Note Editor Basic Blocks Panel

**Date**: 2026-08-16
**Status**: Approved
**Amends**: spec.md
**Approved by**: User (2026-08-16)

---

## Amendment Summary

Adds one approved open question (Q12) and its required behavior to the feature scope. The original spec (Q1–Q11, FR-001–FR-019, AC-001–AC-014) is otherwise unchanged. US-1, US-2, and US-3 remain `passing`; the new behavior is delivered by a new slice **US-4**.

---

## Open Question Added

| # | Question | Status | Answer |
|---|----------|--------|--------|
| Q12 | When the Basic blocks panel is open and the user taps the note editor content or any other editor toolbar button, should the panel auto-close? | ✅ Answered | **Yes.** The panel MUST collapse without inserting a block or otherwise mutating the document. |

---

## Scope Change

### Added to In Scope

- Auto-collapse the open Basic blocks panel when the user interacts with the note editor content area or activates any editor toolbar control other than the Basic blocks trigger and the panel's own tiles, without inserting a block or mutating the document.

### Out of Scope (unchanged)

- Modal/overlay surfaces, Page blocks, images/tables/voice/links/mentions/undo-redo, nested toggles, drag reorder, slash commands, search, favorites, recents, custom templates, list nesting, and Room/API/permission/hardware changes remain out of scope.

---

## New Functional Requirement

- **FR-020**: While the Basic blocks panel is open, when the user interacts with the note editor content area or activates any editor toolbar control other than the Basic blocks trigger and the panel's own tiles, the panel MUST collapse without inserting a block or otherwise mutating the document. The Basic blocks trigger continues to toggle the panel per FR-001/FR-012, and a tile selection continues to insert and collapse per FR-006/FR-007/FR-008.

---

## New Acceptance Criterion

- **AC-015**: Given the Basic blocks panel is open, when the user taps inside the note editor content or activates any editor toolbar control other than the Basic blocks trigger, then the panel collapses, no block is inserted, and the document is unchanged.

---

## New Edge Case

| Scenario | Required behavior |
|----------|-------------------|
| Outside interaction while panel open | Collapse the panel without mutation when the user taps editor content or any toolbar control other than the panel trigger and tiles. |

---

## Screen State Update

The **Editable / panel open** state now also requires: the panel collapses when the user moves focus to the note editor content or activates any other toolbar control, without inserting a block or mutating the document.

---

## Slice Impact

- US-1, US-2, US-3: unchanged and remain `passing`.
- **US-4 (new)**: "Auto-collapse the Basic blocks panel on outside interaction" — owns FR-020 and AC-015, with instrumented UI tests proving editor-content and toolbar-control taps collapse the open panel without mutation, plus regression coverage that the trigger toggle and tile insertion flows still work.
