# NotesTakingApp — Agent Harness

This repository uses a structured **agent harness** — a set of workflows, rules, and skills that guide AI coding agents to deliver production-quality changes safely and consistently.

This README explains how to set up and use the harness with different coding tools.

---

## Quick Start for Agents

**Antigravity IDE** injects `AGENTS.md` automatically — skip to step 2.
All other tools (Codex CLI, Claude, Cursor, Windsurf, Copilot Chat) must load context manually first.

1. **Load context** — Prompt: `Load context for the project` (uses `skills/context-management/`). *(Skip if using Antigravity — already injected.)*
2. **Pick a workflow** from `.agents/workflows/` that matches your task
3. **Follow the workflow stages** — each stage tells you what to load next and what gate to pass before advancing

Do not load all rules and skills upfront. Load only what the current stage requires.


---


## Harness Structure

```
Project Root
├── AGENTS.md                  ← Start here every session. Root map of the harness.
├── README.md                  ← This file. Setup, workflows, and building instructions.
├── .agents/                   ← Agent configuration and resources directory
│   ├── workflows/             ← Pick one per task. Entry point for all coding work.
│   │   ├── harness-planning.md
│   │   ├── harness-generator.md
│   │   ├── harness-evaluation.md
│   │   ├── feature-delivery.md
│   │   ├── feature-review.md
│   │   ├── bug-fixing.md
│   │   └── create-ui-and-verify.md
│   ├── rules/                 ← Mandatory constraints. L1 = always loaded. L3 = on-demand.
│   │   ├── android-architecture.md   ← L1: load every session
│   │   ├── testing-strategy.md       ← L1: load every session
│   │   ├── compose-rules.md          ← L3: load when UI changes
│   │   ├── api-contract-rules.md     ← L3: load when API layer changes
│   │   ├── navigation-rules.md       ← L3: load when navigation changes
│   │   ├── localization-rules.md     ← L3: load when strings change
│   │   ├── analytics-rules.md        ← L3: load when analytics events change
│   │   └── observability.md          ← L3: load when monitoring/logging changes
│   ├── skills/                ← How-to guides. Loaded per stage (L2), not all at once.
│   │   ├── spec-driven-development/
│   │   ├── incremental-implementation/
│   │   ├── ui-verification/
│   │   ├── android-unit-test/
│   │   ├── android-instrumented-ui-test/
│   │   ├── android-integration-test/
│   │   ├── shared-json-scenarios/
│   │   ├── android-code-quality-checks/  ← Code Review step 1: run tools
│   │   ├── code-review-and-quality/      ← Code Review step 2: reasoning review
│   │   ├── security-and-hardening/
│   │   ├── documentation-and-adrs/
│   │   ├── karpathy-guidelines/
│   │   ├── context-management/
│   │   └── shipping-and-launch/
│   └── gates/                 ← CI/Review/Release checklists.
│       ├── ci-checks.md
│       ├── review-checklist.md
│       └── release-checklist.md
├── harness/                   ← Shared harness assets
│   ├── templates/             ← Standard output templates for plans, reviews, and tests.
│   ├── rules-matrix/          ← Visual matrix mapping of rules to files.
│   └── scripts/               ← Validation scripts and contract test runners.
└── docs/                      ← Documentation and artifacts
    ├── current/               ← Active stage outputs (spec_v1.md, implementation_plan_v1.md, summary_v1.md).
    ├── implementation-plans/  ← Archived design and implementation plans.
    ├── knowledge/             ← Post-mortems, pitfalls, past bugs, and ADRs.
    └── changes/               ← Audit trail — one directory per delivered change.
```

---

## Workflows & How to Use Them

Workflows are the central orchestrators of this agent harness. They define a strict, step-by-step pipeline for delivering features, fixing bugs, and verifying changes. Following these workflows ensures that code is implemented with consistent quality, proper tests, and alignment with the project's architecture.

### How to Use Workflows (Execution Protocol)

Before starting any task, developers and AI agents must follow this execution protocol:

1. **Identify the Task Type**: Map your task to the correct workflow using the routing table below.
2. **Read the Workflow File**: Open and read the matching workflow file in full (e.g., `.agents/workflows/feature-delivery.md`) to understand the stages and gates.
3. **Execute Stage-by-Stage**: Perform the stages sequentially. Do not skip any stages.
4. **Invoke Skills**: Each stage designates a specific skill (e.g., `android-implementation`). Invoke that skill using the appropriate tools to guide the generation/verification.
5. **Pass Gates**: Each stage has an automated gate (e.g., `./gradlew assembleDebug` or verification scripts) and/or a human approval gate. Ensure the gate conditions are satisfied before proceeding.
6. **Handle Rollbacks**: If a gate fails (e.g., test failure, lint violation), roll back to the appropriate stage as defined in the workflow's rollback routes.

---

### Workflow Routing Table

