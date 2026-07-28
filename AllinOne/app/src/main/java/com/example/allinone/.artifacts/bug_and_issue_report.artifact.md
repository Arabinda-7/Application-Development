# Post-Migration Bug & Issue Report

This report identifies potential stability and performance issues following the Room Database and Repository pattern migration.

## 🔴 Critical Issues (High Risk)

### 1. `UninitializedPropertyAccessException`
- **Location:** `DataManager.kt`
- **Cause:** `taskRepo`, `habitRepo`, etc., are `lateinit var`. If an Activity or ViewModel accesses a `DataManager` property (like `habits`) before `initialize(context)` completes, the app will crash.
- **Impact:** High crash risk during rapid app startup or background process restoration.

### 2. Concurrent Modification Risk
- **Location:** `DataManager.kt` and all Adapters
- **Cause:** `DataManager` lists (e.g., `tasks`) are updated via background Flows. Although updates are synchronized, UI Adapters read these lists directly without synchronization.
- **Impact:** Possible crashes (`ConcurrentModificationException`) or UI glitches when data is updated while the user is scrolling.

## 🟡 Major Issues (Medium Risk)

### 3. Duplicate Data on Migration
- **Location:** `LegacyMigrationManager.kt`
- **Cause:** Migration uses `insertAll(entities)`. If migration runs twice (e.g., app crash during first run), and Primary Keys (timestamps) are slightly different or UUIDs are regenerated, data will be duplicated in the database.
- **Impact:** Users seeing double entries for tasks, notes, or transactions.

### 4. Memory Redundancy
- **Location:** `DataManager.kt`
- **Cause:** Data is stored both in the Room Database (on disk) and in `MutableList` objects (in RAM). For large datasets, this doubles the memory usage.
- **Impact:** Higher memory pressure on low-end devices.

## 🟢 Minor Issues (Low Risk)

### 5. Inconsistent notifyDataChanged
- **Location:** `DataManager.kt`
- **Cause:** `notifyDataChanged` is called both when the database flow emits and when `saveData` is called. This might trigger redundant UI refreshes.
- **Impact:** Minor performance hit (redundant UI re-renders).

### 6. Silent Persistence Failures
- **Location:** `LegacyMigrationManager.kt` and `DataManager.kt`
- **Cause:** `try-catch` blocks only log errors to console (`printStackTrace`).
- **Impact:** Hard-to-debug data loss issues in production.

---

## Recommended Fixes

1.  **Safety First:** Change `lateinit var` repos to nullable or use a "Ready" flag to prevent early access crashes.
2.  **Thread Safety:** Ensure Adapters use a copy of the list or transition to `ListAdapter` which handles its own background diffing and thread safety.
3.  **Migration Check:** Add a more robust check in `LegacyMigrationManager` (e.g., check if the table is already populated before migrating).
4.  **Modernize UI:** Gradually refactor Activities to use `Flow` directly from Repositories instead of reading static lists from `DataManager`.
