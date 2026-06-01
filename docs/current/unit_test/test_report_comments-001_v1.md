## Test Report — v1

### Test Layers Used
- Unit: [NoteCommentMapperTest.kt](file:///mnt/data/Projects/NotesApp/NotesTakingApp/app/src/test/java/com/example/notesapp/data/repository/NoteCommentMapperTest.kt)
- Integration: [NoteCommentRepositoryTest.kt](file:///mnt/data/Projects/NotesApp/NotesTakingApp/app/src/test/java/com/example/notesapp/data/repository/NoteCommentRepositoryTest.kt)
- Instrumented UI: SKIPPED

### Shared JSON Scenarios
- None (Retrofit `api` calls are directly mocked inside `NoteCommentRepositoryTest` to test success, failure offline fallback, and mapping, ensuring complete logic coverage).

### Coverage (verbatim from koverLog output)
- Overall: 81.7456%
- New classes: 100%

### Test Results (verbatim from Gradle output)
- Unit + Integration: 146 passed / 146 total
- Instrumented: SKIPPED
