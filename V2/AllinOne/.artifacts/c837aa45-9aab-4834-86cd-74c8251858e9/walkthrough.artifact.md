# Walkthrough - Secure Security Question Modification

I have added a security layer that requires the app PIN before a user can set or change their security question.

## Changes Made

### 1. New Verification Mode in LockActivity
- Added `MODE_VERIFY_FOR_RECOVERY` to [LockActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/LockActivity.kt).
- When in this mode, the user must enter their correct PIN. Upon success, the UI transitions to the recovery setup screen.
- Updated headers and logic to handle this new flow securely.

### 2. Enhanced Security Settings UI
- Modified [SettingsActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/SettingsActivity.kt) to include a "Change Security Question" option if one is already set.
- Both "Set Security Question" and "Change Security Question" now launch the PIN verification screen first.

## Verification Results

- [x] Verified that clicking "Change Security Question" prompts for a PIN.
- [x] Verified that an incorrect PIN blocks access to recovery setup.
- [x] Verified that a correct PIN allows access to recovery setup.
- [x] Verified that initial PIN setup still leads to recovery setup automatically (without asking for PIN again).

> [!IMPORTANT]
> This change ensures that even if someone leaves the app open on the settings screen, they cannot compromise the recovery method without knowing the existing PIN.
