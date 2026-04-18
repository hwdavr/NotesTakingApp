# Android feature skill

Use this skill when implementing or modifying Android feature code.

## Goals
- keep changes small and safe
- follow existing architecture
- add tests at the correct layer
- avoid unnecessary refactors

## Required process
1. inspect nearby code and existing patterns
2. match the screen's existing UI technology
3. keep business logic below the UI layer where practical
4. add or update tests:
   - unit tests first for logic
   - integration tests for API/data handling
   - instrumented UI tests only for user-visible behavior

## Important
- if the task involves API error handling, prefer integration tests
- if the task involves navigation or visible state changes, add instrumented UI tests when needed
- do not expand Appium coverage unless the flow is a true top-level smoke path