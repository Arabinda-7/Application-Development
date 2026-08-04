package com.example.allinone.ui.workout

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.allinone.*
import com.example.allinone.domain.repository.WorkoutSettings

object WorkoutUiHandler {

    fun showSettingsPopup(
        activity: Activity,
        anchor: View,
        viewModel: WorkoutViewModel,
        onShowHistory: () -> Unit
    ) {
        val inflater = LayoutInflater.from(activity)
        val menuView = inflater.inflate(R.layout.layout_menu_workout, null, false)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        val menuToggle = menuView.findViewById<View>(R.id.menu_toggle_completed)
        val tvToggle = menuView.findViewById<TextView>(R.id.tv_toggle_completed)
        val ivToggle = menuView.findViewById<ImageView>(R.id.iv_toggle_completed)
        
        val isShowCompleted = DataManager.workoutShowCompleted
        menuToggle.visibility = View.VISIBLE
        tvToggle.text = if (isShowCompleted) "HIDE COMPLETED" else "SHOW COMPLETED"
        ivToggle.setImageResource(if (isShowCompleted) android.R.drawable.ic_menu_view else android.R.drawable.ic_partial_secure)

        menuToggle.setOnClickListener {
            val current = viewModel.workoutSettings.value
            viewModel.updateSettings(current.copy(showCompleted = !current.showCompleted))
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_action_primary).apply {
            visibility = View.VISIBLE
            findViewById<TextView>(R.id.tv_action_primary).text = "HISTORY"
            findViewById<ImageView>(R.id.iv_action_primary).setImageResource(R.drawable.ic_history)
            setOnClickListener {
                onShowHistory()
                popupWindow.dismiss()
            }
        }

        menuView.findViewById<View>(R.id.menu_activity_settings).setOnClickListener {
            activity.startActivity(Intent(activity, WorkoutSettingsActivity::class.java))
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, -150, 0)
    }

    fun setupDynamicFilterChips(
        activity: Activity,
        filterGroup: RadioGroup,
        settings: WorkoutSettings
    ) {
        // Keep ALL chip, remove others
        val allChip = filterGroup.findViewById<View>(R.id.chip_all)
        filterGroup.removeAllViews()
        if (allChip != null) {
            filterGroup.addView(allChip)
        }

        if (settings.filterType == "TIME") {
            addFilterChip(activity, filterGroup, "MORNING")
            addFilterChip(activity, filterGroup, "AFTERNOON")
            addFilterChip(activity, filterGroup, "EVENING")
        } else {
            settings.muscleGroups.forEach { muscle ->
                addFilterChip(activity, filterGroup, muscle.uppercase())
            }
        }
    }

    private fun addFilterChip(activity: Activity, group: RadioGroup, label: String) {
        val rb = RadioButton(activity)
        val density = activity.resources.displayMetrics.density
        val params = RadioGroup.LayoutParams((80 * density).toInt(), (38 * density).toInt())
        params.setMargins((2 * density).toInt(), (2 * density).toInt(), (2 * density).toInt(), (2 * density).toInt())
        rb.layoutParams = params
        rb.background = ContextCompat.getDrawable(activity, R.drawable.filter_chip_bg)
        rb.buttonDrawable = null
        rb.text = label
        rb.gravity = android.view.Gravity.CENTER
        rb.setTextColor(Color.WHITE)
        rb.textSize = 10f
        rb.typeface = android.graphics.Typeface.DEFAULT_BOLD
        rb.id = View.generateViewId()
        group.addView(rb)
    }
}
