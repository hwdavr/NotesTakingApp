---
trigger: always_on
---

# Testing Strategy Rules

## Purpose
Rules for deciding what to test, at which layer, and how much coverage is required.

---

## Test Pyramid

```
      [Instrumented UI tests]      ← smallest layer
      [Integration tests (JVM)]    ← primary verification layer
      [Unit tests (JVM)]            ← foundation
```

Always start at the lowest layer that gives enough confidence.

---

## Layer Selection Rules

### Unit tests (`app/src/test/`)
Use for:
- Business rules and domain use case logic
- ViewModel state transitions (no Android runtime required)
- Mapper logic (DTO → Domain, Domain → UI)
- Formatters, reducers, fallback logic
- UiState creation and state transition logic

Rules:
- All ViewModel tests inherit from `BaseViewModelTest`
- Class name ends with `Test.kt`
- One main scenario per test

### Integration tests (`app/src/test/`)
Use for:
- ViewModel + repository + mocked API end-to-end
- API response → repository → use case → ViewModel → UiState
- API error handling (4xx, 5xx, malformed, timeout)
- DTO parsing and domain mapping
- Cache / Room behavior when Android runtime is not required
- Retry and fallback logic

Rules:
- All ViewModel integration tests inherit from `BaseViewModelIntegrationTest`
- Class name ends with `IntegrationTest.kt`
- Use shared JSON scenarios — do not inline mock data
- If API used by ViewModel: assert `expected.ui` from shared scenario
- If API used only by repo/use case: assert `expected.domain`

### Instrumented UI tests (`app/src/androidTest/`)
Use for:
- Compose rendering that must be verified in Android runtime
- User gesture interaction
- Navigation between screens
- Critical multi-screen flows

Rules:
- Use `createComposeRule()` — not `createAndroidComposeRule` unless Activity is strictly required
- Test the stateless Composable (`Content`) — not the Hilt-wired screen wrapper
- Do not use `Thread.sleep` — use `waitUntil` or `waitForIdle`
- One main business scenario per test
- Do not use real production backend — use mocked data

Do NOT use instrumented UI tests for:
- ViewModel + repository + mocked backend verification when JVM integration tests can cover it

### Appium E2E tests (Black-box)
Use for:
- Smoke testing "happy paths" in release candidates
- Mission-critical journeys spanning multiple integrated systems
- Bugs that only surface when fully integrated with live services

---

## Coverage Requirements

| Scope | Minimum Coverage |
|-------|-----------------|
| Overall project | 80% line coverage |
| New ViewModel classes | 90% line coverage |
| New domain use case classes | 90% line coverage |
| Compose screens | excluded from coverage requirement |

Verify with:
```bash
./gradlew koverLog
./gradlew :app:koverHtmlReportDebug
```

---

## Shared JSON Scenarios

- **Mandatory**: All API endpoint must have at least one integration test using shared JSON scenarios
- Do not create mock response data inline in test code
- Store scenarios in `sharedContracts/test-scenarios/`
- Use the shared-json-scenarios skill to load or generate scenarios
- A scenario may contain `apiMocks`, `expected.domain`, and `expected.ui`
- Each test asserts the layer it owns — do not assert both in one test

---

## Mandatory Test Coverage for New Features

At minimum, every new feature must include:
- ViewModel / state transition tests
- Use case tests if new business logic is added
- Mapper tests if mapping logic is non-trivial
- At least one integration test per API endpoint involved, using shared JSON scenarios

---

## Mandatory Test Coverage for Bug Fixes

Every bug fix must include at least one test that fails before the fix and passes after. Write this test before touching application code when feasible.

**Triage by bug type:**
- **Logic/Calculation bugs**: Unit test
- **Data flow/API mapping/Error state bugs**: Integration test
- **Visual glitches/Unresponsive elements**: Instrumented UI test
- **Navigation crashes/Deep-link issues**: Instrumented UI test
