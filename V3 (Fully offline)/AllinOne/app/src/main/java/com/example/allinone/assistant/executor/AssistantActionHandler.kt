package com.example.allinone.assistant.executor

import android.content.Context
import android.content.Intent
import com.example.allinone.*
import com.example.allinone.assistant.model.CommandAction
import com.example.allinone.data.model.*
import com.example.allinone.domain.repository.HabitRepository
import com.example.allinone.domain.usecase.finance.AddExpenseUseCase
import com.example.allinone.domain.usecase.finance.AddIncomeUseCase
import com.example.allinone.domain.usecase.habit.AddHabitUseCase
import com.example.allinone.domain.usecase.habit.TrackHabitCompletionUseCase
import com.example.allinone.domain.usecase.note.AddNoteUseCase
import com.example.allinone.domain.usecase.task.AddTaskUseCase
import com.example.allinone.domain.usecase.workout.AddWorkoutUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AssistantActionHandler: Centralizes the execution logic for all assistant commands.
 * This decouples the action logic from UI components like Activities.
 */
@Singleton
class AssistantActionHandler @Inject constructor(
    private val addTaskUseCase: AddTaskUseCase,
    private val addHabitUseCase: AddHabitUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val addWorkoutUseCase: AddWorkoutUseCase,
    private val addIncomeUseCase: AddIncomeUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val habitRepository: HabitRepository,
    private val trackHabitCompletionUseCase: TrackHabitCompletionUseCase
) {
    suspend fun executeAction(context: Context, action: CommandAction): String? {
        val payload = action.payload ?: ""
        var statusMessage: String? = null

        when (action.type) {
            "ADD_TASK" -> {
                if (payload.isNotBlank()) {
                    addTaskUseCase(Task(name = payload))
                    statusMessage = "Task saved!"
                }
            }
            "ADD_NOTE" -> {
                val parts = payload.split("|")
                val title = parts.getOrNull(0) ?: "New Note"
                val content = parts.getOrNull(1) ?: ""
                addNoteUseCase(Note(title = title, content = content))
                statusMessage = "Note saved!"
            }
            "ADD_HABIT" -> {
                val parts = payload.split("|")
                val name = parts.getOrNull(0) ?: payload
                val target = parts.getOrNull(1)?.toIntOrNull() ?: 1
                val freq = parts.getOrNull(2) ?: "Anytime"
                addHabitUseCase(Habit(name = name, isCompleted = false, frequency = freq, target = target))
                statusMessage = "Habit created!"
            }
            "ADD_WORKOUT" -> {
                val parts = payload.split("|")
                val name = parts.getOrNull(0) ?: payload
                val mode = parts.getOrNull(1) ?: "Reps"
                val target = parts.getOrNull(2)?.toIntOrNull() ?: 10
                addWorkoutUseCase(Workout(name = name, isCompleted = false, trackingMode = mode, target = target))
                statusMessage = "Workout added!"
            }
            "LOG_INCOME" -> {
                val amount = payload.toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    addIncomeUseCase(amount, "Assistant Log", "General")
                    statusMessage = "Income logged: $amount"
                }
            }
            "LOG_EXPENSE" -> {
                val amount = payload.toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    addExpenseUseCase(amount, "Assistant Log", "General")
                    statusMessage = "Expense logged: $amount"
                }
            }
            "LOG_HABIT" -> {
                val habit = habitRepository.getAllHabits().first().find { it.name.equals(payload, true) }
                if (habit != null) {
                    trackHabitCompletionUseCase(habit, habit.target, true)
                    statusMessage = "Marked habit '${habit.name}' as done!"
                } else {
                    statusMessage = "Habit '$payload' not found."
                }
            }
            "NAVIGATE" -> {
                val intent = when (payload) {
                    "FINANCE" -> Intent(context, FinanceActivity::class.java)
                    "HABITS" -> Intent(context, HabitTrackerActivity::class.java)
                    "SETTINGS" -> Intent(context, SettingsActivity::class.java)
                    "PROJECTS" -> Intent(context, ProjectActivity::class.java)
                    "NOTES" -> Intent(context, NotesActivity::class.java)
                    else -> null
                }
                intent?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(it)
                    statusMessage = "Opening $payload..."
                }
            }
        }
        return statusMessage
    }
}
