package com.example.allinone

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
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

    private val allNotes = DataManager.notes
    private lateinit var noteAdapter: NoteAdapter
    private var currentCategory = "Notes"
    private var displayNotes = mutableListOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

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
            menuView.findViewById<View>(R.id.menu_activity_settings).setOnClickListener { popupWindow.dismiss() }
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
        displayNotes.addAll(allNotes.filter { it.category == currentCategory }.sortedByDescending { it.timestamp })
    }

    private fun showAddNoteDialog() {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_add_note)

        val titleInput = dialog.findViewById<EditText>(R.id.note_title_input)
        val contentInput = dialog.findViewById<EditText>(R.id.note_content_input)
        val colorPreview = dialog.findViewById<View>(R.id.note_color_preview)
        val btnSave = dialog.findViewById<TextView>(R.id.btn_save_note)
        val btnClose = dialog.findViewById<View>(R.id.btn_close_note)
        val tvMetadata = dialog.findViewById<TextView>(R.id.tv_note_metadata)

        var selectedColor = ContextCompat.getColor(this, R.color.card_blue)
        colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)

        val sdf = SimpleDateFormat("dd MMMM h:mm a", Locale.getDefault())
        val currentDateStr = sdf.format(Date())
        
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
}
