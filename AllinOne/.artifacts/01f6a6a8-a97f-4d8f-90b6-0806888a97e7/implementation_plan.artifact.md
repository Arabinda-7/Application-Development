# Implementation Plan - Simplify Habit History UI

This plan removes the "7-Day Completion Trend" and "Performance of the Day" cards from the Habit History section by making the shared `PerformanceDashboardScreen` component more configurable.

## User Review Required

> [!IMPORTANT]
> This change will hide the performance details and trend charts from the **Habit History** tab, leaving a cleaner calendar-focused view. These sections will remain visible in the main **Daily Performance** dashboard and the **Workout History** (unless otherwise specified).

## Proposed Changes

### UI Components

#### [MODIFY] [PerformanceDashboardScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/PerformanceDashboardScreen.kt)
- Add optional boolean parameters `showPerformanceCard` and `showTrendCard` (both defaulting to `true`).
- Wrap the Performance and Trend items in `if` statements within the `LazyColumn`.

---

### Habit Tracker Integration

#### [MODIFY] [HabitTrackerActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/HabitTrackerActivity.kt)
- Update `setupComposeHistory()` to pass `showPerformanceCard = false` and `showTrendCard = false` to the `PerformanceDashboardScreen`.

## Verification Plan

### Manual Verification
1. Open **Habit Tracker**.
2. Switch to the **History** tab.
3. Verify that only the calendar is visible, and the "Performance for..." and "Trend" cards are removed.
4. Open the main **Daily Performance** dashboard from the Home screen.
5. Verify that the cards are still visible there (ensuring no regression in shared components).
6. Open **Workout Tracker** history and verify its current state (it will still show the cards).
