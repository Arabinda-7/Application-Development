package com.example.allinone

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class NotesActivity : AppCompatActivity() {

    private val notes = DataManager.notes
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        val dateTextView = findViewById<TextView>(R.id.tv_date)
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        dateTextView.text = sdf.format(Date())

        val notesList = findViewById<RecyclerView>(R.id.notes_list)
        notesList.layoutManager = LinearLayoutManager(this)
        noteAdapter = NoteAdapter(notes) { noteAdapter.updateNotes(notes) }
        notesList.adapter = noteAdapter

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<View>(R.id.btn_create_new_note).setOnClickListener { showAddNoteDialog() }
    }

    private fun showAddNoteDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.note_title_input)
        val contentInput = dialogView.findViewById<EditText>(R.id.note_content_input)
        val colorPreview = dialogView.findViewById<View>(R.id.note_color_preview)

        var selectedColor = ContextCompat.getColor(this, R.color.card_blue)

        dialogView.findViewById<View>(R.id.note_color_selection_row).setOnClickListener {
            val colors = listOf(
                ContextCompat.getColor(this, R.color.card_blue),
                ContextCompat.getColor(this, R.color.card_orange),
                ContextCompat.getColor(this, R.color.card_green),
                Color.MAGENTA, Color.RED, Color.CYAN
            )
            selectedColor = colors[(colors.indexOf(selectedColor) + 1) % colors.size]
            colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = titleInput.text.toString()
                val content = contentInput.text.toString()
                if (title.isNotEmpty() || content.isNotEmpty()) {
                    notes.add(0, Note(title, content, color = selectedColor))
                    noteAdapter.updateNotes(notes)
                    DataManager.saveData(this)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun showEditNoteDialog(note: Note) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_note, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.note_title_input)
        val contentInput = dialogView.findViewById<EditText>(R.id.note_content_input)
        val colorPreview = dialogView.findViewById<View>(R.id.note_color_preview)

        titleInput.setText(note.title)
        contentInput.setText(note.content)
        var selectedColor = if (note.color != -1) note.color else ContextCompat.getColor(this, R.color.card_blue)
        colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)

        dialogView.findViewById<View>(R.id.note_color_selection_row).setOnClickListener {
            val colors = listOf(
                ContextCompat.getColor(this, R.color.card_blue),
                ContextCompat.getColor(this, R.color.card_orange),
                ContextCompat.getColor(this, R.color.card_green),
                Color.MAGENTA, Color.RED, Color.CYAN
            )
            selectedColor = colors[(colors.indexOf(selectedColor) + 1) % colors.size]
            colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                note.title = titleInput.text.toString()
                note.content = contentInput.text.toString()
                note.color = selectedColor
                noteAdapter.updateNotes(notes)
                DataManager.saveData(this)
            }
            .setNeutralButton("Delete") { _, _ ->
                notes.remove(note)
                noteAdapter.updateNotes(notes)
                DataManager.saveData(this)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
