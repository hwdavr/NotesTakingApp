# Implementation Plan - Profile Email in Settings

Personalize the settings screen by showing the logged-in user's email or "Guest" as the default title.

## Proposed Changes

### 1. Resources
- **File**: `app/src/main/res/values/strings.xml`
- **Change**: Add `<string name="settings_guest">Guest</string>`.

### 2. Data Layer
- **File**: `app/src/main/java/com/example/notesapp/auth/TokenStorage.kt`
- **Change**: 
    - Add `saveUserEmail(email: String)` method.
    - Add `getUserEmail(): String?` method.
    - Update `clearTokens()` to remove the stored email.

### 3. Authentication Logic
- **File**: `app/src/main/java/com/example/notesapp/auth/AuthManager.kt`
- **Change**:
    - Add a `_userEmail = MutableStateFlow<String?>(null)` and public `userEmail: StateFlow<String?>`.
    - In `login()`'s `onSuccess`, decode the `idToken` to get the email and save it via `TokenStorage`.
    - In `checkSession()`, load the stored email from `TokenStorage` into `_userEmail`.
    - In `logout()`'s `onSuccess`, clear the email state.
    - **Note**: I will add `com.auth0.android:jwtdecode:2.0.2` to `app/build.gradle.kts` to safely parse the ID token.

### 4. ViewModel Layer
- **File**: `app/src/main/java/com/example/notesapp/ui/settings/SettingsViewModel.kt`
- **Change**:
    - Define `SettingsUiState` data class.
    - Expose `uiState: StateFlow<SettingsUiState>` by combining `authManager.isLoggedIn` and `authManager.userEmail`.

### 5. UI Layer
- **File**: `app/src/main/java/com/example/notesapp/ui/settings/SettingsScreen.kt`
- **Change**:
    - Collect `uiState` from the ViewModel.
    - Update `SettingsScreenContent` and `HeroBanner` to accept the email.
    - In `HeroBanner`, replace the hardcoded "AI Notes" description with the email or "Guest".

## Testing Plan
- **Unit Tests**:
    - Update `AuthManagerTest` (if exists) or create a new one to verify email extraction and storage.
    - Update/Create `SettingsViewModelTest` to verify `uiState` mapping.
- **UI Verification**:
    - Use `android-ui-verification` skill to check the rendering of the email vs "Guest".

## Risk Review
- **Privacy**: We are displaying the email on screen. This is standard for profile sections.
- **Security**: Email is stored in `EncryptedSharedPreferences` along with tokens, which is safe.
- **JWT Parsing**: Using a dedicated library (`jwtdecode`) is safer than manual string manipulation.
