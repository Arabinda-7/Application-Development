package com.example.allinone

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.allinone.core.utils.ProjectUiHelper
import com.example.allinone.core.utils.ProjectUiHelper.dpToPx
import com.example.allinone.data.model.*
import com.example.allinone.domain.repository.ProjectRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddIdeaActivity : BaseActivity() {

    @Inject
    lateinit var repository: ProjectRepository

    private var ideaId: Long = -1
    private var existingIdea: Note? = null

    private lateinit var titleInput: EditText
    private lateinit var contentInput: EditText
    private lateinit var btnSave: TextView
    private lateinit var btnDelete: View
    private lateinit var btnClose: View
    private lateinit var btnPriority: TextView
    private lateinit var tvCharCount: TextView
    private lateinit var containerDescription: View
    private lateinit var btnToggleDescription: TextView
    private lateinit var containerGoals: View
    private lateinit var goalsList: LinearLayout
    private lateinit var etGoalInput: EditText
    private lateinit var btnAddGoal: View
    private lateinit var btnToggleGoals: TextView
    private lateinit var btnToggleFeatures: TextView
    private lateinit var layoutFeaturesContainer: View
    private lateinit var containerSubfeatures: LinearLayout
    private lateinit var etNewSubfeature: EditText
    private lateinit var btnAddSubfeature: View
    private lateinit var btnConvert: TextView
    private lateinit var btnConvertIcon: View
    private lateinit var tvCreatedAt: TextView
    private lateinit var headerBgAccent: View

    private var currentPriority = 0
    private val tempGoals = mutableListOf<JournalEntry>()
    private var currentTagFilter = "ALL"
    private var isActiveSubfeaturesExpanded = true
    private var isCompletedSubfeaturesExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_idea)

        initViews()
        setupLogic()
        setupKeyboardHandling(findViewById(R.id.add_idea_root), findViewById(R.id.add_idea_content_container))

        ideaId = intent.getLongExtra("IDEA_ID", -1)
        if (ideaId != -1L) {
            lifecycleScope.launch {
                val projects = repository.getAllProjects().first()
                existingIdea = projects.find { it.timestamp == ideaId }
                
                existingIdea?.let {
                    populateUI(it)
                }
            }
        } else {
            DataManager.currentEditingIdeaSubFeatures.clear()
            // Add default sub-feature for new Idea
            DataManager.currentEditingIdeaSubFeatures.add(ProjectFeature("Initial Idea Step", position = 1))
        }
    }

    private fun populateUI(idea: Note) {
        titleInput.setText(idea.title)
        contentInput.setText(idea.content)
        currentPriority = idea.priority
        tempGoals.clear()
        tempGoals.addAll(idea.ideaGoals)
        
        DataManager.currentEditingIdeaSubFeatures.clear()
        DataManager.currentEditingIdeaSubFeatures.addAll(idea.subFeatures)
        
        btnSave.text = "UPDATE"
        btnDelete.visibility = View.VISIBLE
        btnConvertIcon.visibility = View.VISIBLE
        tvCharCount.text = "${idea.content.length} characters"
        
        tvCreatedAt.visibility = View.VISIBLE
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        tvCreatedAt.text = "Created on: ${sdf.format(Date(idea.timestamp))}"
        
        if (idea.content.isNotEmpty()) {
            containerDescription.visibility = View.GONE
            btnToggleDescription.text = "DESCRIPTION ▼"
        }
        
        if (tempGoals.isNotEmpty()) {
            containerGoals.visibility = View.VISIBLE
            btnToggleGoals.text = "IDEA GOALS ▲"
        }
        
        if (DataManager.currentEditingIdeaSubFeatures.isNotEmpty()) {
            layoutFeaturesContainer.visibility = View.VISIBLE
            btnToggleFeatures.text = "FEATURES ▲"
        }
        
        refreshGoalsUI()
        refreshSubFeatures()
        updatePriorityUI()
        validateInputs()
    }

    override fun onResume() {
        super.onResume()
        if (existingIdea != null || ideaId == -1L) {
            refreshSubFeatures()
        }
    }

    private fun initViews() {
        titleInput = findViewById(R.id.note_title_input)
        contentInput = findViewById(R.id.note_content_input)
        btnSave = findViewById(R.id.btn_save_note)
        btnDelete = findViewById(R.id.btn_delete_note)
        btnClose = findViewById(R.id.btn_close_note)
        btnPriority = findViewById(R.id.btn_priority_tag)
        tvCharCount = findViewById(R.id.tv_char_count)
        containerDescription = findViewById(R.id.container_description)
        btnToggleDescription = findViewById(R.id.btn_toggle_description)
        containerGoals = findViewById(R.id.container_goals)
        goalsList = findViewById(R.id.goals_list)
        etGoalInput = findViewById(R.id.et_goal_input)
        btnAddGoal = findViewById(R.id.btn_add_goal)
        btnToggleGoals = findViewById(R.id.btn_toggle_goals)
        btnToggleFeatures = findViewById(R.id.btn_toggle_subfeatures)
        layoutFeaturesContainer = findViewById(R.id.layout_features_container)
        containerSubfeatures = findViewById(R.id.container_subfeatures)
        etNewSubfeature = findViewById(R.id.et_new_subfeature)
        btnAddSubfeature = findViewById(R.id.btn_add_subfeature)
        btnConvert = findViewById(R.id.btn_convert_project)
        btnConvertIcon = findViewById(R.id.btn_convert_project_icon)
        tvCreatedAt = findViewById(R.id.tv_created_at)
        headerBgAccent = findViewById(R.id.header_bg_accent)
    }

    private fun setupLogic() {
        if (existingIdea != null) {
            titleInput.setText(existingIdea?.title)
            contentInput.setText(existingIdea?.content)
            currentPriority = existingIdea?.priority ?: 0
            tempGoals.addAll(existingIdea?.ideaGoals ?: emptyList())
            btnSave.text = "UPDATE"
            btnDelete.visibility = View.VISIBLE
            btnConvertIcon.visibility = View.VISIBLE
            tvCharCount.text = "${existingIdea?.content?.length ?: 0} characters"

            // Show creation time
            tvCreatedAt.visibility = View.VISIBLE
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            existingIdea?.let { idea ->
                tvCreatedAt.text = "Created on: ${sdf.format(Date(idea.timestamp))}"
            }

            // Auto-collapse if content exists
            if (existingIdea?.content?.isNotEmpty() == true) {
                containerDescription.visibility = View.GONE
                btnToggleDescription.text = "DESCRIPTION ▼"
            }

            // Auto-expand if goals exist
            if (tempGoals.isNotEmpty()) {
                containerGoals.visibility = View.VISIBLE
                btnToggleGoals.text = "IDEA GOALS ▲"
            }

            // Auto-expand if features exist
            if (DataManager.currentEditingIdeaSubFeatures.isNotEmpty()) {
                layoutFeaturesContainer.visibility = View.VISIBLE
                btnToggleFeatures.text = "FEATURES ▲"
            } else {
                layoutFeaturesContainer.visibility = View.GONE
                btnToggleFeatures.text = "FEATURES ▼"
            }
        }

        btnClose.setOnClickListener { finish() }
        btnSave.setOnClickListener { saveIdea() }
        btnDelete.setOnClickListener { showDeleteConfirmation() }
        setupListeners()
    }

    private fun showDeleteConfirmation() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Delete Idea")
            .setMessage("Are you sure you want to delete this idea?")
            .setPositiveButton("DELETE") { _, _ ->
                existingIdea?.let { idea ->
                    lifecycleScope.launch {
                        repository.deleteProject(idea)
                        DataManager.saveData(this@AddIdeaActivity)
                        Toast.makeText(this@AddIdeaActivity, "Idea deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun setupListeners() {
        btnToggleDescription.setOnClickListener {
            val isVisible = containerDescription.visibility == View.VISIBLE
            containerDescription.visibility = if (isVisible) View.GONE else View.VISIBLE
            btnToggleDescription.text = if (isVisible) "DESCRIPTION ▼" else "DESCRIPTION ▲"
        }

        btnToggleGoals.setOnClickListener {
            val isVisible = containerGoals.visibility == View.VISIBLE
            containerGoals.visibility = if (isVisible) View.GONE else View.VISIBLE
            btnToggleGoals.text = if (isVisible) "IDEA GOALS ▼" else "IDEA GOALS ▲"
        }

        btnToggleFeatures.setOnClickListener {
            val isVisible = layoutFeaturesContainer.visibility == View.VISIBLE
            layoutFeaturesContainer.visibility = if (isVisible) View.GONE else View.VISIBLE
            btnToggleFeatures.text = if (isVisible) "FEATURES ▼" else "FEATURES ▲"
        }

        btnAddGoal.setOnClickListener {
            val text = etGoalInput.text.toString().trim()
            if (text.isNotEmpty()) {
                tempGoals.add(JournalEntry(text))
                etGoalInput.text.clear()
                refreshGoalsUI()
            }
        }

        btnAddSubfeature.setOnClickListener {
            val name = etNewSubfeature.text.toString().trim()
            val baseName = name.ifEmpty { "New Feature" }
            val finalName = DataManager.getUniqueFeatureName(baseName, DataManager.currentEditingIdeaSubFeatures)
            
            val nextPos = if (DataManager.currentEditingIdeaSubFeatures.isEmpty()) 1 else DataManager.currentEditingIdeaSubFeatures.maxOf { it.position } + 1
            val newFeature = ProjectFeature(name = finalName, position = nextPos)
            DataManager.currentEditingIdeaSubFeatures.add(newFeature)
            etNewSubfeature.text.clear()
            refreshSubFeatures()
            
            startActivity(Intent(this, AddSubFeatureActivity::class.java).apply {
                putExtra("PROJECT_ID", ideaId)
                putExtra("SUB_FEATURE_ID", newFeature.id)
                putExtra("IS_IDEA", true)
            })
        }

        btnPriority.setOnClickListener {
            currentPriority = (currentPriority + 1) % 3
            updatePriorityUI()
        }

        btnConvert.setOnClickListener { showConvertConfirmationDialog() }
        btnConvertIcon.setOnClickListener { showConvertConfirmationDialog() }

        titleInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateInputs() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        contentInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvCharCount.text = "${s?.length ?: 0} characters"
                if (DataManager.projectAutoSaveIdeas) {
                    existingIdea?.let { idea ->
                        idea.content = s.toString()
                        DataManager.saveDataDebounced(this@AddIdeaActivity)
                    }
                }
                if (count == 1 && s?.get(start) == '\n') {
                    val textBefore = s.subSequence(0, start).toString()
                    val lines = textBefore.split("\n")
                    if (lines.isNotEmpty()) {
                        val lastLine = lines.last()
                        if (lastLine.trim().startsWith("•")) { contentInput.text.insert(start + 1, "• ") }
                        else {
                            val match = Regex("^(\\d+)\\. ").find(lastLine.trim())
                            if (match != null) { val nextNum = match.groupValues[1].toInt() + 1; contentInput.text.insert(start + 1, "$nextNum. ") }
                        }
                    }
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        refreshGoalsUI()
        refreshSubFeatures()
        updatePriorityUI()
        validateInputs()
    }

    private fun refreshGoalsUI() {
        ProjectUiHelper.refreshGoalsUI(this, goalsList, tempGoals) { refreshGoalsUI() }
    }


    private fun refreshSubFeatures() {
        containerSubfeatures.removeAllViews()
        containerSubfeatures.addView(ProjectUiHelper.createSubfeatureFilterBar(this, currentTagFilter) { cat ->
            currentTagFilter = cat
            refreshSubFeatures()
        })

        val allSubs = DataManager.currentEditingIdeaSubFeatures
        val filteredSubs = if (currentTagFilter == "ALL") allSubs
        else allSubs.filter { it.tag.uppercase() == currentTagFilter || (currentTagFilter == "OTHER" && it.tag.isEmpty()) }

        val activeSubs = filteredSubs.filter { !it.isCompleted }.sortedBy { it.position }
        val completedSubs = filteredSubs.filter { it.isCompleted }.sortedByDescending { it.position }

        if (activeSubs.isNotEmpty()) {
            ProjectUiHelper.addSectionHeader(this, containerSubfeatures, "Active (${activeSubs.size})", isActiveSubfeaturesExpanded) {
                isActiveSubfeaturesExpanded = !isActiveSubfeaturesExpanded
                refreshSubFeatures()
            }
            if (isActiveSubfeaturesExpanded) {
                activeSubs.forEach { sub -> containerSubfeatures.addView(createSubFeatureItem(sub)) }
            }
        }

        if (completedSubs.isNotEmpty()) {
            ProjectUiHelper.addSectionHeader(this, containerSubfeatures, "Completed (${completedSubs.size})", isCompletedSubfeaturesExpanded) {
                isCompletedSubfeaturesExpanded = !isCompletedSubfeaturesExpanded
                refreshSubFeatures()
            }
            if (isCompletedSubfeaturesExpanded) {
                completedSubs.forEach { sub -> containerSubfeatures.addView(createSubFeatureItem(sub)) }
            }
        }
    }


    private fun createSubFeatureItem(sub: ProjectFeature): View {
        return ProjectUiHelper.createSubFeatureItem(
            this,
            sub,
            onEdit = { s ->
                val intent = Intent(this@AddIdeaActivity, AddSubFeatureActivity::class.java).apply {
                    putExtra("PROJECT_ID", ideaId)
                    putExtra("SUB_FEATURE_ID", s.id)
                    putExtra("IS_IDEA", true)
                }
                startActivity(intent)
            },
            onLongClick = { view, s -> showSubFeatureMenu(view, s) },
            onToggleExpansion = { s ->
                s.isExpanded = !s.isExpanded
                refreshSubFeatures()
            }
        )
    }

    private fun showSubFeatureMenu(anchor: View, sub: ProjectFeature) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.menu_idea_item, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 20f

        val btnMark = menuView.findViewById<View>(R.id.menu_take_day_off)
        val tvMark = menuView.findViewById<TextView>(R.id.tv_action_text)
        val ivMark = menuView.findViewById<ImageView>(R.id.iv_action_icon)
        
        btnMark.visibility = View.VISIBLE
        tvMark.text = if (sub.isCompleted) "MARK INCOMPLETE" else "MARK COMPLETE"
        ivMark.setImageResource(if (sub.isCompleted) R.drawable.icons8_refresh_100 else R.drawable.icons8_check_mark_100)
        ivMark.imageTintList = ColorStateList.valueOf(Color.WHITE)

        val btnEdit = menuView.findViewById<View>(R.id.menu_edit)
        val btnDelete = menuView.findViewById<View>(R.id.menu_delete)

        btnMark.setOnClickListener {
            sub.isCompleted = !sub.isCompleted
            refreshSubFeatures()
            popupWindow.dismiss()
        }

            btnEdit.setOnClickListener {
                popupWindow.dismiss()
                val intent = Intent(this, AddSubFeatureActivity::class.java).apply {
                    putExtra("PROJECT_ID", ideaId)
                    putExtra("SUB_FEATURE_ID", sub.id)
                }
                startActivity(intent)
            }

        btnDelete.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Delete Feature")
                .setMessage("Are you sure you want to remove '${sub.name}'?")
                .setPositiveButton("DELETE") { _, _ ->
                    DataManager.currentEditingIdeaSubFeatures.remove(sub)
                    DataManager.currentEditingIdeaSubFeatures.forEachIndexed { index, feature -> feature.position = index + 1 }
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

    private fun updatePriorityUI() {
        val (text, color) = when(currentPriority) {
            2 -> "HIGH" to Color.parseColor("#FF5252")
            1 -> "MED" to Color.parseColor("#FFB800")
            else -> "LOW" to Color.parseColor("#2EC4B6")
        }
        btnPriority.text = text
        btnPriority.backgroundTintList = ColorStateList.valueOf(color)
        headerBgAccent.backgroundTintList = ColorStateList.valueOf(color)
        
        // Update save button color if valid
        if (btnSave.isEnabled) {
            btnSave.setTextColor(color)
        }
    }

    private fun validateInputs() {
        val title = titleInput.text.toString().trim()
        val isValid = title.isNotEmpty()
        btnSave.alpha = if (isValid) 1.0f else 0.3f
        btnSave.isEnabled = isValid
        
        val priorityColor = when(currentPriority) {
            2 -> Color.parseColor("#FF5252")
            1 -> Color.parseColor("#FFB800")
            else -> Color.parseColor("#2EC4B6")
        }
        
        if (isValid) btnSave.setTextColor(priorityColor) else btnSave.setTextColor(Color.GRAY)
    }

    private fun showConvertConfirmationDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_custom_confirm)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val btnCancel = dialog.findViewById<TextView>(R.id.btn_confirm_cancel)
        val btnAction = dialog.findViewById<TextView>(R.id.btn_confirm_action)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnAction.setOnClickListener {
            dialog.dismiss()
            convertToProject()
        }

        showDialogSafe(dialog)
    }

    private fun convertToProject() {
        val title = titleInput.text.toString().trim()
        if (title.isNotEmpty()) {
            val idea = existingIdea ?: Note(title = title, content = contentInput.text.toString(), category = "Project")
            idea.title = title
            idea.content = contentInput.text.toString()
            idea.category = "Project"
            idea.isDualExist = false
            idea.status = "In Progress"
            idea.priority = currentPriority
            idea.subFeatures.clear()
            idea.subFeatures.addAll(DataManager.currentEditingIdeaSubFeatures)
            idea.ideaGoals.clear()
            idea.ideaGoals.addAll(tempGoals)
            idea.isGlobalProject = true

            lifecycleScope.launch {
                if (existingIdea == null) {
                    repository.insertProject(idea)
                } else {
                    repository.updateProject(idea)
                }
                DataManager.saveData(this@AddIdeaActivity)
                DataManager.currentEditingIdeaSubFeatures.clear()
                Toast.makeText(this@AddIdeaActivity, "Converted to Project Roadmap!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun saveIdea() {
        val title = titleInput.text.toString().trim()
        if (title.isNotEmpty()) {
            val idea = existingIdea ?: Note(title = title, content = contentInput.text.toString(), category = "Idea")
            idea.title = title
            idea.content = contentInput.text.toString()
            idea.category = "Idea"
            idea.priority = currentPriority
            idea.subFeatures.clear()
            idea.subFeatures.addAll(DataManager.currentEditingIdeaSubFeatures)
            idea.ideaGoals.clear()
            idea.ideaGoals.addAll(tempGoals)
            idea.isGlobalProject = true

            lifecycleScope.launch {
                if (existingIdea == null) {
                    repository.insertProject(idea)
                } else {
                    repository.updateProject(idea)
                }
                DataManager.saveData(this@AddIdeaActivity)
                DataManager.currentEditingIdeaSubFeatures.clear()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DataManager.currentEditingIdeaSubFeatures.clear()
    }

}
