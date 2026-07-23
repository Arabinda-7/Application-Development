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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*

class AddProjectActivity : BaseActivity() {

    companion object {
        var currentEditingSubFeatures: MutableList<ProjectFeature> = mutableListOf()
    }

    private var projectIndex: Int = -1
    private var existingNote: Note? = null
    
    private lateinit var titleInput: EditText
    private lateinit var tvTitleHint: TextView
    private lateinit var contentInput: EditText
    private lateinit var rgStatus: RadioGroup
    private lateinit var rgPriority: RadioGroup
    private lateinit var seekProgress: SeekBar
    private lateinit var tvProgressValue: TextView
    private lateinit var btnPin: ImageView
    private lateinit var colorPreview: View
    private lateinit var btnSave: TextView
    private lateinit var tvDeadlineDisplay: TextView
    private lateinit var containerSubfeatures: LinearLayout
    private lateinit var etNewSubfeature: EditText
    private lateinit var containerTemplates: LinearLayout
    private lateinit var headerBgAccent: View

    private lateinit var layoutSubfeaturesHeaderToggle: View
    private lateinit var ivSubfeaturesMainChevron: ImageView
    private lateinit var layoutSubfeaturesFullContainer: View
    private var isSubfeaturesExpanded = true

    private lateinit var containerDescriptionHeader: View
    private lateinit var ivDescriptionChevron: ImageView
    private lateinit var containerGoals: View
    private lateinit var goalsList: LinearLayout
    private lateinit var etGoalInput: EditText
    private lateinit var btnAddGoal: View
    private lateinit var btnToggleGoals: TextView
    
    private var isDescriptionExpanded = true
    private var isGoalsExpanded = true
    private val tempGoals = mutableListOf<JournalEntry>()

    private lateinit var layoutAddSelectors: View
    private lateinit var layoutEditCompact: View
    private lateinit var layoutEditCompactRow2: View
    private lateinit var tvEditStatusLabel: TextView
    private lateinit var tvEditPriorityLabel: TextView
    private lateinit var tvEditDeadlineLabel: TextView
    private lateinit var tvEditColorLabel: View
    private lateinit var btnCycleStatus: View
    private lateinit var btnCyclePriority: View
    private lateinit var btnEditColor: View
    private lateinit var btnEditDeadline: View

    private var currentStatusIdx = 0 // 0: TODO, 1: DOING, 2: DONE, 3: HOLD
    private var currentPriorityIdx = 1 // 0: LOW, 1: MED, 2: HIGH

