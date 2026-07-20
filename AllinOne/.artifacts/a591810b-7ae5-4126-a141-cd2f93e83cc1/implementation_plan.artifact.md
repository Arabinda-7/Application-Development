# Implementation Plan - App-Wide Keyboard Visibility Fixes

Standardize keyboard behavior across all input-heavy screens to ensure that the keyboard never obscures the text being typed.

## User Review Required

> [!IMPORTANT]
> This will apply `adjustResize` to all "Add" and "Settings" activities in the app and increase the bottom padding in their respective scrollable areas. This ensures a consistent typing experience where the viewport always adjusts to the keyboard.

## Proposed Changes

### Android Manifest
#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/AndroidManifest.xml)
- Add `android:windowSoftInputMode="adjustResize"` to the following activities:
    - `AddPersonActivity`
    - `LockActivity`
    - All "Settings" activities (`HabitSettingsActivity`, `WorkoutSettingsActivity`, etc.)

### Layout Spacing (XML)
#### [MODIFY] Multiple Layout Files
Increase `paddingBottom` to at least `48dp` in the inner scrollable containers for:
- `activity_add_finance.xml`
- `activity_add_habit.xml`
- `activity_add_note.xml`
- `activity_add_person.xml`
- `activity_add_project.xml`
- `activity_add_sub_feature.xml`
- `activity_add_task.xml`
- `activity_add_workout.xml`

## Verification Plan

### Manual Verification
1.  **Iterative Testing**: Open every "Add" section (Habit, Workout, Note, Task, Project, Finance, Person).
2.  **Keyboard Trigger**: Scroll to the bottom and focus on the last input field.
3.  **Visual Check**: Verify that the keyboard does not cover the cursor or the text being typed, and the screen is scrollable with the keyboard open.
