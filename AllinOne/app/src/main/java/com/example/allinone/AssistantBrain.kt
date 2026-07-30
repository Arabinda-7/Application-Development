package com.example.allinone

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * AssistantBrain: The logic layer that maps raw analytics to human-friendly advice.
 */
object AssistantBrain {

    private var loadedResponses: List<ResponseItem> = emptyList()

    data class ResponseItem(
        val keys: List<String>,
        val value: String
    )

    fun initialize(context: Context) {
        try {
            val jsonString = context.assets.open("assistant_responses.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<ResponseItem>>() {}.type
            loadedResponses = Gson().fromJson(jsonString, type)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    data class Insight(
        val title: String,
        val description: String,
        val type: String, // "FINANCE", "PRODUCTIVITY", "MINDSET"
        val importance: Int // 0-2 (Low, Med, High)
    )

    /**
     * Scans the user's data and generates a list of actionable insights.
     */
    suspend fun generateInsights(context: Context): List<Insight> {
        val insights = mutableListOf<Insight>()

        // 1. Discipline Milestone
        val totalHabits = DataManager.getTotalHabitsFinished()
        if (totalHabits >= 100) {
            insights.add(Insight(
                "Master of Routine",
                "You've completed $totalHabits habits! Your consistency is becoming a core part of your identity.",
                "PRODUCTIVITY",
                2
            ))
        }

        // 2. Financial Impact
        val totalSavings = synchronized(DataManager.transactions) {
            DataManager.transactions.filter { it.type == "Saving" }.sumOf { it.amount }
        }
        if (totalSavings >= 5000) {
            insights.add(Insight(
                "Wealth Architect",
                "You've saved a total of ${DataManager.financeCurrency}${totalSavings.toInt()}. Your future self will thank you for this discipline.",
                "FINANCE",
                2
            ))
        }

        // 3. Project Velocity
        val completedProjects = synchronized(DataManager.projects) {
            DataManager.projects.count { it.status == "Completed" }
        }
        if (completedProjects >= 5) {
            insights.add(Insight(
                "The Finisher",
                "With $completedProjects completed projects, you've proven you have the grit to see things through to the end.",
                "PRODUCTIVITY",
                1
            ))
        }

        // 4. Momentum Insight
        val currentStreak = DataManager.getCurrentStreak()
        if (currentStreak >= 3) {
            insights.add(Insight(
                "Momentum Alert",
                "You're on a $currentStreak-day streak! Protect this momentum—it's your most valuable asset today.",
                "PRODUCTIVITY",
                1
            ))
        }

        // 5. Morning Briefing: Priority Optimization
        val pendingTasks = synchronized(DataManager.tasks) { DataManager.tasks.filter { !it.isCompleted } }
        if (pendingTasks.isNotEmpty()) {
            val scoredTasks = pendingTasks.map { task ->
                val age = ((System.currentTimeMillis() - task.timestamp) / (24 * 60 * 60 * 1000L)).toInt()
                val daysUntil = task.reminderTime?.let { ((it - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt() }
                task to IntelligenceEngine.calculatePriorityScore(task.priority, age, daysUntil, task.subtasks.size)
            }.sortedByDescending { it.second }.take(3)
            
            val taskNames = scoredTasks.joinToString("\n") { "• ${it.first.name}" }
            insights.add(Insight(
                "Daily Priority Briefing",
                "Based on urgency and impact, here are your top 3 focus areas for today:\n\n$taskNames",
                "MANAGEMENT",
                2
            ))
        }

        // 6. Project Deadline Risk: Velocity-based Forecasting
        val completionTimestamps = synchronized(DataManager.tasks) { 
            DataManager.tasks.mapNotNull { it.completedTimestamp } 
        }
        val velocity = IntelligenceEngine.calculateTaskVelocity(completionTimestamps)
        
        if (velocity > 0) {
            val riskyProjects = synchronized(DataManager.projects) {
                DataManager.projects.filter { project ->
                    val remainingItems = project.subFeatures.count { !it.isCompleted }
                    val deadline = project.deadline
                    if (remainingItems > 0 && deadline != null) {
                        val daysRemaining = ((deadline - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt()
                        val estimatedDays = (remainingItems / velocity).toInt()
                        estimatedDays > daysRemaining
                    } else false
                }
            }

            riskyProjects.forEach { project ->
                insights.add(Insight(
                    "Deadline Risk Detected",
                    "The project '${project.title}' is at risk. At your current velocity, you'll finish in approx. ${(project.subFeatures.count { !it.isCompleted } / velocity).toInt()} days, which is past the deadline.",
                    "RISK",
                    2
                ))
            }
        }

        // 7. Finance Insight: Burn Rate / Forecast
        val spendingSeries = getDailySpendingSeries()
        if (spendingSeries.size >= 3) {
            val predictedSpend = IntelligenceEngine.predictNextValue(spendingSeries)
            val currentBudget = DataManager.monthlyBudget
            val currentSpent = DataManager.getCurrentMonthExpenditure()
            val remaining = currentBudget - currentSpent
            
            if (predictedSpend > 0 && remaining > 0) {
                val daysToExhaustion = (remaining / predictedSpend).toInt()
                if (daysToExhaustion < 7) {
                    insights.add(Insight(
                        "Budget Alert",
                        "At your current rate, your budget will run out in approx. $daysToExhaustion days.",
                        "FINANCE",
                        2
                    ))
                }
            }
        }

        // 2. Productivity Insight: Mood Correlation
        val moodProductivityData = getMoodProductivitySeries()
        if (moodProductivityData.first.size >= 5) {
            val correlation = IntelligenceEngine.calculateCorrelation(moodProductivityData.first, moodProductivityData.second)
            if (correlation > 0.5) {
                insights.add(Insight(
                    "Success Driver Found",
                    "There's a strong link between your mood and productivity. You perform significantly better on 'Positive Mood' days.",
                    "PRODUCTIVITY",
                    1
                ))
            }
        }

        // 3. Mindset Insight: Sentiment Analysis
        val recentNotes = DataManager.notes.take(5).joinToString(" ") { it.content }
        if (recentNotes.isNotEmpty()) {
            val sentiment = IntelligenceEngine.analyzeSentiment(recentNotes)
            if (sentiment < -0.2) {
                insights.add(Insight(
                    "Mindset Check",
                    "Your recent notes show a dip in sentiment. Consider a short break to reset your focus.",
                    "MINDSET",
                    1
                ))
            }
        }

        // 4. Cold Start / Onboarding Insights
        if (insights.isEmpty()) {
            insights.add(Insight(
                "Welcome to AI Insights",
                "Start logging your daily mood and productivity to unlock advanced behavioral correlations.",
                "ONBOARDING",
                0
            ))
            insights.add(Insight(
                "Financial Discovery",
                "Log at least 3 expenses this week to see your predicted budget exhaustion date.",
                "ONBOARDING",
                0
            ))
        }

        return insights
    }

    /**
     * Intent Recognition: Parses natural language commands into actions.
     */
    fun parseCommand(command: String): CommandAction? {
        val cmd = command.lowercase().trim()
        return when {
            cmd.startsWith("add habit") -> {
                val name = cmd.replace("add habit", "").trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                CommandAction("ADD_HABIT", name)
            }
            cmd.startsWith("add task") || cmd.startsWith("remind me to") -> {
                val name = cmd.replace("add task", "").replace("remind me to", "").trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                CommandAction("ADD_TASK", name)
            }
            cmd.startsWith("add workout") -> {
                val name = cmd.replace("add workout", "").trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                CommandAction("ADD_WORKOUT", name)
            }
            cmd.startsWith("add note") || cmd.startsWith("take a quick note") || cmd.startsWith("take a note") -> {
                val name = cmd.replace("add note", "").replace("take a quick note", "").replace("take a note", "").trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                CommandAction("ADD_NOTE", name)
            }
            cmd.contains("log") && (cmd.contains("expense") || cmd.contains("cash")) -> {
                val amountStr = cmd.replace(Regex("[^0-9.]"), "")
                CommandAction("LOG_EXPENSE", amountStr)
            }
            cmd.startsWith("set budget") -> {
                val amountStr = cmd.replace(Regex("[^0-9.]"), "")
                CommandAction("SET_BUDGET", amountStr)
            }
            cmd.contains("start workout") || cmd.contains("go to workout") -> {
                CommandAction("START_WORKOUT", "")
            }
            cmd.contains("show finance") || cmd.contains("open finance") -> {
                CommandAction("NAVIGATE", "FINANCE")
            }
            cmd.contains("show habits") || cmd.contains("go to habits") -> {
                CommandAction("NAVIGATE", "HABITS")
            }
            cmd.contains("show settings") || cmd.contains("open settings") -> {
                CommandAction("NAVIGATE", "SETTINGS")
            }
            cmd.contains("project status") || cmd.contains("how is my project") || cmd.contains("project progress") -> {
                val projectName = cmd.split("project").lastOrNull()?.trim()
                CommandAction("PROJECT_REPORT", projectName ?: "")
            }
            cmd == "help" || cmd == "what can you do" || cmd == "how to use" || cmd == "guide" -> {
                CommandAction("CHAT_RESPONSE", "I can help you in many ways: \n\n" +
                        "• **Track Habits**: 'Add habit Drink Water'\n" +
                        "• **Manage Finances**: 'Log 500 expense'\n" +
                        "• **Tasks**: 'Add task Buy Milk'\n" +
                        "• **Brainstorm**: 'Take a note: Business idea...'\n" +
                        "• **Insights**: View your momentum in the Intelligent Feed.\n\n" +
                        "I'm completely offline and private. Type 'Help' anytime to see this list!")
            }
            else -> {
                val chatResponse = getChatResponse(cmd)
                if (chatResponse != null) CommandAction("CHAT_RESPONSE", chatResponse) else null
            }
        }
    }

    private fun getChatResponse(cmd: String): String? {
        // 1. First, check our extensive knowledge base
        val match = loadedResponses.find { item ->
            item.keys.any { key -> cmd.contains(key.lowercase()) }
        }
        if (match != null) return match.value

        // 2. Handle Impact Summary / Progress queries
        if (cmd.contains("impact") || cmd.contains("summary") || cmd.contains("progress") || cmd.contains("how am i doing")) {
            val habits = DataManager.getTotalHabitsFinished()
            val savings = synchronized(DataManager.transactions) {
                DataManager.transactions.filter { it.type == "Saving" }.sumOf { it.amount }
            }
            val projects = synchronized(DataManager.projects) {
                DataManager.projects.count { it.status == "Completed" }
            }
            
            return "Here's your impact summary: You've mastered $habits habits, saved ${DataManager.financeCurrency}${savings.toInt()}, and completed $projects major projects. Overall, your momentum is looking strong!"
        }

        // 3. Fallback to hardcoded legacy responses
        return when {
            cmd.contains("good morning") -> "Good morning! Since I'm running offline, I might not have real-time weather updates, but I'm fully ready to help you organize your day."
            cmd.contains("hello") || cmd.contains("hi there") -> "Hello! I'm operating completely locally right now. What can I help you draft, organize, or calculate?"
            cmd.contains("good evening") -> "Good evening! Hope your day went well. Even offline, I'm here to help you wind down or plan for tomorrow."
            cmd.contains("good night") -> "Good night! Sleep well, and I'll be right here whenever you need me tomorrow."
            
            cmd.contains("thanks") || cmd.contains("thank you") -> "You're very welcome! I'm always happy to help. Let me know if anything else comes up."
            else -> null
        }
    }

    data class CommandAction(val type: String, val payload: String)

    private fun getDailySpendingSeries(): List<Double> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        // Group transactions by day of month
        val dailyMap = DataManager.transactions
            .filter { 
                val transCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                it.type == "Expense" && 
                transCal.get(Calendar.MONTH) == currentMonth && 
                transCal.get(Calendar.YEAR) == currentYear
            }
            .groupBy { 
                Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.DAY_OF_MONTH)
            }
            .mapValues { it.value.sumOf { t -> t.amount } }

        val today = calendar.get(Calendar.DAY_OF_MONTH)
        return (1..today).map { dailyMap.getOrDefault(it, 0.0) }
    }

    private fun getMoodProductivitySeries(): Pair<List<Double>, List<Double>> {
        val moods = mutableListOf<Double>()
        val productivity = mutableListOf<Double>()

        // Map mood emojis to numbers: 🔥=5, ⚡=4, 🧘=3, 💼=2, 😴=1
        val moodMap = mapOf("🔥" to 5.0, "⚡" to 4.0, "🧘" to 3.0, "💼" to 2.0, "😴" to 1.0)

        DataManager.history.forEach { (date, dayData) ->
            val moodEmoji = DataManager.dailyMoods[date]
            val moodVal = moodMap[moodEmoji]
            if (moodVal != null) {
                val total = dayData.totalHabits + dayData.totalWorkouts
                if (total > 0) {
                    val score = ((dayData.habitsCompleted + dayData.workoutsCompleted).toDouble() / total) * 100
                    moods.add(moodVal)
                    productivity.add(score)
                }
            }
        }

        return Pair(moods, productivity)
    }
}
