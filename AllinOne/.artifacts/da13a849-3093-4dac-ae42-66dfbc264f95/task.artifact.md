# Tasks - Project Access and Workspace UI Synchronization

- [x] Update Workspace Entities with Deadline field
    - [x] Add `deadline` to `FeatureEntity` in `Entities.kt`
    - [x] Add `deadline` to `BugEntity` in `Entities.kt`
- [x] Update `WorkspaceViewModel.kt` to support deadlines for Features and Bugs
- [x] Implement Red Deadline display in `CommonWorkspaceComponents.kt`
- [x] Sync Workspace Card Styling (Colored Titles)
    - [x] Update `GoalViewSection` title color
    - [x] Update `NoteViewSection` title color
    - [x] Update `IdeaViewSection` title color
    - [x] Update `ResourceViewSection` title color
- [x] Add Deadline support and display to all Workspace sections
    - [x] `GoalsSection.kt` (Add picker and display)
    - [x] `TasksSection.kt` (Display red deadline)
    - [x] `FeaturesSection.kt` (Add picker and display)
    - [x] `BugsSection.kt` (Add picker and display)
- [x] Restrict Workspace access in `HomeScreen.kt` when no projects exist
- [x] Verify changes
