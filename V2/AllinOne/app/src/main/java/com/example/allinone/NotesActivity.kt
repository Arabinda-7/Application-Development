package com.example.allinone

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton

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

        applyAutoCleanup()
        initSections()
        setupLogic()

        if (intent.getBooleanExtra("QUICK_ADD", false)) {
            startActivity(Intent(this, AddNoteActivity::class.java).apply {
                putExtra("CATEGORY", viewModel.currentCategory)
            })
        }
    }

    private fun initSections() {
        listSection = NotesListSection(this, findViewById(R.id.notes_list)) {
            listSection.updateDisplayList(viewModel.currentCategory)
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

        navigationSection = NotesNavigationSection(
            this,
            findViewById(R.id.bottom_navigation_notes),
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
            viewModel.currentCategory = category
            listSection.updateDisplayList(category)
            navigationSection.setup(category)
        }

        themeManager = NotesThemeManager(
            this,
            findViewById(R.id.note_aura_background),
            findViewById(R.id.btn_create_new_note)
        )
    }

    private fun setupLogic() {
        headerSection.setup()
        navigationSection.setup(viewModel.currentCategory)
        themeManager.applyTheme()
        listSection.updateDisplayList(viewModel.currentCategory)
        
        setupGestureDetector()
        setupKeyboardHandling(findViewById(R.id.notes_root_layout), findViewById(R.id.notes_content_container))

        findViewById<FloatingActionButton>(R.id.btn_create_new_note).setOnClickListener {
            startActivity(Intent(this, AddNoteActivity::class.java).apply {
                putExtra("CATEGORY", viewModel.currentCategory)
            })
        }
    }

    private fun toggleDeleteMode(enabled: Boolean) {
        viewModel.isDeleteMode = enabled
        listSection.setDeleteMode(enabled)
        headerSection.setSettingsIcon(if (enabled) android.R.drawable.ic_menu_delete else R.drawable.baseline_tune_24)
        findViewById<View>(R.id.btn_create_new_note).visibility = if (enabled) View.GONE else View.VISIBLE
    }

    private fun showSettingsMenu(anchor: View) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.layout_menu_note, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        val btnToggleHidden = menuView.findViewById<View>(R.id.menu_toggle_completed)
        val tvToggleHidden = menuView.findViewById<TextView>(R.id.tv_toggle_completed)
        val ivToggleHidden = menuView.findViewById<ImageView>(R.id.iv_toggle_completed)
        
        btnToggleHidden.visibility = View.VISIBLE
        tvToggleHidden.text = if (DataManager.noteShowHidden) "HIDE HIDDEN" else "SHOW HIDDEN"
        ivToggleHidden.setImageResource(if (DataManager.noteShowHidden) android.R.drawable.ic_menu_view else android.R.drawable.ic_partial_secure)

        btnToggleHidden.setOnClickListener {
            DataManager.noteShowHidden = !DataManager.noteShowHidden
            DataManager.saveData(this)
            listSection.updateDisplayList(viewModel.currentCategory)
            popupWindow.dismiss()
        }

        val btnMultiDelete = menuView.findViewById<View>(R.id.menu_clear_completed)
        btnMultiDelete.visibility = View.VISIBLE
        (if (btnMultiDelete is ViewGroup && btnMultiDelete.childCount > 1) btnMultiDelete.getChildAt(1) as? TextView else null)?.text = "SELECT & DELETE"

        btnMultiDelete.setOnClickListener {
            toggleDeleteMode(true)
            popupWindow.dismiss()
        }
        
        menuView.findViewById<View>(R.id.menu_activity_settings).setOnClickListener {
            startActivity(Intent(this, NoteSettingsActivity::class.java))
            popupWindow.dismiss()
        }
        popupWindow.showAsDropDown(anchor, -150, 0)
    }

    private fun setupGestureDetector() {
        gestureDetector = android.view.GestureDetector(this, object : SwipeGestureListener() {
            override fun onSwipeLeft() {
                if (DataManager.noteVisibleSections.size > 1) {
                    val currentIndex = DataManager.noteVisibleSections.indexOf(viewModel.currentCategory)
                    val nextIndex = (currentIndex + 1) % DataManager.noteVisibleSections.size
                    navigationSection.setup(DataManager.noteVisibleSections[nextIndex])
                }
            }
            override fun onSwipeRight() {
                if (DataManager.noteVisibleSections.size > 1) {
                    val currentIndex = DataManager.noteVisibleSections.indexOf(viewModel.currentCategory)
                    val prevIndex = if (currentIndex <= 0) DataManager.noteVisibleSections.size - 1 else currentIndex - 1
                    navigationSection.setup(DataManager.noteVisibleSections[prevIndex])
                }
            }
        })
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent?): Boolean {
        if (ev != null) gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun applyAutoCleanup() {
        val days = DataManager.noteAutoCleanupDays
        if (days <= 0) return
        val cutoff = System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000)
        if (DataManager.notes.removeAll { it.timestamp < cutoff && it.category != "Stories" }) {
            DataManager.saveData(this)
        }
    }

    fun showEditNoteDialog(note: Note) {
        startActivity(Intent(this, AddNoteActivity::class.java).apply {
            putExtra("NOTE_INDEX", DataManager.notes.indexOf(note))
        })
    }

    override fun onResume() {
        super.onResume()
        navigationSection.setup(viewModel.currentCategory)
        listSection.updateDisplayList(viewModel.currentCategory)
        themeManager.applyTheme()
    }
}
