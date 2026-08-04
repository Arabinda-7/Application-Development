# Implementation Plan - Consolidate Home Visibility Settings

Consolidate individual home section visibility toggles into a single "Home Page Sections" setting that opens a dialog with all options.

## User Review Required

> [!IMPORTANT]
> The "OTHERS" settings section will be simplified. Instead of seeing multiple toggles for each home section (Habits, Workouts, etc.), the user will see a single entry that opens a customization dialog.

## Proposed Changes

### Settings Activity

#### [MODIFY] [SettingsActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/SettingsActivity.kt)
- Add `showHomeVisibilityDialog()` method:
    - Inflates `R.layout.dialog_manage_sections`.
    - Dynamically adds `SwitchCompat` for Habits, Workouts, Tasks, Notes, Projects, and Finance.
    - Saves state to `DataManager` on "APPLY CHANGES".
- Update `showSectionSettings("OTHERS")`:
    - Replace individual visibility `ConfigItem`s with a single `ConfigItem("Home Page Sections", "Customize dashboard visibility")`.

## Verification Plan

### Manual Verification
- Deploy the app.
- Navigate to "Settings" -> "Others".
- Verify that individual "Show X" toggles are gone and replaced by "Home Page Sections".
- Click "Home Page Sections" and verify the dialog opens with all toggles.
- Toggle some sections, click "APPLY CHANGES", and verify that the home screen reflects these changes (or check `DataManager` state).
