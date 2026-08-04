# Implementation Plan - Customizable Home Page Sections

Add a feature to enable or disable the 6 main sections on the home page (Habits, Workouts, Tasks, Notes, Projects, Finance) through the app settings.

## User Review Required

> [!IMPORTANT]
> Disabling a section will hide its card from the Home Page. If both sections in a category (e.g., "Growth & Discipline") are disabled, the category title will also be hidden.

## Proposed Changes

### Data Management

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Add boolean flags for each section: `showHabitSection`, `showWorkoutSection`, `showTaskSection`, `showNoteSection`, `showProjectSection`, `showFinanceSection`.
- Initialize all to `true`.
- Update `saveData` and `loadData` to persist these settings using SharedPreferences.

#### [MODIFY] [DashboardState.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/DashboardState.kt)
- Add corresponding boolean flags to the `DashboardState` data class.

### Main Dashboard

#### [MODIFY] [MainActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/MainActivity.kt)
- Update `refreshState()` to pass the section visibility flags from `DataManager` to the `DashboardState`.

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/HomeScreen.kt)
- Use `androidx.compose.animation.AnimatedVisibility` (or simple `if` conditions) to show/hide section cards based on the flags in `DashboardState`.
- Update `DashboardPair` or its usage to handle cases where only one item in a pair is visible (ensuring it takes the appropriate width).
- Hide category headers (e.g., "Growth & Discipline") if no sections under them are visible.

### Settings

#### [MODIFY] [SettingsActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/SettingsActivity.kt)
- Add 6 new toggle items under the "OTHERS" section settings to allow users to enable/disable each home page section.

## Verification Plan

### Manual Verification
1.  Navigate to **Settings > Others**.
2.  Toggle off "Habit Tracker" and return to the Home Page.
3.  Verify that the Habit card is hidden and the Workout card takes up appropriate space (or is correctly aligned).
4.  Toggle off both "Habit Tracker" and "Workout Routine".
5.  Verify that the "Growth & Discipline" header is also hidden.
6.  Toggle all sections back on and verify they reappear correctly.
7.  Restart the app to ensure settings are persisted.
