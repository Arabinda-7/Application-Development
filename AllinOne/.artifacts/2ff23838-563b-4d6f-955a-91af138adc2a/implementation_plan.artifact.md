# Implementation Plan - Notification Section and Daily Reminders

This plan outlines the addition of a "Notification" section in the app settings, allowing users to schedule morning and night reminders with personalized content based on their tasks and completion rates.

## User Review Required

> [!IMPORTANT]
> The morning and night reminders will require `SCHEDULE_EXACT_ALARM` permission to trigger at precise times. This permission is already declared in the manifest. On Android 13+, the user must also grant the `POST_NOTIFICATIONS` permission.

## Proposed Changes

### Data & Persistence

#### [NEW] [NotificationDataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/NotificationDataManager.kt)
- Create a new data manager to store:
    - `isMorningReminderEnabled: Boolean`
    - `morningReminderTime: String` (e.g., "08:00")
    - `isNightReminderEnabled: Boolean`
    - `nightReminderTime: String` (e.g., "22:00")

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Add delegation for the new notification settings.
- Update `saveData` and `loadData` to persist these settings in `SharedPreferences`.

### UI - Settings

#### [NEW] [NotificationSettingsActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/NotificationSettingsActivity.kt)
- A new activity to manage notification settings.
- Features:
    - Toggle for morning reminder.
    - Time picker for morning reminder.
    - Toggle for night reminder.
    - Time picker for night reminder.
- Automatically schedules/cancels alarms via `NotificationScheduler` when settings change.

#### [NEW] [activity_notification_settings.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_notification_settings.xml)
- Layout for the new notification settings screen, matching the app's dark/glass theme.

#### [MODIFY] [SettingsHubSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/SettingsHubSection.kt)
- Add "Notifications" item to the settings hub.
- Handle navigation to `NotificationSettingsActivity`.

### Notification Logic

#### [NEW] [NotificationScheduler.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/NotificationScheduler.kt)
- Utility to schedule daily recurring alarms for morning and night reminders using `AlarmManager`.

#### [NEW] [DailyReminderReceiver.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DailyReminderReceiver.kt)
- A `BroadcastReceiver` to handle the scheduled alarms.
- **Morning Reminder Logic**:
    - Select a random motivational quote.
    - Identify task sections that have pending tasks for today.
    - Trigger a notification with the quote and an expandable section mentioning these task sections.
- **Night Reminder Logic**:
    - Calculate today's task completion rate.
    - Select a "good court" (closing quote) based on the completion rate.
    - List unfinished task names in the expandable section.
    - Trigger the notification.

#### [NEW] [NotificationQuoteProvider.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/NotificationQuoteProvider.kt)
- Utility to provide motivational quotes and completion-based messages.

### Manifest

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/AndroidManifest.xml)
- Register `NotificationSettingsActivity`.
- Register `DailyReminderReceiver`.

## Verification Plan

### Automated Tests
- Unit tests for `NotificationQuoteProvider` to ensure correct quotes are returned.
- Unit tests for completion rate calculation logic.

### Manual Verification
- Navigate to Settings -> Notifications.
- Enable Morning Reminder and set a time close to current time.
- Verify that the notification appears with a motivational quote and correct task sections.
- Repeat for Night Reminder and verify completion rate and unfinished tasks list.
- Change times and verify that old alarms are cancelled and new ones are scheduled.
- Disable reminders and verify that no notifications are triggered.
