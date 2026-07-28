# Implementation Plan - Project UI Tweaks (Separate Activities)

This plan addresses layout and visibility refinements for the separate project activities (View, Add, Edit).

## Proposed Changes

### [Layouts]

#### [MODIFY] [activity_edit_project.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_edit_project.xml)
- Hide the "Overall Progress" slider (`container_progress_input`).
- Set the Description input (`note_content_input`) to `GONE` by default.
- Set the Goals container (`container_goals`) to `GONE` by default.
- Ensure chevrons for these sections are initialized to "down".

#### [MODIFY] [activity_view_project.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_view_project.xml)
- Set the Description text (`note_content_input`) to `GONE` by default.
- Ensure description chevron is initialized to "down".

---

### [Activities Logic]

#### [MODIFY] [EditProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/EditProjectActivity.kt)
- Implement toggle logic for Description, Project Goals, and Sub-Features.
- Ensure the "Add Sub-feature" input section is functional and correctly displays the list.
- Handle chevron icon updates on expand/collapse.

#### [MODIFY] [AddProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddProjectActivity.kt)
- Implement toggle logic for Description, Project Goals, and Sub-Features.
- Ensure Sub-features and Goals sections are fully integrated with the UI (they were omitted in the initial split).

#### [MODIFY] [ViewProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ViewProjectActivity.kt)
- Update initial UI state to ensure Description is collapsed.
- Ensure chevrons are correctly initialized.

## Verification Plan

### Manual Verification
- **Add Project**: Verify Description and Goals are visible (new projects should probably be expanded by default to prompt input, or as requested).
- **Edit Project**: Verify Progress bar is hidden. Verify Description and Goals are collapsed by default. Verify Sub-features can be added and seen.
- **View Project**: Verify Description is collapsed by default.
