# Implementation Plan - Onboarding Refinement

Refine the onboarding experience with validation, layout adjustments, and dot-based navigation.

## User Review Required

> [!IMPORTANT]
> - **Validation:** Users will be blocked from navigating beyond the Profile Page (index 1) until a Display Name is provided. This applies to both the "Continue" button and swipe gestures.
> - **Dot Navigation:** Dot navigation will be enabled. To maintain validation, clicking a dot for a future page will be restricted if the current state is invalid.

## Proposed Changes

### [Onboarding Activity]

#### [MODIFY] [OnboardingActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/OnboardingActivity.kt)
- Implement validation logic to prevent swiping past the Profile page if the username is blank.
- Update `OnboardingFooter` call to handle dot navigation clicks and pass validation state.
- Disable the "Continue" button on the Profile page if the name is blank.

### [Onboarding Pages]

#### [MODIFY] [OnboardingPages.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/onboarding/OnboardingPages.kt)
- Add a 4dp top gap to all onboarding pages (`ProfilePage`, `OverviewPage`, `GlobalHubPage`, `FeatureDeepDivePage`, `ActivationPage`).

### [Onboarding Components]

#### [MODIFY] [OnboardingComponents.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/onboarding/OnboardingComponents.kt)
- Update `OnboardingFooter` to make progress dots clickable for navigation.
- Ensure dots only navigate if the validation allows.

## Verification Plan

### Manual Verification
- **Validation:**
    - Reach the Profile page.
    - Try to swipe forward with a blank name (should be blocked or snapped back).
    - Try to click "Continue" with a blank name (button should be disabled or show error).
    - Enter a name and verify navigation is unlocked.
- **Layout:**
    - Verify the 4dp top gap on all pages.
- **Dot Navigation:**
    - Click dots to jump between pages.
    - Verify that jumping back from the last page works as requested.
