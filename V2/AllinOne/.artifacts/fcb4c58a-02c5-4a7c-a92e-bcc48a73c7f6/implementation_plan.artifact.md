# Implementation Plan - Habit History Header Refinement

The user wants to move the back button "into the header" in the Habit History screen. I will align the back button horizontally with the title area for a more integrated look in both the Compose and XML versions of the History screen.

## User Review Required

> [!NOTE]
> I will align the back button horizontally with the "MOMENTUM LOG" and Title area to make the header more compact.

## Proposed Changes

### [Compose UI]

#### [MODIFY] [PerformanceDashboardScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/performance/PerformanceDashboardScreen.kt)
- Reorganize the header `item`.
- Place the `onBack` button in a `Row` along with the `title` text.
- Move the calendar icon to the end of that same row or maintain its top-right position.

---

### [XML Layout]

#### [MODIFY] [activity_habit_tracker.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_habit_tracker.xml)
- In the `history_layout` section, align `btn_back_history` horizontally with `tv_grid_month`.
- Use a `Row`-like structure or adjust `ConstraintLayout` anchors to put them on the same line.

#### [MODIFY] [activity_workout_routine.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_workout_routine.xml)
- Apply similar changes to the `history_layout` section in Workout History for consistency.

## Verification Plan

### Manual Verification
- Open Habit History (via menu or swipe).
- Verify the back button is now horizontally aligned with the "HABIT HISTORY" title.
- Verify the back button still works.
- Check the Workout History screen to ensure consistent styling.
