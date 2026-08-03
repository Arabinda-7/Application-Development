package com.example.allinone.workspace.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.allinone.LocalAppStyle
import com.example.allinone.data.model.Note
import com.example.allinone.workspace.data.*
import com.example.allinone.workspace.ui.components.*
import com.example.allinone.workspace.ui.sections.*

/**
 * ProjectWorkspaceScreen: Screen-level composable managing workspace navigation, detail page routing,
 * and sidebar gesture interactions. Delegates UI components to WorkspaceSpeedDialFab,
 * WorkspaceBackgroundAura, WorkspaceDialogs, and WorkspaceTabContent.
 */
@Composable
fun ProjectWorkspaceScreen(
    viewModel: WorkspaceViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(WorkspaceTab.Dashboard) }
    var activeCreationPage by remember { mutableStateOf<WorkspaceAction?>(null) }
    var editingEntity by remember { mutableStateOf<Any?>(null) }
    var entityToDelete by remember { mutableStateOf<Any?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showTransferChoiceDialog by remember { mutableStateOf(false) }
    var pendingImportNote by remember { mutableStateOf<Note?>(null) }
    var isSidebarExpanded by remember { mutableStateOf(false) }
    var projectForStats by remember { mutableStateOf<ProjectEntity?>(null) }
    
    val style = LocalAppStyle.current
    val context = LocalContext.current

    BackHandler {
        if (activeCreationPage != null) {
            activeCreationPage = null
            editingEntity = null
        } else if (isSidebarExpanded) {
            isSidebarExpanded = false
        } else if (currentTab != WorkspaceTab.Dashboard) {
            currentTab = WorkspaceTab.Dashboard
        } else {
            onBack()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = style.backgroundColor
    ) {
        AnimatedContent(
            targetState = activeCreationPage,
            transitionSpec = {
                val duration = 300
                if (targetState != null) {
                    slideInHorizontally(animationSpec = tween(duration)) { it } + fadeIn(animationSpec = tween(duration)) togetherWith 
                    fadeOut(animationSpec = tween(duration))
                } else {
                    fadeIn(animationSpec = tween(duration)) togetherWith 
                    slideOutHorizontally(animationSpec = tween(duration)) { it } + fadeOut(animationSpec = tween(duration))
                }
            },
            label = "CreationPageTransition"
        ) { page ->
            if (page != null) {
                WorkspaceDetailRouter(
                    action = page,
                    viewModel = viewModel,
                    selectedProjectId = uiState.selectedProject?.id ?: "",
                    editingEntity = editingEntity,
                    onBack = { 
                        activeCreationPage = null 
                        editingEntity = null
                    },
                    onEditEntity = { entity, editAction ->
                        editingEntity = entity
                        activeCreationPage = editAction
                    },
                    onDeleteEntity = { entityToDelete = it }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(style.backgroundColor)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    if (isSidebarExpanded && dragAmount.x < -10) { isSidebarExpanded = false; change.consume() }
                                    else if (!isSidebarExpanded && change.position.x < 100 && dragAmount.x > 10) { isSidebarExpanded = true; change.consume() }
                                }
                            )
                        }
                ) {
                    WorkspaceBackgroundAura(selectedProject = uiState.selectedProject)

                    Scaffold(
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        floatingActionButton = {
                            WorkspaceSpeedDialFab(
                                currentTab = currentTab,
                                style = style,
                                onAction = { activeCreationPage = it },
                                onShowImportDialog = { showImportDialog = true }
                            )
                        }
                    ) { padding ->
                        val blurRadius by animateDpAsState(
                            targetValue = if (isSidebarExpanded || projectForStats != null) 8.dp else 0.dp,
                            label = "BlurRadius"
                        )

                        if (projectForStats != null) { 
                            projectForStats?.let { project ->
                                ProjectStatsDialog(
                                    project = project,
                                    viewModel = viewModel,
                                    onDismiss = { projectForStats = null }
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .blur(blurRadius)
                        ) {
                            if (uiState.isLoading && uiState.projects.isEmpty()) { 
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                                    CircularProgressIndicator(color = style.accentColor) 
                                } 
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    if (uiState.projects.isEmpty()) { 
                                        NoProjectsScreen(
                                            onAddProject = { activeCreationPage = WorkspaceAction.AddProject },
                                            onImportProject = { showImportDialog = true },
                                            onTrySample = { viewModel.createSampleProject() },
                                            onToggleMenu = { isSidebarExpanded = !isSidebarExpanded },
                                            isSidebarExpanded = isSidebarExpanded,
                                            modifier = Modifier.statusBarsPadding()
                                        ) 
                                    } else {
                                        WorkspaceHeader(
                                            selectedProject = uiState.selectedProject,
                                            projects = uiState.projects,
                                            currentTab = currentTab,
                                            onProjectSelected = { viewModel.selectProject(it) },
                                            onDeleteProject = { viewModel.deleteProject(it) },
                                            onEditProject = { 
                                                editingEntity = uiState.selectedProject
                                                activeCreationPage = WorkspaceAction.EditProject 
                                            },
                                            onImportRequest = { showImportDialog = true },
                                            isSidebarExpanded = isSidebarExpanded,
                                            onToggleMenu = { isSidebarExpanded = !isSidebarExpanded },
                                            modifier = Modifier.statusBarsPadding()
                                        )
                                        Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                                            WorkspaceTabContent(
                                                currentTab = currentTab,
                                                uiState = uiState,
                                                viewModel = viewModel,
                                                onShowStats = { projectForStats = it },
                                                isStatsShowing = projectForStats != null,
                                                onEditEntity = { entity, editAction ->
                                                    editingEntity = entity
                                                    activeCreationPage = editAction
                                                },
                                                onDeleteEntityRequest = { entityToDelete = it },
                                                onGraduateIdea = {
                                                    if (it is IdeaEntity) viewModel.graduateIdea(it)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        WorkspaceSidebar(
                            selectedTab = currentTab,
                            onTabSelected = { currentTab = it; isSidebarExpanded = false },
                            onBack = onBack,
                            isExpanded = isSidebarExpanded,
                            onToggleExpand = { isSidebarExpanded = !isSidebarExpanded }
                        )
                    }
                }
            }
        }

        WorkspaceDialogs(
            showImportDialog = showImportDialog,
            showTransferChoiceDialog = showTransferChoiceDialog,
            pendingImportNote = pendingImportNote,
            entityToDelete = entityToDelete,
            context = context,
            viewModel = viewModel,
            onDismissImport = { showImportDialog = false },
            onShowTransferChoice = { note ->
                pendingImportNote = note
                showImportDialog = false
                showTransferChoiceDialog = true
            },
            onDismissTransferChoice = {
                showTransferChoiceDialog = false
                pendingImportNote = null
            },
            onDismissDelete = {
                entityToDelete = null
                activeCreationPage = null
                editingEntity = null
            }
        )
    }
}
