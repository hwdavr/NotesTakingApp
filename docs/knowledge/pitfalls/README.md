# Pitfalls

This folder records known footguns, non-obvious interactions, and anti-patterns observed in this codebase.

Each file covers one specific pitfall that is easy to stumble into.

---

## Purpose

- Warn future engineers and agents about traps before they fall into them
- Encode institutional memory about "why we don't do X"
- Complement `rules/` files with concrete observed examples

---

## File naming

`<slug>.md`

Example: `hilt-in-compose-test-crash.md`

---

## Format

```md
# <Title>

## Symptom
<what the developer sees when they hit this>

## Cause
<why it happens>

## What to do instead
<correct approach>

## Reference
<link to commit, PR, or past-bug file>
```
