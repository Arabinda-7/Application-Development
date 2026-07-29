# Security Enhancement Plan

This plan aims to improve the app's security by protecting data at rest, adding biometric authentication, and preventing data leakage.

## User Review Required

> [!IMPORTANT]
> **Data Migration**: Migrating to `EncryptedSharedPreferences` and an encrypted `Room` database might require a one-time data conversion process to ensure existing user data is not lost.

> [!WARNING]
> **Biometric Availability**: Biometric authentication depends on hardware support. A fallback (PIN) will be maintained.

## Proposed Changes

### [Dependencies]

#### [MODIFY] [libs.versions.toml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/gradle/libs.versions.toml)
Add security, biometric, and SQLCipher libraries.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/build.gradle.kts)
Include the new dependencies.

### [Core Security]

#### [NEW] [SecurityManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/SecurityManager.kt)
Create a utility to handle `EncryptedSharedPreferences` and key management.

### [Data Persistence]

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
*   Replace standard `SharedPreferences` with `EncryptedSharedPreferences`.
*   Add logic to migrate existing plain-text preferences to encrypted ones.

#### [MODIFY] [WorkspaceDatabase.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/data/WorkspaceDatabase.kt)
*   Integrate `SQLCipher` to encrypt the Room database.
*   Generate/store a secure passphrase for the database in `EncryptedSharedPreferences`.

### [Authentication]

#### [MODIFY] [LockActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/LockActivity.kt)
*   Implement `BiometricPrompt` for fingerprint/face unlock.
*   Add a toggle in settings to enable/disable Biometric login.

#### [MODIFY] [ProfileSecurityHubSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProfileSecurityHubSection.kt)
*   Update the "App Access Lock" section to reflect Biometric support.

### [Data Leakage Prevention]

#### [MODIFY] [BaseActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/BaseActivity.kt)
*   Add a utility to set `FLAG_SECURE` based on a security setting to prevent screenshots and screen recording.

#### [MODIFY] [SettingsBackupHandler.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/SettingsBackupHandler.kt)
*   Encrypt the exported JSON backup using a user-provided password or a device-bound key.

## Verification Plan

### Automated Tests
*   Unit tests for `SecurityManager` to verify encryption/decryption.
*   Instrumentation tests to verify `EncryptedSharedPreferences` persistence.

### Manual Verification
*   Test App Lock with PIN and Biometrics.
*   Verify that `workspace_database` file cannot be opened with standard SQLite viewers without the key.
*   Check if screenshots are blocked when `FLAG_SECURE` is enabled.
*   Verify backup/restore works with encryption.
