# Implementation Plan - Remove Formatting and Voice Icons from Ideas Section

Remove the formatting (bullet list, numeric list) and voice input icons from the "Add Idea" dialog to simplify the UI as requested.

## Proposed Changes

### [Project Section: UI Cleanup]

#### [MODIFY] [dialog_add_note_project.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/dialog_add_note_project.xml)
- Remove `btn_bullet_list` ImageButton.
- Remove `btn_numeric_list` ImageButton.
- Remove the vertical divider View (with background `#44FFFFFF`).
- Remove `btn_voice_input` ImageButton.

#### [MODIFY] [ProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProjectActivity.kt)
- Remove unused variable declarations in `showAddIdeaDialog`:
    - `btnBullet`
    - `btnNumeric`
    - `btnVoice`

---

## Verification Plan

### Manual Verification
1. **Open Project Idea Editor**: Navigate to the "Ideas" tab in Projects and tap "+" or an existing idea.
2. **Verify UI**: Ensure the toolbar area above the content input no longer shows the list and microphone icons.
3. **Verify Functionality**: Ensure saving and editing ideas still works perfectly.
