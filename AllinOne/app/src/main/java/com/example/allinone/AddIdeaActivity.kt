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
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*

class AddIdeaActivity : BaseActivity() {

    companion object {
        var currentEditingIdeaSubFeatures: MutableList<ProjectFeature> = mutableListOf()
    }

    private var ideaIndex: Int = -1
    private var existingIdea: Note? = null

    private lateinit var titleInput: EditText
    private lateinit var contentInput: EditText
    private lateinit var btnSave: TextView
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

    private var currentPriority = 0
    private val tempGoals = mutableListOf<JournalEntry>()
    private var currentTagFilter = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_idea)

        ideaIndex = intent.getIntExtra("IDEA_INDEX", -1)
        if (ideaIndex != -1 && ideaIndex < DataManager.notes.size) {
            existingIdea = DataManager.notes[ideaIndex]
        }

        currentEditingIdeaSubFeatures.clear()
        currentEditingIdeaSubFeatures.addAll(existingIdea?.subFeatures ?: mutableListOf())

        initViews()
        setupLogic()
        setupKeyboardHandling(findViewById(R.id.add_idea_root), findViewById(R.id.add_idea_content_container))
    }

    private fun initViews() {
        titleInput = findViewById(R.id.note_title_input)
        contentInput = findViewById(R.id.note_content_input)
        btnSave = findViewById(R.id.btn_save_note)
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
    }

    private fun setupLogic() {
        if (existingIdea != null) {
            titleInput.setText(existingIdea?.title)
            contentInput.setText(existingIdea?.content)
            currentPriority = existingIdea?.priority ?: 0
            tempGoals.addAll(existingIdea?.ideaGoals ?: emptyList())
            btnSave.text = "UPDATE"
            btnConvertIcon.visibility = View.VISIBLE
            tvCharCount.text = "${existingIdea?.content?.length ?: 0} characters"

            // Show creation time
            tvCreatedAt.visibility = View.VISIBLE
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            tvCreatedAt.text = "Created on: ${sdf.format(Date(existingIdea!!.timestamp))}"

            // Auto-collapse if content exists
            if (!existingIdea?.content.isNullOrEmpty()) {
                containerDescription.visibility = View.GONE
                btnToggleDescription.text = "DESCRIPTION ▼"
            }

            // Auto-expand if goals exist
            if (tempGoals.isNotEmpty()) {
                containerGoals.visibility = View.VISIBLE
                btnToggleGoals.text = "IDEA GOALS ▲"
            }

            // Auto-expand if features exist
            if (currentEditingIdeaSubFeatures.isNotEmpty()) {
                layoutFeaturesContainer.visibility = View.VISIBLE
                btnToggleFeatures.text = "FEATURES ▲"
            } else {
                layoutFeaturesContainer.visibility = View.GONE
                btnToggleFeatures.text = "FEATURES ▼"
            }
        }

        btnClose.setOnClickListener { finish() }
        btnSave.setOnClickListener { saveIdea() }

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
            if (name.isNotEmpty()) {
                val nextPos = if (currentEditingIdeaSubFeatures.isEmpty()) 1 else currentEditingIdeaSubFeatures.maxOf { it.position } + 1
                currentEditingIdeaSubFeatures.add(ProjectFeature(name = name, position = nextPos))
                etNewSubfeature.text.clear()
                refreshSubFeatures()
            }
        }

        btnPriority.setOnClickListener {
            currentPriority = (currentPriority + 1) % 3
            updatePriorityUI()
        }

        btnConvert.setOnClickListener { convertToProject() }
        btnConvertIcon.setOnClickListener { convertToProject() }

        titleInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateInputs() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        contentInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvCharCount.text = "${s?.length ?: 0} characters"
                if (DataManager.projectAutoSaveIdeas && existingIdea != null) { existingIdea!!.content = s.toString(); DataManager.saveData(this@AddIdeaActivity) }
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
                orientation = LinearLayout.VERTICAL
                setPadding(0, 8.dpToPx(), 0, 8.dpToPx())
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            val tvText = TextView(this).apply {
                text = entry.text
                setTextColor(Color.WHITE)
                textSize = 14f
            }

            val tvDate = TextView(this).apply {
                val date = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(entry.timestamp))
                text = date
                setTextColor(Color.GRAY)
                textSize = 10f
                gravity = android.view.Gravity.END
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            layout.addView(tvText)
            layout.addView(tvDate)
            goalsList.addView(layout)
        }
    }

    private fun refreshSubFeatures() {
        containerSubfeatures.removeAllViews()
        
        val active = currentEditingIdeaSubFeatures.filter { !it.isCompleted }.sortedBy { it.position }
        val completed = currentEditingIdeaSubFeatures.filter { it.isCompleted }.sortedByDescending { it.position }

        active.forEach { containerSubfeatures.addView(createSubFeatureItem(it)) }
        completed.forEach { containerSubfeatures.addView(createSubFeatureItem(it)) }
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
                    putExtra("PROJECT_INDEX", DataManager.notes.indexOf(existingIdea))
                    putExtra("SUB_FEATURE_ID", sub.id)
                    putExtra("IS_IDEA", true)
                }
                startActivity(intent)
            }
        }

        header.addView(tvName)
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
                putExtra("PROJECT_INDEX", DataManager.notes.indexOf(existingIdea))
                putExtra("SUB_FEATURE_ID", sub.id)
            }
            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            currentEditingIdeaSubFeatures.remove(sub)
            refreshSubFeatures()
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_hide_unhide).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_undo).visibility = View.GONE

        popupWindow.showAsDropDown(anchor, 100, 0)
    }

    private fun updatePriorityUI() {
        val (text, color) = when(currentPriority) {
            2 -> "HIGH" to Color.RED
            1 -> "MED" to Color.parseColor("#FFB800")
            else -> "LOW" to Color.parseColor("#2EC4B6")
        }
        btnPriority.text = text
        btnPriority.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun validateInputs() {
        val title = titleInput.text.toString().trim()
        val isValid = title.isNotEmpty()
        btnSave.alpha = if (isValid) 1.0f else 0.3f
        btnSave.isEnabled = isValid
        val themeColor = if (DataManager.projectAddThemeColor != -1) DataManager.projectAddThemeColor else Color.parseColor("#1A73E8")
        if (isValid) btnSave.setTextColor(themeColor) else btnSave.setTextColor(Color.GRAY)
    }

    private fun convertToProject() {
        val title = titleInput.text.toString().trim()
        if (title.isNotEmpty()) {
            val idea = existingIdea ?: Note(title = title, content = contentInput.text.toString(), category = "Project")
            idea.title = title
            idea.content = contentInput.text.toString()
            idea.category = "Project"
            idea.status = "In Progress"
            idea.priority = currentPriority
            idea.subFeatures.clear()
            idea.subFeatures.addAll(currentEditingIdeaSubFeatures)
            idea.ideaGoals.clear()
            idea.ideaGoals.addAll(tempGoals)

            if (existingIdea == null) DataManager.notes.add(0, idea)
            DataManager.saveData(this)
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
            idea.subFeatures.addAll(currentEditingIdeaSubFeatures)
            idea.ideaGoals.clear()
            idea.ideaGoals.addAll(tempGoals)

            if (existingIdea == null) DataManager.notes.add(0, idea)
            DataManager.saveData(this)
            finish()
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
