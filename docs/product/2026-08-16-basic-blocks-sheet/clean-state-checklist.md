# Clean State Checklist — US-4

**Feature workspace**: `docs/product/2026-08-16-basic-blocks-sheet/`
**Slice**: US-4 — Auto-collapse the Basic blocks panel on outside interaction
**Checked**: 2026-08-16

## 1. Build & Compilation

- [x] **Compile Check** — `./gradlew assembleDebug` exited 0.
- [x] **Warning Check** — no active compiler warnings introduced.
- [x] **Dependency Safety** — `./gradlew :app:checkDebugDuplicateClasses` exited 0.
- [x] **Ktlint Verification** — `./gradlew ktlintCheck` exited 0 after running `ktlintFormat`.
- [x] **Static Analysis** — `./gradlew detekt` exited 0.
- [x] **Suppression Audit** — no suppression, baseline, ignore, or rule-exclusion directives added.

## 2. Architecture & Standards

- [x] **Layer Boundaries** — `bash scripts/check-architecture-rules.sh` exited 0 with 0 violations.
- [x] **Domain Isolation** — domain layer remains clean with no Android or UI imports.
- [x] **State Hoisting** — `bash scripts/check-compose-rules.sh` exited 0 with 0 violations.
- [x] **Secret Scanner** — no hardcoded secrets or credentials.
- [x] **API Alignments** — `sharedContracts/openapi.yaml` unchanged; feature uses local document state.

## 3. Runtime & Stability

- [x] **Data Persistence** — document state and focus remain intact on outside tap collapse.
- [x] **Resource Management** — no leaked listeners or background tasks.
- [x] **Navigation Integrity** — inner BackHandler and outside tap collapse open panel without interrupting editor navigation.
- [x] **Secure Sandbox** — on-device note storage; no untrusted IPC.
- [x] **Dispatcher Discipline** — main thread unblocked; coroutines dispatched properly.

## 4. Testing & Quality

- [x] **Test Run** — `./gradlew testDebugUnitTest` exited 0.
- [x] **Global Coverage** — `./gradlew koverLog` reported 83.8649% line coverage (minimum 80%).
- [x] **Feature Coverage** — ViewModel and component logic fully covered.
- [x] **Visual Reference Anchors** — US-3 visual evidence and reference-anchor verification remain valid.
- [x] **Platform Capability Matrix** — `check-platform-evidence.sh` passed for US-4.
- [x] **Real Platform Boundary** — 3/3 connected UI tests passed on `emulator-5554`.
- [x] **Mock Data Discipline** — UI tests use production stateless composable with mock states.
- [x] **TDD Cleanup** — no `@Ignore` or `@Disabled` annotations in test source.
- [x] **Android Runtime Suite** — `BasicBlocksPanelAutoCollapseTest` passed on emulator-5554.
- [x] **Android Lint** — `./gradlew lintDebug` exited 0.

## 5. Observability & Logging

- [x] **Invocation Audits** — N/A.
- [x] **Standardized Logs** — no debug print statements left behind.
- [x] **Context Payloads** — N/A.
- [x] **Warn on Hard Reset** — N/A.

## 6. Cleanliness & State Reset

- [x] **Reset Execution** — N/A.
- [x] **Idempotence** — test runs leave emulator in clean state.
- [x] **Artifact Cleanup** — screenshots saved in `visual_evidence/`.

## 7. Documentation & Handoff

- [x] **Progress Audit** — `progress.md`, `feature_list.json`, `product.md`, and `summary_US-4.md` updated.
- [x] **Session Handoff** — `session-handoff.md` written.
- [x] **ADRs & Pitfalls** — `docs/knowledge/pitfalls/2026-08-16-visual-reference-anchor-evidence.md` cited.
- [x] **Harness Lifecycle** — `bash scripts/check-feature-lifecycle.sh` exited 0: tracker status set to `To be reviewed` with 0 in progress.
