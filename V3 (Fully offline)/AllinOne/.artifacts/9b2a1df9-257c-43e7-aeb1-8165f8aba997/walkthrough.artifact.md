# Walkthrough: Transparent Dashboard Sections in Dark Mode

I have updated the home page to show transparent backgrounds for all major sections (Habits, Workout, Tasks, Notes, Projects, and Finance) when in Dark or OLED mode.

## Changes Made

### 🎨 Theme Logic Optimization
- **File:** [AppTheme.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AppTheme.kt)
- Adjusted `surfaceColor` to use a very low alpha (`0.02f` to `0.05f`) in Dark and OLED modes. This makes the cards appear transparent while still allowing the native blur effect to work.

### 🖼️ UI Component Updates
- **File:** [HomeCards.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/home/components/HomeCards.kt)
- Updated all dashboard card components (`HabitCard`, `WorkoutCard`, `TaskCard`, `NoteCard`, `ProjectCard`, and `FinanceCard`) to use the global `style.surfaceColor`.
- Removed hardcoded opacity overrides, allowing the cards to blend perfectly with the app's background.

## User Experience Impact
- **Sleek Aesthetic:** The "Glassmorphism" effect is now more pronounced, with the background of the app subtly showing through the cards.
- **Improved Contrast:** The colored borders and native blurs now define the card boundaries, resulting in a cleaner and more modern look.

## Verification
- ✅ **OLED Mode:** Cards blend seamlessly with the pure black background.
- ✅ **Dark Mode:** Cards show a subtle frosted effect over the dark surface.
- ✅ **Light Mode:** Maintained appropriate semi-transparent white backgrounds for readability.
