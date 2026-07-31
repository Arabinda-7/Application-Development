# Walkthrough - Fixed Invalid Gradle Java Home

I have corrected the `org.gradle.java.home` property which was pointing to a non-existent directory, causing the build to fail immediately.

## Changes Made

### Build Configuration

#### [gradle.properties](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/gradle.properties)
- Updated `org.gradle.java.home` from `C:/Program Files/Android/Android Studio1/jbr` to `C:/Program Files/Android/Android Studio/jbr`.

## Verification Results

### Automated Tests
- Ran `./gradlew help`.
- **Result:** The "Java home supplied is invalid" error is **resolved**. Gradle now successfully initializes and starts the configuration phase.

## Current Status & Next Steps

While the Java home error is fixed, the build is now encountering a secondary environment variable conflict:

> [!CAUTION]
> **Environment Variable Conflict Detected**
> The build is currently failing with: `Failed to create service ... AndroidLocationsBuildService`.
> This is because both `ANDROID_PREFS_ROOT` and `ANDROID_USER_HOME` are set to `C:\Users\arabi\.android`.
>
> **Action Required:**
> 1. Open **System Environment Variables**.
> 2. Delete the `ANDROID_PREFS_ROOT` variable.
> 3. Restart Android Studio.
