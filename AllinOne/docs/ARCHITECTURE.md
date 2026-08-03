# Architecture Documentation - AllinOne Android Application

## 📐 System Architecture Overview

The **AllinOne** application follows **Clean Architecture** principles combined with **MVVM (Model-View-ViewModel)** and **Hilt Dependency Injection**. The codebase is organized into a **Feature-Based Architecture** layout, isolating distinct domain modules (`task`, `habit`, `finance`, `workout`, `projects`, `notes`, `assistant`) while sharing infrastructure through a common `core/` package.

---

## 🏛️ Layer Responsibilities

```
                                    +----------------------------------+
                                    |         UI Layer                 |
                                    |  (Composables, Activities,       |
                                    |   RecyclerView Adapters)         |
                                    +----------------------------------+
                                                     |
                                                     v
                                    +----------------------------------+
                                    |       ViewModel Layer            |
                                    |  (StateFlow, Hilt @HiltViewModel)|
                                    +----------------------------------+
                                                     |
                                                     v
                                    +----------------------------------+
                                    |       Domain Layer               |
                                    |  (Use Cases, Domain Models,      |
                                    |   Repository Interfaces)         |
                                    +----------------------------------+
                                                     ^
                                                     |
                                    +----------------------------------+
                                    |         Data Layer               |
                                    |  (Repository Impls, Room DAOs,   |
                                    |   Entities, Mappers, DataStore)  |
                                    +----------------------------------+
```

### 1. UI Layer (`feature/<name>/ui/`)
- Contains Jetpack Compose screens, XML layouts, Activities, and RecyclerView Adapters.
- Observes `StateFlow` streams exposed by ViewModels.
- Has zero direct dependency on Room entities or database queries.

### 2. ViewModel Layer (`feature/<name>/viewmodel/`)
- Annotated with `@HiltViewModel`.
- Injects Use Cases and Repository interfaces via Hilt `@Inject constructor`.
- Exposes immutable state using `StateFlow` and `asStateFlow()`.

### 3. Domain Layer (`feature/<name>/domain/`)
- **Domain Models**: Pure Kotlin data classes representing business entities (`Task`, `Habit`, `Workout`, `Note`, `Transaction`, `Project`).
- **Use Cases**: Single-responsibility domain operations (`GetTasksUseCase`, `AddTaskUseCase`, `UpdateUserSettingsUseCase`).
- **Repository Interfaces**: Abstract contracts defined by the domain (`TaskRepository`, `HabitRepository`, `UserRepository`).

### 4. Data Layer (`feature/<name>/data/`)
- **Entities**: Room `@Entity` data classes (`TaskEntity`, `HabitEntity`, `WorkoutEntity`, `ProjectEntity`).
- **DAOs**: Room Database Access Objects (`AppTaskDao`, `AppHabitDao`, `WorkspaceDao`).
- **Mappers**: Bi-directional conversion classes (`TaskMapper`, `HabitMapper`) converting between Entities and Domain Models.
- **Repository Implementations**: Concrete classes (`TaskRepositoryImpl`, `UserRepositoryImpl`) implementing domain interfaces.

---

## 🔄 Data Flow Pipeline

1. **User Action**: The user interacts with a Compose Button or Activity view in `ui/`.
2. **ViewModel Event**: The UI invokes a method on `ViewModel`.
3. **Use Case Execution**: The ViewModel delegates execution to a domain `UseCase`.
4. **Repository Invocation**: The Use Case calls the `Repository` interface.
5. **Database Operation & Mapping**: The `RepositoryImpl` fetches/modifies data in Room via `DAO`, mapping Room `Entity` to `DomainModel` via `Mapper`.
6. **State Flow Emission**: Updated data flows back up to the `ViewModel` as a `StateFlow`, updating the `UI` reactively.

---

## 📁 Repository Directory Hierarchy (`com.example.allinone`)

```
com.example.allinone/
├── core/                                 # Shared Infrastructure, Database, Security & Utils
│   ├── database/                         # AppDatabase & TypeConverters
│   ├── security/                         # SecurityManager & Encryption
│   ├── ui/                               # Shared Jetpack Compose design components
│   └── utils/                            # UIUtils, DateUtils, SwipeGestures
│
├── di/                                   # Hilt DI Modules
│   ├── DatabaseModule.kt                 # Room DB, DAOs, DataStore, Gson
│   ├── RepositoryModule.kt               # Repository Interface Bindings
│   ├── UseCaseModule.kt                  # Domain UseCase Providers
│   └── AssistantModule.kt                # AI Assistant Engine & Voice Handlers
│
└── feature/                              # Feature-First Modules
    ├── task/                             # Task Feature (ui, viewmodel, domain, data)
    ├── habit/                            # Habit Feature (ui, viewmodel, domain, data)
    ├── finance/                          # Finance Feature (ui, viewmodel, domain, data)
    ├── workout/                          # Workout Feature (ui, viewmodel, domain, data)
    ├── projects/                         # Projects Feature (ui, viewmodel, domain, data)
    ├── notes/                            # Notes Feature (ui, viewmodel, domain, data)
    └── assistant/                        # AI Assistant Feature (ui, viewmodel, domain, data)
```
