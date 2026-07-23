# Tasks - Advanced Habit Analytics

- [x] Implement Analytics Logic in `DataManager.kt`
    - [x] Add `getHabitCorrelationMatrix()`
    - [x] Add `getTemporalDensityData()`
    - [x] Add `getRollingAverageProgress()`
- [x] Create Analytics UI Components
    - [x] Implement `ConsistencyHeatmap` (GitHub style)
    - [x] Implement `PunchCardChart` (Power Hours)
    - [x] Implement `CorrelationInsightCard`
- [x] Update `PerformanceDashboardScreen.kt`
    - [x] Integrate Heatmap below Trend Chart
    - [x] Integrate Punch Card/Insights section
- [ ] Verify Implementation
    - [ ] Test with mock data for various historical lengths
    - [ ] Ensure performance (no jank during chart rendering)
