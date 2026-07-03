---
description: You are a senior Android developer delivering a new feature end-to-end.
---

# Workflow: Feature Delivery

## When to use
- Use this workflow when you are acting as the **Generator** (Implementer) agent.
- Implementing a new feature
- Enhancing an existing feature
- Integrating a backend or API change

This workflow is for production-grade delivery — not quick prototyping.

---

## Core Principle

Do not jump directly into coding.
**The Implementation Plan must be approved before any code is written.**
**Every stage's skill must be invoked via the Skill tool — reading the SKILL.md manually is not a substitute.**
**Memory of prior approval does not bypass stages. Source of truth is on-disk artifacts in `docs/current/`. If an artifact is missing, re-run the stage via its skill.**

Pipeline: Requirement, Impact & Design → Plan → [User Approval] → Implementation → Testing → Code Quality Fix → Knowledge

---

## Stage Execution

### Stage 1 — Requirement, Impact & Design Analysis
**INVOKE** the `requirement-analysis` skill via the Skill tool (name: `requirement-analysis`). Reading the SKILL.md manually is not a substitute — the Skill tool is the required mechanism.

Output: `docs/current/spec_v<N>.md` created; `docs/current/summary_v<N>.md` updated with requirements, impacted files, API classification, and UiState/Navigation design.
Gate: requirements clear, impacted files identified, API classified, UiState/Navigation designed. Run `bash scripts/check-stage-artifacts.sh feature-delivery requirement-analysis` — must exit 0.

---

### Stage 2 — Implementation Plan ⛔ STOP
**INVOKE** the `implementation-plan` skill via the Skill tool (name: `implementation-plan`). Reading the SKILL.md manually is not a substitute — the Skill tool is the required mechanism.

Output: `docs/current/implementation_plan_v<N>.md` created; `docs/current/test_plan_v<N>.md` created; `docs/current/summary_v<N>.md` updated.
Gate: Run `bash scripts/check-stage-artifacts.sh feature-delivery implementation-plan` — must exit 0. **STOP — present plan to user. Do not proceed until user explicitly approves.**

---

### Stage 3 — Implementation (Data + Domain + UI)
**INVOKE** the `android-implementation` skill via the Skill tool (name: `android-implementation`). Reading the SKILL.md manually is not a substitute — the Skill tool is the required mechanism.

Output: All source files across Data, Domain, and UI layers created or modified; `docs/current/summary_v<N>.md` updated with Implementation stage marked complete.
Gate: `./gradlew assembleDebug` passes, all layer rules satisfied.

---

### Stage 4 — Testing
**INVOKE** the `android-testing` skill via the Skill tool (name: `android-testing`). Reading the SKILL.md manually is not a substitute — the Skill tool is the required mechanism.

Output: Unit tests, integration tests, and shared JSON scenarios created or updated; `docs/current/summary_v<N>.md` updated with test count and coverage.
Gate: tests pass, coverage targets met.

---

### Stage 5 — Code Quality Fix
**INVOKE** the `code-quality-fix` skill via the Skill tool (name: `code-quality-fix`). Reading the SKILL.md manually is not a substitute — the Skill tool is the required mechanism.

Output: All violations resolved; `docs/current/summary_v<N>.md` updated with code quality results.
Gate: `ktlintCheck`, `detekt`, `lintDebug`, and all custom check scripts exit with code 0.

---

### Stage 6 — Knowledge Capture
**INVOKE** the `knowledge-capture` skill via the Skill tool (name: `knowledge-capture`). Reading the SKILL.md manually is not a substitute — the Skill tool is the required mechanism.

Output: ADRs, past-bugs, and pitfalls recorded; `docs/current/summary_v<N>.md` finalised; `docs/current/progress.md` updated marking the task as Complete.
Gate: all knowledge artefacts produced, task marked Complete in progress file.

---

## Rollback Routes

| Failure | Return to |
|---------|-----------|
| Requirement ambiguity or Plan rejection | Requirement, Impact & Design Analysis |
| Compilation error | Implementation (Data + Domain + UI) |
| Test failure or Coverage gap | Testing (fix implementation if needed, then re-test) |
| Quality check violation | Code Quality Fix (fix root cause, re-run checks) |

---

## Human-in-the-Loop Confirmation Points

1. **After Requirement, Impact & Design Analysis** — user confirms assumptions and designs
2. **After Implementation Plan** — user approves implementation plan *(mandatory always)*