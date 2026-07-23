package com.example.allinone

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class NotesActivity : BaseActivity() {

    private val VOICE_CODE = 1001
    private var activeContentInput: EditText? = null
    private val allNotes = DataManager.notes
    private lateinit var noteAdapter: NoteAdapter
    private var currentCategory = DataManager.noteDefaultCategory
    private var displayNotes = mutableListOf<Note>()
    private var isDeleteMode = false
    private lateinit var gestureDetector: android.view.GestureDetector

    private lateinit var navNotes: View
    private lateinit var navQuestions: View
    private lateinit var navDaily: View
    private lateinit var navStories: View
    private lateinit var ivNotesIcon: ImageView
    private lateinit var tvNotesLabel: TextView
    private lateinit var ivQuestionsIcon: ImageView
    private lateinit var tvQuestionsLabel: TextView
    private lateinit var ivDailyIcon: ImageView
    private lateinit var tvDailyLabel: TextView
    private lateinit var ivStoriesIcon: ImageView
    private lateinit var tvStoriesLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        applyAutoCleanup()

        navNotes = findViewById(R.id.nav_notes)
        navQuestions = findViewById(R.id.nav_questions)
        navDaily = findViewById(R.id.nav_daily)
        navStories = findViewById(R.id.nav_stories)
        
        ivNotesIcon = findViewById(R.id.iv_notes_icon)
        tvNotesLabel = findViewById(R.id.tv_notes_label)
        ivQuestionsIcon = findViewById(R.id.iv_questions_icon)
        tvQuestionsLabel = findViewById(R.id.tv_questions_label)
        ivDailyIcon = findViewById(R.id.iv_daily_icon)
        tvDailyLabel = findViewById(R.id.tv_daily_label)
        ivStoriesIcon = findViewById(R.id.iv_stories_icon)
        tvStoriesLabel = findViewById(R.id.tv_stories_label)

        val notesList = findViewById<RecyclerView>(R.id.notes_list)
        notesList.layoutManager = LinearLayoutManager(this)
        
        updateDisplayList()
        noteAdapter = NoteAdapter(displayNotes) { 
            DataManager.saveData(this)
            updateDisplayList()
        }
        notesList.adapter = noteAdapter

        findViewById<View>(R.id.btn_back).setOnClickListener { 
            if (isDeleteMode) toggleDeleteMode(false) else finish() 
        }

        setupBottomNavigation()
        setupGestureDetector()
        setupKeyboardHandling(findViewById(R.id.notes_root_layout), findViewById(R.id.notes_content_container))
        updateDynamicBackground()
        applySectionTheme()

        findViewById<View>(R.id.btn_notes_settings).setOnClickListener {
            if (isDeleteMode) {
                noteAdapter.deleteSelectedNotes(this)
                toggleDeleteMode(false)
            } else {
                showSettingsMenu(it)
            }
        }

        val btnCreate = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btn_create_new_note)
        btnCreate.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java).apply {
                putExtra("CATEGORY", currentCategory)
            }
            startActivity(intent)
        }

        if (intent.getBooleanExtra("QUICK_ADD", false)) {
            val intentAdd = Intent(this, AddNoteActivity::class.java).apply {
                putExtra("CATEGORY", currentCategory)
            }
            startActivity(intentAdd)
        }
    }

    override fun onResume() {
        super.onResume()
        setupBottomNavigation()
        updateDisplayList()
        if (::noteAdapter.isInitialized) {
            noteAdapter.updateNotes(displayNotes)
        }
        updateDynamicBackground()
        applySectionTheme()
    }

    private fun applySectionTheme() {
        val noteColor = if (DataManager.globalNoteColor != -1) DataManager.globalNoteColor else Color.parseColor("#3A86F0")
        val darkenedFabColor = UIUtils.darkenColor(noteColor, 0.5f)
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btn_create_new_note).backgroundTintList = 
            android.content.res.ColorStateList.valueOf(darkenedFabColor)
        
        updateNavUI()
    }

    private fun updateDynamicBackground() {
        val auraView = findViewById<View>(R.id.note_aura_background) ?: return
        val noteColor = if (DataManager.globalNoteColor != -1) DataManager.globalNoteColor else Color.parseColor("#3A86F0")
        
        val gradient = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                adjustAlpha(noteColor, 0.4f),
                Color.BLACK
            )
        )
        auraView.background = gradient
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun toggleDeleteMode(enabled: Boolean) {
        isDeleteMode = enabled
        noteAdapter.setDeleteMode(enabled)
        val btnSettings = findViewById<ImageButton>(R.id.btn_notes_settings)
        btnSettings.setImageResource(if (enabled) android.R.drawable.ic_menu_delete else R.drawable.baseline_tune_24)
        findViewById<View>(R.id.btn_create_new_note).visibility = if (enabled) View.GONE else View.VISIBLE
    }

    private fun showSettingsMenu(anchor: View) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.layout_activity_settings_menu, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        // Repurpose toggle_completed for SHOW/HIDE HIDDEN
        val btnToggleHidden = menuView.findViewById<View>(R.id.menu_toggle_completed)
        val tvToggleHidden = menuView.findViewById<TextView>(R.id.tv_toggle_completed)
        val ivToggleHidden = menuView.findViewById<ImageView>(R.id.iv_toggle_completed)
        
        btnToggleHidden.visibility = View.VISIBLE
        tvToggleHidden.text = if (DataManager.noteShowHidden) "HIDE HIDDEN" else "SHOW HIDDEN"
        ivToggleHidden.setImageResource(if (DataManager.noteShowHidden) android.R.drawable.ic_menu_view else android.R.drawable.ic_partial_secure)

        btnToggleHidden.setOnClickListener {
            DataManager.noteShowHidden = !DataManager.noteShowHidden
            DataManager.saveData(this)
            updateDisplayList()
            popupWindow.dismiss()
        }

        // Repurpose clear_completed for MULTI-DELETE
        val btnMultiDelete = menuView.findViewById<View>(R.id.menu_clear_completed)
        btnMultiDelete.visibility = View.VISIBLE
        
        val tvLabel = if (btnMultiDelete is ViewGroup && btnMultiDelete.childCount > 1) {
            btnMultiDelete.getChildAt(1) as? TextView
        } else null
        
        tvLabel?.text = "SELECT & DELETE"

        btnMultiDelete.setOnClickListener {
            toggleDeleteMode(true)
            popupWindow.dismiss()
        }
        
        menuView.findViewById<View>(R.id.menu_activity_settings).setOnClickListener {
            showNotesSettingsDialog()
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(anchor, -150, 0)
    }

    private fun setupBottomNavigation() {
        val footer = findViewById<LinearLayout>(R.id.bottom_navigation_notes)

        // 1. Remove all to prepare for re-ordering
        footer.removeAllViews()

        // 2. Add back in the order specified by DataManager.noteVisibleSections
        DataManager.noteVisibleSections.forEach { category ->
            val viewToAdd = when (category) {
                "Notes" -> navNotes
                "Questions" -> navQuestions
                "Daily" -> navDaily
                "Stories" -> navStories
                else -> null
            }
            viewToAdd?.let {
                if (it.parent != null) (it.parent as ViewGroup).removeView(it)
                footer.addView(it)
            }
        }

        // 3. Auto-switch to default if current is hidden or if we just returned from settings
        if (!DataManager.noteVisibleSections.contains(currentCategory)) {
            currentCategory = DataManager.noteDefaultCategory
        }

        navNotes.visibility = if (DataManager.noteVisibleSections.contains("Notes")) View.VISIBLE else View.GONE
        navQuestions.visibility = if (DataManager.noteVisibleSections.contains("Questions")) View.VISIBLE else View.GONE
        navDaily.visibility = if (DataManager.noteVisibleSections.contains("Daily")) View.VISIBLE else View.GONE
        navStories.visibility = if (DataManager.noteVisibleSections.contains("Stories")) View.VISIBLE else View.GONE

        if (DataManager.noteVisibleSections.size > 1) {
            footer.visibility = View.VISIBLE
        } else {
            footer.visibility = View.GONE
            val onlyVisible = DataManager.noteVisibleSections.firstOrNull() ?: "Notes"
            if (currentCategory != onlyVisible) switchCategory(onlyVisible)
        }

        if (!DataManager.noteVisibleSections.contains(currentCategory)) {
            val firstVisible = DataManager.noteVisibleSections.firstOrNull() ?: "Notes"
            switchCategory(firstVisible)
        }

        navNotes.setOnClickListener { switchCategory("Notes") }
        navQuestions.setOnClickListener { switchCategory("Questions") }
        navDaily.setOnClickListener { switchCategory("Daily") }
        navStories.setOnClickListener { switchCategory("Stories") }
        
        updateNavUI()
    }

    private fun switchCategory(category: String) {
        if (category == currentCategory && DataManager.noteVisibleSections.size <= 1) return

        val root = findViewById<ViewGroup>(R.id.notes_content_container)
        androidx.transition.TransitionManager.beginDelayedTransition(root, androidx.transition.AutoTransition())
        
        val sections = DataManager.noteVisibleSections.toMutableList()
        
        if (category == currentCategory) {
            // Double Click: Reset to original order (while keeping only visible ones)
            val originalOrder = listOf("Notes", "Questions", "Daily", "Stories")
            val resetOrder = originalOrder.filter { sections.contains(it) }
            
            DataManager.noteVisibleSections.clear()
            DataManager.noteVisibleSections.addAll(resetOrder)
        } else {
            // Reorder: Move current category to the first position
            if (sections.contains(category)) {
                sections.remove(category)
                sections.add(0, category)
                DataManager.noteVisibleSections.clear()
                DataManager.noteVisibleSections.addAll(sections)
            }
        }
        
        currentCategory = category
        setupBottomNavigation() // Redraw footer with new order
        updateDisplayList()
        updateNavUI()
    }

    private fun setupGestureDetector() {
        gestureDetector = android.view.GestureDetector(this, object : SwipeGestureListener() {
            override fun onSwipeLeft() {
                if (DataManager.noteVisibleSections.size > 1) {
                    val currentIndex = DataManager.noteVisibleSections.indexOf(currentCategory)
                    val nextIndex = (currentIndex + 1) % DataManager.noteVisibleSections.size
                    switchCategory(DataManager.noteVisibleSections[nextIndex])
                }
            }

            override fun onSwipeRight() {
                if (DataManager.noteVisibleSections.size > 1) {
                    val currentIndex = DataManager.noteVisibleSections.indexOf(currentCategory)
                    val prevIndex = if (currentIndex <= 0) DataManager.noteVisibleSections.size - 1 else currentIndex - 1
                    switchCategory(DataManager.noteVisibleSections[prevIndex])
                }
            }
        })
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent?): Boolean {
        if (ev != null) gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun updateNavUI() {
        val navs = mapOf(
            "Notes" to Pair(ivNotesIcon, tvNotesLabel),
            "Questions" to Pair(ivQuestionsIcon, tvQuestionsLabel),
            "Daily" to Pair(ivDailyIcon, tvDailyLabel),
            "Stories" to Pair(ivStoriesIcon, tvStoriesLabel)
        )

        val activeColor = ContextCompat.getColor(this, R.color.chip_selected)
        val inactiveColor = ContextCompat.getColor(this, R.color.text_secondary)

        navs.forEach { (cat, views) ->
            val isActive = cat == currentCategory
            val color = if (isActive) activeColor else inactiveColor
            
            views.first.setColorFilter(color)
            views.second.setTextColor(color)
        }
    }

    private fun updateDisplayList() {
        val filtered = allNotes.filter { 
            it.category == currentCategory && (DataManager.noteShowHidden || !it.isHidden) 
        }.sortedByDescending { it.timestamp }
        
        displayNotes.clear()
        displayNotes.addAll(filtered)
        
        if (::noteAdapter.isInitialized) {
            noteAdapter.updateNotes(filtered)
        }
    }

    private fun showAddNoteDialog() {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_add_note)

        val titleInput = dialog.findViewById<EditText>(R.id.note_title_input)
        val contentInput = dialog.findViewById<EditText>(R.id.note_content_input)
        val colorPreview = dialog.findViewById<View>(R.id.note_color_preview)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_note)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_note)
        val btnVoice = dialog.findViewById<View>(R.id.btn_voice_input)
        val btnReminder = dialog.findViewById<View>(R.id.btn_reminder)
        val tvMetadata = dialog.findViewById<TextView>(R.id.tv_note_metadata)
        
        dialog.findViewById<View>(R.id.tv_title_hint_note).visibility = View.GONE

        var selectedColor = ContextCompat.getColor(this, R.color.card_blue)
        colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)
        
        btnSave.alpha = 1.0f
        btnSave.isEnabled = true

        val sdf = SimpleDateFormat("dd MMMM h:mm a", Locale.getDefault())
        val currentDateStr = sdf.format(Date())
        
        val template = DataManager.noteTemplates[currentCategory] ?: ""
        if (template.isNotEmpty()) {
            contentInput.setText(template)
        }
        contentInput.setSelection(contentInput.text.length)
        
        fun updateMetadata() {
            val count = (titleInput.text.length + contentInput.text.length)
            tvMetadata.text = "$currentDateStr | $count characters"
        }
        updateMetadata()

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { 
                updateMetadata()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        titleInput.addTextChangedListener(textWatcher)
        contentInput.addTextChangedListener(textWatcher)

        colorPreview.setOnClickListener {
            val colors = listOf(ContextCompat.getColor(this, R.color.card_blue), ContextCompat.getColor(this, R.color.card_orange), ContextCompat.getColor(this, R.color.card_green), Color.MAGENTA, Color.RED, Color.CYAN)
            selectedColor = colors[(colors.indexOf(selectedColor) + 1) % colors.size]
            colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        
        btnVoice.setOnClickListener {
            activeContentInput = contentInput
            startVoiceInput()
        }
        
        btnReminder.setOnClickListener {
            showReminderPicker(titleInput.text.toString())
        }

        btnSave.setOnClickListener {
            val title = titleInput.text.toString()
            val content = contentInput.text.toString()
            if (title.isNotEmpty()) {
                val newNote = Note(title, content, color = selectedColor, category = currentCategory)
                DataManager.notes.add(0, newNote)
                updateDisplayList()
                noteAdapter.updateNotes(displayNotes)
                DataManager.saveData(this)
                
                if (intent.getBooleanExtra("QUICK_ADD", false)) {
                    finish()
                } else {
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    fun showEditNoteDialog(note: Note) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_add_note)

        val titleInput = dialog.findViewById<EditText>(R.id.note_title_input)
        val contentInput = dialog.findViewById<EditText>(R.id.note_content_input)
        val colorPreview = dialog.findViewById<View>(R.id.note_color_preview)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_note)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_note)
        val btnVoice = dialog.findViewById<View>(R.id.btn_voice_input)
        val btnReminder = dialog.findViewById<View>(R.id.btn_reminder)
        val tvMetadata = dialog.findViewById<TextView>(R.id.tv_note_metadata)
        
        dialog.findViewById<View>(R.id.tv_title_hint_note).visibility = View.GONE

        titleInput.setText(note.title)
        contentInput.setText(note.content)
        var selectedColor = if (note.color != -1) note.color else ContextCompat.getColor(this, R.color.card_blue)
        colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)

        btnSave.alpha = 1.0f
        btnSave.isEnabled = true
        btnSave.text = "Save"
        
        val sdf = SimpleDateFormat("dd MMMM h:mm a", Locale.getDefault())
        val dateStr = sdf.format(Date(note.timestamp))
        
        fun updateMetadata() {
            val count = (titleInput.text.length + contentInput.text.length)
            tvMetadata.text = "$dateStr | $count characters"
        }
        updateMetadata()

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { 
                updateMetadata()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        titleInput.addTextChangedListener(textWatcher)
        contentInput.addTextChangedListener(textWatcher)

        colorPreview.setOnClickListener {
            val colors = listOf(ContextCompat.getColor(this, R.color.card_blue), ContextCompat.getColor(this, R.color.card_orange), ContextCompat.getColor(this, R.color.card_green), Color.MAGENTA, Color.RED, Color.CYAN)
            selectedColor = colors[(colors.indexOf(selectedColor) + 1) % colors.size]
            colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        
        btnVoice.setOnClickListener {
            activeContentInput = contentInput
            startVoiceInput()
        }
        
        btnReminder.setOnClickListener {
            showReminderPicker(titleInput.text.toString())
        }

        btnSave.setOnClickListener {
            val title = titleInput.text.toString()
            val content = contentInput.text.toString()
            if (title.isNotEmpty()) {
                val index = DataManager.notes.indexOf(note)
                if (index != -1) {
                    val updatedNote = note.copy(
                        title = title,
                        content = content,
                        color = selectedColor
                    )
                    DataManager.notes[index] = updatedNote
                }
                updateDisplayList()
                noteAdapter.updateNotes(displayNotes)
                DataManager.saveData(this)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your note...")
        try {
            startActivityForResult(intent, VOICE_CODE)
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Voice recognition not supported", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!result.isNullOrEmpty()) {
                val spokenText = result[0]
                activeContentInput?.let {
                    val currentText = it.text.toString()
                    val newText = if (currentText.isEmpty()) spokenText else "$currentText $spokenText"
                    it.setText(newText)
                    it.setSelection(it.text.length)
                }
            }
        }
    }

    private fun showReminderPicker(title: String) {
        val calendar = Calendar.getInstance()
        android.app.TimePickerDialog(this, { _, h, m ->
            calendar.set(Calendar.HOUR_OF_DAY, h)
            calendar.set(Calendar.MINUTE, m)
            calendar.set(Calendar.SECOND, 0)
            
            val intent = Intent(this, ReminderReceiver::class.java).apply {
                putExtra("TASK_NAME", "Note: $title")
                putExtra("TASK_TIMESTAMP", System.currentTimeMillis()) 
            }
            
            val pendingIntent = android.app.PendingIntent.getBroadcast(this, title.hashCode(), intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
            val alarmManager = getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
            
            android.widget.Toast.makeText(this, "Reminder set for ${String.format(Locale.US, "%02d:%02d", h, m)}", android.widget.Toast.LENGTH_SHORT).show()
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
    }


    private fun applyAutoCleanup() {
        val days = DataManager.noteAutoCleanupDays
        if (days <= 0) return
        
        val cutoff = System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000)
        val removed = DataManager.notes.removeAll { it.timestamp < cutoff && it.category != "Stories" } 
        if (removed) {
            DataManager.saveData(this)
            updateDisplayList()
        }
    }

    private fun showNotesSettingsDialog() {
        startActivity(Intent(this, NoteSettingsActivity::class.java))
    }

    private fun showTemplateEditorDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories) 
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val title = dialog.findViewById<TextView>(R.id.tv_categories_title)
        val container = dialog.findViewById<android.widget.LinearLayout>(R.id.categories_container)
        val etInput = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAdd = dialog.findViewById<View>(R.id.btn_add_category)

        title.text = "Edit Templates"
        etInput.hint = "Select a category to edit..."
        etInput.isEnabled = false 
        btnAdd.visibility = View.GONE

        val categories = listOf("Daily", "Questions", "Stories")
        categories.forEach { cat ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_task_header, container, false)
            itemView.findViewById<TextView>(R.id.tv_header_title).text = cat
            itemView.findViewById<View>(R.id.iv_header_chevron).visibility = View.GONE
            
            itemView.setOnClickListener {
                showEditSingleTemplateDialog(cat)
                dialog.dismiss()
            }
            container.addView(itemView)
        }
        showDialogSafe(dialog)
    }

    private fun showEditSingleTemplateDialog(category: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget) 
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val title = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val etContent = dialog.findViewById<EditText>(R.id.et_budget_amount)
        val subtext = dialog.findViewById<TextView>(R.id.tv_dialog_subtext)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_budget)

        title.text = "$category Template"
        subtext.text = "Enter text to pre-fill new notes"
        etContent.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        etContent.gravity = android.view.Gravity.START
        etContent.setText(DataManager.noteTemplates[category] ?: "")
        
        btnSave.setOnClickListener {
            DataManager.noteTemplates[category] = etContent.text.toString()
            DataManager.saveData(this)
            dialog.dismiss()
        }
        showDialogSafe(dialog)
    }

    private fun showBulkMoveDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val title = dialog.findViewById<TextView>(R.id.tv_categories_title)
        val container = dialog.findViewById<android.widget.LinearLayout>(R.id.categories_container)
        dialog.findViewById<View>(R.id.container_add_category).visibility = View.GONE

        title.text = "Bulk Move from $currentCategory"
        val targets = listOf("Notes", "Questions", "Daily", "Stories").filter { it != currentCategory }
        
        targets.forEach { target ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_task_header, container, false)
            itemView.findViewById<TextView>(R.id.tv_header_title).text = "Move to $target"
            
            itemView.setOnClickListener {
                allNotes.forEach { if (it.category == currentCategory) it.category = target }
                DataManager.saveData(this)
                switchCategory(currentCategory) 
                dialog.dismiss()
                android.widget.Toast.makeText(this, "Moved all notes to $target", android.widget.Toast.LENGTH_SHORT).show()
            }
            container.addView(itemView)
        }
        showDialogSafe(dialog)
    }
}
