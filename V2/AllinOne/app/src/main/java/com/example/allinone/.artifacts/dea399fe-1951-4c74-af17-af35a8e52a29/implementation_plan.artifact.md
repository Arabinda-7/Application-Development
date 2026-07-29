# Implementation Plan - Project View, History & Settings Fixes

This plan addresses UI refinements for the Project View mode, upgrades the history view to a full-page experience, restricts editing in View Mode, and fixes a crash in Project Settings.

## User Review Required

> [!IMPORTANT]
> - **Full-Page History**: Project history will now be a dedicated activity `ProjectHistoryActivity` with a design matching the Workspace and Workout sections.
> - **View Mode Restrictions**: Editing and deleting (goals/sub-features) will be completely disabled in View Mode. Only completion status can be toggled via long-press.
> - **Crash Fix**: The crash in Project Settings is caused by a missing view ID in the template management dialog layout.

## Proposed Changes

### 1. View Mode Enhancements

#### [MODIFY] [AddProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddProjectActivity.kt)
- Update `setupLogic()` to apply a large immersive aura background when `isViewOnly` is true.
- Hide goal delete buttons and sub-feature edit icons in View Mode.
- Disable direct editing of existing sub-features/goals (Read-only).

#### [MODIFY] [activity_add_project.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_add_project.xml)
- Add an `aura_background` view behind content for the dynamic coloring effect.

### 2. Full-Page Project History

#### [NEW] [ProjectHistoryActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProjectHistoryActivity.kt)
- Dedicated activity to show statistics (Progress, Features, Actions) and a scrollable timeline of project history.

#### [NEW] [activity_project_history.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_project_history.xml)
- Full-page layout with immersive header, stats cards, and a history RecyclerView.

#### [MODIFY] [ProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProjectActivity.kt)
- Redirect history clicks to `ProjectHistoryActivity`.

### 3. Crash Fix (Project Settings)

#### [MODIFY] [dialog_manage_categories_project.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/dialog_manage_categories_project.xml)
- Add the missing `btn_toggle_delete_mode` ImageButton to fix the NullPointerException in `ProjectSettingsActivity`.

## Verification Plan

### Manual Verification
- **View Mode**: Open a project and verify the new aura background and restricted interactions.
- **History**: Verify that clicking history on a project card opens a full-page view with correct stats.
- **Settings**: Verify that opening Project Settings and managing templates no longer crashes the app.
