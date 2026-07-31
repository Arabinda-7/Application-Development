# Implementation Plan - Fix Invalid Gradle Java Home

The project is failing to build because the `org.gradle.java.home` property in `gradle.properties` points to a non-existent directory: `C:/Program Files/Android/Android Studio1/jbr`.

## User Review Required

> [!IMPORTANT]
> The property `org.gradle.java.home` was likely added to resolve a previous issue with the Gradle daemon. However, the path used contains a typo (`Android Studio1` instead of `Android Studio`).

## Proposed Changes

### Build Configuration

#### [MODIFY] [gradle.properties](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/gradle.properties)
- Correct the `org.gradle.java.home` path to point to the valid JetBrains Runtime (JBR) directory: `C:/Program Files/Android/Android Studio/jbr`.

## Verification Plan

### Automated Tests
- Run `./gradlew help` to ensure the Gradle daemon starts successfully with the new path.
- Trigger a build/sync in Android Studio.

### Manual Verification
- Verify that the error "Java home supplied is invalid" no longer appears in the Build output.
