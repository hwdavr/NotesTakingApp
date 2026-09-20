# Clean State Checklist — US-2

## Core Checks

- [x] Build/compile evidence is successful: `assembleDebug` and refreshed Android tests pass.
- [x] `ktlintCheck`, `detekt`, and `lintDebug` exit 0.
- [x] `check-full-source-rules.sh` exits 0 against the complete source tree.
- [x] Required tests run with non-zero counts and coverage passes at 82.09% project-owned line coverage.
- [x] No new suppression, baseline, exclusion, placeholder, dummy, no-op, or secret is introduced.
- [x] Changed files stay within approved Web Bookmark and harness-reference scope.
- [x] Feature metadata, lifecycle state, progress, and handoff evidence are current.
- [x] No stale visual-reference or orphan evidence artifact remains; the five-state report is current.

## Conditional Results

| Trigger | Result | Evidence or feature-specific N/A reason |
|---|---|---|
| API | `N/A — no endpoint, DTO, schema, or OpenAPI change` | `sharedContracts/openapi.yaml` unchanged. |
| ROOM | `N/A — no Room entity, DAO, migration, cache, or restart behavior change in this final flow` | Existing bookmark persistence evidence remains passing. |
| NAV | PASS | `check-journey-registry.sh --run-all`: 7/7 journeys pass. |
| UI | PASS | Visual contract, UI-verification artifact, approved mockup mappings, IME proof, anchors, and all five comparisons pass. |
| PLATFORM | PASS | Platform matrix and real `ACTION_VIEW`/`PackageManager` boundary tests pass on `Medium_Phone(AVD) - 13`. |
| OBS | PASS | Existing structured outcomes remain redacted and no new sensitive logging was added. |
| RESET | `N/A — no reset, unrelated-data deletion, cache clearing, or preference clearing was introduced` | Bookmark deletion remains confirmation-gated and covered by existing acceptance tests. |
| ADR | `N/A — no new public architecture decision beyond the recorded harness/reference-component change` | Reusable keyboard-reference behavior is documented in `docs/product/design_system.md` and the harness retrospective. |

## Final State

- Root feature commit: `5efd5e5`.
- Harness submodule commit: `1082a5e`.
- Feature tracker: `web-bookmark` → `To be reviewed`.
- Final tracked state is clean after the root submodule-pointer update and Stage 9 installation.
