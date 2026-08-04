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
import androidx.lifecycle.lifecycleScope
import com.example.allinone.core.utils.ProjectUiHelper
import com.example.allinone.core.utils.ProjectUiHelper.dpToPx
import com.example.allinone.data.model.JournalEntry
import com.example.allinone.data.model.Note
import com.example.allinone.data.model.ProjectFeature
import com.example.allinone.domain.repository.ProjectRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject
import java.util.*
import java.util.LinkedList

@AndroidEntryPoint
class AddProjectActivity : BaseActivity() {

    @Inject
    lateinit var repository: ProjectRepository

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
    private lateinit var tvDeadlineDisplay: TextView
    private lateinit var btnSetDeadline: TextView

    private var selectedColor = -1
    private var selectedPriority = 1 // MED
    private var selectedStatus = "TODO"
    private var selectedTemplateName: String? = null
    private var selectedDeadline: Long? = null
    private var isDescriptionExpanded = true
    private var isGoalsExpanded = true
    private var isSubfeaturesExpanded = true
    private var currentSubfeatureFilter = "ALL"
    private var currentSearchQuery = ""
    private var isActiveSubfeaturesExpanded = true
    private var isCompletedSubfeaturesExpanded = false
    private val tempGoals = mutableListOf<JournalEntry>()
    private val expandedFeatureIds = LinkedList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)

        DataManager.currentEditingSubFeatures.clear()
        // Add default sub-feature
        DataManager.currentEditingSubFeatures.add(ProjectFeature("Initial Milestone", position = 1))

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
        tvDeadlineDisplay = findViewById(R.id.tv_deadline_display)
        btnSetDeadline = findViewById(R.id.btn_set_deadline)

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

        findViewById<EditText>(R.id.et_search_subfeatures).addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString() ?: ""
                refreshSubFeatures()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

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

        findViewById<View>(R.id.cell_deadline).setOnClickListener { showDeadlinePicker() }
        btnSetDeadline.setOnClickListener { showDeadlinePicker() }

        // Templates
        refreshTemplatesUI()

        findViewById<View>(R.id.btn_add_subfeature).setOnClickListener {
            val nameInput = etNewSubfeature.text.toString().trim()
            val finalName = if (nameInput.isEmpty()) {
                "Subfeature ${DataManager.currentEditingSubFeatures.size + 1}"
            } else {
                DataManager.getUniqueFeatureName(nameInput, DataManager.currentEditingSubFeatures)
            }

            val newFeature = ProjectFeature(finalName, position = DataManager.currentEditingSubFeatures.size + 1)
            DataManager.currentEditingSubFeatures.add(newFeature)
            etNewSubfeature.text.clear()
            refreshSubFeatures()
            startActivity(Intent(this, AddSubFeatureActivity::class.java).apply {
                putExtra("SUB_FEATURE_ID", newFeature.id)
            })
        }
    }

    private fun updateThemeVisuals() {
        colorPreview.backgroundTintList = ColorStateList.valueOf(selectedColor)
        btnSave.setTextColor(selectedColor)
        btnSetDeadline.setTextColor(selectedColor)
        findViewById<ImageView>(R.id.cell_deadline).findViewById<ImageView>(android.R.id.icon)?.imageTintList = ColorStateList.valueOf(selectedColor) // Not correct ID, let's just find the first ImageView in the parent
        
        (findViewById<View>(R.id.cell_deadline) as? LinearLayout)?.let { layout ->
            for (i in 0 until layout.childCount) {
                val child = layout.getChildAt(i)
                if (child is ImageView) child.imageTintList = ColorStateList.valueOf(selectedColor)
            }
        }
        
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
                startActivity(Intent(this@AddProjectActivity, AddSubFeatureActivity::class.java).apply {
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

        // Only show Delete option in Add mode for simplicity
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
                DataManager.currentEditingSubFeatures.forEachIndexed { index, feature -> feature.position = index + 1 }
                refreshSubFeatures()
            }
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_hide_unhide).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_undo).visibility = View.GONE

        popupWindow.showAsDropDown(anchor, 100, 0)
    }

    private fun refreshGoalsUI() {
        ProjectUiHelper.refreshGoalsUI(this, goalsList, tempGoals) { refreshGoalsUI() }
    }

    private fun refreshTemplatesUI() {
        containerTemplates.removeAllViews()
        DataManager.projectTemplates.forEach { (name, steps) ->
            val isSelected = selectedTemplateName == name
            val templateBtn = TextView(this).apply {
                text = name
                setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#80FFFFFF"))
                textSize = 12f
                setTypeface(null, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
                setPadding(24.dpToPx(this@AddProjectActivity), 12.dpToPx(this@AddProjectActivity), 24.dpToPx(this@AddProjectActivity), 12.dpToPx(this@AddProjectActivity))
                
                background = ContextCompat.getDrawable(this@AddProjectActivity, R.drawable.priority_chip_bg)
                backgroundTintList = ColorStateList.valueOf(if (isSelected) selectedColor else Color.parseColor("#11FFFFFF"))
                
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = 12.dpToPx(this@AddProjectActivity) }
                setOnClickListener {
                    selectedTemplateName = name
                    DataManager.currentEditingSubFeatures.clear()
                    steps.forEachIndexed { i, step -> DataManager.currentEditingSubFeatures.add(ProjectFeature(step, position = i + 1)) }
                    refreshSubFeatures()
                    refreshTemplatesUI()
                }
            }
            containerTemplates.addView(templateBtn)
        }
    }

    private fun showDeadlinePicker() {
        val cal = Calendar.getInstance()
        selectedDeadline?.let { cal.timeInMillis = it }
        android.app.DatePickerDialog(this, { _, y, m, d ->
            val newCal = Calendar.getInstance()
            newCal.set(y, m, d)
            selectedDeadline = newCal.timeInMillis
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            tvDeadlineDisplay.text = sdf.format(newCal.time)
            tvDeadlineDisplay.setTextColor(Color.WHITE)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveProject() {
        val title = titleInput.text.toString().trim()
        if (title.isNotEmpty()) {
            if (selectedStatus == "DONE") {
                val unfinishedMilestones = DataManager.currentEditingSubFeatures.count { !it.isCompleted }
                if (unfinishedMilestones > 0) {
                    Toast.makeText(this, "Cannot complete project. $unfinishedMilestones Milestones are still pending.", Toast.LENGTH_LONG).show()
                    return
                }
            }

            val note = Note(title = title, content = contentInput.text.toString(), category = "Project")
            note.status = selectedStatus
            note.priority = selectedPriority
            note.color = selectedColor
            note.deadline = selectedDeadline
            note.isGlobalProject = true
            note.subFeatures.addAll(DataManager.currentEditingSubFeatures)
            note.ideaGoals.addAll(tempGoals)
            
            lifecycleScope.launch {
                repository.insertProject(note)
                DataManager.saveData(this@AddProjectActivity)
                finish()
            }
        } else {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
        }
    }

}
