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
import com.example.allinone.data.model.*
import com.example.allinone.domain.repository.ProjectRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.LinkedList
import javax.inject.Inject

@AndroidEntryPoint
class ViewProjectActivity : BaseActivity() {

    @Inject
    lateinit var repository: ProjectRepository

    private var projectId: Long = -1
    private var project: Note? = null
    
    private lateinit var titleDisplay: TextView
    private lateinit var contentDisplay: TextView
    private lateinit var tvGridStatus: TextView
    private lateinit var tvGridPriority: TextView
    private lateinit var tvDeadlineDisplay: TextView
    private lateinit var colorPreview: View
    private lateinit var btnPin: ImageView
    private lateinit var btnEdit: TextView
    private lateinit var containerSubfeatures: LinearLayout
    private lateinit var goalsList: LinearLayout
    private lateinit var tvFooterDates: TextView
    private lateinit var auraView: View
    private var isDescriptionExpanded = false
    private var isGoalsExpanded = false
    private var currentSubfeatureFilter = "ALL"
    private var currentSearchQuery = ""
    private var isActiveSubfeaturesExpanded = true
    private var isCompletedSubfeaturesExpanded = false
    private val expandedFeatureIds = LinkedList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_project)

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

                updateUI()
            }
        } else {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data in case it was edited
        if (projectId != -1L) {
            lifecycleScope.launch {
                val projects = repository.getAllProjects().first()
                project = projects.find { it.timestamp == projectId }
                project?.let { updateUI() }
            }
        }
    }

    private fun initViews() {
        titleDisplay = findViewById(R.id.note_title_input) // It's an EditText in XML but disabled
        contentDisplay = findViewById(R.id.note_content_input) // Also EditText but disabled
        tvGridStatus = findViewById(R.id.tv_grid_status)
        tvGridPriority = findViewById(R.id.tv_grid_priority)
        tvDeadlineDisplay = findViewById(R.id.tv_deadline_display)
        colorPreview = findViewById(R.id.note_color_preview)
        btnPin = findViewById(R.id.btn_pin)
        btnEdit = findViewById(R.id.btn_save_note) // Text is "EDIT"
        containerSubfeatures = findViewById(R.id.container_subfeatures)
        goalsList = findViewById(R.id.goals_list)
        tvFooterDates = findViewById(R.id.tv_footer_dates)
        auraView = findViewById(R.id.aura_background)

        findViewById<View>(R.id.btn_close_note).setOnClickListener { finish() }
    }

    private fun setupLogic() {
        updateUI()

        btnEdit.setOnClickListener {
            startActivity(Intent(this, EditProjectActivity::class.java).apply {
                putExtra("PROJECT_ID", projectId)
            })
        }

        // Toggle sections logic
        findViewById<View>(R.id.layout_subfeatures_header_toggle).setOnClickListener {
            val container = findViewById<View>(R.id.layout_subfeatures_full_container)
            val chevron = findViewById<ImageView>(R.id.iv_subfeatures_main_chevron)
            val isVisible = container.visibility == View.VISIBLE
            container.visibility = if (isVisible) View.GONE else View.VISIBLE
            chevron.setImageResource(if (isVisible) android.R.drawable.arrow_down_float else android.R.drawable.arrow_up_float)
        }

        findViewById<View>(R.id.container_description_header).setOnClickListener {
            isDescriptionExpanded = !isDescriptionExpanded
            contentDisplay.visibility = if (isDescriptionExpanded) View.VISIBLE else View.GONE
            findViewById<ImageView>(R.id.iv_description_chevron).setImageResource(if (isDescriptionExpanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float)
        }

        findViewById<View>(R.id.container_goals_header).setOnClickListener {
            isGoalsExpanded = !isGoalsExpanded
            findViewById<View>(R.id.container_goals).visibility = if (isGoalsExpanded) View.VISIBLE else View.GONE
            findViewById<ImageView>(R.id.iv_goals_chevron).setImageResource(if (isGoalsExpanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float)
        }

        findViewById<EditText>(R.id.et_search_subfeatures).addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString() ?: ""
                refreshSubFeatures()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun updateUI() {
        project?.let { p ->
            titleDisplay.text = p.title
            contentDisplay.text = p.content
            tvGridStatus.text = p.status
            tvGridPriority.text = when(p.priority) {
                0 -> "LOW"; 2 -> "HIGH"; else -> "MED"
            }
            tvDeadlineDisplay.text = p.deadline?.let { 
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it)) 
            } ?: "No Set"
            
            val color = if (p.color != -1) p.color else ContextCompat.getColor(this, R.color.card_blue)
            colorPreview.backgroundTintList = ColorStateList.valueOf(color)
            
            auraView.background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(color, Color.BLACK)
            )

            btnPin.setImageResource(if (p.isPinned) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
            
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            tvFooterDates.text = "Created: ${sdf.format(Date(p.timestamp))} | Updated: ${sdf.format(Date(p.updatedAt))}"

            refreshSubFeatures()
            refreshGoalsUI()
        }
    }

    private fun refreshSubFeatures() {
        containerSubfeatures.removeAllViews()
        containerSubfeatures.addView(ProjectUiHelper.createSubfeatureFilterBar(this, currentSubfeatureFilter) { cat ->
            currentSubfeatureFilter = cat
            refreshSubFeatures()
        })

        val allSubs = project?.subFeatures ?: emptyList()
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
                // No edit in View mode, but we can allow viewing details
            },
            onLongClick = { view, s -> showSubFeatureMenu(view, s) },
            onToggleExpansion = { s ->
                ProjectUiHelper.handleSubfeatureExpansion(s, expandedFeatureIds, project?.subFeatures ?: emptyList())
                refreshSubFeatures()
            }
        )
    }

    private fun showSubFeatureMenu(anchor: View, sub: ProjectFeature) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.menu_project_feature, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 20f

        val btnMark = menuView.findViewById<View>(R.id.menu_take_day_off)
        val tvMark = menuView.findViewById<TextView>(R.id.tv_action_text)
        val ivMark = menuView.findViewById<ImageView>(R.id.iv_action_icon)
        
        btnMark.visibility = View.VISIBLE
        tvMark.text = if (sub.isCompleted) "MARK INCOMPLETE" else "MARK COMPLETE"
        ivMark.setImageResource(if (sub.isCompleted) R.drawable.icons8_refresh_100 else R.drawable.icons8_check_mark_100)
        ivMark.imageTintList = ColorStateList.valueOf(Color.WHITE)

        menuView.findViewById<View>(R.id.menu_edit).visibility = View.GONE

        btnMark.setOnClickListener {
            sub.isCompleted = !sub.isCompleted
            lifecycleScope.launch {
                project?.let { repository.updateProject(it) }
                DataManager.saveData(this@ViewProjectActivity)
                updateUI()
                popupWindow.dismiss()
            }
        }

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            showStyledConfirmationDialog(
                title = "Delete Milestone",
                message = "Are you sure you want to delete '${sub.name}'?",
                actionText = "DELETE",
                actionColor = Color.parseColor("#FF5252")
            ) {
                project?.subFeatures?.remove(sub)
                project?.subFeatures?.forEachIndexed { index, feature -> feature.position = index + 1 }
                lifecycleScope.launch {
                    project?.let { repository.updateProject(it) }
                    DataManager.saveData(this@ViewProjectActivity)
                    updateUI()
                }
            }
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_hide_unhide).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_undo).visibility = View.GONE

        popupWindow.showAsDropDown(anchor, 100, 0)
    }

    private fun refreshGoalsUI() {
        project?.let { p ->
            ProjectUiHelper.renderGoalsReadOnly(this, goalsList, p.ideaGoals)
        }
    }

}
