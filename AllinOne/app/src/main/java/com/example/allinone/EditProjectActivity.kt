package com.example.allinone

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class EditProjectActivity : BaseActivity() {

    private var projectIndex: Int = -1
    private var project: Note? = null
    
    private lateinit var titleInput: EditText
    private lateinit var contentInput: EditText
    private lateinit var rgStatus: RadioGroup
    private lateinit var rgPriority: RadioGroup
    private lateinit var seekProgress: SeekBar
    private lateinit var tvProgressValue: TextView
    private lateinit var btnPin: ImageView
    private lateinit var btnDelete: View
    private lateinit var colorPreview: View
    private lateinit var btnSave: TextView
    private lateinit var tvDeadlineDisplay: TextView
    private lateinit var cellTheme: View
    private lateinit var cellDeadline: View
    private lateinit var tvGridStatus: TextView
    private lateinit var tvGridPriority: TextView
    private lateinit var cellStatus: View
    private lateinit var cellPriority: View
    private lateinit var containerSubfeatures: LinearLayout
    private lateinit var etNewSubfeature: EditText
    private lateinit var goalsList: LinearLayout
    private lateinit var etGoalInput: EditText
    private lateinit var btnAddGoal: View
    private lateinit var containerGoalsHeader: View
    private lateinit var ivGoalsChevron: ImageView
    private lateinit var tvFooterDates: TextView
    private lateinit var containerDescriptionHeader: View
    private lateinit var ivDescriptionChevron: ImageView
    private lateinit var layoutSubfeaturesHeaderToggle: View
    private lateinit var ivSubfeaturesMainChevron: ImageView
    private lateinit var layoutSubfeaturesFullContainer: View
    private lateinit var containerGoals: View
    private lateinit var auraView: View

    private var isPinned = false
    private var selectedColor = -1
    private var selectedDeadline: Long? = null
    private var isDescriptionExpanded = false
    private var isGoalsExpanded = false
    private var isSubfeaturesExpanded = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_project)

        projectIndex = intent.getIntExtra("PROJECT_INDEX", -1)
        if (projectIndex != -1 && projectIndex < DataManager.projects.size) {
            project = DataManager.projects[projectIndex]
        }

        if (project == null) {
            finish()
            return
        }

        // Initialize shared editing state
        DataManager.currentEditingSubFeatures.clear()
        DataManager.currentEditingSubFeatures.addAll(project?.subFeatures ?: mutableListOf())

        initViews()
        setupLogic()
        setupKeyboardHandling(findViewById(R.id.add_project_root), findViewById(R.id.add_project_content_container))
    }

    override fun onResume() {
        super.onResume()
        refreshSubFeatures()
        refreshGoalsUI()
    }

    private fun initViews() {
        titleInput = findViewById(R.id.note_title_input)
        contentInput = findViewById(R.id.note_content_input)
        rgStatus = findViewById(R.id.rg_status)
        rgPriority = findViewById(R.id.rg_priority)
        seekProgress = findViewById(R.id.seek_progress)
        tvProgressValue = findViewById(R.id.tv_progress_value)
        btnPin = findViewById(R.id.btn_pin)
        btnDelete = findViewById(R.id.btn_delete_note)
        colorPreview = findViewById(R.id.note_color_preview)
        btnSave = findViewById(R.id.btn_save_note)
        tvDeadlineDisplay = findViewById(R.id.tv_deadline_display)
        cellTheme = findViewById(R.id.cell_theme)
        cellDeadline = findViewById(R.id.cell_deadline)
        tvGridStatus = findViewById(R.id.tv_grid_status)
        tvGridPriority = findViewById(R.id.tv_grid_priority)
        cellStatus = findViewById(R.id.cell_status)
        cellPriority = findViewById(R.id.cell_priority)
        containerSubfeatures = findViewById(R.id.container_subfeatures)
        etNewSubfeature = findViewById(R.id.et_new_subfeature)
        goalsList = findViewById(R.id.goals_list)
        etGoalInput = findViewById(R.id.et_goal_input)
        btnAddGoal = findViewById(R.id.btn_add_goal)
        containerGoalsHeader = findViewById(R.id.container_goals_header)
        ivGoalsChevron = findViewById(R.id.iv_goals_chevron)
        tvFooterDates = findViewById(R.id.tv_footer_dates)
        containerDescriptionHeader = findViewById(R.id.container_description_header)
        ivDescriptionChevron = findViewById(R.id.iv_description_chevron)
        layoutSubfeaturesHeaderToggle = findViewById(R.id.layout_subfeatures_header_toggle)
        ivSubfeaturesMainChevron = findViewById(R.id.iv_subfeatures_main_chevron)
        layoutSubfeaturesFullContainer = findViewById(R.id.layout_subfeatures_full_container)
        containerGoals = findViewById(R.id.container_goals)
        auraView = findViewById(R.id.aura_background)

        findViewById<View>(R.id.btn_close_note).setOnClickListener { finish() }
    }

    private fun setupLogic() {
        project?.let { p ->
            titleInput.setText(p.title)
            contentInput.setText(p.content)
            isPinned = p.isPinned
            selectedColor = if (p.color != -1) p.color else ContextCompat.getColor(this, R.color.card_blue)
            selectedDeadline = p.deadline
            seekProgress.progress = p.progress
            tvProgressValue.text = "${p.progress}%"
            
            updateStatusUI(p.status)
            updatePriorityUI(p.priority)
            updateDeadlineUI()
            updateThemeVisuals()
            refreshSubFeatures()
            refreshGoalsUI()
        }

        btnSave.setOnClickListener { saveProject() }
        btnDelete.setOnClickListener { showDeleteConfirmation() }
        btnPin.setOnClickListener { 
            isPinned = !isPinned
            btnPin.setImageResource(if (isPinned) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
        }

        cellStatus.setOnClickListener { showStatusMenu() }
        cellPriority.setOnClickListener { showPriorityMenu() }
        cellTheme.setOnClickListener {
            val colors = listOf(ContextCompat.getColor(this, R.color.card_blue), ContextCompat.getColor(this, R.color.card_orange), ContextCompat.getColor(this, R.color.card_green), Color.MAGENTA, Color.RED, Color.CYAN)
            selectedColor = colors[(colors.indexOf(selectedColor) + 1) % colors.size]
            updateThemeVisuals()
        }
        cellDeadline.setOnClickListener {
            val cal = Calendar.getInstance(); selectedDeadline?.let { cal.timeInMillis = it }
            DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d); selectedDeadline = cal.timeInMillis; updateDeadlineUI()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnAddGoal.setOnClickListener {
            val text = etGoalInput.text.toString().trim()
            if (text.isNotEmpty()) {
                project?.ideaGoals?.add(JournalEntry(text))
                etGoalInput.text.clear()
                refreshGoalsUI()
            }
        }

        containerDescriptionHeader.setOnClickListener {
            isDescriptionExpanded = !isDescriptionExpanded
            contentInput.visibility = if (isDescriptionExpanded) View.VISIBLE else View.GONE
            ivDescriptionChevron.setImageResource(if (isDescriptionExpanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float)
        }

        containerGoalsHeader.setOnClickListener {
            isGoalsExpanded = !isGoalsExpanded
            containerGoals.visibility = if (isGoalsExpanded) View.VISIBLE else View.GONE
            ivGoalsChevron.setImageResource(if (isGoalsExpanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float)
        }

        layoutSubfeaturesHeaderToggle.setOnClickListener {
            isSubfeaturesExpanded = !isSubfeaturesExpanded
            layoutSubfeaturesFullContainer.visibility = if (isSubfeaturesExpanded) View.VISIBLE else View.GONE
            ivSubfeaturesMainChevron.setImageResource(if (isSubfeaturesExpanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float)
        }

        findViewById<View>(R.id.btn_add_subfeature).setOnClickListener {
            val nameInput = etNewSubfeature.text.toString().trim()
            if (nameInput.isNotEmpty()) {
                val finalName = DataManager.getUniqueFeatureName(nameInput, DataManager.currentEditingSubFeatures)
                val newFeature = ProjectFeature(finalName, position = DataManager.currentEditingSubFeatures.size + 1)
                DataManager.currentEditingSubFeatures.add(newFeature)
                etNewSubfeature.text.clear()
                refreshSubFeatures()
                startActivity(Intent(this, AddSubFeatureActivity::class.java).apply {
                    putExtra("PROJECT_INDEX", projectIndex)
                    putExtra("SUB_FEATURE_ID", newFeature.id)
                })
            }
        }
    }

    private fun showStatusMenu() {
        val popup = PopupMenu(this, cellStatus)
        popup.menu.add("TODO")
        popup.menu.add("DOING")
        popup.menu.add("DONE")
        popup.menu.add("HOLD")
        popup.setOnMenuItemClickListener { item ->
            updateStatusUI(item.title.toString())
            true
        }
        popup.show()
    }

    private fun updateStatusUI(status: String) {
        tvGridStatus.text = status
        when(status) {
            "TODO" -> rgStatus.check(R.id.rb_status_todo)
            "DOING" -> rgStatus.check(R.id.rb_status_progress)
            "DONE" -> rgStatus.check(R.id.rb_status_completed)
            "HOLD" -> rgStatus.check(R.id.rb_status_hold)
        }
    }

    private fun showPriorityMenu() {
        val popup = PopupMenu(this, cellPriority)
        popup.menu.add("LOW")
        popup.menu.add("MED")
        popup.menu.add("HIGH")
        popup.setOnMenuItemClickListener { item ->
            val priority = when(item.title) {
                "LOW" -> 0
                "HIGH" -> 2
                else -> 1
            }
            updatePriorityUI(priority)
            true
        }
        popup.show()
    }

    private fun updatePriorityUI(priority: Int) {
        tvGridPriority.text = when(priority) {
            0 -> "LOW"; 2 -> "HIGH"; else -> "MED"
        }
        when(priority) {
            0 -> rgPriority.check(R.id.rb_priority_low)
            2 -> rgPriority.check(R.id.rb_priority_high)
            else -> rgPriority.check(R.id.rb_priority_med)
        }
    }

    private fun updateDeadlineUI() {
        tvDeadlineDisplay.text = selectedDeadline?.let { 
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it)) 
        } ?: "No Set"
    }

    private fun updateThemeVisuals() {
        colorPreview.backgroundTintList = ColorStateList.valueOf(selectedColor)
        
        val gradient = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(selectedColor, Color.BLACK)
        )
        auraView.background = gradient
    }

    private fun refreshSubFeatures() {
        containerSubfeatures.removeAllViews()
        DataManager.currentEditingSubFeatures.forEach { sub ->
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 2.dpToPx(), 0, 2.dpToPx())
                gravity = android.view.Gravity.CENTER_VERTICAL
                isClickable = true
                isFocusable = true
                background = ContextCompat.getDrawable(this@EditProjectActivity, R.drawable.glass_card_bg)
                backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            }
            val tv = TextView(this).apply {
                text = "${sub.position}. ${sub.name}"
                setTextColor(Color.WHITE)
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val btnEdit = ImageView(this).apply {
                setImageResource(R.drawable.icons8_edit_pencil_100)
                val iconSize = 20.dpToPx()
                layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
                setPadding(2.dpToPx(), 2.dpToPx(), 2.dpToPx(), 2.dpToPx())
                imageTintList = ColorStateList.valueOf(Color.GRAY)
                setOnClickListener {
                    startActivity(Intent(this@EditProjectActivity, AddSubFeatureActivity::class.java).apply {
                        putExtra("PROJECT_INDEX", projectIndex)
                        putExtra("SUB_FEATURE_ID", sub.id)
                    })
                }
            }
            layout.addView(tv)
            layout.addView(btnEdit)
            
            layout.setOnLongClickListener {
                showSubFeatureMenu(it, sub)
                true
            }

            containerSubfeatures.addView(layout)
        }
    }

    private fun showSubFeatureMenu(anchor: View, sub: ProjectFeature) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.menu_project_feature, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 20f

        menuView.findViewById<View>(R.id.menu_take_day_off).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_edit).visibility = View.GONE

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Delete Milestone")
                .setMessage("Are you sure you want to remove '${sub.name}'?")
                .setPositiveButton("DELETE") { _, _ ->
                    DataManager.currentEditingSubFeatures.remove(sub)
                    DataManager.currentEditingSubFeatures.forEachIndexed { index, feature -> feature.position = index + 1 }
                    refreshSubFeatures()
                }
                .setNegativeButton("CANCEL", null)
                .show()
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_hide_unhide).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_undo).visibility = View.GONE

        popupWindow.showAsDropDown(anchor, 100, 0)
    }

    private fun refreshGoalsUI() {
        goalsList.removeAllViews()
        project?.ideaGoals?.forEach { goal ->
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            val tv = TextView(this).apply {
                text = goal.text
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val btnEdit = ImageView(this).apply {
                setImageResource(R.drawable.icons8_edit_pencil_100)
                val iconSize = 20.dpToPx()
                layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
                setPadding(2.dpToPx(), 2.dpToPx(), 2.dpToPx(), 2.dpToPx())
                imageTintList = ColorStateList.valueOf(Color.GRAY)
                setOnClickListener { showEditGoalDialog(goal) }
            }
            val btnDel = ImageView(this).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                val iconSize = 20.dpToPx()
                layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply { marginStart = 8.dpToPx() }
                imageTintList = ColorStateList.valueOf(Color.parseColor("#80FFFFFF"))
                setOnClickListener {
                    android.app.AlertDialog.Builder(this@EditProjectActivity)
                        .setTitle("Delete Goal")
                        .setMessage("Are you sure you want to remove this goal?")
                        .setPositiveButton("DELETE") { _, _ ->
                            project?.ideaGoals?.remove(goal)
                            refreshGoalsUI()
                        }
                        .setNegativeButton("CANCEL", null)
                        .show()
                }
            }
            layout.addView(tv)
            layout.addView(btnEdit)
            layout.addView(btnDel)
            goalsList.addView(layout)
        }
    }

    private fun showEditGoalDialog(goal: JournalEntry) {
        val et = EditText(this).apply {
            setText(goal.text)
            setPadding(24.dpToPx(), 16.dpToPx(), 24.dpToPx(), 16.dpToPx())
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("Edit Goal")
            .setView(et)
            .setPositiveButton("UPDATE") { _, _ ->
                val newText = et.text.toString().trim()
                if (newText.isNotEmpty()) {
                    goal.text = newText
                    refreshGoalsUI()
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun saveProject() {
        project?.let { p ->
            p.title = titleInput.text.toString()
            p.content = contentInput.text.toString()
            p.isPinned = isPinned
            p.color = selectedColor
            p.deadline = selectedDeadline
            p.progress = seekProgress.progress
            p.status = tvGridStatus.text.toString()
            p.priority = when(tvGridPriority.text) {
                "LOW" -> 0; "HIGH" -> 2; else -> 1
            }
            p.subFeatures.clear()
            p.subFeatures.addAll(DataManager.currentEditingSubFeatures)
            p.updatedAt = System.currentTimeMillis()
            
            DataManager.saveData(this)
            finish()
        }
    }

    private fun showDeleteConfirmation() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Delete Project")
            .setMessage("Are you sure?")
            .setPositiveButton("DELETE") { _, _ ->
                DataManager.projects.remove(project)
                DataManager.saveData(this)
                finish()
            }.setNegativeButton("CANCEL", null).show()
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
