# Implementation Plan - Scientific Workout Analytics

This plan introduces performance-focused visualizations to the Workout History section, using physiological models and volume analysis to optimize training.

## Proposed Changes

### 1. Analytics Layer (DataManager.kt)
- **Implement `getMuscleDistributionData()`**: Aggregates total volume (sets/reps weighted) per muscle group over the last 30 days.
- **Implement `getMuscleRecoveryStatus()`**: A model that calculates recovery percentages for each muscle group based on the last time they were trained (48-hour decay).
- **Implement `getWeightedWorkoutHeatmap()`**: Returns a list of daily "Workload" scores (Volume-weighted) for the heatmap.

### 2. UI Components (WorkoutAnalyticsComponents.kt)
- [NEW] **Muscle Radar Chart**: A custom `Canvas` component to visualize training symmetry.
- [NEW] **Recovery Status Dashboard**: A grid of "Muscle Readiness" gauges.
- [NEW] **Volume Intensity Heatmap**: A color-coded grid where intensity represents workload, not just completion.

### 3. Integration (PerformanceDashboardScreen.kt)
- Update the workout-specific history view to include these new "Performance" cards.

---

## Proposed Visualizations

### [NEW] Muscle Balance Radar
A 5-axis chart (Chest, Back, Legs, Shoulders, Arms).
- **Goal**: Highlight neglected muscle groups using "Anatomical Coverage" data.

### [NEW] Muscle Recovery "Battery"
A set of vertical or horizontal gauges showing which muscles are 100% recovered and which are still "Fatigued".

### [NEW] Volume Load Heatmap
Similar to the Habit heatmap but with **Weighted Opacity** based on the total sets/reps performed that day.

---

## User Review Required

> [!NOTE]
> The **Recovery Model** assumes a standard 48-hour recovery time for all muscles. We can later add settings to customize this for advanced users (e.g., 72h for legs).

---

## Verification Plan

### Automated Tests
- Test the recovery calculation logic: Ensure a muscle trained 24 hours ago shows ~50% recovery.

### Manual Verification
- Deploy and navigate to Workout History.
- Verify the Radar Chart correctly reflects the volume from the last 30 days.
- Ensure the "Muscle Balance" dialog (if still present) matches the visual data.
