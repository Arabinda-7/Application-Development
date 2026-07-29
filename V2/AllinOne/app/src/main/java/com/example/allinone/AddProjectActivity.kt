package com.example.allinone

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

class AddProjectActivity : BaseActivity() {

    private lateinit var titleInput: EditText
    private lateinit var tvTitleHint: TextView
    private lateinit var contentInput: EditText
    private lateinit var rgStatus: RadioGroup
    private lateinit var rgPriority: RadioGroup
    private lateinit var colorPreview: View
    private lateinit var btnSave: TextView
    private lateinit var containerSubfeatures: LinearLayout
    private lateinit var etNewSubfeature: EditText
    private lateinit var containerTemplates: LinearLayout
    private lateinit var containerDescriptionHeader: View
    private lateinit var ivDescriptionChevron: ImageView
    private lateinit var containerGoalsHeader: View
    private lateinit var ivGoalsChevron: ImageView
    private lateinit var containerGoals: View
    private lateinit var goalsList: LinearLayout
    private lateinit var etGoalInput: EditText
    private lateinit var btnAddGoal: View
    private lateinit var layoutSubfeaturesHeaderToggle: View
    private lateinit var ivSubfeaturesMainChevron: ImageView
    private lateinit var layoutSubfeaturesFullContainer: View
    private lateinit var auraView: View

