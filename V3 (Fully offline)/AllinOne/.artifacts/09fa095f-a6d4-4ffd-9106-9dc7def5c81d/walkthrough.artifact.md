# Walkthrough - Added Back Button and Title to History Screens

I have updated the `PerformanceDashboardScreen` to support a back button and a section title. I also configured the activities that use this screen to provide appropriate titles and navigation logic.

## Changes Made

### 1. Performance Dashboard Enhancements
- Modified `PerformanceDashboardScreen` to accept an optional `title` string.
- Updated the header layout to display the title alongside the back button.
- Improved the alignment of the back button and title for better visual balance.

### 2. Habit History Integration
- Configured `HabitTrackerActivity` to display the "HABIT HISTORY" title.
- Set the back button in the History tab to return to the "TODAY" tab.

### 3. Workout History Integration
- Configured `WorkoutRoutineActivity` to display the "WORKOUT HISTORY" title.
- Set the back button in the History tab to return to the "TODAY" tab.

### 4. Performance History Integration
- Configured `PerformanceHistoryActivity` to display the "PERFORMANCE HISTORY" title.
- The back button continues to finish the activity as it is a standalone screen.

## Verification Results

### Automated Tests
- Executed `:app:assembleDebug` successfully, confirming that all callsites were updated correctly and there are no compilation errors.

### Manual Verification
- Verified that the `PerformanceDashboardScreen` correctly handles the presence or absence of a title.
- Confirmed that the `onBack` lambda is correctly invoked from different activities.
