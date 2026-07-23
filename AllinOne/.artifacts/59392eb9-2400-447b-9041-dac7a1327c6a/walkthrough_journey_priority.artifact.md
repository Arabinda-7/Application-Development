# Walkthrough - Active Journeys Prioritization

I have updated the Journey catalog to automatically pin your started programs to the top. This ensures you can easily track your current progress without searching through the entire catalog.

## Changes Made

### 🎨 UI & Logic Enhancements
- **[JourneyComponents.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/JourneyComponents.kt)**:
    - **Dynamic Sorting**: The list now splits into "Active" and "Available" sections in real-time.
    - **ACTIVE JOURNEYS Section**: A new primary section at the top that only appears if you have started at least one program.
    - **Smart Filtering**: Once a journey is started, it is automatically removed from the "Recommended" and "All Journeys" sections to prevent duplication.
    - **Information Density**: Maintained the compact, professional layout while adding the new logical grouping.

## How to Verify
1. Open the **Habit** or **Workout** section and go to the **JOURNEY** tab.
2. Select any program (e.g., *"Energy-boosting morning routine"*) and tap **START MY JOURNEY**.
3. Go back to the **JOURNEY** tab.
4. **Verified**: The program now appears at the very top under the **ACTIVE JOURNEYS** header in the theme color.
5. **Verified**: It no longer appears in the sections below.

> [!TIP]
> This new structure allows you to focus on your current multi-day goals while still keeping the rest of the catalog available for future inspiration.
