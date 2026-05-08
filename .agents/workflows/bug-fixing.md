---
description: You are a senior Android developer, to diagnose and fix a bug in the application.
---

## Goal
Use this workflow when investigating and fixing a defect, production issue, regression, crash, test failure, or unexpected app behavior.

This workflow prioritizes root-cause analysis over quick patching.

---

## Core Principle

Do not fix symptoms first.

Always move through:

Reproduce → Localize → Root Cause → Fix Plan → [Mandatory user review] → Minimal Change → Regression Tests → Risk Review

---

## Required workflow

### 1. Understand the Bug

Collect and summarize:

- bug description
- expected behavior
- actual behavior
- affected platform
- affected app version
- affected user segment
- affected screen or flow
- frequency
- severity
- logs/screenshots/videos
- backend/API dependency
- recent related changes

If information is missing, state assumptions.

---

### 3. Classify the Bug

Classify the bug as one or more of:

- UI rendering issue
- state management issue
- API contract mismatch
- mapper/model issue
- business logic issue
- navigation issue
- concurrency/race condition
- caching issue
- session/auth issue
- permission issue
- platform-specific issue
- backend dependency issue
- release/configuration issue

Use the classification to guide investigation.

---

### 4. Localize the Fault

Inspect the relevant code path.

Check:

- UI component
- ViewModel/state holder
- use case
- repository
- API client
- DTO/domain mapper
- local cache/database
- feature flag/config
- navigation route
- analytics/logging side effects

Do not modify code until the likely fault area is identified.

---

### 5. API and Data Validation

If API data is involved, compare:

- expected API contract
- actual API response
- DTO model
- domain mapper
- UI model
- nullability handling
- enum handling
- error handling
- empty/partial data handling

Identify whether the bug is caused by:

- backend contract issue
- mobile parsing issue
- backward compatibility issue
- invalid assumption in domain/UI layer

---

### 6. Root Cause Analysis

Before fixing, write a concise root cause. 

Use this format:

```text
Root cause:
The bug happens because [specific code/data/state issue], triggered when [condition], causing [wrong behavior].
```

Avoid vague causes like:
- "state issue"
- "API issue"
- "UI bug"
- "race condition"

Be specific.

---

### 7. Fix Strategy

Choose the smallest safe fix.

Prefer:
- fixing the root cause
- preserving existing behavior
- adding defensive handling for malformed/partial data
- improving state modeling if the bug is caused by ambiguous state
- aligning mapper/domain behavior with API contract

Avoid:
- broad refactoring
- unrelated cleanup
- hardcoded patches
- swallowing errors silently
- changing public behavior without validation

---

### 8. Implementation Plan

Before coding, provide:
- root cause
- proposed fix
- files to modify
- tests to add/update
- risk areas
- rollback considerations if relevant

Present the implementation plan to the user and wait for their explicit approval before proceeding with any implementation.

---

### 9. Regression Test Strategy

Add or update tests to prevent recurrence. 
- Use the **Android test triage skill** to decide which layer the reproduction test belongs to.
- Write a failing test that reproduces the bug before touching the application code.

---

### 10. Verify Edge Cases

Check whether the fix handles:
- null data
- empty data
- partial data
- unknown enum
- slow network
- timeout
- retry
- stale cache
- logged-out/session expired state
- app upgrade scenario
- old backend response
- new backend response

---

### 11. Security and Privacy Check

Ensure the fix does not:
- expose sensitive data
- log tokens or PII
- weaken authentication/session handling
- bypass validation incorrectly
- introduce unsafe WebView/deep link behavior
- change payment/security behavior without review

---

### 12. Release Risk Review

Before finalizing, assess:
- whether this needs hotfix release
- whether backend rollout is required
- whether feature flag/config change is required
- whether force update is required
- whether old app versions are affected
- whether monitoring is needed after release

---

### 13. Final Validation

Before completion:
- run affected tests
- run build
- verify no unrelated changes
- review diff
- confirm the fix matches root cause
- confirm regression test fails before fix and passes after fix where possible

---

### Final Output Format

Always provide:
- Bug summary
- Reproduction or simulation path
- Root cause
- Fix strategy
- Files changed or proposed
- Tests added or proposed
- Edge cases covered
- Security/privacy considerations
- Release risk
- Remaining assumptions

---

### Stop Conditions

Stop and ask for clarification only when:
- the bug cannot be understood from available evidence
- multiple root causes are equally likely and require product/backend input
- the fix may change expected business behavior
- the issue involves payment/auth/security-sensitive flow
- production hotfix decision is required


Otherwise, proceed with reasonable assumptions and state them clearly.