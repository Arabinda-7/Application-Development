# Implementation Plan - Standardize Habit Section Theme and Chip Colors

Standardize the Habit section's colors by ensuring consistent default fallbacks and updating the filter chips to perfectly match the theme color (section color).

## Proposed Changes

### Habit Tracker Activity

#### [MODIFY] [HabitTrackerActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/HabitTrackerActivity.kt)
- **Synchronize Default Color**: Update `updateDynamicBackground` to use `R.color.primary_blue` as the fallback color when `DataManager.globalHabitColor` is not set. This replaces the salmon color (`#FF7A59`) with the consistent blue used in the rest of the Habits section.
- **Vibrant Filter Chips**:
    - Update `applySectionTheme` to use the full `habitColor` for the checked state background of filter chips (Morning, Afternoon, Evening, All).
    - Update text color logic for chips: **White** when checked, and **vibrant theme color** (`habitColor`) when unchecked.
- This ensures the chips look like a cohesive part of the "section" and match the border colors.

### Habit Detail Activity

#### [MODIFY] [HabitDetailActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/HabitDetailActivity.kt)
- **Themed Frequency Chip**: Update `setupUI` to apply the specific habit's color to the `tv_frequency_chip`.
- The chip will use the habit's color for its text and a low-alpha version of the same color for its background.

## Verification Plan

### Manual Verification
- **Default State**: Open the Habit Tracker and verify the background glow and filter chips are both blue.
- **Custom Theme**: Change the habit theme color and verify that the glow, chips, card borders, and FAB all switch to the new color.
- **Chip States**: Verify selected chips are solid vibrant color with white text; unselected chips have colored borders and colored text.
- **Detail Screen**: Verify frequency chips in the detail view match the individual habit's color.
