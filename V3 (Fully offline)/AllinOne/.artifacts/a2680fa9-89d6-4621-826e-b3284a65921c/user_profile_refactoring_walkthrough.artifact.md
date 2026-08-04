# User Profile & Security Refactoring Walkthrough

The User Profile, Security settings, and Appearance configurations have been extracted from `DataManager.kt` and refactored into a Clean Architecture structure.

## Changes Made

### 1. Domain Layer
- **[UserRepository](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/repository/UserRepository.kt)**: Defined an interface for User data and settings.
- **Models**:
    - `UserProfile`: Contains XP, Level, Name, Bio, Avatar, Recent Activities, and Moods.
    - `UserSettings`: Contains Appearance (Theme, Font, Size), Visibility sections, and Security (App Lock, Biometrics).
- **UseCases**: Created targeted use cases in `com.example.allinone.domain.usecase.user`:
    - `GetUserProfileUseCase`
    - `UpdateUserProfileUseCase`
    - `AddXPUseCase`: Encapsulates leveling logic.
    - `GetUserSettingsUseCase`
    - `UpdateUserSettingsUseCase`
    - `AddActivityUseCase`

### 2. Data Layer
- **[UserLocalDataSource](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/datasource/UserLocalDataSource.kt)**: Manages persistence via Encrypted SharedPreferences.
- **[UserRepositoryImpl](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/repository/UserRepositoryImpl.kt)**: Implements the domain repository.

### 3. Dependency Injection
- **[UserModule](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/di/UserModule.kt)**: Provides Hilt bindings for the User repository.

### 4. Legacy Integration
- Updated **[DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)** to inject `UserRepository` and sync legacy `UserDataManager` and `NotificationDataManager` singletons.
- Fixed numerous unresolved references and conflicting declarations in `DataManager.kt` caused by the progressive refactoring.

## Verification
- Verified that Leveling logic is correctly encapsulated in `UserRepositoryImpl.addXP`.
- Verified that reactive settings flow ensures theme and appearance changes are reflected across the app.
- Security flags (App Lock, Biometrics) are now managed through a unified repository.
