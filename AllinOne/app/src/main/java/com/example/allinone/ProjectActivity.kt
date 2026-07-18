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
                showAddIdeaDialog()
            }
        }

        setupBottomNavigation()
        updateUI(true) // Set Projects as default on launch
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

    private fun setupBottomNavigation() {
        val navProjects = findViewById<View>(R.id.nav_projects)
        val navNotes = findViewById<View>(R.id.nav_notes)

        ivProjects = findViewById(R.id.iv_projects_icon)
        tvProjects = findViewById(R.id.tv_projects_label)
        ivNotes = findViewById(R.id.iv_notes_icon)
        tvNotes = findViewById(R.id.tv_notes_label)
        btnAdd = findViewById(R.id.btn_add_project_note)

        navProjects.setOnClickListener { updateUI(true) }
        navNotes.setOnClickListener { updateUI(false) }

        val btnEditMode = findViewById<ImageButton>(R.id.btn_edit_mode)
        btnEditMode.setOnClickListener {
            isEditMode = !isEditMode
            val activeColor = ContextCompat.getColor(this, R.color.chip_selected)
            val inactiveColor = Color.WHITE
            val activeBg = Color.parseColor("#33FFFFFF")
            val inactiveBg = Color.parseColor("#11FFFFFF")

            btnEditMode.imageTintList = ColorStateList.valueOf(if (isEditMode) activeColor else inactiveColor)
            btnEditMode.backgroundTintList = ColorStateList.valueOf(if (isEditMode) activeBg else inactiveBg)
            
            Toast.makeText(this, if (isEditMode) "Edit Mode: ON" else "Edit Mode: OFF", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btn_project_settings).setOnClickListener { showProjectSettingsDialog() }
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
            ivProjects.imageTintList = ColorStateList.valueOf(activeColor)
            ivProjects.backgroundTintList = ColorStateList.valueOf(activeBg)
            tvProjects.setTextColor(activeColor)
            tvProjects.setTypeface(null, android.graphics.Typeface.BOLD)
            
            ivNotes.imageTintList = ColorStateList.valueOf(inactiveColor)
            ivNotes.backgroundTintList = ColorStateList.valueOf(inactiveBg)
            tvNotes.setTextColor(inactiveColor)
            tvNotes.setTypeface(null, android.graphics.Typeface.NORMAL)
        } else {
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

    fun showAddProjectNoteDialog() {
        setupProjectDialog(null)
    }

    fun showEditProjectNoteDialog(note: Note) {
        setupProjectDialog(note)
    }

    fun showEditIdeaDialog(note: Note) {
        showAddIdeaDialog(note)
    }

    private fun setupProjectDialog(existingNote: Note? = null) {
        val dialog = Dialog(this, R.style.FullScreenDialog)
        dialog.setContentView(R.layout.dialog_add_project_note)

        val titleInput = dialog.findViewById<EditText>(R.id.note_title_input)
        val tvTitleHint = dialog.findViewById<TextView>(R.id.tv_title_hint_project)
        val contentInput = dialog.findViewById<EditText>(R.id.note_content_input)

        val rgStatus = dialog.findViewById<RadioGroup>(R.id.rg_status)
        val rgPriority = dialog.findViewById<RadioGroup>(R.id.rg_priority)

        val seekProgress = dialog.findViewById<SeekBar>(R.id.seek_progress)
        val tvProgressValue = dialog.findViewById<TextView>(R.id.tv_progress_value)
        val btnPin = dialog.findViewById<ImageView>(R.id.btn_pin)
        val colorPreview = dialog.findViewById<View>(R.id.note_color_preview)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_note)
        if (existingNote != null) btnSave.text = "UPDATE"

        val tvDeadlineDisplay = dialog.findViewById<TextView>(R.id.tv_deadline_display)
        val containerSubfeatures = dialog.findViewById<LinearLayout>(R.id.container_subfeatures)
        val etNewSubfeature = dialog.findViewById<EditText>(R.id.et_new_subfeature)
        val btnAddSubfeature = dialog.findViewById<View>(R.id.btn_add_subfeature)
        val containerTemplates = dialog.findViewById<LinearLayout>(R.id.container_templates)

        // Initial State
        var isPinned = existingNote?.isPinned ?: false
        var selectedColor = existingNote?.color?.takeIf { it != -1 } ?: ContextCompat.getColor(this, R.color.card_blue)
        var selectedDeadline = existingNote?.deadline
        val tempSubFeatures = existingNote?.subFeatures?.toMutableList() ?: mutableListOf()

        fun validateInputs() {
            val title = titleInput.text.toString().trim()
            val isValid = title.isNotEmpty()

            btnSave.alpha = if (isValid) 1.0f else 0.3f
            btnSave.isEnabled = isValid

            tvTitleHint.visibility = if (isValid) View.GONE else View.VISIBLE
            if (!isValid) startPulseAnimation(tvTitleHint)

            if (isValid) btnSave.setTextColor(selectedColor) else btnSave.setTextColor(Color.GRAY)
            tvTitleHint.setTextColor(selectedColor)
        }

        titleInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateInputs() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        fun updateDeadlineUI() {
            tvDeadlineDisplay.text = selectedDeadline?.let {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
            } ?: "No Deadline Set"
        }

        fun refreshSubFeatures() {
            containerSubfeatures.removeAllViews()
            tempSubFeatures.sortedBy { it.position }.forEach { sub ->
                val layout = LinearLayout(this@ProjectActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, 8, 0, 8)
                }

                val header = LinearLayout(this@ProjectActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER_VERTICAL
                }

                val tvSerial = TextView(this@ProjectActivity).apply {
                    text = "${sub.position}."
                    setTextColor(Color.GRAY)
                    textSize = 14f
                    setPadding(8.dpToPx(), 0, 8.dpToPx(), 0)
                }

                val ctView = CheckedTextView(this@ProjectActivity).apply {
                    text = sub.name
                    setTextColor(Color.WHITE)
                    isChecked = sub.isCompleted
                    setCheckMarkTintList(ColorStateList.valueOf(Color.WHITE))
                    setPadding(0, 8, 0, 8)
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)

                    // Visual Completion Feedback
                    if (sub.isCompleted) {
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        alpha = 0.5f
                    } else {
                        paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                        alpha = 1.0f
                    }

                    setOnClickListener {
                        sub.isCompleted = !sub.isCompleted
                        isChecked = sub.isCompleted

                        val progress = if (tempSubFeatures.isNotEmpty()) (tempSubFeatures.count { it.isCompleted } * 100) / tempSubFeatures.size else 0

                        seekProgress.progress = progress
                        existingNote?.let { if (progress == 100) it.status = "Completed" }
                        existingNote?.let { addHistoryLog(it, "Task Toggled", "${if (sub.isCompleted) "Completed" else "Reopened"}: ${sub.name}") }
                        DataManager.saveData(this@ProjectActivity)
                        updateDisplayList()
                        refreshSubFeatures() // Refresh visuals
                    }

                    setOnLongClickListener {
                        AlertDialog.Builder(this@ProjectActivity)
                            .setTitle("Delete Sub-feature")
                            .setMessage("Are you sure you want to remove '${sub.name}'?")
                            .setPositiveButton("Delete") { _, _ ->
                                tempSubFeatures.remove(sub)
                                refreshSubFeatures()
                                DataManager.saveData(this@ProjectActivity)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                        true
                    }
                }

                val btnEdit = ImageButton(this@ProjectActivity).apply {
                    setImageResource(R.drawable.icons8_edit_pencil_100)
                    background = ContextCompat.getDrawable(this@ProjectActivity, android.R.color.transparent)
                    imageTintList = ColorStateList.valueOf(Color.GRAY)
                    layoutParams = LinearLayout.LayoutParams(24.dpToPx(), 24.dpToPx()).apply {
                        marginEnd = 8.dpToPx()
                    }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
                }

                header.addView(tvSerial)
                header.addView(ctView)
                header.addView(btnEdit)
                layout.addView(header)

                // Metadata: Tag and Due Date
                if (sub.tag.isNotEmpty() || sub.dueDate != null) {
                    val tvMeta = TextView(this@ProjectActivity).apply {
                        val tagStr = if (sub.tag.isNotEmpty()) "[${sub.tag}] " else ""
                        val dateStr = sub.dueDate?.let { "Due: ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it))}" } ?: ""
                        text = "$tagStr$dateStr"
                        setTextColor(if (sub.tag == "BUG") Color.RED else Color.parseColor("#FFB800"))
                        textSize = 10f
                        setPadding(32.dpToPx(), 0, 0, 4.dpToPx())
                    }
                    layout.addView(tvMeta)
                }

                val tvNote = TextView(this@ProjectActivity).apply {
                    text = sub.details
                    setTextColor(Color.GRAY)
                    textSize = 12f
                    visibility = if (sub.details.isNotEmpty()) View.VISIBLE else View.GONE
                    setPadding(12.dpToPx(), 0, 0, 8.dpToPx())
                }
                layout.addView(tvNote)

                btnEdit.setOnClickListener {
                    showEditSubFeatureDialog(sub) {
                        refreshSubFeatures()
                    }
                }

                tvSerial.setOnClickListener {
                    showEditSubFeatureDialog(sub) {
                        refreshSubFeatures()
                    }
                }

                containerSubfeatures.addView(layout)
            }
        }

        // Setup Templates
        if (existingNote == null) {
            DataManager.projectTemplates.forEach { (name, steps) ->
                val templateBtn = TextView(this).apply {
                    text = name
                    setTextColor(Color.WHITE)
                    textSize = 12f
                    setPadding(24.dpToPx(), 12.dpToPx(), 24.dpToPx(), 12.dpToPx())
                    background = ContextCompat.getDrawable(this@ProjectActivity, R.drawable.priority_chip_bg)
                    val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    params.marginEnd = 12.dpToPx()
                    layoutParams = params

                    setOnClickListener {
                        tempSubFeatures.clear()
                        steps.forEachIndexed { i, step ->
                            tempSubFeatures.add(ProjectFeature(step, position = i + 1))
                        }
                        refreshSubFeatures()
                    }
                }
                containerTemplates.addView(templateBtn)
            }
        } else {
            dialog.findViewById<View>(R.id.container_templates_header).visibility = View.GONE
            dialog.findViewById<View>(R.id.scroll_templates).visibility = View.GONE
        }

        existingNote?.let {
            titleInput.setText(it.title)
            contentInput.setText(it.content)
            seekProgress.progress = it.progress
            tvProgressValue.text = "${it.progress}%"
            btnPin.setImageResource(if (isPinned) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
        }
        colorPreview.backgroundTintList = ColorStateList.valueOf(selectedColor)

        // Map priority and status to buttons
        when (existingNote?.priority ?: 1) {
            0 -> rgPriority.check(R.id.rb_priority_low)
            1 -> rgPriority.check(R.id.rb_priority_med)
            2 -> rgPriority.check(R.id.rb_priority_high)
        }

        when (existingNote?.status ?: "Not Started") {
            "Not Started" -> rgStatus.check(R.id.rb_status_todo)
            "In Progress" -> rgStatus.check(R.id.rb_status_progress)
            "Completed" -> rgStatus.check(R.id.rb_status_completed)
            "On Hold" -> rgStatus.check(R.id.rb_status_hold)
        }

        updateDeadlineUI()
        refreshSubFeatures()
        validateInputs()

        // Listeners
        seekProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvProgressValue.text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        dialog.findViewById<View>(R.id.btn_set_deadline).setOnClickListener {
            val cal = Calendar.getInstance()
            selectedDeadline?.let { cal.timeInMillis = it }
            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d)
                selectedDeadline = cal.timeInMillis
                updateDeadlineUI()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            showDialogSafe(datePicker)
        }

        btnAddSubfeature.setOnClickListener {
            val name = etNewSubfeature.text.toString().trim()
            if (name.isNotEmpty()) {
                val nextPos = if (tempSubFeatures.isEmpty()) 1 else tempSubFeatures.maxOf { it.position } + 1
                val newFeature = ProjectFeature(name, position = nextPos)
                tempSubFeatures.add(newFeature)

                refreshSubFeatures()
                etNewSubfeature.text.clear()

                // Update progress
                val progress = (tempSubFeatures.count { it.isCompleted } * 100) / tempSubFeatures.size
                seekProgress.progress = progress
                tvProgressValue.text = "$progress%"
            }
        }

        btnPin.setOnClickListener {
            isPinned = !isPinned
            btnPin.setImageResource(if (isPinned) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
        }

        colorPreview.setOnClickListener {
            val colors = listOf(ContextCompat.getColor(this, R.color.card_blue), ContextCompat.getColor(this, R.color.card_orange), ContextCompat.getColor(this, R.color.card_green), Color.MAGENTA, Color.RED, Color.CYAN)
            selectedColor = colors[(colors.indexOf(selectedColor) + 1) % colors.size]
            colorPreview.backgroundTintList = ColorStateList.valueOf(selectedColor)
            validateInputs()
        }

        dialog.findViewById<View>(R.id.btn_close_note).setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val title = titleInput.text.toString()
            if (title.isNotEmpty()) {
                val note = existingNote ?: Note(title = title, content = "")

                // Track Changes for existing notes
                if (existingNote != null) {
                    val newStatus = when (rgStatus.checkedRadioButtonId) {
                        R.id.rb_status_progress -> "In Progress"
                        R.id.rb_status_completed -> "Completed"
                        R.id.rb_status_hold -> "On Hold"
                        else -> "Not Started"
                    }
                    if (note.status != newStatus) {
                        addHistoryLog(note, "Status Updated", "Changed from ${note.status} to $newStatus")
                    }
                    if (note.progress != seekProgress.progress) {
                        addHistoryLog(note, "Progress Updated", "Progress set to ${seekProgress.progress}%")
                    }

                    // Track sub-feature changes
                    if (note.subFeatures.size != tempSubFeatures.size) {
                        addHistoryLog(note, "Roadmap Updated", "Sub-features list modified.")
                    } else {
                        // Check for completion changes
                        val oldCompleted = note.subFeatures.count { it.isCompleted }
                        val newCompleted = tempSubFeatures.count { it.isCompleted }
                        if (oldCompleted != newCompleted) {
                            addHistoryLog(note, "Roadmap Progress", "Updated completion of sub-tasks.")
                        }
                    }
                } else {
                    addHistoryLog(note, "Project Created", "Initial project board setup.")
                }

                note.title = title
                note.content = contentInput.text.toString()

                note.status = when (rgStatus.checkedRadioButtonId) {
                    R.id.rb_status_progress -> "In Progress"
                    R.id.rb_status_completed -> "Completed"
                    R.id.rb_status_hold -> "On Hold"
                    else -> "Not Started"
                }

                note.priority = when (rgPriority.checkedRadioButtonId) {
                    R.id.rb_priority_low -> 0
                    R.id.rb_priority_high -> 2
                    else -> 1
                }

                note.progress = seekProgress.progress
                note.isPinned = isPinned
                note.color = selectedColor
                note.category = "Project"
                note.deadline = selectedDeadline
                note.subFeatures.clear()
                note.subFeatures.addAll(tempSubFeatures)

                if (existingNote == null) {
                    allNotes.add(0, note)
                    addHistoryLog(note, "Project Created", "Initial project board setup.")
                }
                DataManager.saveData(this)
                updateDisplayList()
                dialog.dismiss()
            }
        }
        showDialogSafe(dialog)
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

    private fun showEditSubFeatureDialog(sub: ProjectFeature, showNameField: Boolean = false, onSaved: () -> Unit) {
        val dialog = Dialog(this, R.style.SeamlessDialog)
        dialog.setContentView(R.layout.dialog_edit_subfeature)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val btnClose = dialog.findViewById<View>(R.id.btn_close_subfeature)
        val etSerial = dialog.findViewById<EditText>(R.id.et_serial_input)
        val etName = dialog.findViewById<EditText>(R.id.et_name_input)
        val etDetails = dialog.findViewById<EditText>(R.id.et_details_input)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_subfeature)

        val containerTags = dialog.findViewById<LinearLayout>(R.id.container_feature_tags)
        val tvDeadline = dialog.findViewById<TextView>(R.id.tv_feature_deadline)

        var selectedTag = sub.tag

        fun refreshTagsUI() {
            containerTags.removeAllViews()
            DataManager.projectCustomTags.forEach { tagName ->
                val chip = TextView(this).apply {
                    text = tagName
                    setTextColor(Color.WHITE)
                    textSize = 12f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setPadding(24.dpToPx(), 12.dpToPx(), 24.dpToPx(), 12.dpToPx())
                    
                    val isSelected = selectedTag == tagName
                    background = ContextCompat.getDrawable(this@ProjectActivity, R.drawable.priority_chip_bg)
                    backgroundTintList = ColorStateList.valueOf(if (isSelected) Color.parseColor("#1A73E8") else Color.parseColor("#33FFFFFF"))
                    
                    val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    params.marginEnd = 8.dpToPx()
                    layoutParams = params

                    setOnClickListener {
                        selectedTag = if (selectedTag == tagName) "" else tagName
                        refreshTagsUI()
                    }
                }
                containerTags.addView(chip)
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        if (showNameField) {
            etName.visibility = View.VISIBLE
            etName.setText(sub.name)
            etSerial.visibility = View.GONE
        } else {
            etSerial.setText(sub.position.toString())
            etSerial.isFocusable = false
            etSerial.isClickable = true
            etSerial.setOnClickListener {
                showNumberRollerDialog(sub.position) { newPos ->
                    etSerial.setText(newPos.toString())
                }
            }
        }

        etDetails.setText(sub.details)
        etDetails.setSelection(etDetails.text.length)

        refreshTagsUI()

        // Feature: Due Dates
        fun updateSubDeadlineUI() {
            tvDeadline.text = sub.dueDate?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it)) } ?: "Set Due Date"
        }
        updateSubDeadlineUI()

        tvDeadline.setOnClickListener {
            val cal = Calendar.getInstance()
            sub.dueDate?.let { cal.timeInMillis = it }
            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d)
                sub.dueDate = cal.timeInMillis
                updateSubDeadlineUI()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            showDialogSafe(datePicker)
        }

        btnSave.setOnClickListener {
            if (showNameField) {
                sub.name = etName.text.toString().trim()
            } else {
                val newPos = etSerial.text.toString().toIntOrNull()
                if (newPos != null) sub.position = newPos
            }
            sub.details = etDetails.text.toString().trim()
            sub.tag = selectedTag

            onSaved()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showNumberRollerDialog(currentVal: Int, onSelected: (Int) -> Unit) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_number_picker)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val picker = dialog.findViewById<NumberPicker>(R.id.number_picker)
        picker.minValue = 1
        picker.maxValue = 100
        picker.value = currentVal
        
        dialog.findViewById<View>(R.id.btn_confirm_picker).setOnClickListener {
            onSelected(picker.value)
            dialog.dismiss()
        }
        showDialogSafe(dialog)
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
                        val progress = (note.subFeatures.count { it.isCompleted } * 100) / note.subFeatures.size
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
        val tvDeadline = dialog.findViewById<TextView>(R.id.tv_detail_deadline)
        val tvTimestamps = dialog.findViewById<TextView>(R.id.tv_detail_timestamps)
        val containerSubfeatures = dialog.findViewById<LinearLayout>(R.id.container_detail_subfeatures)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_details)
        val btnMenu = dialog.findViewById<ImageButton>(R.id.btn_detail_menu)

        tvTitle.text = note.title
        tvStatus.text = note.status.uppercase()
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

        tvDeadline.text = note.deadline?.let {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
        } ?: "No Deadline Set"

        val sdfMeta = SimpleDateFormat("MMM dd", Locale.getDefault())
        val createdStr = sdfMeta.format(Date(note.timestamp))
        val lastUpdate = note.changeHistory.maxByOrNull { it.timestamp }?.timestamp ?: note.timestamp
        val updatedStr = sdfMeta.format(Date(lastUpdate))
        tvTimestamps.text = "Created: $createdStr | Updated: $updatedStr"

        fun refreshDetailsSubFeatures() {
            // Removed TransitionManager to make filtering feel instantaneous rather than a jarring "refresh"
            containerSubfeatures.removeAllViews()

            // ... (rest of the chip setup remains the same)
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

            // Requirement 2: Show only categories which are present
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
                        refreshDetailsSubFeatures() // Re-enabled filtering
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

            // 1. Active Section (Collapsible - Requirement: Remove card background, show like completed)
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
                    setTextColor(Color.parseColor("#80FFFFFF")) // Semi-white for header
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

            // 2. Completed Section (Collapsible)
            if (completedFeatures.isNotEmpty()) {
                val completedHeader = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    setPadding(8.dpToPx(), 24.dpToPx(), 8.dpToPx(), 12.dpToPx())
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
        
        val clickTarget = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            gravity = android.view.Gravity.CENTER_VERTICAL
            addView(tvSerial)
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
            val progress = (note.subFeatures.count { it.isCompleted } * 100) / note.subFeatures.size.coerceAtLeast(1)
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

    private fun showProjectSettingsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_project_settings)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val swArchive = dialog.findViewById<SwitchCompat>(R.id.sw_auto_archive)
        val swSync = dialog.findViewById<SwitchCompat>(R.id.sw_synergy_sync)
        val swAlerts = dialog.findViewById<SwitchCompat>(R.id.sw_deadline_alerts)
        val swAnalytics = dialog.findViewById<SwitchCompat>(R.id.sw_analytics)
        val swDualExist = dialog.findViewById<SwitchCompat>(R.id.sw_dual_exist)
        val swAutoSave = dialog.findViewById<SwitchCompat>(R.id.sw_auto_save_ideas) ?: SwitchCompat(this) // Safety
        val swRoadmapsEnabled = dialog.findViewById<SwitchCompat>(R.id.sw_roadmaps_enabled)
        val swIdeasEnabled = dialog.findViewById<SwitchCompat>(R.id.sw_ideas_enabled)

        swArchive.isChecked = DataManager.projectAutoArchive
        swSync.isChecked = DataManager.projectSynergySync
        swAlerts.isChecked = DataManager.projectDeadlineAlerts
        swAnalytics.isChecked = DataManager.projectAnalyticsEnabled
        swDualExist.isChecked = DataManager.projectDualExistEnabled
        swAutoSave.isChecked = DataManager.projectAutoSaveIdeas
        swRoadmapsEnabled.isChecked = DataManager.projectRoadmapsEnabled
        swIdeasEnabled.isChecked = DataManager.projectIdeasEnabled

        dialog.findViewById<View>(R.id.item_auto_save_ideas)?.setOnClickListener {
            DataManager.projectAutoSaveIdeas = !DataManager.projectAutoSaveIdeas
            swAutoSave.isChecked = DataManager.projectAutoSaveIdeas
        }

        dialog.findViewById<View>(R.id.item_auto_archive).setOnClickListener {
            DataManager.projectAutoArchive = !DataManager.projectAutoArchive
            swArchive.isChecked = DataManager.projectAutoArchive
        }
        dialog.findViewById<View>(R.id.item_synergy_sync).setOnClickListener {
            DataManager.projectSynergySync = !DataManager.projectSynergySync
            swSync.isChecked = DataManager.projectSynergySync
        }
        dialog.findViewById<View>(R.id.item_deadline_alerts).setOnClickListener {
            DataManager.projectDeadlineAlerts = !DataManager.projectDeadlineAlerts
            swAlerts.isChecked = DataManager.projectDeadlineAlerts
        }
        dialog.findViewById<View>(R.id.item_analytics).setOnClickListener {
            DataManager.projectAnalyticsEnabled = !DataManager.projectAnalyticsEnabled
            swAnalytics.isChecked = DataManager.projectAnalyticsEnabled
        }
        dialog.findViewById<View>(R.id.item_dual_exist).setOnClickListener {
            DataManager.projectDualExistEnabled = !DataManager.projectDualExistEnabled
            swDualExist.isChecked = DataManager.projectDualExistEnabled
        }
        dialog.findViewById<View>(R.id.item_toggle_roadmaps).setOnClickListener {
            if (DataManager.projectRoadmapsEnabled && !DataManager.projectIdeasEnabled) {
                Toast.makeText(this, "At least one section must be enabled", Toast.LENGTH_SHORT).show()
            } else {
                DataManager.projectRoadmapsEnabled = !DataManager.projectRoadmapsEnabled
                swRoadmapsEnabled.isChecked = DataManager.projectRoadmapsEnabled
            }
        }
        dialog.findViewById<View>(R.id.item_toggle_ideas).setOnClickListener {
            if (DataManager.projectIdeasEnabled && !DataManager.projectRoadmapsEnabled) {
                Toast.makeText(this, "At least one section must be enabled", Toast.LENGTH_SHORT).show()
            } else {
                DataManager.projectIdeasEnabled = !DataManager.projectIdeasEnabled
                swIdeasEnabled.isChecked = DataManager.projectIdeasEnabled
            }
        }

        dialog.findViewById<View>(R.id.item_manage_templates).setOnClickListener {
            showManageTemplatesDialog()
        }

        dialog.findViewById<View>(R.id.item_manage_tags).setOnClickListener {
            showManageTagsDialog()
        }

        dialog.findViewById<View>(R.id.btn_close_settings).setOnClickListener {
            DataManager.saveData(this)
            updateUI(isProjectsTab)
            updateDisplayList()
            dialog.dismiss()
        }
        showDialogSafe(dialog)
    }

    private fun showManageTemplatesDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val etNew = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAdd = dialog.findViewById<View>(R.id.btn_add_category)
        val title = dialog.findViewById<TextView>(R.id.tv_categories_title)
        val btnDeleteMode = dialog.findViewById<ImageButton>(R.id.btn_toggle_delete_mode)

        title.text = "Project Templates"
        etNew.hint = "Template Name..."

        var isDeleteMode = false

        fun refresh() {
            container.removeAllViews()
            
            // Visual feedback for Delete Mode
            btnDeleteMode.imageTintList = ColorStateList.valueOf(if (isDeleteMode) Color.RED else Color.WHITE)

            DataManager.projectTemplates.keys.forEach { templateName ->
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_category_manage, container, false)
                itemView.findViewById<TextView>(R.id.tv_category_name).text = templateName

                val btnRemove = itemView.findViewById<View>(R.id.btn_remove_category)
                btnRemove.visibility = if (isDeleteMode) View.VISIBLE else View.GONE

                itemView.setOnClickListener {
                    if (isDeleteMode) {
                        // In delete mode, tapping item toggles mode off (or we could just let them tap the X)
                        isDeleteMode = false
                        refresh()
                    } else {
                        Toast.makeText(this, "Steps: ${DataManager.projectTemplates[templateName]?.joinToString(", ")}", Toast.LENGTH_LONG).show()
                    }
                }

                btnRemove.setOnClickListener {
                    if (DataManager.projectTemplates.size > 1) {
                        DataManager.projectTemplates.remove(templateName)
                        DataManager.saveData(this)
                        refresh()
                    } else {
                        Toast.makeText(this, "At least one template required", Toast.LENGTH_SHORT).show()
                    }
                }
                container.addView(itemView)
            }
        }

        btnDeleteMode.setOnClickListener {
            isDeleteMode = !isDeleteMode
            refresh()
        }

        btnAdd.setOnClickListener {
            val name = etNew.text.toString().trim()
            if (name.isNotEmpty() && !DataManager.projectTemplates.containsKey(name)) {
                showCreateTemplateStepsDialog(name) {
                    refresh()
                    etNew.text.clear()
                }
            }
        }

        refresh()
        dialog.show()
    }

    private fun showManageTagsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val etNew = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAdd = dialog.findViewById<View>(R.id.btn_add_category)
        val title = dialog.findViewById<TextView>(R.id.tv_categories_title)
        val btnDeleteMode = dialog.findViewById<ImageButton>(R.id.btn_toggle_delete_mode)

        title.text = "Project Tags"
        etNew.hint = "Tag Name (e.g. UI)..."

        var isDeleteMode = false

        fun refresh() {
            container.removeAllViews()
            
            // Visual feedback for Delete Mode
            btnDeleteMode.imageTintList = ColorStateList.valueOf(if (isDeleteMode) Color.RED else Color.WHITE)

            DataManager.projectCustomTags.forEach { tagName ->
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_category_manage, container, false)
                itemView.findViewById<TextView>(R.id.tv_category_name).text = tagName

                val btnRemove = itemView.findViewById<View>(R.id.btn_remove_category)
                btnRemove.visibility = if (isDeleteMode) View.VISIBLE else View.GONE

                itemView.setOnClickListener {
                    if (isDeleteMode) {
                        isDeleteMode = false
                        refresh()
                    }
                }

                btnRemove.setOnClickListener {
                    if (DataManager.projectCustomTags.size > 1) {
                        DataManager.projectCustomTags.remove(tagName)
                        DataManager.saveData(this)
                        refresh()
                    } else {
                        Toast.makeText(this, "At least one tag required", Toast.LENGTH_SHORT).show()
                    }
                }
                container.addView(itemView)
            }
        }

        btnDeleteMode.setOnClickListener {
            isDeleteMode = !isDeleteMode
            refresh()
        }

        btnAdd.setOnClickListener {
            val name = etNew.text.toString().trim().uppercase()
            if (name.isNotEmpty() && !DataManager.projectCustomTags.contains(name)) {
                DataManager.projectCustomTags.add(name)
                DataManager.saveData(this)
                refresh()
                etNew.text.clear()
            }
        }

        refresh()
        showDialogSafe(dialog)
    }

    private fun showCreateTemplateStepsDialog(templateName: String, onComplete: () -> Unit) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val container = dialog.findViewById<LinearLayout>(R.id.categories_container)
        val etStep = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAddStep = dialog.findViewById<View>(R.id.btn_add_category)
        val title = dialog.findViewById<TextView>(R.id.tv_categories_title)

        // Add a "SAVE TEMPLATE" button at the bottom
        val btnSave = TextView(this).apply {
            text = "SAVE TEMPLATE"
            setTextColor(Color.parseColor("#1A73E8"))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 40, 0, 40)
            isClickable = true
            isFocusable = true
            val outValue = android.util.TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            setBackgroundResource(outValue.resourceId)
        }

        title.text = "Add Steps for: $templateName"
        etStep.hint = "Step name (e.g. Design)..."

        val steps = mutableListOf<String>()

        fun refreshSteps() {
            container.removeAllViews()
            steps.forEach { step ->
                val itemView = LayoutInflater.from(this).inflate(R.layout.item_category_manage, container, false)
                itemView.findViewById<TextView>(R.id.tv_category_name).text = step
                itemView.findViewById<View>(R.id.btn_remove_category).setOnClickListener {
                    steps.remove(step)
                    refreshSteps()
                }
                container.addView(itemView)
            }
            container.addView(btnSave)
        }

        btnAddStep.setOnClickListener {
            val stepName = etStep.text.toString().trim()
            if (stepName.isNotEmpty()) {
                steps.add(stepName)
                etStep.text.clear()
                refreshSteps()
            }
        }

        btnSave.setOnClickListener {
            if (steps.isNotEmpty()) {
                DataManager.projectTemplates[templateName] = steps
                DataManager.saveData(this)
                onComplete()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Add at least one step", Toast.LENGTH_SHORT).show()
            }
        }

        refreshSteps()
        showDialogSafe(dialog)
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

    private var currentVoiceInput: EditText? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 201 && resultCode == RESULT_OK) {
            val results = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0) ?: ""
            currentVoiceInput?.let {
                val start = it.selectionStart
                val end = it.selectionEnd
                it.text.replace(Math.min(start, end), Math.max(start, end), spokenText, 0, spokenText.length)
            }
        }
    }

    private fun showAddIdeaDialog(existingIdea: Note? = null) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_add_note_project)

        val titleInput = dialog.findViewById<EditText>(R.id.note_title_input)
        val tvTitleHint = dialog.findViewById<TextView>(R.id.tv_title_hint_note) ?: dialog.findViewById<TextView>(R.id.tv_name_hint)
        val contentInput = dialog.findViewById<EditText>(R.id.note_content_input)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_note)
        if (existingIdea != null) btnSave.text = "UPDATE"
        val btnClose = dialog.findViewById<View>(R.id.btn_close_note)
        
        // Vibe Color Picker
        val containerVibeColors = dialog.findViewById<LinearLayout>(R.id.container_vibe_colors)
        var selectedVibeColor = existingIdea?.vibeColor ?: -1

        // Vibe Color Picker Logic
        val colors = listOf(Color.parseColor("#FFB800"), Color.parseColor("#2EC4B6"), Color.parseColor("#1A73E8"), Color.parseColor("#FF5252"), Color.MAGENTA, Color.CYAN)
        colors.forEach { color ->
            val colorView = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(40.dpToPx(), 40.dpToPx()).apply { marginEnd = 12.dpToPx() }
                background = ContextCompat.getDrawable(this@ProjectActivity, R.drawable.circle_selected_bg)
                backgroundTintList = ColorStateList.valueOf(color)
                setOnClickListener {
                    selectedVibeColor = color
                    Toast.makeText(this@ProjectActivity, "Vibe Set!", Toast.LENGTH_SHORT).show()
                }
            }
            containerVibeColors.addView(colorView)
        }

        // Journal Section
        val btnToggleJournal = dialog.findViewById<TextView>(R.id.btn_toggle_journal)
        val containerJournal = dialog.findViewById<View>(R.id.container_journal)
        val journalList = dialog.findViewById<LinearLayout>(R.id.journal_list)
        val etJournalInput = dialog.findViewById<EditText>(R.id.et_journal_input)
        val btnAddJournal = dialog.findViewById<View>(R.id.btn_add_journal)

        val tempJournal = existingIdea?.journalEntries?.toMutableList() ?: mutableListOf()

        fun refreshJournalUI() {
            journalList.removeAllViews()
            tempJournal.sortedByDescending { it.timestamp }.forEach { entry ->
                val tv = TextView(this).apply {
                    val date = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(entry.timestamp))
                    text = "[$date] ${entry.text}"
                    setTextColor(Color.GRAY)
                    textSize = 12f
                    setPadding(0, 4.dpToPx(), 0, 4.dpToPx())
                }
                journalList.addView(tv)
            }
        }
        refreshJournalUI()

        btnToggleJournal.setOnClickListener {
            val isVisible = containerJournal.visibility == View.VISIBLE
            containerJournal.visibility = if (isVisible) View.GONE else View.VISIBLE
            btnToggleJournal.text = if (isVisible) "IDEA EVOLUTION JOURNAL ▼" else "IDEA EVOLUTION JOURNAL ▲"
        }

        btnAddJournal.setOnClickListener {
            val text = etJournalInput.text.toString().trim()
            if (text.isNotEmpty()) {
                tempJournal.add(JournalEntry(text))
                etJournalInput.text.clear()
                refreshJournalUI()
            }
        }

        // Share & Mind Map
        dialog.findViewById<View>(R.id.btn_share_idea).setOnClickListener {
            val pitch = "PROJECT IDEA: ${titleInput.text}\n\nDESCRIPTION: ${contentInput.text}\n\nSUB-FEATURES:\n" + 
                        existingIdea?.subFeatures?.joinToString("\n") { "• ${it.name}" }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "New Project Idea")
                putExtra(Intent.EXTRA_TEXT, pitch)
            }
            startActivity(Intent.createChooser(intent, "Share Idea Pitch"))
        }

        dialog.findViewById<View>(R.id.btn_mind_map).setOnClickListener {
            IdeaMindMapDialog(this, existingIdea ?: Note(titleInput.text.toString(), contentInput.text.toString())).show()
        }

        fun validateInputs() {
            val title = titleInput.text.toString().trim()
            val isValid = title.isNotEmpty()
            btnSave.alpha = if (isValid) 1.0f else 0.3f
            btnSave.isEnabled = isValid
            tvTitleHint?.visibility = if (isValid) View.GONE else View.VISIBLE
            if (!isValid) tvTitleHint?.let { startPulseAnimation(it) }
            val themeColor = if (DataManager.projectAddThemeColor != -1) DataManager.projectAddThemeColor else Color.parseColor("#1A73E8")
            if (isValid) btnSave.setTextColor(themeColor) else btnSave.setTextColor(Color.GRAY)
            tvTitleHint?.setTextColor(themeColor)
        }

        titleInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateInputs() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        val btnBullet = dialog.findViewById<ImageButton>(R.id.btn_bullet_list)
        val btnNumeric = dialog.findViewById<ImageButton>(R.id.btn_numeric_list)
        val btnConvert = dialog.findViewById<TextView>(R.id.btn_convert_project)
        val btnConvertIcon = dialog.findViewById<View>(R.id.btn_convert_project_icon)
        val btnVoice = dialog.findViewById<View>(R.id.btn_voice_input)
        val btnPriority = dialog.findViewById<TextView>(R.id.btn_priority_tag)
        val tvCharCount = dialog.findViewById<TextView>(R.id.tv_char_count)
        val etInspirationUrl = dialog.findViewById<EditText>(R.id.et_inspiration_url)

        val containerSubfeatures = dialog.findViewById<LinearLayout>(R.id.container_subfeatures)
        val etNewSubfeature = dialog.findViewById<EditText>(R.id.et_new_subfeature)
        val btnAddSubfeature = dialog.findViewById<View>(R.id.btn_add_subfeature)

        var currentPriority = existingIdea?.priority ?: 0
        val tempSubfeatures = existingIdea?.subFeatures?.toMutableList() ?: mutableListOf()

        fun refreshSubFeatures() {
            containerSubfeatures.removeAllViews()
            tempSubfeatures.forEach { sub ->
                val layout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, 8, 0, 8)
                }
                val header = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER_VERTICAL
                }
                val ctView = CheckedTextView(this).apply {
                    text = sub.name; setTextColor(Color.WHITE); isChecked = sub.isCompleted; setCheckMarkTintList(ColorStateList.valueOf(Color.WHITE))
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    if (sub.isCompleted) { paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG; alpha = 0.5f }
                    setOnClickListener { sub.isCompleted = !sub.isCompleted; refreshSubFeatures() }
                    setOnLongClickListener { tempSubfeatures.remove(sub); refreshSubFeatures(); true }
                }
                val btnEdit = ImageButton(this).apply {
                    setImageResource(R.drawable.icons8_edit_pencil_100); background = ContextCompat.getDrawable(this@ProjectActivity, android.R.color.transparent)
                    imageTintList = ColorStateList.valueOf(Color.GRAY); layoutParams = LinearLayout.LayoutParams(24.dpToPx(), 24.dpToPx()).apply { marginEnd = 8.dpToPx() }
                    scaleType = ImageView.ScaleType.FIT_CENTER; setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
                }
                header.addView(ctView); header.addView(btnEdit); layout.addView(header)
                val tvNote = TextView(this).apply {
                    text = sub.details; setTextColor(Color.GRAY); textSize = 12f; visibility = if (sub.details.isNotEmpty()) View.VISIBLE else View.GONE; setPadding(12.dpToPx(), 0, 0, 8.dpToPx())
                }
                layout.addView(tvNote)
                btnEdit.setOnClickListener { showEditSubFeatureDialog(sub, showNameField = true) { refreshSubFeatures() } }
                containerSubfeatures.addView(layout)
            }
        }

        btnAddSubfeature.setOnClickListener {
            val name = etNewSubfeature.text.toString().trim()
            if (name.isNotEmpty()) { tempSubfeatures.add(ProjectFeature(name = name)); etNewSubfeature.text.clear(); refreshSubFeatures() }
        }

        fun updatePriorityUI() {
            val (text, color) = when(currentPriority) {
                2 -> "HIGH" to Color.RED
                1 -> "MED" to Color.parseColor("#FFB800")
                else -> "LOW" to Color.parseColor("#2EC4B6")
            }
            btnPriority.text = text; btnPriority.backgroundTintList = ColorStateList.valueOf(color)
        }
        updatePriorityUI()

        existingIdea?.let {
            titleInput.setText(it.title); contentInput.setText(it.content); btnSave.text = "UPDATE"
            btnConvertIcon.visibility = View.VISIBLE; tvCharCount.text = "${it.content.length} characters"
            etInspirationUrl.setText(it.inspirationUrl)
            refreshSubFeatures()
        }
        
        validateInputs()

        contentInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvCharCount.text = "${s?.length ?: 0} characters"
                if (DataManager.projectAutoSaveIdeas && existingIdea != null) { existingIdea.content = s.toString(); DataManager.saveData(this@ProjectActivity) }
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

        btnPriority.setOnClickListener { currentPriority = (currentPriority + 1) % 3; updatePriorityUI() }

        btnSave.setOnClickListener {
            val title = titleInput.text.toString()
            if (title.isNotEmpty()) {
                val idea = existingIdea ?: Note(title = title, content = contentInput.text.toString(), category = "ProjectIdea")
                idea.title = title
                idea.content = contentInput.text.toString()
                idea.priority = currentPriority
                idea.vibeColor = selectedVibeColor
                idea.inspirationUrl = etInspirationUrl.text.toString()
                idea.subFeatures.clear()
                idea.subFeatures.addAll(tempSubfeatures)
                idea.journalEntries.clear()
                idea.journalEntries.addAll(tempJournal)
                
                if (existingIdea == null) allNotes.add(0, idea)
                DataManager.saveData(this); updateDisplayList(); dialog.dismiss()
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        showDialogSafe(dialog)
    }
}
