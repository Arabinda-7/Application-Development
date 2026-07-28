package com.example.allinone

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SettingsAppearanceHandler(
    private val context: Context,
    private val onThemeChanged: () -> Unit
) {
    fun showColorPickerDialog(section: String, onColorSelected: () -> Unit) {
        val dialog = Dialog(context); dialog.setContentView(R.layout.dialog_color_picker_appearance)
        dialog.window?.let { it.setBackgroundDrawableResource(android.R.color.transparent); if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) it.attributes.blurBehindRadius = 20; it.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND) }
        val grid = dialog.findViewById<GridLayout>(R.id.color_grid)
        dialog.findViewById<TextView>(R.id.tv_picker_title).text = "SELECT COLOR: $section"
        val colors = listOf("#FF7A59", "#FFB800", "#2EC4B6", "#1A73E8", "#E91E63", "#9C27B0", "#673AB7", "#4CAF50").map { Color.parseColor(it) }
        colors.forEach { color ->
            val v = View(context).apply {
                val s = (48 * context.resources.displayMetrics.density).toInt()
                layoutParams = GridLayout.LayoutParams().apply { width = s; height = s; setMargins(12, 12, 12, 12) }
                background = ContextCompat.getDrawable(context, R.drawable.circle_selected_bg)
                backgroundTintList = ColorStateList.valueOf(color)
                setOnClickListener {
                    when (section) {
                        "HABIT" -> DataManager.globalHabitColor = color
                        "WORKOUT" -> DataManager.globalWorkoutColor = color
                        "TASK" -> DataManager.globalTaskColor = color
                        "PROJECT" -> DataManager.globalProjectColor = color
                        "NOTE" -> DataManager.globalNoteColor = color
                        "FINANCE" -> DataManager.globalFinanceColor = color
                        "ADD_HABIT" -> DataManager.habitAddThemeColor = color
                        "ADD_WORKOUT" -> DataManager.workoutAddThemeColor = color
                        "ADD_TASK" -> DataManager.taskAddThemeColor = color
                        "ADD_PROJECT" -> DataManager.projectAddThemeColor = color
                        "ADD_NOTE" -> DataManager.noteAddThemeColor = color
                        "ADD_FINANCE" -> DataManager.financeAddThemeColor = color
                        "APP_ACCENT" -> DataManager.appAccentColor = color
                    }
                    DataManager.saveData(context); dialog.dismiss(); onColorSelected()
                }
            }
            grid.addView(v)
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { dialog.dismiss() }; dialog.show()
    }

    fun showBorderRadiusSliderDialog() {
        val dialog = Dialog(context); dialog.setContentView(R.layout.dialog_settings_slider_radius)
        dialog.window?.let { it.setBackgroundDrawableResource(android.R.color.transparent); if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) it.attributes.blurBehindRadius = 20; it.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND) }
        val slider = dialog.findViewById<SeekBar>(R.id.settings_slider)
        val valueText = dialog.findViewById<TextView>(R.id.tv_slider_value)
        dialog.findViewById<TextView>(R.id.tv_slider_title).text = "BORDER RADIUS"
        slider.max = 32; slider.progress = DataManager.appBorderRadius; valueText.text = "${slider.progress}dp"
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) { valueText.text = "${p}dp" }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        dialog.findViewById<View>(R.id.btn_save_slider).setOnClickListener { DataManager.appBorderRadius = slider.progress; DataManager.saveData(context); dialog.dismiss(); onThemeChanged() }
        dialog.show()
    }

    fun showIconPickerDialog(section: String, currentPath: String, onIconSelected: () -> Unit) {
        val dialog = Dialog(context); dialog.setContentView(R.layout.dialog_manage_cat_appearance)
        dialog.window?.let { it.setBackgroundDrawableResource(android.R.color.transparent); if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) it.attributes.blurBehindRadius = 20; it.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND) }
        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val icons = listOf(R.drawable.ic_habit_tracker, R.drawable.ic_workout_routine, R.drawable.ic_task, R.drawable.ic_notes, R.drawable.ic_project, R.drawable.ic_finance)
        val rv = RecyclerView(context).apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(p: ViewGroup, t: Int) = object : RecyclerView.ViewHolder(ImageView(p.context).apply {
                    val s = (56 * context.resources.displayMetrics.density).toInt()
                    layoutParams = ViewGroup.LayoutParams(s, s); setPadding(12, 12, 12, 12)
                }) {}
                override fun onBindViewHolder(h: RecyclerView.ViewHolder, p: Int) {
                    val i = icons[p]; val iv = h.itemView as ImageView; iv.setImageResource(i); iv.imageTintList = ColorStateList.valueOf(Color.WHITE)
                    iv.setOnClickListener {
                        when(section) {
                            "HABIT" -> DataManager.globalHabitIcon = i
                            "WORKOUT" -> DataManager.globalWorkoutIcon = i
                            "TASK" -> DataManager.globalTaskIcon = i
                            "PROJECT" -> DataManager.globalProjectIcon = i
                            "NOTE" -> DataManager.globalNoteIcon = i
                            "FINANCE" -> DataManager.globalFinanceIcon = i
                        }
                        DataManager.saveData(context); dialog.dismiss(); onIconSelected()
                    }
                }
                override fun getItemCount() = icons.size
            }
        }
        container.removeAllViews(); container.addView(rv); dialog.show()
    }
}
