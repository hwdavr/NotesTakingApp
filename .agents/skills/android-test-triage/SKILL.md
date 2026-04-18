# Android test triage skill

Use this skill to choose the correct test layer for a scenario.

## Decision rules

### Unit test
Use when:
- pure business logic
- mappers / reducers
- ViewModel logic with fakes
- no Android runtime needed

### Integration test
Use when:
- multiple non-UI components interact
- repository + API/data source
- API success/error handling
- parser + mapper + repo
- Room / database behavior
- ViewModel state transitions need validation
- no UI verification needed

### Instrumented UI test
Use when:
- user-visible behavior matters
- screen rendering from mocked data matters
- user interaction matters
- navigation must be verified
- Android runtime/UI behavior matters
- a critical flow spans screens

### Appium
Use only when:
- packaged app smoke verification is needed
- a thin top-level end-to-end flow is needed

## Key rule
If the same scenario can be verified confidently below the UI, prefer the lower layer.
