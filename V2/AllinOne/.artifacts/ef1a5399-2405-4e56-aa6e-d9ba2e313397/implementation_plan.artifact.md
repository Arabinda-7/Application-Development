# Implementation Plan - Fix Unresolved References in NoteAdapter and ProjectNoteAdapter

The project is failing to compile due to missing methods in `ProjectActivity` and `NotesActivity` that are being called by their respective adapters.

## User Review Required

> [!IMPORTANT]
> I will be adding missing UI methods to `ProjectActivity` and `NotesActivity` to handle project/note management (menus and history). These methods will use existing layouts (`layout_custom_menu.xml`, `dialog_project_history.xml`) to maintain consistency with the app's design.

## Proposed Changes

### [Core Activity Fixes]

#### [MODIFY] [ProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/ProjectActivity.kt)
- Implement `showProjectMenu(anchor: View, note: Note)` to show a custom popup menu for project items.
- Implement `showProjectHistoryDialog(note: Note)` to show the project's activity history.
- Add necessary imports for `PopupWindow`, `LayoutInflater`, `ViewGroup`, etc.

#### [MODIFY] [NotesActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/NotesActivity.kt)
- Implement `showEditNoteDialog(note: Note)` to fix the unresolved reference in `NoteAdapter`.
- This method will simply launch `AddNoteActivity` with the correct extras.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify that the unresolved reference errors are resolved.

### Manual Verification
- Deploy the app to a device/emulator.
- Long-press a note in `NotesActivity` to verify the menu appears and "EDIT" works.
- Long-press a project/idea in `ProjectActivity` to verify the menu appears.
- Click the history icon in `ProjectNoteAdapter` items to verify the history dialog shows up.
