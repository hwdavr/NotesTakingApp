---
applyTo: "app/src/androidTest/**"
---

# Android instrumented test instructions

## Purpose
This folder verifies Android user-visible behavior.

Use this folder only when Android runtime or real UI rendering is required.

Most logic, API handling, ViewModel state transitions, request payload checks, and error permutations should stay in `app/src/test`.

## Choosing the right test layer
Choose the lightest UI test that gives enough confidence.

### 1. Compose Lightest UI Test
**Goal:** Verify rendering and interaction logic of a single Composable in isolation.
- **Use for:** Pure rendering from `UiState`, checking if dialogs appear/hide on callback triggers, verifying UI element visibility.
- **Rule:** Use `createComposeRule()` (NOT `createAndroidComposeRule`).
- **Setup:**
    - Initialize pure `UiState`.
    - Mock interaction callbacks (e.g., `onItemClick`).
    - No ViewModel involved.
- **Naming:** `<ScreenName>Test.kt`.
- **Example:** `FoldersScreenTest.kt`.

### 2. Compose UI Integration Test
**Goal:** Verify the wiring between the UI, ViewModel, and Repository.
- **Use for:** Ensuring that clicking a button triggers a ViewModel action that eventually updates the UI state (e.g., after a network call).
- **Rule:** Use `createComposeRule()` (NOT `createAndroidComposeRule`).
- **Setup:**
    - Use a real `ViewModel` (manually instantiated with mocked dependencies to avoid Hilt overhead).
    - Mock Repositories or use `MockWebServer`.
    - Verify the end-to-end flow within the presentation layer.
- **Naming:** `<ScreenName>IntegrationTest.kt`.
- **Example:** `SettingsScreenIntegrationTest.kt`.

### 3. Navigation / Multi-screen Test
**Goal:** Verify navigation transitions between screens.
- **Use for:** Ensuring deep links or button clicks navigate to the correct destination.
- **Rule:** Use `createComposeRule()` or `createAndroidComposeRule` if an Activity is strictly required.
- **Naming:** `NavigationTest.kt` or `<FlowName>NavigationTest.kt`.

## Decoupling for Testability (MANDATORY)
To ensure tests are stable and avoid Hilt-related process crashes:
- **Decouple UI from Hilt:** UI components should NOT fetch ViewModels using `hiltViewModel()` inside their body.
- **Pass State and Callbacks:** Pass the `UiState` and interaction lambdas (callbacks) as parameters to the Composable.
- **Wrapper Pattern:** Create a "Stateless" content Composable and a "Stateful" wrapper that connects it to the ViewModel.
- **In Tests:** Only test the "Stateless" content or provide a manually instantiated ViewModel to the "Stateful" wrapper.

## Compose UI integration test flow
```
Mock API response (MockWebServer / Mock Repository)
    ↓
Repository / Data Source
    ↓
ViewModel
    ↓
UiState (observed by UI)
    ↓
Compose UI
    ↓
Assert visible behavior
```

## Shared JSON scenario usage
When a shared scenario file exists:
- use apiMocks to control inputs
- assert expected.ui for visible outcomes

## General Rules
- **Avoid Hilt in UI tests** where possible; manual dependency injection is faster and more stable for unit/integration tests.
- **Use step { } blocks** (via Kaspresso or custom wrapper) for business-readable steps.
- **Use Screen/Page Object abstractions** to keep selectors separate from test logic.
- **Do not use Thread.sleep**; use `composeRule.waitUntil` or `composeRule.waitForIdle`.
- **Mocked data only**: Do not use real production backends.
- **Keep one main business scenario per test.**

## Good assertions
- Screen shown
- List or content rendered (verify specific text/tags)
- Button visible or enabled
- Empty/error state shown
- Destination screen shown after click (for navigation tests)


