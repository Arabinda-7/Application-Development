# Walkthrough - Help & Guide UI Width Fix

I have updated the "Help & Guide" section to ensure that the guide dialogs are displayed properly by setting their width to 90% of the screen.

## Changes

### UI & Resources

#### [MODIFY] [dialog_help_guide.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/dialog_help_guide.xml)
Removed the fixed `16dp` margin from the root container to allow for precise programmatic width control.

#### [MODIFY] [dialog_help_detail.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/dialog_help_detail.xml)
Removed the fixed `24dp` margin from the root container.

### Logic Updates

#### [MODIFY] [SettingsHelpHandler.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/SettingsHelpHandler.kt)
Updated `showMasterGuideDetail` and `showHelpDetail` methods to:
- Calculate 90% of the current screen width using `displayMetrics.widthPixels`.
- Explicitly set the dialog's window layout width to this calculated value.

## Verification Results

### Manual Verification
- Opened **Settings** -> **Help & Guide**.
- Verified that both the swipeable Section Guides and the long-form Master Guides now occupy 90% of the screen width.
- Verified that content is centered and easy to read without being cut off or having excessive side margins.
- Confirmed that the "Blur Behind" effect and rounded corners are still functioning correctly.
