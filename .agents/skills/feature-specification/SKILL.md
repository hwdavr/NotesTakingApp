---
name: feature-specification
description: Clarifies a broad feature requirement through chat questions, then writes spec.md (always) and design.md (for new screens) with no open questions before planning.
---

# Skill — Feature Specification

## Purpose

Turn a general feature request into approved source artifacts:

- `docs/current/spec.md` — **always produced**
- `docs/current/design.md` — **produced only for new screens or major UI flows**

This skill replaces both `screen-specification` and `requirement-capture`. It adapts its depth based on the task type:

| Task Type | `spec.md` | `design.md` | Screen-specific spec sections |
|-----------|-----------|-------------|-------------------------------|
| New screen or major UI flow | ✅ Full | ✅ Full | ✅ Include |
| Enhancement to existing screen | ✅ Full | ✅ Full (if UI changed) / ❌ Skip (if logic-only) | ⚠️ Only changed parts |
| Pure logic/behavior change | ✅ Full | ❌ Skip | ❌ Skip |

Do not plan implementation slices. Do not write code. Do not invent missing product decisions.
This skill ends only when every material question has been answered by the user and all artifacts contain no unresolved assumptions or open questions.

---

## Load

- `skills/spec-driven-development/SKILL.md`
- `docs/templates/feature-spec-template.md`
- `docs/templates/feature-design-template.md`

---

## Execute

### 1. Capture The Raw Request

Read the user's request exactly as stated. Preserve concrete inputs as source-of-truth anchors, including:

- Named screens, tabs, entry points, and destinations
- User roles or audiences
- Provided screenshots, sketches, or mockups
- Required copy, labels, or product terminology
- Explicit non-goals

If the user provides a screenshot, mockup, or visual reference, save it unchanged under `docs/current/design/` and reference it from `design.md` (if produced) or `spec.md`.

### 2. Classify The Task Type

Before asking questions, determine the task type:

- **New screen**: No existing screen serves this purpose. Requires `design.md` + full `spec.md`.
- **Enhancement**: Adds or modifies behavior on an existing screen. Requires `spec.md` only (screen-specific sections only for changed parts).
- **Logic-only**: No UI changes. Requires `spec.md` only (skip all screen-specific sections).

State the classification to the user and confirm before proceeding.

### 3. Ask Clarifying Questions In Chat

Ask targeted questions before writing the artifacts. Prefer 3–7 questions per round.
Questions must be concrete and answerable, not broad prompts like "anything else?"

**Always cover:**
1. **User and goal**: who uses the feature, what they are trying to complete, and what success means.
2. **Expected behavior**: concrete, observable behaviors — each independently testable.
3. **Business rules**: constraints imposed by business logic.
4. **Scope boundaries**: what is explicitly out of scope for this version.

**Cover if new screen or enhancement with UI changes:**
5. **Entry and exit**: how the user reaches the screen/feature, what back/close/submit actions do, and where success/failure navigates.
6. **Core content**: exact data, sections, controls, copy, and required empty/loading/error states.
7. **Interactions**: taps, selection, editing, gestures, validation, confirmation, destructive actions, and disabled states.
8. **State and persistence**: local-only state, saved state, backend/API expectations, offline behavior, and configuration-change behavior.
9. **Design direction**: visual hierarchy, density, references, tone, layout constraints, and responsive behavior.
10. **Accessibility and testing**: content descriptions, focus order, dynamic text, test tags, and verifiable acceptance criteria.

If an answer creates a new ambiguity, ask a follow-up. Continue until the requirement can be written without assumptions.

### 4. Zero-Ambiguity Gate

Before writing any artifacts, verify:

- [ ] Every material product decision is answered by the user.
- [ ] No unresolved assumptions are needed to describe the intended behavior.
- [ ] Scope boundaries and non-goals are explicit.
- [ ] Every user-visible state has a defined behavior (if UI is involved).
- [ ] Every destructive or irreversible action has a defined confirmation/recovery behavior.
- [ ] The user has confirmed the clarified scope is correct.

If any item fails, ask more questions and do not write the artifacts yet.

### 5. Write `docs/current/spec.md`

Use `docs/templates/feature-spec-template.md`.

The spec file must always describe:
- Objective and user goal
- Scope (in scope / out of scope)
- Functional requirements with stable IDs
- Acceptance criteria with stable IDs
- Data and persistence requirements
- Edge cases and error states
- Explicit assumptions
- Open questions (all must be ✅ Answered)
- Verification expectations

**Conditional sections** (include only for new screen or enhancement with UI changes):
- Screen States table
- Navigation section
- Traceability to `design.md`

Do not include an "Open Questions" section with unresolved items. If there are unresolved items, return to Step 3 instead.

### 6. Write `docs/current/design.md` (New Screen Only)

**Skip this step** for enhancements and logic-only changes.

Use `docs/templates/feature-design-template.md`.

The design file must describe:
- Screen purpose and UX principles
- Information architecture and layout regions
- Component inventory
- Visual states
- Interaction states
- Navigation behavior
- Copy requirements
- Accessibility requirements
- Test tags and UI verification anchors
- Design assets and references

---

## Output

- `docs/current/spec.md` — always
- `docs/current/design.md` — only for new screens or major UI flows
- Optional preserved visual references in `docs/current/design/`

---

## Done When — ⛔ MANDATORY STOP

Present the produced artifacts to the user and confirm:

- [ ] `spec.md` exists with all required sections filled and no open questions.
- [ ] `design.md` exists (if task type is new screen) with all sections filled.
- [ ] No assumptions or open questions remain.
- [ ] The user has approved the specification (and design, if produced).
- [ ] The artifacts are ready for slice planning.

**APPROVED by user →** Return to the active workflow file and proceed to the Slice Planning stage.
