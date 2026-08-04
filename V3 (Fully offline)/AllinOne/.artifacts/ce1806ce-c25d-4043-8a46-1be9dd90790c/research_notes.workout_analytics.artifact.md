# Workout Analytics Research Notes

## Current Implementation State
- **Screen**: `PerformanceDashboardScreen.kt` handles both Habits and Workouts.
- **Data Source**: `DataManager.kt` (specifically `WorkoutDataManager`).
- **Existing Components**:
    - ACWR (Acute:Chronic Workload Ratio)
    - Muscle Balance (Radar Chart)
    - Training Stability (Gauge)
    - Muscle Readiness (Recovery status)
    - Consistency Heatmap (Volume weighted)

## Identified Gaps in Analytics
1. **Total Volume Trend**: While ACWR shows ratio, users often want to see the raw volume growth (e.g., Total Reps per day).
2. **Exercise Mix**: No visualization showing the variety of exercises performed.
3. **Streak Details**: Streak data is currently just text in the summary.
4. **Time of Day vs Performance**: No correlation shown between when a workout happens and its completion level.

## Data Availability
- `Workout` class has `dailyProgress` (Map<String, Int>) which stores progress for specific dates.
- `Workout` class has `muscleGroups`.
- `Workout` class has `trackingMode` (Sets, Reps, Timer).

## Proposed New Features
1. **Daily Volume Timeline**: A scrollable bar chart showing total volume per day for the last 30 days.
2. **Intensity Matrix**: A grid showing how often the user reached 100% of their target vs lower percentages.
3. **Workout Diversity Index**: A visualization showing the balance between different `trackingMode` types.
4. **Exercise Focus Heatmap**: Showing which days certain muscle groups were targeted (detailed view).
