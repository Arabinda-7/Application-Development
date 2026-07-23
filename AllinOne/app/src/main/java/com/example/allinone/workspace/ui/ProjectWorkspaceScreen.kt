package com.example.allinone.workspace.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.workspace.data.*
import com.example.allinone.LocalAppStyle
import com.example.allinone.workspace.ui.sections.*

enum class WorkspaceTab(val title: String, val icon: ImageVector) {
    Dashboard("Dashboard", Icons.Default.Dashboard),
    Goals("Goals", Icons.Default.Flag),
    Tasks("Tasks", Icons.Default.Checklist),
    Notes("Notes", Icons.Default.Description),
    Features("Features", Icons.Default.Extension),
    Bugs("Bugs", Icons.Default.BugReport),
    Ideas("Ideas", Icons.Default.Lightbulb),
    Resources("Resources", Icons.Default.Folder),
    ActivityLog("Activity", Icons.Default.History)
}

enum class WorkspaceAction(val title: String, val icon: ImageVector) {
    AddProject("New Project", Icons.Default.CreateNewFolder),
    EditProject("Edit Project", Icons.Default.Edit),
    ImportProject("Import Project", Icons.Default.UploadFile),
    
    AddTask("Task", Icons.Default.Add),
    ViewTask("Task Details", Icons.Default.Description),
    EditTask("Edit Task", Icons.Default.Edit),
    
    AddGoal("Goal", Icons.Default.Flag),
    ViewGoal("Goal Details", Icons.Default.Description),
    EditGoal("Edit Goal", Icons.Default.Edit),
    
    AddFeature("Feature", Icons.Default.Extension),
    ViewFeature("Feature Details", Icons.Default.Description),
    EditFeature("Edit Feature", Icons.Default.Edit),
    
    AddBug("Bug", Icons.Default.BugReport),
    ViewBug("Bug Details", Icons.Default.Description),
    EditBug("Edit Bug", Icons.Default.Edit),
    
    AddIdea("Idea", Icons.Default.Lightbulb),
    ViewIdea("Idea Details", Icons.Default.Description),
    EditIdea("Edit Idea", Icons.Default.Edit),
    
    AddNote("Note", Icons.Default.NoteAdd),
    ViewNote("Note Details", Icons.Default.Description),
    EditNote("Edit Note", Icons.Default.Edit),
    
    AddResource("Resource", Icons.Default.Link),
    ViewResource("Resource Details", Icons.Default.Description),
    EditResource("Edit Resource", Icons.Default.Edit)
}

