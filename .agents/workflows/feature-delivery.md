---
description: You are a senior Android developer, to deliver a new feature.
---

## Goal
Use this workflow when implementing a new feature, enhancing an existing feature, or integrating a backend/API change into the mobile app.

This workflow is designed for production-grade mobile delivery, not quick prototyping.

---

## Core Principle

Do not jump directly into coding. Implementation Plan MUST be approved before coding. 

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

**CRITICAL RULE: You MUST stop and present the implementation plan to the user here. You MUST wait for their explicit approval before generating any code or making any file modifications. Do not proceed to the next steps or write any code until the user explicitly approves the plan.**

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
- Use the **Android test triage skill** to decide the minimum test layers needed for the change.
- Follow the golden rule from triage: always test at the lowest layer that gives enough confidence.
- Prefer automation tests including unit tests, integration tests, and UI tests over manual testing when deterministic coverage is possible.
- Separate unit test and integration test into different classes. Only integration tests involving multiple layers should be put into integration test classes, and the class name must end with `IntegrationTest`. Otherwise, just create the unit test class, and put the test cases in their respective layer. Make sure unit test covers view model and domain classes are at least 95%.
- If an API is involved, create at least one integration test.
- If an API is used by a ViewModel function, use the **Shared JSON scenarios skill** to load or generate a shared scenario and add an integration test that asserts the ViewModel-exposed `UiState` against `expected.ui`.
- If an API is used only by domain/repository/use case logic without directly changing UI state, use the **Shared JSON scenarios skill** to load or generate a shared scenario and add an integration test that asserts `expected.domain`.
- Do not create mock response data inline in test cases when a shared JSON scenario should be used.

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

You MUST stop and wait for user input in the following situations:

1. **Mandatory Plan Review (ALWAYS APPLIES):** After completing Step 7, you MUST stop. You are STRICTLY FORBIDDEN from writing any code, using file editing tools, or proceeding further until the user explicitly approves the implementation plan.
2. The requirement is contradictory.
3. The API contract is missing and cannot be inferred.
4. The change may break existing production users.
5. Security/payment/auth behavior is ambiguous.
6. The implementation requires choosing between major architecture options.

For conditions 2-6, state your questions clearly. Even if conditions 2-6 do not apply, you MUST STILL STOP for condition 1 (Mandatory Plan Review). Do not proceed to write code under any circumstances without user approval of the plan.