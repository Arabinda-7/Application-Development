# Post-Refactor App Health & Bug Fix Plan

This plan addresses identified bugs, UI glitches, and architectural loose ends following the migration to Room database and Repository pattern.

## User Review Required

> [!IMPORTANT]
> **Metric Alignment:** I propose aligning "Daily Progress" to use **Completion Rate** (0/1 workouts) as the primary metric, while showing **Intensity/Volume** as a secondary metric. This matches user expectations from the dashboard.

## Proposed Changes

### 🔧 1. Metric Alignment & Clarification
Align workout progress calculations to prevent contradictory percentages between the Today tab and History tab.

#### [MODIFY] [WorkoutDataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/WorkoutDataManager.kt)
- Add `getWeightedWorkoutProgress()` to handle intensity-based progress.
- Keep `getWorkoutProgress()` for simple count-based completion.

#### [MODIFY] [WorkoutPerformanceSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutPerformanceSection.kt)
- Update labels to distinguish between "Completion" and "Intensity".
- Use `getWorkoutProgress()` for the main stat and `getWeightedWorkoutProgress()` for the detailed percentage if needed.

---

### 🚀 2. Loading State Synchronization
Ensure the UI waits for database initialization and migration before dismissing the splash screen.

#### [MODIFY] [MainActivityViewModel.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/MainActivityViewModel.kt)
- Observe `DataManager.isDataLoaded` Flow.
- Only set `dashboardState.isDataLoaded = true` when `DataManager` signals readiness.

---

### 📈 3. Streak Logic Refinement
Fix the bug where workout streaks reset on days the user wasn't scheduled to work out.

#### [MODIFY] [WorkoutDataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/WorkoutDataManager.kt)
- Refactor `getWorkoutStreaks()` to check `repeatDays` when evaluating gaps between completed workouts.

---

### 🔐 4. Security & Cleanup
Secure the application entry points and remove redundant code.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/AndroidManifest.xml)
- Set `android:exported="false"` for internal activities like `AddNoteActivity`, `AddTaskActivity`, etc., to prevent unauthorized direct access.

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Clean up unused functions identified by the analyzer.
- Add clarifying parentheses and fix lint warnings (KTX extensions).

## Verification Plan

### Automated Tests
- N/A (Unit tests not yet set up for this project).

### Manual Verification
1. **Metric Check:** Verify that Today Tab and History Tab show consistent "Workouts (X/Y)" labels.
2. **Splash Check:** Clear app data, launch, and ensure the splash screen persists until data is actually available (not just a black screen).
3. **Streak Check:** Set a workout for "Mon, Wed, Fri", complete Mon, skip Tue, and verify that the streak is still 1 on Wed.
4. **Security Check:** Try to launch `AddNoteActivity` via ADB while the app is locked (if ADB is available) or verify exported flag in Manifest.
