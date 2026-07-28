# Notification Color Customization Plan

This plan updates the "Today's Agenda" notifications to use colors based on their specific section or item color, with logic to ensure visual distinction between items.

## User Review Required

> [!NOTE]
> I will implement a "Contrast Fallback" logic: If an item has the same color as the item immediately preceding it in the list, I will use the app's main accent color as a fallback to ensure they are visually distinct.

## Proposed Changes

### 1. Data Model Update
*   **[MODIFY] [AgendaItem.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AgendaItem.kt)**: Add a `color: Int` field (defaulting to -1).

### 2. Color Extraction Logic
*   **[MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)**: Update `getComprehensiveTodayAgenda` to fetch colors for each item:
    *   **TASKS**: Use `task.color`. If -1, use `globalTaskColor`.
    *   **PROJECTS**: Use `project.color`. If -1, use `globalProjectColor`.
    *   **SUBFEATURES**: Use parent project's color.
    *   **WORKSPACES**: Use the specific entity's color (Project/Goal).

### 3. UI Implementation
*   **[MODIFY] [HomeScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/home/HomeScreen.kt)**:
    *   Use `item.color` for the icon and tag background instead of the generic `style.accentColor`.
    *   Implement logic to check if `item.color` is identical to the previous item's color. If so, apply a fallback color (app accent) for that specific item to satisfy the "show in other color" requirement.

## Verification Plan

### Manual Verification
*   Open Home page notifications.
*   Verify that Tasks show their specific colors (or the Task section color).
*   Verify that Projects show their specific colors.
*   Create two tasks with the exact same color for today.
*   Confirm that the second one uses a different color (Contrast Fallback) to remain distinct.
