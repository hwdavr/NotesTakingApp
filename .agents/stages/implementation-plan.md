# Stage 02 — Implementation Plan

> **Routing**: When this stage is approved, return to the active **workflow** file to determine the next stage.

## Purpose
Produce a concrete, reviewable plan before any code is written.
This is the final gate before implementation begins.

---

## Load
- `skills/incremental-implementation/SKILL.md`
- `rules/testing-strategy.md`
- `docs/templates/implementation-plan-template.md`
- `request_analysis/spec.md` (Stage 01 output)

---

## Execute

1. Using all outputs from Stage 01, compile a complete implementation plan. **You MUST follow the structure and sections in `docs/templates/implementation-plan-template.md` exactly.**

2. The plan must include:
   - Files to create / modify / delete
   - Domain model changes
   - API / DTO changes and defensive handling
   - **OpenAPI Verification**: If new API responses are involved, verify if they are already defined in `sharedContracts/openapi.yaml`. If not, explicitly list the updates required in the OpenAPI spec.
   - UI / state implementation (reference the design in `spec.md`)
   - **Test plan**: (as defined in the implementation plan template). **MANDATORY**: Each new API endpoint must have at least one integration test using shared JSON scenarios.
   - Risks and mitigation

---

## Output

Write `coding/implementation_plan.md`.
Update `summary.md`: mark Stage 02 complete.

---

## Gate — ⛔ MANDATORY STOP

**You MUST stop here and present the implementation plan to the user.**

### Feedback Loop
1. **Feedback**: The user provides feedback via chat, file comments, or direct edits to the plan.
2. **Iteration**: If feedback requires changes to requirements or design, the agent **MUST return to Stage 01** to update the analysis/spec first. Then, update `implementation_plan.md` and request approval again.
3. **Approval**: The agent returns to the active workflow file and proceeds to the next stage defined there.

Do not write any code, create any source files, or call any file-editing tools until the user explicitly approves.

**APPROVED by user →** Return to the active workflow file and proceed to the next stage defined there.
