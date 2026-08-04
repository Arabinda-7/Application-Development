# Fix "Unresolved reference 'tv_search_query'" build error

The build error is caused by a mismatch between the ID used in `MainSearchSection.kt` and the ID defined in `dialog_search_results.xml`. The code expects `tv_search_query`, but the XML defines `tv_search_title`.

## Proposed Changes

### Layouts

#### [MODIFY] [dialog_search_results.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/dialog_search_results.xml)
- Rename `android:id="@+id/tv_search_title"` to `android:id="@+id/tv_search_query"`.

### Code

#### [MODIFY] [MainSearchSection.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/MainSearchSection.kt)
- Add a click listener to `btn_close_search` to dismiss the dialog, ensuring the close button works as intended.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to ensure the project builds successfully.

### Manual Verification
- Deploy the app and perform a search to verify that the results dialog displays correctly and the close button works.
