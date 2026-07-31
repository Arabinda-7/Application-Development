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
import java.text.SimpleDateFormat
import java.util.*

class AddIdeaActivity : BaseActivity() {

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

        ideaId = intent.getLongExtra("IDEA_ID", -1)
        if (ideaId != -1L) {
            existingIdea = synchronized(DataManager.projects) {
                DataManager.projects.find { it.timestamp == ideaId }
            }
        }

        DataManager.currentEditingIdeaSubFeatures.clear()
        DataManager.currentEditingIdeaSubFeatures.addAll(existingIdea?.subFeatures ?: mutableListOf())

        initViews()
        setupLogic()
        setupKeyboardHandling(findViewById(R.id.add_idea_root), findViewById(R.id.add_idea_content_container))
    }

    override fun onResume() {
        super.onResume()
        refreshSubFeatures()
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
                existingIdea?.let { 
                    DataManager.projects.remove(it)
                    DataManager.saveData(this)
                    Toast.makeText(this, "Idea deleted", Toast.LENGTH_SHORT).show()
                    finish()
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
                        DataManager.saveData(this@AddIdeaActivity)
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
        goalsList.removeAllViews()
        tempGoals.sortedByDescending { it.timestamp }.forEach { entry ->
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 4.dpToPx(), 0, 4.dpToPx())
                gravity = android.view.Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            val tvText = TextView(this).apply {
                text = entry.text
                setTextColor(Color.WHITE)
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val btnEdit = ImageView(this).apply {
                setImageResource(R.drawable.icons8_edit_pencil_100)
                val iconSize = 20.dpToPx()
                layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
                setPadding(2.dpToPx(), 2.dpToPx(), 2.dpToPx(), 2.dpToPx())
                imageTintList = ColorStateList.valueOf(Color.GRAY)
                setOnClickListener { showEditGoalDialog(entry) }
            }

            val btnDel = ImageView(this).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                val iconSize = 20.dpToPx()
                layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply { marginStart = 8.dpToPx() }
                imageTintList = ColorStateList.valueOf(Color.parseColor("#80FFFFFF"))
                setOnClickListener {
                    android.app.AlertDialog.Builder(this@AddIdeaActivity)
                        .setTitle("Delete Goal")
                        .setMessage("Are you sure you want to remove this goal?")
                        .setPositiveButton("DELETE") { _, _ ->
                            tempGoals.remove(entry)
                            refreshGoalsUI()
                        }
                        .setNegativeButton("CANCEL", null)
                        .show()
                }
            }

            layout.addView(tvText)
            layout.addView(btnEdit)
            layout.addView(btnDel)
            goalsList.addView(layout)
        }
    }

    private fun showEditGoalDialog(goal: JournalEntry) {
        val et = EditText(this).apply {
            setText(goal.text)
            setPadding(24.dpToPx(), 16.dpToPx(), 24.dpToPx(), 16.dpToPx())
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("Edit Goal")
            .setView(et)
            .setPositiveButton("UPDATE") { _, _ ->
                val newText = et.text.toString().trim()
                if (newText.isNotEmpty()) {
                    goal.text = newText
                    refreshGoalsUI()
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
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
                val isSelected = currentTagFilter == cat
                setTextColor(if (isSelected) Color.WHITE else Color.GRAY)
                background = ContextCompat.getDrawable(this@AddIdeaActivity, R.drawable.priority_chip_bg)
                backgroundTintList = ColorStateList.valueOf(if (isSelected) Color.parseColor("#1A73E8") else Color.parseColor("#11FFFFFF"))

                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    marginEnd = 8.dpToPx()
                }

                setOnClickListener {
                    currentTagFilter = cat
                    refreshSubFeatures()
                }
            }
            filterContainer.addView(chip)
        }
        filterBar.addView(filterContainer)
        containerSubfeatures.addView(filterBar)

        val allSubs = DataManager.currentEditingIdeaSubFeatures
        val filteredSubs = if (currentTagFilter == "ALL") allSubs
        else allSubs.filter { it.tag.uppercase() == currentTagFilter || (currentTagFilter == "OTHER" && it.tag.isEmpty()) }

        val activeSubs = filteredSubs.filter { !it.isCompleted }.sortedBy { it.position }
        val completedSubs = filteredSubs.filter { it.isCompleted }.sortedByDescending { it.position }

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
            setPadding(0, 2.dpToPx(), 0, 2.dpToPx())
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val tvName = TextView(this).apply {
            text = "${sub.position}. ${sub.name}"
            setTextColor(Color.WHITE)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            if (sub.isCompleted) {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                alpha = 0.5f
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
                background = ContextCompat.getDrawable(this@AddIdeaActivity, R.drawable.priority_chip_bg)
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
            background = ContextCompat.getDrawable(this@AddIdeaActivity, R.drawable.priority_chip_bg)
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
                setPadding(4.dpToPx(), 0, 8.dpToPx(), 0)
            } ?: run {
                visibility = View.GONE
            }
        }

        val tvNote = TextView(this).apply {
            text = sub.details
            setTextColor(Color.GRAY)
            textSize = 12f
            setPadding(32.dpToPx(), 4.dpToPx(), 32.dpToPx(), 8.dpToPx())
            visibility = View.GONE
        }

        val btnEdit = ImageView(this).apply {
            setImageResource(R.drawable.icons8_edit_pencil_100)
            imageTintList = ColorStateList.valueOf(Color.GRAY)
            setPadding(2.dpToPx(), 2.dpToPx(), 2.dpToPx(), 2.dpToPx())
            val s = (24 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(s, s)
            setOnClickListener {
                val intent = Intent(this@AddIdeaActivity, AddSubFeatureActivity::class.java).apply {
                    putExtra("PROJECT_ID", ideaId)
                    putExtra("SUB_FEATURE_ID", sub.id)
                    putExtra("IS_IDEA", true)
                }
                startActivity(intent)
            }
        }

        header.addView(tvName)
        header.addView(containerMeta)
        header.addView(tvDate)
        header.addView(btnEdit)
        layout.addView(header)
        layout.addView(tvNote)
        
        layout.setOnClickListener {
            if (sub.details.isNotEmpty()) {
                tvNote.visibility = if (tvNote.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
        }

        layout.setOnLongClickListener { view ->
            showSubFeatureMenu(view, sub)
            true
        }

        return layout
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

            if (existingIdea == null) DataManager.projects.add(0, idea)
            DataManager.saveData(this)
            DataManager.currentEditingIdeaSubFeatures.clear()
            Toast.makeText(this, "Converted to Project Roadmap!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveIdea() {
        val title = titleInput.text.toString().trim()
        if (title.isNotEmpty()) {
            val idea = existingIdea ?: Note(title = title, content = contentInput.text.toString(), category = "ProjectIdea")
            idea.title = title
            idea.content = contentInput.text.toString()
            idea.priority = currentPriority
            idea.subFeatures.clear()
            idea.subFeatures.addAll(DataManager.currentEditingIdeaSubFeatures)
            idea.ideaGoals.clear()
            idea.ideaGoals.addAll(tempGoals)

            if (existingIdea == null) DataManager.projects.add(0, idea)
            DataManager.saveData(this)
            DataManager.currentEditingIdeaSubFeatures.clear()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DataManager.currentEditingIdeaSubFeatures.clear()
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
