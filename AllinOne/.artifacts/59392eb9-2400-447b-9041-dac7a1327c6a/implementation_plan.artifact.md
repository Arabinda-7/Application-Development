# Implementation Plan - Remove Journey Section & UI Polish

This plan details the removal of the "Journey" feature and refining the multi-option selection UI to match the app's professional dark theme.

## User Review Required

> [!IMPORTANT]
> - **Journey Removal**: The "Journey" tab will be completely removed from Habits and Workouts. All related data and logic will be purged.
> - **UI Polish**: The standard system dropdown/list UI (shown in your image) will be replaced with the app's custom glassmorphic selection dialog.

## Proposed Changes

### [Journey Feature Removal]

#### [DELETE] [Journey.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/Journey.kt)
#### [DELETE] [JourneyComponents.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/JourneyComponents.kt)

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Remove `userActiveJourneys`, `predefinedJourneys`.
- Remove `initializePredefinedJourneys()` and `startJourney()`.
- Update `saveData` and `loadData` to remove these keys.

#### [MODIFY] [activity_habit_tracker.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_habit_tracker.xml) & [activity_workout_routine.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_workout_routine.xml)
- Remove the `JOURNEY` tab from the mock bottom navigation.
- Remove the `journey_compose_view`.

#### [MODIFY] [HabitTrackerActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/HabitTrackerActivity.kt) & [WorkoutRoutineActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutRoutineActivity.kt)
- Remove `journeyComposeView` initialization and logic.
- Remove `JOURNEY` case from `switchTab()`.
- Revert navigation UI logic to only handle `TODAY` and `HISTORY`.

### [UI Selection Polish]

#### [MODIFY] [LockActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/LockActivity.kt)
- Update `showQuestionSelectionDialog` to use `dialog_settings_selection` and `item_settings_selection` instead of the generic categories dialog.
- Ensure the dialog is properly styled with the app's dark theme and blur background.

## Verification Plan

### Manual Verification
1.  Open Habits/Workouts: Verify the "JOURNEY" tab is gone.
2.  Open PIN Recovery Setup: Verify the question selection list matches the app's professional dark style (no white backgrounds).
