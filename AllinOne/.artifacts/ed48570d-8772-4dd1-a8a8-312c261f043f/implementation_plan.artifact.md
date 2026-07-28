# Implementation Plan - Onboarding Flow Enhancements

This plan addresses three requests regarding the onboarding flow:
1. Prevent swiping forward from the Profile page if the user hasn't entered their name.
2. Fix the "peeking" bug where users could briefly see the next page before being snapped back.
3. Increase the top margin on onboarding pages from 4dp to 6dp.

## User Review Required

> [!IMPORTANT]
> The solution to prevent swiping forward involves dynamically adjusting the number of pages in the pager. When the name is not filled, the pager will effectively "end" at the Profile page. This prevents the user from swiping forward or even "peeking" at the next page, while still allowing them to swipe back to the Overview page.

## Proposed Changes

### [Onboarding Logic]

#### [MODIFY] [OnboardingActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/OnboardingActivity.kt)
- Update the `pages` list calculation to only include pages after `PROFILE` if `userName` is not blank.
- Remove the `LaunchedEffect` that attempted to snap the user back, as the dynamic page list makes it redundant and more robust.
- Ensure the `profilePageIndex` is correctly identified.

### [UI Components]

#### [MODIFY] [OnboardingPages.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/onboarding/OnboardingPages.kt)
- Update `Spacer(modifier = Modifier.height(4.dp))` to `6.dp` at the top of all onboarding pages:
    - `ProfilePage`
    - `OverviewPage`
    - `GlobalHubPage`
    - `FeatureDeepDivePage`
    - `ActivationPage`

## Verification Plan

### Automated Tests
- Build the project to ensure no regressions in the Compose UI.

### Manual Verification
- **Swiping Restriction**:
    1. Start onboarding.
    2. Go to the Profile page.
    3. Try to swipe forward without entering a name. Verify that it is impossible to swipe forward and no "peeking" of the Global Hub occurs.
    4. Verify that you can still swipe back to the Overview page.
    5. Enter a name. Verify that swiping forward is now possible.
- **UI Gaps**:
    1. Check the top of each onboarding page to verify the increased spacing (6dp).
