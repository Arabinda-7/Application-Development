# Habit Management Refactoring Plan

Refactor Habit management from the monolithic `DataManager.kt` into a Clean Architecture structure using Kotlin Flows, UseCases, and Hilt.

## Proposed Changes

### Domain Layer
#### [NEW] [HabitRepository](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/repository/HabitRepository.kt)
Interface defining habit data operations and settings management.
#### [NEW] [CreateHabitUseCase](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/usecase/habit/CreateHabitUseCase.kt)
#### [NEW] [UpdateHabitUseCase](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/usecase/habit/UpdateHabitUseCase.kt)
#### [NEW] [DeleteHabitUseCase](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/usecase/habit/DeleteHabitUseCase.kt)
#### [NEW] [TrackHabitCompletionUseCase](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/usecase/habit/TrackHabitCompletionUseCase.kt)
Handles progress updates and history logic.
#### [NEW] [GetHabitStatisticsUseCase](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/usecase/habit/GetHabitStatisticsUseCase.kt)
Calculates streaks, heatmaps, and stability index.
#### [NEW] [GetHabitProgressUseCase](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/usecase/habit/GetHabitProgressUseCase.kt)

### Data Layer
#### [NEW] [HabitLocalDataSource](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/datasource/HabitLocalDataSource.kt)
Combines Room (`AppHabitDao`) and Encrypted SharedPreferences for settings.
#### [NEW] [HabitRepositoryImpl](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/repository/HabitRepositoryImpl.kt)
Implements `HabitRepository` interface, using `HabitMapper`.
#### [NEW] [HabitMapper](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/mapper/HabitMapper.kt)
#### [DELETE] [HabitRepository (Old)](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/repository/HabitRepository.kt)

### DI Layer
#### [NEW] [HabitModule](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/di/HabitModule.kt)
Binds `HabitRepositoryImpl` to `HabitRepository`.

### Orchestration
#### [MODIFY] [DataManager](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Updated `DataManagerEntryPoint` to include `HabitRepository`.
- Replaced old repository initialization with EntryPoint injection.
- Updated `startDatabaseObservation` to sync both habits and settings from the new repository.

## Verification Plan
### Automated Tests
- Unit tests for `GetHabitStatisticsUseCase` (Streak logic).
- Unit tests for `TrackHabitCompletionUseCase`.
- Integration test for `HabitRepositoryImpl` with a mock `HabitLocalDataSource`.

### Manual Verification
- Deploy to device and verify habit completion updates correctly.
- Check if habit settings (Sort Order, Show Completed) persist after app restart.
- Verify that streaks and progress bars in the UI match the database state.
