# Bug Reference: Double Padding on FolderDescriptionScreen Top Bar

**Title**: Double Padding on FolderDescriptionScreen Top Bar  
**Date fixed**: 2026-07-11  
**Severity**: low  
**Affected version**: 1.0  

---

## Symptom

A large white space/gap is observed at the top of the `FolderDescriptionScreen` above the top app bar, and the status bar has a dark grey/brown background instead of matching the screen color.

---

## Root Cause

```
Root cause:
The bug happens because the inner Scaffold applied `Modifier.padding(parentPadding)` where `parentPadding` contains the top status bar height from the outer Scaffold, shifting the entire screen down. The `TopAppBar` inside the inner Scaffold also applied default top window insets via `TopAppBarDefaults.windowInsets`, shifting its content down again (double padding).
```

---

## Regression Test

| Test Class | Type | Scenario | Fails Before Fix | Passes After Fix |
|------------|------|----------|-----------------|-----------------|
| `FolderDescriptionScreenTest.kt` | Instrumented UI | `givenTopParentPadding_whenRendering_thenTopAppBarIsAtTopAndNotShifted` | ✅ | ✅ |

### Test description

```kotlin
    @Test
    fun givenTopParentPadding_whenRendering_thenTopAppBarIsAtTopAndNotShifted() {
        composeRule.setContent {
            NotesTakingAppTheme {
                FolderDescriptionContent(
                    parentPadding = PaddingValues(top = 100.dp),
                    state = FolderDescriptionUiState(
                        isLoading = false,
                        folderName = "Receipts",
                        description = "Client receipts"
                    ),
                    onDescriptionChanged = {},
                    onSave = {},
                    onBack = {}
                )
            }
        }

        val topPosition = composeRule.onNodeWithText("Folder description").getUnclippedBoundsInRoot().top
        val message = "TopAppBar should not be shifted down by parentPadding top: topPosition = $topPosition"
        assertTrue(
            message,
            topPosition < 50.dp
        )
    }
```

---

## Fix Summary

**Files changed**:
- `FolderDescriptionScreen.kt` — Changed inner Scaffold modifier to `fillMaxSize()` so it draws behind system status bar, and handled bottom/navigation/IME padding within the body content Box using `.padding(bottom = parentPadding.calculateBottomPadding()).navigationBarsPadding().imePadding()`.
- `Theme.kt` — Added SideEffect in `NotesTakingAppTheme` to configure `window.statusBarColor` and `window.navigationBarColor` to `Color.Transparent`, and set status/navigation bar light/dark appearance based on `darkTheme`.

**Change type**: layout adjustment / visual fix / system bar configuration

---

## Prevention

A new instrumented UI test `givenTopParentPadding_whenRendering_thenTopAppBarIsAtTopAndNotShifted` was added to `FolderDescriptionScreenTest.kt` to verify that passing `parentPadding` with a top offset doesn't shift the app bar.
