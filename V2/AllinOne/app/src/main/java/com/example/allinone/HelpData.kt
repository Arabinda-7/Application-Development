package com.example.allinone

data class HelpFeature(
    val title: String,
    val description: String,
    val imageRes: Int = 0,
    val imageFileName: String? = null
)

data class HelpArticle(
    val title: String,
    val summary: String,
    val content: String,
    val iconRes: Int
)

object HelpData {
    fun getGuideForSection(section: String): List<HelpFeature> {
        return when (section.uppercase()) {
            "HABITS" -> listOf(
                HelpFeature(
                    "Your Habit Dashboard",
                    "Welcome to your rituals. This live view shows your daily progress and streak maintenance. Every tick builds your momentum.",
                    imageRes = R.drawable.ic_habit_tracker,
                    imageFileName = "help_habits.png"
                ),
                HelpFeature(
                    "1. The Ritual Concept",
                    "Habits are not just tasks; they are daily rituals. Use this section to build consistency. Every completion feeds into your 'Pulse Activity' on the home screen.",
                    R.drawable.neon_progress_bar_habit
                ),
                HelpFeature(
                    "2. Maintaining Streaks",
                    "Your streak represents your discipline. The app calculates this daily. If you miss a day, the 'Grace Day' system (if configured) can protect your progress.",
                    R.drawable.ic_habit_tracker
                ),
                HelpFeature(
                    "3. Peak Performance times",
                    "Sort your habits by Morning, Afternoon, or Evening. Our 'Temporal Density' analysis helps you find which part of the day is your most productive.",
                    R.drawable.baseline_tune_24
                ),
                HelpFeature(
                    "4. Habit Science",
                    "We use Pearson Correlation to detect links between different habits. Learn how your hydration affects your morning focus over a 30-day period.",
                    R.drawable.icons8_idea_100
                ),
                HelpFeature(
                    "5. Vacation & Recovery",
                    "Switch on 'Vacation Mode' in Settings to freeze your streaks. This ensures you can rest without the psychological pressure of losing your rank.",
                    R.drawable.ic_water
                )
            )
            "WORKOUTS" -> listOf(
                HelpFeature(
                    "Training Interface",
                    "Track your physical growth here. The dashboard shows active exercises and muscle recovery status in real-time.",
                    imageRes = R.drawable.ic_fitness,
                    imageFileName = "help_workouts.png"
                ),
                HelpFeature(
                    "1. Active Mode Tracking",
                    "When starting a workout, 'Active Mode' highlights your current exercise. Choose between Sets, Reps, or Timer modes for precise progress.",
                    R.drawable.neon_progress_bar_workout
                ),
                HelpFeature(
                    "2. The recovery logic",
                    "The app implements a 48-hour recovery algorithm. After tagging a muscle group (like 'Chest'), the system monitors its status to prevent injury.",
                    R.drawable.icons8_heart_health_100
                ),
                HelpFeature(
                    "3. Muscle distribution",
                    "Balance your training by reviewing your 'Volume Analysis'. See exactly how many sets you've dedicated to each muscle group this month.",
                    R.drawable.ic_fitness
                ),
                HelpFeature(
                    "4. Calorie calculation",
                    "Calories are estimated based on tracking modes: 0.1 kcal/sec for Timer, 0.5 kcal for Reps, and 5.0 kcal per Set of heavy resistance.",
                    R.drawable.icons8_clock_100
                )
            )
            "TASKS" -> listOf(
                HelpFeature(
                    "Work Management",
                    "Organize your immediate to-do list. Categorize by Priority and context to maintain a clear mental state.",
                    imageRes = R.drawable.ic_task,
                    imageFileName = "help_tasks.png"
                ),
                HelpFeature(
                    "1. Priority Architecture",
                    "Tasks are sorted by Priority: High, Medium, and Low. This helps you apply the 'Eisenhower Matrix' logic to your daily to-do list.",
                    R.drawable.ic_task
                ),
                HelpFeature(
                    "2. Categorization",
                    "Segregate your life into 'Work', 'Personal', or 'Shopping'. This reduces cognitive load by showing only what's relevant to your current context.",
                    R.drawable.ic_project
                ),
                HelpFeature(
                    "3. Smart Reminders",
                    "Set exact alerts. The system uses 'Exact Alarms' to wake up the device and notify you at the precise moment a task is due.",
                    R.drawable.icons8_push_100
                ),
                HelpFeature(
                    "4. Workspace Cleanup",
                    "Enable 'Auto-Archive' to hide finished tasks after 7 days. This keeps your list lean and focused on upcoming objectives.",
                    R.drawable.icons8_done_100
                )
            )
            "PROJECTS" -> listOf(
                HelpFeature(
                    "Strategy Roadmap",
                    "Decompose complex goals into actionable milestones. This view bridges high-level planning with daily execution.",
                    imageRes = R.drawable.ic_project,
                    imageFileName = "help_projects.png"
                ),
                HelpFeature(
                    "1. Goal Decomposition",
                    "Projects allow you to break massive goals into 'Sub-Features'. Each node can be prioritized using our integrated 'Number Roller'.",
                    R.drawable.ic_project
                ),
                HelpFeature(
                    "2. The Roadmap View",
                    "Visualize your project as a progressive roadmap. Track percentages for each milestone to see how close you are to the finish line.",
                    R.drawable.icons8_menu_100
                ),
                HelpFeature(
                    "3. Synergy Sync",
                    "Enable 'Synergy Sync' in Settings to have project milestones automatically appear in your main To-Do list as actionable items.",
                    R.drawable.icons8_refresh_100
                ),
                HelpFeature(
                    "4. Accountability Log",
                    "Review 'Change History' to see every edit and milestone completion. This provides a clear audit trail of your hard work.",
                    R.drawable.ic_history
                )
            )
            "NOTES" -> listOf(
                HelpFeature(
                    "Notes Canvas",
                    "A space for raw ideas and structured journaling. Capture thoughts instantly using specialized templates.",
                    imageRes = R.drawable.ic_notes,
                    imageFileName = "help_notes.png"
                ),
                HelpFeature(
                    "1. Journaling Templates",
                    "Don't start from a blank page. Use 'Daily Gratitude' or 'Structured Q&A' templates to capture your thoughts in seconds.",
                    R.drawable.ic_notes
                ),
                HelpFeature(
                    "2. Privacy Layers",
                    "Mark sensitive notes as 'Hidden'. You can toggle their visibility with a global setting to keep your journal private from prying eyes.",
                    R.drawable.icons8_lock_100
                ),
                HelpFeature(
                    "3. Data Life-cycle",
                    "Use 'Auto-Cleanup' for temporary notes. Set an expiration period so the app automatically removes stale information.",
                    R.drawable.icons8_trash_100
                )
            )
            "FINANCE" -> listOf(
                HelpFeature(
                    "The Financial Vault",
                    "Manage your capital. This dashboard visualizes income, expenditure, and your personalized 'Safe Spend' target.",
                    imageRes = R.drawable.ic_finance,
                    imageFileName = "help_finance.png"
                ),
                HelpFeature(
                    "1. Financial Vault",
                    "Manage your cash flow. Input income and expenses to calculate your daily 'Safe Spend' amount shown on the Dashboard.",
                    R.drawable.money_flow
                ),
                HelpFeature(
                    "2. Independent Ledgers",
                    "Create separate ledgers for person-based tracking. Perfect for managing loans, shared rent, or specific event budgets.",
                    R.drawable.lending
                ),
                HelpFeature(
                    "3. Target Progress",
                    "Set monthly savings goals. The app provides a visual progress bar that changes color as you approach your target.",
                    R.drawable.ic_finance
                ),
                HelpFeature(
                    "4. Spending Analytics",
                    "Review category-wise spending. Identify patterns in Food, Transport, and Entertainment to optimize your budget.",
                    R.drawable.expenses
                )
            )
            "OTHERS" -> listOf(
                HelpFeature(
                    "Executive Hub",
                    "Configure your ecosystem. Adjust global scaling, themes, and security to match your operational style.",
                    imageRes = R.drawable.baseline_settings_24,
                    imageFileName = "help_home.png"
                ),
                HelpFeature(
                    "1. XP & Rank System",
                    "Your 'Identity Hub' tracks your growth. Complete tasks to earn XP. The formula `(Level^2)*100` determines your next rank.",
                    R.drawable.xp_progress_bar
                ),
                HelpFeature(
                    "2. Visual Aura",
                    "The dashboard theme is dynamic. It shifts its gradient and glow based on the 'Current Focus' emoji you select each day.",
                    R.drawable.bg_profile_glass
                ),
                HelpFeature(
                    "3. Display scaling",
                    "Adjust Font Size and Display Margins independently. Choose between XS for high density and L for maximum legibility.",
                    R.drawable.baseline_tune_24
                ),
                HelpFeature(
                    "4. Data Governance",
                    "You own your data. Export your entire ecosystem to a JSON file for backup. Restore it anytime via the Import feature.",
                    R.drawable.icons8_refresh_100
                ),
                HelpFeature(
                    "5. App Security",
                    "Enable the 'App Access Lock' to require a PIN or Biometric check every time you open the application.",
                    R.drawable.icons8_padlock_100
                )
            )
            else -> emptyList()
        }
    }

