# Implementation Plan - Optimize Home Page Spacing

The goal is to minimize and standardize the margins and gaps on the Home Page to create a more compact and consistent UI. Specifically, we will reduce the excessive gap between the "Current Focus" label and the mood selection icons, and unify vertical/horizontal spacings throughout the screen.

## User Review Required

> [!NOTE]
> I am proposing to reduce the standard horizontal padding from `24.dp` to `20.dp` and vertical section spacing from `24.dp` to `16.dp`. This will make the UI feel tighter and show more content at once.

## Proposed Changes

### Home Screen UI

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/HomeScreen.kt)

- **Standardize Horizontal Padding**: Change all `24.dp` horizontal paddings to `20.dp`.
- **Reduce Vertical Section Gaps**: Change `Spacer(modifier = Modifier.height(24.dp))` between major sections to `Spacer(modifier = Modifier.height(16.dp))`.
- **Current Focus Section**:
    - Reduce the gap before "Current Focus" header from `24.dp` to `16.dp`.
    - Reduce the gap between "Current Focus" label and Mood Icons from `12.dp` to `6.dp`.
- **Other Spacing Adjustments**:
    - Reduce `Spacer(modifier = Modifier.height(12.dp))` before activity feed and advice cards to `8.dp`.
    - Standardize `DashboardPair` internal spacing to `12.dp` (from `16.dp`).
    - Adjust `Aura Header` bottom padding from `12.dp` to `8.dp`.

## Verification Plan

### Automated Tests
- Since this is a UI-only change, visual verification is primary. I will check for any layout overlaps or regressions.

### Manual Verification
- Deploy the app to the device/emulator.
- Verify the Home Page layout:
    - Check if "Current Focus" and Mood Icons are closer together.
    - Verify that overall margins are consistent (`20.dp`).
    - Ensure the vertical flow feels more compact but still readable.
