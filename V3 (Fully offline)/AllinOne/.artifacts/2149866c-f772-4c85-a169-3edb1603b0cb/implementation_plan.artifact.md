# Implementation Plan: Update "Mark Done" Icon in Project Details

This plan updates the icon for the "MARK DONE" option in the project detail menu to a more descriptive checkmark icon.

## Proposed Changes

### app module

#### [MODIFY] [layout_project_detail_menu.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/layout_project_detail_menu.xml)
- Change the `src` of the `ImageView` inside the `menu_detail_mark_done` container.
- **From**: `@drawable/ic_project`
- **To**: `@drawable/icons8_done_100`

## Verification Plan

### Manual Verification
- Launch the app.
- Go to the **Projects** section.
- Open a project to view its details.
- Open the options menu (top right).
- Verify that the "MARK DONE" option now has the new checkmark icon.
