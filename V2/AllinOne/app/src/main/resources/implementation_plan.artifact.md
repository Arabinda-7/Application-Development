# Implementation Plan - Prevent Items from appearing below the Create Button

The user wants to ensure that no habits or workouts are visible below the "Create" button in their respective tracker screens. Currently, the `RecyclerView` in both `activity_habit_tracker.xml` and `activity_workout_routine.xml` is constrained to the bottom of the parent, which causes it to overlap with the floating "Create" button at the bottom.

## Proposed Changes

### [Layout] [activity_habit_tracker.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_habit_tracker.xml)
- Update the `habit_list` RecyclerView constraints.
- Change `app:layout_constraintBottom_toBottomOf="parent"` to `app:layout_constraintBottom_toTopOf="@id/btn_create_new_habit"`.
- Add a small `android:layout_marginBottom` to the RecyclerView if needed for better spacing.

### [Layout] [activity_workout_routine.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_workout_routine.xml)
- Update the `workout_list` RecyclerView constraints.
- Change `app:layout_constraintBottom_toBottomOf="parent"` to `app:layout_constraintBottom_toTopOf="@id/btn_create_new_workout"`.
- Add a small `android:layout_marginBottom` to the RecyclerView if needed for better spacing.

## Verification Plan

### Manual Verification
- Open Habit Tracker and verify that the list ends above the "CREATE A NEW HABIT" button.
- Open Workout Routine and verify that the list ends above the "CREATE A NEW WORKOUT" button.
- Ensure that scrolling still works correctly and items are not hidden prematurely.
