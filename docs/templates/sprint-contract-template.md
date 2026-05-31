# Sprint Contract Template

Use this template when producing the sprint contract in the **Requirement Analysis & Scoping** stage of the Planner agent.

---

## 🏃 Sprint Overview

*   **Sprint:** `{sprint-id}` (e.g., P05-03)
*   **Feature:** `{feature-name}` (e.g., Multi-turn Q&A conversation history)
*   **Duration:** `{sprint-duration}` (e.g., 1 sprint)

---

## 🎯 Scope

### In Scope
> Explicit list of target capabilities, user flows, and technical components to be implemented.
*   [ ] `{In-scope capability 1}`
*   [ ] `{In-scope capability 2}`
*   [ ] `{In-scope capability 3}`

### Out of Scope
> Explicit list of boundaries, exclusions, and related features deferred to future sprints.
*   *   `{Out-of-scope item 1}` (separate feature/deferred)
*   *   `{Out-of-scope item 2}` (separate feature/deferred)

---

## 👥 Roles

| Role | Responsibility | Handover Trigger |
| :--- | :--- | :--- |
| **Planner** | Define acceptance criteria, scope boundaries, and the verification plan before implementation begins. | Hands off `sprint-contract.md` to **Generator**. |
| **Generator** | Implement the component, application layers, and tests based on the planner specification. | Hands off code and passing tests to **Evaluator**. |
| **Evaluator** | Review implementation against acceptance criteria, run static analysis/tests, and verify coverage. | Hands off final APPROVED audit reports to the User. |

---

## 📐 Acceptance Criteria
> Define numbered, testable, specific, and unambiguous criteria that must be satisfied for this sprint to be complete.
1. `{Acceptance criterion 1 - e.g., UI component renders a list of exchanges as chat bubbles}`
2. `{Acceptance criterion 2 - e.g., User messages appear right-aligned and purple}`
3. `{Acceptance criterion 3 - e.g., Assistant messages appear left-aligned and dark blue}`
4. `{Acceptance criterion 4 - e.g., Each message shows a compact timestamp (HH:MM)}`
5. `{Acceptance criterion 5}`

---

## 🧪 Verification Plan
> Match each acceptance criterion above with a concrete, testable verification method (visual inspection, unit test, integration test, instrumentation).
1. `{Verification method 1 - e.g., Visual inspection of chat bubble layout and alignment}`
2. `{Verification method 2 - e.g., Run Unit tests asserting timestamp formats in HH:MM}`
3. `{Verification method 3 - e.g., Test citation expand/collapse behavior in instrumented UI tests}`
4. `{Verification method 4}`

---

## 📊 Sprint Log
> The audit trail tracking each agent's execution phase, revisions, and evaluation scores.

| Phase | Agent | Target / Outcome | Notes & Core Decisions |
| :--- | :--- | :--- | :--- |
| **Planning** | Planner | `sprint-contract.md` compiled | Criteria defined and scope boundaries set. |
| **Implementation** | Generator | `{Initial implementation / Code written}` | |
| **Review 1** | Evaluator | `{Score X/5 / Findings list}` | |
| **Revision 1** | Generator | `{Fixes applied}` | |
| **Final Review** | Evaluator | APPROVED (Score: `X/5`) | All criteria successfully validated. |