@Composable
fun ProjectWorkspaceScreen(
    viewModel: WorkspaceViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(WorkspaceTab.Dashboard) }
    var activeCreationPage by remember { mutableStateOf<WorkspaceAction?>(null) }
    var editingEntity by remember { mutableStateOf<Any?>(null) }
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
                // Opening details/add section: Scale up and fade in
                fadeIn(animationSpec = tween(duration)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(duration)) togetherWith
                fadeOut(animationSpec = tween(duration))
            } else {
                // Closing: Scale down and fade out
                fadeIn(animationSpec = tween(duration)) togetherWith
                fadeOut(animationSpec = tween(duration)) +
                        scaleOut(targetScale = 0.92f, animationSpec = tween(duration))
            }
        },
        label = "CreationPageTransition"
    )
{ page ->
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
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(style.backgroundColor)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                if (isSidebarExpanded && dragAmount.x < -10) {
                                    isSidebarExpanded = false
                                    change.consume()
                                } else if (!isSidebarExpanded && change.position.x < 100 && dragAmount.x > 10) {
                                    isSidebarExpanded = true
                                    change.consume()
                                }
                            }
                        )
                    }
            ) {
                val projectColorHex = com.example.allinone.DataManager.globalProjectColor
                val baseColor = if (projectColorHex != -1) Color(projectColorHex) else Color(0xFF1A73E8)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    baseColor.copy(alpha = 0.4f),
                                    style.backgroundColor
                                )
                            )
                        )
                )

                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    floatingActionButton = {
                        val action = when(currentTab) {
                            WorkspaceTab.Dashboard -> null
                            WorkspaceTab.Goals -> WorkspaceAction.AddGoal
                            WorkspaceTab.Tasks -> WorkspaceAction.AddTask
                            WorkspaceTab.Notes -> WorkspaceAction.AddNote
                            WorkspaceTab.Features -> WorkspaceAction.AddFeature
                            WorkspaceTab.Bugs -> WorkspaceAction.AddBug
                            WorkspaceTab.Ideas -> WorkspaceAction.AddIdea
                            WorkspaceTab.Resources -> WorkspaceAction.AddResource
                            WorkspaceTab.ActivityLog -> null
                        }

                        if (currentTab == WorkspaceTab.Dashboard) {
                            WorkspaceSpeedDial(onAction = {
                                if (it == WorkspaceAction.ImportProject) {
                                    showImportDialog = true
                                } else {
                                    activeCreationPage = it
                                }
                            })
                        } else if (action != null) {
                            FloatingActionButton(
                                onClick = { activeCreationPage = action },
                                containerColor = style.accentColor,
                                contentColor = Color.White
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add ${action.title}")
                            }
                        }
                    }
                ) { padding ->
                    val blurRadius by animateDpAsState(
                        targetValue = if (isSidebarExpanded || projectForStats != null) 8.dp else 0.dp,
                        label = "BlurRadius"
                    )

                    if (projectForStats != null) {
                        ProjectStatsDialog(
                            project = projectForStats!!,
                            viewModel = viewModel,
                            onDismiss = { projectForStats = null }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .blur(blurRadius)
                    ) {
                        Crossfade(
                            targetState = uiState.isLoading,
                            animationSpec = tween(500),
                            label = "MainLoadingTransition"
                        ) { loading ->
                            if (loading && uiState.projects.isEmpty()) {
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
                                            Column(modifier = Modifier.fillMaxSize()) {
                                                Text(
                                                    text = currentTab.title.uppercase(),
                                                    color = Color.White,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 1.sp,
                                                    modifier = Modifier.padding(bottom = 12.dp)
                                                )
                                                AnimatedContent(
                                                    targetState = currentTab,
                                                    transitionSpec = {
                                                        val duration = 300
                                                        val offset = 30
                                                        if (targetState.ordinal > initialState.ordinal) {
                                                            fadeIn(animationSpec = tween(duration)) +
                                                                    slideInHorizontally(animationSpec = tween(duration)) { offset } togetherWith
                                                            fadeOut(animationSpec = tween(duration)) +
                                                                    slideOutHorizontally(animationSpec = tween(duration)) { -offset }
                                                        } else {
                                                            fadeIn(animationSpec = tween(duration)) +
                                                                    slideInHorizontally(animationSpec = tween(duration)) { -offset } togetherWith
                                                            fadeOut(animationSpec = tween(duration)) +
                                                                    slideOutHorizontally(animationSpec = tween(duration)) { offset }
                                                        }
                                                    },
                                                    label = "TabTransition",
                                                    modifier = Modifier.weight(1f)
                                                ) { targetTab ->
                                                    Box(modifier = Modifier.fillMaxSize()) {
                                                        when (targetTab) {
                                                            WorkspaceTab.Dashboard -> WorkspaceDashboard(
                                                                state = uiState,
                                                                viewModel = viewModel,
                                                                onViewFeature = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.ViewFeature
                                                                },
                                                                onEditFeature = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.EditFeature
                                                                },
                                                                onViewBug = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.ViewBug
                                                                },
                                                                onEditBug = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.EditBug
                                                                },
                                                                onShowStats = { projectForStats = it },
                                                                isStatsShowing = projectForStats != null
                                                            )
                                                            WorkspaceTab.Goals -> GoalViewSection(
                                                                goals = uiState.goals,
                                                                onViewGoal = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.ViewGoal
                                                                },
                                                                onEditGoal = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.EditGoal
                                                                },
                                                                onDeleteGoal = { viewModel.deleteGoal(it) }
                                                            )
                                                            WorkspaceTab.Notes -> NoteViewSection(
                                                                notes = uiState.notes,
                                                                onViewNote = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.ViewNote
                                                                },
                                                                onEditNote = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.EditNote
                                                                },
                                                                onDeleteNote = { viewModel.deleteNote(it) }
                                                            )
                                                            WorkspaceTab.Tasks -> TaskViewSection(
                                                                tasks = uiState.tasks,
                                                                onUpdateTask = { viewModel.updateTask(it) },
                                                                onViewTask = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.ViewTask
                                                                },
                                                                onEditTask = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.EditTask
                                                                },
                                                                onDeleteTask = { viewModel.deleteTask(it) }
                                                            )
                                                            WorkspaceTab.Features -> FeatureViewSection(
                                                                features = uiState.features,
                                                                viewModel = viewModel,
                                                                tasks = uiState.tasks,
                                                                onViewFeature = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.ViewFeature
                                                                },
                                                                onEditFeature = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.EditFeature
                                                                },
                                                                onDeleteFeature = { viewModel.deleteFeature(it) }
                                                            )
                                                            WorkspaceTab.Bugs -> BugViewSection(
                                                                bugs = uiState.bugs,
                                                                viewModel = viewModel,
                                                                onViewBug = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.ViewBug
                                                                },
                                                                onEditBug = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.EditBug
                                                                },
                                                                onDeleteBug = { viewModel.deleteBug(it) }
                                                            )
                                                            WorkspaceTab.Ideas -> IdeaViewSection(
                                                                ideas = uiState.ideas,
                                                                onConvert = { viewModel.graduateIdea(it) },
                                                                onViewIdea = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.ViewIdea
                                                                },
                                                                onEditIdea = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.EditIdea
                                                                },
                                                                onDeleteIdea = { viewModel.deleteIdea(it) }
                                                            )
                                                            WorkspaceTab.Resources -> ResourceViewSection(
                                                                resources = uiState.resources,
                                                                onViewResource = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.ViewResource
                                                                },
                                                                onEditResource = {
                                                                    editingEntity = it
                                                                    activeCreationPage = WorkspaceAction.EditResource
                                                                },
                                                                onDeleteResource = { viewModel.deleteResource(it) }
                                                            )
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

                WorkspaceSidebar(
                    selectedTab = currentTab,
                    onTabSelected = { 
                        currentTab = it
                        isSidebarExpanded = false
                    },
                    onBack = onBack,
                    isExpanded = isSidebarExpanded,
                    onToggleExpand = { isSidebarExpanded = !isSidebarExpanded }
                )
            }
        }
    }

    if (showImportDialog) {
        ImportSelectionDialog(
            onDismiss = { showImportDialog = false },
            onImport = { note ->
                pendingImportNote = note
                showImportDialog = false
                showTransferChoiceDialog = true
            }
        )
    }

    if (showTransferChoiceDialog && pendingImportNote != null) {
        TransferCopyChoiceDialog(
            onDismiss = { 
                showTransferChoiceDialog = false
                pendingImportNote = null
            },
            onChoice = { isTransfer ->
                viewModel.importNote(pendingImportNote!!, isTransfer)
                if (isTransfer) {
                    com.example.allinone.DataManager.saveData(context)
                }
                showTransferChoiceDialog = false
                pendingImportNote = null
            }
        )
    }
}

