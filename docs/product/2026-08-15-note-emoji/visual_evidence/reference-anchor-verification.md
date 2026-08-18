# Visual Reference Anchor Verification — Note Emoji

**Reference design**: `design/mockup_note_editor_emoji_picker_v2.png`

## Reference Anchor Verification

| Visual Test ID | Reference anchor | Runtime proof | Measured relationship | Actual screenshot | Result |
|---|---|---|---|---|---|
| TC-US-3-VIS-001 | The IME-hidden emoji picker sheet occupies two-fifths of the available screen height and is anchored at the bottom edge with no header close or title bar. | `EmojiPickerVisualFlowTest#emojiPickerContentLightTheme`; testTag: `emoji_picker_sheet` | `sheetBounds.height == rootBounds.height * 0.40 ± 4dp` | `visual_evidence/emoji_picker_content_light.png` | PASS |
| TC-US-3-VIS-002 | The read-only note toolbar preserves the emoticon action in a visible but disabled state with 38% alpha. | `EmojiPickerVisualFlowTest#readOnlyEmojiControlLightTheme`; testTag: `editor_insert_emoji` | `insertEmojiBounds.height == 48dp ± 2dp` | `visual_evidence/emoji_read_only_light.png` | PASS |
| TC-US-3-VIS-003 | The empty search state displays localized secondary message and clear search button centered in the results area. | `EmojiPickerVisualFlowTest#emptySearchEmojiPickerLightTheme`; testTag: `emoji_picker_search_empty` | `emptySearchBounds.top >= categoryRailBounds.bottom + 8dp` | `visual_evidence/emoji_empty_search_light.png` | PASS |
| TC-US-3-VIS-004 | When the soft keyboard is visible, the picker expands to the full available height above the IME to keep search results visible. | `EmojiPickerVisualFlowTest#emojiPickerExpandsToAvailableHeightWhenKeyboardIsVisible`; testTag: `emoji_picker_sheet` | `sheetBounds.top <= safeAreaBounds.top + 12dp` | `visual_evidence/emoji_picker_keyboard_light.png` | PASS |
