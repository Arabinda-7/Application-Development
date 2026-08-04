# Implementation Plan - Fixing Data Import/Migration Issues

The user reported that when importing old data to a new phone, the data is not showing up. This is likely due to a combination of encrypted database passphrase mismatch and the database instance not being properly reset during the import process.

## User Review Required

> [!IMPORTANT]
> The fix involves deleting the local (mostly empty) database on the new device during the import process to ensure the imported data can be correctly decrypted using the imported passphrase. This is safe as the import process is intended to overwrite all local data anyway.

## Proposed Changes

### [Core Data & Persistence]

#### [MODIFY] [WorkspaceDatabase.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/data/WorkspaceDatabase.kt)
- Add a `resetDatabase(context: Context)` method to the companion object to close the current instance and delete the database file. This ensures that when the database is re-opened after an import, it uses the correct imported passphrase.

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Update `importData` to:
    1. Call `WorkspaceDatabase.resetDatabase(context)` *before* writing the imported SharedPreferences.
    2. Ensure all data types (including `Long` and `Float`) are correctly handled when restoring SharedPreferences from the JSON map.
    3. Re-initialize the database and populate it with the workspace data after the passphrase has been updated in SharedPreferences.

### [Settings & UI]

#### [MODIFY] [SettingsBackupHandler.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/SettingsBackupHandler.kt)
- Improve error handling and user feedback during the import process.

## Verification Plan

### Automated Tests
- I will attempt to run a build to ensure no syntax errors are introduced. (Note: Full instrumented tests for data migration are difficult in this environment, but I will verify the logic thoroughly).

### Manual Verification
- The user should try to export a backup from their old phone and import it on the new phone.
- Verify that Habits, Workouts, and Workspace Projects all appear after the import and persist after an app restart.
