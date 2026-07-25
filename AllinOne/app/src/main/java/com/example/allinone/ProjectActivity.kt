package com.example.allinone

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProjectActivity : BaseActivity() {

    private val viewModel: ProjectViewModel by viewModels()
    
    private lateinit var listSection: ProjectListSection
    private lateinit var headerSection: ProjectHeaderSection
    private lateinit var navigationSection: ProjectNavigationSection
    private lateinit var themeManager: ProjectThemeManager
    private lateinit var gestureDetector: android.view.GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)

        if (intent.hasExtra("OPEN_PROJECTS")) {
            viewModel.isProjectsTab = intent.getBooleanExtra("OPEN_PROJECTS", false)
        }

        initSections()
        setupLogic()
    }

    private fun initSections() {
        listSection = ProjectListSection(
            this,
            findViewById(R.id.project_notes_list),
            findViewById(R.id.project_ideas_list),
            onProjectClick = { note -> onProjectItemClick(note) },
            onIdeaClick = { note -> showEditIdeaDialog(note) }
        ) {
            listSection.updateDisplayLists()
        }

        headerSection = ProjectHeaderSection(
            findViewById(R.id.project_root_layout),
            onBack = { finish() },
            onEditModeToggled = { toggleEditMode() },
            onSettingsClicked = { startActivity(Intent(this, ProjectSettingsActivity::class.java)) },
            onWorkspaceClicked = { startActivity(Intent(this, com.example.allinone.workspace.ui.activity.WorkspaceActivity::class.java)) }
        )

        navigationSection = ProjectNavigationSection(
            findViewById(R.id.bottom_navigation_projects),
            findViewById(R.id.nav_projects),
            findViewById(R.id.nav_notes),
            findViewById(R.id.iv_projects_icon),
            findViewById(R.id.tv_projects_label),
            findViewById(R.id.iv_notes_icon),
            findViewById(R.id.tv_notes_label)
        ) { isProjects ->
            updateTabUI(isProjects)
        }

        themeManager = ProjectThemeManager(
            findViewById(R.id.project_aura_background),
            findViewById(R.id.btn_add_project_note)
        )
    }

    private fun setupLogic() {
        headerSection.setup()
        navigationSection.setup(viewModel.isProjectsTab)
        themeManager.applyTheme()
        listSection.updateDisplayLists()
        updateTabUI(viewModel.isProjectsTab)

        findViewById<View>(R.id.btn_add_project_note).setOnClickListener {
            if (viewModel.isProjectsTab) {
                startActivity(Intent(this, AddProjectActivity::class.java))
            } else {
                startActivity(Intent(this, AddIdeaActivity::class.java))
            }
        }
        
        setupGestureDetector()
        setupKeyboardHandling(findViewById(R.id.project_root_layout), findViewById(R.id.project_content_container))
    }

    private fun updateTabUI(isProjects: Boolean) {
        var target = isProjects
        if (!DataManager.projectRoadmapsEnabled && target) target = false
        if (!DataManager.projectIdeasEnabled && !target) target = true

        viewModel.isProjectsTab = target
        findViewById<TextView>(R.id.tv_title).text = if (target) "PROJECTS" else "IDEAS"
        listSection.setVisibility(target)
        navigationSection.updateNavUI(target)
        
        findViewById<View>(R.id.btn_edit_mode).visibility = if (target) View.VISIBLE else View.GONE
        findViewById<View>(R.id.btn_workspace).visibility = if (target) View.VISIBLE else View.GONE
        
        val bottomPadding = if (DataManager.projectRoadmapsEnabled && DataManager.projectIdeasEnabled) 100.dpToPx() else 24.dpToPx()
        findViewById<View>(R.id.project_notes_list).setPadding(0, 0, 0, bottomPadding)
        findViewById<View>(R.id.project_ideas_list).setPadding(0, 0, 0, bottomPadding)
    }

    private fun toggleEditMode() {
        viewModel.isEditMode = !viewModel.isEditMode
        val btnEditMode = findViewById<ImageButton>(R.id.btn_edit_mode)
        val activeColor = ContextCompat.getColor(this, R.color.chip_selected)
        val inactiveColor = Color.WHITE
        
        btnEditMode.imageTintList = ColorStateList.valueOf(if (viewModel.isEditMode) activeColor else inactiveColor)
        btnEditMode.backgroundTintList = ColorStateList.valueOf(if (viewModel.isEditMode) Color.parseColor("#33FFFFFF") else Color.parseColor("#11FFFFFF"))
        
        Toast.makeText(this, if (viewModel.isEditMode) "Edit Mode: ON" else "Edit Mode: OFF", Toast.LENGTH_SHORT).show()
    }

    fun onProjectItemClick(note: Note) {
        if (viewModel.isEditMode) {
            showEditProjectNoteDialog(note)
        } else {
            startActivity(Intent(this, AddProjectActivity::class.java).apply {
                putExtra("PROJECT_INDEX", DataManager.projects.indexOf(note))
                putExtra("IS_VIEW_ONLY", true)
            })
        }
    }

    fun showEditProjectNoteDialog(note: Note) {
        startActivity(Intent(this, AddProjectActivity::class.java).apply {
            putExtra("PROJECT_INDEX", DataManager.projects.indexOf(note))
        })
    }

    fun showEditIdeaDialog(note: Note) {
        startActivity(Intent(this, AddIdeaActivity::class.java).apply {
            putExtra("IDEA_INDEX", DataManager.projects.indexOf(note))
        })
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun setupGestureDetector() {
        gestureDetector = android.view.GestureDetector(this, object : SwipeGestureListener() {
            override fun onSwipeLeft() {
                if (!viewModel.isProjectsTab && DataManager.projectRoadmapsEnabled) updateTabUI(true)
            }
            override fun onSwipeRight() {
                if (viewModel.isProjectsTab && DataManager.projectIdeasEnabled) updateTabUI(false)
            }
        })
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent?): Boolean {
        if (ev != null) gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    fun showProjectMenu(anchor: View, note: Note) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.layout_custom_menu, null)

        val popupWindow = PopupWindow(
            menuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 10f

        val actionView = menuView.findViewById<View>(R.id.menu_take_day_off)
        val actionText = menuView.findViewById<TextView>(R.id.tv_action_text)
        val actionIcon = menuView.findViewById<ImageView>(R.id.iv_action_icon)

        if (note.status == "Completed") {
            actionText.text = "MARK INCOMPLETE"
            actionIcon.setImageResource(R.drawable.icons8_coffee_100) // Or some other icon
        } else {
            actionText.text = "MARK COMPLETE"
            actionIcon.setImageResource(R.drawable.icons8_check_mark_100)
        }

        actionView.setOnClickListener {
            note.status = if (note.status == "Completed") "In Progress" else "Completed"
            note.progress = if (note.status == "Completed") 100 else note.progress
            note.changeHistory.add(ProjectHistory(action = "Status Changed", description = "Manually marked as ${note.status}"))
            DataManager.saveData(this)
            listSection.updateDisplayLists()
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_edit).setOnClickListener {
            popupWindow.dismiss()
            if (note.category == "Project") {
                showEditProjectNoteDialog(note)
            } else {
                showEditIdeaDialog(note)
            }
        }

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            DataManager.projects.remove(note)
            DataManager.saveData(this)
            listSection.updateDisplayLists()
            popupWindow.dismiss()
        }

        val hideUnhideView = menuView.findViewById<View>(R.id.menu_hide_unhide)
        hideUnhideView.visibility = View.VISIBLE
        val hideText = menuView.findViewById<TextView>(R.id.tv_hide_unhide_text)
        hideText.text = if (note.isPinned) "UNPIN" else "PIN"
        menuView.findViewById<ImageView>(R.id.iv_hide_unhide_icon).setImageResource(
            if (note.isPinned) android.R.drawable.btn_star_big_off else android.R.drawable.btn_star_big_on
        )

        hideUnhideView.setOnClickListener {
            note.isPinned = !note.isPinned
            DataManager.saveData(this)
            listSection.updateDisplayLists()
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, 0, -anchor.height)
    }

    fun showProjectHistoryDialog(note: Note) {
        startActivity(Intent(this, ProjectHistoryActivity::class.java).apply {
            putExtra("PROJECT_INDEX", DataManager.projects.indexOf(note))
        })
    }

    override fun onResume() {
        super.onResume()
        updateTabUI(viewModel.isProjectsTab)
        listSection.updateDisplayLists()
        themeManager.applyTheme()
    }
}
