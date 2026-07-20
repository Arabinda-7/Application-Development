# Implementation Plan - UI Sync for Task Section

The user wants to apply the same UI improvements (uniform styling, unclipped borders, and theme-synced footer colors) to the Task section, matching the Habits and Workouts sections.

## User Review Required

> [!NOTE]
> For the Task section, categories are dynamic and scrollable. I will update their styling to match the "capsule" look (38dp height, 1.5dp stroke) and ensure the borders are not clipped. The bottom navigation will also be synced with the global task theme color.

## Proposed Changes

### UI Layouts

#### [MODIFY] [activity_task.xml](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/res/layout/activity_task.xml)
- Add `android:clipChildren="false"` and `android:clipToPadding="false"` to the `HorizontalScrollView` (`chip_scroll_categories`) and its child `RadioGroup` (`category_filter_group`).
- This ensures the chip outlines are not cut off when scrolling or drawing.

### Logic / Styling

#### [MODIFY] [TaskActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/TaskActivity.kt)
- **Implement `applySectionTheme()`**:
    *   Apply the global task color (from `DataManager`) to the active footer navigation item.
    *   Update `updateNavUI()` to use the dynamic theme color.
    *   Call `applySectionTheme()` in `onCreate` and `onResume`.
- **Refine `setupFilters()`**:
    *   Increase chip height to `38dp`.
    *   Apply the programmatic `GradientDrawable` (capsule shape, 1.5dp stroke) to the category chips.
    *   Ensure unselected chips have the 1.5dp stroke and selected chips have the solid theme color.
- **Immediate Startup Sync**: Ensure `updateNavUI()` is called within `applySectionTheme()` to prevent the "blue flicker" on activity start.

## Verification Plan

### Manual Verification
- Deploy the app and navigate to the Tasks section.
- Verify that the category filter chips have the new "capsule" styling with clear 1.5dp borders.
- Verify that the bottom navigation active state uses the global task color.
- Check that there is no blue flicker when entering the Tasks page.
