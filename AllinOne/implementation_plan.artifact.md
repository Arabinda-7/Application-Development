# Implementation Plan - Advanced Workout Analytics (Senior DS Perspective)

This plan implements the "Senior Data Science" visualizations for the Workout History, focusing on physiological modeling (ACWR), progression velocity, and training stability.

## User Review Required

> [!IMPORTANT]
> The **Acute:Chronic Workload Ratio (ACWR)** model is a high-level sports science metric. I will use "Estimated Volume" (Sets × Reps) as the primary workload unit.
>
> [!NOTE]
> For **Training Entropy**, we will calculate the variance in "Workout Start Times" over the last 30 days. This requires checking when `completedDates` were added (using `timestamp` if available, otherwise assuming standard times).

## Proposed Changes

### 1. Data Analytics Layer ([DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt))

#### [MODIFY] `DataManager.kt`
- Implement `getVolumeWeightedHeatmap(calendar)`: Calculates workload intensity per day.
- Implement `getMuscleDistributionData()`: Real implementation aggregating volume per muscle group (30d).
- Implement `getMuscleRecoveryStatus()`: Real implementation using a 48h linear decay model per muscle group.
- [NEW] `getACWRData()`: Returns acute (7d) and chronic (28d) workload averages for the ACWR chart.
- [NEW] `getTrainingStabilityScore()`: Calculates entropy of workout timestamps.
- [NEW] `getExerciseProgressionVelocity()`: Calculates the Z-Score of volume for a specific exercise over time.

### 2. UI Components ([AnalyticsComponents.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/AnalyticsComponents.kt))

#### [MODIFY] `AnalyticsComponents.kt`
- [NEW] `ACWRChart`: A dual-axis area chart showing Fitness (Chronic) vs Fatigue (Acute) and the "Sweet Spot" corridor.
- [NEW] `StabilityChaosGauge`: A circular gauge visualizing "Training Entropy" (Consistency).
- [NEW] `ProgressionVelocityChart`: A line chart showing the relative growth (Z-Score) of total workload.

### 3. Screen Integration ([PerformanceDashboardScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/ui/performance/PerformanceDashboardScreen.kt))

#### [MODIFY] `PerformanceDashboardScreen.kt`
- Integrate the new `ACWRChart` and `StabilityChaosGauge` into the `isWorkoutContext` flow.
- Re-organize the analytics section to prioritize high-impact data science cards.

---

## Verification Plan

### Automated Tests
- **Workload Logic**: Verify `getDailyVolume` correctly handles different tracking modes (Sets vs Reps).
- **Decay Model**: Test that a muscle group trained 24 hours ago shows exactly 50% recovery in the model.
- **ACWR Math**: Verify that if volume is perfectly consistent, ACWR returns 1.0.

### Manual Verification
- Deploy to device and navigate to Workout History.
- Verify the **Volume Heatmap** shows varying intensities (opacities) based on workout "heaviness".
- Check that the **Muscle Radar Chart** accurately reflects the breakdown of muscle groups from the last 30 days of data.
- Observe the **ACWR Chart** "Sweet Spot" corridor (0.8 - 1.3) updates correctly as new mock data is added.
