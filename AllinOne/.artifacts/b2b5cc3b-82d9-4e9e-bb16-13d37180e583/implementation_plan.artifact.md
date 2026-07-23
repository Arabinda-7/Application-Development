# Implementation Plan - Advanced Goal Interaction & Digital Timer

This plan improves the interaction flow by making the goal title clickable and introduces a dual-roller digital timer for better precision.

## User Review Required

> [!NOTE]
> - Clicking the "Goal Target" title will now trigger the same roller dialog as the roller icon.
> - The Timer roller will be upgraded to a dual-roller system: one for Minutes and one for Seconds (e.g., `10min : 30sec`).

## Proposed Changes

### [Component] Layouts

#### [NEW] [dialog_timer_roller.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/dialog_timer_roller.xml)
- A new dialog layout containing two `NumberPicker`s:
    - **Minutes**: 0 to 59
    - **Seconds**: 0 to 59
- Includes "MIN" and "SEC" labels for clarity.

### [Component] Logic

#### [MODIFY] [AddWorkoutActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddWorkoutActivity.kt)
#### [MODIFY] [WorkoutRoutineActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutRoutineActivity.kt)
- Update `initViews` to bind the `tv_goal_title` view.
- Add a click listener to `tv_goal_title` that triggers the roller dialog corresponding to the `selectedMode`.
- Implement `showTimerRollerDialog()`:
    - Converts total seconds from the input field into `minutes` and `remaining seconds` for the pickers.
    - On confirmation, converts the picker values back to total seconds and updates the input field.
- Update the existing roller icon listeners to use the new `showTimerRollerDialog()`.

## Verification Plan

### Manual Verification
1. Open "Add Workout".
2. Click on the text "Goal Target".
3. Verify that the appropriate roller dialog opens (Reps roller for Reps mode, etc.).
4. Switch to **Timer** mode.
5. Click either the roller icon or the "Goal Target" title.
6. Verify the new dual-roller dialog opens with Minutes and Seconds.
7. Select a time (e.g., 2 min 15 sec) and confirm.
8. Verify the input field shows `135` (total seconds).
