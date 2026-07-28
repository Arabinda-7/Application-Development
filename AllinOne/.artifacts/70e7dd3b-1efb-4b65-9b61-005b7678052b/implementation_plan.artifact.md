# Implementation Plan - Edit Project Page Selection UI

Update the Status and Priority selection menus in the Project Page to match the app's "Glass" and "Aura" aesthetic, replacing the standard Android `PopupMenu`.

## User Review Required

> [!IMPORTANT]
> The plan involves replacing the current `PopupMenu` with a custom-styled `AlertDialog` or `BottomSheetDialog` to ensure visual consistency with the rest of the app's UI (glass cards, dark theme, etc.).

## Proposed Changes

### [app/src/main/res/layout]

#### [NEW] [dialog_selector.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/dialog_selector.xml)
- Create a custom layout for selection.
- Use `MaterialCardView` with rounded corners (24dp-28dp) and a dark/transparent background.
- Include a list of options, each styled with icons and text consistent with the app's design.

### [app/src/main/java/com/example/allinone]

#### [MODIFY] [EditProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/EditProjectActivity.kt)
- Update `showStatusMenu()` to use the new custom selection dialog.
- Update `showPriorityMenu()` to use the new custom selection dialog.
- Ensure the selected option updates the UI and state correctly.

## Verification Plan

### Manual Verification
- Deploy the app and navigate to the Edit Project page.
- Tap on the "Status" cell and verify the new selection UI appears and matches the app's style.
- Tap on the "Priority" cell and verify the same.
- Confirm that selecting an option updates the cell text and the underlying project data when saved.