@Composable
fun WorkspaceSpeedDial(onAction: (WorkspaceAction) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val style = LocalAppStyle.current

    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(horizontalAlignment = Alignment.End) {
                listOf(WorkspaceAction.AddProject, WorkspaceAction.ImportProject).forEach { action ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp).clickable {
                            onAction(action)
                            expanded = false
                        }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = style.surfaceColor,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(action.title, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontSize = 14.sp)
                        }
                        FloatingActionButton(
                            onClick = {
                                onAction(action)
                                expanded = false
                            },
                            containerColor = style.accentColor,
                            contentColor = Color.White,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(action.icon, contentDescription = action.title, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = style.accentColor,
            contentColor = Color.White
        ) {
            Icon(if (expanded) Icons.Default.Close else Icons.Default.Add, contentDescription = "Add")
        }
    }
}

@Composable
fun WorkspaceDetailRouter(
    action: WorkspaceAction,
    viewModel: WorkspaceViewModel,
    selectedProjectId: String,
    editingEntity: Any? = null,
    onBack: () -> Unit,
    onEditEntity: (Any, WorkspaceAction) -> Unit
) {
    when (action) {
        WorkspaceAction.AddProject, WorkspaceAction.EditProject -> {
            ProjectAddEditSection(
                project = editingEntity as? ProjectEntity,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        
        WorkspaceAction.AddGoal, WorkspaceAction.EditGoal -> {
            GoalAddEditSection(
                goal = editingEntity as? GoalEntity,
                projectId = selectedProjectId,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        WorkspaceAction.ViewGoal -> {
            (editingEntity as? GoalEntity)?.let {
                GoalDetailSection(goal = it, onBack = onBack, onEdit = { onEditEntity(it, WorkspaceAction.EditGoal) })
            }
        }

        WorkspaceAction.AddTask, WorkspaceAction.EditTask -> {
            TaskAddEditSection(
                task = editingEntity as? TaskEntity,
                projectId = selectedProjectId,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        WorkspaceAction.ViewTask -> {
            (editingEntity as? TaskEntity)?.let {
                TaskDetailSection(
                    task = it,
                    onBack = onBack,
                    onEdit = { onEditEntity(it, WorkspaceAction.EditTask) },
                    onUpdateTask = { viewModel.updateTask(it) }
                )
            }
        }

        WorkspaceAction.AddFeature, WorkspaceAction.EditFeature -> {
            FeatureAddEditSection(
                feature = editingEntity as? FeatureEntity,
                projectId = selectedProjectId,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        WorkspaceAction.ViewFeature -> {
            (editingEntity as? FeatureEntity)?.let {
                FeatureDetailSection(feature = it, onBack = onBack, onEdit = { onEditEntity(it, WorkspaceAction.EditFeature) })
            }
        }

        WorkspaceAction.AddBug, WorkspaceAction.EditBug -> {
            BugAddEditSection(
                bug = editingEntity as? BugEntity,
                projectId = selectedProjectId,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        WorkspaceAction.ViewBug -> {
            (editingEntity as? BugEntity)?.let {
                BugDetailSection(bug = it, onBack = onBack, onEdit = { onEditEntity(it, WorkspaceAction.EditBug) })
            }
        }

        WorkspaceAction.AddIdea, WorkspaceAction.EditIdea -> {
            IdeaAddEditSection(
                idea = editingEntity as? IdeaEntity,
                projectId = selectedProjectId,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        WorkspaceAction.ViewIdea -> {
            (editingEntity as? IdeaEntity)?.let {
                IdeaDetailSection(idea = it, onBack = onBack, onEdit = { onEditEntity(it, WorkspaceAction.EditIdea) })
            }
        }

        WorkspaceAction.AddNote, WorkspaceAction.EditNote -> {
            NoteAddEditSection(
                note = editingEntity as? NoteEntity,
                projectId = selectedProjectId,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        WorkspaceAction.ViewNote -> {
            (editingEntity as? NoteEntity)?.let {
                NoteDetailSection(note = it, onBack = onBack, onEdit = { onEditEntity(it, WorkspaceAction.EditNote) })
            }
        }

        WorkspaceAction.AddResource, WorkspaceAction.EditResource -> {
            ResourceAddEditSection(
                resource = editingEntity as? ResourceEntity,
                projectId = selectedProjectId,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        WorkspaceAction.ViewResource -> {
            (editingEntity as? ResourceEntity)?.let {
                ResourceDetailSection(resource = it, onBack = onBack, onEdit = { onEditEntity(it, WorkspaceAction.EditResource) })
            }
        }
        
        else -> {}
    }
}

@Composable
fun WorkspaceSidebar(selectedTab: WorkspaceTab, onTabSelected: (WorkspaceTab) -> Unit, onBack: () -> Unit, isExpanded: Boolean, onToggleExpand: () -> Unit) {
    val style = LocalAppStyle.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val expandedWidth = configuration.screenWidthDp.dp / 4
    val sidebarWidth by animateDpAsState(targetValue = if (isExpanded) expandedWidth else 0.dp, animationSpec = spring(stiffness = Spring.StiffnessLow), label = "SidebarWidth")
    val sidebarBg by animateColorAsState(targetValue = if (isExpanded) style.surfaceColor else Color.Transparent, label = "SidebarBg")

    if (isExpanded || sidebarWidth > 0.dp) {
        if (isExpanded) { Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)).clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) { onToggleExpand() }) }
        Surface(color = sidebarBg, modifier = Modifier.fillMaxHeight().width(sidebarWidth).shadow(if (isExpanded) 12.dp else 0.dp)) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.Start) {
                Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(64.dp).clickable { onToggleExpand() }, contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.MenuOpen, contentDescription = "Close Sidebar", tint = Color.White)
                        if (isExpanded) { Spacer(modifier = Modifier.height(4.dp)); Text("SECTIONS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp) }
                    }
                }
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
                        WorkspaceTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            val contentColor = if (isSelected) style.accentColor else Color.White.copy(alpha = 0.5f)
                            Surface(onClick = { onTabSelected(tab) }, color = if (isSelected) style.accentColor.copy(alpha = 0.1f) else Color.Transparent, modifier = Modifier.fillMaxWidth().height(72.dp)) {
                                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = tab.icon, contentDescription = tab.title, tint = contentColor, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = tab.title, color = contentColor, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp).clickable { onBack() }, contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Workspace", tint = Color.White.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("EXIT", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun NoProjectsScreen(onAddProject: () -> Unit, onImportProject: () -> Unit, onTrySample: () -> Unit, onToggleMenu: () -> Unit, isSidebarExpanded: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp).height(48.dp), contentAlignment = Alignment.CenterStart) {
            if (!isSidebarExpanded) { IconButton(onClick = onToggleMenu) { Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White) } }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("No Workspaces Yet", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Create a project or import an existing one to start.", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, modifier = Modifier.padding(horizontal = 32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onAddProject, modifier = Modifier.fillMaxWidth(0.6f)) { Icon(Icons.Default.Add, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Create First Project") }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onImportProject, modifier = Modifier.fillMaxWidth(0.6f)) { Icon(Icons.Default.UploadFile, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Import from AllInOne") }
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onTrySample) { Text("Try a Sample Project", color = Color.White.copy(alpha = 0.4f)) }
        }
    }
}

@Composable
fun WorkspaceHeader(selectedProject: ProjectEntity?, projects: List<ProjectEntity>, currentTab: WorkspaceTab, onProjectSelected: (String) -> Unit, onDeleteProject: (ProjectEntity) -> Unit, onEditProject: () -> Unit, onImportRequest: () -> Unit, isSidebarExpanded: Boolean, onToggleMenu: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            val menuButtonWidth by animateDpAsState(
                targetValue = if (isSidebarExpanded) 0.dp else 56.dp,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "MenuButtonWidth"
            )
            Box(modifier = Modifier.width(menuButtonWidth), contentAlignment = Alignment.CenterStart) {
                if (menuButtonWidth > 10.dp) {
                    IconButton(onClick = onToggleMenu) { Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White) }
                }
            }
            Column {
                Text("Workspace", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                Text(selectedProject?.name ?: "Select Project", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }
        var showProjectMenu by remember { mutableStateOf(false) }
        Box {
            if (currentTab == WorkspaceTab.Dashboard) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditProject, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Project", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onImportRequest) { Icon(Icons.Default.UploadFile, contentDescription = "Import", tint = Color.White.copy(alpha = 0.7f)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { showProjectMenu = true }, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp)); Text("Switch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            WorkspaceDropdown(expanded = showProjectMenu, onDismissRequest = { showProjectMenu = false }) {
                projects.forEach { project ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(project.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = Color.White)
                                IconButton(onClick = { onDeleteProject(project); showProjectMenu = false }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        onClick = { onProjectSelected(project.id); showProjectMenu = false }
                    )
                }
            }
        }
    }
}

@Composable
fun ImportSelectionDialog(onDismiss: () -> Unit, onImport: (com.example.allinone.Note) -> Unit) {
    val notes = remember { com.example.allinone.DataManager.projects.filter { it.category == "Project" || it.category == "ProjectIdea" || it.subFeatures.isNotEmpty() } }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Import Project or Idea", color = Color.White) }, containerColor = Color(0xFF1A1A1A), text = {
        if (notes.isEmpty()) { Text("No existing projects or ideas found to import.", color = Color.White.copy(alpha = 0.6f)) }
        else { LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) { items(notes) { note -> Surface(onClick = { onImport(note) }, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) { Row(modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) { Icon(imageVector = if (note.category == "Project") Icons.Default.Folder else Icons.Default.Lightbulb, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(16.dp)); Column { Text(note.title, color = Color.White, fontWeight = FontWeight.Bold); Text("${note.subFeatures.size} Milestones | ${note.category}", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp) } } } } } }
    }, confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("CLOSE") } })
}

@Composable
fun TransferCopyChoiceDialog(onDismiss: () -> Unit, onChoice: (Boolean) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Workspace Import", color = Color.White, fontWeight = FontWeight.Black) },
        containerColor = Color(0xFF1A1A1A),
        text = {
            Column {
                Text(
                    "How would you like to bring this project into your workspace?",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    onClick = { onChoice(true) },
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MoveUp, contentDescription = null, tint = Color(0xFFFFB800))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Transfer", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Moves the project to Workspace and removes it from the main Projects tab. (No sync link)",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    onClick = { onChoice(false) },
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF1A73E8))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Copy", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Creates a duplicate in Workspace. Original project stays in the main tab. (No sync link)",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.White.copy(alpha = 0.5f))
            }
        }
    )
}
