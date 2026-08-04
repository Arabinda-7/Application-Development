# Implementation Plan - Fixing Legacy Data Import

The user reported that importing data from an old version shows "success" but data in different sections (Habits, Workouts, Tasks, etc.) is missing. Analysis of the provided backup file `allinone_backup_1785186129338.json` reveals two main issues:
1. **Key Mismatch**: The old version used keys like `habits`, `tasks`, `projects`, while the current version uses `habits_data`, `tasks_data`, `projects_data`.
2. **Type Mismatch**: The old version exported these sections as JSON Arrays/Objects, while the current version stores them as JSON Strings inside `SharedPreferences`. The current `importData` logic skips any value that isn't a `String`, `Boolean`, or `Double`.

## User Review Required

> [!IMPORTANT]
> The import logic will now automatically map legacy keys to the current format and convert JSON structures (Arrays/Maps) back into the expected String format for `SharedPreferences`.

## Proposed Changes

### Data Management Layer

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Update `importData` function:
    - Add a mapping of legacy keys to modern keys.
    - Update the `forEach` loop to handle `List` and `Map` values by converting them to JSON strings using `Gson().toJson()`.
    - Ensure legacy keys are redirected to their modern counterparts during the import process.
    - Improve numeric type handling to prevent truncation of `Long` values (timestamps).

## Verification Plan

### Automated Tests
- I will verify the code compiles and the logic correctly maps the keys identified from the sample JSON.

### Manual Verification
1. **Import Legacy File**: Use the provided `allinone_backup_1785186129338.json` file to perform an import.
2. **Verify Sections**:
    - Check if **Habits** are restored (e.g., "Wakeup before 7am").
    - Check if **Projects** are restored (e.g., "All in one 3.0").
    - Check if **Notes** are restored (e.g., "Hello", "Tree").
    - Check if **Workspace** data is restored (the backup contains `workspaceProjects`, etc.).
3. **Verify UI**: Ensure the UI refreshes and shows the restored data immediately.
