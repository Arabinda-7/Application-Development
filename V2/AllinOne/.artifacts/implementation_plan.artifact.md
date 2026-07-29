# Implementation Plan - Workout Section Expansion Logic

Modify the workout list to ensure only one item can be expanded at a time and collapse all items when navigating away from the screen.

## User Review Required

> [!IMPORTANT]
> The "collapse on leave" will be implemented in `onStop()` to ensure items are collapsed when the user moves to another activity or the home screen.

## Proposed Changes

### [Workout Section]

#### [MODIFY] [WorkoutAdapter.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutAdapter.kt)
- Update click listeners for `workoutCard` and `expandChevron` to collapse any other expanded workout when a new one is expanded.
- Add a public `collapseAll()` method that resets `isExpanded` for all workouts.

#### [MODIFY] [WorkoutRoutineActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutRoutineActivity.kt)
- Override `onStop()` and call `listSection.workoutAdapter.collapseAll()`.

## Verification Plan

### Automated Tests
- None planned as this is mostly UI/Adapter logic.

### Manual Verification
1. Open the Workout Routine screen.
2. Expand a workout item.
3. Click on another workout item and verify the first one collapses while the second one expands.
4. Expand an item and then press "Back" or navigate to another screen (e.g., Settings).
5. Return to the Workout Routine screen and verify all items are collapsed.
