# Walkthrough - Real-time UI Updates

I have implemented changes to ensure that the application UI reacts in real-time to data changes, covering both global application state and the specific Workspace feature.

## Changes Made

### 1. Observable Global State (`DataManager.kt`)
I added a `SharedFlow` to the `DataManager` singleton to act as a signal for data changes. Every time `saveData()` or state-modifying functions (like `addXP` or `addActivity`) are called, a signal is emitted.

```kotlin
// DataManager.kt
val dataChangeSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

fun notifyDataChanged() {
    dataChangeSignal.tryEmit(Unit)
}
```

### 2. Reactive Dashboard (`MainActivity.kt`)
The `MainActivity` now listens to the `dataChangeSignal`. When a change is detected (e.g., user updates their profile name in another activity), the Dashboard refreshes its state immediately.

```kotlin
// MainActivity.kt
LaunchedEffect(Unit) {
    DataManager.dataChangeSignal.collect {
        refreshState()
    }
}
```

### 3. Reactive Workspace (`WorkspaceViewModel.kt`)
Fixed a reactivity gap in the Workspace feature. Previously, when a specific project was selected, the overall list of projects was static. I have updated the `combine` logic to include the full project list flow from the repository.

```kotlin
// WorkspaceViewModel.kt
val allProjectsFlow = repository.getAllProjects()
combine(..., allProjectsFlow) { array ->
    // ...
    projects = array[9] as List<ProjectEntity>
}
```

## Verification Results

- **Global State**: Changes to `DataManager` properties now trigger UI refreshes in `MainActivity`.
- **Workspace**: The project list now stays in sync with the database even while viewing a specific project's dashboard.
- **Performance**: Used `MutableSharedFlow` with `extraBufferCapacity = 1` and `tryEmit` to ensure non-blocking updates and avoid memory leaks.

> [!TIP]
> This pattern can be extended to other view models if they rely on `DataManager` for certain settings, ensuring a consistent and "live" feel across the entire app.
