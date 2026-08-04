# Implementation Plan - Root Cause Analysis & Entity Unification

The `MissingType` error indicates that KSP cannot resolve a symbol referenced in `AppDatabase`. This is often caused by duplicate class names in different packages, star imports, or broken symbol resolution in the K2 compiler.

## Proposed Changes

### 1. Unique Entity Naming
To eliminate resolution ambiguity, I will rename the entities that have duplicate names.
*   `com.example.allinone.workspace.data.NoteEntity` -> `WorkspaceNoteEntity`
*   `com.example.allinone.workspace.data.TaskEntity` -> `WorkspaceTaskEntity`
*   `com.example.allinone.data.database.NoteEntity` -> `GlobalNoteEntity`
*   `com.example.allinone.data.database.TaskEntity` -> `GlobalTaskEntity`

### 2. Cleanup star imports and ambiguity
*   Remove all `import ...*` in Room-related files.
*   Use fully qualified names in `AppDatabase` entities array to be 100% explicit.

### 3. Stability Reset
*   Disable KSP 2 temporarily and use KSP 1 (K1) to see if it provides better error messages or bypasses the K2 crash.
*   Revert `room.generateKotlin` to `false` for maximum compatibility during debugging.

## Verification Plan

### Manual Verification
1.  **Daemon Kill**: `./gradlew --stop`
2.  **Clean**: `./gradlew clean`
3.  **Step-by-Step Enablement**:
    *   Start with `AppDatabase` having 0 entities and 0 DAOs.
    *   Add `WorkspaceDao` and its entities.
    *   Add Global DAOs and their entities one by one.
    *   Identify which entity/DAO triggers the `MissingType` error.
4.  **Logging**: Use `./gradlew :app:kspDebugKotlin --info --stacktrace` at each step.
