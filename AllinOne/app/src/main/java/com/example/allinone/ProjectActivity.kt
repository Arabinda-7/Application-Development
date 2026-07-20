package com.example.allinone

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import java.text.SimpleDateFormat
import java.util.*

class ProjectActivity : BaseActivity() {

    private val allNotes = DataManager.notes
    private lateinit var projectAdapter: ProjectNoteAdapter
    private lateinit var ideaAdapter: NoteAdapter
    private lateinit var gestureDetector: android.view.GestureDetector
    private var displayNotes = mutableListOf<Note>()
    private var displayIdeas = mutableListOf<Note>()
    private var isProjectsTab = true
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.project_root_layout)) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(v.paddingLeft, statusBars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottom_navigation_projects)) { v, insets ->
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, navBars.bottom)
            insets
        }

        val projectList = findViewById<RecyclerView>(R.id.project_notes_list)
        projectList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        val ideaList = findViewById<RecyclerView>(R.id.project_ideas_list)
        ideaList.layoutManager = LinearLayoutManager(this)

        val dateTextView = findViewById<TextView>(R.id.tv_date)
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        dateTextView.text = sdf.format(DataManager.getTrackingCalendar().time)

        updateDisplayList()
        projectAdapter = ProjectNoteAdapter(displayNotes) {
            DataManager.saveData(this)
            updateDisplayList()
        }
        projectList.adapter = projectAdapter

        ideaAdapter = NoteAdapter(displayIdeas) {
            DataManager.saveData(this)
            updateDisplayList()
        }
        ideaList.adapter = ideaAdapter

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        val btnCreate = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btn_add_project_note)
        if (DataManager.projectAddThemeColor != -1) {
            btnCreate.backgroundTintList = ColorStateList.valueOf(DataManager.projectAddThemeColor)
        }
        btnCreate.setOnClickListener {
            if (isProjectsTab) {
                startActivity(Intent(this, AddProjectActivity::class.java))
            } else {
                startActivity(Intent(this, AddIdeaActivity::class.java))
            }
        }

        setupBottomNavigation()
        updateUI(false) // Set Ideas as default on launch
        setupGestureDetector()
    }

    override fun onResume() {
        super.onResume()
        updateDisplayList()
    }

    private lateinit var ivProjects: ImageView
    private lateinit var tvProjects: TextView
    private lateinit var ivNotes: ImageView
    private lateinit var tvNotes: TextView
    private lateinit var btnAdd: View
    private lateinit var btnEditMode: View
    private lateinit var btnWorkspace: View

    private fun setupBottomNavigation() {
        val navProjects = findViewById<View>(R.id.nav_projects)
        val navNotes = findViewById<View>(R.id.nav_notes)

        ivProjects = findViewById(R.id.iv_projects_icon)
        tvProjects = findViewById(R.id.tv_projects_label)
        ivNotes = findViewById(R.id.iv_notes_icon)
        tvNotes = findViewById(R.id.tv_notes_label)
        btnAdd = findViewById(R.id.btn_add_project_note)
        btnEditMode = findViewById(R.id.btn_edit_mode)
        btnWorkspace = findViewById(R.id.btn_workspace)

        navProjects.setOnClickListener { updateUI(true) }
        navNotes.setOnClickListener { updateUI(false) }

        btnEditMode.setOnClickListener {
            isEditMode = !isEditMode
            val activeColor = ContextCompat.getColor(this, R.color.chip_selected)
            val inactiveColor = Color.WHITE
            val activeBg = Color.parseColor("#33FFFFFF")
            val inactiveBg = Color.parseColor("#11FFFFFF")

            (btnEditMode as ImageButton).imageTintList = ColorStateList.valueOf(if (isEditMode) activeColor else inactiveColor)
            btnEditMode.backgroundTintList = ColorStateList.valueOf(if (isEditMode) activeBg else inactiveBg)

            Toast.makeText(this, if (isEditMode) "Edit Mode: ON" else "Edit Mode: OFF", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btn_project_settings).setOnClickListener { 
            startActivity(Intent(this, ProjectSettingsActivity::class.java))
        }
        findViewById<View>(R.id.btn_workspace).setOnClickListener {
            startActivity(Intent(this, com.example.allinone.workspace.ui.activity.WorkspaceActivity::class.java))
        }
    }

    fun onProjectItemClick(note: Note) {
        if (isEditMode) {
            val intent = Intent(this, AddProjectActivity::class.java).apply {
                putExtra("PROJECT_INDEX", allNotes.indexOf(note))
            }
            startActivity(intent)
        } else {
            showProjectDetailsDialog(note)
        }
    }

    private fun updateUI(isProjects: Boolean) {
        val projectList = findViewById<View>(R.id.project_notes_list)
        val ideaList = findViewById<View>(R.id.project_ideas_list)
        val bottomNav = findViewById<View>(R.id.bottom_navigation_projects)
        val navProjects = findViewById<View>(R.id.nav_projects)
        val navNotes = findViewById<View>(R.id.nav_notes)

        // Feature: Safety Check - Ensure current tab is valid
        var targetIsProjects = isProjects
        if (!DataManager.projectRoadmapsEnabled && targetIsProjects) targetIsProjects = false
        if (!DataManager.projectIdeasEnabled && !targetIsProjects) targetIsProjects = true

        // Feature: Dynamic Footer Visibility (Hide if only one section is active)
        val showFooter = DataManager.projectRoadmapsEnabled && DataManager.projectIdeasEnabled
        bottomNav.visibility = if (showFooter) View.VISIBLE else View.GONE

        navProjects.visibility = if (DataManager.projectRoadmapsEnabled) View.VISIBLE else View.GONE
        navNotes.visibility = if (DataManager.projectIdeasEnabled) View.VISIBLE else View.GONE

        isProjectsTab = targetIsProjects
        projectList.visibility = if (targetIsProjects) View.VISIBLE else View.GONE
        ideaList.visibility = if (targetIsProjects) View.GONE else View.VISIBLE
        btnAdd.visibility = View.VISIBLE

        // Programmatic padding adjustment for when footer is hidden
        val bottomPadding = if (showFooter) 100.dpToPx() else 24.dpToPx()
        projectList.setPadding(0, 0, 0, bottomPadding)
        ideaList.setPadding(0, 0, 0, bottomPadding)

        val activeColor = Color.WHITE
        val inactiveColor = Color.parseColor("#80FFFFFF") // 50% White for inactive
        val activeBg = Color.parseColor("#33FFFFFF") // 20% White for active background
        val inactiveBg = Color.TRANSPARENT

        // Highlight Active Section
        if (targetIsProjects) {
            btnEditMode.visibility = View.VISIBLE
            btnWorkspace.visibility = View.VISIBLE
            
            ivProjects.imageTintList = ColorStateList.valueOf(activeColor)
            ivProjects.backgroundTintList = ColorStateList.valueOf(activeBg)
            tvProjects.setTextColor(activeColor)
            tvProjects.setTypeface(null, android.graphics.Typeface.BOLD)

            ivNotes.imageTintList = ColorStateList.valueOf(inactiveColor)
            ivNotes.backgroundTintList = ColorStateList.valueOf(inactiveBg)
            tvNotes.setTextColor(inactiveColor)
            tvNotes.setTypeface(null, android.graphics.Typeface.NORMAL)
        } else {
            btnEditMode.visibility = View.GONE
            btnWorkspace.visibility = View.GONE

            ivProjects.imageTintList = ColorStateList.valueOf(inactiveColor)
            ivProjects.backgroundTintList = ColorStateList.valueOf(inactiveBg)
            tvProjects.setTextColor(inactiveColor)
            tvProjects.setTypeface(null, android.graphics.Typeface.NORMAL)

            ivNotes.imageTintList = ColorStateList.valueOf(activeColor)
            ivNotes.backgroundTintList = ColorStateList.valueOf(activeBg)
            tvNotes.setTextColor(activeColor)
            tvNotes.setTypeface(null, android.graphics.Typeface.BOLD)
        }
    }

    private fun setupGestureDetector() {
        gestureDetector = android.view.GestureDetector(this, object : SwipeGestureListener() {
            override fun onSwipeLeft() {
                if (isProjectsTab && DataManager.projectIdeasEnabled) updateUI(false)
            }

            override fun onSwipeRight() {
                if (!isProjectsTab && DataManager.projectRoadmapsEnabled) updateUI(true)
            }
        })
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent?): Boolean {
        if (ev != null) gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun updateDisplayList() {
        displayNotes.clear()
        displayIdeas.clear()

        val activeNotes = allNotes.filter { !it.isArchived }
        
        // Feature: Dual Exist (Global OR Per-Project)
        val roadmapList = activeNotes.filter {
            DataManager.projectDualExistEnabled || it.isDualExist || it.category == "Project" || it.subFeatures.isNotEmpty()
        }
        val visibleRoadmaps = if (DataManager.projectAutoArchive) {
            roadmapList.filter { it.status != "Completed" }
        } else {
            roadmapList
        }
        displayNotes.addAll(visibleRoadmaps.sortedWith(compareByDescending<Note> { it.isPinned }
            .thenBy { it.status == "Completed" }
            .thenByDescending { it.timestamp }))

        val ideasList = activeNotes.filter {
            DataManager.projectDualExistEnabled || it.isDualExist || it.category == "ProjectIdea" || (it.category != "Project" && it.subFeatures.isEmpty())
        }
        displayIdeas.addAll(ideasList.sortedByDescending { it.timestamp })

        if (::projectAdapter.isInitialized) {
            projectAdapter.updateNotes(displayNotes)
        }
        if (::ideaAdapter.isInitialized) {
            ideaAdapter.notifyDataSetChanged()
        }
    }

    fun showEditProjectNoteDialog(note: Note) {
        val intent = Intent(this, AddProjectActivity::class.java).apply {
            putExtra("PROJECT_INDEX", allNotes.indexOf(note))
        }
        startActivity(intent)
    }

    fun showEditIdeaDialog(note: Note) {
        val intent = Intent(this, AddIdeaActivity::class.java).apply {
            putExtra("IDEA_INDEX", allNotes.indexOf(note))
        }
        startActivity(intent)
    }

    private fun startPulseAnimation(view: View) {
        if (view.tag == "pulsing") return
        view.tag = "pulsing"
        view.animate().alpha(0.4f).setDuration(800).withEndAction {
            view.animate().alpha(1.0f).setDuration(800).withEndAction {
                view.tag = null
                if (view.visibility == View.VISIBLE) startPulseAnimation(view)
            }
        }.start()
    }

    fun showProjectHistoryDialog(note: Note) {
        val dialog = Dialog(this, R.style.FullScreenDialog)
        dialog.setContentView(R.layout.dialog_project_history)

        val historyList = dialog.findViewById<RecyclerView>(R.id.history_list)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_history)

        historyList.layoutManager = LinearLayoutManager(this)

        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_project_history, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val history = note.changeHistory.sortedByDescending { it.timestamp }[position]
                holder.itemView.findViewById<TextView>(R.id.tv_history_action).text = history.action
                holder.itemView.findViewById<TextView>(R.id.tv_history_description).text = history.description
                holder.itemView.findViewById<TextView>(R.id.tv_history_time).text = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(history.timestamp))
            }

            override fun getItemCount(): Int = note.changeHistory.size
        }
        historyList.adapter = adapter

        btnClose.setOnClickListener { dialog.dismiss() }
        showDialogSafe(dialog)
    }

    private fun showSelectSubfeatureDialog(note: Note, pending: List<ProjectFeature>, parentDialog: Dialog) {
        val dialog = Dialog(this, R.style.SeamlessDialog)
        dialog.setContentView(R.layout.dialog_select_subfeature)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val rv = dialog.findViewById<RecyclerView>(R.id.rv_pending_subfeatures)
        val btnCancel = dialog.findViewById<View>(R.id.btn_close_selection)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val sub = pending[position]
                (holder.itemView as TextView).apply {
                    text = "${sub.position}. ${sub.name}"
                    setTextColor(Color.WHITE)
                    setPadding(16.dpToPx(), 32, 16.dpToPx(), 32)
                    setOnClickListener {
                        sub.isCompleted = true
                        
                        // Weighted Progress
                        val totalWeight = note.subFeatures.sumOf { it.weight }.coerceAtLeast(1)
                        val completedWeight = note.subFeatures.filter { it.isCompleted }.sumOf { it.weight }
                        val progress = (completedWeight * 100) / totalWeight
                        
                        note.progress = progress
                        if (progress == 100) note.status = "Completed"
                        addHistoryLog(note, "Task Completed", "Finished via Menu: ${sub.name}")
                        DataManager.saveData(this@ProjectActivity)
                        updateDisplayList()

                        dialog.dismiss()
                        if (note.status != "Completed") {
                            parentDialog.dismiss()
                            showProjectDetailsDialog(note)
                        } else {
                            parentDialog.dismiss()
                        }
                    }
                }
            }

            override fun getItemCount(): Int = pending.size
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun addHistoryLog(note: Note, action: String, description: String) {
        note.changeHistory.add(ProjectHistory(action = action, description = description))
        DataManager.addActivity("$action: ${note.title}")
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    fun showProjectDetailsDialog(note: Note) {
        val dialog = Dialog(this, R.style.FullScreenDialog)
        dialog.setContentView(R.layout.dialog_project_details)

        var currentDetailsTagFilter = "ALL"

        val tvTitle = dialog.findViewById<TextView>(R.id.tv_detail_title)
        val tvStatus = dialog.findViewById<TextView>(R.id.tv_detail_status)
        val tvPriority = dialog.findViewById<TextView>(R.id.tv_detail_priority)
        val tvContent = dialog.findViewById<TextView>(R.id.tv_detail_content)
        val tvTimestamps = dialog.findViewById<TextView>(R.id.tv_detail_timestamps)
        val containerSubfeatures = dialog.findViewById<LinearLayout>(R.id.container_detail_subfeatures)
        val containerGoals = dialog.findViewById<LinearLayout>(R.id.container_detail_goals)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_details)
        val btnMenu = dialog.findViewById<ImageButton>(R.id.btn_detail_menu)

        val headerDescription = dialog.findViewById<View>(R.id.container_description_header)
        val ivDescChevron = dialog.findViewById<ImageView>(R.id.iv_description_chevron)
        val headerGoals = dialog.findViewById<View>(R.id.container_goals_header)
        val ivGoalsChevron = dialog.findViewById<ImageView>(R.id.iv_goals_chevron)

        val btnCycleColor = dialog.findViewById<View>(R.id.btn_detail_cycle_color)
        val colorPreviewComp = dialog.findViewById<View>(R.id.tv_detail_color_preview)
        val btnEditDeadline = dialog.findViewById<View>(R.id.btn_detail_edit_deadline)
        val tvDeadlineComp = dialog.findViewById<TextView>(R.id.tv_detail_deadline_compact)

        var isDescExpanded = true
        var isGoalsExpanded = true

        headerDescription.setOnClickListener {
            isDescExpanded = !isDescExpanded
            tvContent.visibility = if (isDescExpanded) View.VISIBLE else View.GONE
            ivDescChevron.setImageResource(if (isDescExpanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float)
        }

        headerGoals.setOnClickListener {
            isGoalsExpanded = !isGoalsExpanded
            containerGoals.visibility = if (isGoalsExpanded) View.VISIBLE else View.GONE
            ivGoalsChevron.setImageResource(if (isGoalsExpanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float)
        }

        tvTitle.text = note.title
        
        val statusText = when(note.status) {
            "DOING", "In Progress" -> "DOING"
            "DONE", "Completed" -> "DONE"
            "HOLD", "On Hold" -> "HOLD"
            else -> "TODO"
        }
        tvStatus.text = statusText
        
        tvContent.text = if (note.content.isEmpty()) "No description provided." else note.content

        btnMenu.setOnClickListener { view ->
            val inflater = LayoutInflater.from(this)
            val menuView = inflater.inflate(R.layout.layout_project_detail_menu, null)
            val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
            popupWindow.elevation = 20f

            val btnMarkDone = menuView.findViewById<View>(R.id.menu_detail_mark_done)
            val btnEdit = menuView.findViewById<View>(R.id.menu_detail_edit)
            val btnHistory = menuView.findViewById<View>(R.id.menu_detail_history)
            val btnConvertNote = menuView.findViewById<View>(R.id.menu_detail_convert_note)

            if (note.status == "Completed") {
                btnMarkDone.visibility = View.GONE
            }

            btnMarkDone.setOnClickListener {
                popupWindow.dismiss()
                val pendingSubfeatures = note.subFeatures.filter { !it.isCompleted }.sortedBy { it.position }

                if (pendingSubfeatures.isEmpty()) {
                    note.status = "Completed"
                    note.progress = 100
                    addHistoryLog(note, "Quick Mark", "Project marked as Completed")
                    DataManager.saveData(this@ProjectActivity)
                    updateDisplayList()
                    dialog.dismiss()
                } else {
                    showSelectSubfeatureDialog(note, pendingSubfeatures, dialog)
                }
            }

            btnEdit.setOnClickListener {
                popupWindow.dismiss()
                dialog.dismiss()
                val intent = Intent(this@ProjectActivity, AddProjectActivity::class.java).apply {
                    putExtra("PROJECT_INDEX", allNotes.indexOf(note))
                }
                startActivity(intent)
            }

            btnHistory.setOnClickListener {
                popupWindow.dismiss()
                showProjectHistoryDialog(note)
            }

            btnConvertNote.setOnClickListener {
                popupWindow.dismiss()
                note.category = "ProjectIdea"
                note.isDualExist = true // Ensure it stays in both if requested
                addHistoryLog(note, "Conversion", "Converted Roadmap back to Idea state")
                DataManager.saveData(this@ProjectActivity)
                updateDisplayList()
                dialog.dismiss()
                Toast.makeText(this@ProjectActivity, "Roadmap converted to Idea!", Toast.LENGTH_SHORT).show()
            }

            popupWindow.showAsDropDown(view, -150, 0)
        }

        val priorityText = when(note.priority) {
            2 -> "HIGH"
            1 -> "MED"
            else -> "LOW"
        }
        val priorityColor = when(note.priority) {
            2 -> Color.RED
            1 -> Color.parseColor("#FFB800")
            else -> Color.parseColor("#2EC4B6")
        }
        tvPriority.text = priorityText
        tvPriority.setTextColor(priorityColor)

        // Simple Clicking logic for Details Dialog
        val statusContainer = dialog.findViewById<View>(R.id.tv_detail_status).parent as View
        val priorityContainer = dialog.findViewById<View>(R.id.tv_detail_priority).parent as View

        statusContainer.setOnClickListener {
            val statuses = listOf("TODO", "DOING", "DONE", "HOLD")
            val currentStatus = when(note.status) {
                "DOING", "In Progress" -> "DOING"
                "DONE", "Completed" -> "DONE"
                "HOLD", "On Hold" -> "HOLD"
                else -> "TODO"
            }
            val nextIdx = (statuses.indexOf(currentStatus) + 1) % 4
            note.status = statuses[nextIdx]
            tvStatus.text = statuses[nextIdx]
            
            addHistoryLog(note, "Quick Change", "Status updated to ${note.status}")
            DataManager.saveData(this)
            updateDisplayList()
        }

        priorityContainer.setOnClickListener {
            val nextPriority = (note.priority + 1) % 3
            note.priority = nextPriority
            
            val newPText = when(nextPriority) {
                2 -> "HIGH"
                1 -> "MED"
                else -> "LOW"
            }
            val newPColor = when(nextPriority) {
                2 -> Color.RED
                1 -> Color.parseColor("#FFB800")
                else -> Color.parseColor("#2EC4B6")
            }
            tvPriority.text = newPText
            tvPriority.setTextColor(newPColor)
            
            addHistoryLog(note, "Quick Change", "Priority updated to $newPText")
            DataManager.saveData(this)
            updateDisplayList()
        }

        // Compact Meta Row 2 Logic
        colorPreviewComp.backgroundTintList = ColorStateList.valueOf(note.color.takeIf { it != -1 } ?: Color.BLUE)
        
        val updateDeadlineUIComp = {
            tvDeadlineComp.text = note.deadline?.let {
                SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it))
            } ?: "No Set"
        }
        updateDeadlineUIComp()

        btnCycleColor.setOnClickListener {
            val colors = listOf(0xFFFF7A59, 0xFFFFB800, 0xFF2EC4B6, 0xFF3A86F0, 0xFF1A73E8, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF4CAF50)
            var currentIdx = colors.map { it.toInt() }.indexOf(note.color)
            if (currentIdx == -1) currentIdx = 0
            val nextIdx = (currentIdx + 1) % colors.size
            note.color = colors[nextIdx].toInt()
            colorPreviewComp.backgroundTintList = ColorStateList.valueOf(note.color)
            DataManager.saveData(this)
            updateDisplayList()
        }

        btnEditDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()
            note.deadline?.let { calendar.timeInMillis = it }
            DatePickerDialog(this, { _, y, m, d ->
                val newCal = Calendar.getInstance()
                newCal.set(y, m, d)
                note.deadline = newCal.timeInMillis
                updateDeadlineUIComp()
                DataManager.saveData(this)
                updateDisplayList()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }


        val sdfMeta = SimpleDateFormat("MMM dd", Locale.getDefault())
        val createdStr = sdfMeta.format(Date(note.timestamp))
        val lastUpdate = note.changeHistory.maxByOrNull { it.timestamp }?.timestamp ?: note.timestamp
        val updatedStr = sdfMeta.format(Date(lastUpdate))
        tvTimestamps.text = "Created: $createdStr | Updated: $updatedStr"

        // Populate Goals
        containerGoals.removeAllViews()
        if (note.ideaGoals.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No goals added yet."
                setTextColor(Color.parseColor("#4DFFFFFF"))
                textSize = 12f
                setPadding(8.dpToPx(), 0, 0, 0)
            }
            containerGoals.addView(emptyTv)
        } else {
            note.ideaGoals.forEach { goal ->
                val goalLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(12.dpToPx(), 8.dpToPx(), 12.dpToPx(), 6.dpToPx())
                }
                
                val contentRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER_VERTICAL
                }

                val bullet = TextView(this).apply {
                    text = "•"
                    setTextColor(Color.parseColor("#80FFFFFF"))
                    textSize = 14f
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = 8.dpToPx() }
                }
                val goalTv = TextView(this).apply {
                    text = goal.text
                    setTextColor(Color.WHITE)
                    textSize = 14f
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }
                contentRow.addView(bullet)
                contentRow.addView(goalTv)
                goalLayout.addView(contentRow)

                val timeStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(goal.timestamp))
                val tvTime = TextView(this).apply {
                    text = timeStr
                    setTextColor(Color.parseColor("#4DFFFFFF"))
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 9f)
                    gravity = android.view.Gravity.END
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        topMargin = 2.dpToPx()
                    }
                }
                goalLayout.addView(tvTime)

                containerGoals.addView(goalLayout)
            }
        }

        fun refreshDetailsSubFeatures() {
            containerSubfeatures.removeAllViews()

            val chipContainer = HorizontalScrollView(this).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(0, 0, 0, 16.dpToPx())
                scrollBarSize = 0
                isHorizontalScrollBarEnabled = false
            }

            val rgFilters = RadioGroup(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            val usedTags = note.subFeatures.map { it.tag.ifEmpty { "GENERAL" } }.distinct()
            val tags = mutableListOf("ALL")
            tags.addAll(usedTags.sorted())

            tags.forEach { tag ->
                val rb = RadioButton(this).apply {
                    id = View.generateViewId()
                    text = tag.uppercase()
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 10f)
                    setTextColor(Color.WHITE)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    gravity = android.view.Gravity.CENTER
                    buttonDrawable = null
                    background = ContextCompat.getDrawable(this@ProjectActivity, R.drawable.filter_chip_bg)

                    val height = 32.dpToPx()
                    val params = RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height)
                    params.setMargins(0, 0, 8.dpToPx(), 0)
                    layoutParams = params
                    setPadding(20.dpToPx(), 0, 20.dpToPx(), 0)

                    isChecked = tag == currentDetailsTagFilter

                    setOnClickListener {
                        currentDetailsTagFilter = tag
                        refreshDetailsSubFeatures()
                    }
                }
                rgFilters.addView(rb)
            }
            chipContainer.addView(rgFilters)
            containerSubfeatures.addView(chipContainer)

            val filteredSubFeatures = if (currentDetailsTagFilter == "ALL") {
                note.subFeatures
            } else {
                note.subFeatures.filter { (it.tag.ifEmpty { "GENERAL" }).equals(currentDetailsTagFilter, ignoreCase = true) }
            }

            val activeFeatures = filteredSubFeatures.filter { !it.isCompleted }.sortedBy { it.position }
            val completedFeatures = filteredSubFeatures.filter { it.isCompleted }

            if (activeFeatures.isNotEmpty()) {
                val activeHeader = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 12.dpToPx())
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        DataManager.projectActiveExpanded = !DataManager.projectActiveExpanded
                        refreshDetailsSubFeatures()
                    }
                }

                val tvActiveLabel = TextView(this).apply {
                    text = "ACTIVE (${activeFeatures.size})"
                    setTextColor(Color.parseColor("#80FFFFFF"))
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 11f)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }

                val ivChevron = ImageView(this).apply {
                    setImageResource(if (DataManager.projectActiveExpanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float)
                    imageTintList = ColorStateList.valueOf(Color.parseColor("#80FFFFFF"))
                    layoutParams = LinearLayout.LayoutParams(16.dpToPx(), 16.dpToPx())
                }

                activeHeader.addView(tvActiveLabel)
                activeHeader.addView(ivChevron)
                containerSubfeatures.addView(activeHeader)

                if (DataManager.projectActiveExpanded) {
                    activeFeatures.forEach { sub ->
                        containerSubfeatures.addView(createSubFeatureViewItem(note, sub, dialog, { refreshDetailsSubFeatures() }))
                    }
                }
            }

            if (completedFeatures.isNotEmpty()) {
                val completedHeader = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 12.dpToPx())
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        DataManager.projectCompletedExpanded = !DataManager.projectCompletedExpanded
                        refreshDetailsSubFeatures()
                    }
                }

                val tvCompLabel = TextView(this).apply {
                    text = "COMPLETED (${completedFeatures.size})"
                    setTextColor(Color.parseColor("#4DFFFFFF"))
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 11f)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }

                val ivChevron = ImageView(this).apply {
                    setImageResource(if (DataManager.projectCompletedExpanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float)
                    imageTintList = ColorStateList.valueOf(Color.parseColor("#4DFFFFFF"))
                    layoutParams = LinearLayout.LayoutParams(16.dpToPx(), 16.dpToPx())
                }

                completedHeader.addView(tvCompLabel)
                completedHeader.addView(ivChevron)
                containerSubfeatures.addView(completedHeader)

                if (DataManager.projectCompletedExpanded) {
                    completedFeatures.sortedByDescending { it.position }.forEach { sub ->
                        containerSubfeatures.addView(createSubFeatureViewItem(note, sub, dialog, { refreshDetailsSubFeatures() }))
                    }
                }
            }
        }

        refreshDetailsSubFeatures()
        btnClose.setOnClickListener { dialog.dismiss() }
        showDialogSafe(dialog)
    }

    private fun createSubFeatureViewItem(note: Note, sub: ProjectFeature, parentDialog: Dialog, onRefresh: () -> Unit): View {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 2.dpToPx(), 0, 2.dpToPx())
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 8.dpToPx()) // Reduced gap further (8dp)
            layoutParams = params
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val tvSerial = TextView(this).apply {
            text = "${sub.position}."
            setTextColor(Color.GRAY)
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding(0, 0, 12.dpToPx(), 0)
        }

        val tvName = TextView(this).apply {
            text = sub.name
            setTextColor(Color.WHITE)
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 15f)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            if (sub.isCompleted) {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                alpha = 0.5f
            }
        }

        val tvNote = TextView(this).apply {
            text = sub.details
            setTextColor(Color.GRAY)
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
            setPadding(24.dpToPx(), 4.dpToPx(), 32.dpToPx(), 8.dpToPx())
            visibility = View.GONE
        }
        
        // Right: Tag & Metadata Icons
        val containerRight = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, 0, 8.dpToPx(), 0)
        }

        if (sub.tag.isNotEmpty()) {
            containerRight.addView(TextView(this).apply {
                text = sub.tag.uppercase()
                setTextColor(Color.WHITE)
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 8f)
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(8.dpToPx(), 2.dpToPx(), 8.dpToPx(), 2.dpToPx())
                val tagColor = when(sub.tag.uppercase()) {
                    "UI" -> Color.parseColor("#E91E63")
                    "LOGIC" -> Color.parseColor("#673AB7")
                    "BUG" -> Color.RED
                    else -> Color.parseColor("#33FFFFFF")
                }
                background = ContextCompat.getDrawable(this@ProjectActivity, R.drawable.priority_chip_bg)
                backgroundTintList = ColorStateList.valueOf(tagColor)
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = 8.dpToPx() }
            })
        }

        if (sub.weight > 1) {
            containerRight.addView(TextView(this).apply {
                text = "w${sub.weight}"
                setTextColor(Color.parseColor("#80FFFFFF"))
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 9f)
                setPadding(4.dpToPx(), 2.dpToPx(), 4.dpToPx(), 2.dpToPx())
            })
        }

        if (sub.blockedByNodeId.isNotEmpty()) {
            val isBlocked = note.subFeatures.find { it.id == sub.blockedByNodeId }?.isCompleted == false
            if (isBlocked) {
                containerRight.addView(ImageView(this).apply {
                    setImageResource(R.drawable.icons8_lock_100)
                    imageTintList = ColorStateList.valueOf(Color.parseColor("#FF5252"))
                    layoutParams = LinearLayout.LayoutParams(14.dpToPx(), 14.dpToPx()).apply { marginEnd = 4.dpToPx() }
                })
            }
        }

        if (sub.resourceUrl.isNotEmpty()) {
            containerRight.addView(ImageView(this).apply {
                setImageResource(R.drawable.icons8_connect_100)
                imageTintList = ColorStateList.valueOf(Color.parseColor("#1A73E8"))
                layoutParams = LinearLayout.LayoutParams(16.dpToPx(), 16.dpToPx())
                setOnClickListener {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(sub.resourceUrl))
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this@ProjectActivity, "Invalid Link", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        val tvUrgency = View(this).apply {
            val size = 6.dpToPx()
            layoutParams = LinearLayout.LayoutParams(size, size).apply { marginEnd = 8.dpToPx() }
            val color = when(sub.priority) {
                2 -> Color.RED
                1 -> Color.parseColor("#FFB800")
                else -> Color.TRANSPARENT
            }
            background = ContextCompat.getDrawable(this@ProjectActivity, R.drawable.circle_selected_bg)
            backgroundTintList = ColorStateList.valueOf(color)
            visibility = if (sub.priority > 0) View.VISIBLE else View.GONE
        }

        val clickTarget = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            gravity = android.view.Gravity.CENTER_VERTICAL
            addView(tvSerial)
            addView(tvUrgency)
            addView(tvName)
            setOnClickListener {
                if (sub.details.isNotEmpty()) {
                    tvNote.visibility = if (tvNote.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                }
            }
            setOnLongClickListener {
                showSubFeatureMenu(it, note, sub, parentDialog, onRefresh)
                true
            }
        }

        header.addView(clickTarget)
        header.addView(containerRight)

        if (sub.dueDate != null) {
            val tvDate = TextView(this).apply {
                text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(sub.dueDate!!))
                setTextColor(Color.parseColor("#FF5252"))
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 10f)
                setPadding(12.dpToPx(), 0, 8.dpToPx(), 0)
            }
            header.addView(tvDate)
        }
        
        layout.addView(header)
        layout.addView(tvNote)
        return layout
    }

    private fun showSubFeatureMenu(anchor: View, note: Note, sub: ProjectFeature, parentDialog: Dialog, onRefresh: () -> Unit) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.layout_custom_menu, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 20f

        val btnMark = menuView.findViewById<View>(R.id.menu_take_day_off)
        val tvMark = menuView.findViewById<TextView>(R.id.tv_action_text)
        val ivMark = menuView.findViewById<ImageView>(R.id.iv_action_icon)
        
        btnMark.visibility = View.VISIBLE
        tvMark.text = if (sub.isCompleted) "MARK INCOMPLETE" else "MARK COMPLETE"
        ivMark.setImageResource(if (sub.isCompleted) R.drawable.icons8_refresh_100 else R.drawable.icons8_check_mark_100)
        ivMark.imageTintList = ColorStateList.valueOf(Color.WHITE)

        val ivEditMenu = menuView.findViewById<ImageView>(R.id.iv_edit_icon)
        ivEditMenu.setImageResource(R.drawable.icons8_edit_pencil_100)

        val ivDeleteMenu = menuView.findViewById<ImageView>(R.id.iv_delete_icon)
        ivDeleteMenu.setImageResource(R.drawable.icons8_trash_100)

        btnMark.setOnClickListener {
            sub.isCompleted = !sub.isCompleted
            
            // Weighted Progress
            val totalWeight = note.subFeatures.sumOf { it.weight }.coerceAtLeast(1)
            val completedWeight = note.subFeatures.filter { it.isCompleted }.sumOf { it.weight }
            val progress = (completedWeight * 100) / totalWeight
            
            note.progress = progress
            if (progress == 100) note.status = "Completed"
            DataManager.saveData(this)
            popupWindow.dismiss()
            onRefresh()
        }

        menuView.findViewById<View>(R.id.menu_edit).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_delete).visibility = View.GONE

        menuView.findViewById<View>(R.id.menu_hide_unhide).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_undo).visibility = View.GONE

        popupWindow.showAsDropDown(anchor, 100, 0)
    }

    fun showProjectMenu(anchor: View, note: Note) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.layout_custom_menu, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 20f

        // Configure Menu Items
        val btnDayOff = menuView.findViewById<View>(R.id.menu_take_day_off)
        val btnEdit = menuView.findViewById<View>(R.id.menu_edit)
        val btnDelete = menuView.findViewById<View>(R.id.menu_delete)
        val btnPin = menuView.findViewById<View>(R.id.menu_hide_unhide)
        val tvPin = menuView.findViewById<TextView>(R.id.tv_hide_unhide_text)
        val ivPin = menuView.findViewById<ImageView>(R.id.iv_hide_unhide_icon)
        val btnUndo = menuView.findViewById<View>(R.id.menu_undo)

        btnDayOff.visibility = View.GONE
        btnUndo.visibility = View.GONE

        btnPin.visibility = View.VISIBLE
        tvPin.text = if (note.isPinned) "UNPIN" else "PIN"
        ivPin.setImageResource(if (note.isPinned) android.R.drawable.btn_star_big_off else android.R.drawable.btn_star_big_on)
        ivPin.imageTintList = ColorStateList.valueOf(Color.WHITE)

        btnEdit.setOnClickListener {
            popupWindow.dismiss()
            if (note.category == "Project") {
                showEditProjectNoteDialog(note)
            } else {
                showEditIdeaDialog(note)
            }
        }

        btnDelete.setOnClickListener {
            popupWindow.dismiss()
            allNotes.remove(note)
            DataManager.saveData(this)
            updateDisplayList()
        }

        btnPin.setOnClickListener {
            popupWindow.dismiss()
            note.isPinned = !note.isPinned
            DataManager.saveData(this)
            updateDisplayList()
        }

        popupWindow.showAsDropDown(anchor, 150, -100)
    }
}
