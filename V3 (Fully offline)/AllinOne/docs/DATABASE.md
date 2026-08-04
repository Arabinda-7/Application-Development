# Database Documentation - Room & SQLCipher Encryption

## 🗄️ Database Architecture

The **AllinOne** database layer is powered by **Room Database** integrated with **SQLCipher** for hardware-backed, encrypted local storage.

- **Database Class**: `com.example.allinone.data.database.AppDatabase`
- **Database Name**: `all_in_one_db`
- **Current Version**: `14`
- **Encryption**: SQLCipher via `SupportOpenHelperFactory`

---

## 📊 Database Entities & Schema Table Overview

| Table Name | Entity Class | Primary Key | Description |
|---|---|---|---|
| `global_tasks` | `GlobalTaskEntity` / `TaskEntity` | `id` (UUID String) | Categorized to-do list tasks, priorities, subtasks JSON. |
| `habits` | `HabitEntity` | `id` (UUID String) | Habit tracking, streaks, target days, completed dates JSON. |
| `workouts` | `WorkoutEntity` | `id` (UUID String) | Workout routines, exercise adapters, set metrics. |
| `global_notes` | `GlobalNoteEntity` | `id` (UUID String) | Notebook notes, pinned states, markdown contents. |
| `transactions` | `TransactionEntity` | `id` (UUID String) | Financial transactions, income/expense tags, category IDs. |
| `personal_ledgers` | `PersonalLedgerEntity` | `id` (UUID String) | Person-to-person debt and credit ledger accounts. |
| `ledger_entries` | `LedgerEntryEntity` | `id` (UUID String) | Individual debit/credit entries linked to ledgers. |
| `projects` | `ProjectEntity` | `id` (UUID String) | High-level project boards, weighted progress, health status. |
| `goals` | `GoalEntity` | `id` (UUID String) | Sub-goals and milestones linked via Foreign Key to `projects`. |
| `workspace_notes` | `WorkspaceNoteEntity` | `id` (UUID String) | Project-specific notes linked via Foreign Key to `projects`. |
| `tasks` | `WorkspaceTaskEntity` | `id` (UUID String) | Project-specific milestone tasks. |
| `features` | `FeatureEntity` | `id` (UUID String) | Project feature backlog items. |
| `bugs` | `BugEntity` | `id` (UUID String) | Defect and issue tracking entries. |
| `ideas` | `IdeaEntity` | `id` (UUID String) | Mind-map ideas and creative branches. |
| `resources` | `ResourceEntity` | `id` (UUID String) | Reference documentation links and assets. |
| `ai_chats` | `AiChatEntity` | `id` (UUID String) | Offline AI Assistant conversation chat messages. |
| `ai_chat_sessions` | `AiChatSessionEntity` | `id` (UUID String) | AI Assistant chat sessions and metadata. |
| `assistant_memory` | `AssistantMemoryEntity` | `id` (UUID String) | Long-term offline AI memory slots and key-value attributes. |

---

## 🔒 Security & SQLCipher Encryption

Database passphrases are dynamically generated and protected using Android `EncryptedSharedPreferences` and KeyStore through `SecurityManager`:

```kotlin
val passphrase = SecurityManager.getDatabasePassphrase(context).toByteArray()
val factory = SupportOpenHelperFactory(passphrase)

val db = Room.databaseBuilder(context, AppDatabase::class.java, "all_in_one_db")
    .openHelperFactory(factory)
    .fallbackToDestructiveMigration()
    .build()
```

---

## 🔑 Type Converters (`WorkspaceTypeConverters.kt`)

Room custom type converters handle complex serialization for non-primitive attributes:
- `List<String>` $\leftrightarrow$ JSON String
- `List<Subtask>` $\leftrightarrow$ JSON String
- `Map<String, String>` $\leftrightarrow$ JSON String
