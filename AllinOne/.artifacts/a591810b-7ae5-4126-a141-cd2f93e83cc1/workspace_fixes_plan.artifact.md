# Implementation Plan - Workspace UI Jumps and Data Persistence Fixes

This plan addresses the layout shifting in the Workspace and investigates/fixes potential project data loss issues.

## User Review Required

> [!IMPORTANT]
> **Data Persistence Clarification**: The Workspace data (Projects, Tasks, Bugs, etc.) is stored in a separate Room database (`workspace_database.db`) on your device. It is **not** currently included in the `DataManager` JSON export/import file. If you import a backup file from another device, your Workspace projects will not appear unless you re-import them from the "Projects" section. I will refine the state management to ensure projects don't "disappear" during app usage.

## Proposed Changes

### [Workspace UI]

#### [MODIFY] [ProjectWorkspaceScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/ProjectWorkspaceScreen.kt)

1.  **Fix Layout Jump in `NoProjectsScreen`**:
    *   The top `Box` containing the menu button currently collapses when the menu is opened (because `IconButton` is removed). I will give this `Box` a fixed height to maintain the layout structure.
2.  **Fix Layout Jump in `WorkspaceHeader`**:
    *   Ensure the menu icon area remains the same size when hidden to prevent the header text from shifting.

### [Workspace Logic]

#### [MODIFY] [WorkspaceViewModel.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/WorkspaceViewModel.kt)

1.  **Race Condition Fix**:
    *   Manage the project details collection with a `Job`. Cancel any existing collection before starting a new one in `selectProject`. This prevents multiple projects' data from "fighting" for the UI state.
2.  **Persistence Health Check**:
    *   Ensure `loadProjects()` is robust and doesn't emit an empty state prematurely.

#### [MODIFY] [WorkspaceDatabase.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/data/WorkspaceDatabase.kt)

1.  **Database Versioning**:
    *   Verify the current version and ensure `fallbackToDestructiveMigration()` is used appropriately (or added migrations if possible, though destructive is safer for prototype iterations).

## Verification Plan

### Automated Tests
*   Run the app.
*   Navigate to Workspace.
*   Verify UI stability when toggling the sidebar.
*   Rapidly switch projects to verify state consistency.

### Manual Verification
*   Import a project, close the app, and reopen to confirm it's still there.
*   Verify that "Import Project" correctly shows notes from the main Projects tab.
