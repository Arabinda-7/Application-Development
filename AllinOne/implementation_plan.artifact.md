# Implementation Plan - Unify Project and Idea Title Sizes

The user wants to adjust the font size of project and idea titles to match the "title of different section". Based on the project structure, main activity titles (like "PROJECTS", "TASKS", "HABITS") are consistently set to **20sp**. Currently, project and idea titles in list items are set to **16sp** (XML) or **15sp** (Compose).

This plan will update these titles to **20sp** for consistency across the app.

## User Review Required

> [!IMPORTANT]
> I have assumed that "title of different section" refers to the main activity headers (e.g., "PROJECTS", "TASKS") which are **20sp**. If you meant a different section (like the 10sp headers on the Home Screen or the 28sp titles in edit screens), please let me know.

## Proposed Changes

### [UI Components: Project & Idea Titles]

#### [MODIFY] [project_note_item.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/project_note_item.xml)
- Update `tv_note_title` `android:textSize` from `16sp` to `20sp`.

#### [MODIFY] [note_list_item.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/note_list_item.xml)
- Update `note_title` `android:textSize` from `16sp` to `20sp`.

#### [MODIFY] [ProjectWorkspaceScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/ProjectWorkspaceScreen.kt)
- Update `ProjectOverviewItem` project name `fontSize` from `15.sp` to `20.sp`.
- Update `IdeaBacklog` idea title `fontSize` to `20.sp` (it currently uses default).
- Update `GoalsTree` goal title `fontSize` from `15.sp` to `20.sp` (for consistency with projects/ideas).

## Verification Plan

### Manual Verification
1. Open the **Projects** activity and verify the titles in the list items match the "PROJECTS" header at the top.
2. Open the **Ideas** tab and verify the titles match.
3. Open the **Project Workspace** and verify the project overview and idea backlog titles are larger (20sp).
