# NotesTakingApp

Android notes app built with Kotlin, Jetpack Compose, Room, Hilt, Auth0, and a backend API integration driven by the OpenAPI contract in `../NotesAppBackend/openapi.yaml`.

## Backend Integration

The app now uses:

- Auth0 access tokens with the backend API audience
- Retrofit + Moshi + OkHttp for API calls
- Room as a local cache for folders and notes
- Backend item IDs and tree structure based on `parentId`

Set these values in `local.properties` before running the app:

```properties
AUTH0_CLIENT_ID=your_auth0_native_client_id
AUTH0_AUDIENCE=https://notes-app.api
API_BASE_URL=http://10.0.2.2:8080/
```

`API_BASE_URL` defaults to the Android emulator loopback for a backend running on the same machine.

## Harness Engineering

This project utilizes a "Harness Engineering" approach for UI development. This involves iteratively refining the user interface to match the design specifications as closely as possible. 

The process involves:
1. Taking the original UI design mockups.
2. Implementing the UI using Jetpack Compose.
3. Running automated UI tests to capture screenshots of the rendered implementation.
4. Comparing the rendered screenshots against the original design.
5. Iterating on the Compose code to fix alignment, colors, typography, and spacing.
6. Repeating the process until the rendered UI closely matches the original design.

### Example: Settings Screen

Below is an example of the Harness Engineering process applied to the Settings screen.

**Original Design:**

![Original Settings Design](UX/settings.png)

**Rendered Implementation (After Iterations):**

![Rendered Settings Implementation](UX/settings_rendered.png)