| Category | Workflow File | When to Use / Task Type |
| :--- | :--- | :--- |
| **Complex Features** | [harness-planning.md](file:///.agents/workflows/harness-planning.md) | Clarifying requirements and planning a complex feature by breaking it into deliverable vertical slices. |
| | [harness-generator.md](file:///.agents/workflows/harness-generator.md) | Implementing complex features step-by-step from vertical slices. |
| | [harness-evaluation.md](file:///.agents/workflows/harness-evaluation.md) | Code and test review of an implemented complex change. |
| **Ad-Hoc / Simple Tasks** | [feature-delivery.md](file:///.agents/workflows/feature-delivery.md) | Standard new feature, simple enhancement, or ad-hoc integration. |
| | [bug-fixing.md](file:///.agents/workflows/bug-fixing.md) | Bug, crash, regression, or unexpected behavior. |
| | [create-ui-and-verify.md](file:///.agents/workflows/create-ui-and-verify.md) | UI implementation or visual update from a design mockup. |
| | [feature-review.md](file:///.agents/workflows/feature-review.md) | Independent code and test review before merging a change. |

---

### Detailed Workflow Overviews

#### 1. Complex Feature workflows (Harness-Based)
* **Planning ([harness-planning.md](file:///.agents/workflows/harness-planning.md))**: Creates a spec, a design, and breaks the feature down into independent, vertically-sliced implementation plans.
* **Generation ([harness-generator.md](file:///.agents/workflows/harness-generator.md))**: Executes the planned vertical slices sequentially. Each slice goes through complete implementation, unit/integration testing, and quality checks.
* **Evaluation ([harness-evaluation.md](file:///.agents/workflows/harness-evaluation.md))**: Performs a rigorous post-implementation audit to verify completeness against the original design, code quality standards, and coverage metrics.

#### 2. Feature Delivery ([feature-delivery.md](file:///.agents/workflows/feature-delivery.md))
Guides the end-to-end implementation of standard features:
* **Stage 1**: Requirement, Impact & Design Analysis (spec creation).
* **Stage 2**: Implementation & Test Planning (requires user approval).
* **Stage 3**: Implementation (Data, Domain, and UI layers).
* **Stage 4**: Testing (writing unit/integration/UI tests).
* **Stage 5**: Code Quality Fix (static analysis & lint checks).
* **Stage 6**: Knowledge Capture (recording ADRs, pitfalls).

#### 3. Bug Fixing ([bug-fixing.md](file:///.agents/workflows/bug-fixing.md))
Prioritizes reproducibility and test-driven fixes:
* **Stage 1**: Bug reproduction (writing a failing test that isolates the bug).
* **Stage 2**: Code fix implementation.
* **Stage 3**: Regression and integration testing.
* **Stage 4**: Static analysis and quality validation.

#### 4. Create UI & Verify ([create-ui-and-verify.md](file:///.agents/workflows/create-ui-and-verify.md))
Dedicated to styling and pixel-perfect layouts:
* Focuses on composing layouts from mockups.
* Uses instrumented UI tests to capture screenshots of stateless composables.
* Performs side-by-side visual comparisons against the mockup to polish elements.

#### 5. Feature Review ([feature-review.md](file:///.agents/workflows/feature-review.md))
Independent evaluation before merging a feature:
* Assesses completeness, styling, and test coverage.
* Runs code quality tooling and performs manual inspects.
* Outputs a review report outlining any findings to be fixed.

---

## Context Loading Strategy

The harness uses three context layers to keep the agent context window below 40% fill:

| Layer | When | What |
|---|---|---|
| **L1 — Always** | Every session | `AGENTS.md` + `rules/android-architecture.md` + `rules/testing-strategy.md` |
| **L2 — Per stage** | As stages advance | The skill(s) listed in the current stage's **Load** section |
| **L3 — On demand** | When needed | `docs/knowledge/`, specific `rules/` files, `sharedContracts/openapi.yaml` |

**Do not preload everything.** Load what the current stage requires, nothing more.

---

## Build Commands

```bash
./gradlew assembleDebug              # build check
./gradlew testDebugUnitTest          # unit + integration tests
./gradlew koverLog                   # coverage (must be ≥ 80% overall)
./gradlew ktlintCheck                # formatting check
./gradlew ktlintFormat               # auto-fix formatting
./gradlew detekt                     # static analysis
./gradlew connectedDebugAndroidTest  # instrumented UI tests (when UI changed)
```

---

## Distribution

```bash
./gradlew appDistributionUploadDebug     # Debug build → Firebase App Distribution
./gradlew appDistributionUploadRelease   # Release build → Firebase App Distribution
```

---

## UI Verification Process

The harness includes a pixel-accurate UI verification workflow. When a UI change is made:

1. Reference the design mockup from `UX/`
2. Implement in Jetpack Compose
3. Run instrumented UI tests to capture screenshots
4. Compare rendered output against the mockup using the `ui-verification` skill
5. Iterate on layout, colors, typography, and spacing until they match

| Design | Rendered |
|--------|----------|
| ![Design](UX/settings.png) | ![Rendered](UX/settings_rendered.png) |

---

## API Contract

The backend contract lives in `sharedContracts/openapi.yaml` and is the single source of truth for DTO shapes. Shared JSON test scenarios in `sharedContracts/test-scenarios/` are consumed by both Android integration tests and the backend test suite.
