---
name: Android test triage skill
description: Guidance for selecting the optimal testing layer based on scenario complexity and requirements.
---

# Android Test Triage Strategy

This skill guides the selection of the most efficient and effective testing layer for any given development task or bug fix.

## Test Layer Selection Matrix

### 1. Unit Tests (Local JVM)
**Scope:** Isolated logic components with zero Android runtime dependencies.
**Best for:**
*   **Business Logic:** Domain rules, formatters, and validators.
*   **Data Transformation:** Mappers, reducers, and parsers.
*   **ViewModel Logic:** Pure state logic verified using fakes and mocks.
*   **Bug Fixes:** Logic errors, edge cases, nullability issues, and calculation bugs.

### 2. Integration Tests (Local JVM with MockWebServer)
**Scope:** Interactions between multiple architectural components (excluding the UI).
**Best for:**
*   **Data Layer:** Repository integration with APIs, DAOs, or local databases (Room).
*   **State Management:** Complex ViewModel state transitions and asynchronous flow coordination.
*   **Networking:** Success/error handling, retry policies, and API response mapping.
*   **Bug Fixes:** Incorrect loading/error states, broken data flows, or repository-level regressions.

### 3. Instrumented UI Tests (Device / Emulator)
**Scope:** Visual rendering and user interaction within the Android environment.
**Best for:**
*   **UI/UX Verification:** Component rendering, layout integrity, and user gestures (clicks, scrolls).
*   **Navigation:** Verifying screen transitions and deep-link routing.
*   **Runtime Dependencies:** Scenarios requiring actual Android system services or hardware features.
*   **Bug Fixes:** Visual glitches, unresponsive UI elements, and navigation crashes.

### 4. Appium E2E Tests (Black-box)
**Scope:** End-to-end verification of the production-ready application.
**Best for:**
*   **Smoke Testing:** High-level validation of the "happy path" in a release candidate.
*   **Critical Flows:** Mission-critical user journeys spanning multiple integrated systems.
*   **Full System Integration:** Bugs that only surface when the app is fully integrated with live services.

---

## The Golden Rule
**Always test at the lowest possible level.** If a scenario can be verified confidently at a lower layer (e.g., Unit instead of Integration), prefer the lower layer to ensure faster execution, higher stability, and easier debugging.
