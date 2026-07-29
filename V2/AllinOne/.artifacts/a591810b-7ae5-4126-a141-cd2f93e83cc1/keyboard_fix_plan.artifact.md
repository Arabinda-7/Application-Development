# Implementation Plan - Fix Keyboard Overlapping Input Fields

Ensure that the UI remains visible and scrollable when the keyboard is open, especially for inputs located at the bottom of the screen in the Project Workspace and Ideas sections.

## Proposed Changes

### Project Workspace (Compose)
#### [MODIFY] [ProjectWorkspaceScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/ProjectWorkspaceScreen.kt)
- **Add `imePadding()`**: Apply `Modifier.imePadding()` to the root `Column` of `WorkspaceCreationScreen`. This ensures that the bottom of the scrollable content is lifted above the keyboard.
- **Add `navigationBarsPadding()`**: Ensure navigation bar area is also respected for a clean edge-to-edge experience.

### Ideas Section (XML/Views)
#### [MODIFY] [activity_add_idea.xml](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/res/layout/activity_add_idea.xml)
- **Bottom Content Spacing**: Add a `paddingBottom` of `32dp` to the inner `LinearLayout` within the `NestedScrollView`. This provides extra space to ensure that the last input field and "Created on" text are fully visible above the keyboard when focused.

## Verification Plan

### Manual Verification
1.  **Workspace Creation**:
    - Open the Workspace and select "Add Bug" or "Add Resource".
    - Tap the bottom-most text field (e.g., "Steps to Reproduce" or "URL").
    - Verify that the keyboard opens and the screen automatically scrolls to keep the input field visible.
2.  **Ideas Section**:
    - Open "Add Idea".
    - Scroll to the bottom and tap "Add a feature...".
    - Verify that the input remains visible above the keyboard.