    fun getMasterGuides(): List<HelpArticle> {
        return listOf(
            HelpArticle(
                "The Master Guide to Journeys",
                "Deep dive into the science and philosophy of our 30-day programs.",
                """
                Each program is meticulously designed to move you through the three pillars of behavioral change:
                
                1. ADAPTATION (Day 1-7): Low Friction. Just show up. Build the identity.
                2. BUILDING (Day 8-21): Consistency. Resist the urge to quit. Lock in the routine.
                3. CONSOLIDATION (Day 22-30): Mastery. Standardize the results. Make it permanent.
                
                WHY IT WORKS:
                - Morning Routine: Circadian rhythm alignment.
                - Fasting: Autophagy and insulin sensitivity.
                - Sugar Detox: Dopamine receptor reset.
                
                PRO TIP: If you miss a day, don't restart! Just resume the next day. The 30-day timeline is a guide, not a prison.
                """.trimIndent(),
                R.drawable.ic_habit_tracker
            ),
            HelpArticle(
                "All-in-One Ecosystem Overview",
                "How your habits, workouts, and finances interconnect.",
                """
                The heart of your productivity system is the EXECUTIVE DASHBOARD.
                
                DYNAMIC AURA: The header gradient changes based on your Mood Log.
                SYNERGY SYNC: Project milestones automatically appear in your main To-Do list.
                SAFE SPEND: Your budget is automatically calculated by subtracting expenses from your Vault.
                
                DATA GOVERNANCE: You own your data. Export to JSON anytime via the Identity Hub.
                """.trimIndent(),
                R.drawable.ic_project
            ),
            HelpArticle(
                "Health & Recovery Science",
                "Understanding the 48-hour recovery logic and volume analysis.",
                """
                The app implements a 48-hour recovery algorithm. After tagging a muscle group, the system monitors its status to prevent injury.
                
                VOLUME MATH:
                - Calories are estimated based on tracking modes:
                - 0.1 kcal/sec for Timer
                - 0.5 kcal for Reps
                - 5.0 kcal per Set of resistance
                
                PEARSON CORRELATION: We detect links between rituals. Learn how hydration affects your morning focus over a 30-day period.
                """.trimIndent(),
                R.drawable.ic_fitness
            )
        )
    }
}
