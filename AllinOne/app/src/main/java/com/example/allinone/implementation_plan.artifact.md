# Implementation Plan - Fix Habit Completion Crash

The goal is to fix the crash occurring when users complete habits, which is likely caused by uninitialized map fields in habits loaded from older data.

## User Review Required

> [!IMPORTANT]
> This fix will ensure that all existing habits have their `dailyProgress` and `completedDates` fields properly initialized after loading from storage. This prevents NullPointerExceptions when interacting with habits created before these fields were added.

## Proposed Changes

### [Component] Data Management

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Update `loadData` to iterate through all habits after deserialization.
- Initialize `dailyProgress` and `completedDates` if they are null (due to Gson deserialization of older objects).

---

### [Component] Habit Adapter

#### [MODIFY] [HabitAdapter.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/HabitAdapter.kt)
- Ensure `applyFilterAndSort()` is called when a multi-target habit (target > 1) is completed via incrementing. This ensures the habit moves to the "Completed" section immediately.
- Add null-safe access to `dailyProgress` just as an extra layer of protection.

## Verification Plan

### Automated Tests
- I will verify the logic by manual inspection and ensure the code builds successfully.

### Manual Verification
1. Open the app with existing habit data.
2. Complete a single-target habit (checkbox) and verify no crash occurs.
3. Complete a multi-target habit (increment buttons) and verify it moves to the completed section without crashing.
