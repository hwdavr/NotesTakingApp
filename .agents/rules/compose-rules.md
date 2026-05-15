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
