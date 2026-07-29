# Implementation Plan - Separate Add and View Project Layouts

The user pointed out that unifying the "Add Project" and "View Project" layouts caused the viewing experience to change unexpectedly. To resolve this and prevent confusion, I will separate the two layouts into distinct files. The "Add/Edit" screen will use the new chip-style UI, while the "View" screen will use the immersive "Glass Card / Grid" style with the full-screen aura background.

## User Review Required

> [!IMPORTANT]
> - I will create a new layout file `activity_view_project.xml` specifically for the viewing experience.
> - `AddProjectActivity.kt` will dynamically switch between `activity_add_project.xml` and `activity_view_project.xml` based on the `isViewOnly` flag.
> - I will ensure that the "View" screen retains the "Glass Card" aesthetic that fits the "Full Screen Aura" theme.

## Proposed Changes

### New Layout

#### [NEW] [activity_view_project.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_view_project.xml)
- Implement the "View Only" layout using the 2x2 grid (cell_status, cell_priority, cell_theme, cell_deadline).
- Use `glass_card_bg` for the cells.
- Include an `aura_background` view (radial gradient) to provide the immersive "Full Screen Aura" effect.
- Ensure all interactive elements (like the add-goal input) are either removed or hidden for the view-only mode.

### Modified Layout

#### [MODIFY] [activity_add_project.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_add_project.xml)
- Keep the current chip-style UI for "Add/Edit" mode.
- Remove any "View Only" specific logic or hidden views if they are no longer needed here.

### Activity Logic

#### [MODIFY] [AddProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddProjectActivity.kt)
- **`onCreate`**: Use `if (isViewOnly) setContentView(R.layout.activity_view_project) else setContentView(R.layout.activity_add_project)`.
- **`initViews`**: Handle optional view initialization (e.g., `cell_status` only exists in the view layout).
- **`setupLogic`**: Ensure logic for updating `tvGridStatus` and `tvGridPriority` is only executed when those views are present.
- **`updateThemeVisuals`**: Update the aura background color in "View Only" mode to match the project's selected color.

## Verification Plan

### Manual Verification
- **Add Project**: Open the "Add Project" screen and verify it uses the chip-style UI.
- **Edit Project**: Open an existing project in "Edit" mode (e.g., from the menu) and verify it also uses the chip-style UI.
- **View Project**: Tap on a project in the list to view it and verify it uses the "Glass Card" UI with the immersive aura background.
- **Interactions**: Verify that in "View Only" mode, the status/priority cells are visible but not clickable (or show a read-only state).
