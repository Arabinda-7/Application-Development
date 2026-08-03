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
                background = ContextCompat.getDrawable(this@ViewProjectActivity, R.drawable.priority_chip_bg)
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

        // 2. Active Section
        if (activeSubs.isNotEmpty()) {
            addSectionHeader("Active (${activeSubs.size})", isActiveSubfeaturesExpanded) {
                isActiveSubfeaturesExpanded = !isActiveSubfeaturesExpanded
                refreshSubFeatures()
            }
            if (isActiveSubfeaturesExpanded) {
                activeSubs.forEach { sub -> containerSubfeatures.addView(createSubFeatureItem(sub)) }
            }
        }

        // 3. Completed Section
        if (completedSubs.isNotEmpty()) {
            addSectionHeader("Completed (${completedSubs.size})", isCompletedSubfeaturesExpanded) {
                isCompletedSubfeaturesExpanded = !isCompletedSubfeaturesExpanded
                refreshSubFeatures()
            }
            if (isCompletedSubfeaturesExpanded) {
                completedSubs.forEach { sub -> containerSubfeatures.addView(createSubFeatureItem(sub)) }
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

    private fun createSubFeatureItem(sub: ProjectFeature): View {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 4.dpToPx(), 0, 4.dpToPx())
            isClickable = true
            isFocusable = true
            background = ContextCompat.getDrawable(this@ViewProjectActivity, R.drawable.glass_card_bg)
            backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val tvName = TextView(this).apply {
            text = "${sub.position}. ${sub.name}"
            setTextColor(Color.WHITE)
            textSize = 15f
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
                background = ContextCompat.getDrawable(this@ViewProjectActivity, R.drawable.priority_chip_bg)
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
            background = ContextCompat.getDrawable(this@ViewProjectActivity, R.drawable.priority_chip_bg)
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
                setPadding(8.dpToPx(), 0, 0, 0)
            } ?: run {
                visibility = View.GONE
            }
        }

        val tvDetails = TextView(this).apply {
            text = sub.details
            setTextColor(Color.GRAY)
            textSize = 12f
            setPadding(24.dpToPx(), 4.dpToPx(), 8.dpToPx(), 8.dpToPx())
            visibility = if (sub.isExpanded && sub.details.isNotEmpty()) View.VISIBLE else View.GONE
        }

        header.addView(tvName)
        header.addView(containerMeta)
        header.addView(tvDate)
        
        layout.addView(header)
        layout.addView(tvDetails)

        layout.setOnClickListener {
            if (sub.details.isNotEmpty()) {
                if (sub.isExpanded) {
                    sub.isExpanded = false
                    expandedFeatureIds.remove(sub.id)
                } else {
                    if (expandedFeatureIds.size >= 2) {
                        val oldestId = expandedFeatureIds.pollFirst()
                        if (oldestId != null) {
                            project?.subFeatures?.find { it.id == oldestId }?.isExpanded = false
                        }
                    }
                    sub.isExpanded = true
                    expandedFeatureIds.addLast(sub.id)
                }
                refreshSubFeatures()
            }
        }
        
        layout.setOnLongClickListener {
            showSubFeatureMenu(it, sub)
            true
        }

        return layout
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
        goalsList.removeAllViews()
        project?.ideaGoals?.forEach { goal ->
            val tvGoal = TextView(this).apply {
                text = "• ${goal.text}"
                setTextColor(Color.WHITE)
                textSize = 14f
                setPadding(0, 4.dpToPx(), 0, 4.dpToPx())
            }
            goalsList.addView(tvGoal)
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
