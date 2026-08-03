package com.example.allinone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.allinone.data.model.Note
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class NotesActivity : BaseActivity() {

    private val viewModel: NotesViewModel by viewModels()
    
    private lateinit var listSection: NotesListSection
    private lateinit var headerSection: NotesHeaderSection
    private lateinit var navigationSection: NotesNavigationSection
    private lateinit var themeManager: NotesThemeManager
    private lateinit var gestureDetector: android.view.GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        initSections()
        setupLogic()
        observeViewModel()

        viewModel.applyAutoCleanup()

        if (intent.getBooleanExtra("QUICK_ADD", false)) {
            startActivity(Intent(this, AddNoteActivity::class.java).apply {
                putExtra("CATEGORY", viewModel.currentCategory.value)
            })
        }
    }

    private fun initSections() {
        listSection = NotesListSection(this, findViewById(R.id.notes_list)) {
            // Data changed callback
        }

        headerSection = NotesHeaderSection(
            findViewById(R.id.notes_root_layout),
            onBack = { if (viewModel.isDeleteMode) toggleDeleteMode(false) else finish() },
            onSettingsClicked = { anchor ->
                if (viewModel.isDeleteMode) {
                    listSection.deleteSelectedNotes()
                    toggleDeleteMode(false)
                } else {
                    showSettingsMenu(anchor)
                }
            }
        )

        val navRoot = findViewById<LinearLayout>(R.id.bottom_navigation_notes)
        navigationSection = NotesNavigationSection(
            this,
            navRoot,
            mapOf(
                "Notes" to findViewById(R.id.nav_notes),
                "Questions" to findViewById(R.id.nav_questions),
                "Daily" to findViewById(R.id.nav_daily),
                "Stories" to findViewById(R.id.nav_stories)
            ),
            mapOf(
                "Notes" to findViewById(R.id.iv_notes_icon),
                "Questions" to findViewById(R.id.iv_questions_icon),
                "Daily" to findViewById(R.id.iv_daily_icon),
                "Stories" to findViewById(R.id.iv_stories_icon)
            ),
            mapOf(
                "Notes" to findViewById(R.id.tv_notes_label),
                "Questions" to findViewById(R.id.tv_questions_label),
                "Daily" to findViewById(R.id.tv_daily_label),
                "Stories" to findViewById(R.id.tv_stories_label)
            )
        ) { category ->
            viewModel.setCategory(category)
        }

        themeManager = NotesThemeManager(
            this, 
            findViewById(R.id.note_aura_background),
            findViewById(R.id.btn_create_new_note)
        )
    }

    private fun setupLogic() {
        headerSection.setup()
        
        findViewById<FloatingActionButton>(R.id.btn_create_new_note).setOnClickListener {
            startActivity(Intent(this, AddNoteActivity::class.java).apply {
                putExtra("CATEGORY", viewModel.currentCategory.value)
            })
        }

        gestureDetector = android.view.GestureDetector(this, object : SwipeGestureListener() {
            override fun onSwipeLeft() {
                val settings = viewModel.settings.value
                if (settings.visibleSections.size > 1) {
                    val currentIndex = settings.visibleSections.indexOf(viewModel.currentCategory.value)
                    val nextIndex = (currentIndex + 1) % settings.visibleSections.size
                    viewModel.setCategory(settings.visibleSections[nextIndex])
                }
            }
            override fun onSwipeRight() {
                val settings = viewModel.settings.value
                if (settings.visibleSections.size > 1) {
                    val currentIndex = settings.visibleSections.indexOf(viewModel.currentCategory.value)
                    val prevIndex = if (currentIndex <= 0) settings.visibleSections.size - 1 else currentIndex - 1
                    viewModel.setCategory(settings.visibleSections[prevIndex])
                }
            }
        })
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.notes.collect { notes ->
                        listSection.updateDisplayList(notes)
                    }
                }
                launch {
                    viewModel.currentCategory.collect { category ->
                        navigationSection.updateNavUI(category)
                    }
                }
                launch {
                    viewModel.settings.collect { settings ->
                        navigationSection.setup(viewModel.currentCategory.value, settings)
                        themeManager.applyTheme()
                    }
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent?): Boolean {
        if (ev != null) gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun toggleDeleteMode(enabled: Boolean) {
        viewModel.isDeleteMode = enabled
        listSection.setDeleteMode(enabled)
        headerSection.setSettingsIcon(if (enabled) R.drawable.ic_trash else R.drawable.baseline_tune_24)
    }

    private fun showSettingsMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add("Delete Mode").setOnMenuItemClickListener {
            toggleDeleteMode(true)
            true
        }
        popup.menu.add("Settings").setOnMenuItemClickListener {
            startActivity(Intent(this, NoteSettingsActivity::class.java))
            true
        }
        popup.show()
    }

    fun showEditNoteDialog(note: Note) {
        startActivity(Intent(this, AddNoteActivity::class.java).apply {
            putExtra("NOTE_ID", note.timestamp) 
        })
    }

    override fun onResume() {
        super.onResume()
        themeManager.applyTheme()
    }
}