    private var selectedColor = -1
    private var selectedPriority = 1 // MED
    private var selectedStatus = "TODO"
    private var isDescriptionExpanded = true
    private var isGoalsExpanded = true
    private var isSubfeaturesExpanded = true
    private var currentSubfeatureFilter = "ALL"
    private var isActiveSubfeaturesExpanded = true
    private var isCompletedSubfeaturesExpanded = false
    private val tempGoals = mutableListOf<JournalEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)

        DataManager.currentEditingSubFeatures.clear()

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
        tvTitleHint = findViewById(R.id.tv_title_hint_project)
        contentInput = findViewById(R.id.note_content_input)
        rgStatus = findViewById(R.id.rg_status)
        rgPriority = findViewById(R.id.rg_priority)
        colorPreview = findViewById(R.id.note_color_preview)
        btnSave = findViewById(R.id.btn_save_note)
        containerSubfeatures = findViewById(R.id.container_subfeatures)
        etNewSubfeature = findViewById(R.id.et_new_subfeature)
        containerTemplates = findViewById(R.id.container_templates)
        containerDescriptionHeader = findViewById(R.id.container_description_header)
        ivDescriptionChevron = findViewById(R.id.iv_description_chevron)
        containerGoalsHeader = findViewById(R.id.container_goals_header)
        ivGoalsChevron = findViewById(R.id.iv_goals_chevron)
        containerGoals = findViewById(R.id.container_goals)
        goalsList = findViewById(R.id.goals_list)
        etGoalInput = findViewById(R.id.et_goal_input)
        btnAddGoal = findViewById(R.id.btn_add_goal)
        layoutSubfeaturesHeaderToggle = findViewById(R.id.layout_subfeatures_header_toggle)
        ivSubfeaturesMainChevron = findViewById(R.id.iv_subfeatures_main_chevron)
        layoutSubfeaturesFullContainer = findViewById(R.id.layout_subfeatures_full_container)
        auraView = findViewById(R.id.aura_background)

        findViewById<View>(R.id.btn_close_note).setOnClickListener { finish() }
    }

    private fun setupLogic() {
        selectedColor = ContextCompat.getColor(this, R.color.card_blue)
        updateThemeVisuals()

        btnSave.setOnClickListener { saveProject() }

        colorPreview.setOnClickListener {
            val colors = listOf(ContextCompat.getColor(this, R.color.card_blue), ContextCompat.getColor(this, R.color.card_orange), ContextCompat.getColor(this, R.color.card_green), Color.MAGENTA, Color.RED, Color.CYAN)
            selectedColor = colors[(colors.indexOf(selectedColor) + 1) % colors.size]
            updateThemeVisuals()
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

        btnAddGoal.setOnClickListener {
            val goalText = etGoalInput.text.toString().trim()
            if (goalText.isNotEmpty()) {
                tempGoals.add(JournalEntry(goalText))
                etGoalInput.text.clear()
                refreshGoalsUI()
            }
        }

        layoutSubfeaturesHeaderToggle.setOnClickListener {
            isSubfeaturesExpanded = !isSubfeaturesExpanded
            layoutSubfeaturesFullContainer.visibility = if (isSubfeaturesExpanded) View.VISIBLE else View.GONE
            ivSubfeaturesMainChevron.setImageResource(if (isSubfeaturesExpanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float)
        }

        rgStatus.setOnCheckedChangeListener { _, checkedId ->
            selectedStatus = when (checkedId) {
                R.id.rb_status_progress -> "DOING"
                R.id.rb_status_completed -> "DONE"
                R.id.rb_status_hold -> "HOLD"
                else -> "TODO"
            }
        }

        rgPriority.setOnCheckedChangeListener { _, checkedId ->
            selectedPriority = when (checkedId) {
                R.id.rb_priority_low -> 0
                R.id.rb_priority_high -> 2
                else -> 1
            }
        }

        // Templates
        DataManager.projectTemplates.forEach { (name, steps) ->
            val templateBtn = TextView(this).apply {
                text = name; setTextColor(Color.WHITE); textSize = 12f
                setPadding(24.dpToPx(), 12.dpToPx(), 24.dpToPx(), 12.dpToPx())
                background = ContextCompat.getDrawable(this@AddProjectActivity, R.drawable.priority_chip_bg)
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = 12.dpToPx() }
                setOnClickListener {
                    DataManager.currentEditingSubFeatures.clear()
                    steps.forEachIndexed { i, step -> DataManager.currentEditingSubFeatures.add(ProjectFeature(step, position = i + 1)) }
                    refreshSubFeatures()
                }
            }
            containerTemplates.addView(templateBtn)
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
                    putExtra("SUB_FEATURE_ID", newFeature.id)
                })
            }
        }
    }

    private fun updateThemeVisuals() {
        colorPreview.backgroundTintList = ColorStateList.valueOf(selectedColor)
        btnSave.setTextColor(selectedColor)
        
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
                background = ContextCompat.getDrawable(this@AddProjectActivity, R.drawable.priority_chip_bg)
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
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 4.dpToPx(), 0, 4.dpToPx())
            gravity = android.view.Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            background = ContextCompat.getDrawable(this@AddProjectActivity, R.drawable.glass_card_bg)
            backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
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
                background = ContextCompat.getDrawable(this@AddProjectActivity, R.drawable.priority_chip_bg)
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
            background = ContextCompat.getDrawable(this@AddProjectActivity, R.drawable.priority_chip_bg)
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
                startActivity(Intent(this@AddProjectActivity, AddSubFeatureActivity::class.java).apply {
                    putExtra("SUB_FEATURE_ID", sub.id)
                })
            }
        }
        layout.addView(tv)
        layout.addView(containerMeta)
        layout.addView(tvDate)
        layout.addView(btnEdit)
        
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

        // Only show Delete option in Add mode for simplicity
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
        tempGoals.forEach { goal ->
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
                    android.app.AlertDialog.Builder(this@AddProjectActivity)
                        .setTitle("Delete Goal")
                        .setMessage("Are you sure you want to remove this goal?")
                        .setPositiveButton("DELETE") { _, _ ->
                            tempGoals.remove(goal)
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
        val title = titleInput.text.toString().trim()
        if (title.isNotEmpty()) {
            val note = Note(title = title, content = contentInput.text.toString(), category = "Project")
            note.status = selectedStatus
            note.priority = selectedPriority
            note.color = selectedColor
            note.subFeatures.addAll(DataManager.currentEditingSubFeatures)
            note.ideaGoals.addAll(tempGoals)
            
            DataManager.projects.add(0, note)
            DataManager.saveData(this)
            finish()
        } else {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
