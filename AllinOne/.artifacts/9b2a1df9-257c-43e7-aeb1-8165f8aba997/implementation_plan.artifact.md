# Transparent Dashboard Sections in Dark Mode

This plan focuses on making the background of the home page sections (Habits, Workout, Tasks, Notes, Projects, Finance) transparent when the app is in Dark Mode or OLED mode. This will enhance the "Glassmorphism" effect and provide a cleaner, more modern look.

## User Review Required

> [!IMPORTANT]
> Making card backgrounds fully transparent in dark mode means they will blend with the black background. We will rely on the existing sweep-gradient borders and native blurs to maintain the card's visual structure.

## Proposed Changes

### UI Components

#### [MODIFY] [HomeCards.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/home/components/HomeCards.kt)
- Update the `containerColor` logic for all cards (`HabitCard`, `WorkoutCard`, `TaskCard`, `NoteCard`, `ProjectCard`, `FinanceCard`).
- Remove the hardcoded `0.4f` alpha override that was making "GLASS" cards too opaque.
- Implement a logic where in Dark/OLED mode, the card background alpha is reduced to `0.05f` or `0.02f` (virtually transparent) to let the app background show through.

### Theme Logic

#### [MODIFY] [AppTheme.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AppTheme.kt)
- Refine the `surfaceColor` definition in `AppStyle` to ensure it provides a truly transparent base for dark modes.

## Verification Plan

### Manual Verification
- **Dark Mode:** Switch the app to Dark Mode and verify that the dashboard cards have a transparent background, showing the underlying black surface while maintaining their frosted glass border/blur.
- **OLED Mode:** Verify that cards look perfectly integrated with the pure black background.
- **Light Mode:** Ensure Light Mode remains readable with its appropriate semi-transparent white background.
