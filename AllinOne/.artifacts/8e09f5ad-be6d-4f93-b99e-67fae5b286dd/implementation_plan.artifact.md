# Implementation Plan - Real-time Performance Dashboard Filters

Enable real-time dashboard updates when switching between primary filter types (Overall, Habits, Workouts). This involves adding a high-level filter selector and ensuring all analytics components react to state changes immediately.

## User Review Required

> [!IMPORTANT]
> The "Primary Filter" will be added at the top of the Performance Dashboard. This will allow users to switch between **Overall**, **Habit-specific**, and **Workout-focused** analytics without leaving the screen.

## Proposed Changes

### [Component] Performance Dashboard

#### [MODIFY] [PerformanceDashboardScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/performance/PerformanceDashboardScreen.kt)
- Define a `PerformanceFilterType` enum: `OVERALL`, `HABITS`, `WORKOUTS`.
- Add a internal state for the primary filter.
- Implement a `PrimaryFilterSelector` (LazyRow of Chips) at the top of the screen.
- Synchronize `isWorkoutContext` and `selectedHabitName` logic with the primary filter.
- Ensure all `remember` blocks for data calculations are keyed by the relevant filter state to trigger re-computation in real-time.
- Update the Habit Selector visibility: Only show it when the "HABITS" primary filter is selected.

### [Component] Compose Handlers

#### [MODIFY] [PerformanceHistoryComposeHandler.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/PerformanceHistoryComposeHandler.kt)
#### [MODIFY] [HabitHistoryComposeHandler.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/HabitHistoryComposeHandler.kt)
#### [MODIFY] [WorkoutHistoryComposeHandler.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutHistoryComposeHandler.kt)
- Update these handlers to support the new unified screen behavior if needed, or simply pass the initial filter type.

## Verification Plan

### Automated Tests
- Since this is primarily a UI and state management change, manual verification is preferred to ensure "real-time" responsiveness.

### Manual Verification
1. Open the Performance Dashboard.
2. Change the Primary Filter (Overall -> Habits -> Workouts).
3. Verify that all cards (Heatmap, Trends, Advanced Analytics) update immediately.
4. Select a specific habit in the Habits view and verify the dashboard reflects that habit's data.
5. Verify that the theme color updates based on the selected mode/habit.
