# Fix Crash when opening Workout Activity

The app crashes when opening the Workout Routine screen due to a `NullPointerException` in `WorkoutThemeManager`. This is caused by passing null references for chips that don't exist in the layout but are expected by the theme manager.

## User Review Required

> [!IMPORTANT]
> The fix involves changing the `WorkoutThemeManager` constructor to accept a `RadioGroup` instead of a static list of `RadioButton`s. This ensures that dynamically added filter chips also receive the theme styling.

## Proposed Changes

### [app]

#### [MODIFY] [WorkoutThemeManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutThemeManager.kt)
- Update constructor to take `filterGroup: RadioGroup` instead of `chips: List<RadioButton>`.
- Update `applyChips` to iterate over children of `filterGroup`.
- Add null safety checks.

#### [MODIFY] [WorkoutRoutineActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutRoutineActivity.kt)
- Update `WorkoutThemeManager` initialization to pass the `RadioGroup` found by `R.id.filter_chips`.

## Verification Plan

### Automated Tests
- N/A (UI-related logic, will rely on manual verification).

### Manual Verification
1. Open the app.
2. Navigate to the Workout section.
3. Verify the app no longer crashes.
4. Verify that filter chips (ALL, MORNING, etc.) have the correct theme colors applied.
5. Check if dynamic chips (like muscle group filters) also show the correct theme when switching filter types in settings.
