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

## User Scenarios & Testing *(mandatory)*

### User Story 1 - [Brief Title] (Priority: P1)

[Describe this user journey in plain language]

**Why this priority**: [Explain the value and why it has this priority level]

**Independent Test**: [Describe how this can be tested independently - e.g., "Can be fully tested by [specific action] and delivers [specific value]"]

**Acceptance Criterion**:

1. **Given** [initial state], **When** [action], **Then** [expected outcome]
2. **Given** [initial state], **When** [action], **Then** [expected outcome]

**Verification Plan**:
Match each acceptance criterion above with a concrete, testable verification method (visual inspection, unit test, integration test, instrumentation).

1. Visual inspection of chat bubble layout and alignment
2. Run Unit tests asserting timestamp formats in HH:MM

---

### User Story 2 - [Brief Title] (Priority: P2)

[Describe this user journey in plain language]

**Why this priority**: [Explain the value and why it has this priority level]

**Independent Test**: [Describe how this can be tested independently]

**Acceptance Criterion**:

1. **Given** [initial state], **When** [action], **Then** [expected outcome]

**Verification Plan**:
Match each acceptance criterion above with a concrete, testable verification method (visual inspection, unit test, integration test, instrumentation).

1. Visual inspection of chat bubble layout and alignment

---

### User Story 3 - [Brief Title] (Priority: P3)

[Describe this user journey in plain language]

**Why this priority**: [Explain the value and why it has this priority level]

**Independent Test**: [Describe how this can be tested independently]

**Acceptance Criterion**:

1. **Given** [initial state], **When** [action], **Then** [expected outcome]

**Verification Plan**:
Match each acceptance criterion above with a concrete, testable verification method (visual inspection, unit test, integration test, instrumentation).

1. Visual inspection of chat bubble layout and alignment
---

[Add more user stories as needed, each with an assigned priority]

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
