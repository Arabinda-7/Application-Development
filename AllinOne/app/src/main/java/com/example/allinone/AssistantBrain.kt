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
    private var nluDataset: List<NluItem> = emptyList()

    private var activeSession: Session? = null

    sealed class Session {
        data class HabitCreation(
            var step: HabitStep = HabitStep.TITLE,
            var tempName: String = "",
            var tempTarget: Int = 1,
            var tempFrequency: String = "Anytime"
        ) : Session()

        data class HabitCompletion(
            var step: CompletionStep = CompletionStep.NAME,
            var tempName: String = ""
        ) : Session()

        data class WorkoutCreation(
            var step: WorkoutStep = WorkoutStep.TITLE,
            var tempName: String = "",
            var tempMode: String = "Reps",
            var tempTarget: Int = 0,
            var tempRepsPerSet: Int = 0,
            var tempFrequency: String = "Anytime"
        ) : Session()

        data class WorkoutCompletion(
            var step: CompletionStep = CompletionStep.NAME,
            var tempName: String = ""
        ) : Session()

        data class TaskCreation(
            var step: TaskStep = TaskStep.NAME,
            var tempName: String = "",
            val tempSubtasks: MutableList<String> = mutableListOf(),
            var tempReminderDate: String? = null,
            var tempReminderTime: String? = null
        ) : Session()

        data class NoteCreation(
            var step: NoteStep = NoteStep.TITLE,
            var tempTitle: String = "",
            var tempContent: String = ""
        ) : Session()
    }

    enum class HabitStep { TITLE, CONFIRM_DEFAULT, TARGET, FREQUENCY }
    enum class CompletionStep { NAME, CONFIRM }
    enum class WorkoutStep { TITLE, MODE, TARGET, CONFIRM_DEFAULT, FREQUENCY }
    enum class TaskStep { NAME, SUBTASK_PROMPT, SUBTASK_NAME, REMINDER_PROMPT, REMINDER_DATE, REMINDER_TIME }
    enum class NoteStep { TITLE, CONTENT }

    data class ResponseItem(
        val keys: List<String>,
        val values: List<String>
    )

    data class NluItem(
        val input: String,
        val output: NluOutput
    )

    data class NluOutput(
        val action: CommandAction,
        val response: String
    )

    fun initialize(context: Context) {
        try {
            val gson = Gson()
            val resType = object : TypeToken<List<ResponseItem>>() {}.type
            val nluType = object : TypeToken<List<NluItem>>() {}.type

            val allResponses = mutableListOf<ResponseItem>()
            val allNlu = mutableListOf<NluItem>()

            context.assets.list("assistant")?.forEach { fileName ->
                try {
                    val json = context.assets.open("assistant/$fileName").bufferedReader().use { it.readText() }
                    if (fileName == "nlu_commands.json") {
                        val nluItems: List<NluItem> = gson.fromJson(json, nluType)
                        allNlu.addAll(nluItems)
                    } else if (fileName.endsWith(".json")) {
                        val resItems: List<ResponseItem> = gson.fromJson(json, resType)
                        allResponses.addAll(resItems)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AssistantBrain", "Error loading asset: $fileName", e)
                }
            }

            loadedResponses = allResponses
            nluDataset = allNlu
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
                    val remainingItems = project.subFeatures?.count { !it.isCompleted } ?: 0
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
                    "The project '${project.title}' is at risk. At your current velocity, you'll finish in approx. ${((project.subFeatures?.count { !it.isCompleted } ?: 0) / velocity).toInt()} days, which is past the deadline.",
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

        if (cmd == "cancel") {
            activeSession = null
            return CommandAction("CHAT_RESPONSE", "Operation cancelled. What else can I help you with?")
        }

        // Handle Active Sessions
        activeSession?.let { session ->
            return when (session) {
                is Session.HabitCreation -> handleHabitCreationSession(session, cmd)
                is Session.HabitCompletion -> handleHabitCompletionSession(session, cmd)
                is Session.WorkoutCreation -> handleWorkoutCreationSession(session, cmd)
                is Session.WorkoutCompletion -> handleWorkoutCompletionSession(session, cmd)
                is Session.TaskCreation -> handleTaskCreationSession(session, cmd)
                is Session.NoteCreation -> handleNoteCreationSession(session, cmd)
            }
        }

        // 1. Check exact matches in NLU dataset for high-confidence responses
        nluDataset.find { it.input.equals(command, ignoreCase = true) }?.let {
            val action = it.output.action
            action.dynamicResponse = it.output.response
            return action
        }

        // Multi-turn Triggers
        if (cmd == "create a habit" || cmd == "start a habit" || cmd == "new habit") {
            activeSession = Session.HabitCreation()
            return CommandAction("CHAT_RESPONSE", "Creating a new habit, what will be the title?")
        }

        if (cmd == "create a workout" || cmd == "start a workout" || cmd == "new workout") {
            activeSession = Session.WorkoutCreation()
            return CommandAction("CHAT_RESPONSE", "Creating a new workout, what will be the title?")
        }

        if (cmd.startsWith("mark habit") || cmd.startsWith("habit mark") || cmd == "mark as completed") {
            val name = cmd.replace("mark habit", "").replace("habit mark", "").trim()
            return if (name.isEmpty()) {
                activeSession = Session.HabitCompletion(CompletionStep.NAME)
                CommandAction("CHAT_RESPONSE", "Which habit would you like to mark as completed?")
            } else {
                val habit = DataManager.habits.find { it.name.contains(name, ignoreCase = true) }
                if (habit != null) {
                    activeSession = Session.HabitCompletion(CompletionStep.CONFIRM, habit.name)
                    CommandAction("CHAT_RESPONSE", "Marking the habit '${habit.name}' as completed?")
                } else {
                    CommandAction("CHAT_RESPONSE", "I couldn't find a habit named '$name'. Which one should I mark?")
                }
            }
        }

        if (cmd == "create a task" || cmd == "new task" || cmd == "add task") {
            activeSession = Session.TaskCreation()
            return CommandAction("CHAT_RESPONSE", "Creating a new task, what will be the title?")
        }

        if (cmd == "create a note" || cmd == "new note" || cmd == "take a note") {
            activeSession = Session.NoteCreation()
            return CommandAction("CHAT_RESPONSE", "Creating a new note, what will be the title?")
        }

        if (cmd.contains("context of the note") || cmd.contains("content of the note")) {
            val name = cmd.substringAfter("note").trim().removeSurrounding("'").removeSurrounding("\"")
            val note = DataManager.notes.find { it.title.contains(name, ignoreCase = true) }
            return if (note != null) {
                CommandAction("CHAT_RESPONSE", "The context of '${note.title}' is: ${note.content}")
            } else {
                CommandAction("CHAT_RESPONSE", "I couldn't find a note named '$name'.")
            }
        }

        if ((cmd.contains("find me a note") || cmd.contains("search for a note")) && (cmd.contains("summary") || cmd.contains("read"))) {
            val query = cmd.split("about", "named", "called").lastOrNull()?.replace("summary", "")?.replace("read", "")?.trim() ?: ""
            val note = DataManager.notes.find { it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true) }
            return if (note != null) {
                val content = if (note.content.length > 200) note.content.take(200) + "..." else note.content
                CommandAction("CHAT_RESPONSE", "I found a note titled '${note.title}'. Here is a summary: $content")
            } else {
                CommandAction("CHAT_RESPONSE", "I couldn't find any note related to '$query'.")
            }
        }

        if (cmd.contains("note names") || cmd.contains("list my notes") || cmd.contains("what are my notes")) {
            val list = DataManager.notes.joinToString("\n") { "• ${it.title}" }
            return if (list.isNotEmpty()) {
                CommandAction("CHAT_RESPONSE", "Here are your saved notes:\n\n$list")
            } else {
                CommandAction("CHAT_RESPONSE", "You haven't saved any notes yet.")
            }
        }

        if (cmd.startsWith("mark task") || cmd.startsWith("task mark") || (cmd.startsWith("mark") && cmd.contains(" task"))) {
            val name = cmd.replace("mark task", "").replace("task mark", "").replace("mark", "").replace("task", "").trim()
            val task = DataManager.tasks.find { it.name.contains(name, ignoreCase = true) }
            if (task != null) {
                return if (task.subtasks.isNotEmpty() && task.subtasks.any { !it.isCompleted }) {
                    val subs = task.subtasks.filter { !it.isCompleted }.joinToString(", ") { it.name }
                    CommandAction("CHAT_RESPONSE", "There are pending subtasks: $subs. Please mark the subtasks before marking the main task.")
                } else {
                    CommandAction("MARK_TASK_COMPLETE", task.name, "Marking the task '${task.name}' as completed?")
                }
            } else if (name.isEmpty()) {
                activeSession = Session.HabitCompletion(CompletionStep.NAME) // Reuse completion step for name prompt
                return CommandAction("CHAT_RESPONSE", "Which task would you like to mark as completed?")
            }
        }

        if (cmd.startsWith("mark subtask ") && cmd.contains(" in ")) {
            val parts = cmd.replace("mark subtask ", "").split(" in ")
            if (parts.size == 2) {
                val subName = parts[0].trim()
                val taskName = parts[1].trim()
                return CommandAction("MARK_SUBTASK_COMPLETE", "$taskName|$subName")
            }
        }

        // Detailed Habit/Workout Queries
        if (cmd.contains("incomplete habits") || (cmd.contains("habits") && cmd.contains("incomplete")) || cmd.contains("active habits")) {
            val count = DataManager.habits.count { !it.isCompleted }
            return CommandAction("CHAT_RESPONSE", "You have $count incomplete habits for today. Stay focused!")
        }

        if (cmd.contains("incomplete workouts") || (cmd.contains("workouts") && cmd.contains("incomplete")) || cmd.contains("active workouts")) {
            val count = DataManager.workouts.count { !it.isCompleted }
            return CommandAction("CHAT_RESPONSE", "You have $count incomplete workouts for today. Let's get moving!")
        }

        if (cmd.contains("active tasks") || cmd.contains("incomplete tasks") || cmd.contains("tasks for today")) {
            val count = DataManager.tasks.count { !it.isCompleted }
            return CommandAction("CHAT_RESPONSE", "You have $count active tasks to handle. You can do it!")
        }

        if (cmd.contains("task names") || cmd.contains("name of my tasks") || cmd.contains("what are my tasks")) {
            val active = DataManager.tasks.filter { !it.isCompleted }
            return if (active.isNotEmpty()) {
                val list = active.joinToString("\n") { "• ${it.name}" }
                CommandAction("CHAT_RESPONSE", "Here are your active tasks:\n\n$list")
            } else {
                CommandAction("CHAT_RESPONSE", "Your task list is clear! No active tasks right now.")
            }
        }

        if (cmd.contains("tasks in ")) {
            val category = cmd.substringAfter("tasks in ").trim()
            val list = DataManager.tasks.filter { it.category.equals(category, ignoreCase = true) && !it.isCompleted }
            return if (list.isNotEmpty()) {
                val titles = list.joinToString("\n") { "• ${it.name}" }
                CommandAction("CHAT_RESPONSE", "Tasks in $category:\n\n$titles")
            } else {
                CommandAction("CHAT_RESPONSE", "No active tasks found in the $category category.")
            }
        }

        if (cmd.contains("completed habits") || (cmd.contains("habits") && cmd.contains("completed")) || cmd.contains("how many completed")) {
            val count = DataManager.habits.count { it.isCompleted }
            return CommandAction("CHAT_RESPONSE", "You've finished $count habits today. Great job!")
        }

        if (cmd.contains("completed workouts") || (cmd.contains("workouts") && cmd.contains("completed"))) {
            val count = DataManager.workouts.count { it.isCompleted }
            return CommandAction("CHAT_RESPONSE", "You've finished $count workouts today. Keep it up!")
        }

        if (cmd.contains("total habits") || (cmd.contains("habits") && cmd.contains("total"))) {
            val count = DataManager.habits.size
            return CommandAction("CHAT_RESPONSE", "You have a total of $count habits in your tracker.")
        }

        if (cmd.contains("total workouts") || (cmd.contains("workouts") && cmd.contains("total"))) {
            val count = DataManager.workouts.size
            return CommandAction("CHAT_RESPONSE", "You have a total of $count workouts in your list.")
        }

        // Time-based Availability Queries
        if (cmd.contains("available in") || cmd.contains("show morning") || cmd.contains("show evening") || cmd.contains("show afternoon")) {
            val time = when {
                cmd.contains("morning") -> "Morning"
                cmd.contains("afternoon") -> "Afternoon"
                cmd.contains("evening") -> "Evening"
                else -> "Anytime"
            }
            val isHabit = cmd.contains("habit")
            val isWorkout = cmd.contains("workout")

            return if (isHabit) {
                val list = DataManager.habits.filter { it.frequency.equals(time, ignoreCase = true) && !it.isCompleted }.joinToString("\n") { "• ${it.name}" }
                if (list.isNotEmpty()) CommandAction("CHAT_RESPONSE", "Active $time habits:\n\n$list")
                else CommandAction("CHAT_RESPONSE", "No active habits for the $time.")
            } else if (isWorkout) {
                val list = DataManager.workouts.filter { it.frequency.equals(time, ignoreCase = true) && !it.isCompleted }.joinToString("\n") { "• ${it.name}" }
                if (list.isNotEmpty()) CommandAction("CHAT_RESPONSE", "Active $time workouts:\n\n$list")
                else CommandAction("CHAT_RESPONSE", "No active workouts for the $time.")
            } else {
                CommandAction("CHAT_RESPONSE", "Would you like to see habits or workouts for the $time?")
            }
        }

        // Workout Progress Interaction
        if (cmd.startsWith("add ") && (cmd.contains(" reps") || cmd.contains(" sets") || cmd.contains(" min") || cmd.contains(" sec"))) {
            val parts = cmd.split(" in ")
            if (parts.size == 2) {
                val valPart = parts[0].replace("add ", "").trim()
                val workoutName = parts[1].trim()
                val num = valPart.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
                val workout = DataManager.workouts.find { it.name.contains(workoutName, ignoreCase = true) }
                if (workout != null) {
                    var increment = num
                    if (valPart.contains("min")) increment *= 60
                    return CommandAction("UPDATE_WORKOUT_PROGRESS", "${workout.name}|$increment", "Added $num to ${workout.name}!")
                }
            }
        }

        if (cmd.startsWith("finish ") || cmd.startsWith("complete ")) {
            val name = cmd.replace("finish ", "").replace("complete ", "").trim()
            val workout = DataManager.workouts.find { it.name.contains(name, ignoreCase = true) }
            return if (workout != null) {
                activeSession = Session.WorkoutCompletion(CompletionStep.CONFIRM, workout.name)
                CommandAction("CHAT_RESPONSE", "Marking the workout '${workout.name}' as completed?")
            } else {
                activeSession = Session.WorkoutCompletion(CompletionStep.NAME)
                CommandAction("CHAT_RESPONSE", "Which workout would you like to finish?")
            }
        }

        if (cmd.contains("how many") && cmd.contains(" in ") && (cmd.contains("reps") || cmd.contains("sets") || cmd.contains("go"))) {
            val workoutName = cmd.split(" in ").last().trim()
            val workout = DataManager.workouts.find { it.name.contains(workoutName, ignoreCase = true) }
            if (workout != null) {
                val remaining = Math.max(0, workout.target - workout.progress)
                val unit = if (workout.trackingMode == "Timer") "seconds" else workout.trackingMode
                return CommandAction("CHAT_RESPONSE", "$remaining $unit to go in ${workout.name}.")
            }
        }

        // 2. Pattern Matching / Regex for parameters
        return when {
            cmd.startsWith("add habit") -> {
                val name = cmd.replace("add habit", "").trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                CommandAction("ADD_HABIT", name)
            }
            cmd.contains("log") && cmd.contains("income") -> {
                val amountStr = cmd.replace(Regex("[^0-9.]"), "")
                CommandAction("LOG_INCOME", amountStr)
            }
            cmd.startsWith("add task") || cmd.startsWith("remind me to") -> {
                val name = cmd.replace("add task", "").replace("remind me to", "").trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                if (cmd.contains("with subtasks")) {
                    val parts = name.split("with subtasks")
                    val parent = parts[0].trim()
                    val subs = parts.getOrNull(1)?.split(",")?.map { it.trim() }?.joinToString("|") ?: ""
                    CommandAction("CREATE_NESTED_TASK", "$parent:$subs")
                } else {
                    CommandAction("ADD_TASK", name)
                }
            }
            cmd.startsWith("add workout") -> {
                val name = cmd.replace("add workout", "").trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                CommandAction("ADD_WORKOUT", name)
            }
            cmd.startsWith("add note") || cmd.startsWith("take a quick note") || cmd.startsWith("take a note") -> {
                val name = cmd.replace("add note", "").replace("take a quick note", "").replace("take a note", "").trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                CommandAction("ADD_NOTE", name)
            }
            cmd.contains("search notes for") -> {
                val query = cmd.substringAfter("search notes for").trim().removeSurrounding("'").removeSurrounding("\"")
                CommandAction("SEARCH_NOTES", query)
            }
            cmd.contains("log") && (cmd.contains("expense") || cmd.contains("cash")) -> {
                val amountStr = cmd.replace(Regex("[^0-9.]"), "")
                CommandAction("LOG_EXPENSE", amountStr)
            }
            cmd.startsWith("log mood") || cmd.startsWith("log today's mood") -> {
                val mood = cmd.replace("log today's mood", "").replace("log mood", "").replace("as", "").trim()
                CommandAction("LOG_MOOD", mood)
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
            cmd.contains("project status") || cmd.contains("how is my project") || cmd.contains("project progress") || cmd.contains("deadline") -> {
                val projectName = cmd.split(Regex("project|for")).lastOrNull()?.trim()?.removeSuffix("due")?.removeSuffix("deadline")?.trim()
                CommandAction("PROJECT_REPORT", projectName ?: "")
            }
            cmd == "help" || cmd == "what can you do" || cmd == "how to use" || cmd == "guide" -> {
                CommandAction("CHAT_RESPONSE", "I can help you in many ways: \n\n" +
                        "• **Track Habits**: 'Add habit Drink Water'\n" +
                        "• **Manage Finances**: 'Log 500 expense' or 'Log 5000 income'\n" +
                        "• **Tasks**: 'Add task Buy Milk with subtasks eggs, bread'\n" +
                        "• **Notes**: 'Search notes for AI ideas'\n" +
                        "• **Insights**: View your momentum in the Intelligent Feed.\n\n" +
                        "I'm completely offline and private. Type 'Help' anytime to see this list!")
            }
            else -> {
                val chatResponse = getChatResponse(cmd)
                if (chatResponse != null) CommandAction("CHAT_RESPONSE", chatResponse) else null
            }
        }
    }

    private fun handleHabitCreationSession(session: Session.HabitCreation, cmd: String): CommandAction {
        return when (session.step) {
            HabitStep.TITLE -> {
                session.tempName = cmd.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                session.step = HabitStep.CONFIRM_DEFAULT
                CommandAction("CHAT_RESPONSE", "Other data will be default?")
            }
            HabitStep.CONFIRM_DEFAULT -> {
                if (cmd.contains("yes") || cmd == "y") {
                    val habitName = session.tempName
                    activeSession = null
                    CommandAction("ADD_HABIT", habitName, "Habit '$habitName' is created!")
                } else if (cmd.contains("no") || cmd == "n") {
                    session.step = HabitStep.TARGET
                    CommandAction("CHAT_RESPONSE", "What is the daily target (e.g. 10)?")
                } else {
                    CommandAction("CHAT_RESPONSE", "Please answer with 'Yes' or 'No'.")
                }
            }
            HabitStep.TARGET -> {
                val target = cmd.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 1
                session.tempTarget = target
                session.step = HabitStep.FREQUENCY
                CommandAction("CHAT_RESPONSE", "When do you want to do this? (Morning, Afternoon, Evening, Anytime)")
            }
            HabitStep.FREQUENCY -> {
                val freq = when {
                    cmd.contains("morning") -> "Morning"
                    cmd.contains("afternoon") -> "Afternoon"
                    cmd.contains("evening") -> "Evening"
                    else -> "Anytime"
                }
                val habitName = session.tempName
                val habitTarget = session.tempTarget
                activeSession = null
                CommandAction("ADD_HABIT", "$habitName|$habitTarget|$freq", "Habit '$habitName' created with target $habitTarget for $freq!")
            }
        }
    }

    private fun handleWorkoutCreationSession(session: Session.WorkoutCreation, cmd: String): CommandAction {
        return when (session.step) {
            WorkoutStep.TITLE -> {
                session.tempName = cmd.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                session.step = WorkoutStep.MODE
                CommandAction("CHAT_RESPONSE", "What will be the goal? (Reps, Sets, or Timer)")
            }
            WorkoutStep.MODE -> {
                session.tempMode = when {
                    cmd.contains("rep") -> "Reps"
                    cmd.contains("set") -> "Sets"
                    cmd.contains("time") -> "Timer"
                    else -> "Reps"
                }
                session.step = WorkoutStep.TARGET
                val prompt = if (session.tempMode == "Sets") "How many sets and reps per set? (e.g. 3, 10)" else "What is the target value?"
                CommandAction("CHAT_RESPONSE", prompt)
            }
            WorkoutStep.TARGET -> {
                if (session.tempMode == "Sets") {
                    val nums = cmd.split(Regex("[^0-9]")).filter { it.isNotEmpty() }.mapNotNull { it.toIntOrNull() }
                    session.tempTarget = nums.getOrNull(0) ?: 3
                    session.tempRepsPerSet = nums.getOrNull(1) ?: 10
                } else {
                    var num = cmd.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 10
                    if (session.tempMode == "Timer" && cmd.contains("min")) num *= 60
                    session.tempTarget = num
                }
                session.step = WorkoutStep.CONFIRM_DEFAULT
                CommandAction("CHAT_RESPONSE", "Other data will be default?")
            }
            WorkoutStep.CONFIRM_DEFAULT -> {
                if (cmd.contains("yes") || cmd == "y") {
                    val name = session.tempName
                    val mode = session.tempMode
                    val target = session.tempTarget
                    val rps = session.tempRepsPerSet
                    activeSession = null
                    val payload = "$name|$mode|$target|$rps|Anytime"
                    CommandAction("ADD_WORKOUT", payload, "Workout '$name' with $target $mode created!")
                } else {
                    session.step = WorkoutStep.FREQUENCY
                    CommandAction("CHAT_RESPONSE", "When do you want to do this? (Morning, Afternoon, Evening, Anytime)")
                }
            }
            WorkoutStep.FREQUENCY -> {
                val freq = when {
                    cmd.contains("morning") -> "Morning"
                    cmd.contains("afternoon") -> "Afternoon"
                    cmd.contains("evening") -> "Evening"
                    else -> "Anytime"
                }
                val name = session.tempName
                val mode = session.tempMode
                val target = session.tempTarget
                val rps = session.tempRepsPerSet
                activeSession = null
                val payload = "$name|$mode|$target|$rps|$freq"
                CommandAction("ADD_WORKOUT", payload, "Workout '$name' created for $freq!")
            }
        }
    }

    private fun handleTaskCreationSession(session: Session.TaskCreation, cmd: String): CommandAction {
        return when (session.step) {
            TaskStep.NAME -> {
                session.tempName = cmd.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                session.step = TaskStep.SUBTASK_PROMPT
                CommandAction("CHAT_RESPONSE", "Any subtasks for '${session.tempName}'?")
            }
            TaskStep.SUBTASK_PROMPT -> {
                if (cmd.contains("yes") || cmd == "y") {
                    session.step = TaskStep.SUBTASK_NAME
                    CommandAction("CHAT_RESPONSE", "What is the subtask name?")
                } else {
                    session.step = TaskStep.REMINDER_PROMPT
                    CommandAction("CHAT_RESPONSE", "Do you want to set a reminder?")
                }
            }
            TaskStep.SUBTASK_NAME -> {
                session.tempSubtasks.add(cmd.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
                session.step = TaskStep.SUBTASK_PROMPT
                CommandAction("CHAT_RESPONSE", "Added subtask. Any more subtasks?")
            }
            TaskStep.REMINDER_PROMPT -> {
                if (cmd.contains("yes") || cmd == "y") {
                    session.step = TaskStep.REMINDER_DATE
                    CommandAction("CHAT_RESPONSE", "What date? (e.g. today, tomorrow, or YYYYMMDD)")
                } else {
                    finalizeTaskCreation(session)
                }
            }
            TaskStep.REMINDER_DATE -> {
                session.tempReminderDate = cmd
                session.step = TaskStep.REMINDER_TIME
                CommandAction("CHAT_RESPONSE", "What time? (e.g. 10:00 AM, 5 PM)")
            }
            TaskStep.REMINDER_TIME -> {
                session.tempReminderTime = cmd
                finalizeTaskCreation(session)
            }
        }
    }

    private fun handleNoteCreationSession(session: Session.NoteCreation, cmd: String): CommandAction {
        return when (session.step) {
            NoteStep.TITLE -> {
                session.tempTitle = cmd.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                session.step = NoteStep.CONTENT
                CommandAction("CHAT_RESPONSE", "What is the content of the note?")
            }
            NoteStep.CONTENT -> {
                val title = session.tempTitle
                val content = cmd
                activeSession = null
                CommandAction("ADD_NOTE", "$title|$content", "Note '$title' is saved!")
            }
        }
    }

    private fun finalizeTaskCreation(session: Session.TaskCreation): CommandAction {
        val name = session.tempName
        val subs = session.tempSubtasks.joinToString(",")
        
        var reminderTimestamp: Long? = null
        if (session.tempReminderDate != null && session.tempReminderTime != null) {
            // Very basic parser for today/tomorrow + HH:mm
            val cal = Calendar.getInstance()
            if (session.tempReminderDate!!.contains("tomorrow")) cal.add(Calendar.DAY_OF_YEAR, 1)
            
            val time = session.tempReminderTime!!.lowercase()
            var hour = time.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 9
            if (time.contains("pm") && hour < 12) hour += 12
            if (time.contains("am") && hour == 12) hour = 0
            
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, 0)
            reminderTimestamp = cal.timeInMillis
        }
        
        activeSession = null
        val payload = "$name|$subs|${reminderTimestamp ?: ""}"
        return CommandAction("ADD_TASK", payload, "Task '$name' is saved!")
    }

    private fun handleHabitCompletionSession(session: Session.HabitCompletion, cmd: String): CommandAction {
        return when (session.step) {
            CompletionStep.NAME -> {
                val habit = DataManager.habits.find { it.name.contains(cmd, ignoreCase = true) }
                if (habit != null) {
                    session.tempName = habit.name
                    session.step = CompletionStep.CONFIRM
                    CommandAction("CHAT_RESPONSE", "Marking the habit '${habit.name}' as completed?")
                } else {
                    CommandAction("CHAT_RESPONSE", "I couldn't find a habit matching '$cmd'. Please try another name or say 'cancel'.")
                }
            }
            CompletionStep.CONFIRM -> {
                if (cmd.contains("yes") || cmd == "y") {
                    val name = session.tempName
                    activeSession = null
                    CommandAction("LOG_HABIT", name, "Habit '$name' marked as completed!")
                } else if (cmd.contains("no") || cmd == "n") {
                    activeSession = null
                    CommandAction("CHAT_RESPONSE", "Okay, I won't mark it. What else can I do?")
                } else {
                    CommandAction("CHAT_RESPONSE", "Please confirm with 'Yes' or 'No'.")
                }
            }
        }
    }

    private fun handleWorkoutCompletionSession(session: Session.WorkoutCompletion, cmd: String): CommandAction {
        return when (session.step) {
            CompletionStep.NAME -> {
                val workout = DataManager.workouts.find { it.name.contains(cmd, ignoreCase = true) }
                if (workout != null) {
                    session.tempName = workout.name
                    session.step = CompletionStep.CONFIRM
                    CommandAction("CHAT_RESPONSE", "Marking the workout '${workout.name}' as completed?")
                } else {
                    CommandAction("CHAT_RESPONSE", "I couldn't find a workout matching '$cmd'. Please try another name or say 'cancel'.")
                }
            }
            CompletionStep.CONFIRM -> {
                if (cmd.contains("yes") || cmd == "y") {
                    val name = session.tempName
                    activeSession = null
                    CommandAction("COMPLETE_WORKOUT", name, "Workout '$name' marked as completed!")
                } else if (cmd.contains("no") || cmd == "n") {
                    activeSession = null
                    CommandAction("CHAT_RESPONSE", "Okay, I won't mark it. What else can I do?")
                } else {
                    CommandAction("CHAT_RESPONSE", "Please confirm with 'Yes' or 'No'.")
                }
            }
        }
    }

    private fun getChatResponse(cmd: String): String? {
        val sanitizedCmd = cmd.lowercase().trim().replace(Regex("[^a-z0-9 ]"), "")
        
        // 1. First, check our extensive knowledge base
        val match = loadedResponses.find { item ->
            item.keys.any { key -> 
                val sanitizedKey = key.lowercase().replace(Regex("[^a-z0-9 ]"), "")
                sanitizedCmd.contains(sanitizedKey) || sanitizedKey.contains(sanitizedCmd)
            }
        }
        if (match != null && match.values.isNotEmpty()) return match.values.random()

        // 2. Handle Impact Summary / Progress queries
        if (sanitizedCmd.contains("impact") || sanitizedCmd.contains("summary") || sanitizedCmd.contains("progress") || sanitizedCmd.contains("how am i doing")) {
            val habits = DataManager.getTotalHabitsFinished()
            val savings = synchronized(DataManager.transactions) {
                DataManager.transactions.filter { it.type == "Saving" }.sumOf { it.amount }
            }
            val projects = synchronized(DataManager.projects) {
                DataManager.projects.count { it.status == "Completed" }
            }
            
            val responses = listOf(
                "Here's your impact summary: You've mastered $habits habits, saved ${DataManager.financeCurrency}${savings.toInt()}, and completed $projects major projects. Overall, your momentum is looking strong!",
                "You're making great progress! So far, you've completed $habits habits and $projects projects, with ${DataManager.financeCurrency}${savings.toInt()} in savings. Keep it up!",
                "Current stats check: $habits habits done, $projects projects finished, and ${DataManager.financeCurrency}${savings.toInt()} saved. You're on fire!"
            )
            return responses.random()
        }

        // 3. Fallback to hardcoded legacy responses
        return when {
            cmd.contains("good morning") -> listOf(
                "Good morning! Since I'm running offline, I might not have real-time weather updates, but I'm fully ready to help you organize your day.",
                "Morning! Ready to tackle your habits and tasks today? I'm standing by.",
                "Good morning! Let's make today productive. What's first on the list?"
            ).random()
            
            cmd.contains("hello") || cmd.contains("hi there") -> listOf(
                "Hello! I'm operating completely locally right now. What can I help you draft, organize, or calculate?",
                "Hi! I'm your local AI assistant. How's your day going?",
                "Hello there! Ready to get some work done?"
            ).random()
            
            cmd.contains("good evening") -> listOf(
                "Good evening! Hope your day went well. Even offline, I'm here to help you wind down or plan for tomorrow.",
                "Good evening! Ready to review your progress for today?",
                "Evening! How did your tasks and habits go today?"
            ).random()
            
            cmd.contains("good night") -> listOf(
                "Good night! Sleep well, and I'll be right here whenever you need me tomorrow.",
                "Good night! Don't forget to review your day before you sleep.",
                "Rest well! Tomorrow is a fresh start for your goals."
            ).random()
            
            cmd.contains("thanks") || cmd.contains("thank you") -> listOf(
                "You're very welcome! I'm always happy to help. Let me know if anything else comes up.",
                "Anytime! That's what I'm here for.",
                "Glad I could help. What else is on your mind?"
            ).random()
            
            else -> null
        }
    }

    data class CommandAction(
        val type: String,
        val payload: String,
        var dynamicResponse: String? = null
    )

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
