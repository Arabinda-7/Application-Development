# Implementation Plan - Dynamic Core Capabilities in Onboarding

Update the "Core Capabilities" section in the onboarding deep dive to show a varying number of features for each module, accurately reflecting the available functionality in the app.

## Proposed Changes

### [Onboarding Components]

#### [MODIFY] [OnboardingComponents.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/onboarding/OnboardingComponents.kt)
- Update `FeatureCapabilitiesGrid` with refined and varied feature lists for each section:
    - **HABITS (10)**: Custom Streaks, Heatmap Trends, Stability Index, Resilience Score, Aura Themes, Smart Notifications, Vacation Mode, Bulk Log Mode, Grace Days, Daily Reset Control.
    - **TASKS (8)**: Priority Levels, Custom Categories, Subtask Support, Timed Reminders, Auto-Archive, Advanced Search, Progress Analytics, Quick Add Actions.
    - **NOTES (9)**: Daily Journaling, Question Templates, Story Writing, Voice Input, Auto-Cleanup, Aura Colors, Character Counts, Hidden Logs, Rich Formatting.
    - **FINANCE (11)**: Monthly Budgeting, Savings Goals, Safe Spend Logic, Heatmap Analytics, Personal Ledgers, Income vs Exp, Currency Support, Category Icons, Category Colors, Ledger Entries, History Tracking.
    - **PROJECTS (7)**: Roadmaps, Milestones, Sub-features, Ideas Hub, Synergy Sync, Deadline Alerts, Progress Automation.
    - **WORKOUTS (12)**: Muscle Balance, Muscle Groups, Rest Timer, Sets & Reps, Progression Score, ACWR Analytics, Recovery Status, Volume Tracking, History Heatmap, Stability Score, Routine Management, Workout Detail.

## Verification Plan

### Manual Verification
1.  Navigate through each section's Deep Dive page in onboarding.
2.  Verify that the number of features displayed in "CORE CAPABILITIES" varies by section (e.g., Projects should have 7, while Workouts has 12).
3.  Confirm the UI layout remains compact and clean regardless of the list length.
