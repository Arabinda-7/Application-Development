# Implementation Plan - Redesign Workspace Activity Section

This plan aims to redesign the "Activity" section of the Workspace to match the timeline-based "Project History" UI.

## Proposed Changes

### [Workspace Component]

#### [MODIFY] [ProjectWorkspaceScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/ProjectWorkspaceScreen.kt)
- **Redesign `ActivityLogView`**:
    - Use a `LazyColumn` to display logs.
    - Sort logs by descending timestamp.
    - Create a new `@Composable` function `ActivityLogItem` that replicates the `item_project_history.xml` layout:
        - Left side: A vertical timeline line and a circular dot.
        - Right side: Column containing Action (bold), Description, and Formatted Timestamp.
    - Use `SimpleDateFormat("MMM dd, h:mm a")` for the timestamp formatting.
    - Ensure the vertical line connects between items (except for the last one).

## Verification Plan

### Manual Verification
1. Open the Workspace section.
2. Navigate to the "Activity" tab.
3. Verify that the logs are displayed in a timeline format.
4. Check that the vertical lines and dots align correctly.
5. Verify that the most recent activities are at the top.
6. Perform some actions in the workspace (e.g., add a task) and verify they appear in the new activity timeline.
