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
- Only integration tests involving multiple layers should be put into integration test classes, and the class name must end with **IntegrationTest** (e.g., `<Class>IntegrationTest.kt`).
- Otherwise, just create the unit test class ending with **Test** (e.g., `<Class>Test.kt`), and put the test cases in their respective layer.

### Shared JSON scenario rule
- if an API is used by a ViewModel function, create an integration test backed by a shared JSON scenario and assert the ViewModel-exposed `UiState` against `expected.ui`
- if an API is used only by domain/repository/use case logic without directly changing UI state, create an integration test backed by a shared JSON scenario and assert `expected.domain`
- a shared scenario may contain both `expected.domain` and `expected.ui`, but each test should assert the layer it owns


## Must cover when relevant
- success response
- 4xx and 5xx response handling
- malformed payload
- empty body or partial payload
- network disconnect / timeout
- unknown enum / fallback logic
- retry or fallback behavior

## Rules
- no rendered UI assertions; assert `UiState` instead
- deterministic only
- one main scenario per test
- prefer this layer before adding UI tests
- each API should have at least one integration test
- no mocking data in tests, use shared JSON scenarios instead
- Run `./gradlew koverLog` after unit tests execution and make sure the overall line coverage is not less than 80%.


