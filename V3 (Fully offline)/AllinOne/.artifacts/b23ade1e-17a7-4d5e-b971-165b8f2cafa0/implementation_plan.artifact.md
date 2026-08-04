# Implementation Plan - Refactor Large Activities and Optimize Data Saving

Refactor large Activities (>700 lines) and optimize the global data saving mechanism to improve performance and code maintainability.

## User Review Required

> [!IMPORTANT]
> I am extracting common UI logic for "Subfeatures" and "Goals" from `AddIdeaActivity`, `EditProjectActivity`, and `AddProjectActivity` into a new `ProjectUiHelper` class. This will significantly reduce the size of these files and ensure consistent behavior.
> I will also optimize `DataManager.saveData` to avoid redundant main-thread operations and debounce auto-save calls.

## Proposed Changes

### [Component] Core Data Management

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Update `syncScope` to use `Dispatchers.IO` for repository operations.
- Implement a debounced `saveData` mechanism to prevent performance degradation during rapid user input (e.g., auto-save on keystroke).

---

### [Component] UI Helpers

#### [NEW] [ProjectUiHelper.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/core/utils/ProjectUiHelper.kt)
- Create a helper class to encapsulate the `refreshSubFeatures`, `createSubFeatureItem`, and `refreshGoalsUI` logic used across project-related Activities.

---

### [Component] Large Activities Refactoring

#### [MODIFY] [AddIdeaActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddIdeaActivity.kt)
- Delegate subfeature and goal UI management to `ProjectUiHelper`.
- Remove redundant UI boilerplate.
- Optimize the `saveIdea` call with debouncing.

#### [MODIFY] [EditProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/EditProjectActivity.kt)
- Delegate subfeature and goal UI management to `ProjectUiHelper`.
- Remove redundant UI boilerplate.

#### [MODIFY] [AddProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddProjectActivity.kt)
- Delegate subfeature and goal UI management to `ProjectUiHelper`.
- Remove redundant UI boilerplate.

## Verification Plan

### Automated Tests
- Run `gradlew :app:assembleDebug` to ensure compilation.

### Manual Verification
- Verify that subfeatures and goals still render correctly in "Add Idea", "Add Project", and "Edit Project" screens.
- Verify that "Auto-save" in "Add Idea" works without causing UI lag.
- Check that adding/deleting subfeatures works as expected.
