# Walkthrough - Fixed Gradle Daemon and Build Configuration Errors

I have resolved the Gradle daemon startup error and identified a follow-up environment variable conflict that was preventing your build from completing.

## Changes Made

### Build Configuration

#### [gradle.properties](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/gradle.properties)
- Added `org.gradle.java.home` to point to a working Java 21 installation (`C:/Program Files/Android/Android Studio1/jbr`). This fixed the "daemon terminated unexpectedly" error caused by a broken JBR path in the default Android Studio folder.

## Verification Results

### Automated Tests
- Ran `./gradlew help` which now successfully connects to the daemon and initializes the project.
- Verified that unsetting the `ANDROID_PREFS_ROOT` environment variable allows the build to complete successfully.

## Next Steps for You

> [!IMPORTANT]
> **Action Required:** To fully resolve the build failure, you must remove the redundant `ANDROID_PREFS_ROOT` environment variable from your system settings.
>
> The Android Gradle Plugin (AGP) is reporting a conflict because both `ANDROID_PREFS_ROOT` and `ANDROID_USER_HOME` are set to the same path. AGP recommends using only `ANDROID_USER_HOME`.
>
> **How to remove it on Windows:**
> 1. Search for "Edit the system environment variables" in the Start menu.
> 2. Click **Environment Variables**.
> 3. Under "User variables", find `ANDROID_PREFS_ROOT`, select it, and click **Delete**.
> 4. Restart Android Studio (or your terminal) for the change to take effect.
