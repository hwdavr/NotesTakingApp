# AGENTS.md — NotesTakingApp

This repository is a Kotlin + Jetpack Compose + Room Android app scaffold. Treat it as an early-stage product app, not a throwaway demo.

## Purpose

Build and refine a notes app that is:
- visually polished
- architecturally clean
- testable
- easy for both humans and coding agents to extend

The current repo is small, but changes should still preserve maintainability.

## Agent mode

The primary coding workflow for this repo uses **antigravity**.
When working in this project:
- prefer clear, structured tasks over vague open-ended prompts
- keep plans and summaries concise
- make deterministic validation part of the normal flow
- do not rely on “it should work” without verification when verification is available

## Current project shape

Current known structure:
- `app/` — Android application module
- `UX/` — design source SVGs for target screens
- root Gradle files — project build configuration

Current known stack:
- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Room
- KSP
- Java/Kotlin target 17
- minSdk 24 / targetSdk 34 / compileSdk 34

Current known status from README:
- bottom navigation exists
- Notes / Folders / Settings UI scaffold exists
- Room database setup exists
- seed data exists
- next likely work includes ViewModels, note editor flow, folder actions, and closer UI matching

## General coding rules

### 1. Keep the app easy to reason about
Prefer:
- small composables
- explicit state
- predictable navigation
- simple data flow

Avoid:
- putting business logic directly inside composables
- hidden side effects
- large god files
- unnecessary abstraction for a small app

### 2. Prefer incremental changes
For feature work:
- make the smallest coherent change that moves the feature forward
- keep diffs reviewable
- avoid mixing unrelated refactors into feature work unless necessary

### 3. Respect the app’s likely architecture direction
Even though the repo is still small, bias toward this layering:
- **UI layer**: composables, screen state rendering, event wiring
- **state layer**: ViewModels / UI state holders
- **data layer**: Room entities/DAO/repository access

Target direction:
- Composables should mostly render state and emit events
- ViewModels should coordinate screen state and user actions
- Room/data access should stay outside UI code

### 4. Match the provided UX assets when implementing screens
The `UX/` folder is a design hint and should be used when implementing or polishing screens.
When making UI changes:
- preserve the intended structure from the SVGs
- match spacing, hierarchy, and emphasis as closely as reasonable
- do not overfit decorative details if it harms maintainability

## Build and validation rules

### Baseline checks
After meaningful code changes, prefer running:
- `./gradlew assembleDebug`
- `./gradlew test`

If lint or additional checks are added later, include them in the normal validation flow.

### When touching UI
If a change affects screen behavior or navigation, also verify at least one of:
- preview still renders sensibly
- screenshot/snapshot test passes, if available
- UI automation/instrumented test passes, if available
- emulator/manual validation path is documented in the task summary

### No fake confidence
Do not claim a screen, interaction, or persistence flow works unless one of the following is true:
- it was verified by a build/test/check
- it was verified through automation
- it was explicitly marked unverified

## Harness engineering rules for this repo

This project should gradually become easier for agents to work in safely.

### Add structure when mistakes repeat
If the same type of mistake happens more than once, prefer encoding it into the repo through:
- clearer docs
- a new rule in this file
- test coverage
- debug entry points
- stable selectors / test tags

### Prefer deterministic checks over self-evaluation
If a behavior can be checked by:
- build
- test
- screenshot comparison
- UI automation

then use that rather than relying on agent self-assessment.

### Important interactions should become testable
If a change introduces or modifies important interactions, strongly consider adding:
- `Modifier.testTag(...)` for key elements
- native UI tests for important button clicks/navigation
- screenshot tests for important visual states

Examples of important interactions:
- open note
- create note
- save note
- delete note
- archive toggle
- folder selection
- navigation between Notes / Folders / Settings

## UI automation guidance

If this repo adds UI automation, prefer **native Android test tooling first**.
For this app, that usually means:
- Compose UI tests
- instrumented tests
- screenshot tests

If the goal is only visual verification:
- prefer screenshot/snapshot-style testing when possible

If the goal is interaction verification:
- prefer Compose UI tests with stable test tags

### Test tag guidance
For important UI nodes, use stable semantic names such as:
- `notes_tab`
- `folders_tab`
- `settings_tab`
- `new_note_button`
- `save_note_button`
- `delete_note_button`
- `archive_toggle`
- `folder_picker`
- `note_editor_screen`

Use names that describe product meaning, not visual placement.

## Navigation and debugability

As the app grows, prefer making important screens easier to reach in tests.
Helpful patterns include:
- clear Navigation Compose routes
- direct navigation helpers for tests
- debug-only shortcuts or deep links when useful
- deterministic fake/seed data for test scenarios

This helps both humans and agents verify UI changes without brittle multi-step setup.

## Scope discipline

Unless a task explicitly requires it, avoid unrelated edits to:
- Gradle/plugin versions
- app id / namespace
- broad theming redesign
- repository-wide package moves
- large-scale refactors

For feature work, keep focus on the relevant screen, state, and data flow.

## Good task pattern for this repo

A good implementation task usually follows this order:
1. understand the target screen/flow
2. inspect related UI and data code
3. decide minimal architecture additions needed
4. implement UI/state/data changes
5. add or update test tags/tests if interaction risk is meaningful
6. run build/tests
7. summarize changed files, validation, and any known gaps

## What to optimize for

Optimize for:
- clarity
- correctness
- maintainability
- design fidelity where it matters
- agent-friendly structure

Do not optimize for:
- cleverness
- unnecessary abstractions
- large speculative architectures
- visually perfect but brittle code

## If cross-platform parity matters later

If this app eventually needs Android/iOS parity testing as part of a broader product, keep this repo’s responsibility focused on:
- strong native Android tests
- stable semantic test identifiers
- clear behavioral contracts

Cross-platform Appium-style parity should be a thin outer layer, not the primary Android verification layer.
