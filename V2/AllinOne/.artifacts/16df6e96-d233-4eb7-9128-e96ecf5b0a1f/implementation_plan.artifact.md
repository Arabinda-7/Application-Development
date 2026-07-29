# [COMPLETED] Implementation Plan - Isolated Mood Section Scaling

The goal is to isolate the "Current Focus" (Mood Icons) section on the Home Page so that it is strictly controlled by a specific set of settings and is unaffected by other app-wide scaling (like the "Text Font Size" setting).

## User Review Required

> [!IMPORTANT]
> This plan will decouple the Mood section's density from the rest of the app. Even if you change the global "Text Font Size" to Large, the Mood icons will NOT grow unless you specifically change the "Global Display Size", "Home Page Display Size", or "Current Focus Size".
>
> Scaling Logic:
> 1. If **Follow System Settings** is ON: Icons use the system default density (1.0x).
> 2. If OFF: Icons use `System Density * Global Scale * Home Scale * Focus Scale`.

## Proposed Changes

### [Component: UI Logic]

#### [MODIFY] [UIUtils.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/UIUtils.kt)
- Add `getIsolatedMoodDensity(state: DashboardState)` to calculate the specific density for the mood section.
- This calculation will use `Resources.getSystem().displayMetrics.density` as the baseline to ensure it's "Isolated" from any other app-level density modifications.

### [Component: State Management]

#### [MODIFY] [DashboardState.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DashboardState.kt)
- Add `isSystemAppearanceEnabled` to the data class.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/MainActivity.kt)
- Update `refreshState()` to include `isSystemAppearanceEnabled`.

### [Component: Dashboard UI]

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/HomeScreen.kt)
- Locate the "Sentiment Tracker" (Mood Icons) section.
- Use the new `getIsolatedMoodDensity` to override `LocalDensity` for this specific block of code.

## Verification Plan

### Manual Verification
1. Open **Settings > UI & Appearance**.
2. Change **Text Font Size** to "L" (Large).
    - Verify: The rest of the app text grows, but the **Mood Icons** remain their previous size.
3. Change **Global Display Size** or **Home Page Display Size**.
    - Verify: The **Mood Icons** scale accordingly.
4. Toggle **Follow System Settings**.
    - Verify: The **Mood Icons** snap back to system default size regardless of other manual app settings.
