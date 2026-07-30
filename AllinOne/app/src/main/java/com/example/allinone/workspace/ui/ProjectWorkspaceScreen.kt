package com.example.allinone.workspace.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.workspace.data.*
import com.example.allinone.LocalAppStyle
import com.example.allinone.workspace.ui.sections.*

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
    var pendingImportNote by remember { mutableStateOf<com.example.allinone.Note?>(null) }
    var isSidebarExpanded by remember { mutableStateOf(false) }
    var projectForStats by remember { mutableStateOf<ProjectEntity?>(null) }
    
    val style = LocalAppStyle.current
    val context = androidx.compose.ui.platform.LocalContext.current

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
                val targetColor = remember(uiState.selectedProject) {
                    val projectColor = uiState.selectedProject?.color ?: -1
                    val globalColor = com.example.allinone.DataManager.globalProjectColor
                    if (projectColor != -1) Color(projectColor)
                    else if (globalColor != -1) Color(globalColor)
                    else Color(0xFF1A73E8)
                }
                
                val baseColor by animateColorAsState(
                    targetValue = targetColor,
                    animationSpec = tween(300),
                    label = "BaseColorAnimation"
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    baseColor.copy(alpha = 0.25f),
                                    baseColor.copy(alpha = 0.12f),
                                    baseColor.copy(alpha = 0.06f),
                                    baseColor.copy(alpha = 0.02f),
                                    Color.Transparent
                                ),
                                center = androidx.compose.ui.geometry.Offset(x = 600f, y = 100f),
                                radius = 2500f
                            )
                        )
                )

                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    floatingActionButton = {
                        if (currentTab == WorkspaceTab.Dashboard) {
                            WorkspaceSpeedDial(onAction = { if (it == WorkspaceAction.ImportProject) showImportDialog = true else activeCreationPage = it })
                        } else {
                            val action = when(currentTab) {
                                WorkspaceTab.Goals -> WorkspaceAction.AddGoal
                                WorkspaceTab.Tasks -> WorkspaceAction.AddTask
                                WorkspaceTab.Notes -> WorkspaceAction.AddNote
                                WorkspaceTab.Features -> WorkspaceAction.AddFeature
                                WorkspaceTab.Bugs -> WorkspaceAction.AddBug
                                WorkspaceTab.Ideas -> WorkspaceAction.AddIdea
                                WorkspaceTab.Resources -> WorkspaceAction.AddResource
                                else -> null
                            }
                            action?.let { a ->
                                FloatingActionButton(onClick = { activeCreationPage = a }, containerColor = style.accentColor, contentColor = Color.White) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            }
                        }
                    }
                ) { padding ->
                    val blurRadius by animateDpAsState(targetValue = if (isSidebarExpanded || projectForStats != null) 8.dp else 0.dp, label = "BlurRadius")

                    if (projectForStats != null) { 
                        projectForStats?.let { project ->
                            ProjectStatsDialog(project = project, viewModel = viewModel, onDismiss = { projectForStats = null })
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize().padding(padding).blur(blurRadius)) {
                        if (uiState.isLoading && uiState.projects.isEmpty()) { 
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                                CircularProgressIndicator(color = style.accentColor) 
                            } 
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                if (uiState.projects.isEmpty()) { 
                                    NoProjectsScreen(onAddProject = { activeCreationPage = WorkspaceAction.AddProject }, onImportProject = { showImportDialog = true }, onTrySample = { viewModel.createSampleProject() }, onToggleMenu = { isSidebarExpanded = !isSidebarExpanded }, isSidebarExpanded = isSidebarExpanded, modifier = Modifier.statusBarsPadding()) 
                                } else {
                                    WorkspaceHeader(selectedProject = uiState.selectedProject, projects = uiState.projects, currentTab = currentTab, onProjectSelected = { viewModel.selectProject(it) }, onDeleteProject = { viewModel.deleteProject(it) }, onEditProject = { editingEntity = uiState.selectedProject; activeCreationPage = WorkspaceAction.EditProject }, onImportRequest = { showImportDialog = true }, isSidebarExpanded = isSidebarExpanded, onToggleMenu = { isSidebarExpanded = !isSidebarExpanded }, modifier = Modifier.statusBarsPadding())
                                    Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            Text(text = currentTab.title.uppercase(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp))
                                            AnimatedContent(
                                                targetState = currentTab,
                                                transitionSpec = {
                                                    val d = 250
                                                    fadeIn(tween(d)) togetherWith fadeOut(tween(d))
                                                },
                                                label = "TabTransition", 
                                                modifier = Modifier.weight(1f)
                                            ) { targetTab ->
                                                    Box(modifier = Modifier.fillMaxSize()) {
                                                        when (targetTab) {
                                                            WorkspaceTab.Dashboard -> WorkspaceDashboard(
                                                                state = uiState, 
                                                                viewModel = viewModel, 
                                                                onShowStats = { projectForStats = it }, 
                                                                isStatsShowing = projectForStats != null
                                                            )
                                                            WorkspaceTab.Goals -> GoalViewSection(goals = uiState.goals, onViewGoal = { editingEntity = it; activeCreationPage = WorkspaceAction.ViewGoal }, onEditGoal = { editingEntity = it; activeCreationPage = WorkspaceAction.EditGoal }, onDeleteGoal = { viewModel.deleteGoal(it) })
                                                            WorkspaceTab.Notes -> NoteViewSection(notes = uiState.notes, onViewNote = { editingEntity = it; activeCreationPage = WorkspaceAction.ViewNote }, onEditNote = { editingEntity = it; activeCreationPage = WorkspaceAction.EditNote }, onDeleteNote = { entityToDelete = it })
                                                            WorkspaceTab.Tasks -> TaskViewSection(tasks = uiState.tasks, onUpdateTask = { viewModel.updateTask(it) }, onViewTask = { editingEntity = it; activeCreationPage = WorkspaceAction.ViewTask }, onEditTask = { editingEntity = it; activeCreationPage = WorkspaceAction.EditTask }, onDeleteTask = { viewModel.deleteTask(it) })
                                                            WorkspaceTab.Features -> FeatureViewSection(features = uiState.features, viewModel = viewModel, tasks = uiState.tasks, onViewFeature = { editingEntity = it; activeCreationPage = WorkspaceAction.ViewFeature }, onEditFeature = { editingEntity = it; activeCreationPage = WorkspaceAction.EditFeature }, onDeleteFeature = { viewModel.deleteFeature(it) })
                                                            WorkspaceTab.Bugs -> BugViewSection(bugs = uiState.bugs, viewModel = viewModel, onViewBug = { editingEntity = it; activeCreationPage = WorkspaceAction.ViewBug }, onEditBug = { editingEntity = it; activeCreationPage = WorkspaceAction.EditBug }, onDeleteBug = { viewModel.deleteBug(it) })
                                                            WorkspaceTab.Ideas -> IdeaViewSection(ideas = uiState.ideas, onConvert = { viewModel.graduateIdea(it) }, onViewIdea = { editingEntity = it; activeCreationPage = WorkspaceAction.ViewIdea }, onEditIdea = { editingEntity = it; activeCreationPage = WorkspaceAction.EditIdea }, onDeleteIdea = { viewModel.deleteIdea(it) })
                                                            WorkspaceTab.Resources -> ResourceViewSection(resources = uiState.resources, onViewResource = { editingEntity = it; activeCreationPage = WorkspaceAction.ViewResource }, onEditResource = { editingEntity = it; activeCreationPage = WorkspaceAction.EditResource }, onDeleteResource = { viewModel.deleteResource(it) })
                                                            WorkspaceTab.ActivityLog -> ActivityLogSection(uiState.logs)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                WorkspaceSidebar(selectedTab = currentTab, onTabSelected = { currentTab = it; isSidebarExpanded = false }, onBack = onBack, isExpanded = isSidebarExpanded, onToggleExpand = { isSidebarExpanded = !isSidebarExpanded })
            }
        }
    }

    if (showImportDialog) { ImportSelectionDialog(onDismiss = { showImportDialog = false }, onImport = { pendingImportNote = it; showImportDialog = false; showTransferChoiceDialog = true }) }
    if (showTransferChoiceDialog && pendingImportNote != null) {
        TransferCopyChoiceDialog(onDismiss = { showTransferChoiceDialog = false; pendingImportNote = null }, onChoice = { isTransfer ->
            pendingImportNote?.let { note ->
                viewModel.importNote(note, isTransfer)
                if (isTransfer) com.example.allinone.DataManager.saveData(context)
            }
            showTransferChoiceDialog = false; pendingImportNote = null
        })
    }
    if (entityToDelete != null) {
        entityToDelete?.let { entity ->
            DeleteConfirmationDialog(entity = entity, onDismiss = { entityToDelete = null }, onConfirm = {
                when (entity) {
                    is NoteEntity -> viewModel.deleteNote(entity)
                    is TaskEntity -> viewModel.deleteTask(entity)
                    is GoalEntity -> viewModel.deleteGoal(entity)
                    is FeatureEntity -> viewModel.deleteFeature(entity)
                    is BugEntity -> viewModel.deleteBug(entity)
                    is IdeaEntity -> viewModel.deleteIdea(entity)
                    is ResourceEntity -> viewModel.deleteResource(entity)
                }
                entityToDelete = null; activeCreationPage = null; editingEntity = null
            })
        }
    }
}
}
