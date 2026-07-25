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

    AnimatedContent(
        targetState = activeCreationPage,
        transitionSpec = {
            val duration = 350
            if (targetState != null) {
                fadeIn(animationSpec = tween(duration)) + scaleIn(initialScale = 0.92f, animationSpec = tween(duration)) togetherWith fadeOut(animationSpec = tween(duration))
            } else {
                fadeIn(animationSpec = tween(duration)) togetherWith fadeOut(animationSpec = tween(duration)) + scaleOut(targetScale = 0.92f, animationSpec = tween(duration))
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
                val projectColorHex = com.example.allinone.DataManager.globalProjectColor
                val baseColor = if (projectColorHex != -1) Color(projectColorHex) else Color(0xFF1A73E8)
                
                Box(modifier = Modifier.fillMaxWidth().height(350.dp).background(brush = Brush.verticalGradient(colors = listOf(baseColor.copy(alpha = 0.4f), style.backgroundColor))))

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

                    if (projectForStats != null) { ProjectStatsDialog(project = projectForStats!!, viewModel = viewModel, onDismiss = { projectForStats = null }) }

                    Column(modifier = Modifier.fillMaxSize().padding(padding).blur(blurRadius)) {
                        Crossfade(targetState = uiState.isLoading, animationSpec = tween(500), label = "MainLoadingTransition") { loading ->
                            if (loading && uiState.projects.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = style.accentColor) } }
                            else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    if (uiState.projects.isEmpty()) { NoProjectsScreen(onAddProject = { activeCreationPage = WorkspaceAction.AddProject }, onImportProject = { showImportDialog = true }, onTrySample = { viewModel.createSampleProject() }, onToggleMenu = { isSidebarExpanded = !isSidebarExpanded }, isSidebarExpanded = isSidebarExpanded, modifier = Modifier.statusBarsPadding()) }
                                    else {
                                        WorkspaceHeader(selectedProject = uiState.selectedProject, projects = uiState.projects, currentTab = currentTab, onProjectSelected = { viewModel.selectProject(it) }, onDeleteProject = { viewModel.deleteProject(it) }, onEditProject = { editingEntity = uiState.selectedProject; activeCreationPage = WorkspaceAction.EditProject }, onImportRequest = { showImportDialog = true }, isSidebarExpanded = isSidebarExpanded, onToggleMenu = { isSidebarExpanded = !isSidebarExpanded }, modifier = Modifier.statusBarsPadding())
                                        Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                                            Column(modifier = Modifier.fillMaxSize()) {
                                                Text(text = currentTab.title.uppercase(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 12.dp))
                                                AnimatedContent(
                                                    targetState = currentTab,
                                                    transitionSpec = {
                                                        val d = 300; val o = 30
                                                        if (targetState.ordinal > initialState.ordinal) { fadeIn(tween(d)) + slideInHorizontally(tween(d)) { o } togetherWith fadeOut(tween(d)) + slideOutHorizontally(tween(d)) { -o } }
                                                        else { fadeIn(tween(d)) + slideInHorizontally(tween(d)) { -o } togetherWith fadeOut(tween(d)) + slideOutHorizontally(tween(d)) { o } }
                                                    },
                                                    label = "TabTransition", modifier = Modifier.weight(1f)
                                                ) { targetTab ->
                                                    Box(modifier = Modifier.fillMaxSize()) {
                                                        when (targetTab) {
                                                            WorkspaceTab.Dashboard -> WorkspaceDashboard(state = uiState, viewModel = viewModel, onViewFeature = { editingEntity = it; activeCreationPage = WorkspaceAction.ViewFeature }, onEditFeature = { editingEntity = it; activeCreationPage = WorkspaceAction.EditFeature }, onViewBug = { editingEntity = it; activeCreationPage = WorkspaceAction.ViewBug }, onEditBug = { editingEntity = it; activeCreationPage = WorkspaceAction.EditBug }, onShowStats = { projectForStats = it }, isStatsShowing = projectForStats != null)
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
                }
                WorkspaceSidebar(selectedTab = currentTab, onTabSelected = { currentTab = it; isSidebarExpanded = false }, onBack = onBack, isExpanded = isSidebarExpanded, onToggleExpand = { isSidebarExpanded = !isSidebarExpanded })
            }
        }
    }

    if (showImportDialog) { ImportSelectionDialog(onDismiss = { showImportDialog = false }, onImport = { pendingImportNote = it; showImportDialog = false; showTransferChoiceDialog = true }) }
    if (showTransferChoiceDialog && pendingImportNote != null) {
        TransferCopyChoiceDialog(onDismiss = { showTransferChoiceDialog = false; pendingImportNote = null }, onChoice = { isTransfer ->
            viewModel.importNote(pendingImportNote!!, isTransfer)
            if (isTransfer) com.example.allinone.DataManager.saveData(context)
            showTransferChoiceDialog = false; pendingImportNote = null
        })
    }
    if (entityToDelete != null) {
        DeleteConfirmationDialog(entity = entityToDelete!!, onDismiss = { entityToDelete = null }, onConfirm = {
            when (entityToDelete) {
                is NoteEntity -> viewModel.deleteNote(entityToDelete as NoteEntity)
                is TaskEntity -> viewModel.deleteTask(entityToDelete as TaskEntity)
                is GoalEntity -> viewModel.deleteGoal(entityToDelete as GoalEntity)
                is FeatureEntity -> viewModel.deleteFeature(entityToDelete as FeatureEntity)
                is BugEntity -> viewModel.deleteBug(entityToDelete as BugEntity)
                is IdeaEntity -> viewModel.deleteIdea(entityToDelete as IdeaEntity)
                is ResourceEntity -> viewModel.deleteResource(entityToDelete as ResourceEntity)
            }
            entityToDelete = null; activeCreationPage = null; editingEntity = null
        })
    }
}
