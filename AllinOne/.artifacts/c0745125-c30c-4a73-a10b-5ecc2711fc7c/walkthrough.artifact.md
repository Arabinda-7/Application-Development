# Walkthrough - Reverted to Material Icons

I have reverted the app's icons from the illustrative Icons8 set back to the standard Material Icons from the **Material Icons Extended** library. This restores the clean, uniform look of the application.

## Changes Made

### UI Components Reversion
- **Bottom Navigation:** Reverted Home, AI, and Settings icons to their standard Material versions (`Home`, `AutoAwesome`, `Settings`).
- **Floating Action Button (FAB):** Reverted the main "Add" and "Close" icons in the speed dial.
- **Quick Actions:** Icons for "Task" (`Add`), "Cash" (`ShoppingCart`), and "Note" (`Edit`) are back to standard vectors.
- **Home Header:** Search, Notifications, Mic, and Send buttons now use standard Material Icons.
- **Dashboard Cards:** The category cards for Habits, Workouts, Tasks, etc., now use their original Material vector icons.

### Code Cleanup
- **Type Safety:** Restored the use of `ImageVector` for icons in composables, removing the `Painter` overhead.
- **Tinting:** Removed custom logic that was preserving PNG colors, allowing icons to once again respect theme colors and accent tinting.
- **Data Management:** Reverted global default resource IDs in `DashboardState` and data managers to point back to original vector drawables.

### Onboarding
- **Flow Reversion:** The onboarding screens now use standard Material icons (`Diversity3`, `Verified`, etc.) and the `OnboardingSection` model has been reverted to use `ImageVector`.

## Icon Mapping Summary

| Component | Material Icon Used |
| :--- | :--- |
| **Home Nav** | `Icons.Default.Home` |
| **Settings Nav** | `Icons.Default.Settings` |
| **AI Button** | `Icons.Default.AutoAwesome` |
| **Habits** | `Icons.Default.SelfImprovement` |
| **Workouts** | `Icons.Default.FitnessCenter` |
| **Tasks** | `Icons.Default.Checklist` |
| **Notes** | `Icons.Default.Description` |
| **Projects** | `Icons.Default.AccountTree` |
| **Finance** | `Icons.Default.AccountBalanceWallet` |

## Verification Results
- Verified that all icons render correctly as vector graphics.
- Confirmed that icons respond to active theme colors (Dynamic Color, OLED mode, etc.).
- Build successful with no resource type mismatches.
