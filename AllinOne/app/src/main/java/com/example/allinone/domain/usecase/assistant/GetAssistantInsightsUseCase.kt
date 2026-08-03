package com.example.allinone.domain.usecase.assistant

import com.example.allinone.AssistantBrain
import com.example.allinone.core.utils.IntelligenceEngine
import com.example.allinone.domain.repository.*
import com.example.allinone.domain.usecase.habit.GetHabitStatisticsUseCase
import com.example.allinone.domain.usecase.finance.GetFinancialSummaryUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetAssistantInsightsUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val financeRepository: FinanceRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val noteRepository: NoteRepository,
    private val getHabitStatisticsUseCase: GetHabitStatisticsUseCase,
    private val getFinancialSummaryUseCase: GetFinancialSummaryUseCase
) {

    suspend operator fun invoke(): List<AssistantBrain.Insight> {
        val insights = mutableListOf<AssistantBrain.Insight>()
        
        val habits = habitRepository.getAllHabits().first()
        val transactions = financeRepository.getTransactions().first()
        val projects = projectRepository.getAllProjects().first()
        val tasks = taskRepository.getTasks().first()
        val financialSummary = getFinancialSummaryUseCase().first()
        val financeSettings = financeRepository.getFinanceSettings().first()

        // 1. Discipline Milestone
        val totalHabits = getHabitStatisticsUseCase.getTotalFinished(habits)
        if (totalHabits >= 100) {
            insights.add(AssistantBrain.Insight(
                "Master of Routine",
                "You've completed $totalHabits habits! Your consistency is becoming a core part of your identity.",
                "PRODUCTIVITY",
                2
            ))
        }

        // 2. Financial Impact
        val totalSavings = transactions.filter { it.type == "Saving" }.sumOf { it.amount }
        if (totalSavings >= 5000) {
            insights.add(AssistantBrain.Insight(
                "Wealth Architect",
                "You've saved a total of ${financeSettings.currency}${totalSavings.toInt()}. Your future self will thank you for this discipline.",
                "FINANCE",
                2
            ))
        }

        // 3. Project Velocity
        val completedProjects = projects.count { it.status == "Completed" }
        if (completedProjects >= 5) {
            insights.add(AssistantBrain.Insight(
                "The Finisher",
                "With $completedProjects completed projects, you've proven you have the grit to see things through to the end.",
                "PRODUCTIVITY",
                1
            ))
        }

        // 4. Momentum Insight
        val currentStreak = getHabitStatisticsUseCase.getHabitStreak(habits)
        if (currentStreak >= 3) {
            insights.add(AssistantBrain.Insight(
                "Momentum Alert",
                "You're on a $currentStreak-day streak! Protect this momentum—it's your most valuable asset today.",
                "PRODUCTIVITY",
                1
            ))
        }

        // 5. Morning Briefing: Priority Optimization
        val pendingTasks = tasks.filter { !it.isCompleted }
        if (pendingTasks.isNotEmpty()) {
            val scoredTasks = pendingTasks.map { task ->
                val age = ((System.currentTimeMillis() - task.timestamp) / (24 * 60 * 60 * 1000L)).toInt()
                val daysUntil = task.reminderTime?.let { ((it - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt() }
                task to IntelligenceEngine.calculatePriorityScore(task.priority, age, daysUntil, task.subtasks.size)
            }.sortedByDescending { it.second }.take(3)
            
            val taskNames = scoredTasks.joinToString("\n") { "• ${it.first.name}" }
            insights.add(AssistantBrain.Insight(
                "Daily Priority Briefing",
                "Based on urgency and impact, here are your top 3 focus areas for today:\n\n$taskNames",
                "MANAGEMENT",
                2
            ))
        }

        // 6. Project Deadline Risk
        val completionTimestamps = tasks.mapNotNull { it.completedTimestamp }
        val velocity = IntelligenceEngine.calculateTaskVelocity(completionTimestamps)
        
        if (velocity > 0) {
            projects.filter { project ->
                val remainingItems = project.subFeatures.count { !it.isCompleted }
                val deadline = project.deadline
                if (remainingItems > 0 && deadline != null) {
                    val daysRemaining = ((deadline - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt()
                    val estimatedDays = (remainingItems / velocity).toInt()
                    estimatedDays > daysRemaining
                } else false
            }.forEach { project ->
                insights.add(AssistantBrain.Insight(
                    "Deadline Risk Detected",
                    "The project '${project.title}' is at risk. At your current velocity, you'll finish in approx. ${((project.subFeatures.count { !it.isCompleted }) / velocity).toInt()} days, which is past the deadline.",
                    "RISK",
                    2
                ))
            }
        }

        // 7. Finance Alert
        if (financialSummary.budgetRemaining < financialSummary.monthlyBudget * 0.1) {
            insights.add(AssistantBrain.Insight(
                "Budget Warning",
                "You have less than 10% of your budget remaining. Consider reviewing your upcoming expenses.",
                "FINANCE",
                2
            ))
        }

        return insights
    }
}
