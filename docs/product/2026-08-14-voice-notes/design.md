# Feature Design — Voice Notes & Audio Transcripts

**Date**: 2026-08-14
**Status**: Final
**Source request**: Plan Voice Notes & Audio Transcripts feature (unlimited recording, on-device progressive STT, inline editor block, Home FAB entry, Settings storage management)
**Related spec**: `spec.md`
**Project design system**: `docs/product/design_system.md`
**Approved design-system exceptions**: None — all colors, typography, spacing, shapes, and component families strictly reuse the existing tokens and patterns from the project design system.

---

## Screens Covered

| # | Screen / Surface | Status |
|---|-----------------|--------|
| 1 | Voice Recorder Screen (Full-screen immersive) | New |
| 2 | HomeNotesScreen — FAB Mini Sheet (Text Note / Record Note) | Updated |
| 3 | NoteEditorScreen — Mic Toolbar Icon + Inline VoiceNote Document Block | Updated |
| 4 | SettingsScreen — Voice Notes Section (format toggle + storage stats) | Updated |

---

## Screen 1 — Voice Recorder Screen (Full-screen Immersive)

### Purpose

The full-screen immersive recorder is the primary capture surface for voice notes. It removes all editor chrome and navigation distractions so the user can focus on speaking. It visualizes the live recording amplitude, shows elapsed time, and displays progressive transcript text in real time, giving the user confidence that both audio and text are being captured correctly.

### UX Principles

- **Minimal chrome, maximum focus**: Large primary controls at the bottom thumb-reach zone; status/timer and waveform at top; live transcript scrolls naturally in the middle 60% of screen.
- **Light-theme continuity**: The recorder uses the project baseline `background = #F8F7FF` and `surface = #FFFFFF`, with `primary = #7C6CF2` for the record action and waveform so the new flow feels like part of the existing app shell.
- **Recording status is unmistakeable**: Pulsing red dot + "REC" label always visible when in progress; paused state shows a solid pill-shaped "PAUSED" badge in accentYellow. No way to confuse recording vs. idle.

### Entry And Exit

- **Entry points**:
  - (A) NoteEditorScreen → Mic icon (`testTag = editor_mic_btn`) → navigates forward to VoiceRecorderScreen route with `noteId = currentNoteId` argument.
  - (B) HomeNotesScreen → existing HomeAddButton tap (`testTag = home_add_fab`) → HomeFabMiniSheet → "Record Note" option → navigates to VoiceRecorderScreen with `noteId = null` (a placeholder note will be allocated before recording starts by the ViewModel).
- **Primary success exit**: User taps Stop button → Saving spinner shows (300 ms minimum display time even for fast saves) → pops to NoteEditorScreen for the target note with the new VoiceNote block already inserted.
- **Cancel/back behavior**: System Back gesture OR Back arrow in top-left → triggers the same confirmation dialog as Discard button: title "Delete this recording?", message "All unsaved audio and transcript will be lost." Buttons: Cancel / Discard. Discard → pops to the entry screen. Cancel → stays on recorder with session unchanged.
- **Failure exit or recovery**: Storage-full pre-flight → blocking error dialog with only OK button; on OK → navigates back to entry screen. Mid-recording IO error → auto-stop + partial save + snackbar "Recording stopped: storage full. Saved [duration]" → proceeds to success exit (whatever could be saved is saved; no data lost).

### Information Architecture

Top-to-bottom layout in a full-screen `Scaffold` with light `background = #F8F7FF` and white elevated surfaces, with no app bottom navigation bar:

