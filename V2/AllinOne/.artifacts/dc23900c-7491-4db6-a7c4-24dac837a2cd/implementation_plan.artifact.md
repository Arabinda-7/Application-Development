# Implementation Plan - Redesign Project View Section

The goal is to update the `AddProjectActivity` (and its associated layout) to match the provided reference image for the "Project View" section. This includes a more stylized meta information grid, improved section headers, and a footer with creation/update dates.

## User Review Required

> [!IMPORTANT]
> The redesign will significantly change the layout of the project details screen.
> The "Edit" mode will still use the same layout but with enabled controls.

## Proposed Changes

### [Component Name]

#### [MODIFY] [activity_add_project.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_add_project.xml)
- Update background color to a deep navy blue.
- Redesign `layout_add_selectors` into a 2x2 grid for STATUS, PRIORITY, THEME, and DEADLINE.
- Ensure section headers (Description, Goals, Sub-features) match the bold style with arrows.
- Add a footer `TextView` at the bottom for "Created | Updated" dates.

#### [MODIFY] [AddProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddProjectActivity.kt)
- Update `initViews` to bind new grid elements.
- Update `setupLogic` to populate the new grid.
- Implement click listeners for the grid items to allow editing when not in view-only mode.
- Populate the footer with the project's creation and update timestamps.
- Refine the Sub-features list styling to match the reference.

#### [MODIFY] [Note.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/Note.kt)
- Add `updatedAt` field to the `Note` class to track the last modification time.

## Verification Plan

### Automated Tests
- None (UI changes).

### Manual Verification
- Open an existing project and verify the new layout matches the reference image.
- Toggle between sections and verify expand/collapse logic.
- Verify that clicking on grid items (in edit mode) opens the appropriate pickers.
- Verify the "Created | Updated" footer displays correct dates.
