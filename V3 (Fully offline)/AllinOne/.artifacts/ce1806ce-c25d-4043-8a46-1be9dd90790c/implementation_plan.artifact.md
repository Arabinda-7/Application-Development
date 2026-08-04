# Implementation Plan - Advanced Workout Analytics & Visualizations

Add comprehensive analytic and visualization features to the Workout History section to provide users with deeper insights into their training progress, volume, and consistency.

## User Review Required

> [!IMPORTANT]
> The new features will be added to the `PerformanceDashboardScreen` specifically when `isWorkoutContext` is true. This ensures the Daily Performance (Habits) section remains unchanged as requested.

## Proposed Changes

### Data Layer
#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Add `getMonthlyVolumeData()`: Returns a list of daily total volume for the current month.
- Add `getWorkoutDiversityData()`: Returns distribution of tracking modes.
- Add `getIntensityDistribution()`: Returns counts of workouts completed at various percentage ranges.
- Add `getDailyMuscleFocus()`: Returns which muscle groups were trained each day.

### UI Components
#### [MODIFY] [AnalyticsComponents.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AnalyticsComponents.kt)
- Add `VolumeProgressionChart`: A bar chart for daily volume.
- Add `WorkoutDiversityChart`: A horizontal distribution bar.
- Add `IntensityHeatmap`: A color-coded grid for workout intensity.
- Add `MuscleFocusGrid`: A small dot-grid showing muscle group targeting.

### Main Screen Integration
#### [MODIFY] [PerformanceDashboardScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/performance/PerformanceDashboardScreen.kt)
- Integrate new components inside the `if (isWorkoutContext)` block.
- Reorganize the layout to group related analytics (e.g., "Physiological readiness" vs "Volume & Intensity").

## Verification Plan

### Automated Tests
- Run existing unit tests to ensure `DataManager` logic for history remains intact.
- Add new tests for volume calculation logic in `DataManager`.

### Manual Verification
- Deploy to device/emulator.
- Navigate to Workout History.
- Verify that the new cards (Volume Progression, Workout Diversity, etc.) are visible and populated with data.
- Switch to Daily Performance (Habits) and verify no changes are present there.
