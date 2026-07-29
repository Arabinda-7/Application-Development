# Walkthrough - App Lock Recovery (Forgot PIN)

I have implemented a security recovery feature for the App Lock. Users can now set a security question and answer, which can be used to reset their PIN if they forget it.

## Changes Made

### Security Core & Data
- **DataManager**: Added `appLockQuestion` and `appLockAnswer` to store recovery credentials securely.
- **Persistence**: Updated save/load logic to ensure recovery info is kept across app restarts.

### Security UI & Flow
- **LockActivity**:
    - **Recovery Mode**: Added logic to verify security answers. If correct, the user is automatically transitioned to the PIN creation screen.
    - **Setup Integration**: After confirming a new PIN, users are now immediately prompted to select a security question and provide an answer.
    - **Forgot PIN? Option**: A new button appears on the authentication screen if recovery info is available.
    - **UI Improvements**: The recovery question now appears in a styled card that matches the app's theme.
    - **Custom Selection UI**: Replaced the standard Android Spinner with a custom selection dialog that matches the app's dark/glassy aesthetic. Users now tap a styled field to choose their security question from a themed list.
- **New Layouts**:
    - **activity_lock.xml**: Added a hidden recovery container that slides in when "Forgot PIN?" is clicked.
    - **dialog_recovery_setup.xml**: Created a custom dialog for selecting a question and entering an answer during setup.
- **Settings**:
    - **Retroactive Setup**: Existing users who already have a PIN but no recovery question will see a "Set Security Question" option in the Security settings.

## Verification Results

### Automated Tests
- Successfully ran `app:assembleDebug`.

### Manual Verification Steps
1. **Initial Setup**:
   - Go to Settings > Security.
   - Enable App Lock and set a PIN.
   - **Verify**: A dialog should appear asking for a Security Question and Answer.
2. **Forgot PIN Scenario**:
   - Close the app and re-open to trigger the lock screen.
   - Click **Forgot PIN?**.
   - **Verify**: The keypad fades, and the security question is displayed.
   - Enter the correct answer and click **Verify & Unlock**.
   - **Verify**: You should be prompted to create a **NEW PIN**.
3. **Invalid Answer**:
   - Try entering a wrong answer in the recovery screen.
   - **Verify**: An "Incorrect Answer" toast appears and access remains denied.

> [!CAUTION]
> If a user has not set a security question (e.g., from a very old version) and forgets their PIN, they will be locked out. Encourage users to set their recovery question via Settings.
