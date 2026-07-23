# Implementation Plan - Remove Manage Tags from Project Settings

The user wants to remove the "Manage Tags" option from the Project Settings screen. This involves removing the UI entry in `ProjectSettingsActivity` and its associated dialog logic, as well as updating the `dialog_project_settings.xml` layout.

## User Review Required

> [!IMPORTANT]
> This change will remove the ability for users to customize project tags (e.g., TASKS, NOTES, FEATURES) from the settings menu. The default tags will still be available in the project creation flow unless further changes are requested.

## Proposed Changes

### Project Settings Component

#### [MODIFY] [ProjectSettingsActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProjectSettingsActivity.kt)
- Remove the `ConfigItem` for "Manage Tags" in the `loadSettings()` method.
- Remove the `showManageTagsDialog()` private method.

#### [MODIFY] [dialog_project_settings.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/dialog_project_settings.xml)
- Remove the `LinearLayout` with ID `item_manage_tags` and its preceding comment.

## Verification Plan

### Manual Verification
- Deploy the app and navigate to **Settings > Projects**.
- Verify that the "Manage Tags" option is no longer visible in the list.
- Check if `dialog_project_settings.xml` is used anywhere else (though currently no references were found) to ensure no broken UI if it appears.
