# Implementation Plan - Fix Loading Screen Icon Rendering

The goal is to fix the icon display in the `LoadingScreen`. The user reports that icons are not showing properly, which is likely due to incorrect color conversion and forced tinting of multi-colored PNG assets.

## User Review Required

> [!IMPORTANT]
> I will be switching from `Icon` to `Image` for the loading steps. This will stop the forced monochromatic tinting, allowing the original colors of the `icons8` assets to be displayed. The themed colors will still be used for the progress ring.

## Proposed Changes

### UI Components

#### [MODIFY] [LoadingScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/components/LoadingScreen.kt)

- **Fix Color Conversion**: Change `Color(Int.toLong())` to the proper Compose `Color(Int)` constructor to avoid bit-shifting issues with Android colors.
- **Improve Icon Rendering**:
    - Replace `Icon` with `Image`.
    - Remove the `tint` parameter to preserve original icon colors (especially for `icons8` PNGs).
    - Increase the icon size slightly (from `80.dp` to `100.dp`) for better visibility within the `200.dp` ring.
- **Dynamic Icons**: Use `DataManager`'s global icons where appropriate, but maintain the specific "startup" icons if they are preferred for their visual style.

## Verification Plan

### Automated Tests
- Render the `LoadingScreenPreview` using `render_compose_preview` and verify the icon is clearly visible and colored correctly.

### Manual Verification
1.  Launch the app.
2.  Observe the loading screen.
3.  **Expected**: The icons should appear in their original multi-colored design (if applicable) and be correctly centered and sized within the progress ring. The ring itself should match the themed color.
