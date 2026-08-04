# Implementation Plan - Remove Footer from Habit and Workout Sections

Remove the bottom navigation footer (`bottom_nav_mock`) from the Habit Tracker and Workout Routine screens. Tab switching will remain accessible via swipe gestures and the settings menu.

## Proposed Changes

### Layouts

#### [MODIFY] [activity_habit_tracker.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_habit_tracker.xml)
- Remove the `bottom_nav_mock` `LinearLayout` block.

#### [MODIFY] [activity_workout_routine.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_workout_routine.xml)
- Remove the `bottom_nav_mock` `LinearLayout` block.

### Logic

#### [MODIFY] [HabitNavigationSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/HabitNavigationSection.kt)
- Remove footer-related parameters from the constructor (`ivToday`, `tvTodayNav`, `ivHistory`, `tvHistoryNav`).
- Remove `setup()` logic that sets click listeners on the footer tabs.
- Remove `updateNavUI()` logic as there is no navigation UI to update.

#### [MODIFY] [WorkoutNavigationSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutNavigationSection.kt)
- Remove footer-related parameters from the constructor.
- Remove `setup()` and `updateNavUI()` logic.

#### [MODIFY] [HabitTrackerActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/HabitTrackerActivity.kt)
- Update the initialization of `navigationSection` to match the new constructor.

#### [MODIFY] [WorkoutRoutineActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutRoutineActivity.kt)
- Update the initialization of `navigationSection` to match the new constructor.

## Verification Plan

### Manual Verification
- Deploy the app.
- Navigate to the Habit Tracker and Workout Routine sections.
- Verify that the bottom navigation bar is gone.
- Verify that swiping left/right still switches between "Today" and "History" tabs.
- Verify that the "History" option in the settings menu still works.
