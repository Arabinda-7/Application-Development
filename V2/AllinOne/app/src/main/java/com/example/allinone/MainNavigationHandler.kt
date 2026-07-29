package com.example.allinone

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.allinone.workspace.ui.activity.WorkspaceActivity

class MainNavigationHandler(private val context: Context) {
    fun navigateToHabits() {
        context.startActivity(Intent(context, HabitTrackerActivity::class.java))
    }

    fun navigateToWorkout() {
        context.startActivity(Intent(context, WorkoutRoutineActivity::class.java))
    }

    fun navigateToTodos() {
        context.startActivity(Intent(context, TaskActivity::class.java))
    }

    fun navigateToNotes() {
        context.startActivity(Intent(context, NotesActivity::class.java))
    }

    fun navigateToProjects() {
        context.startActivity(Intent(context, ProjectActivity::class.java))
    }

    fun navigateToFinance() {
        context.startActivity(Intent(context, FinanceActivity::class.java))
    }

    fun navigateToSettings() {
        context.startActivity(Intent(context, SettingsActivity::class.java))
    }

    fun navigateToWorkspace() {
        context.startActivity(Intent(context, WorkspaceActivity::class.java))
    }

    fun navigateToProfile() {
        context.startActivity(Intent(context, ProfileActivity::class.java))
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
        if (DataManager.showHabitSection || DataManager.showWorkoutSection) {
            context.startActivity(Intent(context, PerformanceHistoryActivity::class.java))
        }
    }
}
