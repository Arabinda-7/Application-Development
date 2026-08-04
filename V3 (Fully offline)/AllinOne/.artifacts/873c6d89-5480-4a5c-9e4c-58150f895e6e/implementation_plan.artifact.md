# Implementation Plan - Smoother Workspace Transitions

The user reported that page transitions in the Workspace feel "flashy". This plan aims to replace the aggressive horizontal slides with more subtle, professional Material-style transitions.

## Proposed Changes

### 1. Refine Workspace Transitions

#### [MODIFY] [ProjectWorkspaceScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/ui/ProjectWorkspaceScreen.kt)

- **Creation/Detail Navigation**:
    - Current: 450ms full-width horizontal slide.
    - New: 350ms duration using `fadeIn` + `scaleIn(initialScale = 0.96f)` when opening, and `fadeOut` + `scaleOut(targetScale = 0.96f)` when closing. This "Shared Z-Axis" style transition is standard for entering sub-screens.
- **Tab Switching**:
    - Current: Full-width horizontal slide based on ordinal index.
    - New: Crossfade with a subtle slide (offset of 30dp). This reduces the "whiplash" effect when switching between dashboard modules.

## Verification Plan

### Manual Verification
- **Entering Details**: Click a Goal or Bug and verify the screen "lifts" into view smoothly rather than sliding aggressively.
- **Switching Tabs**: Navigate between "Dashboard", "Goals", and "Tasks" to ensure the transition is subtle and doesn't distract from the content.
- **Consistency**: Ensure the back navigation feels equally smooth.
