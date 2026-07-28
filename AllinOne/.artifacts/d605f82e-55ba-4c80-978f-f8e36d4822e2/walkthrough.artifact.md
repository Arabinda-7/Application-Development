# Simplified Project UI (Unified Add/View)

I have refined the Project section by unifying the **Add Project** and **View Project** experiences. The UI is now consistently simple and clear, removing the complex "Aura" background and compact layouts from the view mode.

## Changes Made

### 1. Unified Project UI
- **Structural Parity**: Viewing a project now looks exactly like the "Add Project" screen.
- **Removed Aura Background**: The full-screen radial aura has been removed from the "View Project" mode to maintain a simple, clean aesthetic.
- **Simplified Header**: Both modes now share the same top-accent header background.
- **Clear Selectors**: Removed the confusing "compact" rows. Status and Priority are now always shown as clear `RadioGroup` selectors, providing better visibility of all options.

### 2. Interaction Guardrails
- **Read-Only Context**: In `isViewOnly` mode, the selectors and inputs are disabled to prevent accidental modifications while maintaining a familiar layout.
- **Removed Edit Option**: Hidden the "EDIT" button from the View Project screen to ensure it remains a pure viewing experience as requested.
- **Unified Logic**: Consolidated the UI logic in `AddProjectActivity` to handle both modes through a single set of views.

## Verification Results

### Manual UI Check
- **Add Project**: Verified the simple layout remains functional.
- **View Project**: Confirmed the UI is now identical to "Add Project", with the full-screen aura removed and selectors visible but read-only.
- **Clean Resources**: Redundant layouts like `dialog_project_details.xml` and unused code have been removed.
