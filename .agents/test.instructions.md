---
applyTo: "app/src/test/**"
---

# JVM unit and integration test instructions

> [!IMPORTANT]
> These instructions have been merged into the [android-unit-test](file:///mnt/data/Projects/NotesApp/NotesTakingApp/.agents/skills/android-unit-test/SKILL.md) skill.
> Please refer to the skill file for the full documentation on coverage requirements, naming conventions, and testing standards.

## Summary of Key Rules
- **Coverage**: Overall 80%+, New ViewModels/Domain 90%+.
- **Naming**: 
    - Unit Tests: `<Class>Test.kt`
    - Integration Tests: `<Class>IntegrationTest.kt` (only for multi-layer tests).
- **Base Classes**: 
    - Unit: `BaseViewModelTest`
    - Integration: `BaseViewModelIntegrationTest`
- **Assertions**: Always assert `UiState` rather than UI components.
