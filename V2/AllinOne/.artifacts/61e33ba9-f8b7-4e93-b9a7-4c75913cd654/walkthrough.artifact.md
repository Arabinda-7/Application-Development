# Walkthrough - Unique Subfeature Naming

I have implemented a unique naming mechanism for subfeatures in both Projects and Ideas. This ensures that when a user enters a duplicate name (or leaves the name empty), the app automatically generates a unique name by appending "v1", "v2", etc.

## Changes Made

### Life Architecture Core
#### [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
- Added `getUniqueFeatureName` utility function to safely generate unique names based on existing features in a list.

### Project & Idea Management
#### [AddProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddProjectActivity.kt)
- Updated subfeature addition to use `getUniqueFeatureName`.
- Removed duplicate name restriction toast, allowing automatic renaming instead.
- Handles empty input by defaulting to "New Feature".

#### [AddIdeaActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddIdeaActivity.kt)
- Added automatic unique naming for idea subfeatures.
- Now supports empty input by defaulting to "New Feature" (previously required non-empty input).

#### [AddSubFeatureActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AddSubFeatureActivity.kt)
- Updated the edit/save logic to ensure that renaming a subfeature to an existing name results in a unique version (e.g., "Feature v1") rather than failing with a toast.

## Verification Results

### Automated Tests
- Static analysis performed on modified files; no new errors introduced.

### Manual Verification Required
- [ ] Add multiple subfeatures with the same name in a Project.
- [ ] Add multiple subfeatures with empty names in an Idea.
- [ ] Edit a subfeature's name to match another one.
