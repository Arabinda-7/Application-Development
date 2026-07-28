# Walkthrough - Workout Activity Crash Fix

I have fixed the crash that occurred when clicking the "Add Workout" button. The issue was caused by a mismatch between the activity code and the layout XML file.

## Changes Made

### UI Resources
#### [MODIFY] [activity_add_workout.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_add_workout.xml)
- Added missing `TextView` elements with IDs `tv_label_sets` and `tv_label_reps_per_set`.
- These elements were required by `AddWorkoutActivity.kt` for initialization and to handle click events for the "Sets" tracking mode.
- Styled the new labels to match the existing "Reps" and "Timer" labels.

## Verification Results

### Code Consistency
- Verified that all `findViewById` calls in `AddWorkoutActivity.kt` now have corresponding IDs in `activity_add_workout.xml`.
- The `lateinit var` properties `tvLabelSets` and `tvLabelRepsPerSet` will now be correctly initialized, preventing the crash.

### Visual Layout
- The "Sets" tracking mode UI now correctly displays labels for "SETS" and "REPS", making it consistent with the "Reps" (REPS) and "Timer" (SECONDS) modes.
