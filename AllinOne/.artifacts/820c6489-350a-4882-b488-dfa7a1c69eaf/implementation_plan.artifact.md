# Implementation Plan - Increase Edit Icon Size in Workspace

The user wants to make the edit icon in the Workspace Dashboard more visible by increasing its size.

## Proposed Changes

I will increase the size of the edit icon (pencil icon) in the Workspace header and in the detail screens for various sections (Goals, Tasks, Features, Bugs, etc.).

### [Workspace UI]

#### [MODIFY] [ProjectWorkspaceScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/ProjectWorkspaceScreen.kt)
- Increase the edit icon size in the `WorkspaceHeader` from `14.dp` to `20.dp`.
- Increase the `IconButton` size for the edit action from `28.dp` to `40.dp` to accommodate the larger icon and provide a better touch target.

#### [MODIFY] [GoalsSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/GoalsSection.kt)
- Increase the edit icon size in `GoalDetailSection` by adding `modifier = Modifier.size(28.dp)`.

#### [MODIFY] [TasksSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/TasksSection.kt)
- Increase the edit icon size in `TaskDetailSection` by adding `modifier = Modifier.size(28.dp)`.

#### [MODIFY] [FeaturesSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/FeaturesSection.kt)
- Increase the edit icon size in `FeatureDetailSection` by adding `modifier = Modifier.size(28.dp)`.

#### [MODIFY] [BugsSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/BugsSection.kt)
- Increase the edit icon size in `BugDetailSection` by adding `modifier = Modifier.size(28.dp)`.

#### [MODIFY] [NotesSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/NotesSection.kt)
- Increase the edit icon size in `NoteDetailSection` by adding `modifier = Modifier.size(28.dp)`.

#### [MODIFY] [IdeasSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/IdeasSection.kt)
- Increase the edit icon size in `IdeaDetailSection` by adding `modifier = Modifier.size(28.dp)`.

#### [MODIFY] [ResourcesSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/ResourcesSection.kt)
- Increase the edit icon size in `ResourceDetailSection` by adding `modifier = Modifier.size(28.dp)`.

## Verification Plan

### Manual Verification
1.  Open Workspace and ensure a project is selected.
2.  On the Dashboard tab, verify the edit icon in the top right is larger and clearly visible.
3.  Open the detail screen for any Goal, Task, Feature, etc.
4.  Verify the edit icon in the top right of the detail screen is larger.
