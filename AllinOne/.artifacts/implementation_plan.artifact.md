# Fix Unresolved reference 'etInspirationUrl' in ProjectActivity.kt

The build is failing because `etInspirationUrl` is used in `ProjectActivity.kt` within the `showAddIdeaDialog` method but is never declared or initialized. This variable should refer to the `EditText` in the `dialog_add_note_project.xml` layout that holds the inspiration URL.

## Proposed Changes

### [Component Name] ProjectActivity

#### [MODIFY] [ProjectActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ProjectActivity.kt)

- Initialize `etInspirationUrl` in the `showAddIdeaDialog` method using `dialog.findViewById(R.id.et_inspiration_url)`.
- Update the `btnSave` click listener to save the content of `etInspirationUrl` to the `Note` object's `inspirationUrl` property.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify the build error is resolved.

### Manual Verification
- Deploy the app to a device or emulator.
- Open the Projects section.
- Add or edit a Project Idea.
- Verify that the "Website URL..." field is correctly pre-filled when editing an existing idea.
- Verify that changes to the "Website URL..." field are saved.
