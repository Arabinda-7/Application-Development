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

    private var projectId: Long = -1
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
    private var currentSubfeatureFilter = "ALL"
    private var isActiveSubfeaturesExpanded = true
    private var isCompletedSubfeaturesExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_project)

        projectId = intent.getLongExtra("PROJECT_ID", -1)
        if (projectId != -1L) {
            project = synchronized(DataManager.projects) {
                DataManager.projects.find { it.timestamp == projectId }
            }
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
                project?.subFeatures?.add(newFeature) // Immediate sync for persistence
                etNewSubfeature.text.clear()
                refreshSubFeatures()
                startActivity(Intent(this, AddSubFeatureActivity::class.java).apply {
                    putExtra("PROJECT_ID", projectId)
                    putExtra("SUB_FEATURE_ID", newFeature.id)
                })
            }
        }
    }

    private fun showStatusMenu() {
        val options = listOf("TODO", "DOING", "DONE", "HOLD")
        val icons = listOf(
            android.R.drawable.ic_menu_edit,
            R.drawable.icons8_clock_100,
            R.drawable.icons8_done_100,
            android.R.drawable.ic_menu_recent_history
        )
        val colors = listOf(
            Color.WHITE,
            Color.parseColor("#FFB800"),
            Color.parseColor("#2EC4B6"),
            Color.parseColor("#FF5252")
        )

        showCustomSelector("SELECT STATUS", options, icons, colors) { selected ->
            updateStatusUI(selected)
        }
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
        val options = listOf("LOW", "MED", "HIGH")
        val icons = listOf(
            R.drawable.priority_chip_bg,
            R.drawable.priority_chip_bg,
            R.drawable.priority_chip_bg
        )
        val colors = listOf(
            Color.parseColor("#2EC4B6"),
            Color.parseColor("#FFB800"),
            Color.RED
        )

        showCustomSelector("SELECT PRIORITY", options, icons, colors) { selected ->
            val priority = when(selected) {
                "LOW" -> 0
                "HIGH" -> 2
                else -> 1
            }
            updatePriorityUI(priority)
        }
    }

    private fun showCustomSelector(title: String, options: List<String>, icons: List<Int>, colors: List<Int>, onSelected: (String) -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_selector, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<TextView>(R.id.dialog_title).text = title
        val container = dialogView.findViewById<LinearLayout>(R.id.options_container)

        options.forEachIndexed { index, option ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_selector_option, container, false)
            val tv = itemView.findViewById<TextView>(R.id.option_text)
            val iv = itemView.findViewById<ImageView>(R.id.option_icon)

            tv.text = option
            iv.setImageResource(icons[index])
            iv.imageTintList = ColorStateList.valueOf(colors[index])
            
            itemView.setOnClickListener {
                onSelected(option)
                dialog.dismiss()
            }
            container.addView(itemView)
        }

        dialog.show()
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
        
        // 1. Filter Bar
        val filterBar = HorizontalScrollView(this).apply {
            scrollBarSize = 0
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 8.dpToPx())
            }
        }
        val filterContainer = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val categories = listOf("ALL", "TASKS", "FEATURES", "BUGS", "RESOURCES", "OTHER")
        
        categories.forEach { cat ->
            val chip = TextView(this).apply {
                text = cat
                textSize = 10f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(12.dpToPx(), 6.dpToPx(), 12.dpToPx(), 6.dpToPx())
                val isSelected = currentSubfeatureFilter == cat
                setTextColor(if (isSelected) Color.WHITE else Color.GRAY)
                background = ContextCompat.getDrawable(this@EditProjectActivity, R.drawable.priority_chip_bg)
                backgroundTintList = ColorStateList.valueOf(if (isSelected) Color.parseColor("#1A73E8") else Color.parseColor("#11FFFFFF"))
                
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    marginEnd = 8.dpToPx()
                }
                
                setOnClickListener {
                    currentSubfeatureFilter = cat
                    refreshSubFeatures()
                }
            }
            filterContainer.addView(chip)
        }
        filterBar.addView(filterContainer)
        containerSubfeatures.addView(filterBar)

        if (!isSubfeaturesExpanded) return

        val allSubs = DataManager.currentEditingSubFeatures
        val filteredSubs = if (currentSubfeatureFilter == "ALL") allSubs 
                          else allSubs.filter { it.tag.uppercase() == currentSubfeatureFilter || (currentSubfeatureFilter == "OTHER" && it.tag.isEmpty()) }

        val activeSubs = filteredSubs.filter { !it.isCompleted }
        val completedSubs = filteredSubs.filter { it.isCompleted }

        // 2. Active Section
        if (activeSubs.isNotEmpty()) {
            addSectionHeader("Active (${activeSubs.size})", isActiveSubfeaturesExpanded) {
                isActiveSubfeaturesExpanded = !isActiveSubfeaturesExpanded
                refreshSubFeatures()
            }
            if (isActiveSubfeaturesExpanded) {
                activeSubs.forEach { sub -> addSubfeatureRow(sub) }
            }
        }

        // 3. Completed Section
        if (completedSubs.isNotEmpty()) {
            addSectionHeader("Completed (${completedSubs.size})", isCompletedSubfeaturesExpanded) {
                isCompletedSubfeaturesExpanded = !isCompletedSubfeaturesExpanded
                refreshSubFeatures()
            }
            if (isCompletedSubfeaturesExpanded) {
                completedSubs.forEach { sub -> addSubfeatureRow(sub) }
            }
        }
    }

    private fun addSectionHeader(title: String, isExpanded: Boolean, onClick: () -> Unit) {
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(4.dpToPx(), 8.dpToPx(), 4.dpToPx(), 4.dpToPx())
            setOnClickListener { onClick() }
        }
        val tv = TextView(this).apply {
            text = title.uppercase()
            setTextColor(Color.GRAY)
            textSize = 10f
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val iv = ImageView(this).apply {
            setImageResource(if (isExpanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float)
            imageTintList = ColorStateList.valueOf(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(16.dpToPx(), 16.dpToPx())
        }
        header.addView(tv)
        header.addView(iv)
        containerSubfeatures.addView(header)
    }

    private fun addSubfeatureRow(sub: ProjectFeature) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 4.dpToPx(), 0, 4.dpToPx())
            isClickable = true
            isFocusable = true
            background = ContextCompat.getDrawable(this@EditProjectActivity, R.drawable.glass_card_bg)
            backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val tv = TextView(this).apply {
            text = "${sub.position}. ${sub.name}"
            setTextColor(Color.WHITE)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            if (sub.isCompleted) {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                alpha = 0.6f
            }
        }
        
        val containerMeta = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        if (sub.tag.isNotEmpty()) {
            val tvTag = TextView(this).apply {
                text = sub.tag.uppercase()
                setTextColor(Color.parseColor("#1A73E8"))
                textSize = 10f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(4.dpToPx(), 2.dpToPx(), 4.dpToPx(), 2.dpToPx())
                background = ContextCompat.getDrawable(this@EditProjectActivity, R.drawable.priority_chip_bg)
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1A73E8")).withAlpha(30)
                val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.marginEnd = 4.dpToPx()
                layoutParams = params
            }
            containerMeta.addView(tvTag)
        }

        val priorityText = when(sub.priority) { 2 -> "HIGH"; 1 -> "MED"; else -> "LOW" }
        val priorityColor = when(sub.priority) { 2 -> Color.RED; 1 -> Color.parseColor("#FFB800"); else -> Color.parseColor("#2EC4B6") }
        val tvPriority = TextView(this).apply {
            text = priorityText
            setTextColor(priorityColor)
            textSize = 10f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(4.dpToPx(), 2.dpToPx(), 4.dpToPx(), 2.dpToPx())
            background = ContextCompat.getDrawable(this@EditProjectActivity, R.drawable.priority_chip_bg)
            backgroundTintList = ColorStateList.valueOf(priorityColor).withAlpha(30)
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.marginEnd = 4.dpToPx()
            layoutParams = params
        }
        containerMeta.addView(tvPriority)

        val tvDate = TextView(this).apply {
            sub.dueDate?.let {
                text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it))
                setTextColor(Color.RED)
                textSize = 12f
                setPadding(4.dpToPx(), 0, 8.dpToPx(), 0)
            } ?: run {
                visibility = View.GONE
            }
        }

        val btnEdit = ImageView(this).apply {
            setImageResource(R.drawable.icons8_edit_pencil_100)
            val iconSize = 20.dpToPx()
            layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
            setPadding(2.dpToPx(), 2.dpToPx(), 2.dpToPx(), 2.dpToPx())
            imageTintList = ColorStateList.valueOf(Color.GRAY)
            setOnClickListener {
                startActivity(Intent(this@EditProjectActivity, AddSubFeatureActivity::class.java).apply {
                    putExtra("PROJECT_ID", projectId)
                    putExtra("SUB_FEATURE_ID", sub.id)
                })
            }
        }

        val tvDetails = TextView(this).apply {
            text = sub.details
            setTextColor(Color.GRAY)
            textSize = 12f
            setPadding(24.dpToPx(), 4.dpToPx(), 8.dpToPx(), 8.dpToPx())
            visibility = if (sub.isExpanded && sub.details.isNotEmpty()) View.VISIBLE else View.GONE
        }

        header.addView(tv)
        header.addView(containerMeta)
        header.addView(tvDate)
        header.addView(btnEdit)
        
        layout.addView(header)
        layout.addView(tvDetails)

        layout.setOnClickListener {
            if (sub.details.isNotEmpty()) {
                sub.isExpanded = !sub.isExpanded
                tvDetails.visibility = if (sub.isExpanded) View.VISIBLE else View.GONE
            }
        }
        
        layout.setOnLongClickListener {
            showSubFeatureMenu(it, sub)
            true
        }

        containerSubfeatures.addView(layout)
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
                    project?.subFeatures?.remove(sub) // Immediate sync for persistence
                    DataManager.currentEditingSubFeatures.forEachIndexed { index, feature -> feature.position = index + 1 }
                    project?.subFeatures?.forEachIndexed { index, feature -> feature.position = index + 1 }
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
