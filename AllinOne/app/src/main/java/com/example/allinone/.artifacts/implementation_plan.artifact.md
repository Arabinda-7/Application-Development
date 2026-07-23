# Implementation Plan - Fix Dashboard Flickering During Onboarding

The user reported that the Dashboard (HomeScreen) is briefly visible for about a second when opening the app for the first time or during onboarding. This is caused by `MainActivity` rendering its content before redirecting the user to `OnboardingActivity`.

## Proposed Changes

### [MODIFY] [MainActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/MainActivity.kt)

- Move the `isOnboardingCompleted` check from the Compose `setContent` block to the `onCreate` method.
- Perform the check immediately after `DataManager.loadData(this)`.
- If onboarding is not completed, start `OnboardingActivity` and `finish()` `MainActivity` immediately.

This ensures that:
1. No UI from `MainActivity` (including the splash screen and dashboard) is rendered if the user hasn't completed onboarding.
2. The redirection happens instantly, improving the user experience.

## Verification Plan

### Manual Verification
1. Clear app data or uninstall/reinstall the app.
2. Launch the app.
3. Verify that it goes directly to the Onboarding screen without showing the Dashboard.
4. Complete onboarding.
5. Verify that after onboarding, the app shows the Splash Screen and then the Dashboard as expected.
6. Close and reopen the app to ensure normal operation (Splash -> Dashboard).
