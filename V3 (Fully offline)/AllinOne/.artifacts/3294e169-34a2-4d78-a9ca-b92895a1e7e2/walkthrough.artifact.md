# Walkthrough - Advanced Workout Analytics

I have implemented professional-grade data science visualizations for the Workout History section. These changes transform the workout log into a scientific training diagnostic tool.

## Key Changes

### 1. Physiological Readiness (ACWR)
Introduced the **Acute:Chronic Workload Ratio (ACWR)** chart. This visualization compares your "Fatigue" (7-day workload) against your "Fitness" (28-day average).
- A "Sweet Spot" green corridor (0.8 - 1.3 ratio) highlights the optimal training intensity for growth.
- Helps prevent overtraining and injury by identifying workload spikes.

### 2. Muscle Balance & Recovery
- **Muscle Radar Chart**: Visualizes the volume distribution across major muscle groups (Chest, Back, Legs, Shoulders, Arms) over the last 30 days.
- **Recovery Dashboard**: A set of gauges showing real-time recovery status based on a 48-hour physiological decay model.

### 3. Training Stability & Volume
- **Stability Chaos Gauge**: Measures the mathematical "Entropy" of your training rhythm.
- **Volume Heatmap**: The consistency grid now uses **Weighted Opacity**—darker cells represent heavier sessions (Total Volume = Sets × Reps).

---

## Technical Implementation Details

- **DataManager.kt**: Added complex analytical functions:
    - `calculateRollingWorkload`: Computes mean volume over moving windows.
    - `getACWRData`: Generates data points for the dual-axis chart.
    - `getMuscleRecoveryStatus`: Implements the 48h linear decay model.
- **AnalyticsComponents.kt**: Built custom `Canvas`-based Compose components for high-performance rendering of the ACWR area chart and Circular Gauges.

---

## Verification Results

### Automated Tests
- ✅ Build successful (`app:assembleDebug`).
- ✅ Verified `calculateRollingWorkload` logic with edge cases (zero volume days).

### Manual Verification Recommendation
- Open **Workout History**.
- Observe the new **Physiological Readiness** card at the top of the analytics section.
- Check the **Muscle Balance** radar chart to see your training symmetry.
- Verify the **Volume Intensity** heatmap at the bottom reflects different workout "weights".

---

> [!TIP]
> To get the most out of these charts, ensure you are logging **Reps** and **Sets** (or **Timer** duration) for your workouts, as the models use these to calculate total training volume.
