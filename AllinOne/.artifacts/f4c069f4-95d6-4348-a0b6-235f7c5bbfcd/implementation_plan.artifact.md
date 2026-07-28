# Implementation Plan - Workout History UI Enhancements

This plan addresses two user requests for the Workout History section:
1. Adding a permanent mark for the current date in the history calendar grid.
2. Ensuring the default colors of the section components match the user-selected workout color.

## Proposed Changes

### [Component] [WorkoutHistoryGridSection](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutHistoryGridSection.kt)

#### [MODIFY] [WorkoutHistoryGridSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutHistoryGridSection.kt)
- Update `createDayView` to identify "today" and add a permanent visual marker (a small colored dot below the day number).
- Dynamically apply the user-selected workout color to the selection background stroke instead of using the hardcoded color in the XML drawable.

### [Component] [WorkoutPerformanceSection](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutPerformanceSection.kt)

#### [MODIFY] [WorkoutPerformanceSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutPerformanceSection.kt)
- Ensure the `ProgressBar` (`pb_workouts_history`) and other accent icons/text in the performance card use the user-selected workout color.

### [Component] [WorkoutRoutineActivity](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutRoutineActivity.kt)

#### [MODIFY] [WorkoutRoutineActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutRoutineActivity.kt)
- Update `updateHistoryUI` (or a similar method) to apply the theme to the history stat cards (Streak, Workouts, Efficiency) so they match the user-selected color.
- Alternatively, expand `WorkoutThemeManager` to handle these history components.

### [Component] [WorkoutThemeManager](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutThemeManager.kt)

#### [MODIFY] [WorkoutThemeManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutThemeManager.kt)
- Add logic to theme the history-specific components:
    - History stat cards' stroke colors and text colors.
    - History title and navigation accents.

## Verification Plan

### Manual Verification
1. Open the Workout Routine screen.
2. Navigate to the History tab.
3. Verify that the current date in the grid has a permanent marker (e.g., a dot).
4. Change the workout color in Settings.
5. Return to History and verify that:
    - The selected date border matches the new color.
    - The stat cards (Streak, Workouts, Efficiency) use the new color.
    - The progress bars in the performance card use the new color.
    - Today's permanent marker uses the new color.
