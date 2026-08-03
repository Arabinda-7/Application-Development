# Contribution Guidelines - AllinOne Project

Thank you for your interest in contributing to **AllinOne**! Please follow these guidelines to maintain high code quality, security, and architectural consistency.

---

## 🛠️ Environment & Prerequisites

1. **Android Studio**: Android Studio Koala (2024.1.1+) or newer.
2. **JDK**: JDK 17 / Kotlin 2.0+.
3. **Android SDK**: API 26 (Android 8.0) minimum, API 34+ target.

---

## 📐 Architectural Rules

1. **Clean Feature Separation**: Feature code must reside inside `feature/<feature_name>/` containing `ui/`, `viewmodel/`, `domain/`, and `data/`.
2. **Feature Isolation**: Never import or reference another feature package directly. Use core abstractions or domain interfaces.
3. **Entity vs Model Separation**: Room `@Entity` classes must live in `data/local/entity/` and pure domain models in `domain/model/`.
4. **Hilt Dependency Injection**: Always use `@HiltViewModel` for ViewModels and `@Inject constructor()` for repositories, use cases, and mappers.
5. **Privacy & Offline First**: No remote HTTP calls or cloud NLP analytics. All processing must remain 100% on-device.

---

## 🧪 Testing Guidelines

1. **Unit Tests**: Place unit tests under `app/src/test/java/com/example/allinone/` extending `RepositoryTestTemplate`, `UseCaseTestTemplate`, or `AssistantTestTemplate`.
2. **Frameworks**: Use **JUnit 5**, **MockK**, and `kotlinx.coroutines.test`.
3. **Verification Command**:
   ```bash
   ./gradlew test
   ./gradlew :app:kspDebugKotlin
   ```

---

## 📝 Commit & Pull Request Workflow

1. **Branch Naming**:
   - `feature/feature-name` (e.g., `feature/voice-date-parsing`)
   - `fix/bug-description` (e.g., `fix/habit-streak-counter`)
2. **Commit Messages**: Follow standard semantic commits:
   - `feat(assistant): add natural date parser`
   - `fix(task): resolve completion toggle bug`
   - `refactor(di): add Hilt DatabaseModule`
