# Implementation Plan - Standardize Project Sub-feature UI & Behavior

standardize the user interface and interaction model for project milestones (sub-features) across Project View, Project Edit, and Ideas.

## User Review Required

> [!IMPORTANT]
> **Behavior Change**: Clicking a milestone in the **Roadmap Edit** (`AddProjectActivity`) or **Ideas section** will now toggle its description/details (`tvNote`). To open the full editor, you must tap the **Pencil icon** on the right. This unifies the behavior across the entire app.

## Proposed Changes

### [Component: UI Unification]

#### [MODIFY] [ProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProjectActivity.kt)
- **Project View Spacing**: In `createSubFeatureViewItem`, add a bottom margin of `12.dpToPx()`. This gives items more breathing room.
- **Category Tags**: Add right-aligned category tags (UI, LOGIC, BUG) to every milestone item in the View and Ideas section.
- **Ideas Milestone Editor**: Update the "Pencil" click listener in the Idea dialog to open the full-screen `AddSubFeatureActivity` instead of a small dialog. This provides the "Advanced" features (Weight, Urgency, Blocked By) to ideas as well.
- **Menu Unification**: Ensure long-press always opens the "Mark/Delete" professional menu.

#### [MODIFY] [AddProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddProjectActivity.kt)
- **Standard Layout**: Update `createSubFeatureItem` to include category tags on the right and a `12.dpToPx()` vertical gap.
- **Behavior Sync**: Change single tap to toggle the description visibility. Use the Pencil icon as the trigger for the full editor.

---

## Verification Plan

### Manual Verification
1.  **Project Roadmap View**: Open a project's details. Verify items have 12dp spacing and show tags (e.g., "LOGIC") on the right.
2.  **Project Roadmap Edit**: Go to edit a project. Verify that clicking a task name expands its details, and the category tag is visible. Verify the pencil icon opens the full editor.
3.  **Project Ideas**: Create/Edit an idea. Verify the milestones section now has the tag filter bar, collapsible sections, and category tags. Verify the Pencil icon opens the advanced full-screen editor.
4.  **Consistency Check**: Verify that "UI" (Pink), "LOGIC" (Purple), and "BUG" (Red) tags have consistent colors across all 3 screens.
