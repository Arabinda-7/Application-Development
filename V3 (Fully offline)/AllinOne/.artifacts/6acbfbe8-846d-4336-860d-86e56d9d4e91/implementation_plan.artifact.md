# Implementation Plan: Notification Red Dot for New Today's Items

This plan ensures that the notification red dot on the Home Screen reappears whenever a new item (task, project milestone, or workspace task/goal) is added or scheduled for today, even if the user has already viewed the notifications for the day.

## User Review Required

> [!IMPORTANT]
> The red dot will now persist across app restarts if a new item for today was added but not yet viewed.
> It currently tracks:
> - Global Tasks with today's reminder.
> - Project Milestones (Sub-features) with today's due date.
> - Workspace Tasks and Goals with today's deadline.

## Proposed Changes

### Data Layer

#### [MODIFY] [WorkspaceDataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/WorkspaceDataManager.kt)
- Add `var hasNewTodayNotifications: Boolean = false`.

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Expose `hasNewTodayNotifications` from `WorkspaceDataManager`.
- Add `KEY_HAS_NEW_TODAY_NOTIF` constant.
- Update `saveData` and persistence logic to handle this new flag.
- Add helper `checkAndSetNewTodayNotification(timestamp: Long?)` to detect today's items.

### Feature Logic

#### [MODIFY] [AddTaskActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddTaskActivity.kt)
- Call `DataManager.checkAndSetNewTodayNotification(selectedReminder)` when saving a task.

#### [MODIFY] [AddSubFeatureActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddSubFeatureActivity.kt)
- Call `DataManager.checkAndSetNewTodayNotification(selectedDueDate)` when saving a sub-feature.

#### [MODIFY] [WorkspaceViewModel.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/WorkspaceViewModel.kt)
- Call `DataManager.checkAndSetNewTodayNotification` in `addTask`, `insertTask`, `updateTask`, `addGoal`, and `updateGoal`.

### UI Layer

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/home/HomeScreen.kt)
- Update `showRedDot` logic: `state.todayAgenda.isNotEmpty() && (DataManager.lastViewedNotificationDate != todayDateString || DataManager.hasNewTodayNotifications)`.
- Reset `DataManager.hasNewTodayNotifications = false` in `onNotificationsClick`.

## Verification Plan

### Automated Tests
- N/A (UI and Logic integration test preferred).

### Manual Verification
1. Open the app. If there are today's items, the red dot should be visible (if not viewed today).
2. Click the bell icon. The red dot should disappear.
3. Add a new Task with a reminder set for **today**.
4. Return to the Home Screen. The red dot should be **visible** again.
5. Click the bell icon. The red dot should disappear.
6. Add a new Task with a reminder set for **tomorrow**.
7. Return to the Home Screen. The red dot should **not** be visible.
8. Repeat steps 3-7 for Project Milestones and Workspace Tasks.
9. Restart the app after adding a today's task. The red dot should still be visible until clicked.
