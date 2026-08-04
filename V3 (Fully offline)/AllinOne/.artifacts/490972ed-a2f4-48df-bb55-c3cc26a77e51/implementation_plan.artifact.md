# Implementation Plan - Inline Subfeature Details Expansion

The user wants to expand subfeature rows to show their details inline when tapped, matching the behavior in the "Ideas Section" (AddIdeaActivity). I will replace the previously implemented dialog with this inline expansion logic.

## Proposed Changes

### 1. ViewProjectActivity
#### [MODIFY] [ViewProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ViewProjectActivity.kt)
- Update `createSubFeatureItem` to:
    - Use a vertical `LinearLayout` as the root.
    - Move current content into a horizontal `header` layout.
    - Add a `tvNote` TextView below the header for `details`.
    - Toggle `tvNote` visibility and update `sub.isExpanded` on tap.

### 2. AddProjectActivity
#### [MODIFY] [AddProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddProjectActivity.kt)
- Update `addSubfeatureRow` to follow the same vertical expansion pattern.

### 3. EditProjectActivity
#### [MODIFY] [EditProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/EditProjectActivity.kt)
- Update `addSubfeatureRow` to follow the same vertical expansion pattern.

### 4. Cleanup
#### [MODIFY] [BaseActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/BaseActivity.kt)
- Remove `showSubFeatureDetails` as it's no longer needed.
#### [DELETE] [dialog_subfeature_details.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/dialog_subfeature_details.xml)
- Remove the unused dialog layout.

## Verification Plan

### Manual Verification
1. Open a Project (View, Add, or Edit mode).
2. Tap on a subfeature that has details.
3. Verify that the details appear inline below the subfeature name.
4. Tap again to verify it collapses.
5. Tap on a subfeature without details; verify no expansion occurs.
