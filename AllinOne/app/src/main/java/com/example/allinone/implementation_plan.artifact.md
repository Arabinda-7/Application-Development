# Implementation Plan - Fix Unresolved reference 'showAddIdeaDialog'

This plan outlines the steps to implement the missing `showAddIdeaDialog` method in `ProjectActivity.kt` to resolve the build error.

## User Review Required

> [!IMPORTANT]
> The `showAddIdeaDialog` method was missing its definition in `ProjectActivity.kt`. I will implement it using the existing `dialog_add_note_project.xml` layout, which is designed for "Idea" management.

## Proposed Changes

### [Component Name]

#### [MODIFY] [ProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProjectActivity.kt)
- Implement `showAddIdeaDialog(existingNote: Note? = null)` method.
- Use `dialog_add_note_project.xml` to inflate the dialog.
- Initialize UI components:
    - `note_title_input` (EditText)
    - `note_content_input` (EditText)
    - `btn_save_note` (TextView)
    - `btn_close_note` (ImageView)
    - `btn_mind_map` (ImageButton)
    - `btn_priority_tag` (TextView)
- Handle "New" vs "Edit" logic:
    - If `existingNote` is provided, pre-fill the inputs.
    - If not, create a new `Note` object with `category = "ProjectIdea"`.
- Implement save logic:
    - Update the `Note` object with input values.
    - If it's a new note, add it to `DataManager.notes`.
    - Save data using `DataManager.saveData(this)`.
    - Refresh the UI using `updateDisplayList()`.
- (Optional) Hook up Journal and Sub-feature containers if needed for a complete experience.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify the build error is resolved.

### Manual Verification
- Deploy the app.
- Go to the "Projects" section and switch to the "Ideas" tab.
- Tap the "+" button and verify the "Add Idea" dialog opens.
- Enter a title and content, then save.
- Verify the new idea appears in the list.
- Long-press an idea and select "Edit" to verify the dialog opens with pre-filled data.
