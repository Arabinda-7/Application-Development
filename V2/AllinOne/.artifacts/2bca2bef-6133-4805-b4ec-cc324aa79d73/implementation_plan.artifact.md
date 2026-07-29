# Implementation Plan - Advanced Habit Analytics (Data Science Edition)

This plan introduces advanced data visualizations and behavioral insights to the Habit History section, transforming it from a simple log into a personal performance analytics dashboard.

## Proposed Changes

### 1. Data Analysis Layer (DataManager.kt)
- **Implement Pearson Correlation**: Calculate the relationship between different habits' completion patterns to identify "Keystone Habits".
- **Temporal Density Calculation**: Aggregating completion data by Day-of-Week and Time-of-Day (Morning/Afternoon/Evening) for "Punch Card" analytics.
- **Rolling Averages**: Calculate 7-day and 30-day moving averages for performance to smooth out daily volatility.

### 2. UI Components (PerformanceDashboardScreen.kt)
- [NEW] **Consistency Heatmap**: A 4-week/1-month grid showing daily performance intensity using color gradients.
- [NEW] **Punch Card Visualization**: A density grid showing the user's "Power Hours".
- [MODIFY] **PerformanceDashboardScreen**: Add a scrollable "Insights" section at the bottom.

### 3. Detail Views (HabitDetailActivity.kt)
- [NEW] **Correlation Highlight**: Show which other habits are positively influenced when the current habit is completed.

---

## Proposed Visualizations

### [NEW] Consistency Heatmap
A GitHub-style grid where each cell's opacity/color matches the daily completion percentage.
- **Why**: Visualizes momentum and identify patterns in "off-days".

### [NEW] Habit Influence Graph (Correlation)
A textual insight card: *"You are 80% more likely to 'Exercise' when you 'Wake up early'."*
- **Why**: Helps the user identify their "Success Triggers".

### [NEW] Success Heatmap (Punch Card)
A grid of 7 days vs 3 times of day.
- **Why**: Pinpoints when the user is most disciplined.

---

## User Review Required

> [!IMPORTANT]
> Some of these visualizations (like Correlation) require at least 14 days of historical data to be statistically significant. I will implement "Empty State" placeholders for new users.

---

## Verification Plan

### Automated Tests
- Unit tests for the correlation calculation logic in `DataManager`.

### Manual Verification
- Deploy to device and navigate to Habit History.
- Verify the Heatmap renders correctly for the current month.
- Check if the "Power Hours" punch card matches actual tracking behavior.
