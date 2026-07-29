# Implementation Plan - Synchronize Workspace UI Styling

Synchronize the titles and font sizes across different workspace sections (Ideas, Goals, Tasks, Features, Bugs, Resources) to match the styling of the **Projects** and **Notes** sections.

## Proposed Changes

### 1. Item Titles Synchronization (List/Grid Views)
Unify the styling of item titles in all sections to match the "Projects" section (`20.sp`, `FontWeight.Bold`, no index prefix).

#### [MODIFY] [IdeasSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/IdeasSection.kt)
- Remove the `${index + 1}. ` prefix from the title in `IdeaViewSection`.

#### [MODIFY] [NotesSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/NotesSection.kt)
- Update `NoteViewSection` title to `20.sp` and `FontWeight.Bold`.

#### [MODIFY] [GoalsSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/GoalsSection.kt)
- Remove the `${index + 1}. ` prefix from the title in `GoalViewSection`.
- Ensure title font size is `20.sp` (already is).

#### [MODIFY] [TasksSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/TasksSection.kt)
- Update `TaskItemUI` title to `18.sp` (using slightly smaller than 20 for density) and remove the index prefix.

#### [MODIFY] [CommonWorkspaceComponents.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/CommonWorkspaceComponents.kt)
- Update `FeatureItemCard` and `BugItemCard` titles to `18.sp` and remove index prefix.

#### [MODIFY] [ResourcesSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/ResourcesSection.kt)
- Update `ResourceViewSection` title to `20.sp`.

---

### 2. Section Header Synchronization
Unify the section headers in the main workspace view to match the dashboard sub-headers.

#### [MODIFY] [ProjectWorkspaceScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/ProjectWorkspaceScreen.kt)
- Increase `currentTab.title` font size from `11.sp` to `18.sp`.
- Update `FontWeight` to `FontWeight.Black` (consistent with dashboard).
- Update color to `Color.White` or `style.accentColor` (consistent with dashboard).

## Verification Plan

### Automated Tests
- N/A (UI layout changes)

### Manual Verification
- Deploy the app and navigate through all Workspace tabs (Dashboard, Goals, Tasks, Notes, Features, Bugs, Ideas, Resources).
- Verify that the section titles at the top are consistent.
- Verify that item titles in the lists are consistent in size (`20.sp` or `18.sp`) and formatting (no index prefix).
- Ensure no layout regressions in dense views like Tasks or Notes grid.
