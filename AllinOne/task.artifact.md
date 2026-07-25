# Habit History Enhancement Task List

- [x] **Phase 1: Foundation & Habit-Specific History**
    - [x] Update `HabitDataManager` with analytical functions
        - [x] Implement `getHabitHistory(habitName, calendar)` for heatmaps
        - [x] Implement `calculateStreaks(habitName)`
    - [x] Update `PerformanceDashboardScreen` UI
        - [x] Add Habit Selector (Chips or horizontal list)
        - [x] Display Streaks (Current/Best)
        - [x] Link Habit Selector to Heatmap and Performance cards
- [x] **Phase 2: Gamification & Reflections**
    - [x] Add "Daily Notes/Reflection" field to `DayHistory` or a separate storage
    - [x] Add UI to edit/view notes for a selected day in the dashboard
- [x] **Phase 3: Advanced Visualizations**
    - [x] Implement Cyclical Radar Chart (Sun-Sat performance)
    - [x] Implement Stability Index (Routine variance)
    - [x] Implement Habit Stacking Flow (Markov Chain probabilities)
