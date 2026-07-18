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

    private var isPinned = false
    private var selectedColor = -1
    private var selectedDeadline: Long? = null
    private val tempSubFeatures = mutableListOf<ProjectFeature>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sticky_header)) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val basePadding = (24 * resources.displayMetrics.density).toInt()
            v.setPadding(basePadding, statusBars.top + basePadding, basePadding, basePadding)
            insets
        }

        projectIndex = intent.getIntExtra("PROJECT_INDEX", -1)
        if (projectIndex != -1 && projectIndex < DataManager.notes.size) {
            existingNote = DataManager.notes[projectIndex]
        }

        tempSubFeatures.addAll(existingNote?.subFeatures ?: mutableListOf())
        currentEditingSubFeatures = tempSubFeatures

        initViews()
        setupLogic()
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
        
        findViewById<View>(R.id.btn_close_note).setOnClickListener { finish() }
    }

    private fun setupLogic() {
        isPinned = existingNote?.isPinned ?: false
        selectedColor = existingNote?.color?.takeIf { it != -1 } ?: ContextCompat.getColor(this, R.color.card_blue)
        selectedDeadline = existingNote?.deadline
        tempSubFeatures.addAll(existingNote?.subFeatures ?: mutableListOf())

        if (existingNote != null) {
            titleInput.setText(existingNote?.title)
            contentInput.setText(existingNote?.content)
            seekProgress.progress = existingNote?.progress ?: 0
            tvProgressValue.text = "${existingNote?.progress}%"
            btnPin.setImageResource(if (isPinned) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
            btnSave.text = "UPDATE"
            
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
        }

        colorPreview.backgroundTintList = ColorStateList.valueOf(selectedColor)
        updateDeadlineUI()
        refreshSubFeatures()
        
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
                    }
                }
                containerTemplates.addView(templateBtn)
            }
        } else {
            findViewById<View>(R.id.container_templates_header).visibility = View.GONE
            findViewById<View>(R.id.scroll_templates).visibility = View.GONE
        }

        // Listeners
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
            val finalName = name.ifEmpty { "New Feature" }
            
            if (tempSubFeatures.any { it.name.equals(finalName, ignoreCase = true) }) {
                Toast.makeText(this, "A feature with this name already exists", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newFeature = ProjectFeature(finalName, position = if (tempSubFeatures.isEmpty()) 1 else tempSubFeatures.maxOf { it.position } + 1)
            tempSubFeatures.add(newFeature)
            etNewSubfeature.text.clear()
            
            // Requirement 1: Immediately open full-screen editor
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
            colorPreview.backgroundTintList = ColorStateList.valueOf(selectedColor); validateInputs()
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

    private fun updateDeadlineUI() {
        tvDeadlineDisplay.text = selectedDeadline?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it)) } ?: "No Deadline Set"
    }

    private var currentTagFilter = "ALL"

    private fun refreshSubFeatures() {
        containerSubfeatures.removeAllViews()

        // Requirement 2: Filter Chips (Match Habit style reference)
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
                    refreshSubFeatures() // Re-enabled filtering
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

        // 2. Completed Section (Collapsible - Requirement 4)
        if (completedFeatures.isNotEmpty()) {
            val completedHeader = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(8.dpToPx(), 24.dpToPx(), 8.dpToPx(), 12.dpToPx())
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

        // Quick Edit Icon (Requirement 3)
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

        val clickTarget = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            gravity = android.view.Gravity.CENTER_VERTICAL
            addView(tvSerial)
            addView(tvName)
            setOnClickListener {
                val intent = Intent(this@AddProjectActivity, AddSubFeatureActivity::class.java).apply {
                    putExtra("PROJECT_INDEX", projectIndex)
                    putExtra("SUB_FEATURE_ID", sub.id)
                }
                startActivity(intent)
            }
            setOnLongClickListener {
                showSubFeatureMenu(it, sub)
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

    private fun updateProjectProgress() {
        val progress = if (tempSubFeatures.isNotEmpty()) (tempSubFeatures.count { it.isCompleted } * 100) / tempSubFeatures.size else 0
        seekProgress.progress = progress
        tvProgressValue.text = "$progress%"
    }

    private fun saveProject() {
        val title = titleInput.text.toString().trim()
        if (title.isNotEmpty()) {
            val note = existingNote ?: Note(title = title, content = "")
            note.title = title; note.content = contentInput.text.toString()
            note.status = when (rgStatus.checkedRadioButtonId) {
                R.id.rb_status_progress -> "In Progress"; R.id.rb_status_completed -> "Completed"; R.id.rb_status_hold -> "On Hold"; else -> "Not Started"
            }
            note.priority = when (rgPriority.checkedRadioButtonId) {
                R.id.rb_priority_low -> 0; R.id.rb_priority_high -> 2; else -> 1
            }
            note.progress = seekProgress.progress; note.isPinned = isPinned; note.color = selectedColor; note.category = "Project"; note.deadline = selectedDeadline
            note.subFeatures.clear(); note.subFeatures.addAll(tempSubFeatures)

            if (existingNote == null) DataManager.notes.add(0, note)
            DataManager.saveData(this); setResult(RESULT_OK); finish()
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
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
