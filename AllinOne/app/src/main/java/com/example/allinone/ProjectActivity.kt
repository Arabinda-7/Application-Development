package com.example.allinone

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.allinone.data.model.Note
import com.example.allinone.data.model.ProjectHistory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProjectActivity : BaseActivity() {

    private val viewModel: ProjectViewModel by viewModels()
    
    private lateinit var listSection: ProjectListSection
    private lateinit var headerSection: ProjectHeaderSection
    private lateinit var navigationSection: ProjectNavigationSection
    private lateinit var themeManager: ProjectThemeManager
    private lateinit var gestureDetector: android.view.GestureDetector

    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projects)

        if (intent.hasExtra("OPEN_PROJECTS")) {
            viewModel.setProjectsTab(intent.getBooleanExtra("OPEN_PROJECTS", false))
        }

        initSections()
        setupLogic()
        observeViewModel()
    }

    private fun initSections() {
        listSection = ProjectListSection(
            this,
            findViewById(R.id.project_notes_list),
            findViewById(R.id.project_ideas_list)
        ) {
            // Data change callback
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
            viewModel.setProjectsTab(isProjects)
        }

        themeManager = ProjectThemeManager(
            findViewById(R.id.project_aura_background),
            findViewById(R.id.btn_add_project_note)
        )
    }

    private fun setupLogic() {
        headerSection.setup()
        themeManager.applyTheme()
        navigationSection.setup(viewModel.isProjectsTab.value)

        findViewById<View>(R.id.btn_add_project_note).setOnClickListener {
            if (viewModel.isProjectsTab.value) {
                startActivity(Intent(this, AddProjectActivity::class.java))
            } else {
                startActivity(Intent(this, AddIdeaActivity::class.java))
            }
        }
        
        setupGestureDetector()
        setupKeyboardHandling(findViewById(R.id.project_root_layout), findViewById(R.id.project_content_container))
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.projects.collect { projects ->
                        listSection.updateDisplayLists(projects)
                    }
                }
                launch {
                    viewModel.isProjectsTab.collect { isProjects ->
                        updateTabUI(isProjects)
                    }
                }
                launch {
                    viewModel.settings.collect { settings ->
                        syncDataManager(settings)
                        navigationSection.setup(viewModel.isProjectsTab.value)
                    }
                }
            }
        }
    }

    private fun syncDataManager(settings: com.example.allinone.domain.repository.ProjectSettings) {
        DataManager.projectRoadmapsEnabled = settings.roadmapsEnabled
        DataManager.projectIdeasEnabled = settings.ideasEnabled
        DataManager.projectAutoSaveIdeas = settings.autoSaveIdeas
        DataManager.projectAutoArchive = settings.autoArchive
        DataManager.projectSynergySync = settings.synergySync
        DataManager.projectDeadlineAlerts = settings.deadlineAlerts
        DataManager.projectAnalyticsEnabled = settings.analyticsEnabled
        DataManager.projectTemplates.clear()
        DataManager.projectTemplates.putAll(settings.projectTemplates)
    }

    private fun updateTabUI(isProjects: Boolean) {
        // Logic for roadmaps/ideas enabled would normally come from settings
        // For now keeping it simple or assuming enabled
        findViewById<TextView>(R.id.tv_title).text = if (isProjects) "PROJECTS" else "IDEAS"
        listSection.setVisibility(isProjects)
        navigationSection.updateNavUI(isProjects)
        
        findViewById<View>(R.id.btn_edit_mode).visibility = if (isProjects) View.VISIBLE else View.GONE
        findViewById<View>(R.id.btn_workspace).visibility = if (isProjects) View.VISIBLE else View.GONE
        
        val bottomPadding = 100.dpToPx() 
        findViewById<View>(R.id.project_notes_list).setPadding(0, 0, 0, bottomPadding)
        findViewById<View>(R.id.project_ideas_list).setPadding(0, 0, 0, bottomPadding)
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        listSection.setEditMode(isEditMode)
        findViewById<ImageButton>(R.id.btn_edit_mode).imageTintList = ColorStateList.valueOf(
            if (isEditMode) Color.RED else Color.WHITE
        )
        Toast.makeText(this, if (isEditMode) "Edit Mode Active" else "Edit Mode Disabled", Toast.LENGTH_SHORT).show()
    }

    fun showDeleteProjectConfirmation(note: Note) {
        showStyledConfirmationDialog(
            title = "Delete Project",
            message = "Are you sure you want to delete '${note.title}'?",
            actionText = "DELETE",
            actionColor = Color.parseColor("#FF5252")
        ) {
            lifecycleScope.launch {
                viewModel.deleteProject(note)
                DataManager.saveData(this@ProjectActivity)
            }
        }
    }

    fun onProjectItemClick(note: Note) {
        if (note.category == "Project") {
            startActivity(Intent(this, ViewProjectActivity::class.java).apply {
                putExtra("PROJECT_ID", note.timestamp)
            })
        } else {
            showEditIdeaDialog(note)
        }
    }

    fun showEditIdeaDialog(note: Note) {
        startActivity(Intent(this, AddIdeaActivity::class.java).apply {
            putExtra("IDEA_ID", note.timestamp)
        })
    }

    fun showEditProjectNoteDialog(note: Note) {
        startActivity(Intent(this, EditProjectActivity::class.java).apply {
            putExtra("PROJECT_ID", note.timestamp)
        })
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun setupGestureDetector() {
        gestureDetector = android.view.GestureDetector(this, object : SwipeGestureListener() {
            override fun onSwipeLeft() {
                if (!viewModel.isProjectsTab.value) viewModel.setProjectsTab(true)
            }
            override fun onSwipeRight() {
                if (viewModel.isProjectsTab.value) viewModel.setProjectsTab(false)
            }
        })
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent?): Boolean {
        if (ev != null) gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    fun showProjectMenu(anchor: View, note: Note) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.menu_project_feature, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 20f

        menuView.findViewById<View>(R.id.menu_edit).setOnClickListener {
            popupWindow.dismiss()
            showEditProjectNoteDialog(note)
        }

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            popupWindow.dismiss()
            showDeleteProjectConfirmation(note)
        }
        
        // Hide unused options for simplicity
        menuView.findViewById<View>(R.id.menu_take_day_off).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_hide_unhide).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_undo).visibility = View.GONE

        popupWindow.showAsDropDown(anchor, 100, 0)
    }

    fun showProjectHistoryDialog(note: Note) {
        startActivity(Intent(this, ProjectHistoryActivity::class.java).apply {
            putExtra("PROJECT_ID", note.timestamp)
        })
    }

    override fun onResume() {
        super.onResume()
        themeManager.applyTheme()
    }
}
