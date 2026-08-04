package com.example.allinone

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.allinone.workspace.ui.activity.WorkspaceActivity

class MainNavigationHandler(private val context: Context) {
    private var lastNavigateTime: Long = 0
    private fun canNavigate(): Boolean {
        if (System.currentTimeMillis() - lastNavigateTime < 500) return false
        lastNavigateTime = System.currentTimeMillis()
        return true
    }

    fun navigateToHabits() {
        if (!canNavigate()) return
        context.startActivity(Intent(context, HabitTrackerActivity::class.java))
    }

    fun navigateToWorkout() {
        if (!canNavigate()) return
        context.startActivity(Intent(context, WorkoutRoutineActivity::class.java))
    }

    fun navigateToTodos() {
        if (!canNavigate()) return
        context.startActivity(Intent(context, TaskActivity::class.java))
    }

    fun navigateToNotes() {
        if (!canNavigate()) return
        context.startActivity(Intent(context, NotesActivity::class.java))
    }

    fun navigateToProjects() {
        if (!canNavigate()) return
        context.startActivity(Intent(context, ProjectActivity::class.java))
    }

    fun navigateToFinance() {
        if (!canNavigate()) return
        context.startActivity(Intent(context, FinanceActivity::class.java))
    }

    fun navigateToSettings() {
        if (!canNavigate()) return
        context.startActivity(Intent(context, SettingsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
    }

    fun navigateToAssistant() {
        if (!canNavigate()) return
        context.startActivity(Intent(context, AssistantActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
    }

    fun navigateToWorkspace() {
        if (!canNavigate()) return
        context.startActivity(Intent(context, WorkspaceActivity::class.java))
    }

    fun navigateToProfile() {
        if (!canNavigate()) return
        context.startActivity(Intent(context, ProfileActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
        if (context is Activity) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                context.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.slide_in_left, R.anim.slide_out_right)
            } else {
                @Suppress("DEPRECATION")
                context.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
    }

    fun navigateToPerformanceHistory() {
        if (!canNavigate()) return
        if (DataManager.showHabitSection || DataManager.showWorkoutSection) {
            context.startActivity(Intent(context, PerformanceHistoryActivity::class.java))
        }
    }
}
