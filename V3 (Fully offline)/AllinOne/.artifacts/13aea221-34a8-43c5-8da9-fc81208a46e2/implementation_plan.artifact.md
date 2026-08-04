# Implementation Plan - Eliminating Data Loss and Fixing Import/Restart Consistency

This plan addresses the critical issue where data disappears after a restart or requires multiple imports. The root cause is a race condition between Room's asynchronous observers and the app's auto-save mechanism.

## Root Cause Analysis

1.  **Observer Race**: In `startDatabaseObservation`, multiple Room flows are `combined`. If Room emits an initial "empty" state (common after a DB reset or during a cold boot), the `combine` block triggers and clears all in-memory lists (`habits`, `tasks`, etc.).
2.  **Auto-Wipe**: Once `isDataLoaded` becomes true (at the end of `initialize`), the next `saveData` call (which is frequent) triggers `performSave`. Since memory was cleared by the observer, `performSave` syncs the "empty" state back to Room, permanently deleting the data.
3.  **Import Lag**: `importData` resets the database. The new database instance might take a few milliseconds to "warm up". If `initialize` runs immediately, its first read might return empty.

## Proposed Changes

### Logic Layer (DataManager)

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- **State Reset**: Set `isDataLoaded.value = false` at the very beginning of `initialize`.
- **Domain Isolation**: Refactor `startDatabaseObservation` to use separate coroutines for each data domain. This prevents a "loading" state in one table from clearing others.
- **Initial Sync Guard**: In each observer flow, add an `onStart { delay(100) }` or similar, and **ignore the first emission if it is empty** but memory is already populated.
- **`performSave` Sanity Check**: Add a "Wipe Guard". If a list in memory is empty, but the corresponding Room table contains data, **log a warning and skip the sync**. This prevents accidental mass-deletion due to UI/Memory glitches.
- **Atomic Initialization**: Ensure `loadData` (suspend) finishes its Room fetch and memory population **before** starting the continuous observers.

### Repository Layer

#### [MODIFY] [TaskRepository.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/repository/TaskRepository.kt) (and others)
- Add a `isEmpty(): Boolean` or `getCount(): Int` method to allow `DataManager` to check the current Room state before deciding to sync or overwrite.

## Verification Plan

### Manual Verification
- **The "Restart" Test**: Add data, restart app. Verify data stays.
- **The "Import" Test**: Import once. Verify data shows up **immediately**.
- **The "Wipe" Test**: Simulate an empty-memory save (force clear lists in memory via debugger if possible). Verify Room is NOT wiped.
