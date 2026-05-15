# Implementation Plan Template

Use this template when producing the plan in **Stage 02**.

---

## Feature / Bug

> One line description of what is being implemented or fixed.

---

## Requirement Summary

> 2–3 sentences. What is being built, why, and for whom.

---

## Impact Summary

| Layer | Files Affected | Change Type |
|-------|---------------|-------------|
| UI | `path/to/Screen.kt` | modify |
| Presentation | `path/to/ViewModel.kt` | modify |
| Domain | `path/to/UseCase.kt` | new |
| Data | `path/to/Dto.kt`, `path/to/Mapper.kt` | modify |
| Navigation | `path/to/NavGraph.kt` | extend |
| Tests | `path/to/ViewModelTest.kt` | modify |

---

## API Changes

- **Classification**: backward compatible / backward compatible but risky / breaking / none
- **Force update required**: yes / no / unknown
- **Fields added**: `fieldName: Type`
- **Fields removed**: `fieldName`
- **Fields changed**: `fieldName: OldType → NewType`
- **OpenAPI Status**: <Already defined in sharedContracts/openapi.yaml / Requires update: list changes>

---

## Files to Create

| File | Purpose |
|------|---------|
| `path/to/NewFile.kt` | reason |

---

## Files to Modify

| File | What Changes |
|------|-------------|
| `path/to/ExistingFile.kt` | description of change |

---

## Files to Delete

| File | Reason |
|------|--------|
| `path/to/OldFile.kt` | reason |

---

## UiState Design

```kotlin
data class ExampleUiState(
    val isLoading: Boolean = false,
    val content: ExampleUiModel? = null,
    val error: UiError? = null,
)
```

States covered:
- [ ] Loading
- [ ] Success / Content
- [ ] Empty
- [ ] Error
- [ ] Retry
- [ ] Permission / Auth (if applicable)

---

## Test Plan

### Test Layer Selection
| Layer | Included | Reason |
|-------|----------|--------|
| Unit tests (`app/src/test/`) | ✅ / ❌ | |
| Integration tests (`app/src/test/`) | ✅ / ❌ | |
| Instrumented UI tests (`app/src/androidTest/`) | ✅ / ❌ | |

### Unit Tests
| Test Class | What It Tests |
|------------|--------------|
| `<Class>Test.kt` | Success, Error, Edge cases |

### Integration Tests
> **MANDATORY**: Each new API endpoint must have at least one integration test using shared JSON scenarios.
| Test Class | API Endpoint | Shared Scenario File |
|------------|-------------|---------------------|
| `<Class>IntegrationTest.kt` | `GET /endpoint` | `scenario.json` |

### Shared JSON Scenarios
| Scenario File | API Mock | Expected Domain | Expected UI |
|---------------|----------|-----------------|-------------|
| `scenario.json` | ✅ | ✅ | ✅ |

Location: `sharedContracts/test-scenarios/`

### Instrumented UI Tests
| Test Class | Scenarios |
|------------|-----------|
| `<Screen>Test.kt` | renders state, interactive elements |

### Coverage Target
- Overall: ≥ 80%
- New classes: ≥ 90%

### Verification Commands
```bash
./gradlew testDebugUnitTest
./gradlew koverLog
./gradlew connectedDebugAndroidTest
```

---

## Explicit Assumptions

1. <assumption>

---

## Risks

1. Risk: <what could go wrong> — Mitigation: <how to reduce it>

---

## Migration / Compatibility Notes

> Any Room migration, backward compatibility handling, or phased rollout considerations.

---

## Out of Scope

> List anything explicitly NOT being changed in this task to prevent scope creep.
