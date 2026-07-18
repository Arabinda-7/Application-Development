# Implementation Plan - Universal Icon Expansion

This plan describes how to integrate the full suite of newly project-added `icons8` and Material icons into the selection dialogs across the entire application.

## Proposed Changes

### 1. Habit Icon Selection
Expand the icon picker in the Habit creation screen to include a wider range of lifestyle and productivity icons.

- **[MODIFY] [AddHabitActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddHabitActivity.kt)**:
    - Update `showIconSelectionDialog` to include icons for: editing, trash, checkmarks, folders, files, and more.

### 2. Workout Icon Selection
Enrich the workout icon picker with all newly identifying exercise and health-related graphics.

- **[MODIFY] [AddWorkoutActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddWorkoutActivity.kt)**:
    - Update `showIconSelectionDialog` to include the latest fitness icons identifying specific movements and equipment.

### 3. Global Appearance Icons
Update the master icon management settings to allow users to select from the complete project icon library for their main dashboard sections.

- **[MODIFY] [SettingsActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/SettingsActivity.kt)**:
    - Update `showIconPickerDialog` to feature a comprehensive grid of all identified project icons.

## Verification Plan

### Manual Verification
1.  **Habit Creation**: Tapping "Choose Icon" in Add Habit should reveal a significantly longer list including the new Pencil, Trash, and Checkmark icons.
2.  **Workout Creation**: Tapping "Choose Icon" in Add Workout should show a full grid of various exercise movements.
3.  **Appearance Settings**: Navigating to Appearance > Icons and changing a section icon should offer the full range of icons available in the project.
4.  **Visual Check**: Verify that all icons render clearly and respect the dynamic tinting applied in the dialogs.
