# ADR-001: Scope ViewModels to Navigation Destinations

## Status
Accepted

## Date
2026-05-03

## Context
The Notes application was previously using Activity-scoped ViewModels for all main screens (`HomeViewModel`, `FoldersViewModel`, `SettingsViewModel`, `NoteEditorViewModel`). These ViewModels were instantiated at the root of the `AppNavGraph` and passed down as parameters.

This approach led to several critical issues:
- **Stale Data after Login**: ViewModels were created when the app launched. Their `init` blocks triggered data synchronization before the user was authenticated. Since the instances were reused after login, the data never refreshed from the server.
- **State Persistence Bugs**: The `NoteEditorViewModel` would retain content from previously edited notes because it was never cleared between editing sessions.
- **Resource Management**: ViewModels remained in memory even when the user was not on the corresponding screens.

## Decision
Refactor `AppNavGraph` to scope ViewModels to their respective navigation destinations (`NavBackStackEntry`) using the `hiltViewModel()` function inside each `composable` block.

## Alternatives Considered

### Manual Refresh Trigger
- **Pros**: Minimal change to existing architecture; preserves Activity-scoping for tests.
- **Cons**: Requires manual boilerplate in ViewModels and navigation callbacks; doesn't fix the "stale editor state" issue; more error-prone.
- **Rejected**: Does not address the root lifecycle issue and leads to a fragmented state management strategy.

### Reactive Login Observation in ViewModels
- **Pros**: Robustly handles authentication state changes.
- **Cons**: Adds `AuthManager` dependency to all ViewModels; doesn't fix the "stale editor state" issue.
- **Rejected**: Sub-optimal compared to standard Jetpack Compose lifecycle management.

## Consequences
- **Automatic Refresh**: `HomeViewModel` and others are recreated after login (when navigating from Onboarding to Home), triggering fresh data sync.
- **Isolated State**: `NoteEditorViewModel` state is fresh for each editing session.
- **Clean Logout**: All ViewModels are cleared when the navigation stack is popped to the root upon logout.
- **Test Impact**: Some instrumented tests that relied on Activity-scoping or manual ViewModel injection into `AppNavGraph` may require updates.
