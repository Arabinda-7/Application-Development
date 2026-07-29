# Implementation Plan - Border-Only Checkbox Design

This plan refines the checkbox design to use colored borders without solid fills, and extends themed checkboxes to the task list.

## Proposed Changes

### UI & Drawables

#### [MODIFY] [checkbox_checked.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/drawable/checkbox_checked.xml)
- Remove the solid fill from the checked state.
- Add a 2dp stroke to match the unchecked state, ensuring the circular border remains visible and colored when checked.
- Keep the checkmark icon in the center.

### Task Section

#### [MODIFY] [TaskAdapter.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/TaskAdapter.kt)
- Update `onBindViewHolder` to apply the task's theme color (or global task color) to the `taskCompleted` checkbox.
- Update `renderSubtasks` to apply the same themed tinting to the subtask `CheckedTextView` checkmarks.

## Verification Plan

### Automated Tests
- Build the project to verify no UI-related regressions.

### Manual Verification
1. Open Habits/Workouts/Tasks.
2. Verify that incomplete checkboxes show a colored circular border only.
3. Verify that completed checkboxes show a colored circular border with a checkmark, but **no solid background fill**.
4. Verify that the card elements (bars, borders) in Habits/Workouts remain colored as per previous requirements.
