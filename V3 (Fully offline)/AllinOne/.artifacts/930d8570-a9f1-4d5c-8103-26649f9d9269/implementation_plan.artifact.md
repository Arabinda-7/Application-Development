# Fix invalid resource names

Rename drawable resources that contain hyphens and update their references in the codebase. Android resource names must only contain lowercase a-z, 0-9, or underscore.

## Proposed Changes

### [Component Name]

#### [MODIFY] [IconPreview.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/IconPreview.kt)
Update the resource references to use underscores instead of hyphens.

#### [RENAME]
- `icons8-arrow-100.png` -> `icons8_arrow_100.png`
- `icons8-arrow-100-2.png` -> `icons8_arrow_100_2.png`
- `icons8-arrow-100-3.png` -> `icons8_arrow_100_3.png`
- `icons8-arrow-100-4.png` -> `icons8_arrow_100_4.png`
- `icons8-arrow-100-5.png` -> `icons8_arrow_100_5.png`
- `icons8-arrow-100-6.png` -> `icons8_arrow_100_6.png`
- `icons8-arrow-100-7.png` -> `icons8_arrow_100_7.png`
- `icons8-arrow-100-8.png` -> `icons8_arrow_100_8.png`
- `icons8-arrow-100-9.png` -> `icons8_arrow_100_9.png`
- `icons8-arrow-100-10.png` -> `icons8_arrow_100_10.png`
- `icons8-arrow-100-11.png` -> `icons8_arrow_100_11.png`
- `icons8-arrow-100-12.apng.png` -> `icons8_arrow_100_12_apng.png`

## Verification Plan

### Automated Tests
- Run `./gradlew :app:mergeDebugResources` to verify that the resource merging error is resolved.
- Build the project to ensure no other compilation errors.