1. **Top Status Bar (48dp)**: Left-aligned `testTag = recorder_close` Back arrow (`textPrimary = #191627`). Center: Recording status pill on `surface = #FFFFFF` — when recording in progress, a red `• REC [elapsed HH:MM:SS]` badge using `error = #C44A4A`; when paused, a pill "PAUSED" in `accentYellow = #FFD66B`; when idle/ready, "Ready to record" in `textSecondary = #7B7694`. Right-aligned optional warning icon `testTag = recorder_stt_warning` (hidden by default; shown with `contentDescription = "Transcription unavailable"` when on-device model is missing, colored `accentYellow = #FFD66B`).
2. **Waveform + Timer Zone (192dp)**: Centered vertically above transcript. Top half = large elapsed timer in `headlineLarge` / 40sp, bold, `textPrimary = #191627` — formatted `MM:SS` under 1 hour and `HH:MM:SS` otherwise. Bottom half = live waveform bar graph: 64 vertical amplitude bars, each width = 3dp, max height = 72dp, horizontal gap = 2dp, rounded corners, tinted with `primary = #7C6CF2` and `secondary = #9B8CFF` only; no gradient. Bars animate on every audio frame (~30 fps). Paused = bars freeze at last value and dim to 50% alpha.
3. **Live Transcript Preview (flexible / vertical scroll)**: Scrollable `LazyColumn` with `testTag = recorder_transcript_preview` starting empty and appending partial text chunks as `SpeechRecognizer` partial/final results arrive. Text uses `bodyLarge` 16sp, `textPrimary = #191627`, line-height 22sp, paragraph gap 8dp. Partial chunks append inline and show a blinking cursor (▎) at end when recognizer is actively producing a chunk. Paused = cursor dims to 38% alpha; resumed = cursor re-brightens. If STT unavailable, shows a centered hint "Transcription unavailable — audio still saves. You can type the transcript later after saving." in `textSecondary = #7B7694` with `testTag = recorder_stt_unavailable_hint`.
4. **Bottom Control Bar (112dp bottom safe zone + 24dp top padding = 136dp)**:
   - Row centered horizontally.
   - Left: `testTag = recorder_discard_btn` — Discard. Material outlined circular icon button, 56dp, stroke `border = #E7E3F6`, icon = `Icons.Outlined.Close`, tint `textPrimary = #191627`. 48×48dp minimum touch target.
   - Center: `testTag = recorder_toggle_record_btn` — Pause/Resume. When Recording in progress → shows Pause icon in a 72dp filled circle, background = `surface = #FFFFFF`, icon tint `primary = #7C6CF2`. When Paused → shows Play/Resume icon in the same circle. This is NOT the final save.
   - Right of center but inside same control row: `testTag = recorder_stop_btn` — Stop-and-save. 64dp filled circular button, background = `primary = #7C6CF2`, icon = filled checkmark `Icons.Filled.Check`, icon tint `onPrimary = #FFFFFF`.
   - Right: `testTag = recorder_recording_duration_chip` — informational chip only; displays current format and bitrate "AAC · 128 kbps" or "OPUS · 32 kbps" in `textSecondary = #7B7694`, 12sp, in a small rounded outline chip using `border = #E7E3F6`. Non-interactive.

Component spacing between controls: 32dp between Discard/Pause circle, 24dp between Pause circle and Stop circle, 32dp between Stop circle and info chip.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|-----------|---------|-----------------|----------|
| Recorder Close Back Arrow | Dismiss recorder with discard confirmation | default / pressed (ripple) | `recorder_close` |
| Recording Status Pill | Badge showing recording / paused / ready | Rec(ording): pulsing red dot + time; Paused: yellow accent PAUSED; Ready: grey text | `recorder_status_pill` |
| STT Unavailable Warning Icon | Warns user transcription off; non-interactive | visible (yellow) / hidden | `recorder_stt_warning` |
| Elapsed Timer Text | Large HH:MM:SS / MM:SS counter | In progress: bright; Paused: slightly dim (80% alpha); Ready: `00:00` | `recorder_elapsed_timer` |
| Waveform Bars (64) | Live amplitude visualization | Animating (primary/secondary bars) / Paused frozen (50% alpha) / Idle (flat zero-height) | `recorder_waveform` |
| Transcript Preview LazyColumn | Live transcript text append area | Scrolling with content / Empty (STT disabled hint) | `recorder_transcript_preview` |
| STT Unavailable Hint Text (in empty column) | Explain no transcript will be made | Hidden by default; visible only when STT missing | `recorder_stt_unavailable_hint` |
| Discard Button (Outlined Circle) | Discard recording with confirm dialog | default / pressed / disabled (during Saving state) | `recorder_discard_btn` |
| Pause/Resume Toggle Button (Filled Circle) | Toggle recording pause/resume | Recording→shows Pause icon; Paused→shows Resume icon; disabled during Saving | `recorder_toggle_record_btn` |
| Stop-and-Save Button (Filled Circle Primary) | Save and navigate to editor | default / pressed / disabled during Saving state (38% alpha) | `recorder_stop_btn` |
| Format Info Chip | Display current capture format/bitrate | Display only (non-interactive); AAC label vs OPUS label | `recorder_format_chip` |
| Saving Spinner (Overlay) | Shown while flushing audio file to disk | Visible for ≥ 300ms minimum during Stop | `recorder_saving_spinner` |
| Storage Full Error Dialog | Pre-flight failure blocking dialog | Shown only at start when free space < 128MB | `recorder_storage_full_dialog` |

### Visual States

