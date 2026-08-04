# Implementation Plan - Fix Unresolved references in DataManager

The project fails to build due to several missing methods in `DataManager.kt` that are being called in `AddFinanceActivity.kt`, `MainActivity.kt`, and other parts of the app. The primary reported error is `Unresolved reference 'addActivity'`, but further analysis reveals other missing methods: `getTotalDailyProgress`, `getGrowthAdvice`, `getManagementAdvice`, and `getTodayAgendaNotifications`.

## Proposed Changes

### [Component: Data Layer]

#### [MODIFY] [UserDataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/data/UserDataManager.kt)
- Add `addActivity(activity: String)` function to manage the `recentActivities` list.
- Maintain a maximum of 20 items in the list, adding new ones to the top.

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Add delegate function `addActivity(activity: String)`.
- Implement `getTotalDailyProgress()`: returns average of habit and workout progress.
- Implement `getGrowthAdvice(mood: String?)`: returns advice based on the user's current mood.
- Implement `getManagementAdvice(mood: String?)`: returns general productivity advice.
- Implement `getTodayAgendaNotifications()`: returns the current day's agenda from `WorkspaceDataManager`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify that all unresolved reference errors are resolved.

### Manual Verification
- Deploy the app and verify the Dashboard (MainActivity) loads correctly with overall progress and advice.
- Add a finance transaction and verify it appears in recent activities.
