# Task: Fix Data Import and Visibility Issues

- [x] Update `DataManager.kt` to improve import reliability
    - [x] Reset `data_migrated_to_sql` flag during import
    - [x] Clear SharedPreferences before restoring data in `importData`
    - [x] Refine `loadData` and observation logic to ensure consistency
- [x] Update `LegacyMigrationManager.kt` with enhanced logging and error handling
- [ ] Verify fix by simulating a legacy import into a migrated app state
