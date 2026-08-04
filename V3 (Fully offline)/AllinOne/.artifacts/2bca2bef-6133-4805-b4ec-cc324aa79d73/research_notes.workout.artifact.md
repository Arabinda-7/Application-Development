# Workout History Visualization Research (DS Engineer Persona)

For the Workout section, the data is more multidimensional than habits. We have **Muscle Groups**, **Tracking Modes (Sets/Reps/Timer)**, and **Calculated Calories**. The goal is to move from "Checking off a list" to "Scientific Training Optimization."

## 1. Muscle Balance Radar (Spider) Chart
- **Concept**: Visualizing the symmetry of your training.
- **Visualization**: A 5 or 6-point radar chart representing major muscle groups (Chest, Back, Legs, Shoulders, Arms, Cardio).
- **Data Science Angle**: Deviation Analysis. A perfectly symmetric polygon indicates balanced training, while skewness highlights potential injury risks or neglected areas (e.g., "Leg Day" skipped too often).
- **Metric**: Total volume (Sets/Reps) per group over the last 30 days.

## 2. Muscle Recovery "Readiness" Gauges
- **Concept**: A physiological decay model for muscle fatigue.
- **Visualization**: Horizontal progress bars or "Battery" icons for each muscle group.
- **Logic**: Apply a 48-72 hour recovery window. Every time a muscle is trained, the battery drops to 0%. It "recharges" linearly over time.
- **Data Insight**: Helps the user decide what to train today based on readiness rather than a rigid schedule.

## 3. Volume Distribution (Stacked Area / Streamgraph)
- **Concept**: Visualizing "Training Blocks" or Periodization.
- **Visualization**: A stacked area chart where each "layer" is a muscle group's total reps over time.
- **Data Insight**: Reveals if the user is stuck in a "plateau" or if they are successfully cycling through different focuses (e.g., "Endurance Block" vs "Hypertrophy Block").

## 4. Caloric Burn Velocity (Time-Series)
- **Concept**: Efficiency analysis.
- **Visualization**: Line graph comparing "Workout Duration" vs "Calories Burned."
- **Data Science Angle**: Finding the "Sweet Spot." Identify which workout types (e.g., Sets vs Timer) yield the highest caloric return per minute of effort.

## 5. Frequency Heatmap (Volume-Weighted)
- **Different from Habits**: Instead of just "Completed (Green/Red)", the intensity (shade) of the cell is determined by the **Total Load** (Sum of Reps/Sets) for that day.
- **Utility**: Distinguishes between a "Light Recovery Day" and a "Heavy Max Session."

---

# Suggested Immediate Implementation
1. **Muscle Balance Radar Chart**: Replaces the text-based "Muscle Stats" dialog with a professional visual.
2. **Recovery Status Dashboard**: Uses a linear decay model to show "Ready to Train" muscles.
3. **Volume Load Heatmap**: A weighted version of the consistency grid.
