# Architecture Decisions

This folder records Architecture Decision Records (ADRs) for this project.

An ADR is written when a significant architectural or engineering decision is made that future engineers and agents should understand.

---

## When to write an ADR

Write an ADR when:
- A new pattern is introduced that others must follow
- A technology is chosen over alternatives
- A layer boundary rule is deliberately violated with justification
- A public API or contract is changed in a meaningful way
- A significant trade-off is made

---

## File naming

`ADR-NNN-<slug>.md`

Example: `ADR-001-use-hilt-for-di.md`

Increment `NNN` from the last existing ADR.

---

## Template

```md
# ADR-NNN — <Title>

## Status
Accepted / Deprecated / Superseded by ADR-NNN

## Context
<Why was this decision needed? What problem were we solving?>

## Decision
<What was decided?>

## Consequences
<What becomes easier? What becomes harder? What must now be followed as a result?>

## Date
<YYYY-MM-DD>
```
