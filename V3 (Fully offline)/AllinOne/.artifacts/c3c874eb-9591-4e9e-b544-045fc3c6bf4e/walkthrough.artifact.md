# Walkthrough - Advanced Project Sub-features

I have upgraded the Project milestones (sub-features) with advanced management logic, visual indicators, and automated reminders.

## Changes Made

### 1. Weighted Progress Logic
- **Impact-Based Progress**: Each sub-feature now has a **Weight (1–10)**.
- The project's overall progress bar no longer just counts tasks; it calculates the sum of weights.
- *Example*: Completing one "High Weight" task will now move the progress bar significantly more than completing several "Low Weight" tasks.

### 2. Milestone Dependencies ("Blocked By")
- **Locked Milestones**: You can now set one milestone to be "Blocked By" another.
- In the project view, blocked milestones show a **🔒 Lock** icon and provide visual feedback that a prerequisite is pending.

### 3. Urgency & Resource Tracking
- **Urgency Levels**: Set individual milestones to **Low, Medium, or High Urgency**. A color-coded dot appears next to the task name in the list.
- **Resource Links**: Attach a URL directly to a milestone. A **🔗 Link** icon appears in the list for instant access to related documents or websites.

### 4. Smart Milestone Reminders
- **Individual Alerts**: You can now enable a reminder for specific sub-features.
- The app will trigger a system notification when the sub-feature's due date is reached, helping you manage individual deadlines within a larger project.

### 5. Enhanced Editor UI
- Refactored the **Edit Sub-feature** screen with:
    - A Weight slider.
    - Urgency selection chips.
    - A searchable dependency selector.
    - A reminder toggle integrated with the date picker.

## Verification

### Files Modified
- [ProjectFeature.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProjectFeature.kt)
- [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- [AddSubFeatureActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddSubFeatureActivity.kt)
- [AddProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddProjectActivity.kt)
- [ProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProjectActivity.kt)
- [ReminderReceiver.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ReminderReceiver.kt)

### Manual Test Steps (for user)
1.  **Test Weighting**: Edit a project. Set one sub-feature to Weight 10 and another to Weight 1. Mark the Weight 10 one complete. Observe the progress bar jump significantly.
2.  **Test Dependency**: Edit a sub-feature and set it to be blocked by another. Check the project list to see the lock icon.
3.  **Test Reminders**: Enable a reminder for a sub-feature and set the time to 1 minute from now.
4.  **Test Links**: Add a URL to a sub-feature. Tap the link icon in the list to open it.
