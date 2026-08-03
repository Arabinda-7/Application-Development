# Implementation Plan - Wiring Disconnected Features and Cleanup

This plan addresses the identified "disconnected" features by wiring them into the existing UI flow and removing dead or placeholder code.

## User Review Required

> [!IMPORTANT]
> **Task Analytics Entry Point**: I plan to add the Task Analytics trigger as a long-press action on the "Tasks" footer icon or as a new menu item. Please let me know if you prefer a different location.

## Proposed Changes

### [Navigation & Connectivity]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/MainActivity.kt)
- Wire the `onNavigateToWorkspace` callback to launch `WorkspaceActivity`.

#### [MODIFY] [AssistantActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AssistantActivity.kt)
- Add "Feed" to the overflow menu.
- Add a "View Intelligent Feed" button near the insights section to launch `AssistantFeedActivity`.

#### [MODIFY] [TaskNavigationSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/TaskNavigationSection.kt)
- Add a long-click listener to the Tasks navigation item to trigger `TaskAnalyticsHandler.show()`.

---

### [UI Enhancements]

#### [MODIFY] [VoiceOverlay.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/home/components/VoiceOverlay.kt)
- Integrate `VoiceAuraGlow` and `GoogleVoiceBars` from `AssistantAnimations.kt` to replace the static mic icon during active listening.

---

### [Cleanup]

#### [DELETE] [ProjectBoardActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProjectBoardActivity.kt)
#### [DELETE] [FinanceHeatmapActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/FinanceHeatmapActivity.kt)
#### [DELETE] [ArchitectTreeAdapter.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ArchitectTreeAdapter.kt)
#### [DELETE] [MenuAdapter.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/MenuAdapter.kt)

## Verification Plan

### Automated Tests
- Run build to ensure no broken references after deletion.

### Manual Verification
- Launch the app and verify:
  1. Clicking "Workspace" from the Agenda dialog opens the Workspace screen.
  2. The Assistant screen has a "Feed" option that opens the Intelligent Feed.
  3. Long-pressing the "Tasks" icon in the Task screen shows the analytics dialog.
  4. The voice assistant overlay shows pulsing animations when active.
