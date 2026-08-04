# Implementation Plan: Detailed Agenda Info and Navigation Paths

The user wants the "Today's Agenda" notifications to show more detailed information (descriptions, priorities) and a clear "path" to where the item belongs in the app (e.g., "Workspace > AllInOne > MVP").

## User Review Required

> [!IMPORTANT]
> I will introduce an `AgendaItem` data class to hold rich metadata for each notification. I will also update the UI to make these items clickable, allowing for direct navigation to the relevant section.

## Proposed Changes

### [Data Models]

#### [NEW] [AgendaItem.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AgendaItem.kt)
- Define `AgendaItem` with fields: `title`, `details`, `path`, `category`, and `navigationTarget`.

#### [MODIFY] [DashboardState.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DashboardState.kt)
- Update `todayAgenda` to be `Map<String, List<AgendaItem>>`.

### [Core Logic]

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Update `workspaceTodayAgenda` type to `Map<String, List<AgendaItem>>`.
- Update `updateWorkspaceAgenda` to fetch project names and create `AgendaItem`s with full paths.
- Update `getTodayAgendaNotifications` to include details (priority for tasks, progress for projects) and paths for all items.
- Update `scanFeaturesForAgenda` to include the parent project name in the path.

### [UI Components]

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/HomeScreen.kt)
- Update `NotificationItem` (or create a new one) to display the title, details, and the breadcrumb path.
- Make agenda items clickable to navigate to their respective sections (Tasks, Projects, Workspace).

## Verification Plan

### Manual Verification
- Add a task with high priority and a reminder. Verify the priority and "Tasks > General" path appear in the agenda.
- Add a roadmap item in a project. Verify it shows "Projects > [Project Name] > Roadmap" as the path.
- Add a task in the Workspace. Verify it shows "Workspace > [Project Name] > Tasks" as the path.
- Click an item in the agenda and verify it navigates to the correct screen.
