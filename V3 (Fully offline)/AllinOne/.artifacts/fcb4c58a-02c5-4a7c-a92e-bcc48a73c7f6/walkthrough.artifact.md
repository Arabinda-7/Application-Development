# Walkthrough - Reverting History Title Position

I have reverted the title name position in the Habit and Workout History screens to their original vertical arrangement, as requested.

## Changes Made

### 1. Compose UI Header Revert
- **File**: `PerformanceDashboardScreen.kt`
- Restored the vertical layout where the Title ("HABIT HISTORY" / "WORKOUT HISTORY") appears below the back and calendar buttons.
- Ensured proper spacing and padding consistent with the original design.

### 2. XML Layout Header Revert
- **Files**: `activity_habit_tracker.xml`, `activity_workout_routine.xml`
- Reverted the constraint changes in the `history_layout` section.
- The month/year titles (`tv_grid_month`, `tv_history_title`) are once again positioned below the back button (`btn_back_history`).

## Verification Results

### Automated Tests
- Ran `:app:assembleDebug` - **Build Successful**.

### Manual Verification
- Verified that the titles in both Habit and Workout history are back in their original positions.
- Verified that the layout still looks correct on the device.
