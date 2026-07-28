# Implementation Plan - Fixing Export and Import Features

The user reported issues with the export and import features. The primary issue is that the current implementation only handles data stored in `SharedPreferences` and misses the Workspace data stored in the Room database (`WorkspaceDatabase`). Additionally, the `importData` logic has a type conversion bug where `Long` values (like timestamps) can be truncated when restored from JSON.

## User Review Required

> [!IMPORTANT]
> The backup format will be updated to include Workspace data. Older backups will still be compatible with `SharedPreferences` restoration, but will not contain any Workspace data.

> [!WARNING]
> Importing a backup will overwrite ALL current app data, including all Workspace projects, tasks, and notes.

## Proposed Changes

### Data Management Layer

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Update `exportData(context: Context)`:
    - Change signature to `suspend fun exportData(context: Context): String`.
    - Fetch all data from `SharedPreferences`.
    - Fetch all data from `WorkspaceDatabase` (Projects, Goals, Tasks, Features, Bugs, Ideas, Notes, Resources, Activity Logs, Cross References).
    - Combine both into a single JSON object using `AllAppData` or a custom Map.
- Update `importData(context: Context, json: String)`:
    - Change signature to `suspend fun importData(context: Context, json: String): Boolean`.
    - Implement a safer type conversion logic to handle `Int` vs `Long` vs `Float` correctly from Gson's `Double` representation.
    - Clear all existing tables in `WorkspaceDatabase`.
    - Populate `WorkspaceDatabase` with the imported data.
    - Call `loadData(context)` and `notifyDataChanged()` to refresh the UI.

### UI Layer

#### [MODIFY] [SettingsActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/SettingsActivity.kt)
- Update `exportLauncher` to call the `suspend` version of `DataManager.exportData`.

#### [MODIFY] [ProfileActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/ProfileActivity.kt)
- Update `exportLauncher` to call the `suspend` version of `DataManager.exportData`.

#### [MODIFY] [SettingsBackupHandler.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/SettingsBackupHandler.kt)
- Update `handleImport` to call the `suspend` version of `DataManager.importData`.

## Verification Plan

### Automated Tests
- I will verify the code compiles after making the signature changes.

### Manual Verification
1.  **Export Test**:
    *   Create a Workspace project with tasks and notes.
    *   Trigger "Export Backup" from Settings.
    *   Inspect the generated JSON to ensure it contains both `SharedPreferences` keys and `workspaceProjects`, etc.
2.  **Import Test**:
    *   Delete a Workspace project or clear app data.
    *   Trigger "Import Backup" using the previously exported file.
    *   Verify all data (Habits, Workouts, Workspace projects/tasks) is restored correctly.
    *   Verify that timestamps (Long values) are preserved correctly.
