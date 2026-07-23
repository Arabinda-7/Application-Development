# Implementation Plan - Context Menus for Workspace Items

The user wants to be able to Edit and Delete workspace items (Goals, Tasks, Bugs, Features, Ideas, Resources) by long-pressing on them.

## Proposed Changes

### 1. Refactor Item UI to Support Long-Press
- [MODIFY] [ProjectWorkspaceScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/ProjectWorkspaceScreen.kt)
    - Add `androidx.compose.foundation.combinedClickable` to item cards/rows.
    - Implement a `DropdownMenu` within each item that triggers on `onLongClick`.
    - Provide "Edit" and "Delete" actions in the menu.

### 2. Connect UI Actions to Repository/ViewModel
- [MODIFY] [ProjectWorkspaceScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/ProjectWorkspaceScreen.kt)
    - **Edit Action**: Sets `activeCreationPage` (e.g., `EditTask`) and `editingEntity` (the actual object) in the `ProjectWorkspaceScreen` state.
    - **Delete Action**: Calls the corresponding delete method in `WorkspaceViewModel` (e.g., `viewModel.deleteTask(task)`).
    - Update all list views (`GoalsTree`, `TasksKanban`, `FeaturePlanner`, `BugTracker`, `IdeaBacklog`, `ResourceDirectory`) to pass these callbacks down to the individual items.

### 3. Support Editing for All Types
- [MODIFY] [ProjectWorkspaceScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/ProjectWorkspaceScreen.kt)
    - Ensure `WorkspaceCreationScreen` correctly pre-fills all fields when an `editingEntity` is provided for any type. (Logic is already partially there from a previous step, but I'll double-check and complete it).

## Verification Plan

### Manual Verification
- **Goals**: Long-press a goal. Verify the menu appears. Edit the goal and verify it saves. Delete the goal and verify it disappears.
- **Tasks**: Long-press a task in the Kanban board. Verify the menu. Edit and Delete.
- **Features/Bugs**: Same as above.
- **Ideas/Resources**: Long-press and verify Edit/Delete functionality.
- **Activity Log**: Verify that deletions are recorded in the activity history.
