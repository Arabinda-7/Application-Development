# Implementation Plan - Immersive Aura Headers

Extend the "Aura" background effect into the system notification bar (status bar) across all major app sections, matching the immersive look of the Home and Habit screens.

## User Review Required

> [!IMPORTANT]
> This change involves modifying the root layout structure of several major screens to separate the "Background" layer from the "Content" layer. This is necessary to allow the background to draw behind the status bar while keeping buttons and text safely padded below it.

## Proposed Changes

### [BaseActivity Refinement]

I will update the global keyboard and inset handler to support immersive backgrounds.

#### [MODIFY] [BaseActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/BaseActivity.kt)
- Update `setupKeyboardHandling(rootView: View, topPaddingView: View?)`:
    - The `rootView` will handle the bottom insets (keyboard and navigation bar).
    - The `topPaddingView` (e.g., your header or content container) will handle the status bar insets.
    - This allows the background (which is a sibling to `topPaddingView`) to remain at the absolute top of the screen.

### [Layout Enhancements]

I will restructure the following layouts to support immersion:

#### [MODIFY] [activity_task.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_task.xml)
- Wrap all content (except `task_aura_background`) in a new `ConstraintLayout` with id `task_content_container`.

#### [MODIFY] [activity_notes.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_notes.xml)
- Wrap all content (except `note_aura_background`) in a new `ConstraintLayout` with id `notes_content_container`.

#### [MODIFY] [activity_projects.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_projects.xml)
- Wrap all content (except `project_aura_background`) in a new `ConstraintLayout` with id `project_content_container`.

#### [MODIFY] [activity_finance.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_finance.xml)
- Wrap all content (except `finance_aura_background`) in a new `ConstraintLayout` with id `finance_content_container`.

### [Activity Logic Updates]

I will update the activities to use the new immersive padding logic.

#### [MODIFY] [TaskActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/TaskActivity.kt)
#### [MODIFY] [NotesActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/NotesActivity.kt)
#### [MODIFY] [ProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProjectActivity.kt)
#### [MODIFY] [FinanceActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/FinanceActivity.kt)
#### [MODIFY] [WorkoutRoutineActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/WorkoutRoutineActivity.kt)
- Update calls to `setupKeyboardHandling` to pass both the root and the new content container.

## Verification Plan

### Manual Verification
- **Visual Check**: Open each section and verify the background color extends to the very top, behind the time and battery icons.
- **Usability Check**: Verify that the "Back" button and "Title" are not overlapping with the status bar icons.
- **Keyboard Check**: Verify that the keyboard still correctly pushes the layout up without breaking the immersive header.
