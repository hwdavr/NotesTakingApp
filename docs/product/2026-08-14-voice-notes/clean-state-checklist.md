# Clean State Checklist — voice-notes-audio-transcripts

Date: 2026-08-15

## Build & Compilation

- [x] `./gradlew assembleDebug --console=plain` — exit 0.
- [x] Compiler warning audit — no Kotlin/Java compiler warnings; the existing native-strip informational message is not a compiler warning.
- [x] `./gradlew checkDebugDuplicateClasses --console=plain` — exit 0.
- [x] `./gradlew ktlintCheck --console=plain` — exit 0.
- [x] `./gradlew detekt --console=plain` — exit 0.
- [x] Suppression audit — no new `@Suppress`, `@SuppressLint`, `tools:ignore`, lint/Detekt/Ktlint disables, baselines, or broad exclusions.

## Architecture & Standards

- [x] `bash scripts/check-architecture-rules.sh --all` — exit 0; layer/package rules are green.
- [x] `bash scripts/check-localization-rules.sh --all` — exit 0; interactive icon descriptions are localized.
- [x] `bash scripts/check-compose-rules.sh` — exit 0.
- [x] Domain isolation — moved use cases retain domain-only dependencies; Detekt passes.
- [x] Secret scan — no hardcoded feature secrets/tokens detected; configuration remains in `local.properties`/`BuildConfig`.
- [x] API alignment — N/A; no OpenAPI endpoint or DTO contract changed.

## Runtime & Stability

- [x] `./gradlew testDebugUnitTest --console=plain` — exit 0.
- [x] `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest --console=plain` — exit 0; 74/74 on API 33.
- [x] Room/file persistence and cleanup — repository/use-case integration tests pass, including rollback and direct Voice-block deletion.
- [x] Resource management — service smoke and full connected suite pass; recorder, transcript, focus, and notification listeners are released through service teardown paths.
- [x] Navigation integrity — focused Home/editor/settings/visual instrumented tests pass; full production-route visual navigation remains a documented residual.
- [x] Dispatcher discipline — service and ViewModel asynchronous work remains scoped; unit tests pass.

## Testing & Quality

- [x] `./gradlew koverLog --rerun-tasks --console=plain` — exit 0; 81.8898% aggregate application line coverage.
- [x] Per-class Kover HTML — new voice use cases are 100% line-covered; `VoiceRecorderViewModel` is 93.4% line-covered.
- [x] Mock-data discipline — no new shared API scenario is required; feature uses repository/platform seams already defined by the project.
- [x] No temporary `@Ignore`/`@Disabled` annotations were introduced.

## Observability & Logging

- [x] Service actions and failures retain structured tag/message logging without transcript/audio payloads.
- [x] No user-generated transcript content is logged.
- [x] Failure paths retain session identifiers through state metadata; no new hard-reset path was added.

## Cleanliness & State Reset

- [x] No generated build artifacts or device-only screenshots are staged outside the declared visual evidence paths.
- [x] Private visual evidence files are non-empty PNGs under `visual_evidence/`.
- [x] `git diff --check HEAD` — exit 0.
- [x] `jq empty docs/product/2026-08-14-voice-notes/feature_list.json` — exit 0; all five slices remain `passing`.

## Documentation & Handoff

- [x] `progress.md`, `session-handoff.md`, summary, both review reports, feature evidence, and product tracker are updated.
- [x] Product tracker is `To be human reviewed`; post-transition lifecycle check exited 0.
- [x] Residual risks are explicitly listed in both review reports and the handoff.
- [x] No ADR/API contract update is required; this fix pass changes implementation/test evidence only.