    private var isPinned = false
    private var selectedColor = -1
    private var selectedDeadline: Long? = null
    private val tempSubFeatures = mutableListOf<ProjectFeature>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)

        projectIndex = intent.getIntExtra("PROJECT_INDEX", -1)
        if (projectIndex != -1 && projectIndex < DataManager.projects.size) {
            existingNote = DataManager.projects[projectIndex]
        }

        tempSubFeatures.clear()
        tempSubFeatures.addAll(existingNote?.subFeatures ?: mutableListOf())
        currentEditingSubFeatures = tempSubFeatures

        tempGoals.clear()
        tempGoals.addAll(existingNote?.ideaGoals ?: mutableListOf())

        initViews()
        setupLogic()
        setupKeyboardHandling(findViewById(R.id.add_project_root), findViewById(R.id.add_project_content_container))
    }

    private fun initViews() {
        titleInput = findViewById(R.id.note_title_input)
        tvTitleHint = findViewById(R.id.tv_title_hint_project)
        contentInput = findViewById(R.id.note_content_input)
        rgStatus = findViewById(R.id.rg_status)
        rgPriority = findViewById(R.id.rg_priority)
        seekProgress = findViewById(R.id.seek_progress)
        tvProgressValue = findViewById(R.id.tv_progress_value)
        btnPin = findViewById(R.id.btn_pin)
        colorPreview = findViewById(R.id.note_color_preview)
        btnSave = findViewById(R.id.btn_save_note)
        tvDeadlineDisplay = findViewById(R.id.tv_deadline_display)
        containerSubfeatures = findViewById(R.id.container_subfeatures)
        etNewSubfeature = findViewById(R.id.et_new_subfeature)
        containerTemplates = findViewById(R.id.container_templates)
        
        layoutSubfeaturesHeaderToggle = findViewById(R.id.layout_subfeatures_header_toggle)
        ivSubfeaturesMainChevron = findViewById(R.id.iv_subfeatures_main_chevron)
        layoutSubfeaturesFullContainer = findViewById(R.id.layout_subfeatures_full_container)

        containerDescriptionHeader = findViewById(R.id.container_description_header)
        ivDescriptionChevron = findViewById(R.id.iv_description_chevron)
        containerGoals = findViewById(R.id.container_goals)
        goalsList = findViewById(R.id.goals_list)
        etGoalInput = findViewById(R.id.et_goal_input)
        btnAddGoal = findViewById(R.id.btn_add_goal)
        btnToggleGoals = findViewById(R.id.btn_toggle_goals)

        layoutAddSelectors = findViewById(R.id.layout_add_selectors)
        layoutEditCompact = findViewById(R.id.layout_edit_compact)
        layoutEditCompactRow2 = findViewById(R.id.layout_edit_compact_row2)
        tvEditStatusLabel = findViewById(R.id.tv_edit_status_label)
        tvEditPriorityLabel = findViewById(R.id.tv_edit_priority_label)
        tvEditDeadlineLabel = findViewById(R.id.tv_edit_deadline_label)
        tvEditColorLabel = findViewById(R.id.tv_edit_color_label)
        btnCycleStatus = findViewById(R.id.btn_cycle_status)
        btnCyclePriority = findViewById(R.id.btn_cycle_priority)
        btnEditColor = findViewById(R.id.btn_edit_color)
        btnEditDeadline = findViewById(R.id.btn_edit_deadline)
        headerBgAccent = findViewById(R.id.header_bg_accent)

        findViewById<View>(R.id.btn_close_note).setOnClickListener { finish() }
    }

    private fun setupLogic() {
        isPinned = existingNote?.isPinned ?: false
        selectedColor = existingNote?.color?.takeIf { it != -1 } ?: ContextCompat.getColor(this, R.color.card_blue)
        selectedDeadline = existingNote?.deadline

        if (existingNote != null) {
            titleInput.setText(existingNote?.title)
            contentInput.setText(existingNote?.content)
            seekProgress.progress = existingNote?.progress ?: 0
            tvProgressValue.text = "${existingNote?.progress}%"
            btnPin.setImageResource(if (isPinned) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
            btnSave.text = "UPDATE"
            
            when (existingNote?.priority ?: 1) {
                0 -> { rgPriority.check(R.id.rb_priority_low); currentPriorityIdx = 0 }
                1 -> { rgPriority.check(R.id.rb_priority_med); currentPriorityIdx = 1 }
                2 -> { rgPriority.check(R.id.rb_priority_high); currentPriorityIdx = 2 }
            }
            when (existingNote?.status ?: "Not Started") {
                "Not Started", "TODO" -> { rgStatus.check(R.id.rb_status_todo); currentStatusIdx = 0 }
                "In Progress", "DOING" -> { rgStatus.check(R.id.rb_status_progress); currentStatusIdx = 1 }
                "Completed", "DONE" -> { rgStatus.check(R.id.rb_status_completed); currentStatusIdx = 2 }
                "On Hold", "HOLD" -> { rgStatus.check(R.id.rb_status_hold); currentStatusIdx = 3 }
            }

            layoutAddSelectors.visibility = View.GONE
            layoutEditCompact.visibility = View.VISIBLE
            layoutEditCompactRow2.visibility = View.VISIBLE
            updateCompactLabels()

            // Auto-expand if sub-features exist
            if (tempSubFeatures.isNotEmpty()) {
                isSubfeaturesExpanded = true
                layoutSubfeaturesFullContainer.visibility = View.VISIBLE
                ivSubfeaturesMainChevron.setImageResource(android.R.drawable.arrow_up_float)
            } else {
                isSubfeaturesExpanded = false
                layoutSubfeaturesFullContainer.visibility = View.GONE
                ivSubfeaturesMainChevron.setImageResource(android.R.drawable.arrow_down_float)
            }
        } else {
            layoutAddSelectors.visibility = View.VISIBLE
            layoutEditCompact.visibility = View.GONE
            layoutEditCompactRow2.visibility = View.GONE
            
            isSubfeaturesExpanded = true // Expanded by default for new projects
            layoutSubfeaturesFullContainer.visibility = View.VISIBLE
        }

        colorPreview.backgroundTintList = ColorStateList.valueOf(selectedColor)
        updateThemeVisuals()
        updateDeadlineUI()
        refreshSubFeatures()
        refreshGoalsUI()

        // Section Toggles
        layoutSubfeaturesHeaderToggle.setOnClickListener {
            isSubfeaturesExpanded = !isSubfeaturesExpanded
            layoutSubfeaturesFullContainer.visibility = if (isSubfeaturesExpanded) View.VISIBLE else View.GONE
            ivSubfeaturesMainChevron.setImageResource(if (isSubfeaturesExpanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float)
        }

        containerDescriptionHeader.setOnClickListener {
            isDescriptionExpanded = !isDescriptionExpanded
            contentInput.visibility = if (isDescriptionExpanded) View.VISIBLE else View.GONE
            ivDescriptionChevron.setImageResource(if (isDescriptionExpanded) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float)
        }

        btnToggleGoals.setOnClickListener {
            isGoalsExpanded = !isGoalsExpanded
            containerGoals.visibility = if (isGoalsExpanded) View.VISIBLE else View.GONE
            btnToggleGoals.text = if (isGoalsExpanded) "PROJECT GOALS ▲" else "PROJECT GOALS ▼"
        }

        btnAddGoal.setOnClickListener {
            val goalText = etGoalInput.text.toString().trim()
            if (goalText.isNotEmpty()) {
                tempGoals.add(JournalEntry(goalText))
                etGoalInput.text.clear()
                refreshGoalsUI()
            }
        }
        
        // Templates
        if (existingNote == null) {
            DataManager.projectTemplates.forEach { (name, steps) ->
                val templateBtn = TextView(this).apply {
                    text = name; setTextColor(Color.WHITE); textSize = 12f
                    setPadding(24.dpToPx(), 12.dpToPx(), 24.dpToPx(), 12.dpToPx())
                    background = ContextCompat.getDrawable(this@AddProjectActivity, R.drawable.priority_chip_bg)
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = 12.dpToPx() }
                    setOnClickListener {
                        tempSubFeatures.clear()
                        steps.forEachIndexed { i, step -> tempSubFeatures.add(ProjectFeature(step, position = i + 1)) }
                        refreshSubFeatures()
                        updateProjectProgress()
                    }
                }
                containerTemplates.addView(templateBtn)
            }
        } else {
            findViewById<View>(R.id.container_templates_header).visibility = View.GONE
            findViewById<View>(R.id.scroll_templates).visibility = View.GONE
        }

        // Listeners
        btnCycleStatus.setOnClickListener {
            currentStatusIdx = (currentStatusIdx + 1) % 4
            updateCompactLabels()
        }
        btnCyclePriority.setOnClickListener {
            currentPriorityIdx = (currentPriorityIdx + 1) % 3
            updateCompactLabels()
        }
        btnEditColor.setOnClickListener {
            val colors = listOf(0xFFFF7A59, 0xFFFFB800, 0xFF2EC4B6, 0xFF3A86F0, 0xFF1A73E8, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF4CAF50)
            var currentIdx = colors.map { it.toInt() }.indexOf(selectedColor)
            if (currentIdx == -1) currentIdx = 0
            val nextIdx = (currentIdx + 1) % colors.size
            selectedColor = colors[nextIdx].toInt()
            updateThemeVisuals()
            validateInputs()
        }
        btnEditDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()
            selectedDeadline?.let { calendar.timeInMillis = it }
            DatePickerDialog(this, { _, y, m, d ->
                val newCal = Calendar.getInstance()
                newCal.set(y, m, d)
                selectedDeadline = newCal.timeInMillis
                updateDeadlineUI()
                updateCompactLabels()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        seekProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { tvProgressValue.text = "$progress%" }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<View>(R.id.btn_set_deadline).setOnClickListener {
            val cal = Calendar.getInstance(); selectedDeadline?.let { cal.timeInMillis = it }
            DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d); selectedDeadline = cal.timeInMillis; updateDeadlineUI()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        findViewById<View>(R.id.btn_add_subfeature).setOnClickListener {
            val name = etNewSubfeature.text.toString().trim()
            val baseName = name.ifEmpty { "New Feature" }
            
            val finalName = DataManager.getUniqueFeatureName(baseName, tempSubFeatures)

            val newFeature = ProjectFeature(finalName, position = if (tempSubFeatures.isEmpty()) 1 else tempSubFeatures.maxOf { it.position } + 1)
            tempSubFeatures.add(newFeature)
            etNewSubfeature.text.clear()
            
            updateProjectProgress()
            refreshSubFeatures()

            // Open full-screen editor
            val intent = Intent(this, AddSubFeatureActivity::class.java).apply {
                putExtra("PROJECT_INDEX", projectIndex)
                putExtra("SUB_FEATURE_ID", newFeature.id)
            }
            startActivity(intent)
        }

        btnPin.setOnClickListener { isPinned = !isPinned; btnPin.setImageResource(if (isPinned) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off) }
        colorPreview.setOnClickListener {
            val colors = listOf(ContextCompat.getColor(this, R.color.card_blue), ContextCompat.getColor(this, R.color.card_orange), ContextCompat.getColor(this, R.color.card_green), Color.MAGENTA, Color.RED, Color.CYAN)
            selectedColor = colors[(colors.indexOf(selectedColor) + 1) % colors.size]
            updateThemeVisuals()
            validateInputs()
        }

        titleInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateInputs() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        btnSave.setOnClickListener { saveProject() }
        validateInputs()
    }

    override fun onResume() {
        super.onResume()
        updateProjectProgress()
        refreshSubFeatures()
    }

    private fun validateInputs() {
        val title = titleInput.text.toString().trim()
        val isValid = title.isNotEmpty()
        btnSave.alpha = if (isValid) 1.0f else 0.3f; btnSave.isEnabled = isValid
        tvTitleHint.visibility = if (isValid) View.GONE else View.VISIBLE
        if (!isValid) startPulseAnimation(tvTitleHint)
        if (isValid) btnSave.setTextColor(selectedColor) else btnSave.setTextColor(Color.GRAY)
        tvTitleHint.setTextColor(selectedColor)
    }

    private fun updateThemeVisuals() {
        colorPreview.backgroundTintList = ColorStateList.valueOf(selectedColor)
        headerBgAccent.backgroundTintList = ColorStateList.valueOf(selectedColor)
        tvEditColorLabel.backgroundTintList = ColorStateList.valueOf(selectedColor)
        if (btnSave.isEnabled) {
            btnSave.setTextColor(selectedColor)
        }
        tvTitleHint.setTextColor(selectedColor)
    }

    private fun updateDeadlineUI() {
        tvDeadlineDisplay.text = selectedDeadline?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it)) } ?: "No Deadline Set"
    }

    private var currentTagFilter = "ALL"

    private fun refreshSubFeatures() {
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

        val tags = mutableListOf("ALL")
        tags.addAll(DataManager.projectCustomTags)
        if (!tags.contains("GENERAL")) tags.add("GENERAL")

        tags.forEach { tag ->
            val rb = RadioButton(this).apply {
                id = View.generateViewId()
                text = tag.uppercase()
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 10f)
                setTextColor(Color.WHITE)
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = android.view.Gravity.CENTER
                buttonDrawable = null
                background = ContextCompat.getDrawable(this@AddProjectActivity, R.drawable.filter_chip_bg)

                val height = 32.dpToPx()
                val params = RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height)
                params.setMargins(0, 0, 8.dpToPx(), 0)
                layoutParams = params
                setPadding(20.dpToPx(), 0, 20.dpToPx(), 0)

                isChecked = tag == currentTagFilter

                setOnClickListener {
                    currentTagFilter = tag
                    refreshSubFeatures()
                }
            }
            rgFilters.addView(rb)
        }
        chipContainer.addView(rgFilters)
        containerSubfeatures.addView(chipContainer)

        val filteredSubFeatures = if (currentTagFilter == "ALL") {
            tempSubFeatures
        } else {
            tempSubFeatures.filter { (it.tag.ifEmpty { "GENERAL" }).equals(currentTagFilter, ignoreCase = true) }
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
                    refreshSubFeatures()
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
                    containerSubfeatures.addView(createSubFeatureItem(sub))
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
                    refreshSubFeatures()
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
                    containerSubfeatures.addView(createSubFeatureItem(sub))
                }
            }
        }
    }

    private fun createSubFeatureItem(sub: ProjectFeature): View {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 2.dpToPx(), 0, 2.dpToPx())
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 4.dpToPx()) // Reduced gap further (4dp)
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
            setPadding(32.dpToPx(), 4.dpToPx(), 32.dpToPx(), 8.dpToPx())
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
                    "TASKS" -> Color.parseColor("#1A73E8")
                    "NOTES" -> Color.parseColor("#9E9E9E")
                    "FEATURES" -> Color.parseColor("#673AB7")
                    "BUGS" -> Color.parseColor("#F44336")
                    "RESOURCES" -> Color.parseColor("#009688")
                    "UI" -> Color.parseColor("#E91E63")
                    "LOGIC" -> Color.parseColor("#673AB7")
                    "BUG" -> Color.RED
                    else -> Color.parseColor("#33FFFFFF")
                }
                background = ContextCompat.getDrawable(this@AddProjectActivity, R.drawable.priority_chip_bg)
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
            val isBlocked = tempSubFeatures.find { it.id == sub.blockedByNodeId }?.isCompleted == false
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
                        Toast.makeText(this@AddProjectActivity, "Invalid Link", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        // Quick Edit Icon
        val btnEdit = ImageView(this).apply {
            setImageResource(R.drawable.icons8_edit_pencil_100)
            imageTintList = ColorStateList.valueOf(Color.GRAY)
            setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
            val s = (32 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(s, s)
            setOnClickListener {
                val intent = Intent(this@AddProjectActivity, AddSubFeatureActivity::class.java).apply {
                    putExtra("PROJECT_INDEX", projectIndex)
                    putExtra("SUB_FEATURE_ID", sub.id)
                }
                startActivity(intent)
            }
        }

        val tvUrgency = View(this).apply {
            val size = 6.dpToPx()
            layoutParams = LinearLayout.LayoutParams(size, size).apply { marginEnd = 12.dpToPx() }
            val color = when(sub.priority) {
                2 -> Color.RED; 1 -> Color.parseColor("#FFB800"); else -> Color.TRANSPARENT
            }
            background = ContextCompat.getDrawable(this@AddProjectActivity, R.drawable.circle_selected_bg)
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
                showSubFeatureMenu(it, sub)
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
        
        header.addView(btnEdit)

        layout.addView(header)
        layout.addView(tvNote)
        return layout
    }

    private fun showSubFeatureMenu(anchor: View, sub: ProjectFeature) {
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
            updateProjectProgress()
            refreshSubFeatures()
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_edit).setOnClickListener {
            popupWindow.dismiss()
            val intent = Intent(this, AddSubFeatureActivity::class.java).apply {
                putExtra("PROJECT_INDEX", projectIndex)
                putExtra("SUB_FEATURE_ID", sub.id)
            }
            startActivity(intent)
        }

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            tempSubFeatures.remove(sub)
            updateProjectProgress()
            refreshSubFeatures()
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
                orientation = LinearLayout.VERTICAL
                setPadding(12.dpToPx(), 12.dpToPx(), 12.dpToPx(), 8.dpToPx())
                background = ContextCompat.getDrawable(this@AddProjectActivity, R.drawable.glass_card_bg)
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10FFFFFF"))
                val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 0, 0, 8.dpToPx())
                layoutParams = params
            }

            val contentRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val icon = ImageView(this).apply {
                setImageResource(R.drawable.icons8_done_100)
                imageTintList = ColorStateList.valueOf(Color.parseColor("#80FFFFFF"))
                layoutParams = LinearLayout.LayoutParams(16.dpToPx(), 16.dpToPx())
            }

            val tvGoalContent = TextView(this).apply {
                text = goal.text
                setTextColor(Color.WHITE)
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 12.dpToPx()
                }
            }

            val deleteBtn = ImageView(this).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                imageTintList = ColorStateList.valueOf(Color.parseColor("#40FFFFFF"))
                layoutParams = LinearLayout.LayoutParams(20.dpToPx(), 20.dpToPx())
                setOnClickListener {
                    tempGoals.remove(goal)
                    refreshGoalsUI()
                }
            }

            contentRow.addView(icon)
            contentRow.addView(tvGoalContent)
            contentRow.addView(deleteBtn)
            layout.addView(contentRow)

            val timeStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(goal.timestamp))
            val tvTime = TextView(this).apply {
                text = timeStr
                setTextColor(Color.parseColor("#4DFFFFFF"))
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 9f)
                gravity = android.view.Gravity.END
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = 4.dpToPx()
                }
            }
            layout.addView(tvTime)
            
            goalsList.addView(layout)
        }
    }

    private fun updateProjectProgress() {
        // Weighted Progress Calculation
        val totalWeight = tempSubFeatures.sumOf { it.weight }.coerceAtLeast(1)
        val completedWeight = tempSubFeatures.filter { it.isCompleted }.sumOf { it.weight }
        val progress = (completedWeight * 100) / totalWeight
        
        seekProgress.progress = progress
        tvProgressValue.text = "$progress%"

        // Smart Status Automation
        when (progress) {
            0 -> rgStatus.check(R.id.rb_status_todo)
            100 -> rgStatus.check(R.id.rb_status_completed)
            else -> if (rgStatus.checkedRadioButtonId == R.id.rb_status_todo) rgStatus.check(R.id.rb_status_progress)
        }
    }

    private fun saveProject() {
        val title = titleInput.text.toString().trim()
        if (title.isNotEmpty()) {
            val note = existingNote ?: Note(title = title, content = "")
            note.title = title; note.content = contentInput.text.toString()
            
            if (existingNote == null) {
                note.status = when (rgStatus.checkedRadioButtonId) {
                    R.id.rb_status_progress -> "DOING"; R.id.rb_status_completed -> "DONE"; R.id.rb_status_hold -> "HOLD"; else -> "TODO"
                }
                note.priority = when (rgPriority.checkedRadioButtonId) {
                    R.id.rb_priority_low -> 0; R.id.rb_priority_high -> 2; else -> 1
                }
            } else {
                val statuses = listOf("TODO", "DOING", "DONE", "HOLD")
                note.status = statuses[currentStatusIdx]
                note.priority = currentPriorityIdx
            }
            
            note.progress = seekProgress.progress; note.isPinned = isPinned; note.color = selectedColor; note.category = "Project"; note.deadline = selectedDeadline
            note.subFeatures.clear(); note.subFeatures.addAll(tempSubFeatures)
            note.ideaGoals.clear(); note.ideaGoals.addAll(tempGoals)

            if (existingNote == null) DataManager.projects.add(0, note)
            DataManager.saveData(this); setResult(RESULT_OK)
            currentEditingSubFeatures.clear()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentEditingSubFeatures.clear()
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
    private fun updateCompactLabels() {
        val statuses = listOf("TODO", "DOING", "DONE", "HOLD")
        val priorities = listOf("LOW", "MED", "HIGH")
        
        tvEditStatusLabel.text = statuses[currentStatusIdx]
        
        tvEditPriorityLabel.text = priorities[currentPriorityIdx]
        tvEditPriorityLabel.setTextColor(when(currentPriorityIdx) {
            2 -> Color.RED
            1 -> Color.parseColor("#FFB800")
            else -> Color.parseColor("#2EC4B6")
        })

        updateThemeVisuals()
        tvEditDeadlineLabel.text = selectedDeadline?.let { 
            SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it)) 
        } ?: "No Set"
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
}
