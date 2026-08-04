# Implementation Plan - Extract Task Functionality from DataManager

Extract task-related logic from the monolithic `DataManager.kt` into a clean architecture structure.

## User Review Required

> [!IMPORTANT]
> This refactoring will move all Task-related logic from `DataManager` and `TaskDataManager` into a new `TaskRepository`.
> ViewModels will need to be updated to inject the new `TaskRepository` or Use Cases instead of using `DataManager.tasks` directly.

## Proposed Changes

### [Component] Domain Layer
Create interfaces and use cases for Task operations.

#### [MODIFY] [TaskRepository](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/repository/TaskRepository.kt)
Update the interface to include all necessary methods for task management and settings.

#### [NEW] [AddTaskUseCase](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/app/src/main/java/com/example/allinone/domain/usecase/task/AddTaskUseCase.kt)
#### [NEW] [UpdateTaskUseCase](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/app/src/main/java/com/example/allinone/domain/usecase/task/UpdateTaskUseCase.kt)
#### [NEW] [DeleteTaskUseCase](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/app/src/main/java/com/example/allinone/domain/usecase/task/DeleteTaskUseCase.kt)
#### [NEW] [GetTasksUseCase](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/app/src/main/java/com/example/allinone/domain/usecase/task/GetTasksUseCase.kt)

### [Component] Data Layer
Implement the repository and data source.

#### [NEW] [TaskLocalDataSource](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/app/src/main/java/com/example/allinone/data/datasource/TaskLocalDataSource.kt)
Handles Room database operations and SharedPreferences for task settings.

#### [NEW] [TaskRepositoryImpl](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/app/src/main/java/com/example/allinone/data/repository/TaskRepositoryImpl.kt)
Implementation of `TaskRepository` using `TaskLocalDataSource`.

### [Component] Infrastructure (DI)
Prepare for Hilt by adding necessary annotations and a DI module.

#### [NEW] [TaskDataModule](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/app/src/main/java/com/example/allinone/di/TaskDataModule.kt)
Hilt module for binding the `TaskRepository`.

### [Component] DataManager Cleanup
Remove task-related logic from `DataManager.kt` and `TaskDataManager.kt`.

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/app/src/main/java/com/example/allinone/DataManager.kt)
Remove task-related properties, synchronization logic, and database observation.

#### [MODIFY] [TaskDataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/app/src/main/java/com/example/allinone/data/TaskDataManager.kt)
Deprecate or remove properties as they move to the repository.

## Verification Plan

### Automated Tests
- Create unit tests for `TaskRepositoryImpl` and `UseCases` using a mock data source.

### Manual Verification
- Verify that tasks are still correctly loaded, added, updated, and deleted in the UI.
- Verify that task settings (e.g., show completed) are persisted across app restarts.
