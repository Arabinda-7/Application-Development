# Implementation Plan - Fix Mixed Habit/Workout History

The user is seeing history data from "other items" (workouts or other habits) when viewing history in the Habit Tracker. This is primarily caused by `DataManager` and `PerformanceDashboardScreen` aggregating habit and workout data together even when a specific context (Habit or Workout) is expected.

## User Review Required

> [!IMPORTANT]
> The general "History" tab in Habit Tracker and Workout Tracker will now show ONLY items relevant to that section.
> The global "Performance History" (from the Dashboard) will continue to show both for a complete overview.

## Proposed Changes

### DataManager

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Add `getHabitStreak()` and `getWorkoutStreak()` methods.
- Refactor `getGlobalCompletionRate()` to take a `type` parameter (HABITS, WORKOUTS, ALL).
- Refactor `getHeatmapData()` to take a `type` parameter.
- Refactor `getTotalDailyProgress()` to take a `type` parameter.

### UI Components

#### [MODIFY] [PerformanceDashboardScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/PerformanceDashboardScreen.kt)
- Update `PerformanceSummary` to respect `isWorkoutContext` (hide workouts when in habit context, hide habits when in workout context).
- Update `TrendChart` to respect `isWorkoutContext` (show single bar instead of double bar if requested).
- Ensure "Overall Completion" in the summary card reflects only the relevant items for the current context.

### Activities

#### [MODIFY] [HabitTrackerActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/HabitTrackerActivity.kt)
- Update `updateHistoryUI()` to use habit-specific streak and completion rates.
- Update `setupDynamicHistoryGrid()` to show only habit progress in the calendar circles.
- Update `updatePerformanceCard()` to show only habit progress.

#### [MODIFY] [WorkoutRoutineActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutRoutineActivity.kt)
- Update `updateHistoryUI()`, `setupDynamicHistoryGrid()`, and `updatePerformanceCard()` to use workout-specific data.

#### [MODIFY] [HabitDetailActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/HabitDetailActivity.kt)
- Fix `tv_rate_fraction` text from "habits" to "days" or "completions" to avoid confusion when viewing a single habit.

## Verification Plan

### Automated Tests
- I will verify the logic changes by reviewing the code and ensuring the filtering is applied correctly.

### Manual Verification
- Deploy the app.
- Go to Habit Tracker -> History. Verify that only habit completions affect the heatmap and stats.
- Go to Workout Tracker -> History. Verify that only workout completions affect the heatmap and stats.
- Go to a specific Habit's detail screen. Verify the stats label is corrected.
