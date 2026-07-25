# Implementation Plan - Reduce Task Card Free Space

The user wants to reduce the "free space" in the task card, especially when there are subtasks. The current layout has multiple layers of padding and margins that accumulate, resulting in excessive empty space at the bottom and between elements.

## Proposed Changes

### 1. Update Task Card Layouts
Modify the XML layouts to reduce vertical margins and padding.

#### [MODIFY] [item_task_tasks.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/item_task_tasks.xml)
- Reduce parent `ConstraintLayout` vertical padding from `16dp` to `12dp`.
- Reduce `subtask_list_container` `layout_marginTop` from `12dp` to `8dp`.
- Change `subtask_list_container` `layout_marginBottom` from `12dp` to `4dp`.

#### [MODIFY] [item_task_list.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/item_task_list.xml)
- Apply the same changes as in `item_task_tasks.xml`.

### 2. Update Task Adapter
Reduce the programmatic padding applied to subtask items.

#### [MODIFY] [TaskAdapter.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/TaskAdapter.kt)
- In `renderSubtasks`, change `ctView.setPadding(0, 16, 0, 16)` to `ctView.setPadding(0, 8, 0, 8)`.

## Verification Plan

### Manual Verification
- Deploy the app to a device or emulator.
- Add a task with one or more subtasks.
- Expand the task to see the subtasks.
- Verify that the vertical space inside the card is significantly reduced and more compact.
