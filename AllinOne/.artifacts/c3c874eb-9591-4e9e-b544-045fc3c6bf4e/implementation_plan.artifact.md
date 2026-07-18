# Implementation Plan - Rename Task Section Files

Rename all files in the "Task" section from "todo" or "todolist" variants to simply use "task".

## Proposed Changes

### [File Renames]

- **Kotlin Source**:
    - Rename [ToDoListActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ToDoListActivity.kt) to `TaskActivity.kt`.
- **Layout Resources**:
    - Rename [activity_to_do_list.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_to_do_list.xml) to `activity_task.xml`.
- **Drawable Resources**:
    - Rename [ic_todo_list.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/drawable/ic_todo_list.xml) to `ic_task.xml`.

### [Code & Configuration Updates]

#### [MODIFY] [TaskActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/TaskActivity.kt) (formerly ToDoListActivity.kt)
- Update class name from `ToDoListActivity` to `TaskActivity`.
- Update `setContentView(R.layout.activity_to_do_list)` to `setContentView(R.layout.activity_task)`.
- Update internal log tags or string references if applicable.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/AndroidManifest.xml)
- Update `.ToDoListActivity` entry to `.TaskActivity`.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/MainActivity.kt)
- Update all `Intent(this, ToDoListActivity::class.java)` to `Intent(this, TaskActivity::class.java)`.
- Update `R.drawable.ic_todo_list` to `R.drawable.ic_task`.

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Update `R.drawable.ic_todo_list` references to `R.drawable.ic_task`.

#### [MODIFY] [SettingsActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/SettingsActivity.kt)
- Update `R.drawable.ic_todo_list` and `ToDoListActivity` references.

#### [MODIFY] [TaskAdapter.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/TaskAdapter.kt)
- Update cast from `ToDoListActivity` to `TaskActivity`.

#### [MODIFY] Layout Files
- Update any `tools:context=".ToDoListActivity"` to `tools:context=".TaskActivity"`.
- Update any `@drawable/ic_todo_list` to `@drawable/ic_task`.

## Verification Plan

### Build & Run
- Ensure the project builds successfully after renames.
- Verify the Task section opens correctly from the Home screen.
- Verify the Task icon is still visible in Settings and the Home screen.

### Manual Verification
- Navigate to the Task section.
- Add a new task.
- Check if the layout is correct (no missing resources).
- Open Settings and check if the Task icon is correct.
