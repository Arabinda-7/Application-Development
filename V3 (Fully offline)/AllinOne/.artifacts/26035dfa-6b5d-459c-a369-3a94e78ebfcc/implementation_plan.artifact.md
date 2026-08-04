# Onboarding Enhancement Plan

Suggesting and implementing a more personalized and goal-oriented onboarding experience for the "All in One" app.

## Proposed Changes

### [Onboarding]

#### [MODIFY] [OnboardingModels.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/onboarding/OnboardingModels.kt)
- Add new `OnboardingPageType` values: `APPEARANCE` and `JOURNEY`.

#### [MODIFY] [OnboardingPages.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/onboarding/OnboardingPages.kt)
- [NEW] Implement `AppearancePage` composable:
    - Allows choosing the system accent color from a predefined set of "Energy Colors".
    - Allows toggling between Theme Modes (Dark, Light, OLED).
    - Previews the changes in real-time.
- [NEW] Implement `JourneyPage` composable:
    - Displays the `predefinedJourneys` from `DataManager`.
    - Allows the user to select a "Starting Mission".
    - Selecting a journey will automatically enable the relevant modules in the Global Hub.

#### [MODIFY] [OnboardingActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/OnboardingActivity.kt)
- Add state variables for `selectedThemeMode` and `selectedAccentColor`.
- Add state for `selectedJourneyId`.
- Update the `pages` list logic to include `APPEARANCE` (after Profile) and `JOURNEY` (before Global Hub).
- Update `completeOnboarding` to save the new theme settings and initialize the selected journey if any.

#### [MODIFY] [OnboardingComponents.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/onboarding/OnboardingComponents.kt)
- [NEW] Add a `ThemeOption` component for the Appearance page.
- [NEW] Add a `JourneyCard` component for the Journey selection page.

## Verification Plan

### Manual Verification
1.  Run the app and clear data to trigger onboarding.
2.  Verify the new **Appearance** page correctly updates the preview colors and theme.
3.  Verify the **Journey** page displays the missions and allows selection.
4.  Complete onboarding and verify that:
    -   The app's accent color matches the selection.
    -   The selected journey's modules are enabled in the dashboard.
    -   The user name and avatar are still correctly saved.
