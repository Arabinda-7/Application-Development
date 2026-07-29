# Task Checklist - Fix Edit Feature for Workouts & Habits

- `[x]` Refactor to ID-based identification (Timestamp)
    - `[x]` Update `AddWorkoutActivity.kt` to load by `WORKOUT_ID`
    - `[x]` Update `AddHabitActivity.kt` to load by `HABIT_ID`
    - `[x]` Update `WorkoutAdapter.kt` to pass ID instead of index
    - `[x]` Update `HabitAdapter.kt` to pass ID instead of index
- `[x]` Enhance Detail Screens with Edit functionality
    - `[x]` Add Edit icon to `activity_workout_detail.xml`
    - `[x]` Add Edit icon to `activity_habit_detail.xml`
    - `[x]` Implement edit click in `WorkoutDetailActivity.kt`
    - `[x]` Implement edit click in `HabitDetailActivity.kt`
- `[x]` Complete the Habit Editor
    - `[x]` Add Tracking Mode grid to `activity_add_habit.xml`
    - `[x]` Add Goal Target section with roller to `activity_add_habit.xml`
    - `[x]` Update `AddHabitActivity.kt` logic to handle new fields and rollers
- `[x]` Verify all edit flows (List -> Edit, Detail -> Edit)
