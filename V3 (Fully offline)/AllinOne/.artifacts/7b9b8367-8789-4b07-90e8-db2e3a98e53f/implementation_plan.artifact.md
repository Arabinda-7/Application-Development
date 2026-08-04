# Implementation Plan: Today's Deadline Notifications

Improve the app's notification system to proactively notify the user about tasks, projects, and workspace items (project tasks/milestones) due today. This includes both the in-app agenda dialog and system-level notifications.

## User Review Required

> [!IMPORTANT]
> The app will now query the **Workspace Database** to include "Professional" project tasks and milestones in the daily agenda.
> We will also add a **Summary System Notification** that appears when the app starts if there are deadlines for the day, ensuring you never miss a commitment.

## Proposed Changes

### [Workspace Data Layer]
Integrate deadline querying into the Workspace Room database.

#### [MODIFY] [WorkspaceDao.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/data/WorkspaceDao.kt)
- Add `@Query` methods to fetch `ProjectEntity`, `TaskEntity`, and `GoalEntity` filtered by a date range (start/end of today).

---

### [Core Data Management]
Unify the "Today's Agenda" logic to include Workspace data.

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Add `workspaceTodayAgenda` property to store pre-fetched workspace reminders.
- Add a `suspend fun updateWorkspaceAgenda(context: Context)` method to query the Room DB and populate `workspaceTodayAgenda`.
- Update `getTodayAgendaNotifications()` to merge `workspaceTodayAgenda` into the returned map.

---

### [App Lifecycle & Notifications]
Trigger updates and show proactive alerts.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/MainActivity.kt)
- In `refreshState()`, use `lifecycleScope` to call `DataManager.updateWorkspaceAgenda()`.
- Add logic to trigger a **System Notification** (using the existing `ReminderReceiver` channel) if new deadlines are detected for today.

#### [MODIFY] [ReminderReceiver.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ReminderReceiver.kt)
- Add a helper method to show a "Summary Notification" for multiple deadlines (e.g., "You have 5 things due today!").

## Verification Plan

### Automated Tests
- No new automated tests planned, but we will verify logs to ensure Room queries are returning expected items for mock dates.

### Manual Verification
1. Create a **Main Task** with a reminder for today.
2. Create a **Workspace Project Task** with a due date for today.
3. Restart the app.
4. Verify the **Red Dot** appears on the notification bell.
5. Open the Bell icon and verify both tasks are listed in the "Today's Agenda" dialog.
6. Verify a **System Notification** appears in the status bar summarizing the day's deadlines.
