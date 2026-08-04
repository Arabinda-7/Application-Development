# Implementation Plan - Glassmorphic Workout History UI

I will update the Workout History stat cards and the grid section to use a glassmorphic style (semi-transparent backgrounds with vibrant strokes) to better integrate with the app's dark, immersive theme.

## Proposed Changes

### [Component] UI Layer

#### [MODIFY] [activity_workout_routine.xml](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/res/layout/activity_workout_routine.xml)
- Convert stat `CardView`s to `com.google.android.material.card.MaterialCardView`.
- Set `app:cardBackgroundColor="#11FFFFFF"` (Glassy semi-transparent white) for all three cards and the `history_grid_container`.
- Apply vibrant `app:strokeColor` to the stat cards:
    - Current Streak: `#2196F3` (Blue)
    - Workouts: `#FF9800` (Orange)
    - Efficiency: `#FFC107` (Amber)
- Set `app:strokeWidth="1.5dp"` for the stat cards.
- Update text colors:
    - Numbers (`history_current_streak`, `history_workouts_finished`, `history_efficiency`): Use the vibrant colors (`#2196F3`, `#FF9800`, `#FFC107`) for a "neon" pop effect.
    - All titles and footers: Use standard white/gray (`@color/white` or `#B0B0B0`) for consistent legibility on a dark background.

## Verification Plan

### Manual Verification
1.  Open **Workout Routine** -> **History**.
2.  Verify the three cards are now semi-transparent, showing the background aura/gradient through them.
3.  Verify the cards have vibrant borders matching their category.
4.  Verify the numbers are vibrant and "pop" against the dark background.
5.  Check that the History Grid section also has a semi-transparent background, matching the overall page style.
