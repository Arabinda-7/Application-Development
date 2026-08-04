# Implementation Plan - Fix Missing App Settings Features

The user reports that no features are showing in the app settings screen. This investigation identified that the `RecyclerView` in `SettingsActivity` is not being initialized with a `LayoutManager`, which prevents it from rendering any items. Additionally, a `setup()` method in `SettingsHubSection` that handles initialization is never called.

## Proposed Changes

### [Component: Settings]

#### [MODIFY] [SettingsActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/SettingsActivity.kt)
- Initialize `LinearLayoutManager` for the `settings_list` RecyclerView in `onCreate`.
- Call `hubSection.setup()` instead of `hubSection.showHub()` when the current path is "HUB" to ensure proper initialization of the hub section.
- Ensure `showSectionSettings` also works correctly with the initialized `LayoutManager`.

#### [MODIFY] [SettingsHubSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/SettingsHubSection.kt)
- Update `setup()` to ensure it only initializes once if called multiple times, or simply rely on `SettingsActivity` to manage the lifecycle. (Actually, calling it once from `SettingsActivity` is enough).

## Verification Plan

### Automated Tests
- N/A (UI logic issue)

### Manual Verification
1.  Launch the app.
2.  Navigate to App Settings.
3.  Verify that the main settings menu (HUB) is visible with all features (Habit Tracker, Workout, To-Do List, etc.).
4.  Navigate into a sub-section (e.g., "Appearance Settings") and verify items are visible.
5.  Navigate back to the HUB and verify it reappears correctly.
