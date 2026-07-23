# Implementation Plan - Enhance Monthly Momentum in History

The user wants to explicitly mention the "Monthly Momentum" section in both the Workout History and Habit History screens and provide details about what the feature represents.

## Proposed Changes

### [Performance Dashboard](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/PerformanceDashboardScreen.kt)

#### [MODIFY] [PerformanceDashboardScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/PerformanceDashboardScreen.kt)

- **Header Update**: Change the subtitle from `"MOMENTUM LOG"` to `"MONTHLY MOMENTUM"`.
- **Consistency Heatmap Card**:
    - Title: Unified to `"MONTHLY MOMENTUM"`.
    - Subtitle/Details: Add a descriptive subtitle to the card to explain the data.
        - Habit Context: *"Daily completion heat-map across all tracked habits. Darker shades indicate higher success rates."*
        - Workout Context: *"Volume-weighted frequency map. Darker shades indicate higher intensity or total volume per session."*

### [UI Components](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AnalyticsComponents.kt)

#### [MODIFY] [AnalyticsComponents.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AnalyticsComponents.kt)

- Update `DashboardCard` or add a version that supports a subtitle/description to clearly display the "feature details" requested by the user.

## Verification Plan

### Manual Verification
- Deploy the app.
- Navigate to **Habit History**:
    - Verify header: "MONTHLY MOMENTUM".
    - Verify Heatmap title: "MONTHLY MOMENTUM".
    - Verify Heatmap description: "Daily completion heat-map...".
- Navigate to **Workout History**:
    - Verify header: "MONTHLY MOMENTUM".
    - Verify Heatmap title: "MONTHLY MOMENTUM".
    - Verify Heatmap description: "Volume-weighted frequency map...".
