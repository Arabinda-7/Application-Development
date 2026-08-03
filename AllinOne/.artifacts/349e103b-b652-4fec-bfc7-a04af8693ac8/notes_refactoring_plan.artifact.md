# Notes & Projects Refactoring Implementation Plan

This plan outlines the extraction of Notes and Projects functionality from `DataManager.kt` into a Clean Architecture structure. Currently, these two features share the `Note` model and `NoteRepository`.

## Proposed Changes

### Domain Layer

#### [MODIFY] [NoteRepository.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/repository/NoteRepository.kt)
Define an interface for handling both personal Notes and Global Projects.

#### [NEW] [Note Use Cases](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/usecase/notes/)
- `GetNotesUseCase`: Observe personal notes.
- `GetProjectsUseCase`: Observe global projects.
- `CreateNoteUseCase`: logic for adding a new note or project.
- `UpdateNoteUseCase`: Updating content, pinning, or archiving.
- `DeleteNoteUseCase`: Permanent removal.
- `SearchNotesUseCase`: Logic for searching through titles and content.
- `AutoCleanupNotesUseCase`: Logic for deleting old notes based on settings.

---

### Data Layer

#### [NEW] [NoteLocalDataSource.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/datasource/NoteLocalDataSource.kt)
Manages Room (`AppNoteDao`) and `SharedPreferences` for note-specific settings (auto-cleanup, hidden visibility, templates).

#### [NEW] [NoteRepositoryImpl.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/repository/NoteRepositoryImpl.kt)
Implementation of the domain repository.

---

### Dependency Injection

#### [NEW] [NoteModule.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/di/NoteModule.kt)
Hilt module for Note-related dependencies.

---

## Function Migration Map

| DataManager Function | New Location |
| :--- | :--- |
| `notes` / `projects` (Lists) | `NoteRepository` (Flows) |
| `searchNotes()` | `SearchNotesUseCase` |
| `sanitizeNote()` | `NoteRepositoryImpl` (Mapping/Logic) |
| `noteAutoCleanupDays` | `NoteLocalDataSource` (SharedPreferences) |
| `noteTemplates` | `NoteLocalDataSource` (SharedPreferences) |
| `initializeNoteFields()` | `NoteRepositoryImpl` (Internal logic) |

---

## Verification Plan

### Automated Tests
- Unit tests for `SearchNotesUseCase` to ensure case-insensitive matching.
- Unit tests for `AutoCleanupNotesUseCase` to verify date-based filtering.
- Repository tests for Room entity mapping.

### Manual Verification
- Verify personal notes and global projects are correctly separated in their respective screens.
- Test "Auto-Cleanup" feature by setting a low day count and verifying old notes are removed.
- Verify templates can be created and applied to new notes.
