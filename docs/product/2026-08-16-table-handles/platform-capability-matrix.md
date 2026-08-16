# Platform Capability Matrix — Table Handles

## Classification

This feature has no special platform-bound capability. It uses standard Jetpack Compose rendering and local in-memory/JSON document state. No permission, hardware, network, model, locale service, or external Android API adapter is introduced.

Minimum API: 24. Target API: 34.

`feature_list.json` therefore declares `platform_validation.required: false`. The unsupported-environment policy remains `fail_loudly`; missing emulator/runtime evidence cannot be recorded as a pass. Instrumented UI tests are still required because focus, touch, modal sheets, and rendering are Android-runtime behavior.

## Runtime Matrix

| Capability / boundary | Minimum API | Target API | Owner | Input | Output | Unsupported fallback |
|---|---:|---:|---|---|---|---|
| Compose table cell focus and touch | 24 | 34 | `TableDocumentBlock` | Cell tap | Focused table target and visible handles | Fail instrumented test; no silent skip |
| Material 3 modal bottom sheet | 24 | 34 | Editor UI layer | Handle tap | Column/row/table options surface | Fail instrumented test; no silent skip |
| Local document JSON serialization | 24 | 34 | `NoteDocument` | `TableBlock` state | Backward-compatible JSON | Existing `fitToWidth=false` default |
| Existing editor auto-save | 24 | 34 | `NoteEditorViewModel` | Updated `NoteDocument` | Existing repository save | Existing editor save/error behavior |
| API-24 standard Android runtime | 24 | 34 | Android test runtime | Compose editor flow | Instrumented UI assertions | Fail loudly if runtime is unavailable |

## Required Runtime Evidence

- No real platform boundary test is required by the platform contract.
- `TableHandlesScreenTest` must run on `emulator-5554` for focus, touch, sheet, and rendering evidence.
- If the emulator is unavailable, the connected test command must fail or the slice must be marked `Blocked`; it must not be skipped.

## Unsupported Environment Policy

The policy is `fail_loudly`. A missing emulator, Android runtime, or required test environment is a failed verification or a documented `Blocked` state, never a skipped pass.
