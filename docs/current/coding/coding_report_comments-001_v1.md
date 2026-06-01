## Coding Report — v1

### Files Changed
| File | Layer | Action | Notes |
|------|-------|--------|-------|
| [NoteBlockCommentEntity.kt](file:///mnt/data/Projects/NotesApp/NotesTakingApp/app/src/main/java/com/example/notesapp/data/local/NoteBlockCommentEntity.kt) | Data | NEW | Room Entity class for local SQLite caching |
| [NoteBlockCommentDao.kt](file:///mnt/data/Projects/NotesApp/NotesTakingApp/app/src/main/java/com/example/notesapp/data/local/NoteBlockCommentDao.kt) | Data | NEW | Dao queries and insertions for comments |
| [AppDatabase.kt](file:///mnt/data/Projects/NotesApp/NotesTakingApp/app/src/main/java/com/example/notesapp/data/local/AppDatabase.kt) | Data | MODIFY | Added Comment Entity and DAO, incremented version to 8 |
| [ApiModels.kt](file:///mnt/data/Projects/NotesApp/NotesTakingApp/app/src/main/java/com/example/notesapp/data/remote/ApiModels.kt) | Data | MODIFY | Added ApiNoteBlockComment and CreateNoteBlockCommentRequest DTOs |
| [NotesApiService.kt](file:///mnt/data/Projects/NotesApp/NotesTakingApp/app/src/main/java/com/example/notesapp/data/remote/NotesApiService.kt) | Data | MODIFY | Added listNoteBlockComments and createNoteBlockComment endpoints |
| [AppModule.kt](file:///mnt/data/Projects/NotesApp/NotesTakingApp/app/src/main/java/com/example/notesapp/di/AppModule.kt) | Data | MODIFY | Provided NoteBlockCommentDao and bound NoteCommentRepository |
| [NoteBlockComment.kt](file:///mnt/data/Projects/NotesApp/NotesTakingApp/app/src/main/java/com/example/notesapp/domain/comment/model/NoteBlockComment.kt) | Domain | NEW | Domain model representation |
| [NoteCommentRepository.kt](file:///mnt/data/Projects/NotesApp/NotesTakingApp/app/src/main/java/com/example/notesapp/domain/comment/repository/NoteCommentRepository.kt) | Domain | NEW | Repository interface |
| [NoteCommentMapper.kt](file:///mnt/data/Projects/NotesApp/NotesTakingApp/app/src/main/java/com/example/notesapp/data/repository/NoteCommentMapper.kt) | Data | NEW | Mapper to convert DTO, Entity, and Domain representations |
| [NoteCommentRepositoryImpl.kt](file:///mnt/data/Projects/NotesApp/NotesTakingApp/app/src/main/java/com/example/notesapp/data/repository/NoteCommentRepositoryImpl.kt) | Data | NEW | Repository implementation handling caching and network offline fallbacks |

### Key Decisions
- Standardized offline-first caching structure: `observeComments` retrieves a live Room database Flow while `refreshComments` syncs from the remote API and overrides cache.
- Implemented robust offline fallback: `addComment` attempts remote POST first; if network throws an exception, it generates a local comment with prefix `comment_` and caches it offline immediately.

### UiState Implemented
- N/A (Data/Domain Layer task only)

### testTags Added
- N/A (Data/Domain Layer task only)

### Known Gaps
- None.
