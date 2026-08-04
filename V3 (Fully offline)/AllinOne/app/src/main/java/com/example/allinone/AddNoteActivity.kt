package com.example.allinone

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.allinone.data.model.Note
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AddNoteActivity : BaseActivity() {

    private val VOICE_CODE = 1001
    private var noteId: Long = -1L
    private var existingNote: Note? = null
    private var currentCategory: String = "Notes"
    
    private val viewModel: AddNoteViewModel by viewModels()

    private lateinit var titleInput: EditText
    private lateinit var contentInput: EditText
    private lateinit var colorPreview: View
    private lateinit var btnSave: TextView
    private lateinit var btnDelete: View
    private lateinit var tvMetadata: TextView
    private var selectedColor: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        noteId = intent.getLongExtra("NOTE_ID", -1L)
        currentCategory = intent.getStringExtra("CATEGORY") ?: "Notes"
        
        initViews()
        
        lifecycleScope.launch {
            if (noteId != -1L) {
                existingNote = viewModel.getNoteById(noteId)
            }
            setupLogic()
        }

        setupKeyboardHandling(findViewById(R.id.add_note_root), findViewById(R.id.add_note_content_container))
    }

    private fun initViews() {
        titleInput = findViewById(R.id.note_title_input)
        contentInput = findViewById(R.id.note_content_input)
        colorPreview = findViewById(R.id.note_color_preview)
        btnSave = findViewById(R.id.btn_save_note)
        btnDelete = findViewById(R.id.btn_delete_note)
        tvMetadata = findViewById(R.id.tv_note_metadata)
        
        findViewById<View>(R.id.btn_close_note).setOnClickListener { finish() }
    }

    private fun setupLogic() {
        selectedColor = if (existingNote?.color != null && existingNote?.color != -1) {
            existingNote?.color ?: ContextCompat.getColor(this, R.color.card_blue)
        } else {
            ContextCompat.getColor(this, R.color.card_blue)
        }
        colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)

        val sdf = SimpleDateFormat("dd MMMM h:mm a", Locale.getDefault())
        val dateStr = existingNote?.let { sdf.format(Date(it.timestamp)) } ?: sdf.format(Date())

        if (existingNote != null) {
            titleInput.setText(existingNote?.title)
            contentInput.setText(existingNote?.content)
            btnSave.text = "SAVE"
            btnDelete.visibility = View.VISIBLE
            btnDelete.setOnClickListener { showDeleteConfirmation() }
        } else {
            val template = viewModel.settings.value.noteTemplates[currentCategory] ?: ""
            if (template.isNotEmpty()) {
                contentInput.setText(template)
                contentInput.setSelection(contentInput.text.length)
            }
        }

        fun updateMetadata() {
            val count = (titleInput.text.length + contentInput.text.length)
            tvMetadata.text = "$dateStr | $count characters"
            
            val hasTitle = titleInput.text.toString().trim().isNotEmpty()
            btnSave.isEnabled = hasTitle
            btnSave.alpha = if (hasTitle) 1.0f else 0.5f
        }
        updateMetadata()

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { updateMetadata() }
            override fun afterTextChanged(s: Editable?) {}
        }
        titleInput.addTextChangedListener(textWatcher)
        contentInput.addTextChangedListener(textWatcher)

        colorPreview.setOnClickListener {
            val colors = listOf(ContextCompat.getColor(this, R.color.card_blue), ContextCompat.getColor(this, R.color.card_orange), ContextCompat.getColor(this, R.color.card_green), Color.MAGENTA, Color.RED, Color.CYAN)
            selectedColor = colors[(colors.indexOf(selectedColor) + 1) % colors.size]
            colorPreview.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedColor)
        }

        findViewById<View>(R.id.btn_voice_input).apply {
            visibility = if (viewModel.settings.value.voiceInputEnabled) View.VISIBLE else View.GONE
            setOnClickListener {
                checkAndRequestPermission(android.Manifest.permission.RECORD_AUDIO) {
                    startVoiceInput()
                }
            }
        }
        findViewById<View>(R.id.btn_reminder).setOnClickListener {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                checkAndRequestPermission(android.Manifest.permission.POST_NOTIFICATIONS) {
                    showReminderPicker(titleInput.text.toString())
                }
            } else {
                showReminderPicker(titleInput.text.toString())
            }
        }

        btnSave.setOnClickListener { saveNote() }
    }

    private fun showDeleteConfirmation() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("DELETE") { _, _ ->
                existingNote?.let { 
                    viewModel.deleteNote(it)
                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your note...")
        try {
            startActivityForResult(intent, VOICE_CODE)
        } catch (e: Exception) {
            Toast.makeText(this, "Voice recognition not supported", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!result.isNullOrEmpty()) {
                val spokenText = result[0]
                val currentText = contentInput.text.toString()
                val newText = if (currentText.isEmpty()) spokenText else "$currentText $spokenText"
                contentInput.setText(newText)
                contentInput.setSelection(contentInput.text.length)
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
            
            alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            
            Toast.makeText(this, "Reminder set for ${String.format(Locale.US, "%02d:%02d", h, m)}", Toast.LENGTH_SHORT).show()
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
    }

    private fun saveNote() {
        val title = titleInput.text.toString().trim()
        val content = contentInput.text.toString()
        if (title.isNotEmpty()) {
            val noteToSave = existingNote?.copy(
                title = title,
                content = content,
                color = selectedColor,
                updatedAt = System.currentTimeMillis()
            ) ?: Note(title, content, color = selectedColor, category = currentCategory)
            
            viewModel.saveNote(noteToSave, existingNote != null)
            setResult(RESULT_OK)
            finish()
        } else {
            titleInput.error = "Title is required"
            Toast.makeText(this, "Please enter a title for your note", Toast.LENGTH_SHORT).show()
        }
    }
}
