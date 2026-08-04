# Implementation Plan - Modernize Help & Guide UI Width

The goal is to fix the "Help & Guide" section by ensuring the guide dialogs occupy 90% of the screen width, making them more readable and visually consistent.

## User Review Required

> [!IMPORTANT]
> The Help Guide dialogs will be updated to occupy 90% of the screen width. Root margins will be removed from the XML layouts to ensure the programmatic width control is precise.

## Proposed Changes

### UI Resources

#### [MODIFY] [dialog_help_guide.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/dialog_help_guide.xml)
- Remove `android:layout_margin="16dp"` from the root `MaterialCardView`.
- Ensure `layout_width` is `match_parent`.

#### [MODIFY] [dialog_help_detail.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/dialog_help_detail.xml)
- Remove `android:layout_margin="24dp"` from the root `MaterialCardView`.
- Ensure `layout_width` is `match_parent`.

#### [MODIFY] [item_help_feature.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/item_help_feature.xml)
- Adjust `paddingHorizontal` if necessary to ensure content looks good at 90% width. (Currently 24dp, might keep it).

### Logic Updates

#### [MODIFY] [SettingsHelpHandler.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/SettingsHelpHandler.kt)
- In `showMasterGuideDetail` and `showHelpDetail`, programmatically set the dialog window width to 90% of the screen width.
- Use `context.resources.displayMetrics.widthPixels` to calculate the 90% width.

## Verification Plan

### Manual Verification
1. Open **Settings** -> **Help & Guide**.
2. Tap on any guide (e.g., Habits Guide).
3. Verify that the dialog:
    - Occupies 90% of the screen width.
    - Has consistent spacing on left and right.
    - Content (image, title, description) is properly visible and not cut off.
4. Tap on "Master Guides" (if available) to verify the detail dialog also uses 90% width.
