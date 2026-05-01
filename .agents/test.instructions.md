---
applyTo: "app/src/test/**"
---

# JVM unit and integration test instructions

## Purpose
This folder is the main verification layer for logic, ViewModel state, API handling, and data flow.

Most tests should live here because they are faster, more stable, and easier to debug than instrumented UI tests.

## Use this folder for

### Unit tests
Use unit tests for:
- business rules
- domain use cases
- mappers
- reducers
- formatting / fallback logic
- ViewModel logic that does not require Android runtime
- UiState creation and state transition logic

All ViewModel tests should inherit from **BaseViewModelTest**.

### Integration tests
Use integration tests for:
- ViewModel + repository + mocked API
- API response → repository → use case → ViewModel → UiState
- API error handling
- request parameter / request payload correctness
- retry and fallback logic
- DTO parsing and domain mapping
- cache / database behavior when Android runtime is not required

All Viewmodel integration tests should inherit from **BaseViewModelIntegrationTest**.

### Naming for Unit Tests and Integration tests
- Use **<Class>Test.kt** naming convention for Unit Tests.
- Use **<Class>IntegrationTest.kt** for Integration Tests.

## Must cover when relevant
- success response
- 4xx and 5xx response handling
- malformed payload
- empty body or partial payload
- network disconnect / timeout
- unknown enum / fallback logic
- retry or fallback behavior

## Rules
- no UI assertions
- deterministic only
- one main scenario per test
- prefer this layer before adding UI tests