| State | User Sees | User Can Do |
|-------|-----------|-------------|
| Loading (Pre-flight) | Centered `CircularProgressIndicator` on `background = #F8F7FF`; text "Checking microphone & storage…" below | Nothing — all controls disabled until checks complete (≤ 400 ms) |
| Ready (Pre-Record) | Timer `00:00`, waveform flat, transcript area empty or STT-unavailable hint; status pill = "Ready to record" in textSecondary; Pause button → Start icon (recording hasn't begun? Actually: Ready state auto-starts on entry. Skip explicit Ready state — recorder begins immediately after permission/pre-flight success. Merges into In-Progress for simpler UX.) | Auto-start; no explicit Ready tap required. |
| In-Progress | Red `• REC 00:42` status pill; waveform bars animating in primary/secondary; transcript actively appending with blinking cursor; Pause-button = Pause icon. Foreground notification visible. | Pause, Stop/Save, or Discard. Back button = Discard with confirm. |
| Paused | "PAUSED" yellow pill; waveform frozen dim; timer frozen value; cursor dim; Pause-button → Resume (play triangle) icon; notification shows Resume action. | Resume, Stop/Save, or Discard. |
| Saving | Overlay dim with centered CircularProgressIndicator + text "Saving audio and transcript…"; all buttons disabled (38% alpha, non-clickable). | No user actions possible. |
| Error: Storage Full | Blocking M3 AlertDialog: title "Not enough storage", message "Free at least 128 MB on your device to record voice notes.", button OK only. | Tap OK → navigates back. |
| Error: No Mic Hardware | Entry path from Mic/FAB already disabled with `enabled=false`; if somehow reached, dialog "Microphone unavailable" → OK → back. | N/A |
| Error: STT Model Missing | Top status bar shows yellow warning icon `recorder_stt_warning`; transcript area shows "Transcription unavailable" hint text. Recording proceeds normally; only transcript is skipped. | Still record, Pause, Stop, Discard normally. |

### Interaction Rules

- **Primary action**: Stop-and-Save button (`recorder_stop_btn`) = saves current audio + transcript, inserts block, navigates to editor. Large 64dp primary-filled circle, most visually prominent control (purple/violet).
- **Secondary actions**:
  - Pause/Resume toggle — pauses or resumes MediaRecorder and SpeechRecognizer chunk loop; notification buttons mirror the same actions.
  - Discard — opens "Delete this recording?" confirmation dialog (2-button AlertDialog: Cancel, Discard).
  - Close (back arrow) — identical to Discard.
- **Validation**: Pre-flight storage ≥ 128 MB must pass before any recording begins; otherwise, error dialog blocks entry. `RECORD_AUDIO` permission must be granted (permission flow happens before this screen is visible; if permission was revoked while on this screen, next control press surfaces the rationale again).
- **Destructive actions**:
  - Discard requires AlertDialog confirmation with explicit copy mentioning that both audio and transcript are lost.
  - Context-switch (active Home recording → Editor Mic tap) silently discards old recording — NO confirmation per user's explicit choice; implemented in `RecordingSessionManager` logic, visible only as a fast screen transition to the new recorder.
- **Gestures**: Vertical swipe within transcript area scrolls the preview text. Horizontal swipe has no gesture (no conflict with system back). All primary actions via buttons — no gesture-only controls.

### Copy Requirements

All strings via `stringResource()` — no hardcoded text.

| Element | Copy |
|---------|------|
| Status pill (in progress) | `• REC %s` with formatted elapsed time, e.g. `• REC 03:24` |
| Status pill (paused) | `PAUSED` |
| Status pill (ready) | `Ready to record` |
| Live transcript hint (no STT) | `Transcription unavailable — audio still saves. You can add the transcript manually after saving.` |
| Permission rationale dialog title | `Microphone access` |
| Permission rationale message | `Voice Notes needs microphone access to record your voice. Audio and transcripts are stored privately on your device only.` |
| Permission rationale positive button | `Grant` |
| Permission permanent-deny snackbar | `Microphone permission denied. Open Settings to grant access.` |
| Permission snackbar action | `Open Settings` |
| Discard confirmation title | `Delete this recording?` |
| Discard confirmation message | `All unsaved audio and transcript will be lost.` |
| Discard confirm button (destructive) | `Discard` |
| Discard cancel button | `Cancel` |
| Storage full error title | `Not enough available storage` |
| Storage full error message | `Free at least 128 MB of internal storage on your device to record voice notes.` |
| Storage full button | `OK` |
| Saving overlay text | `Saving audio and transcript…` |
| STT warning contentDescription | `Transcription unavailable. Audio will still be saved.` |
| Discard button contentDescription | `Discard recording` |
| Pause button contentDescription | `Pause recording` |
| Resume button contentDescription | `Resume recording` |
| Stop button contentDescription | `Save recording and insert into note` |
| Close button contentDescription | `Close recorder (discard)` |

### Accessibility

- Every icon button has `contentDescription` from Copy Requirements table and `Modifier.minimumInteractiveComponentSize()` → 48×48dp (actual visible icon circles larger — inner interactive bound guaranteed).
- Waveform bar graph marked `Modifier.semantics { contentDescription = "Live amplitude waveform; visually indicates recording level" }` — `invisibleToTalkBack()` if TalkBack prefers reading just timer and status, not per-bar; default leaves the content description available.
- Transcript preview area: `semantics(mergeDescendants = true) {}` so TalkBack reads the entire concatenated transcript as one text region instead of per-line.
- Status pill: when recording, announces "Recording, elapsed HH hours MM minutes SS seconds" via liveRegion; when paused announces "Recording paused".
- Saving overlay: `progressSemantics` with label; blocks all control semantics when displayed.
- Minimum color contrast: textPrimary #191627 on background #F8F7FF and onPrimary #FFFFFF on primary #7C6CF2 meet the design-system contrast targets; all status and destructive colors are paired with iconography and text.
- Dynamic font scaling: Timer text size caps at 1.8× system scale to prevent overflow; waveform height scales with display density but not font scale (visual-only).
- Focus order (keyboard/TalkBack traversal): Close → Status → STT warning → Timer → Waveform → Transcript → Discard → Pause/Resume → Stop → Info Chip — follows natural screen reading top-to-bottom, left-to-right, then bottom controls left-to-right.

### Responsive And Configuration Behavior

- **Portrait (default)**: Layout as described; waveform max 72dp bar height, 64 bars.
- **Landscape**: Timer and waveform swap to left 40% / right 60% split row at top (30% screen height max); transcript spans full width below; bottom control bar centered horizontally. Waveform = 42 bars but taller (max 96dp) to use the wider landscape space.
- **Tablets (≥ 600dp sw)**: Max recorder content width constrained to 560dp centered (keeps controls reachable); transcript horizontal padding 32dp instead of 16dp on phones.
- **Configuration change (rotation, split-screen, multi-window)**: Recording session owned by foreground service survives config change; ViewModel `SavedStateHandle` preserves elapsed, pause state, transcript so UI reconnects seamlessly without data loss or re-composition glitches.

### Design Assets

- **Mockup image**: `design/mockup_recorder_screen_v3.png` — AI-generated visual mockup of the full-screen immersive Voice Recorder in the Recording (in-progress) visual state showing waveform, live transcript, and bottom control trio (Discard/Pause/Stop). Uses the updated Light Theme baseline: `background = #F8F7FF`, `surface = #FFFFFF`, `primary = #7C6CF2`, `textPrimary = #191627`, and no gradients.

### Out Of Scope For This Design

- Speaker labels or multi-party recording indicators (diarization is out of scope).
- Playback-from-recorder preview (playback only happens inside editor's inline player after save).
- Trim / cut / edit the audio during recording or on save.

---

## Screen 2 — HomeNotesScreen: FAB Mini Sheet (Text Note / Record Note)

### Purpose

Existing Home screen FAB previously started a text note instantly. The updated FAB opens a compact M3 ModalBottomSheet with two side-by-side tiles so users can explicitly choose between a typed text note (existing behavior) or a new voice recording (new feature).

### UX Principles

- **Fast path preserved with one more tap**: Sheet is a 2-tile grid; no scroll, no extra sections. Total incremental cost for existing text-note users = 1 extra tap.
- **Icon-first recognition**: Each tile has a large 32dp Material icon matching the action + short label. "Text Note" uses `Icons.Outlined.Edit`; "Record Note" uses `Icons.Outlined.Mic` with primary-brand mic tint for differentiation.

### Entry And Exit

- **Entry points**: `HomeNotesScreen` → existing `HomeAddButton` tap (`testTag = home_add_fab`, added as the stable feature contract) opens the mini-sheet.
- **Primary success exit**: Selecting "Text Note" navigates to new empty NoteEditorScreen (unchanged behavior). Selecting "Record Note" navigates to VoiceRecorderScreen with noteId = null.
- **Cancel/back behavior**: Tap outside sheet or system Back → sheet dismissed. No other side effects.
- **Failure exit**: Sheet dismisses; no failure states (pre-flights happen on VoiceRecorderScreen entry instead).

### Information Architecture

ModalBottomSheet, top corners 20dp (matches EditorNoteActionsSheet's shape pattern), `containerColor = surface = #FFFFFF`:

1. **Sheet drag handle (4dp height x 36dp width)**: Centered at top, `divider` color, 2dp rounded, 12dp top margin.
2. **Sheet title row**: `testTag = home_fab_sheet_title` — Text "Create" / `MaterialTheme.typography.titleMedium` semibold (textPrimary), 20dp top, 16dp start padding.
3. **2-tile row** (horizontal equal-width, each 1:1 width with 12dp gap between):
   - Tile 1: `testTag = home_fab_text_note` — Card shape 12dp radius, background = `surface`, border = `border` color 1dp, 16dp padding, column vertical centered: `Icons.Outlined.Edit` 32dp `textSecondary`, below Text "Text Note" label `bodyMedium` 14sp semibold `textPrimary`.
   - Tile 2: `testTag = home_fab_record_note` — Card shape 12dp radius, background = `highlight = #F0F4FF` with `primary = #7C6CF2` border 1dp to emphasize the new action, 16dp padding, column vertical centered: `Icons.Outlined.Mic` 32dp `primary` tint, below Text "Record Note" label `bodyMedium` 14sp semibold `primary`.
4. **Bottom padding**: 24dp below tiles plus `navigationBarsPadding()`.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|-----------|---------|-----------------|----------|
| HomeAddButton (existing) | Open mini-sheet | default / pressed | `home_add_fab` |
| Sheet title | "Create" label | static | `home_fab_sheet_title` |
| Text Note tile | Start new text note | default / pressed ripple / disabled (if note creation locked) | `home_fab_text_note` |
| Record Note tile | Start new voice recording | default / pressed ripple / disabled if no mic hardware | `home_fab_record_note` |

### Visual States

| State | User Sees | User Can Do |
|-------|-----------|-------------|
| Default (Sheet Open) | Both tiles visible; Record Note has primary border+highlight to stand out. | Tap either tile; tap outside to dismiss. |
| No microphone hardware | Record Note tile: 38% alpha (disabled); icon tint = `textTertiary`; contentDescription includes "microphone unavailable". | Record Note tile disabled; Text Note still works; tap outside dismiss. |

### Interaction Rules

- **Primary actions**: Tap a tile → immediate navigation, sheet auto-dismissed via `onDismissRequest` before navigation fires (standard M3 sheet choreography).
- **Secondary actions**: Tap outside scrim area → sheet dismissed silently.
- **No validation on sheet**: Permission and storage pre-flights are deferred to the Recorder screen. This keeps the sheet ultra-fast and simple.
- **No destructive actions**: No confirmations needed; these are create-forward actions, not deletes.
- **Gestures**: Sheet swipe down dismisses; no horizontal gestures.

### Copy Requirements

| Element | Copy |
|---------|------|
| Sheet title | `Create` |
| Text Note tile label | `Text Note` |
| Record Note tile label | `Record Note` |
| Tile contentDescriptions | `New text note`, `New voice recording` |

### Accessibility

- Tile cards: `Modifier.semantics(mergeDescendants = true)` so TalkBack reads "New text note button", "New voice recording button" in one pass.
- Minimum touch target: each tile is ≥ 112dp tall × ≥ 160dp wide — way above 48×48dp minimum.
- Disabled Record Note tile: `enabled = false`, plus `Modifier.semantics { stateDescription = "Unavailable: no microphone" }`.

### Responsive And Configuration Behavior

- Portrait: 2 tiles side-by-side as described.
- Landscape / Tablet: Max sheet content width 560dp centered for ergonomics (no 100% wide tiles on 10"+ tablets). 2 tiles still side-by-side with wider cards.

### Design Assets

- **Mockup image**: `design/mockup_home_fab_sheet_v3.png` — shows the Light Theme Home surface dimmed behind the standard modal bottom sheet with the "Create" title and two side-by-side tiles: Text Note and highlighted Record Note.

### Out Of Scope For This Design

- FAB long-press shortcuts (custom actions are not added; only tap sheet).
- 3+ options in the sheet; only Text vs Record in v1.

---

## Screen 3 — NoteEditorScreen: Mic Toolbar Icon + Inline VoiceNote Document Block

### Purpose

Integrates voice capture entry point and saved voice-note playback directly into the existing rich text editor flow without disrupting the current text editing behavior. The Mic icon wires the current placeholder Mic button already present in the editor's bottom tool rail. Saved VoiceNotes render as a structured inline block pair: audio player card + editable transcript text.

### UX Principles

- **Minimal intrusion to existing editor UX**: Mic icon is a toolbar button (no layout shift for the rest); the inline VoiceNote block is a card visually distinct from text paragraphs so the user always knows where playback controls live vs where editable text lives.
- **Clear audio-only deletion**: User can remove just the audio file (keeping transcript text) via an explicit trash icon on the player card with confirmation — the transcript remains as editable text, accessible via keyboard and normal editing.
- **Aligns with existing block structure**: VoiceNote player = new `EditorBlock.Voice` metadata card; transcript = existing `EditorBlock.TextBlock` immediately following. Reuses the full existing rendering, caret, and editing pipeline for the transcript — zero new editing logic required.

### Entry And Exit

- **Entry points for Mic**: `NoteEditorScreen` → existing toolbar Mic icon (now wired, was unused placeholder) with `testTag = editor_mic_btn` → navigates forward to `VoiceRecorderScreen` (Screen 1) with the current noteId as argument. After Stop (Screen 1 success exit), user is popped back to the editor and the new VoiceNote block appears at the last focused caret/block position.
- **Exit (remove block)**: Player trash icon → confirm "Delete audio file? The transcript text will remain as editable note content." → deletes audio on disk and removes the player card; transcript block stays.
- **Exit (full note delete)**: Deleting the note deletes all audio files via cascade cleanup (transcript blocks and all go).

### Information Architecture

Surface has two UI regions within the existing NoteEditorScreen (the top bar remains unchanged; the existing bottom formatting toolbar gains the recorder action):

**Region A — Editor Bottom Formatting Toolbar (existing, one new action wired)**:
- Existing `DefaultBottomBar` tool rail already contains the Mic icon after the image action. Wire that button to the recorder and assign `testTag = editor_mic_btn`; use `Icons.Outlined.Mic` (22dp inside the existing editor tool tile), tint = `primary = #7C6CF2` when enabled. The top action bar (back/share/more) remains unchanged.

**Region B — Inline VoiceNote Block (inserted between existing text blocks within the document scrollable column)**:
VoiceNote block = 2 stacked siblings with 16dp top/bottom margin around the pair and 8dp gap between them:

1. **Audio Player Card** (new `VoiceNotePlayer` composable):
   - `Modifier.fillMaxWidth()`, `surface = #FFFFFF` background, 12dp rounded corners, `border = #E7E3F6` 1dp stroke, 12dp inner padding.
   - Row layout, vertical alignment = Center.CenterVertically:
     - **Speaker icon** (leftmost, 28dp): `Icons.Outlined.VolumeUp` (material outlined), tint `secondary = #9B8CFF`. `contentDescription = "Voice recording"`.
     - Gap 12dp → **Play/Pause button** (36dp filled circular M3 button): `testTag = voice_play_pause_btn`. Idle/Not-playing = filled primary background `#7C6CF2`, `onPrimary = #FFFFFF` triangle Play icon; Playing = filled primary background, white Pause icon. Pressing toggles state between Play ↔ Pause via ExoPlayer.
     - Gap 12dp → **Seek/progress column** (flexible width, `weight = 1f`):
       - Top row = `Slider` (Compose M3), `testTag = voice_seek_slider`, thumb color = `primary = #7C6CF2`, track color = primary 24% active / `divider = #E7EBF0` inactive; live current position (ms) as user drags.
       - Below slider, row with two `bodySmall` 12sp labels: left and right `textSecondary = #7B7694` for elapsed and total duration, separated by `Spacer` with weight.
     - Gap 16dp → **Right-side info+action column**:
       - Top: file-size label, `bodySmall` 12sp, `textTertiary`, e.g. "2.4 MB", `testTag = voice_file_size_label`.
       - Bottom (48×48dp touch, 24dp visible icon): `testTag = voice_delete_audio_btn`, trash-can `Icons.Outlined.Delete`, tint = `error = #C44A4A`. `contentDescription = "Delete audio only, keep transcript text"`. On tap → confirmation dialog.

2. **Transcript TextBlock (standard EditorBlock.TextBlock)** (immediately follows the player card, 8dp top gap):
   - No new styling; looks identical to any other rich text paragraph block the user types. Editable caret can be placed inside it via normal tap. Bold/italic/link formatting toolbar is available when text inside is selected — same as any other block. When user first receives it after a recording, the text is a plain paragraph (no formatting) containing the concatenated STT transcript; user is free to reformat, delete, split, merge, or add content anywhere.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|-----------|---------|-----------------|----------|
| Editor Mic Icon (toolbar) | Open recorder screen | default / pressed / disabled (no mic hardware) | `editor_mic_btn` |
| VoiceNotePlayer Card | Container for player row | visible / hidden (when audio deleted-only) | `voice_player_card` |
| Speaker Icon | Static indicator | Static | — |
| Play/Pause Button | Toggle playback | Play (idle), Pause (playing), disabled during seek-drag | `voice_play_pause_btn` |
| Seek Slider | Scrub playback position | default / active dragging thumb / disabled while paused-at-start | `voice_seek_slider` |
| Elapsed Time Label | MM:SS elapsed | Updates during playback | `voice_elapsed_label` |
| Total Duration Label | MM:SS total | Static after load | `voice_duration_label` |
| File Size Label | "2.4 MB" etc. | Static after load | `voice_file_size_label` |
| Delete-Audio (Trash) Icon | Delete file, keep transcript | default / pressed; shows confirm dialog on click | `voice_delete_audio_btn` |
| Delete-Audio Confirm Dialog | Confirm destructive action | title + message + Cancel / Delete buttons (Delete is error-tinted, destructive) | `voice_delete_audio_dialog` |
| Transcript TextBlock (existing type reused) | Editable transcript | Focused / unfocused / selected text, same as existing text block | N/A (reuses existing block test tags) |

### Visual States

| State | User Sees | User Can Do |
|-------|-----------|-------------|
| Editor default (no voice blocks) | Mic icon visible and enabled in top-right action cluster | Tap Mic → opens recorder |
| Editor with 1+ VoiceNote blocks | Each block = Player Card + following editable transcript paragraph. Player shows Play triangle, elapsed `00:00`, total duration, file size. | Play, seek, delete-audio-only, edit transcript text freely. |
| Playing (inline) | Play button shows Pause icon. Slider thumb advances in real-time; elapsed ticks. | Pause, seek to position, trash. |
| Paused mid-playback | Play button shows Play triangle; slider frozen at last position; elapsed shows partial time. | Resume by tapping Play, seek to new position, trash. |
| Audio Deleted (transcript kept) | Player card is gone; only the text block remains. The text block is indistinguishable from any other paragraph block. | Normal text edit; no audio actions. |
| Error: Audio file missing on load (orphan) | Player card shows: "Audio file missing" in error text color with error info icon inline; Play disabled; trash still works (no-op for file delete, removes card only; transcript remains). | Tap trash to dismiss orphan card; edit transcript. |

### Interaction Rules

- **Primary action (Mic icon)**: Navigate to Recorder (Screen 1) — no permission rationale on this click (rationale is shown on Recorder pre-flight).
- **Play/Pause toggle**: Uses Media3 ExoPlayer `Player.play() / pause()`. When playing reaches end of audio, player auto-resets to start state (elapsed = 00:00, Play icon visible, ready for replay).
- **Seek**: Slider value change immediate seek during drag via `Player.seekTo()`. Elapsed label updates live during drag; no separate confirm step.
- **Trash / Delete Audio Only**: Confirm dialog title "Delete audio file?", message "The transcript text will remain as editable note content. Audio cannot be recovered after deletion." Buttons: Cancel / Delete (destructive, error color). On Delete → delete file, set `audioFilePath = null`, remove Player card but keep the following `EditorBlock.TextBlock` in the document.
- **Delete block entirely via editor (e.g. backspace at start of transcript)**: If user deletes the transcript TextBlock, also delete the preceding Voice player card and audio file. This is handled in editor's block-delete logic (block-pair awareness).
- **Validation**: No in-editor validation for transcript content; it's free-form text, same as existing paragraphs.
- **Gestures**: Vertical scroll for document; slider drag for seek; no custom horizontal swipe gestures.

### Copy Requirements

| Element | Copy |
|---------|------|
| Mic button contentDescription | `Record voice note` |
| Play button contentDescription | `Play voice recording` |
| Pause button contentDescription | `Pause voice recording` |
| Seek slider contentDescription | `Playback position, currently %s of %s` |
| File size label format | `%.1f MB` (e.g. 2.4 MB) — if < 1 MB, show `%d KB` (e.g. 840 KB) |
| Delete audio dialog title | `Delete audio file?` |
| Delete audio dialog message | `The transcript text will remain as editable note content. Audio cannot be recovered after deletion.` |
| Delete audio dialog Cancel button | `Cancel` |
| Delete audio dialog Delete button (destructive) | `Delete` |
| Orphan/missing-audio message | `Audio file missing` |

### Accessibility

- Mic icon: 48×48dp, contentDescription "Record voice note", proper semantics role = Button.
- Player card: `semantics(mergeDescendants = true)` groups speaker icon, play/pause, slider, elapsed, duration, file size, and delete action into one logical "Voice recording: duration 4:32, 2.4 MB" announcement. Then inner controls retain individual focusability.
- Play/Pause toggles `stateDescription` ("Playing" / "Paused").
- Slider uses Progress semantics with value (0..100%) and a custom `stateDescription` for "Current position MM minutes SS seconds of total MM minutes SS seconds".
- Delete action uses explicit error-tinted icon + destructive copy in dialog.
- Keyboard focus order within card: Play/Pause → Slider (focusable thumb) → Delete Audio → then transcript block below.
- All colors meet contrast ratios (design system enforced; no custom colors in this screen's new UI).

### Responsive And Configuration Behavior

- **Portrait / Landscape / Tablet**: Player card fills max-width (with editor-standard horizontal padding); no reflow. On tablets (≥ 600dp sw), player height stays compact; no 2-column split within card.
- **Configuration change (rotation, split-screen, keyboard open/close)**: ExoPlayer instance is retained via ViewModel (not in Composable local state) so playback position survives rotation. Slider and labels rebind immediately.

### Design Assets

- **Mockup image**: `design/mockup_editor_voice_block_v3.png` — shows the existing Note Editor screen in the updated Light Theme baseline with the unchanged top bar, the wired Mic action in the bottom tool rail, and an inline VoiceNote block with speaker icon, Play button, seek slider, elapsed/duration, file size, and destructive trash icon. The editable transcript follows as normal body text.

### Out Of Scope For This Design

- Playback waveform visualization (explicitly no waveform in saved player; only seek bar).
- Playback-speed toggle (1.0× only in v1; no 0.75× / 1.25× / 1.5× / 2×).
- Skip forward/back 10s buttons (user can seek via slider).
- Volume controls (handled by system volume, not in-app).

---

## Screen 4 — SettingsScreen: Voice Notes Section

### Purpose

New section added to the bottom of the existing Settings screen. Lets users (1) view total storage consumed by all voice-note audio files, and (2) toggle the default capture format between AAC (M4A) — the default with wide compatibility and larger file size — and OPUS (OGG) — more efficient for speech with smaller files but newer codec support.

### UX Principles

- **Fits existing Settings style**: Section header pattern, left-label + right-control rows, exactly matching the existing Settings screen's row layout. No new component family.
- **Transparent about storage**: Total size is prominent with a human-readable value and a helper line explaining what counts. Format toggle has a descriptive subtitle per option so users know the tradeoff.

### Entry And Exit

- **Entry points**: Existing Settings Screen navigation (Bottom Nav / Profile → Settings); section appears at the end of the Settings scrollable list after all existing items.
- **Primary success exit**: No explicit "Save" — format toggle writes to DataStore instantly on selection change; user observes "Saved" briefly via a one-line confirmation snackbar (or no snack, since toggle state is visible directly).
- **Cancel/back behavior**: Standard system back closes Settings. No discard needed since writes are atomic and instant.

### Information Architecture

Appends a new group card to the existing Settings scrollable list. Uses Settings' existing `SettingsSectionHeader(title)` composable + `SettingsRow` pattern (whatever pattern the existing Settings screen uses for left/right rows):

1. **Section Header**: Title text "Voice Notes" with same style as other Settings section headers.
2. **Storage Info Row** (informational, non-interactive):
   - Left column: Label "Total storage used" `bodyMedium` semibold `textPrimary`. Below it 12sp `textSecondary` helper: "Audio files for all saved voice notes".
   - Right column: Bold size value `titleMedium` `primary` color, e.g. "184.6 MB", `testTag = settings_voice_storage_value`. Below it 12sp `textTertiary` count line: "12 recordings".
3. **Audio Format Toggle Row** (interactive, opens picker on tap OR inline segmented 2-option control; uses segmented toggle for faster access):
   - Left column: Label "Audio format" `bodyMedium` semibold `textPrimary`. Below helper line 12sp `textSecondary`: "AAC: wider device support. OPUS: smaller speech files."
   - Right column: Two-option segmented toggle / M3 `TabRow` with 2 Tabs, `testTag = settings_voice_format_toggle`:
     - Tab 1: `testTag = settings_voice_format_aac`, label "AAC" — selected by default (app install default). Selected = primary pill background, white text. Unselected = surface, textSecondary.
     - Tab 2: `testTag = settings_voice_format_opus`, label "OPUS". Same state rules.
   - On switch from AAC → OPUS or OPUS → AAC: writes preference atomically; only affects *new* recordings; existing recordings keep their original format.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|-----------|---------|-----------------|----------|
| Voice Notes Section Header | Group title | static | `settings_voice_section_header` |
| Storage Used Label + Size | Display total size + count | static (recomputes on Settings screen open via ViewModel) | `settings_voice_storage_value` |
| Format Section Label + Helper | Describe tradeoff | static | — |
| Format Toggle AAC Tab | Default | selected / unselected | `settings_voice_format_aac` |
| Format Toggle OPUS Tab | Option 2 | selected / unselected | `settings_voice_format_opus` |
| Format Tab Row (whole) | Segmented toggle container | — | `settings_voice_format_toggle` |

### Visual States

| State | User Sees | User Can Do |
|-------|-----------|-------------|
| Default Content | All three rows visible with AAC selected (if never changed by user); storage shows current real size/count. | Tap OPUS to switch codec for future recordings. |
| Empty / No recordings yet | Section still renders; storage = "0 B · 0 recordings" (no size label hidden). | Toggle format works; nothing else special. |
| Codec pre-flight failure (OEM doesn't support OPUS) | OPUS tab auto-deselects, AAC becomes selected again; 3-line snackbar at bottom: "Your device doesn't support OPUS encoding. Switched back to AAC." | N/A — handled auto. User can read message. |

### Interaction Rules

- **Primary action**: Format toggle click → immediately writes preference → applies to future recordings. Existing files unchanged.
- **No destructive actions**: No delete-all in Settings for v1; deletions happen in-editor per-clip (Screen 3 trash icon) or via note delete.
- **No validation for storage row**: Pure read-only display.
- **Accessibility-friendly format toggle**: When TalkBack highlights the toggle, it reads "Audio format. Currently AAC. Double-tap to switch to OPUS" or vice versa.

### Copy Requirements

| Element | Copy |
|---------|------|
| Section header | `Voice Notes` |
| Storage row label | `Total storage used` |
| Storage helper line | `Audio files for all saved voice notes.` |
| Storage count format (plural) | `%d recordings` |
| Storage count format (singular) | `%d recording` |
| Format row label | `Audio format` |
| Format helper line | `AAC: wider device support · OPUS: smaller speech files.` |
| Tab label AAC | `AAC` |
| Tab label OPUS | `OPUS` |
| OPUS unsupported snackbar | `Your device doesn't support OPUS encoding. Switched back to AAC.` |

### Accessibility

- Section header: `Modifier.semantics { heading() }` to treat it as TalkBack navigation heading.
- Storage row: `mergeDescendants = true` → "Total storage used 184.6 megabytes, 12 recordings".
- Format segmented 2-tab toggle: Tab semantics with `selected = true/false` and `stateDescription` = current value; action = switch.

### Responsive And Configuration Behavior

- Standard Settings list: scrolls vertically; full-width rows. No landscape/tablet special layout.
- Configuration change: Value reads from DataStore on every recomposition; state survives rotation and split-screen.

### Design Assets

- **Mockup image**: `design/mockup_settings_voice_v3.png` — shows the existing Light Theme Settings shell scrolled to the appended "Voice Notes" section: standard section header, white bordered card, storage stats, and AAC / OPUS segmented toggle with AAC selected.

### Out Of Scope For This Design

- Per-clip listing / deletion in Settings (only total stats are shown here; per-clip actions live in the editor).
- Auto-purge / auto-delete older-than-N configuration (not in v1 scope).
- Bitrate / sample-rate sub-options (fixed sensible defaults for v1: AAC @ 128 kbps / 44.1 kHz stereo actually mono speech, OPUS @ 32 kbps / 16 kHz mono — no user tuning needed).

---
