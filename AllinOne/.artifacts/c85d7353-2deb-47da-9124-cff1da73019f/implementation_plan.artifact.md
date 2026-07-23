# Reinforce App Stability against Dynamic View Removal

Extend the "View Caching" pattern to remaining major activities to prevent potential `NullPointerException` crashes caused by dynamic layout changes or activity recreation.

## User Review Required

> [!NOTE]
> This is a proactive stability improvement. I have identified several activities that call `findViewById` inside methods triggered by `onResume` or recurrent logic (like graph updates). While not currently crashing, these are at risk if their layouts ever become conditional.

## Proposed Changes

### [Component Name]

#### [MODIFY] [FinanceHistoryActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/FinanceHistoryActivity.kt)
- Cache `containerSpendGraph`, `avgLine`, `avgLabel`, `tooltipCard`, and `tooltipText`.
- Initialize in `onCreate`.
- Update `updateSpendGraph` to use these properties.

#### [MODIFY] [LedgerActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/LedgerActivity.kt)
- Cache `btnAddLedger`, `cardTotalBorrowed`, `cardTotalLent`, and `cardNetBalance`.
- Initialize in `onCreate`.
- Update `applySectionTheme` to use these properties.
- Fix minor indentation and redundant `setupKeyboardHandling` call.

#### [MODIFY] [SettingsActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/SettingsActivity.kt)
- Cache `layoutProfileHub`.
- Initialize in `onCreate`.
- Update `showHub` and `showSectionSettings` to use this property.

#### [MODIFY] [HabitDetailActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/HabitDetailActivity.kt)
- Cache `calendarGrid` and `tvMonth`.
- Initialize in `onCreate`.
- Update `setupCalendar` to use these properties.

## Verification Plan

### Automated Tests
- Build the app to ensure no syntax errors.

### Manual Verification
- Navigate through Finance History, Ledger, Settings, and Habit Details.
- Verify that UI updates (theme changes, month navigation) still work perfectly.
- Ensure no crashes occur when navigating back and forth between these screens.
