# Project Management Clean Architecture Refactoring

The project management logic has been extracted from `DataManager.kt` into a dedicated Clean Architecture structure.

## Components Created

### 1. Domain Layer
- **[ProjectRepository](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/repository/ProjectRepository.kt)**: Interface defining project operations and settings.
- **[ProjectSettings](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/repository/ProjectRepository.kt)**: Data class for all project-related preferences (auto-archive, templates, etc.).
- **UseCases**:
    - **[CreateProjectUseCase](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/usecase/project/CreateProjectUseCase.kt)**: Handles new project initialization.
    - **[UpdateProjectUseCase](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/usecase/project/UpdateProjectUseCase.kt)**: Persists project changes.
    - **[DeleteProjectUseCase](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/usecase/project/DeleteProjectUseCase.kt)**: Removes projects.
    - **[AddMilestoneUseCase](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/usecase/project/AddMilestoneUseCase.kt)**: Adds milestones (`subFeatures`) and records history.
    - **[GetProjectProgressUseCase](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/usecase/project/GetProjectProgressUseCase.kt)**: Calculates % progress based on milestone completion.

### 2. Data Layer
- **[ProjectLocalDataSource](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/datasource/ProjectLocalDataSource.kt)**: Manages `EncryptedSharedPreferences` for settings and filters `GlobalNoteEntity` for projects.
- **[ProjectRepositoryImpl](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/repository/ProjectRepositoryImpl.kt)**: Coordinates data flow using `NoteMapper`.

### 3. Dependency Injection
- **[ProjectModule](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/di/ProjectModule.kt)**: Hilt bindings for `ProjectRepository`.

## Integration Highlights
- **Reactive Sync**: `DataManager.kt` now observes `ProjectRepository.getAllProjects()` and `ProjectRepository.getProjectSettings()`.
- **Thread Safety**: Updates to `ProjectDataManager` (legacy) are now pushed from the repository Flow, ensuring consistency.
- **Encrypted Settings**: Project settings have been migrated from plain `SharedPreferences` to `EncryptedSharedPreferences` via the new repository.

## Usage Example (ViewModel)
```kotlin
@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val createProjectUseCase: CreateProjectUseCase,
    private val projectRepository: ProjectRepository
) : ViewModel() {
    val projects = projectRepository.getAllProjects()

    fun create(title: String) {
        viewModelScope.launch { createProjectUseCase(title, "") }
    }
}
```
