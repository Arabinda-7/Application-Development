# Tasks - Fix Data Loss and Consistency

- [x] Refactor `DataManager.startDatabaseObservation` to domain-isolated observers
- [x] Implement "Wipe Guard" in Room observers to prevent clearing memory with empty emissions
- [x] Guard `performSave` sync operations to skip if lists are empty (prevents accidental wipes)
- [x] Set `isDataLoaded = false` at start of initialization
- [x] Verify data persistence after app restart and single import
