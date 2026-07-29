# Workout Detail UI Simplification & Partial Progress

This plan simplifies the workout detail screen to match the "Add Project" style and ensures partial progress (e.g., 50%) is correctly displayed and editable in the individual history section.

## User Review Required

> [!IMPORTANT]
> Tapping a date in the calendar will now show a simple dialog to enter your progress (e.g., number of reps or sets completed) instead of just toggling between 0% and 100%.

## Proposed Changes

### UI Simplification (Style parity with Add Project)

#### [MODIFY] [activity_workout_detail.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_workout_detail.xml)
- Adopt the `AddProject` layout structure:
    - Dynamic header accent view.
    - Sticky toolbar with Back and Edit (Pencil) icons.
    - Title and meta chips at the top of the scrollable content.
    - Clear section headers for "OVERALL STATS" and "WORKOUT LOG".
    - Simplified, consistent card styles for stats.

### Partial Progress & Real-time Updates

#### [MODIFY] [WorkoutDetailActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutDetailActivity.kt)
- **Data Binding:** Update `setupCalendar` to fetch exact progress from `workout.dailyProgress`. If an entry exists, use that percentage; otherwise, fall back to `completedDates` (100% or 0%).
- **Interaction:** Replace the simple toggle logic with `showProgressInputDialog(dateKey)`.
    - This dialog will allow users to enter a value between 0 and `workout.target`.
    - It will automatically calculate the percentage and update both `dailyProgress` and `completedDates`.
- **UI Refresh:** Ensure `updateStats()` and `setupCalendar()` are called after every data change for immediate feedback.

## Verification Plan

### Manual Verification
1.  **Style Check:** Verify the new layout looks clean and consistent with the "Add Project" screen.
2.  **Partial Progress:**
    - Open a workout with a target (e.g., 10 Reps).
    - Tap a date in the calendar.
    - Enter "5" in the input dialog.
    - Verify the calendar day shows a half-filled progress circle (50%).
    - Verify the "Completion Rate" stat updates to reflect this partial progress.
3.  **Persistence:** Re-open the activity to confirm the partial progress is saved.
