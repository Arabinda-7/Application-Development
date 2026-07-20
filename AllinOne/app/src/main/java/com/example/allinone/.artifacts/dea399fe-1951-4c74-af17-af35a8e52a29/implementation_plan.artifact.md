# Implementation Plan - Global Keyboard Dismissal

Implement the "dismiss keyboard on tap outside" feature across all remaining sections of the app by ensuring all activities inherit from `BaseActivity` and properly handle touch events.

## User Review Required

> [!NOTE]
> This change involves migrating several activities from `AppCompatActivity` to `BaseActivity`. This is safe as `BaseActivity` itself extends `AppCompatActivity` and provides additional global features like theme management and the new keyboard dismissal logic.

## Proposed Changes

### [Activity Migration]

The following activities will be updated to extend `BaseActivity` instead of `AppCompatActivity`. This will automatically enable the keyboard dismissal logic implemented in the previous task.

#### [MODIFY] [FinanceHistoryActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/FinanceHistoryActivity.kt)
#### [MODIFY] [FinanceMonthHistoryActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/FinanceMonthHistoryActivity.kt)
#### [MODIFY] [HabitDetailActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/HabitDetailActivity.kt)
#### [MODIFY] [LedgerHistoryActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/LedgerHistoryActivity.kt)
#### [MODIFY] [LockActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/LockActivity.kt)
#### [MODIFY] [PersonalLedgerBookActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/PersonalLedgerBookActivity.kt)
#### [MODIFY] [PersonalLedgerHubActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/PersonalLedgerHubActivity.kt)
#### [MODIFY] [TimerActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/TimerActivity.kt)
#### [MODIFY] [WorkoutDetailActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutDetailActivity.kt)

### [BaseActivity Refinement]

I will double-check `BaseActivity` to ensure it doesn't have redundant imports and that the logic is robust.

#### [MODIFY] [BaseActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/BaseActivity.kt)

## Verification Plan

### Manual Verification
- Deploy the app and navigate to different sections (e.g., Finance History, Ledger History).
- Tap on any input field (if present) or any interactive area.
- Verify that tapping outside the input field or on a "blank" area dismisses the keyboard.
- Verify that standard click behaviors (buttons, list items) still work as expected.
