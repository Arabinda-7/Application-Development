# Implementation Plan - Unique Subfeature Naming

Ensure that subfeatures have unique names by automatically appending "v1", "v2", etc., when a duplicate name is entered. This applies to both custom names and default names ("New Feature").

## Proposed Changes

### [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)

- Add a utility function `getUniqueFeatureName(baseName: String, existingFeatures: List<ProjectFeature>): String` to generate a unique name by appending "v1", "v2", etc.

### [AddProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddProjectActivity.kt)

- Modify the `btnAddSubfeature` click listener to use `DataManager.getUniqueFeatureName`.
- Remove the Toast message for duplicate names.

### [AddIdeaActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddIdeaActivity.kt)

- Modify the `btnAddSubfeature` click listener to use `DataManager.getUniqueFeatureName`.
- Add a default name "New Feature" if the input is empty.

### [AddSubFeatureActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddSubFeatureActivity.kt)

- Modify `saveFeature` to use `DataManager.getUniqueFeatureName` for the name if a duplicate is detected (excluding the feature being edited itself).

## Verification Plan

### Manual Verification
1. Open a Project and add multiple subfeatures with the same name. Verify they are renamed to "name v1", "name v2", etc.
2. Add a subfeature with an empty name. Verify it becomes "New Feature", and subsequent empty additions become "New Feature v1", etc.
3. Open an Idea and perform the same tests.
4. Edit an existing subfeature's name to match another subfeature's name. Verify it gets auto-renamed.
