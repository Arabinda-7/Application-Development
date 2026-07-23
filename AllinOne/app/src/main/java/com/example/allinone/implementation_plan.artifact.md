# Implementation Plan - Fix Unresolved Reference 'MuscleRadarChart'

The user is encountering a build error because `MuscleRadarChart` and `RecoveryStatusDashboard` are missing from the project, even though they are being called in `PerformanceDashboardScreen.kt`. I will implement these components in `AnalyticsComponents.kt`.

## User Review Required

> [!IMPORTANT]
> The missing components will be implemented using standard Jetpack Compose Canvas and Layout APIs to match the existing design language of the "All in One" app.

## Proposed Changes

### [Component Name]

#### [MODIFY] [AnalyticsComponents.kt](file:///C:/Users/arabi/OneDrive/Desktop/App/Development/AllinOne/app/src/main/java/com/example/allinone/AnalyticsComponents.kt)

- Implement `MuscleRadarChart(muscleDistribution: Map<String, Int>, themeColor: Color)`:
    - Uses `Canvas` to draw a radar (spider) chart.
    - Labels each axis with the muscle group name.
    - Draws a polygon representing the distribution of volume/intensity.
- Implement `RecoveryStatusDashboard(recoveryStatus: Map<String, Float>, themeColor: Color)`:
    - Displays a list or grid of muscle groups with their recovery percentage.
    - Uses progress indicators to show how close a muscle is to full recovery (48-hour model).
- Add necessary imports for `Canvas`, `Path`, `Math` etc.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to ensure the project builds successfully.

### Manual Verification
- Deploy the app to a device/emulator.
- Navigate to the Performance Dashboard.
- Switch to Workout Context (if possible) and verify the charts are rendered correctly.
