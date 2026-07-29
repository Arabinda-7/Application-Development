# Implementation Plan - Project Section Refinement

The user wants to refine the Project section by fixing titles, implementing a missing view mode, updating progress calculations, and removing delete icons.

## User Review Required

> [!IMPORTANT]
> - **View Section**: I am re-implementing the `showProjectDetailsDialog` which seems to have been lost or was missing. This will provide a full-screen view mode for projects.
> - **Progress Calculation**: Progress will now be weighted based on sub-feature priority: High (5pt), Mid (2pt), Low (1pt). The manual progress bar in the edit screen will be hidden.

## Proposed Changes

### Project Activity (View Mode & Titles)

#### [MODIFY] [ProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProjectActivity.kt)
- Update `updateTabUI` to set the title to "IDEAS" or "PROJECTS" based on the selected tab.
- Implement `showProjectDetailsDialog(note: Note)` to show a detailed view of the project.
- Implement `refreshDetailsSubFeatures` and `createSubFeatureViewItem` helpers.
- Call `showProjectDetailsDialog` from `onProjectItemClick` when not in edit mode.

### Add Project Screen (Progress & Delete Icon)

#### [MODIFY] [AddProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddProjectActivity.kt)
- Update `updateProjectProgress()` to use the weighted priority points system.
- Remove code that makes `btnDelete` visible when editing.

#### [MODIFY] [activity_add_project.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_add_project.xml)
- Hide the `container_progress_input` (SeekBar and its label).

### Add Idea Screen (Delete Icon)

#### [MODIFY] [AddIdeaActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddIdeaActivity.kt)
- Remove code that makes `btnDelete` visible when editing.

## Verification Plan

### Manual Verification
- Verify title changes when switching tabs in `ProjectActivity`.
- Verify clicking a project opens the detail dialog.
- Verify progress calculation in `AddProjectActivity` by adding sub-features with different priorities.
- Verify the trash icon is removed from the toolbar in edit screens.
