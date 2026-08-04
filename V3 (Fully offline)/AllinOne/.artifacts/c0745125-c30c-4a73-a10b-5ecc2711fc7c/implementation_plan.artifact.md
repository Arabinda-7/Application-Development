# Implementation Plan - Revert to Material Icons

The goal is to revert the illustrative Icons8 icons back to the standard Material Icons from the **Material Icons Extended** library, ensuring a consistent and clean look across the app.

## User Review Required

> [!NOTE]
> This change will remove all `icons8_*` PNG icons from the main UI and replace them with standard `ImageVector` icons from the Material library. This includes reverting changes in the Home Screen, Header, Assistant, and Onboarding flows.

## Icon Mapping Table

Below is the list of icons used in each location, mapped to their Material Icons Extended library equivalents.

| UI Location | Component | Material Icon Used |
| :--- | :--- | :--- |
| **Home Screen** | Bottom Nav: Home | `Icons.Default.Home` |
| | Bottom Nav: Settings | `Icons.Default.Settings` |
| | AI Assistant Button | `Icons.Default.AutoAwesome` |
| | FAB (Main Add/Close) | `Icons.Default.Add` / `Icons.Default.Close` |
| | Quick Action: Task | `Icons.Default.Add` |
| | Quick Action: Cash | `Icons.Default.ShoppingCart` |
| | Quick Action: Note | `Icons.Default.Edit` |
| **Home Header** | Notifications Button | `Icons.Default.Notifications` |
| | Search Toggle/Close | `Icons.Default.Search` / `Icons.Default.Close` |
| | Search Leading Icon | `Icons.Default.Search` |
| | Voice/Send Button | `Icons.Default.Mic` / `Icons.AutoMirrored.Filled.Send` |
| **Dashboard Cards**| Habit Tracker | `Icons.Default.SelfImprovement` |
| | Workout / Fitness | `Icons.Default.FitnessCenter` |
| | To-Do Tasks | `Icons.Default.Checklist` |
| | Notes / Writing | `Icons.Default.Description` |
| | Project / Folders | `Icons.Default.AccountTree` |
| | Finance / Savings | `Icons.Default.AccountBalanceWallet` |
| **AI Assistant** | Menu: New Chat | `Icons.Default.Add` |
| | Menu: History | `Icons.Default.History` |
| | Menu: Intelligent Feed| `Icons.Default.AutoAwesome` |
| | Menu: Settings | `Icons.Default.Settings` |
| | Hint/Suggestions | `Icons.Default.Lightbulb` / `Icons.Default.TipsAndUpdates` |
| **Assistant History**| Search Bar | `Icons.Default.Search` |
| | Clear All Button | `Icons.Default.DeleteSweep` |
| | Thread Icon | `Icons.Default.ChatBubbleOutline` |
| | Delete Thread | `Icons.Default.DeleteSweep` |
| **Onboarding** | Overview Icon | `Icons.Default.Diversity3` |
| | Activation Success | `Icons.Default.Verified` |

## Proposed Changes

### Core State & Data
#### [MODIFY] [DashboardState.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DashboardState.kt)
- Revert default icon resource IDs to the original vector drawables (`ic_habit_tracker`, etc.).

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt) and [data sub-managers](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/data/)
- Revert global icon defaults.

### UI Components
#### [MODIFY] [HomeScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/home/HomeScreen.kt)
- Revert `FooterItem`, `QuickActionItem`, and `FloatingActionButton` to use `ImageVector` instead of `Painter`.

#### [MODIFY] [HomeHeader.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/home/components/HomeHeader.kt)
- Revert header icons to use standard Material icons.

#### [MODIFY] [HomeCards.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/ui/home/components/HomeCards.kt)
- Remove `isIcons8` logic and restore standard tinting.

#### [MODIFY] [AssistantActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AssistantActivity.kt)
- Revert menu and mic icons.

#### [MODIFY] [AssistantHistoryActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AssistantHistoryActivity.kt)
- Revert search, delete, and thread icons.

### Onboarding
#### [MODIFY] [OnboardingModels.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/onboarding/OnboardingModels.kt), [OnboardingActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/OnboardingActivity.kt), and [OnboardingPages.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/onboarding/OnboardingPages.kt)
- Revert `OnboardingSection` to use `ImageVector` and restore original Material icons.

## Verification Plan

### Automated Tests
- Build the project to ensure no resource or type mismatch errors.

### Manual Verification
- Verify the Home screen looks exactly as it did before the icon change.
- Ensure all icons respond correctly to theme colors (tinting).
- Verify the Onboarding flow uses standard Material icons.
