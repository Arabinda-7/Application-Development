# Implementation Plan - Refine Settings Selection Dialog

This plan focuses on making the multi-option selection dialog more compact and visually appealing by tightening the layout and adopting a "floating pill" aesthetic for the options.

## User Review Required

> [!TIP]
> I am proposing to change the dialog from a full-width list to a centered "floating" card with tighter spacing. This will make it feel more integrated and "perfectly fitted" as requested.

## Proposed Changes

### [UI Layouts]

#### [MODIFY] [dialog_settings_selection.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/dialog_settings_selection.xml)
- Change `layout_width` to `wrap_content` with a `minWidth`.
- Center the title and reduce vertical padding.
- Adjust internal margins to accommodate the new floating style.

#### [MODIFY] [item_settings_selection.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/item_settings_selection.xml)
- Reduce vertical padding from `16dp` to `12dp`.
- Add horizontal margins so the selection highlight looks like a distinct pill inside the dialog.
- Center the text for a more "menu-like" feel when the dialog is narrow.

#### [NEW] [bg_item_selection_ripple.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/drawable/bg_item_selection_ripple.xml)
- Create a ripple effect that matches the `12dp` corner radius of the selection highlight.

### [Logic]

#### [MODIFY] [ConfigAdapter.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ConfigAdapter.kt)
- Update `window.setLayout` to use `WRAP_CONTENT` for width, allowing the dialog to shrink for short options.

## Verification Plan

### Manual Verification
- Open settings and trigger various selection dialogs (e.g., Currency, Theme, Sort Order).
- Verify the dialog width adjusts reasonably to the content.
- Check the new "pill" look of the selections and ensure the checkmark is still correctly placed.
