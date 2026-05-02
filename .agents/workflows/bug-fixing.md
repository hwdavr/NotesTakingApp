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

### 1. Clarify & Plan
- **MANDATORY**: Before writing any code, you MUST clarify the bug details, reproduction steps, and expected behavior with the user.
- Apply the **karpathy-guidelines**: Don't assume. Surface any confusion or tradeoffs.
- Generate a detailed implementation plan using an artifact file. Focus on surgical, minimal changes.
- Present the implementation plan to the user and wait for their explicit approval before proceeding with any implementation.

### 2. Reproduce and Diagnose
- Use the **Android test triage skill** to decide which layer the reproduction test belongs to.
- Write a failing test that reproduces the bug before touching the application code.

### 3. Implement the Fix
- Follow the **karpathy-guidelines** to make surgical changes that directly address the root cause.
- Do not engage in unrelated refactoring or "cleanups" unless strictly necessary for the fix.

### 4. Add or Update Tests
- Ensure the failing reproduction test now passes.
- Update any other affected tests to reflect the corrected behavior.

### 5. Verification & Polish
- Ensure the app builds: `./gradlew assembleDebug`.
- Run all relevant tests to ensure no regressions were introduced:
  - Local tests: `./gradlew testDebugUnitTest`.
  - Instrumented tests: `./gradlew connectedDebugAndroidTest`.

## Rules
- **Adhere to Karpathy Guidelines**: Bias toward caution over speed. Think before coding.
- **Prevent Recurrence**: A bug fix must include a test that ensures the bug does not return.
- **Keep changes focused**: Avoid unrelated refactoring.
- **No flaky tests**: Avoid `Thread.sleep`. Use proper coroutine test dispatchers or idling resources.

## Report Result
Return:
1. Files created or updated.
2. Summary of the root cause and the applied fix.
3. Which skills were used (especially how karpathy-guidelines influenced the fix).
4. Result of the test runs.