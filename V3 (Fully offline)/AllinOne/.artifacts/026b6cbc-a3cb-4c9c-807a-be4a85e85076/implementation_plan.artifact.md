# Implementation Plan - 16 KB Page Size Compatibility Fix

The app is showing a compatibility warning on Android 15+ devices because some native libraries (`libandroidx.graphics.path.so` and `libsqlcipher.so`) are not aligned to 16 KB page sizes. This plan outlines the steps to upgrade these libraries and the build configuration to support 16 KB page sizes.

## User Review Required

> [!IMPORTANT]
> This update involves migrating from the legacy SQLCipher library (`android-database-sqlcipher`) to the modern one (`sqlcipher-android`). This is a major change that affects how the database is loaded and initialized. While I will handle the code changes, please verify that your encrypted database remains accessible after the update.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/gradle/libs.versions.toml)
- Update `composeBom` to `2026.06.01` (to get 16 KB aligned `graphics-path`).
- Replace legacy SQLCipher with the modern version:
    - Replace `net.zetetic:android-database-sqlcipher` with `net.zetetic:sqlcipher-android`.
    - Update version to `4.17.0`.
- Add explicit dependency for `androidx.graphics:graphics-path:1.1.0` if necessary, or ensure BOM handles it.

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/build.gradle.kts)
- Update SQLCipher dependency reference.
- Add `androidx.sqlite:sqlite` dependency as required by the new SQLCipher.

### Application Logic

#### [MODIFY] [AllInOneApplication.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AllInOneApplication.kt)
- Update imports from `net.sqlcipher.database.SQLiteDatabase` to `net.zetetic.database.sqlcipher.SQLiteDatabase`.
- Replace `SQLiteDatabase.loadLibs(this)` with `System.loadLibrary("sqlcipher")`.

#### [MODIFY] [AppDatabase.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/data/AppDatabase.kt)
- Update imports from `net.sqlcipher.database.SupportFactory` to `net.zetetic.database.sqlcipher.SupportOpenHelperFactory`.
- Replace `SupportFactory` with `SupportOpenHelperFactory`.

## Verification Plan

### Automated Tests
- Run `gradlew assembleDebug` to ensure the project builds correctly.
- If possible, I will check the alignment of the generated APK using the steps from the documentation (requires local tools).

### Manual Verification
- Deploy the app to a 16 KB page size emulator.
- Verify that the "Android App Compatibility" dialog no longer appears.
- Verify that the app can successfully open and read from the encrypted database.
