# Implementation Plan - Fix App Launch Freeze

The app is currently frozen on the launch screen ("Optimizing Ecosystem... 0%") due to a Room database schema mismatch that prevents the initialization process from completing. This causes the UI to wait indefinitely for the data to be loaded.

## User Review Required

> [!IMPORTANT]
> The fix involves bumping the Room database version and using `fallbackToDestructiveMigration()`. This will **wipe all existing local data** in the Room database to reconcile the schema. Since the app also has a legacy SharedPreferences-based data storage and a migration manager, most data should be restored if it hasn't been fully migrated yet, but any data only present in the current Room schema will be lost.

## Proposed Changes

### Core
#### [MODIFY] [AppDatabase.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/data/AppDatabase.kt)
- Bump database version from 7 to 8 to resolve the schema integrity check failure.

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Wrap the initialization logic to ensure `isDataLoaded` is updated even if an exception occurs, preventing the UI from hanging forever on the splash screen.

## Verification Plan

### Manual Verification
- Deploy the app to the emulator.
- Observe if the splash screen animation starts and the app proceeds to the Home screen.
- Verify that data is correctly loaded/migrated.
