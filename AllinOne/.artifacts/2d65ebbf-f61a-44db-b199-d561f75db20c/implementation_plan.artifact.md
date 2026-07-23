# Implementation Plan - Add Configurable Loading Screen

This plan covers adding a splash screen (loading screen) to the app startup and a setting to control its duration.

## User Review Required

> [!IMPORTANT]
> The loading screen will appear every time the app starts. The "Loading Time" setting allows users to extend this duration if they prefer a smoother visual transition or want to ensure all data is processed before the main dashboard appears.

## Proposed Changes

### [Component Name] Data Management
#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Add `startupLoadingTime` (default 2000ms).
- Add `isFirstLaunch` flag (optional, can be used for special splash logic).
- Implement a mock `processDataForSmoothExecution()` method.

### [Component Name] UI - Settings
#### [MODIFY] [SettingsActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/SettingsActivity.kt)
- Add "Startup Loading Time" option in the "Others" section.
- Implement a slider dialog to adjust the loading time (1s to 5s).

### [Component Name] UI - Main Dashboard
#### [MODIFY] [MainActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/MainActivity.kt)
- Introduce a `showSplash` state variable.
- Use `LaunchedEffect` to handle the startup sequence:
    1. Show Splash Screen.
    2. Call `DataManager.processDataForSmoothExecution()`.
    3. Wait for the configured `startupLoadingTime`.
    4. Hide Splash Screen.
    5. Check for onboarding if necessary.
- Add a `SplashScreen` composable function.

## Verification Plan

### Automated Tests
- N/A (Manual UI verification is more appropriate for splash screen transitions).

### Manual Verification
- **First Launch**: Clear app data and launch. Verify splash screen appears, then redirects to onboarding.
- **Regular Launch**: Launch app after onboarding. Verify splash screen shows for the configured time.
- **Settings**: Go to Settings -> Others -> Startup Loading Time. Change to 5s. Close app and relaunch. Verify splash screen stays for 5s.
- **Visuals**: Ensure the splash screen is simple and matches the app's aesthetic.
