# Implementation Plan - Fix Muscle Group Visibility in Add Workout

The user reported that muscle group names are not visible in the "Add Workout" section. Investigation revealed that the `Chip` components used for muscle groups are being styled incorrectly, mixing `android:background` with Material `Chip` specific attributes, which leads to rendering issues in modern Material themes. Additionally, muscle group categories are not being persisted in `DataManager`.

## User Review Required

> [!IMPORTANT]
> The fix involves changing how `Chip` components are styled programmatically. This will ensure they are visible and follow the app's dark theme aesthetics. I will also add persistence for muscle group categories which was previously missing.

## Proposed Changes

### Workout Data Persistence
#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Add `KEY_WORKOUT_MUSCLE_GROUPS` constant.
- Update `saveData` to persist `workoutMuscleGroups`.
- Update `loadData` to restore `workoutMuscleGroups` with default values as fallback.

### Add Workout UI
#### [MODIFY] [AddWorkoutActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddWorkoutActivity.kt)
- Refactor the muscle chip creation logic.
- Remove `android:background` usage on `Chip`.
- Use `setChipBackgroundColor`, `setChipStrokeColor`, and `setChipStrokeWidth` with appropriate `ColorStateList`.
- Ensure `setTextColor` is set correctly for all states.
- Set `chipStartPadding` and `chipEndPadding` for better readability.

## Verification Plan

### Manual Verification
1. Open the "Workout" section.
2. Tap the "+" (Add Workout) button.
3. Verify that muscle group chips (Chest, Back, Legs, etc.) are visible with white text on a dark bordered background.
4. Select a muscle group and verify it highlights correctly.
5. (Bonus) Go to Workout Settings, add a new muscle group, restart the app, and verify it persists and appears in the Add Workout screen.
