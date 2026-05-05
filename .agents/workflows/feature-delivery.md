---
description: You are a senior Android developer, to deliver a new feature.
---

## Goal
Use this workflow when implementing a new feature, enhancing an existing feature, or integrating a backend/API change into the mobile app.

This workflow is designed for production-grade mobile delivery, not quick prototyping.

---

## Core Principle

Do not jump directly into coding.

Always move through:

Requirement → Impact Analysis → Architecture → Implementation Plan → [Mandatory user review] → Tests → Risk Review → Code Changes

---

## Required workflow

### 1. Requirement Analysis & Discovery

Read the user story, ticket, product requirement, and API contract (`sharedContracts/openapi.yaml`). Read the design if it is provided.

Identify:

- business goal
- user flow
- affected screens
- affected API endpoints
- affected domain logic
- affected platforms
- feature flag requirement
- analytics or observability requirement

If the requirement is unclear, state assumptions and clarify first before proceeding.

---

### 2. Classify the Change

Classify the feature as one or more of:

- new UI feature
- API integration
- business logic change
- configuration change
- authentication/session-related change
- payment/security-sensitive change
- release/rollout change
- cross-platform consistency change

Use this classification and the **Android test triage skill** to decide the implementation and test depth.

---

### 3. API Contract Review

If API changes are involved, review the API contract before implementation.

Check:

- new fields
- removed fields
- renamed fields
- changed data type
- changed enum value
- changed nullability
- changed error response
- changed pagination
- changed authentication requirement
- changed backward compatibility

Classify the API change as:

- backward compatible
- backward compatible but risky
- breaking change

Explicitly state whether a mobile force update may be required.
- If new API responses are involved, check if they are already defined in `sharedContracts/openapi.yaml`, otherwise, update it in the openapi.yaml.
---

### 4. Existing Code Impact Analysis

Search the existing codebase for related:

- screens
- ViewModels
- use cases
- repositories
- API clients
- DTOs
- domain models
- mappers
- navigation routes
- analytics events
- tests

Before modifying code, summarize the affected areas.

---

### 5. Architecture Design

Follow the existing project architecture.

For Android, prefer:

- UI layer for rendering only
- ViewModel for UI state orchestration
- UseCase for business rules
- Repository for data access
- DTO/API model separated from domain model
- mapper from API model to domain model
- mapper from domain model to UI model if needed

Do not put business logic directly into composables or UI components.

---

### 6. State Design

Define the UI state before coding.

Include:

- loading state
- success state
- empty state
- error state
- partial data state
- retry state
- permission/auth state if applicable

Use explicit state models instead of scattered boolean flags where possible.

---

### 7. Implementation Plan

Before coding, provide an implementation plan.

Include:

- files to create
- files to modify
- domain changes
- API changes
- UI changes
- navigation changes
- test changes
- migration or compatibility handling

Present the implementation plan to the user and wait for their explicit approval before proceeding with any implementation.

---

### 8. Implementation Rules

When implementing:

- implement in small, working slices
- test and verify each slice before expanding scope
- preserve existing behavior unless the requirement explicitly changes it
- avoid unrelated refactoring
- avoid broad rewrites
- keep naming consistent with the existing codebase
- follow existing dependency injection patterns
- follow existing error handling patterns
- follow existing logging and analytics patterns
- keep changes easy to review
- follow **Android feature skill** principles for UI components
- add stable selectors (`testTag` for Compose, `id` for XML) for testability

If a larger refactor is necessary, explain why.

---

### 9. Data & API Planning

If new API responses are involved:

- check if they are already defined in `sharedContracts/openapi.yaml`, otherwise update it
- use **Shared JSON scenarios skill** to load mocks if available, otherwise generate one using the skill
- do not create mock response data inline in test cases
- define necessary DTOs and Domain Models

---

### 10. Testing Strategy

Define and implement tests according to impact. Prefer writing the failing test first for new logic, bug fixes, and behavior changes.
- Use **Shared JSON scenarios skill** to load mocks if they are available, otherwise, generate one using the skill. Don't create mock response data in the test cases.

#### Unit Tests

Use unit tests for:

- ViewModel 
- use cases
- domain rules
- mappers
- validators
- formatting logic
- error mapping

#### Integration Tests

Use integration-style tests for:

- API response to UI state, good for cross platform testing with shared JSON scenarios
- business rules across multiple layers
- loading/success/error transition
- retry behavior
- empty state handling

#### UI Tests

Use UI tests when:

- UI behavior is important
- multiple UI states must be verified
- regression risk is high
- the feature affects core user journeys

For Compose UI tests:

- test rendering from fake/mock state
- test user interaction
- test important text, buttons, and navigation triggers
- avoid testing implementation details

#### End-to-End Tests

Use E2E tests only for:

- critical business flows
- payment/login/session flows
- high-risk production paths
- flows where backend integration risk is high

---

### 11. Cross-Platform Consistency

All the features exist on both Android and iOS, identify:

- shared API contract
- shared business rules
- shared edge cases
- platform-specific differences
- test scenarios that should be aligned

If shared JSON scenarios exist, update or propose updates.

---

### 12. Observability and Analytics

Check whether the feature needs:

- analytics events
- error logs
- API latency tracking
- screen performance tracking
- crash breadcrumbs
- business funnel tracking

Do not log sensitive data.

---

### 13. Security and Privacy Review

Review:

- token handling
- sensitive data exposure
- logs
- screenshots
- clipboard usage
- deep links
- WebView usage
- payment/card data handling
- PII storage
- local persistence

If sensitive data is involved, use the safest existing pattern in the codebase.

---

### 14. Rollout and Release Review

Before finalizing, check:

- backward compatibility
- force update requirement
- feature flag requirement
- phased rollout requirement
- migration requirement
- app version dependency
- backend deployment dependency
- monitoring plan

---

### 15. Final Validation

Before completion:

- Ensure the app builds: `./gradlew assembleDebug`.
- Run all relevant tests:
  - Local tests: `./gradlew testDebugUnitTest`.
  - Instrumented tests: `./gradlew connectedDebugAndroidTest`.
- Verify UI fidelity if a design was provided.
- For UI changes, apply the **Android UI verification skill** with the cheapest reliable checks first.
- check formatting/lint if available
- review changed files using the **code-review-and-quality skill**
- verify no unrelated changes were introduced
- summarize remaining risks

---

## Final Output Format

Always provide:

1. Requirement summary
2. Impact analysis
3. API compatibility assessment
4. Architecture/design summary
5. Implementation plan
6. Test plan
7. Security/privacy considerations
8. Rollout/release considerations
9. Files changed or proposed
10. Open questions or assumptions

---

## Stop Conditions

Stop and ask for clarification only when:

- Mandatory user review after step 7
- the requirement is contradictory
- the API contract is missing and cannot be inferred
- the change may break existing production users
- security/payment/auth behavior is ambiguous
- the implementation requires choosing between major architecture options

Otherwise, proceed with reasonable assumptions and state them clearly.