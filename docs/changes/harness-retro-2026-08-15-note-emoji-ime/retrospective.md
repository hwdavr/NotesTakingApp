# Harness Retrospective — Note Emoji Keyboard State

## Incident

- **Trigger**: After the two-fifths-height picker refinement, the user supplied `docs/product/2026-08-15-note-emoji/visual_evidence/emoji_picker_keyboard_defect.jpg`, showing the keyboard open while the picker remained too short to expose useful search results.
- **Observed evidence**: The baseline visual test at `246805c` contained only `emojiPickerContentLightTheme`; `git show 246805c:app/src/androidTest/java/com/example/notesapp/editor/EmojiPickerVisualFlowTest.kt | rg 'fun emojiPicker|performTextInput|isImeVisible|keyboard'` returned only the content-state method. The sprint contract had only `TC-US-3-VIS-001` through `TC-US-3-VIS-003` and no keyboard-visible acceptance row.
- **Reproduced harness escape**: `bash scripts/check-stage-artifacts.sh harness-planning slice-planning docs/product/2026-08-15-note-emoji` exited 0 while the follow-up `feature_list.json` contained `emojiPickerExpandsToAvailableHeightWhenKeyboardIsVisible` but `sprint-contract.md` had no matching visual row.
- **Affected stage**: Visual acceptance traceability during planning/evaluation and the fix-pass re-verification handoff. The original application defect is already fixed in `809a838`; this retrospective addresses why the contract could not require that boundary.

## Classification And Root Cause

**Primary classification**: `WORKFLOW_GAP`.

The sprint-contract template already requires one `TC-US-*-VIS` row per visually distinct completed-flow state, but `scripts/check-stage-artifacts.sh` only checked that the final visual owner had at least one visual row. No gate compared the visual methods and evidence listed in `feature_list.json` with the sprint-contract rows. A newly added keyboard-visible verification command could therefore be recorded without a corresponding acceptance row.

The short picker itself was an `APPLICATION_DEFECT` and remains outside this harness repair. It was routed through the UI implementation workflow and is proven by `EmojiPickerVisualFlowTest#emojiPickerExpandsToAvailableHeightWhenKeyboardIsVisible`.

## Invariant

For the final visual-verification owner, every visual instrumented method listed in `feature_list.json` must have a matching `TC-*-VIS-*` row in `sprint-contract.md`, the same Test ID in `acceptance_test_ids`, and successful `connectedDebugAndroidTest` evidence before the feature can pass its artifact gate.

## Harness Change

- Added [`scripts/check-visual-evidence-contract.sh`](../../../scripts/check-visual-evidence-contract.sh), which enforces the invariant and fails on missing rows, acceptance IDs, visual methods, or successful connected evidence.
- Added [`scripts/tests/visual-evidence-contract-test.sh`](../../../scripts/tests/visual-evidence-contract-test.sh), including the old false-pass shape: a keyboard visual method in `feature_list.json` without a sprint-contract row is rejected.
- Attached the validator to [`scripts/check-stage-artifacts.sh`](../../../scripts/check-stage-artifacts.sh), the CI gate, `harness-evaluation`, and `harness-fix`; the sprint-contract template now documents the required mirror relationship.
- Synchronized the approved keyboard-visible state into [`docs/product/2026-08-15-note-emoji/sprint-contract.md`](../../product/2026-08-15-note-emoji/sprint-contract.md) as `TC-US-3-VIS-004` and recorded its existing successful evidence in [`feature_list.json`](../../product/2026-08-15-note-emoji/feature_list.json).
- No application source, product runtime behavior, lifecycle status, slice status, or implementation authorization was changed by this retrospective.

## Verification

| Command | Result |
|---|---|
| `bash -n scripts/check-visual-evidence-contract.sh scripts/tests/visual-evidence-contract-test.sh` | Exit 0. |
| `bash scripts/tests/visual-evidence-contract-test.sh` | Exit 0 — valid evidence passes; missing contract row, missing acceptance ID, and unsuccessful evidence are rejected. |
| `bash scripts/check-visual-evidence-contract.sh docs/product/2026-08-15-note-emoji` | Exit 0 — visual methods, contract rows, IDs, and evidence align. |
| `bash scripts/check-stage-artifacts.sh harness-planning slice-planning docs/product/2026-08-15-note-emoji` | Exit 0 — stage artifacts and the new visual contract gate pass. |
| `bash scripts/tests/platform-evidence-contract-test.sh` | Exit 0. |
| `bash scripts/tests/feature-lifecycle-contract-test.sh` | Exit 0. |
| `bash scripts/check-feature-lifecycle.sh` | Exit 0 — `2 feature(s), 0 in progress`. |
| `jq empty docs/product/2026-08-15-note-emoji/feature_list.json` | Exit 0. |
| `git diff --check` | Exit 0. |

No Android build or app test rerun was required for this harness-only change: no application source or Android test source changed. The existing committed product fix remains covered by the previously recorded 95/95 connected replay and 83.4701% coverage.

## Routed Items And Remaining Risk

- The user-visible keyboard layout requirement is now represented in the feature contract, but future product changes must add a distinct visual row and evidence rather than relying on a broad visual-class pass.
- The validator cannot infer an unrecorded visual state that is absent from both `feature_list.json` and `sprint-contract.md`; product/design review must still identify newly introduced states and assign them IDs.
- The callable Skill tool was not exposed in this session. The complete `harness-retrospective` instructions were read and followed directly, and this limitation is recorded for the next reviewer.
