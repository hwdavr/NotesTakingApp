# AGENTS.md — NotesTakingApp

This is the root context file. Read this first, then navigate to the relevant workflow.
This file is an Index & Map — not an encyclopedia. Keep it under 120 lines.

---

## Project

Kotlin + Jetpack Compose + Room Android notes app. Treat it as a production product, not a demo.

**Tech stack**: Kotlin · Jetpack Compose · Material 3 · Navigation Compose · Room · Hilt · KSP · Java/Kotlin 17 · minSdk 24 / targetSdk 34

**Module structure**:
- `app/` — Android application module
- `UX/` — design assets
- `sharedContracts/` — OpenAPI contract + shared test scenarios

---

## Context Loading — L1 / L2 / L3

Load context in layers to keep the context window below 40% fill. More is not better.

| Layer | When | What to load |
|-------|------|-------------|
| **L1 — Always** | Every session | This file + `rules/android-architecture.md` + `rules/testing-strategy.md` |
| **L2 — Phase-triggered** | Per stage | The skill(s) listed in the current stage's **Load** section |
| **L3 — On-demand** | When needed | `docs/knowledge/` docs, specific `rules/` files, `sharedContracts/openapi.yaml` |

Do not preload all rules and all skills at once. Load what the current stage requires.

---

## Harness Structure

| Folder | Purpose |
|--------|---------|
| `workflows/` | **Start here.** Pick the workflow that matches the task. |
| `stages/` | Step-by-step processes invoked by workflows. |
| `rules/` | Mandatory constraints (L1 core + L3 on-demand). |
| `skills/` | How-to guides — loaded per stage (L2). |
| `docs/templates/` | Standard output formats for plans, reviews, tests. |
| `gates/` | CI checks and review/release checklists. |
| `docs/knowledge/` | Past bugs, pitfalls, architecture decisions (L3). |
| `docs/changes/` | Audit trail — one directory per delivered change. |

---

## Workflows

| Workflow | When to use |
|----------|-------------|
| `workflows/feature-delivery.md` | New feature, enhancement, or API integration |
| `workflows/bug-fixing.md` | Bug, crash, regression, or unexpected behavior |
| `workflows/create-ui-and-verify.md` | UI implementation or update from a design |
| `workflows/api-contract-update.md` | Backend API contract change |

---

## Skills Index

Existing skills — load the SKILL.md from the relevant folder:

| Skill folder | Used in stage |
|---|---|
| `skills/context-management/` | Session start · context drift recovery · switching feature areas |
| `skills/spec-driven-development/` | Stage — Requirement, Impact & Design |
| `skills/android-ui-verification/` | Stage — UI Layer |
| `skills/android-unit-test/` | Stage — Testing |
| `skills/android-instrumented-ui-test/` | Stage — Testing |
| `skills/shared-json-scenarios/` | Stage — Testing |
| `skills/code-review-and-quality/` | Stage — Code Review |
| `skills/android-code-quality-checks/` | Stage — Code Review |
| `skills/incremental-implementation/` | Stage — Implementation Plan |
| `skills/documentation-and-adrs/` | Stage — Knowledge Capture |
| `skills/karpathy-guidelines/` | Any stage — coding discipline |
| `skills/security-and-hardening/` | Stage — Code Review (security-sensitive changes) |

---

## Non-negotiable Rules

- **No secrets in source code** — use `local.properties` + `BuildConfig`
- **No business logic in Composables**
- **No DTOs outside the data layer**
- **No hardcoded strings** — always `stringResource()`
- **All interactive elements must have `testTag`**
- **Every new feature must have tests**
- **Implementation plan must be approved by user before any code is written**
- **Every stage gate must pass before advancing** — do not skip gates

---

## Build Commands

This project runs in WSL. Ensure the correct WSL environment before running Gradle.

```bash
./gradlew assembleDebug              # build check
./gradlew testDebugUnitTest          # unit + integration tests
./gradlew koverLog                   # coverage (must be ≥ 80% overall)
./gradlew ktlintCheck                # formatting
./gradlew detekt                     # static analysis
./gradlew connectedDebugAndroidTest  # instrumented UI tests (when UI changed)
```

---

## Distribution Commands

Use the following Gradle tasks to package and distribute the application to Firebase App Distribution:

```bash
./gradlew appDistributionUploadDebug     # Package and distribute the Debug build (version: 1.0-Debug) to MyAccounts
./gradlew appDistributionUploadRelease   # Package and distribute the Release build (version: 1.0-Release) to MyAccounts
```

---

## When you find a bug in the harness itself

Fix it immediately. Update the relevant stage, rule, or gate to prevent recurrence.
Document the fix in `docs/knowledge/pitfalls/` if it could affect future changes.
