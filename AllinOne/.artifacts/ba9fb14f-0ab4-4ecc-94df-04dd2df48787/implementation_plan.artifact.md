# Implementation Plan - Workspace Bug Tracking Enhancements

This plan aims to transform the basic bug tracker into a robust, developer-centric bug management system within the Workspace.

## Proposed Features

### 1. **Enhanced "Add Bug" Experience**
- **Priority Level**: Separate from Severity (e.g., a "Minor" severity bug might be "High" priority if it blocks a release).
- **Environment Context**: Metadata for `Environment` (Dev, Beta, Production) and `App Version`.
- **Steps to Reproduce UI**: A more structured input area or auto-formatting for reproduction steps.

### 2. **Advanced Bug Dashboard**
- **Bug Kanban Board**: Visual workflow transitions: `Open` → `Confirmed` → `Fixing` → `Fixed` → `Verified`.
- **Severity Heatmap**: Color-coded card borders or backgrounds based on severity:
    - **Critical**: Pulsing Red Glow.
    - **High**: Solid Red.
    - **Medium**: Orange/Amber.
    - **Low**: Green/Teal.
- **Smart Filtering**: Quickly filter bugs by Severity, Priority, or Status.

### 3. **Developer Insights**
- **Linked Tasks**: Ability to see which Workspace Task is tracking the actual fix.
- **Assigned Fixer**: Visual indicator of which "Person" (from the Ledger/People section) is fixing the bug.

---

## Proposed Changes

### Data Layer
#### [MODIFY] [Entities.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/workspace/data/Entities.kt)
- Add `priority` (Int), `environment` (String), and `version` (String) fields to `BugEntity`.

### UI Components
#### [MODIFY] [ProjectWorkspaceScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/ProjectWorkspaceScreen.kt)
- **Update `WorkspaceCreationScreen`**: Add inputs for Priority and Environment.
- **Revamp `BugTracker`**:
    - Replace the simple list with a multi-column Kanban board or an advanced "Bug List" with badges.
    - Implement `BugCard` with severity color coding and quick actions (e.g., "Confirm Bug").
- **Bug Filters**: Add a row of chips at the top of the Bug section for filtering.

---

## User Review Required

> [!IMPORTANT]
> Do you prefer a **Kanban Board** (columns for each status) or an **Advanced List** (single list with detailed badges and sorting) for the Bug section?

> [!NOTE]
> I can also add a feature to **Auto-Generate a Task** when a bug is marked as "Confirmed." Would you like this automation?

## Verification Plan

### Automated Tests
- Run `gradlew app:assembleDebug` to verify Room entity changes and Compose layout stability.

### Manual Verification
1.  **Add Bug**: Create a bug with the new Priority and Environment fields.
2.  **View Dashboard**: Observe the color-coding for different severities.
3.  **Transition**: Move a bug from "Open" to "Fixed" and verify the status updates in the database.
