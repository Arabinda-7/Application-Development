# Tasks - Fix Habit/Workout Completion Sync Issues

- [x] Update `DataManager.kt` for immediate save support
- [x] Update Habit Section
    - [x] `HabitTrackerActivity.kt`: Listen to `dataChangeSignal`
    - [x] `HabitAdapter.kt`: Update callback and trigger immediate save
    - [x] `HabitListSection.kt`: Pass immediate flag
- [x] Update Workout Section
    - [x] `WorkoutRoutineActivity.kt`: Listen to `dataChangeSignal`
    - [x] `WorkoutAdapter.kt`: Update callback and trigger immediate save
    - [x] `WorkoutListSection.kt`: Pass immediate flag
- [x] Verification
    - [x] Build project
    - [x] Manual verification of completion persistence
