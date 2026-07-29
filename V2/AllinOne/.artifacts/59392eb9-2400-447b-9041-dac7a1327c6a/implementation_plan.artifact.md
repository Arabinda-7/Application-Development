# Implementation Plan - Fix Settings Title Glitch

This plan addresses the UI glitch where the section title persists when navigating back to the main Settings page.

## User Review Required

> [!NOTE]
> The fix involves centralizing the logic for showing the main settings hub in `SettingsActivity` to ensure all UI elements (including the title) are correctly reset.

## Proposed Changes

### [Settings]

#### [MODIFY] [SettingsActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App/Development/AllinOne/app/src/main/java/com/example/allinone/SettingsActivity.kt)
- Add a `showHub()` private method to handle resetting the UI to the main hub state.
- Update `handleBackNavigation()` to call `showHub()`.
- Update `onCreate()` to ensure the title is correct when starting at the HUB.

## Verification Plan

### Manual Verification
1.  Open Settings.
2.  Navigate to a section (e.g., "Lock & Security").
3.  Observe the title change to "SECURITY".
4.  Press back button or the back icon.
5.  Observe the title change back to "APP SETTINGS".
6.  Repeat for other sections like "Others" or "Help & Guide".
7.  Navigate to "Appearance Settings" -> "Section Icons".
8.  Go back from "Section Icons" to "Appearance Settings". Title should be "APPEARANCE".
9.  Go back from "Appearance Settings" to main Hub. Title should be "APP SETTINGS".
