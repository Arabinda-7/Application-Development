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
import androidx.lifecycle.lifecycleScope
import com.example.allinone.core.utils.ProjectUiHelper
import com.example.allinone.core.utils.ProjectUiHelper.dpToPx
import com.example.allinone.data.model.JournalEntry
import com.example.allinone.data.model.Note
import com.example.allinone.data.model.ProjectFeature
import com.example.allinone.domain.repository.ProjectRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.LinkedList
import javax.inject.Inject

@AndroidEntryPoint
class EditProjectActivity : BaseActivity() {

    @Inject
    lateinit var repository: ProjectRepository

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
    private var currentSearchQuery = ""
    private var isActiveSubfeaturesExpanded = true
    private var isCompletedSubfeaturesExpanded = false
    private val expandedFeatureIds = LinkedList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_project)

        initViews()
        setupLogic()
        setupKeyboardHandling(findViewById(R.id.add_project_root), findViewById(R.id.add_project_content_container))

        projectId = intent.getLongExtra("PROJECT_ID", -1)
        if (projectId != -1L) {
            lifecycleScope.launch {
                val projects = repository.getAllProjects().first()
                project = projects.find { it.timestamp == projectId }
                
                if (project == null) {
                    finish()
                    return@launch
                }

                // Initialize shared editing state
                DataManager.currentEditingSubFeatures.clear()
                DataManager.currentEditingSubFeatures.addAll(project?.subFeatures ?: mutableListOf())

                populateUI()
            }
        } else {
            finish()
        }
    }

    private fun populateUI() {
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
    }

    override fun onResume() {
        super.onResume()
        if (project != null) {
            refreshSubFeatures()
            refreshGoalsUI()
        }
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

        findViewById<EditText>(R.id.et_search_subfeatures).addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString() ?: ""
                refreshSubFeatures()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        findViewById<View>(R.id.btn_add_subfeature).setOnClickListener {
            val nameInput = etNewSubfeature.text.toString().trim()
            val finalName = if (nameInput.isEmpty()) {
                "Subfeature ${DataManager.currentEditingSubFeatures.size + 1}"
            } else {
                DataManager.getUniqueFeatureName(nameInput, DataManager.currentEditingSubFeatures)
            }
            
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
        containerSubfeatures.addView(ProjectUiHelper.createSubfeatureFilterBar(this, currentSubfeatureFilter) { cat ->
            currentSubfeatureFilter = cat
            refreshSubFeatures()
        })

        if (!isSubfeaturesExpanded) return

        val allSubs = DataManager.currentEditingSubFeatures
        val filteredSubs = allSubs.filter { sub ->
            val matchesCategory = if (currentSubfeatureFilter == "ALL") true 
                                 else sub.tag.uppercase() == currentSubfeatureFilter || (currentSubfeatureFilter == "OTHER" && sub.tag.isEmpty())
            val matchesSearch = if (currentSearchQuery.isEmpty()) true 
                               else sub.name.contains(currentSearchQuery, true) || sub.details.contains(currentSearchQuery, true)
            matchesCategory && matchesSearch
        }

        val activeSubs = filteredSubs.filter { !it.isCompleted }
        val completedSubs = filteredSubs.filter { it.isCompleted }

        if (activeSubs.isNotEmpty()) {
            ProjectUiHelper.addSectionHeader(this, containerSubfeatures, "Active (${activeSubs.size})", isActiveSubfeaturesExpanded) {
                isActiveSubfeaturesExpanded = !isActiveSubfeaturesExpanded
                refreshSubFeatures()
            }
            if (isActiveSubfeaturesExpanded) {
                activeSubs.forEach { sub -> addSubfeatureRow(sub) }
            }
        }

        if (completedSubs.isNotEmpty()) {
            ProjectUiHelper.addSectionHeader(this, containerSubfeatures, "Completed (${completedSubs.size})", isCompletedSubfeaturesExpanded) {
                isCompletedSubfeaturesExpanded = !isCompletedSubfeaturesExpanded
                refreshSubFeatures()
            }
            if (isCompletedSubfeaturesExpanded) {
                completedSubs.forEach { sub -> addSubfeatureRow(sub) }
            }
        }
    }


    private fun addSubfeatureRow(sub: ProjectFeature) {
        val row = ProjectUiHelper.createSubFeatureItem(
            this,
            sub,
            onEdit = { s ->
                startActivity(Intent(this@EditProjectActivity, AddSubFeatureActivity::class.java).apply {
                    putExtra("PROJECT_ID", projectId)
                    putExtra("SUB_FEATURE_ID", s.id)
                })
            },
            onLongClick = { view, s -> showSubFeatureMenu(view, s) },
            onToggleExpansion = { s ->
                ProjectUiHelper.handleSubfeatureExpansion(s, expandedFeatureIds, DataManager.currentEditingSubFeatures)
                refreshSubFeatures()
            }
        )
        containerSubfeatures.addView(row)
    }

    private fun showSubFeatureMenu(anchor: View, sub: ProjectFeature) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.menu_project_feature, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 20f

        menuView.findViewById<View>(R.id.menu_take_day_off).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_edit).visibility = View.GONE

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            showStyledConfirmationDialog(
                title = "Delete Milestone",
                message = "Are you sure you want to remove '${sub.name}'?",
                actionText = "DELETE",
                actionColor = Color.parseColor("#FF5252")
            ) {
                DataManager.currentEditingSubFeatures.remove(sub)
                project?.subFeatures?.remove(sub) // Immediate sync for persistence
                DataManager.currentEditingSubFeatures.forEachIndexed { index, feature -> feature.position = index + 1 }
                project?.subFeatures?.forEachIndexed { index, feature -> feature.position = index + 1 }
                refreshSubFeatures()
            }
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_hide_unhide).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_undo).visibility = View.GONE

        popupWindow.showAsDropDown(anchor, 100, 0)
    }

    private fun refreshGoalsUI() {
        project?.let { p ->
            ProjectUiHelper.refreshGoalsUI(this, goalsList, p.ideaGoals) { refreshGoalsUI() }
        }
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
            p.isGlobalProject = true
            
            lifecycleScope.launch {
                repository.updateProject(p)
                DataManager.saveData(this@EditProjectActivity)
                finish()
            }
        }
    }

    private fun showDeleteConfirmation() {
        showStyledConfirmationDialog(
            title = "Delete Project",
            message = "Are you sure you want to delete this project? This action cannot be undone.",
            actionText = "DELETE",
            actionColor = Color.parseColor("#FF5252")
        ) {
            project?.let { p ->
                lifecycleScope.launch {
                    repository.deleteProject(p)
                    DataManager.saveData(this@EditProjectActivity)
                    finish()
                }
            }
        }
    }

}
