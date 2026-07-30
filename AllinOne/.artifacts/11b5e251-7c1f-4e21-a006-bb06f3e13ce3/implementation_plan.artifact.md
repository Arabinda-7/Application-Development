# Implementation Plan - Fix OLED Theme Visual Glitch in Profile

The user reported a bug where enabling the "OLED Theme" in the Profile page causes other toggles in the "Lock & Privacy" section to appear as if they are also "ON".

## Analysis
Investigation reveals that `ProfileSecurityHubSection.kt` has an `applyTint` method that overrides the `thumbTintList` of all switches with a single accent color (the "mood color"). This override uses a fixed `ColorStateList` that does not differentiate between the "checked" and "unchecked" states. On a pure black background (OLED mode), a bright accent-colored switch thumb looks "active" even when it is in the OFF position, leading to the reported confusion.

## Proposed Changes

### [MODIFY] [ProfileSecurityHubSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProfileSecurityHubSection.kt)
- Update `applyTint` to create a dynamic `ColorStateList` for the switch thumb and track.
- The `ColorStateList` will use the accent color only when the switch is `checked`.
- The "unchecked" state will use a neutral grey (`#9E9E9E` for thumb and a darker grey for track) to provide clear visual feedback that the switch is OFF.

### [MODIFY] [ProfileActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProfileActivity.kt)
- Update `setupHeaderBackground` to dynamically set the background color of the `Impact Summary` and `Security Hub` cards.
- In OLED mode, these cards will be set to `Color.BLACK` to provide a "true black" experience and improve contrast with the neutral grey of inactive switches.

## Verification Plan

### Manual Verification
1.  Launch the app and navigate to the **Profile** page.
2.  Go to the **Lock & Privacy** section.
3.  Ensure some toggles (e.g., App Access Lock) are **OFF**.
4.  Toggle the **OLED Theme** to **ON**.
5.  **Verify**: The OFF switches should now show a grey thumb and track, clearly indicating they are inactive, instead of appearing tinted with the accent color.
6.  **Verify**: The card backgrounds should blend seamlessly with the black background.
7.  Toggle the **OLED Theme** to **OFF** and verify the dark theme visuals still look correct.
