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
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class NotesActivity : AppCompatActivity() {

    private val VOICE_CODE = 1001
    private var activeContentInput: EditText? = null
    private val allNotes = DataManager.notes
    private lateinit var noteAdapter: NoteAdapter
    private var currentCategory = DataManager.noteDefaultCategory
    private var displayNotes = mutableListOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        applyAutoCleanup()

        val notesList = findViewById<RecyclerView>(R.id.notes_list)
        notesList.layoutManager = LinearLayoutManager(this)
        
        updateDisplayList()
        noteAdapter = NoteAdapter(displayNotes) { 
            DataManager.saveData(this)
            updateDisplayList()
        }
        notesList.adapter = noteAdapter

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        setupBottomNavigation()

        findViewById<View>(R.id.btn_notes_settings).setOnClickListener {
            val inflater = LayoutInflater.from(this)
            val menuView = inflater.inflate(R.layout.layout_activity_settings_menu, null)
            val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
            popupWindow.elevation = 10f
            
            // Hide task-specific items
            menuView.findViewById<View>(R.id.menu_clear_completed).visibility = View.GONE
            menuView.findViewById<View>(R.id.menu_toggle_completed).visibility = View.GONE
            
            menuView.findViewById<View>(R.id.menu_activity_settings).setOnClickListener {
                showNotesSettingsDialog()
                popupWindow.dismiss()
            }
            popupWindow.showAsDropDown(it, -150, 0)
        }

        findViewById<View>(R.id.btn_create_new_note).setOnClickListener { showAddNoteDialog() }
    }

    private fun setupBottomNavigation() {
        val navNotes = findViewById<View>(R.id.nav_notes)
        val navQuestions = findViewById<View>(R.id.nav_questions)
        val navDaily = findViewById<View>(R.id.nav_daily)
        val navStories = findViewById<View>(R.id.nav_stories)

        navNotes.setOnClickListener { switchCategory("Notes") }
        navQuestions.setOnClickListener { switchCategory("Questions") }
        navDaily.setOnClickListener { switchCategory("Daily") }
        navStories.setOnClickListener { switchCategory("Stories") }
        
        updateNavUI()
    }

    private fun switchCategory(category: String) {
        currentCategory = category
        updateDisplayList()
        noteAdapter.updateNotes(displayNotes)
        updateNavUI()
    }

    private fun updateNavUI() {
        val navs = mapOf(
            "Notes" to Pair(findViewById<ImageView>(R.id.iv_notes_icon), findViewById<TextView>(R.id.tv_notes_label)),
            "Questions" to Pair(findViewById<ImageView>(R.id.iv_questions_icon), findViewById<TextView>(R.id.tv_questions_label)),
            "Daily" to Pair(findViewById<ImageView>(R.id.iv_daily_icon), findViewById<TextView>(R.id.tv_daily_label)),
            "Stories" to Pair(findViewById<ImageView>(R.id.iv_stories_icon), findViewById<TextView>(R.id.tv_stories_label))
        )

        navs.forEach { (cat, views) ->
            val isActive = cat == currentCategory
            val color = if (isActive) Color.WHITE else Color.GRAY
            val bgAlpha = if (isActive) "#66FFFFFF" else "#22FFFFFF"
            
            views.first.setColorFilter(color)
            views.first.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(bgAlpha))
            views.second.setTextColor(color)
        }
    }

    private fun updateDisplayList() {
        displayNotes.clear()
        displayNotes.addAll(allNotes.filter { 
            it.category == currentCategory && (DataManager.noteShowHidden || !it.isHidden) 
        }.sortedByDescending { it.timestamp })
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

        var selectedColor = ContextCompat.getColor(this, R.color.card_blue)
        colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)

        val sdf = SimpleDateFormat("dd MMMM h:mm a", Locale.getDefault())
        val currentDateStr = sdf.format(Date())
        
        // Apply Template
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
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateMetadata() }
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
            if (title.isNotEmpty() || content.isNotEmpty()) {
                allNotes.add(0, Note(title, content, color = selectedColor, category = currentCategory))
                updateDisplayList()
                noteAdapter.updateNotes(displayNotes)
                DataManager.saveData(this)
                dialog.dismiss()
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

        titleInput.setText(note.title)
        contentInput.setText(note.content)
        var selectedColor = if (note.color != -1) note.color else ContextCompat.getColor(this, R.color.card_blue)
        colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)

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
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateMetadata() }
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
            note.title = titleInput.text.toString()
            note.content = contentInput.text.toString()
            note.color = selectedColor
            updateDisplayList()
            noteAdapter.updateNotes(displayNotes)
            DataManager.saveData(this)
            dialog.dismiss()
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
                putExtra("TASK_TIMESTAMP", System.currentTimeMillis()) // Using current as a unique ID
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
        val removed = DataManager.notes.removeAll { it.timestamp < cutoff && it.category != "Stories" } // Keep stories safe
        if (removed) {
            DataManager.saveData(this)
            updateDisplayList()
        }
    }

    private fun showNotesSettingsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_notes_settings)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val itemDefault = dialog.findViewById<View>(R.id.item_default_cat)
        val itemCleanup = dialog.findViewById<View>(R.id.item_auto_cleanup)
        val itemTemplates = dialog.findViewById<View>(R.id.item_templates)
        val itemBulk = dialog.findViewById<View>(R.id.item_bulk_move)
        val itemExport = dialog.findViewById<View>(R.id.item_export_notes)
        val itemShowHidden = dialog.findViewById<View>(R.id.item_show_hidden)
        val swShowHidden = dialog.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.sw_show_hidden)
        val tvDefaultSummary = dialog.findViewById<TextView>(R.id.tv_default_cat_summary)
        val tvCleanupSummary = dialog.findViewById<TextView>(R.id.tv_cleanup_summary)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_settings)

        tvDefaultSummary.text = "Current: ${DataManager.noteDefaultCategory}"
        tvCleanupSummary.text = if (DataManager.noteAutoCleanupDays > 0) "Cleanup after ${DataManager.noteAutoCleanupDays} days" else "Disabled"
        swShowHidden?.isChecked = DataManager.noteShowHidden

        itemDefault.setOnClickListener {
            val categories = listOf("Notes", "Questions", "Daily", "Stories")
            val next = categories[(categories.indexOf(DataManager.noteDefaultCategory) + 1) % categories.size]
            DataManager.noteDefaultCategory = next
            DataManager.saveData(this)
            tvDefaultSummary.text = "Current: $next"
            android.widget.Toast.makeText(this, "Default tab set to $next", android.widget.Toast.LENGTH_SHORT).show()
        }

        itemCleanup.setOnClickListener {
            val options = listOf(0, 7, 30, 90)
            val current = DataManager.noteAutoCleanupDays
            val next = options[(options.indexOf(current).coerceAtLeast(0) + 1) % options.size]
            DataManager.noteAutoCleanupDays = next
            DataManager.saveData(this)
            tvCleanupSummary.text = if (next > 0) "Cleanup after $next days" else "Disabled"
            android.widget.Toast.makeText(this, "Auto-cleanup: ${if (next > 0) "$next days" else "Off"}", android.widget.Toast.LENGTH_SHORT).show()
        }

        itemShowHidden.setOnClickListener {
            DataManager.noteShowHidden = !DataManager.noteShowHidden
            swShowHidden?.isChecked = DataManager.noteShowHidden
            DataManager.saveData(this)
            updateDisplayList()
            noteAdapter.updateNotes(displayNotes)
        }

        itemTemplates.setOnClickListener { showTemplateEditorDialog() }
        itemBulk.setOnClickListener { showBulkMoveDialog() }
        itemExport.setOnClickListener { exportCategoryToText() }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showTemplateEditorDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_manage_categories) // Re-use layout structure
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val title = dialog.findViewById<TextView>(R.id.tv_categories_title)
        val container = dialog.findViewById<android.widget.LinearLayout>(R.id.categories_container)
        val etInput = dialog.findViewById<EditText>(R.id.et_new_category)
        val btnAdd = dialog.findViewById<View>(R.id.btn_add_category)

        title.text = "Edit Templates"
        etInput.hint = "Select a category to edit..."
        etInput.isEnabled = false // User selects category first
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
        dialog.show()
    }

    private fun showEditSingleTemplateDialog(category: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_set_budget) // Re-use input layout
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
        dialog.show()
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
                switchCategory(currentCategory) // Refresh
                dialog.dismiss()
                android.widget.Toast.makeText(this, "Moved all notes to $target", android.widget.Toast.LENGTH_SHORT).show()
            }
            container.addView(itemView)
        }
        dialog.show()
    }

    private fun exportCategoryToText() {
        val notesToExport = allNotes.filter { it.category == currentCategory }
        if (notesToExport.isEmpty()) {
            android.widget.Toast.makeText(this, "No notes to export in $currentCategory", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val builder = StringBuilder()
        builder.append("=== $currentCategory EXPORT ===\n\n")
        notesToExport.forEach { note ->
            builder.append("Title: ${note.title}\n")
            builder.append("Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(note.timestamp))}\n")
            builder.append("Content:\n${note.content}\n")
            builder.append("---------------------------\n\n")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$currentCategory Export")
            putExtra(Intent.EXTRA_TEXT, builder.toString())
        }
        startActivity(Intent.createChooser(intent, "Export Notes"))
    }
}
