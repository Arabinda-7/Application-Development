# Implementation Plan - Modernize Dropdown Menus (App UI Matching)

Style the Workspace context menus (long-press options) to perfectly match the app's aesthetic (Glass/Dark theme with custom accents).

## Proposed Changes

### Workspace UI Sections

#### [MODIFY] [CommonWorkspaceComponents.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/CommonWorkspaceComponents.kt)
- Create a `StyledDropdownMenu` and `StyledDropdownMenuItem` wrapper (or just apply styles to existing ones).
- Style will include:
    - **Background**: `style.surfaceColor` (Dark/Glass).
    - **Shape**: `RoundedCornerShape(style.borderRadius)`.
    - **Border**: Subtle border matching the app's card style.
    - **Text Color**: White with appropriate icons.
    - **Divider**: Consistent with the app's separators.

#### [MODIFY] Update all Section Files
Update the following files to use the new styled menus:
- [TasksSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/TasksSection.kt)
- [BugsSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/BugsSection.kt)
- [GoalsSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/GoalsSection.kt)
- [IdeasSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/IdeasSection.kt)
- [NotesSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/NotesSection.kt)
- [ResourcesSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/sections/ResourcesSection.kt)

#### [MODIFY] [ProjectWorkspaceScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/ProjectWorkspaceScreen.kt)
- Style the `WorkspaceHeader` project selection dropdown.

## Verification Plan

### Manual Verification
1. Long-press any Workspace item.
2. Verify the menu background matches the app's surface color.
3. Verify the corners are rounded according to `AppStyle`.
4. Verify icons and text are correctly colored and aligned.
