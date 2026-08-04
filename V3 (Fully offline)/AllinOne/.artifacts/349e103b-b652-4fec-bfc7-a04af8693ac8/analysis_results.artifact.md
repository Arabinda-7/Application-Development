# DataManager.kt Responsibility Analysis & Architectural Review

## 1. Complete Responsibility Analysis

`DataManager.kt` is currently a **God Object** that acts as a central hub for state, persistence, business logic, and analytics. It violates the Single Responsibility Principle (SRP) by managing nearly every feature in the application.

### Features Handled:
- **State Management**: In-memory `MutableList` and `MutableMap` for Habits, Workouts, Notes, Projects, Finance, Tasks, User Profile, and Settings.
- **Persistence Layer**: Custom JSON serialization to `SharedPreferences` and synchronization with `Room` repositories.
- **Analytics Engine**: Complex calculations for workout recovery, habit correlations, and financial forecasting.
- **Security & Lifecycle**: App locking, onboarding state, and background data reset logic.
- **AI Integration**: Configuration and session management for the AI Assistant.

### Responsibility Breakdown by Module:

| Module | Responsibilities in DataManager |
| :--- | :--- |
| **Habits** | Streaks, progress calculation, performance by frequency, habit correlation matrix. |
| **Workouts** | Volume calculations, ACWR (Acute:Chronic Workload Ratio), muscle distribution/recovery, workout history reconstruction. |
| **Finance** | Income/Expenditure tracking, budget resets, savings goals, category management. |
| **Notes/Projects** | Search logic, auto-archive, template management, field sanitization. |
| **User/Profile** | XP/Leveling system, profile data governance, activity logging. |
| **Common/Core** | Agenda merging, notifications scheduling, SharedPreferences migration, Export/Import. |

### Function Categorization:
- **Database Related**: `startDatabaseObservation`, `performSave` (syncAll calls), `loadTasksOnly`.
- **File/Storage Related**: `getPrefs`, `migrateToEncryptedPrefs`, `exportData`, `importData`, `loadData`.
- **Business Logic**: `addXP`, `calculateDayHistory`, `calculateRollingWorkload`, `getGrowthAdvice`, `getComprehensiveTodayAgenda`.

---

## 2. Dependency Map

The current flow uses in-memory lists as the source of truth, creating a fragile bridge between the UI and Persistence.

| Function | Model Used | Dependency | Proposed New Location |
| :--- | :--- | :--- | :--- |
| `saveHabit()` | `Habit` | `HabitRepository` (Room) + `Prefs` | `HabitRepository` |
| `addXP()` | `Context` | `SharedPreferences` | `UserStatsUseCase` |
| `calculateWorkoutVolume()` | `Workout` | In-memory `workouts` list | `WorkoutAnalyticsUseCase` |
| `addIncome()` | `Transaction` | `FinanceRepository` + `Prefs` | `FinanceRepository` |
| `getGrowthAdvice()` | `String` | None (Static logic) | `AiAdviceUseCase` |
| `exportData()` | `AllAppData` | `Gson` + All Repositories | `DataMigrationManager` |

---

## 3. Suggested Architecture

We should move towards a **Feature-based Clean Architecture**.

### Directory Structure:
```text
data/
  common/             # Base classes, Database modules
  habits/
    local/            # HabitDao, HabitPrefs
    repository/       # HabitRepositoryImpl
  workouts/
    ...
domain/
  habits/
    model/            # Habit, HabitHistory
    usecase/          # GetHabitStreaksUseCase, CompleteHabitUseCase
  workouts/
    usecase/          # CalculateRecoveryUseCase
presentation/
  habits/
    HabitViewModel.kt
  workouts/
    WorkoutViewModel.kt
```

---

## 4. Critical Issues Identified

### Violations of SOLID & Clean Code:
- **SRP Violation**: `DataManager` is 2158 lines long. It should be split into at least 10 different repositories and use cases.
- **Tight Coupling**: Almost all ViewModels directly access `DataManager` static fields, making unit testing impossible without mocking the entire global state.
- **Hidden Dependencies**: Many functions require a `Context` passed in as an argument, making them hard to test and prone to memory leaks if not handled carefully.

### Performance Issues:
- **Main Thread Serializing**: `performSave` uses `Gson().toJson()` on massive objects. For 1000+ notes or transactions, this will cause significant UI jank.
- **Memory Pressure**: Keeping every habit, workout, and note in memory simultaneously is inefficient for large datasets.
- **Redundant Persistence**: Writing to both Room and SharedPreferences ("Dual Writes") doubles the I/O overhead.

### Possible Bugs:
- **Race Conditions**: `synchronized(habits)` protects the list, but the subsequent `syncAll(hList)` is an async database operation. If two saves happen close together, the database might end up in an inconsistent state.
- **Data Loss Risk**: The 500ms debounce in `saveData` means the last change might not be saved if the app process is terminated abruptly (e.g., system clear).

---

## 5. Step-by-Step Migration Plan

### Phase 1: Decoupling & Repository Extraction
1. **Extract Feature Settings**: Move SharedPreferences keys and logic from `DataManager` to feature-specific `SettingsDataSource` classes (e.g., `HabitSettingsDataSource`).
2. **Shift Source of Truth**: Update ViewModels to observe `Flows` from `Repository` classes instead of accessing `DataManager.habits` directly.
3. **Database-First Approach**: Change `DataManager` logic to write directly to Repositories, which should handle their own persistence to Room, rather than the "syncAll" batch approach.

### Phase 2: Domain Logic Extraction
1. **Create UseCases**: Move logic like `calculateRollingWorkload` or `getHabitStreaks` into dedicated Domain UseCase classes.
2. **Move Analytics**: Extract the complex workout and habit analytics into a `domain.analytics` package.

### Phase 3: Infrastructure Cleanup
1. **Refactor Export/Import**: Move the massive `importData` logic to a dedicated `DataMigrationManager`.
2. **Deprecate DataManager**: Once all ViewModels are using Repositories/UseCases, delete the in-memory lists and finally the `DataManager` itself.
3. **DI Implementation**: Inject Repositories/UseCases into ViewModels using Hilt/Koin instead of accessing the `DataManager` singleton.
