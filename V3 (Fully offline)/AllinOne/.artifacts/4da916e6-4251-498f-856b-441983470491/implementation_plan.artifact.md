# Implementation Plan - Fixing Workspace Data Export/Import

The user reported that workspace data is not saved when exporting data, and after importing, the workspace is empty. This is because workspace data is stored in a Room database (`WorkspaceDatabase`), while the export/import logic in `DataManager` only handles data stored in `SharedPreferences` (including a simplified `projects` list of `Note` objects).

## Proposed Changes

### 1. Model Updates

#### [MODIFY] [AppData.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/AppData.kt)
- Import workspace entities from `com.example.allinone.workspace.data`.
- Update `AllAppData` data class to include fields for all workspace entities (Projects, Tasks, Goals, Features, Bugs, Ideas, Notes, Resources, Activity Logs, and Cross References).

### 2. Database Updates

#### [MODIFY] [WorkspaceDao.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/workspace/data/WorkspaceDao.kt)
- Add `suspend` methods to fetch all records from each table as `List` (for export).
- Add `suspend` methods to insert multiple records (for import).
- Add `suspend` methods to delete all records from each table (for clean import).

### 3. Data Management Logic

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Update `exportData()`:
    - Change signature to `suspend fun exportData(context: Context): String`.
    - Fetch all workspace data from `WorkspaceDatabase` and include it in `AllAppData`.
- Update `importData()`:
    - Change signature to `suspend fun importData(context: Context, json: String): Boolean`.
    - Extract workspace data from `AllAppData`.
    - Clear existing workspace data in `WorkspaceDatabase`.
    - Insert imported workspace data into `WorkspaceDatabase`.

### 4. UI/Activity Updates

#### [MODIFY] [SettingsActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/SettingsActivity.kt)
- Update `exportLauncher` and `importLauncher` to call the new `suspend` versions of `exportData` and `importData` within a `lifecycleScope.launch`.

#### [MODIFY] [ProfileActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/ProfileActivity.kt)
- Update `exportLauncher` to call the new `suspend` version of `exportData` within a `lifecycleScope.launch`.

## Verification Plan

### Manual Verification
- Create a project in the Workspace with some tasks, goals, and notes.
- Export the data to a JSON file.
- Verify that the JSON file contains the workspace data (e.g. `workspaceProjects`, `workspaceTasks`, etc.).
- Delete the app data or clear the database.
- Import the data from the JSON file.
- Verify that the Workspace projects and all related items (tasks, goals, etc.) are restored.
- Verify that other data (habits, workouts, etc.) are also correctly restored.
