# Implementation Plan - Fix Workout History Stats

The user reported that the "Current Streak", "Workouts", and "Efficiency" sections in the Workout History screen are not updating. My investigation revealed that these stats are updated by `updateHistoryUI()` in `WorkoutRoutineActivity.kt`, but this method is only called when a workout timer finishes. It is missing from the general UI update flow and the data change listener. Additionally, the "Efficiency" calculation relies on a potentially empty `history` map in `DataManager`.

## User Review Required

> [!IMPORTANT]
> The "Efficiency" calculation will be changed to look at the last 30 days of completion data by default if no historical snapshots exist. This will provide a more meaningful "Consistency" metric than just a lifetime average of sparse snapshots.

## Proposed Changes

### Data Layer

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Refactor `getGlobalCompletionRate(type: String)` to calculate completion rate dynamically if the `history` map is empty or sparse.
- Ensure `checkAndResetDailyStats` creates a snapshot of the day's performance before resetting, so the `history` map populates naturally over time.

### UI Layer

#### [MODIFY] [WorkoutRoutineActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutRoutineActivity.kt)
- Update `updateAllUI()` to include a call to `updateHistoryUI()`.
- Call `updateHistoryUI()` in `onResume()` to ensure stats are fresh when returning to the activity.
- Call `updateHistoryUI()` when switching to the `HISTORY` tab via the `navigationSection` callback.

#### [MODIFY] [WorkoutPerformanceSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutPerformanceSection.kt)
- Minor cleanup: Ensure that the dynamic grid and performance cards are properly synchronized with the global stats.

## Verification Plan

### Automated Tests
- None at this stage as most logic is in Activity/Object.

### Manual Verification
1. Open **Workout Routine** activity.
2. Navigate to **History** tab.
3. Observe initial stats (Streak, Workouts, Efficiency).
4. Go back to **Today** tab and mark a workout as complete.
5. Return to **History** tab and verify that:
    - **Workouts** count increased.
    - **Efficiency** (Efficiency) percentage updated.
    - **Current Streak** updated if it was the first workout of a new day.
6. Verify that changing the date in the history grid correctly updates the "Performance for [Date]" card while keeping the top "Global" stats accurate.
