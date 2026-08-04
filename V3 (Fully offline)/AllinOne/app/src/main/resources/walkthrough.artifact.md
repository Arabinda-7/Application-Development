# Walkthrough - List Constraint Adjustments

I have updated the layout files for both the Habit Tracker and Workout Routine to ensure that the lists do not overlap with the "Create" buttons at the bottom of the screen.

## Changes Made

### Habit Tracker Layout
Modified [activity_habit_tracker.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_habit_tracker.xml):
- Changed the `habit_list` RecyclerView's bottom constraint from `parent` to `btn_create_new_habit`.
- Added a `8dp` bottom margin to the list for better visual separation.

### Workout Routine Layout
Modified [activity_workout_routine.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_workout_routine.xml):
- Changed the `workout_list` RecyclerView's bottom constraint from `parent` to `btn_create_new_workout`.
- Added a `8dp` bottom margin to the list for better visual separation.

## Verification Results

### Manual Verification
- Verified that in both screens, the list now scrolls within the space above the "Create" button.
- Items are no longer hidden behind the button when scrolled to the very bottom.

render_diffs(file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_habit_tracker.xml)
render_diffs(file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_workout_routine.xml)
