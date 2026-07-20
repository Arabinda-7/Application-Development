# Implementation Plan - Compact Metadata & Goal Timestamps

This plan enhances the project section by adding timestamps to individual goals and streamlining the Theme Color and Deadline metadata into compact, interactive sections.

## User Review Required

> [!IMPORTANT]
> - **Timestamps**: Every goal will now show its creation date and time in the bottom-right corner.
> - **Metadata Layout**: "Theme Color" and "Deadline" will be moved into a compact horizontal bar, matching the "Status" and "Priority" section.
> - **Interactive View**: In the project details viewer, you'll be able to tap the "Deadline" block to quickly change the date without entering full edit mode.

## Proposed Changes

### UI Components
#### [MODIFY] [activity_add_project.xml](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/res/layout/activity_add_project.xml)
- Update `layout_edit_compact` to be part of a larger `meta_grid` or add a second horizontal bar.
- Create `layout_edit_meta_2` for Color and Deadline.
- Set up IDs for the new compact labels (`tv_edit_color_label`, `tv_edit_deadline_label`).

#### [MODIFY] [dialog_project_details.xml](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/res/layout/dialog_project_details.xml)
- Add a new `LinearLayout` row below `row_badges` for "COLOR" and "DEADLINE".
- Remove the old deadline text from the footer to avoid redundancy.

### Logic
#### [MODIFY] [AddProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/AddProjectActivity.kt)
- **Goal Timestamps**: Update `refreshGoalsUI` to include a small timestamp `TextView` in each goal item's layout.
- **Compact Meta**:
    - Handle visibility of the new compact Color/Deadline section.
    - Implement click listeners for `btn_edit_color` and `btn_edit_deadline`.
    - `btn_edit_deadline` will trigger the standard `DatePickerDialog`.
    - Update the labels programmatically.

#### [MODIFY] [ProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/ProjectActivity.kt)
- **Goal Timestamps**: Update the details dialog populate logic to show timestamps for goals.
- **Compact Meta**:
    - Populate the new Color and Deadline blocks.
    - Add a click listener to the Deadline block to open a `DatePickerDialog` for quick updates.

## Verification Plan

### Automated Tests
- Run `gradlew app:assembleDebug` to verify build integrity.

### Manual Verification
1.  **Goal Timestamps**: Add a project goal and verify the date/time appears at the bottom right of the goal card.
2.  **Compact View**: Open project details and verify the 2-row metadata grid (Status/Priority and Color/Deadline).
3.  **Quick Edit**: Tap the Deadline block in the details dialog and verify you can change the date and it saves immediately.
