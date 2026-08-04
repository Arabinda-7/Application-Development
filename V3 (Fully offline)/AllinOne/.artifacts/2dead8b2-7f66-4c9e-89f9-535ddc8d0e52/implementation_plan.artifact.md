# Implementation Plan - Force Dark Theme by Default

The user wants the app to be in Dark Theme by default, even if the system setting is not dark, while keeping the "Follow System Settings" feature (which currently handles both scaling and theme).

## Proposed Changes

We will decouple the Theme Mode from the "Follow System Settings" toggle. "Follow System Settings" will now specifically refer to syncing Display and Font size with the system, as per its description. The Theme Mode will be independently selectable and will default to "DARK".

### 1. [MODIFY] [BaseActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/BaseActivity.kt)
- Update `applyAppTheme()` to always use `DataManager.appThemeMode` for setting the night mode, regardless of `isSystemAppearanceEnabled`.
- Handle "SYSTEM" mode if we add it, or just use the current modes.

### 2. [MODIFY] [UIUtils.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/UIUtils.kt)
- Update `wrapContext()` to apply the `appThemeMode` override independently of `isSystemAppearanceEnabled`.

### 3. [MODIFY] [SettingsActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/SettingsActivity.kt)
- Move "Theme Mode", "Accent Color", "Border Radius", "Card Style", "Font Family", and "Show Shadows" out of the conditional block that depends on `isSystemAppearanceEnabled`.
- They will now always be visible and configurable.
- Update "Follow System Settings" description if necessary (though it already mentions display and font size).

### 4. [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Ensure default `appThemeMode` is `"DARK"`.
- Update comments to reflect that `isSystemAppearanceEnabled` is for scaling.

## Verification Plan

### Manual Verification
- Launch the app on a device with System Theme set to LIGHT. Verify the app is in DARK mode by default.
- Go to Settings -> Others.
- Turn ON "Follow System Settings". Verify the app remains in DARK mode.
- Change "Theme Mode" to "LIGHT". Verify the app changes to LIGHT mode.
- Change "Theme Mode" back to "DARK" or "OLED". Verify it works as expected.
- Verify that "Follow System Settings" still affects scaling (if possible to test by changing system display/font size).
