# Walkthrough: Updated "Mark Done" Icon in Project Details

I have updated the icon for the "MARK DONE" option in the project detail menu to make it more intuitive.

## Changes

### app module

#### [layout_project_detail_menu.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/layout_project_detail_menu.xml)
- Changed the icon for the "MARK DONE" menu item.
- **Old Icon**: `@drawable/ic_project` (standard project icon)
- **New Icon**: `@drawable/icons8_done_100` (checkmark icon)

## Verification Results

### Manual Verification
- Launch the app.
- Go to the **Projects** section.
- Open a project to view its details.
- Open the options menu (top right).
- [x] Verified that the "MARK DONE" option now features the checkmark icon.
