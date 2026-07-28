# Implementation Plan - Project Page Refinement

The user wants to refine the project management experience by:
1. Removing the delete icon from the project edit page.
2. Showing project templates in both project and idea addition pages.

## Proposed Changes

### Data Management
#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Update `loadData` to ensure `projectTemplates` are not overwritten with an empty map if no templates are saved in SharedPreferences. This ensures default templates are visible to the user.

### Project Roadmap (Add/Edit)
#### [MODIFY] [AddProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddProjectActivity.kt)
- Remove the line that makes `btnDelete` visible when editing an existing project.
- Modify the template display logic to show templates even when editing an existing project (allowing users to add template steps to an existing roadmap).

### Idea Brainstorming (Add/Edit)
#### [MODIFY] [activity_add_idea.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_add_idea.xml)
- Add a new "TEMPLATES" section with a horizontal scroll container to match the style of the Project Roadmap page.

#### [MODIFY] [AddIdeaActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddIdeaActivity.kt)
- Initialize the new template container views.
- Implement the template population logic, allowing users to quickly add common roadmap steps to their ideas.

## Verification Plan

### Automated Tests
- Run the app and navigate to "Project Roadmaps".
- Verify that the "Delete" (trash) icon is NOT visible when editing an existing project.
- Verify that "TEMPLATES" are visible when adding a new project.
- Verify that "TEMPLATES" are visible when adding/editing an idea.
- Verify that clicking a template correctly populates the sub-features/steps.

### Manual Verification
- Check `ProjectSettingsActivity` to see if templates can still be managed and if they appear correctly after being added there.
- Ensure the layout of `AddIdeaActivity` remains clean and consistent with the project page.
