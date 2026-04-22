# Android Architecture Rules

## Purpose
This document defines the architecture constraints for this Android project.

These rules are mandatory for:
- human contributors
- AI coding tools
- code reviewers

If a change conflicts with these rules, the change must be rejected unless the architecture document is updated in the same PR with justification.

---

## Architecture Overview

This project follows a layered architecture with feature-based organization.

Primary goals:
- maintainable code
- predictable state flow
- low regression risk
- clear separation of responsibilities
- safe AI-assisted code generation

Recommended layer flow:

UI -> Presentation -> Domain -> Data

Dependencies must only flow inward.

---

## Layer Responsibilities

### UI Layer
Examples:
- Compose screens
- Fragments / Activities
- UI rendering
- user interaction handling
- observing ViewModel state

UI layer responsibilities:
- render from `UiState`
- send user actions/events to ViewModel
- handle UI-only concerns such as focus, local animation triggers, navigation callbacks

UI layer must NOT:
- call repositories directly
- contain business rules
- parse API responses
- perform DTO to domain mapping
- access remote/local data sources directly

---

### Presentation Layer
Examples:
- ViewModels
- UI mappers
- UI event/state reducers

Presentation layer responsibilities:
- expose screen state as `UiState`
- coordinate use cases
- transform domain models into UI models
- manage loading / success / error state transitions

Presentation layer must NOT:
- call Retrofit/service/database directly
- contain persistent storage logic
- contain heavy business logic that belongs to domain

---

### Domain Layer
Examples:
- use cases
- domain models
- repository interfaces
- business rules

Domain layer responsibilities:
- define business behavior
- define repository contracts
- contain business validation and decision logic
- remain platform-independent as much as possible

Domain layer must NOT:
- depend on Android framework classes
- depend on UI classes
- depend on Retrofit/Room implementation details

---

### Data Layer
Examples:
- repository implementations
- remote data sources
- local data sources
- DTOs
- mappers from DTO to domain

Data layer responsibilities:
- fetch/store data
- map external data models to domain models
- implement repository interfaces from domain

Data layer must NOT:
- expose DTOs to presentation/UI
- contain UI state logic
- make navigation decisions

---

## Dependency Rules

Allowed:
- UI depends on Presentation
- Presentation depends on Domain
- Data depends on Domain
- app module wires dependencies together

Not allowed:
- UI depends directly on Data
- Domain depends on UI
- Domain depends on Android SDK/framework
- Presentation depends directly on remote/local data source implementations
- UI imports DTOs from data layer

---

## State Management Rules

### Single source of truth
Each screen must render from a single primary `UiState`.

Example:
- `ProfileUiState`
- `BillsUiState`
- `LoginUiState`

Avoid:
- multiple disconnected booleans spread across Fragment/Compose screen
- mixing raw repository output directly into UI rendering

---

### UiState rules
`UiState` should represent what the screen needs to render.

`UiState` should:
- be stable and explicit
- include loading / content / error information
- avoid holding Android framework objects unless truly necessary

Preferred patterns:
- single immutable data class
- sealed state when screen modes are distinct

Example:
```kotlin
data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: ProfileUiModel? = null,
    val error: UiError? = null
)
```

---

### One-off events
Use a separate event/effect mechanism for one-time actions such as:
- toast/snackbar
- navigation
- open dialog
- tracking one-time analytics event if needed

Do not encode one-time events as permanent state fields unless intentionally modeled that way.

---

## Mapping Rules

Allowed mapping:
- DTO -> Domain in Data layer
- Domain -> UI model in Presentation layer

Not allowed:
- DTO -> UI directly in UI layer
- Domain -> DTO in UI layer
- API response objects passed into Compose/Fragment directly

---

## Networking Rules

- Retrofit/API services must only be used in data layer
- request/response DTOs must stay in data layer
- repository implementation translates data source results into domain models
- error handling should be normalized before reaching presentation

---

## Concurrency Rules

- ViewModel owns screen-level coroutine coordination
- use cases may be suspend functions
- repositories may expose suspend or Flow based APIs according to project standards
- Android lifecycle concerns must not leak into domain layer

---

## Error Handling Rules

Errors exposed to UI must be transformed into UI-friendly representations.

Do not:
- expose raw backend error payloads directly to UI
- expose transport-layer exceptions directly to UI rendering code

Preferred:
- map into domain error or UI error model
- keep technical detail in logs, not screen state

---

## Testing Rules

### Required tests for new feature logic
At minimum, new behavior should include:
- ViewModel/state transition tests
- use case tests if business logic is added
- mapper tests if mapping logic is non-trivial

### Required tests for bug fixes
A bug fix must include at least one test that would fail before the fix when feasible.

---

## Package / Folder Guidance

Recommended structure:

feature/<feature-name>/
- ui/
- presentation/
- domain/
- data/

core/
- ui/
- network/
- common/
- testing/

Example:

feature/profile/
- ui/ProfileScreen.kt
- presentation/ProfileViewModel.kt
- presentation/ProfileUiState.kt
- domain/GetProfileUseCase.kt
- domain/Profile.kt
- domain/ProfileRepository.kt
- data/ProfileRepositoryImpl.kt
- data/remote/ProfileApi.kt
- data/remote/ProfileDto.kt

---

## Forbidden Patterns

The following are not allowed unless explicitly approved:

- Fragment/Compose screen directly calling repository
- ViewModel directly calling Retrofit API
- DTO used outside data layer
- business rules inside Compose screen / Fragment
- domain layer importing Android framework classes
- adding feature logic without tests
- AI-generated code merged without human review

---

## AI-specific Constraints

When AI tools modify this project, they must follow these rules:

- preserve layer boundaries
- do not move logic into UI for convenience
- do not introduce new patterns when an existing project pattern already exists
- prefer editing the smallest possible scope
- add or update tests for behavior changes
- do not refactor unrelated files unless explicitly requested

---

## PR Review Checklist

Reviewers must check:

- Does UI render from `UiState`?
- Is any business logic incorrectly placed in UI/presentation?
- Are DTOs leaking outside data layer?
- Are repository boundaries respected?
- Are tests added or updated?
- Did AI-generated code introduce unrelated changes?