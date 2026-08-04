# Workout Refactoring Implementation Plan

This plan details the migration of Workout-related logic from `DataManager.kt` and `WorkoutDataManager.kt` to a Clean Architecture structure.

## Proposed Changes

### Domain Layer

#### [NEW] [WorkoutRepository.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/repository/WorkoutRepository.kt)
Interface for workout CRUD operations, history retrieval, and settings management.

#### [NEW] [WorkoutProgress.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/model/WorkoutProgress.kt)
Domain model for summarizing workout streaks, progress, and intensity.

#### [NEW] [Workout Use Cases](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/usecase/workout/)
- `GetWorkoutsUseCase`: Observe all workouts via Flow.
- `AddWorkoutUseCase`: Add a new workout session.
- `UpdateWorkoutUseCase`: Update an existing workout (e.g., mark as completed).
- `DeleteWorkoutUseCase`: Remove a workout.
- `GetWorkoutHistoryUseCase`: Retrieve completed workout dates and summaries.
- `CalculateWorkoutProgressUseCase`: Logic for daily/monthly progress and calories.
- `GetWorkoutAnalyticsUseCase`: Advanced logic for ACWR, muscle distribution, and volume.

---

### Data Layer

#### [NEW] [WorkoutLocalDataSource.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/datasource/WorkoutLocalDataSource.kt)
Encapsulates access to `AppWorkoutDao` (Room) and Workout-specific `SharedPreferences`.

#### [MODIFY] [WorkoutRepositoryImpl.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/repository/WorkoutRepositoryImpl.kt)
Implementation of the domain repository using the local data source.

---

### Dependency Injection

#### [NEW] [WorkoutModule.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/di/WorkoutModule.kt)
Hilt module for Workout dependencies.

---

## Function Migration Map

| DataManager / WorkoutDataManager Function | New Location |
| :--- | :--- |
| `getWorkoutProgress()` | `CalculateWorkoutProgressUseCase` |
| `getWorkoutStreaks()` | `CalculateWorkoutProgressUseCase` |
| `calculateWorkoutVolume()` | `GetWorkoutAnalyticsUseCase` |
| `getMuscleDistributionData()` | `GetWorkoutAnalyticsUseCase` |
| `getACWRData()` | `GetWorkoutAnalyticsUseCase` |
| `workoutMuscleGroups` (List) | `WorkoutLocalDataSource` (SharedPreferences) |
| `workoutWeightUnit` (Setting) | `WorkoutLocalDataSource` (SharedPreferences) |

---

## Verification Plan

### Automated Tests
- Unit tests for analytics logic in `GetWorkoutAnalyticsUseCase` (ACWR, Volume).
- Repository tests to verify Room integration and SharedPreferences persistence.

### Manual Verification
- Verify workout progress updates on the dashboard.
- Confirm muscle recovery status and volume charts display correctly in the analytics section.
