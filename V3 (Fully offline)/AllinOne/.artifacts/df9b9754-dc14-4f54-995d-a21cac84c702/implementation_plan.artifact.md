# Implementation Plan - Dual Analytic Integration (Compose Dashboard + XML History)

Add new analytic and visualization features (Streak Milestones, Monthly Momentum, Resilience) to the XML-based **Habit History** section, while maintaining the existing implementation in the **Performance Dashboard**.

## Proposed Changes

### [Navigation]

#### [MODIFY] [HabitNavigationSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/HabitNavigationSection.kt)
- Revert `switchTab("HISTORY")` to show the XML `history_layout` and hide `history_compose_view`. This ensures the Habit Tracker's "History" tab points to the intended screen.

---

### [Habit History Enhancements (XML)]

#### [MODIFY] [activity_habit_tracker.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_habit_tracker.xml)
- Add the following sections inside the `history_layout` ScrollView:
    - **Streak Milestones Card**: Includes a progress bar and text labels for current streak and next target.
    - **Monthly Momentum Card**: A container for displaying completion percentages over the last 6 months.
    - **Advanced Metrics Row**: Displays "Routine Stability" and "Resilience" side-by-side using circular or numeric indicators.

#### [MODIFY] [HabitPerformanceSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/HabitPerformanceSection.kt)
- Update `update(dateKey: String)` to fetch data from `DataManager` (Resilience, Momentum, Milestones).
- Bind this data to the new XML views.
- Implement a helper to dynamically generate "Momentum bars" in the XML container.

## Verification Plan

### Manual Verification
- **Habit Tracker**: Navigate to the "History" tab. Verify that the XML screen (circular grid) is shown and contains the new Milestone, Momentum, and Resilience cards.
- **Performance Dashboard**: Open the dashboard from the main menu. Verify that the new analytics are still present there as well.
- **Data Sync**: Complete a habit and verify that both screens reflect the updated streak/momentum data.
