---
name: android-architecture
description: Guidelines and instructions for the Android project architecture, based on the official android/architecture-samples.
---
# Android Architecture Guidelines

Use this skill when designing or implementing new features, components, or layers in the Android app to ensure architectural consistency.

## Core Architecture Principles
This project follows the official Modern Android Development (MAD) architecture guidelines showcased in [android/architecture-samples](https://github.com/android/architecture-samples). 

- **Single-Activity Architecture**: The app uses a single Activity with Jetpack Compose Navigation for moving between screens.
- **Jetpack Compose**: The entire User Interface is built natively using Jetpack Compose.
- **Unidirectional Data Flow (UDF)**: State flows down from the ViewModel to the Compose UI, and events flow up from the UI to the ViewModel.
- **Reactive Programming**: Utilize Kotlin Coroutines and Flow for all asynchronous operations and data streams.

## Layered Architecture
The app is separated into distinct layers to separate concerns:

### 1. Presentation Layer (UI Layer)
- **Compose Screens**: Each feature or screen is represented by a composable function.
- **ViewModels**: One ViewModel per screen or feature. ViewModels hold and expose the UI state and handle UI events.
- **State Management**: Expose UI state via `StateFlow` or `Flow`.

### 2. Domain Layer (Optional)
- Use UseCases to encapsulate complex business logic that is shared across multiple ViewModels.

### 3. Data Layer
- **Repositories**: The single source of truth for data. Repositories coordinate between different data sources.
- **Data Sources**: e.g., Local (Room Database) and Remote (Network API / Fake remote).
- Never expose data source details directly to the Presentation layer.

## Dependency Injection
- Use **Hilt** for dependency injection across all layers. Ensure proper scoping (e.g., `@Singleton`, `@ViewModelScoped`).

## Testing Strategy
- **Unit Tests**: Test ViewModels, Repositories, and business logic.
- **Integration Tests**: Verify the interactions between different layers (e.g., Repository with Room Database).
- **Instrumented UI Tests**: Verify Compose screens and user flows using Compose Test Rules.

## Required Process
1. When adding a new feature, start by defining the Data Layer (Repositories) if new data is needed.
2. Build the ViewModel to expose the necessary state and define events.
3. Build the Jetpack Compose UI to consume the state and send events.
4. Inject dependencies using Hilt.
5. Add appropriate tests at each layer.
