# Compose Rules

## Purpose
Rules for writing Jetpack Compose UI in this project.

---

## Composable Responsibilities

A Composable should:
- Receive `UiState` and event callbacks as parameters
- Render the current state
- Call callbacks on user interactions — it does not call the ViewModel directly

A Composable must NOT:
- Call use cases or repositories
- Contain business logic or data transformation
- Import ViewModel directly inside the composable body (use DI patterns at the screen level)
- Use hardcoded strings — always use `stringResource()`
- Use hardcoded colors — always use `LocalAppColors.current.<token>` (see Colors section)

---

## Stateless / Stateful Pattern

Split screens into:

```kotlin
// Stateful wrapper — wires ViewModel (tested via integration tests)
@Composable
fun NoteDetailScreen(viewModel: NoteDetailViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NoteDetailContent(
        uiState = uiState,
        onSaveClick = viewModel::onSaveClick,
    )
}

// Stateless content — tested in isolation via createComposeRule()
@Composable
fun NoteDetailContent(
    uiState: NoteDetailUiState,
    onSaveClick: () -> Unit,
) { ... }
```

Always test `NoteDetailContent` (stateless) — not `NoteDetailScreen` (stateful) — in UI tests.

---

## Test Tags

Add `Modifier.testTag(...)` to all:
- Primary CTAs and action buttons
- Key content containers (note card, list items)
- Empty state and error state views
- Loading indicators
- Navigation elements (tabs, back buttons)

Use descriptive, stable names:
```kotlin
Modifier.testTag("note_detail_save_button")      // ✅
Modifier.testTag("button_${note.id}")            // ❌ — unstable, ID-dependent
Modifier.testTag("btn")                          // ❌ — not descriptive
```

---

## String Resources

All user-visible text must use `stringResource()`:
```kotlin
Text(stringResource(R.string.note_detail_title))   // ✅
Text("Note title")                                  // ❌
```

String resource key naming convention: `<screen>_<element>_<type>`
```
note_detail_title_label
home_empty_state_message
folders_create_button_label
```

---

## Colors

**Never hardcode colors inline in Composables.** Hardcoded colors prevent theming and make night mode impossible to implement.

```kotlin
Text(color = Color(0xFF7281A7))          // ❌ — hardcoded, cannot be themed
Text(color = Color.Red)                  // ❌ — hardcoded

Text(color = LocalAppColors.current.textSecondary)  // ✅
```

### Where to define colors

All shared colors must be defined in a dedicated theme file (e.g. `ui/theme/AppColors.kt`):

```kotlin
data class AppColors(
    val textPrimary: Color,
    val textSecondary: Color,
    val backgroundSurface: Color,
    val commentHighlight: Color,
    // add more as needed
)

val LightAppColors = AppColors(
    textPrimary = Color(0xFF1A1A2E),
    textSecondary = Color(0xFF7281A7),
    backgroundSurface = Color(0xFFF5F5F5),
    commentHighlight = Color(0xFFFFF9C4),
)

val DarkAppColors = AppColors(
    textPrimary = Color(0xFFE0E0E0),
    textSecondary = Color(0xFFAABBCC),
    backgroundSurface = Color(0xFF1E1E2E),
    commentHighlight = Color(0xFF3A3A20),
)

val LocalAppColors = compositionLocalOf { LightAppColors }
```

Provide at the app root and switch based on system dark mode:
```kotlin
val colors = if (isSystemInDarkTheme()) DarkAppColors else LightAppColors
CompositionLocalProvider(LocalAppColors provides colors) {
    AppContent()
}
```

### Rules
- **All color values go in `AppColors.kt`** — no `Color(0x...)` anywhere else in the codebase
- **Access via `LocalAppColors.current.<token>`** — never via a global singleton or hardcoded value
- **Name by semantic purpose** (`textSecondary`, `commentHighlight`) — not by value (`gray`, `yellow`)
- Adding a new color requires adding it to **both** `LightAppColors` and `DarkAppColors`

---

## Component Extraction

Extract reusable Composables to `components/` when:
- The same UI structure appears in more than one screen
- A component has its own internal state or complexity
- A component is independently testable

Keep components focused — one visual responsibility per component.

---

## State Hoisting

Always hoist state to the lowest common ancestor that needs it.
Do not hoist state higher than necessary.
Avoid `remember` in tests by keeping stateless content Composables as the primary test surface.

---

## Performance Rules

- Prefer `LazyColumn` over `Column` with `forEach` for lists
- Avoid unnecessary recompositions: pass stable types as parameters
- Use `key()` in lazy lists when items have stable IDs
- Avoid creating lambdas inside the composable body — pass them as parameters
