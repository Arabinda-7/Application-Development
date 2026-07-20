# Implementation Plan - Workspace Sidebar Scrim & Blur

This plan adds a glass-like blur effect to the background when the menu is expanded and allows users to dismiss the menu by tapping anywhere outside of it.

## Proposed Changes

### [Workspace Component]

#### [MODIFY] [ProjectWorkspaceScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/ProjectWorkspaceScreen.kt)
- **Background Blur**:
    - Use `animateDpAsState` for a `blurRadius` (0dp to 10dp).
    - Apply `Modifier.blur(blurRadius)` to the main `Scaffold` content.
- **Dismissible Scrim**:
    - Insert an interactive layer (Scrim) between the content and the sidebar.
    - This layer will be a `Box` with a semi-transparent background.
    - Add a `clickable` modifier to this `Box` that triggers `isSidebarExpanded = false`.
    - Use `AnimatedVisibility` for a smooth fade-in/out of the scrim.
- **Visual Polish**:
    - Ensure the scrim and blur animations are synchronized with the sidebar sliding out.

## Verification Plan

### Manual Verification
1. Open the Workspace.
2. Open the sidebar (via icon, swipe, or long-press).
3. Verify that the background dashboard and cards become slightly blurred and darkened.
4. Tap on the blurred dashboard area.
5. Verify the sidebar collapses and the blur disappears.
6. Verify clicking on the sidebar icons still works and doesn't close the sidebar (unless it's an action that navigates).
