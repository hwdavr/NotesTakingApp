# Add Android Feature

You are working in an Android repository.

## Goal
Implement a new feature or modify an existing one, ensuring that business logic, data handling, and UI are all correctly implemented and verified at the appropriate test layers.

## Required Skills
- **Android feature skill**: General guidance on implementation and architecture.
- **Android test triage skill**: Determining the correct test layer (Unit, Integration, Instrumented).
- **Android integration test skill**: Testing logic, repositories, and API interactions.
- **Android instrumented UI test skill**: Verifying user-visible behavior and UI states.
- **Shared JSON scenarios skill**: (If applicable) Defining and using cross-platform API mocks.

## Required workflow

### 1. Triage the feature requirements
- Use the **Android test triage skill** to decide which layers need testing.
- Identify:
  - Pure logic (Unit tests)
  - API/Repository/ViewModel state logic (Integration tests)
  - UI rendering/interaction/navigation (Instrumented UI tests)

### 2. Inspect existing patterns
- Find existing features similar to the one being added.
- Identify the target packages for:
  - UI (Screens, Components)
  - ViewModels
  - Repositories/Data sources
  - DTOs/Domain Models
- Reuse existing architecture, themes, styles, and DI patterns.

### 3. Plan Data & API (if needed)
- If new API responses are involved, check if they are already defined in `sharedContracts/openai.yaml`, otherwise, update it in the openapi.yaml.
- Use **Shared JSON scenarios skill** to load mocks if they are available, otherwise, generate one using the skill. Don't create mock response data in the test cases.
- Define necessary DTOs and Domain Models.

### 4. Implementation Phase
- **Logic & Data**: Implement or update Repositories and ViewModels.
- **UI**: Implement the screen or update existing components.
  - Follow the **Android feature skill** principles.
  - Add stable selectors (`testTag` for Compose, `id` for XML) for testing.
- **TDD approach**: Prefer writing tests before or alongside implementation.

### 5. Add or Update Tests
- **Integration Tests**: Use **Android integration test skill** to verify:
  - API success/error handling via `MockWebServer`.
  - ViewModel state transitions.
  - Repository data mapping.
- **Instrumented UI Tests**: Use **Android instrumented UI test skill** to verify:
  - Screen rendering with mocked data.
  - Key user interactions (clicks, text input).
  - Navigation between screens.
- **Unit Tests**: For any standalone utility or business logic.

### 6. Verification & Polish
- Ensure the app builds: `./gradlew assembleDebug`.
- Run all relevant tests:
  - Local tests: `./gradlew testDebugUnitTest`.
  - Instrumented tests: `./gradlew connectedDebugAndroidTest`.
- Verify UI fidelity if a design was provided.

## Rules
- **Prefer lower-level tests**: If logic can be verified in an integration test, don't duplicate it in a UI test.
- **Stable selectors**: Always use `testTag` or `id` for UI assertions.
- **No flaky tests**: Avoid `Thread.sleep`. Use proper coroutine test dispatchers or idling resources.
- **Consistent architecture**: Match the repository's MVVM/MVI/Clean Architecture patterns.
- **Keep changes focused**: Avoid unrelated refactoring unless necessary for the feature.

## Report Result
Return:
1. Files created or updated.
2. Summary of the test coverage (which layers were used).
3. Which skills were used.
4. Key implementation decisions (architecture, DI, testing strategy).
5. Missing APIs.
6. Result of the test runs.
