# User Profile & System Settings Refactoring Plan

This plan outlines the extraction of user identity, statistics, and system-wide configuration logic from `DataManager.kt` into a Clean Architecture structure.

## Proposed Changes

### Domain Layer

#### [NEW] [UserRepository.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/repository/UserRepository.kt) [ALREADY EXISTS]
Interface for user profile (identity, XP) and system settings (theme, security, display).

#### [NEW] [User Use Cases](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/usecase/user/UserUseCases.kt) [ALREADY EXISTS]
- `GetUserProfileUseCase`: Observe name, bio, avatar, and stats.
- `AddXPUseCase`: Encapsulates the leveling logic (XP -> Level conversion).
- `GetUserSettingsUseCase`: Observe app theme, font size, and section visibility.
- `UpdateUserSettingsUseCase`: Unified logic for updating system preferences.
- `AddActivityUseCase`: Logging recent activities.

---

### Data Layer

#### [NEW] [UserLocalDataSource.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/datasource/UserLocalDataSource.kt) [ALREADY EXISTS]
Centralizes the mapping between `SharedPreferences` keys and domain models. Handles encryption for security settings.

#### [NEW] [UserRepositoryImpl.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/repository/UserRepositoryImpl.kt) [ALREADY EXISTS]
Implementation using the local data source and handling profile entity mapping.

---

### Dependency Injection

#### [NEW] [UserModule.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/di/UserModule.kt) [ALREADY EXISTS]
Hilt module for User-related dependencies.

---

### Presentation Layer

#### [MODIFY] [ProfileViewModel.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProfileViewModel.kt)
Migrate from `DataManager` to Use Cases.

#### [MODIFY] [SettingsViewModel.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/SettingsViewModel.kt)
Migrate from `DataManager` to Use Cases.

#### [MODIFY] [ProfileActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProfileActivity.kt)
Add `@AndroidEntryPoint` and use injected ViewModel.

#### [MODIFY] [SettingsActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/SettingsActivity.kt)
Add `@AndroidEntryPoint` and use injected ViewModel.

---

## Function Migration Map

| DataManager Function | New Location |
| :--- | :--- |
| `addXP()` | `AddXPUseCase` |
| `userXP` / `userLevel` | `GetUserProfileUseCase` |
| `appThemeMode` / `appAccentColor` | `GetUserSettingsUseCase` |
| `isAppLockEnabled` / `appLockPin` | `GetUserSettingsUseCase` |
| `showHabitSection` / `showTaskSection` | `GetUserSettingsUseCase` |

---

## Verification Plan

### Automated Tests
- Unit tests for `AddXPUseCase` in `UserRepositoryImpl` (already implemented).

### Manual Verification
- Verify profile changes (name, avatar) reflect immediately.
- Test theme switching and app lock functionality.
- Confirm home screen section visibility updates correctly.
