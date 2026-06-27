package com.example.allinone

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import java.text.SimpleDateFormat
import java.util.*

class ProjectActivity : AppCompatActivity() {

    private val allNotes = DataManager.notes
    private lateinit var projectAdapter: ProjectNoteAdapter
    private var displayNotes = mutableListOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)

        val projectList = findViewById<RecyclerView>(R.id.project_notes_list)
        projectList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        val dateTextView = findViewById<TextView>(R.id.tv_date)
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        dateTextView.text = sdf.format(DataManager.getTrackingCalendar().time)

        updateDisplayList()
        projectAdapter = ProjectNoteAdapter(displayNotes) {
            DataManager.saveData(this)
            updateDisplayList()
        }
        projectList.adapter = projectAdapter

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        val btnCreate = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btn_add_project_note)
        if (DataManager.projectAddThemeColor != -1) {
            btnCreate.backgroundTintList = android.content.res.ColorStateList.valueOf(DataManager.projectAddThemeColor)
        }
        btnCreate.setOnClickListener { showAddProjectNoteDialog() }
        findViewById<View>(R.id.btn_project_settings).setOnClickListener { showProjectSettingsDialog() }
    }

    private fun updateDisplayList() {
        displayNotes.clear()
        val filtered = allNotes.filter { it.category == "Project" }

        val visibleNotes = if (DataManager.projectAutoArchive) {
            filtered.filter { it.status != "Completed" }
        } else {
            filtered
        }

        displayNotes.addAll(visibleNotes.sortedWith(compareByDescending<Note> { it.isPinned }
            .thenBy { it.status == "Completed" } // Completed at bottom
            .thenByDescending { it.timestamp }))

        if (::projectAdapter.isInitialized) {
            projectAdapter.updateNotes(displayNotes)
        }
    }

    fun showAddProjectNoteDialog() {
        setupProjectDialog(null)
    }

    fun showEditProjectNoteDialog(note: Note) {
        setupProjectDialog(note)
    }

    private fun setupProjectDialog(existingNote: Note? = null) {
        val dialog = Dialog(this, R.style.FullScreenDialog)
        dialog.setContentView(R.layout.dialog_add_project_note)

        val titleInput = dialog.findViewById<EditText>(R.id.note_title_input)
        val contentInput = dialog.findViewById<EditText>(R.id.note_content_input)

        val rgStatus = dialog.findViewById<RadioGroup>(R.id.rg_status)
        val rgPriority = dialog.findViewById<RadioGroup>(R.id.rg_priority)

        val seekProgress = dialog.findViewById<SeekBar>(R.id.seek_progress)
        val tvProgressValue = dialog.findViewById<TextView>(R.id.tv_progress_value)
        val btnPin = dialog.findViewById<ImageView>(R.id.btn_pin)
        val colorPreview = dialog.findViewById<View>(R.id.note_color_preview)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_note)
        if (DataManager.projectAddThemeColor != -1) {
            btnSave.setTextColor(DataManager.projectAddThemeColor)
        }
        val btnClose = dialog.findViewById<View>(R.id.btn_close_note)

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
                    setCheckMarkTintList(android.content.res.ColorStateList.valueOf(Color.WHITE))
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
                    setImageResource(android.R.drawable.ic_menu_edit)
                    background = ContextCompat.getDrawable(this@ProjectActivity, android.R.color.transparent)
                    imageTintList = android.content.res.ColorStateList.valueOf(Color.GRAY)
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
        colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)

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
            android.app.DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d)
                selectedDeadline = cal.timeInMillis
                updateDeadlineUI()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
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
            colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)
        }

        btnClose.setOnClickListener { dialog.dismiss() }

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
        dialog.show()
    }

    private fun showEditSubFeatureDialog(sub: ProjectFeature, onSaved: () -> Unit) {
        val dialog = Dialog(this, R.style.SeamlessDialog)
        dialog.setContentView(R.layout.dialog_edit_subfeature)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val btnClose = dialog.findViewById<View>(R.id.btn_close_subfeature)
        val etSerial = dialog.findViewById<EditText>(R.id.et_serial_input)
        val etDetails = dialog.findViewById<EditText>(R.id.et_details_input)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_subfeature)

        btnClose.setOnClickListener { dialog.dismiss() }

        etSerial.setText(sub.position.toString())
        etDetails.setText(sub.details)
        etDetails.setSelection(etDetails.text.length)

        btnSave.setOnClickListener {
            val newPos = etSerial.text.toString().toIntOrNull()
            if (newPos != null) {
                sub.position = newPos
            }
            sub.details = etDetails.text.toString().trim()
            onSaved()
            dialog.dismiss()
        }
        dialog.show()
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
        dialog.show()
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
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    fun showProjectDetailsDialog(note: Note) {
        val dialog = Dialog(this, R.style.FullScreenDialog)
        dialog.setContentView(R.layout.dialog_project_details)

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
                showEditProjectNoteDialog(note)
            }

            btnHistory.setOnClickListener {
                popupWindow.dismiss()
                showProjectHistoryDialog(note)
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
            "Deadline: " + SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it))
        } ?: "No Deadline Set"

        val sdfMeta = SimpleDateFormat("MMM dd", Locale.getDefault())
        val createdStr = sdfMeta.format(Date(note.timestamp))
        val lastUpdate = note.changeHistory.maxByOrNull { it.timestamp }?.timestamp ?: note.timestamp
        val updatedStr = sdfMeta.format(Date(lastUpdate))
        tvTimestamps.text = "Created: $createdStr | Updated: $updatedStr"

        note.subFeatures.sortedBy { it.position }.forEach { sub ->
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 8, 0, 8)
            }

            val header = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val tvSerial = TextView(this).apply {
                text = "${sub.position}."
                setTextColor(Color.GRAY)
                textSize = 14f
                setPadding(8.dpToPx(), 0, 8.dpToPx(), 0)
            }

            val ctView = CheckedTextView(this).apply {
                text = sub.name
                setTextColor(Color.WHITE)
                isChecked = sub.isCompleted
                setCheckMarkTintList(android.content.res.ColorStateList.valueOf(Color.WHITE))
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
                    val progress = if (note.subFeatures.isNotEmpty()) (note.subFeatures.count { it.isCompleted } * 100) / note.subFeatures.size else 0

                    note.progress = progress
                    if (progress == 100) note.status = "Completed"
                    addHistoryLog(note, "Task Toggled", "${if (sub.isCompleted) "Completed" else "Reopened"}: ${sub.name}")
                    DataManager.saveData(this@ProjectActivity)
                    updateDisplayList()

                    // SMARTER REFRESH: Only close and reopen if status changed to Completed
                    if (note.status == "Completed") {
                        dialog.dismiss()
                    } else {
                        // Manual UI update for smoothness
                        if (sub.isCompleted) {
                            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                            alpha = 0.5f
                        } else {
                            paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                            alpha = 1.0f
                        }
                    }
                }

                setOnLongClickListener {
                    AlertDialog.Builder(this@ProjectActivity)
                        .setTitle("Remove Sub-feature")
                        .setMessage("Remove '${sub.name}' from roadmap?")
                        .setPositiveButton("Remove") { _, _ ->
                            note.subFeatures.remove(sub)
                            DataManager.saveData(this@ProjectActivity)
                            updateDisplayList()
                            showProjectDetailsDialog(note)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                    true
                }
            }

            header.addView(tvSerial)
            header.addView(ctView)
            layout.addView(header)

            val tvNote = TextView(this).apply {
                text = sub.details
                setTextColor(Color.GRAY)
                textSize = 12f
                setPadding(12.dpToPx(), 0, 0, 8.dpToPx())
                visibility = View.GONE // Hidden by default
            }
            layout.addView(tvNote)

            ctView.setOnClickListener {
                if (tvNote.visibility == View.VISIBLE) {
                    tvNote.visibility = View.GONE
                } else if (sub.details.isNotEmpty()) {
                    tvNote.visibility = View.VISIBLE
                }
            }

            containerSubfeatures.addView(layout)
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
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

        swArchive.isChecked = DataManager.projectAutoArchive
        swSync.isChecked = DataManager.projectSynergySync
        swAlerts.isChecked = DataManager.projectDeadlineAlerts
        swAnalytics.isChecked = DataManager.projectAnalyticsEnabled

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

        dialog.findViewById<View>(R.id.btn_close_settings).setOnClickListener {
            DataManager.saveData(this)
            updateDisplayList()
            dialog.dismiss()
        }
        dialog.show()
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
        ivPin.imageTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)

        btnEdit.setOnClickListener {
            popupWindow.dismiss()
            showEditProjectNoteDialog(note)
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
