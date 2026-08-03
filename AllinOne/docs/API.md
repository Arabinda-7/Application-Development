# API & Domain Repository Reference

## 🛠️ Repository Interfaces (`domain/repository/`)

### 1. `TaskRepository`
```kotlin
interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getTaskSettings(): Flow<TaskSettings>
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun clearCompletedTasks()
}
```

### 2. `HabitRepository`
```kotlin
interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    suspend fun insertHabit(habit: Habit)
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
}
```

### 3. `WorkoutRepository`
```kotlin
interface WorkoutRepository {
    fun getAllWorkouts(): Flow<List<Workout>>
    fun getWorkoutSettings(): Flow<WorkoutSettings>
    suspend fun insertWorkout(workout: Workout)
    suspend fun updateWorkout(workout: Workout)
    suspend fun deleteWorkout(workout: Workout)
}
```

### 4. `NoteRepository`
```kotlin
interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun getNoteSettings(): Flow<NoteSettings>
    suspend fun insertNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
}
```

### 5. `FinanceRepository`
```kotlin
interface FinanceRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getFinanceSettings(): Flow<FinanceSettings>
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
}
```

---

## 🎯 Primary Domain Use Cases (`domain/usecase/`)

| Use Case Class | Target Package | Description |
|---|---|---|
| `GetTasksUseCase` | `domain.usecase.task` | Emits active task stream from repository. |
| `AddTaskUseCase` | `domain.usecase.task` | Validates and inserts new task into database. |
| `UpdateTaskUseCase` | `domain.usecase.task` | Updates existing task attributes. |
| `DeleteTaskUseCase` | `domain.usecase.task` | Removes task entry. |
| `GetHabitsUseCase` | `domain.usecase.habit` | Emits habit list flow. |
| `GetWorkoutsUseCase` | `domain.usecase.workout` | Emits workout routines. |
| `GetProjectsUseCase` | `domain.usecase.project` | Emits active project board notes. |
| `GetUserSettingsUseCase` | `domain.usecase.user` | Emits user preferences and theme settings. |
