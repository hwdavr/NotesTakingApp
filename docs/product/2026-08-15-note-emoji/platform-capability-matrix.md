# Platform Capability Matrix — Note Emoji

**Feature workspace**: `docs/product/2026-08-15-note-emoji/`  
**Validation mode during planning**: Contract only; no runtime result is claimed.

## Runtime Matrix

| Capability / boundary | Minimum API / environment | Target API / environment | Shipped owner and input/output contract | Planned real validation | Planning state |
|---|---|---|---|---|---|
| Unicode code-point persistence | API 24 emulator/device | API 34 emulator/device | `NoteEditorViewModel` receives selected Unicode text and writes it through existing `NoteDocument`/`RichText`; output is existing note JSON and exports. | JVM document/repository integration plus Android editor flow. | Planned |
| Compose editable-text cursor/selection | API 24 emulator/device | API 34 emulator/device | Existing Compose `TextField` selection offsets enter `NoteEditorViewModel`; output is updated body text and cursor after exact inserted Unicode sequence. | `NoteEditorEmojiPickerTest` on connected Android runtime. | Planned |
| Android emoji font glyph rendering | API 24 emulator/device | API 34 emulator/device | Android `Paint.hasGlyph` evaluates a selected catalog Unicode sequence from the shipped picker/text path; output is a real device-font glyph capability result. | `TC-US-3-REAL-UNICODE`: `EmojiPickerPlatformTest#unicodeEmojiHasGlyphOnAndroidRuntime` using `connectedDebugAndroidTest`. | Planned |
| DataStore Recent persistence | API 24 emulator/device | API 34 emulator/device | `RecentEmojiRepository` stores/reloads the exact selected Unicode sequence locally; output is ordered Recent UI data. | DataStore integration test plus real Android picker flow. | Planned |
| M3 sheet/accessibility/layout | API 24 emulator/device | API 34 emulator/device | `NoteEditorScreen` composes the standard sheet, category rail, grid, disabled semantics, and test tags; output is accessible rendered UI. | Focused connected Compose tests and US-3 visual captures. | Planned |

## Device Resource Ownership

| Resource | Single owner | Input | Output | Fallback |
|----------|--------------|-------|--------|----------|
| Android system emoji font | Android platform text renderer, invoked by the shipped Compose editor | Unicode emoji sequence from the bundled catalog | Rendered glyph or platform fallback glyph | Preserve exact Unicode string even if the font lacks a glyph; do not replace, discard, or corrupt note text. |
| DataStore preferences file | `RecentEmojiRepository` data implementation | Exact selected Unicode sequence | Device-local ordered Recent list | Treat read failure as empty Recent; catalog and insertion stay available. |
| Note document storage | Existing `NoteRepository`/Room/sync pipeline | Existing JSON containing Unicode RichText | Saved/reloaded/synced/exported note content | Existing local-save behavior; no new network dependency. |

## Unsupported Environment Policy

**Policy**: `fail_loudly`.

- Runtime verification uses a connected Android emulator (`ANDROID_SERIAL=emulator-5554`) for API 24 minimum compatibility, API 33 verification baseline, and API 34 target compatibility where configured.
- A missing emulator, an unavailable configured API level, inability to run `connectedDebugAndroidTest`, or a failing Android `Paint.hasGlyph` assertion causes the exact command to exit non-zero. The slice is marked `Blocked` or `Revise`; it is never marked passing.
- JVM/fake catalog tests supplement but cannot replace `TC-US-3-REAL-UNICODE`. The real test must call the shipped Android boundary, assert a device-font result, and record successful connected-test evidence in `feature_list.json` before evaluation can pass.
- The product fallback is data-safe, not a test exemption: unsupported glyph display may use the platform fallback glyph, but the original Unicode code points remain unchanged in notes, sync, sharing, and export.

## Evidence Required Before Evaluation

| Test ID | Required runtime proof | Result required |
|---|---|---|
| TC-US-3-REAL-UNICODE | `EmojiPickerPlatformTest` runs on the connected Android runtime and calls `Paint.hasGlyph` for catalog default and selected skin-tone sequences. | `connectedDebugAndroidTest` exit status 0; recorded evidence. |
| TC-US-3-VIS-001 | Production picker content state is asserted and exported to `visual_evidence/emoji_picker_content_light.png`. | Non-empty screenshot and prior state assertion pass. |
| TC-US-3-VIS-002 | Production read-only disabled state is asserted and exported to `visual_evidence/emoji_read_only_light.png`. | Non-empty screenshot and prior state assertion pass. |
| TC-US-3-VIS-003 | Production empty-search state is asserted and exported to `visual_evidence/emoji_empty_search_light.png`. | Non-empty screenshot and prior state assertion pass. |
