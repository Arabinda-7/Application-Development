# Implementation Plan - Workout History Section

Improve the workout history section in `WorkoutRoutineActivity` to show full or partial progress using a calendar grid, as requested by the user.

## User Review Required

> [!IMPORTANT]
> The current "HISTORY" tab in `WorkoutRoutineActivity` uses a Compose-based dashboard. I will be switching it to use the XML-based `history_layout` which provides the calendar grid view shown in your reference image.

> [!NOTE]
> I will add a `dailyProgress` map to the `Workout` model to track partial progress historically, as currently it only tracks full completion dates.

## Proposed Changes

### Data Layer

#### [MODIFY] [Workout.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/Workout.kt)
- Add `var dailyProgress: MutableMap<String, Int> = mutableMapOf()` to track historical progress percentages.

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Update `getHeatmapData` for `WORKOUTS` to use `dailyProgress` if available, or fall back to `completedDates`.
- Ensure `saveData` correctly handles the updated `Workout` model.

### UI Layer

#### [MODIFY] [activity_workout_routine.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_workout_routine.xml)
- Add a Performance Card section inside the `history_layout` (below the grid) to show details for the selected date. This will mirror the habit history layout.

#### [MODIFY] [WorkoutRoutineActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutRoutineActivity.kt)
- Update `switchTab("HISTORY")` to show `history_layout` and hide `history_compose_view`.
- Implement `updatePerformanceCard(dateKey)` to show workout completion details for the selected day.
- Update `createDayView` to:
    - Match the visual style in the reference image (day number in center, circular ring).
    - Add selection logic (highlight the selected day).
    - Tint the circular progress bar with the workout theme color.
- Update `setupDynamicHistoryGrid` to ensure it populates the grid correctly when switching to the history tab.
- Update `workoutAdapter` listener to update `dailyProgress` in `DataManager`.

#### [MODIFY] [WorkoutAdapter.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutAdapter.kt)
- Ensure progress changes update the `dailyProgress` map for the current day.

## Verification Plan

### Manual Verification
1. Open Workout Routine.
2. Complete some workouts fully and some partially.
3. Switch to the HISTORY tab.
4. Verify the calendar grid shows progress rings (full or partial) for the current day and past days.
5. Tap on a day in the grid and verify the performance card below updates with the correct details.
6. Verify the colors match the workout theme.
