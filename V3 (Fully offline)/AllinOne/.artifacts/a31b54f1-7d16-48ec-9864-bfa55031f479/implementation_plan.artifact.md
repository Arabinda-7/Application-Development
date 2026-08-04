# Fix Unresolved Reference 'currentDetailsTagFilter'

The variable `currentDetailsTagFilter` is used in `refreshDetailsSubFeatures()` but is not declared anywhere in `ProjectActivity.kt`. This causes a build failure.

## Proposed Changes

### [ProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/ProjectActivity.kt)

#### [MODIFY] [ProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App%20Development/AllinOne/app/src/main/java/com/example/allinone/ProjectActivity.kt)
Declare `currentDetailsTagFilter` as a local variable within `showProjectDetailsDialog` so it can be captured by the local function `refreshDetailsSubFeatures`.

```kotlin
    fun showProjectDetailsDialog(note: Note) {
        val dialog = Dialog(this, R.style.FullScreenDialog)
        dialog.setContentView(R.layout.dialog_project_details)

        var currentDetailsTagFilter = "ALL"
        ...
```

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify the project builds successfully.

### Manual Verification
- Deploy the app and open the project details dialog.
- Verify that filtering by tags works as expected.
