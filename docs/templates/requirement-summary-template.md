# Requirement Summary — <Feature Name>

Use this template when producing the requirement summary in the **Feature Requirement Capture** stage.

**Date**: YYYY-MM-DD
**Status**: Draft / Final

---

## Design Reference

- Original Mockup: [design_mockup.png](docs/current/design/design_mockup.png) (if design/UI is provided)

---

## User Goal

> As a [user type], I want to [action] so that [outcome].

---

## Functional Requirements

- **FR-001**: System MUST [specific capability, e.g., "allow users to create accounts"]
- **FR-002**: System MUST [specific capability, e.g., "validate email addresses"]
- **FR-003**: Users MUST be able to [key interaction, e.g., "reset their password"]
- **FR-004**: System MUST [data requirement, e.g., "persist user preferences"]
- **FR-005**: System MUST [behavior, e.g., "log all security events"]

*Example of marking unclear requirements:*

- **FR-006**: System MUST authenticate users via [NEEDS CLARIFICATION: auth method not specified - email/password, SSO, OAuth?]
- **FR-007**: System MUST retain user data for [NEEDS CLARIFICATION: retention period not specified]

---

## Success Criteria *(mandatory)*

- **SC-001**: [Measurable metric, e.g., "Users can complete account creation in under 2 minutes"]
- **SC-002**: [Measurable metric, e.g., "System handles 1000 concurrent users without degradation"]
- **SC-003**: [User satisfaction metric, e.g., "90% of users successfully complete primary task on first attempt"]
- **SC-004**: [Business metric, e.g., "Reduce support tickets related to [X] by 50%"]

---

## Edge Cases

- What happens when [boundary condition]?
- How does system handle [error scenario]?

---

## Non-Goals

- <What is explicitly out of scope — be specific>

---

## Explicit Assumptions

| # | Assumption | Risk if Wrong |
|---|------------|---------------|
| A1 | \<assumption\> | \<impact if assumption is false\> |

---

## Open Questions

All questions must be ✅ Answered before this document is approved.

| # | Question | Status | Answer |
|---|----------|--------|--------|
| Q1 | \<question\> | ⚠️ Unanswered / ✅ Answered | \<answer\> |
