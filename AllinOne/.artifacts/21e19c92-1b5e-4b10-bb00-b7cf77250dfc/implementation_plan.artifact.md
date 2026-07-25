# Implementation Plan - Project Modularization & Refactoring

This plan outlines the steps to decompose large, multi-section files into smaller, specialized modules. This will improve maintainability and follow the principle of "one file per specific section/function".

## User Review Required

> [!IMPORTANT]
> This refactoring will involve moving a significant amount of code. While I will ensure all references are updated, this is a major architectural change.

## Proposed Changes

### 1. UI Refactoring (Performance Dashboard)
Break down `PerformanceDashboardScreen.kt` into a new package `com.example.allinone.ui.performance`.

#### [NEW] [PerformanceSummary.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/performance/components/PerformanceSummary.kt)
- `PerformanceSummary`
- `ProgressRow`
- `CustomLinearProgressIndicator`
- `WorkoutProgressCircleItem`

#### [NEW] [PerformanceCharts.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/performance/components/PerformanceCharts.kt)
- `TrendChart`
- `DoubleBar`
- `LegendItem`

#### [NEW] [PerformanceCalendar.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/performance/components/PerformanceCalendar.kt)
- `CalendarDayItem`
- `DashboardCard` (Shared layout)

#### [MODIFY] [PerformanceDashboardScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/PerformanceDashboardScreen.kt)
- Keep only the main `PerformanceDashboardScreen` composable.
- Import components from the new files.

---

### 2. Data Refactoring (DataManager)
Decompose `DataManager.kt` (97KB) into specialized managers.

#### [NEW] [HabitDataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/HabitDataManager.kt)
- Habit tracking logic, streaks, and performance calculations.

#### [NEW] [WorkoutDataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/WorkoutDataManager.kt)
- Workout tracking, muscle balance, and calorie calculations.

#### [NEW] [FinanceDataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/FinanceDataManager.kt)
- Budgeting, transactions, and safe-spend calculations.

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Keep core persistence (Save/Load) and global state.
- Delegate specific domain logic to the new managers.

---

### 3. Home Screen Refactoring
Modularize `HomeScreen.kt` (67KB).

#### [NEW] [HomeCards.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/home/HomeCards.kt)
- `HabitCard`, `WorkoutCard`, `TaskCard`, `NoteCard`, `ProjectCard`, `FinanceCard`.

#### [NEW] [HomeHeader.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/home/HomeHeader.kt)
- Aura Header, Profile Greeting, and Search Bar components.

## Verification Plan

### Automated Tests
- Run full build to ensure all imports and references are correctly resolved.
- Verify that `DataManager` singleton still functions as the primary API for the rest of the app.

### Manual Verification
- Navigate through Performance History, Habit Tracker, and Home Screen to ensure UI consistency.
- Verify that real-time data updates still work across the new file boundaries.
