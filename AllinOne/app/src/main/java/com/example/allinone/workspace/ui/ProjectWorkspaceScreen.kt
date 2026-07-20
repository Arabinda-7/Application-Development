package com.example.allinone.workspace.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.workspace.data.*
import com.example.allinone.LocalAppStyle

@Composable
fun ProjectWorkspaceScreen(
    viewModel: WorkspaceViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(WorkspaceTab.Dashboard) }
    var activeCreationPage by remember { mutableStateOf<WorkspaceAction?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var isSidebarExpanded by remember { mutableStateOf(false) }
    
    val style = LocalAppStyle.current

    AnimatedContent(
        targetState = activeCreationPage,
        transitionSpec = {
            if (targetState != null) {
                slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
            } else {
                slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
            }
        },
        label = "CreationPageTransition"
    ) { page ->
        if (page != null) {
            WorkspaceCreationScreen(
                action = page,
                viewModel = viewModel,
                selectedProjectId = uiState.selectedProject?.id ?: "",
                onBack = { activeCreationPage = null }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(style.backgroundColor)
                    .statusBarsPadding()
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
                Scaffold(
                    containerColor = Color.Transparent,
                    floatingActionButton = {
                        WorkspaceSpeedDial(onAction = {
                            if (it == WorkspaceAction.ImportProject) {
                                showImportDialog = true
                            } else {
                                activeCreationPage = it
                            }
                        })
                    }
                ) { padding ->
                    val blurRadius by animateDpAsState(
                        targetValue = if (isSidebarExpanded) 8.dp else 0.dp,
                        label = "BlurRadius"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .blur(blurRadius)
                    ) {
                        if (uiState.projects.isEmpty()) {
                            NoProjectsScreen(
                                onAddProject = { activeCreationPage = WorkspaceAction.AddProject },
                                onImportProject = { showImportDialog = true },
                                onTrySample = { viewModel.createSampleProject() },
                                onToggleMenu = { isSidebarExpanded = !isSidebarExpanded },
                                isSidebarExpanded = isSidebarExpanded
                            )
                        } else {
                            WorkspaceHeader(
                                selectedProject = uiState.selectedProject,
                                projects = uiState.projects,
                                onProjectSelected = { viewModel.selectProject(it) },
                                onDeleteProject = { viewModel.deleteProject(it) },
                                onImportRequest = { showImportDialog = true },
                                isSidebarExpanded = isSidebarExpanded,
                                onToggleMenu = { isSidebarExpanded = !isSidebarExpanded }
                            )

                        Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = currentTab.title.uppercase(),
                                    color = style.accentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Box(modifier = Modifier.weight(1f)) {
                                    when (currentTab) {
                                        WorkspaceTab.Dashboard -> WorkspaceDashboard(uiState, viewModel)
                                        WorkspaceTab.Goals -> GoalsTree(uiState.goals)
                                        WorkspaceTab.Notes -> NotesView(uiState.notes)
                                        WorkspaceTab.Tasks -> TasksKanban(uiState.tasks, onUpdateTask = { viewModel.updateTask(it) })
                                        WorkspaceTab.Features -> FeaturePlanner(uiState.features)
                                        WorkspaceTab.Bugs -> BugTracker(uiState.bugs)
                                        WorkspaceTab.Ideas -> IdeaBacklog(uiState.ideas, onConvert = { viewModel.convertIdeaToTask(it) })
                                        WorkspaceTab.Resources -> ResourceDirectory(uiState.resources)
                                        WorkspaceTab.ActivityLog -> ActivityLogView(uiState.logs)
                                    }
                                }
                            }
                        }
                        }
                    }
                }

                // Floating Sidebar
                WorkspaceSidebar(
                    selectedTab = currentTab,
                    onTabSelected = { 
                        currentTab = it
                        isSidebarExpanded = false // Optional: collapse on selection
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
                viewModel.importNote(note)
                showImportDialog = false
            }
        )
    }
}

enum class WorkspaceAction(val title: String, val icon: ImageVector) {
    AddProject("New Project", Icons.Default.CreateNewFolder),
    ImportProject("Import Project", Icons.Default.UploadFile),
    AddTask("Task", Icons.Default.Add),
    AddGoal("Goal", Icons.Default.Flag),
    AddFeature("Feature", Icons.Default.Extension),
    AddBug("Bug", Icons.Default.BugReport),
    AddIdea("Idea", Icons.Default.Lightbulb),
    AddNote("Note", Icons.Default.NoteAdd),
    AddResource("Resource", Icons.Default.Link)
}

@Composable
fun WorkspaceSpeedDial(onAction: (WorkspaceAction) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val style = LocalAppStyle.current

    Column(horizontalAlignment = Alignment.End) {
        if (expanded) {
            WorkspaceAction.values().forEach { action ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp).clickable {
                        onAction(action)
                        expanded = false
                    }
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = style.surfaceColor,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(action.title, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White, fontSize = 12.sp)
                    }
                    FloatingActionButton(
                        onClick = {
                            onAction(action)
                            expanded = false
                        },
                        containerColor = style.accentColor,
                        contentColor = Color.White,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(action.icon, contentDescription = action.title, modifier = Modifier.size(20.dp))
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
fun WorkspaceCreationScreen(
    action: WorkspaceAction,
    viewModel: WorkspaceViewModel,
    selectedProjectId: String,
    onBack: () -> Unit
) {
    val style = LocalAppStyle.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // Entity-Specific States
    var priority by remember { mutableStateOf(1) } // 0, 1, 2
    var severity by remember { mutableStateOf("Medium") }
    var impact by remember { mutableStateOf(3f) }
    var difficulty by remember { mutableStateOf(3f) }
    var colorInt by remember { mutableStateOf(-1) }
    var iconName by remember { mutableStateOf("") }
    var resourceType by remember { mutableStateOf("URL") }
    var resourcePath by remember { mutableStateOf("") }
    var complexity by remember { mutableStateOf("Medium") }
    var steps by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(style.backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Custom Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New ${action.title}",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }
            
            Button(
                onClick = {
                    when (action) {
                        WorkspaceAction.AddProject -> viewModel.addProject(title, description, colorInt, iconName)
                        WorkspaceAction.AddTask -> viewModel.addTask(title, selectedProjectId, description, priority)
                        WorkspaceAction.AddGoal -> viewModel.addGoal(title, selectedProjectId, description)
                        WorkspaceAction.AddFeature -> viewModel.addFeature(title, selectedProjectId, description, complexity)
                        WorkspaceAction.AddBug -> viewModel.addBug(title, selectedProjectId, description, severity, steps)
                        WorkspaceAction.AddIdea -> viewModel.addIdea(title, selectedProjectId, description, impact.toInt(), difficulty.toInt())
                        WorkspaceAction.AddNote -> viewModel.addNote(title, description, selectedProjectId)
                        WorkspaceAction.AddResource -> viewModel.addResource(title, resourceType, resourcePath, selectedProjectId)
                        else -> {}
                    }
                    onBack()
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = style.accentColor)
            ) {
                Text("CREATE", fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("General Info", color = style.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = style.accentColor,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(if (action == WorkspaceAction.AddNote) "Content (Markdown)" else "Description") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = style.accentColor,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Entity-Specific Forms
            when (action) {
                WorkspaceAction.AddTask -> {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Priority", color = style.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("LOW", "MED", "HIGH").forEachIndexed { index, label ->
                            val isSel = priority == index
                            val color = when(index) {
                                2 -> Color.Red
                                1 -> Color(0xFFFFB800)
                                else -> Color(0xFF2EC4B6)
                            }
                            Surface(
                                onClick = { priority = index },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) color else color.copy(alpha = 0.1f),
                                border = if (isSel) null else BorderStroke(1.dp, color.copy(alpha = 0.2f)),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(label, color = if (isSel) Color.Black else color, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
                WorkspaceAction.AddBug -> {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Severity", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Low", "Medium", "High", "Critical").forEach { label ->
                            val isSel = severity == label
                            Surface(
                                onClick = { severity = label },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) Color.Red else Color.Red.copy(alpha = 0.1f),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(label.uppercase(), color = if (isSel) Color.White else Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = steps,
                        onValueChange = { steps = it },
                        label = { Text("Steps to Reproduce") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
                WorkspaceAction.AddIdea -> {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Impact (1-5)", color = style.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Slider(value = impact, onValueChange = { impact = it }, valueRange = 1f..5f, steps = 3)
                    Text("Difficulty (1-5)", color = style.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Slider(value = difficulty, onValueChange = { difficulty = it }, valueRange = 1f..5f, steps = 3)
                }
                WorkspaceAction.AddProject -> {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Theme Color", color = style.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    val colors = listOf(0xFFFF7A59, 0xFFFFB800, 0xFF2EC4B6, 0xFF3A86F0, 0xFF1A73E8, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF4CAF50)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        colors.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(c))
                                    .border(if (colorInt == c.toInt()) 2.dp else 0.dp, Color.White, CircleShape)
                                    .clickable { colorInt = c.toInt() }
                            )
                        }
                    }
                }
                WorkspaceAction.AddResource -> {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Type", color = style.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf("FILE", "URL", "CONTACT").forEach { type ->
                            FilterChip(
                                selected = resourceType == type,
                                onClick = { resourceType = type },
                                label = { Text(type) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resourcePath,
                        onValueChange = { resourcePath = it },
                        label = { Text("URL or Path") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
                WorkspaceAction.AddFeature -> {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Complexity", color = style.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf("Low", "Medium", "High").forEach { c ->
                            FilterChip(
                                selected = complexity == c,
                                onClick = { complexity = c },
                                label = { Text(c) }
                            )
                        }
                    }
                }
                else -> {}
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

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

@Composable
fun WorkspaceSidebar(
    selectedTab: WorkspaceTab,
    onTabSelected: (WorkspaceTab) -> Unit,
    onBack: () -> Unit,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val style = LocalAppStyle.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val expandedWidth = configuration.screenWidthDp.dp / 4
    
    val sidebarWidth by animateDpAsState(
        targetValue = if (isExpanded) expandedWidth else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "SidebarWidth"
    )

    val sidebarBg by animateColorAsState(
        targetValue = if (isExpanded) style.surfaceColor else Color.Transparent,
        label = "SidebarBg"
    )

    if (isExpanded || sidebarWidth > 0.dp) {
        // Scrim / Background Dimmer
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { onToggleExpand() }
            )
        }

        Surface(
            color = sidebarBg,
            modifier = Modifier
                .fillMaxHeight()
                .width(sidebarWidth)
                .shadow(if (isExpanded) 12.dp else 0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start
            ) {
                // Sidebar Header (Alignment Match)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp) // Adjusted to align toggle button with Dashboard title
                        .clickable { onToggleExpand() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                            contentDescription = "Close Sidebar",
                            tint = Color.White
                        )
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "SECTIONS",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .weight(1f)
                    ) {
                        WorkspaceTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            val contentColor = if (isSelected) style.accentColor else Color.White.copy(alpha = 0.5f)
                            
                            Surface(
                                onClick = { onTabSelected(tab) },
                                color = if (isSelected) style.accentColor.copy(alpha = 0.1f) else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        tint = contentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = tab.title,
                                        color = contentColor,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Exit Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Exit Workspace",
                                tint = Color.White.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "EXIT",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun NoProjectsScreen(
    onAddProject: () -> Unit,
    onImportProject: () -> Unit,
    onTrySample: () -> Unit,
    onToggleMenu: () -> Unit,
    isSidebarExpanded: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating Menu Icon Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (!isSidebarExpanded) {
                IconButton(onClick = onToggleMenu) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CreateNewFolder,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No Workspaces Yet",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Create a project or import an existing one to start.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onAddProject,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create First Project")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = onImportProject,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import from AllInOne")
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            TextButton(onClick = onTrySample) {
                Text("Try a Sample Project", color = Color.White.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
fun WorkspaceHeader(
    selectedProject: ProjectEntity?,
    projects: List<ProjectEntity>,
    onProjectSelected: (String) -> Unit,
    onDeleteProject: (ProjectEntity) -> Unit,
    onImportRequest: () -> Unit,
    isSidebarExpanded: Boolean,
    onToggleMenu: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isSidebarExpanded) {
                IconButton(onClick = onToggleMenu) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Column {
                Text("Workspace", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                Text(selectedProject?.name ?: "Select Project", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        var showProjectMenu by remember { mutableStateOf(false) }
        Box {
            Row {
                IconButton(onClick = onImportRequest) {
                    Icon(Icons.Default.UploadFile, contentDescription = "Import", tint = Color.White.copy(alpha = 0.7f))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { showProjectMenu = true }) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Switch")
                }
            }
            DropdownMenu(expanded = showProjectMenu, onDismissRequest = { showProjectMenu = false }) {
                projects.forEach { project ->
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(project.name, modifier = Modifier.weight(1f))
                                IconButton(onClick = { onDeleteProject(project) }) {
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
fun ImportSelectionDialog(
    onDismiss: () -> Unit,
    onImport: (com.example.allinone.Note) -> Unit
) {
    val notes = remember { 
        com.example.allinone.DataManager.notes.filter { 
            it.category == "Project" || it.category == "ProjectIdea" || it.subFeatures.isNotEmpty() 
        } 
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Project or Idea", color = Color.White) },
        containerColor = Color(0xFF1A1A1A),
        text = {
            if (notes.isEmpty()) {
                Text("No existing projects or ideas found to import.", color = Color.White.copy(alpha = 0.6f))
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(notes) { note ->
                        Surface(
                            onClick = { onImport(note) },
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (note.category == "Project") Icons.Default.Folder else Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(note.title, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("${note.subFeatures.size} Milestones | ${note.category}", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE") }
        }
    )
}

// Module Views (Placeholders/Basic Impl)

@Composable
fun WorkspaceDashboard(state: WorkspaceUIState, viewModel: WorkspaceViewModel) {
    val style = LocalAppStyle.current
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Metrics Section
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard("Progress", "${state.selectedProject?.progress ?: 0}%", style.accentColor)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard("Weighted", "${state.selectedProject?.weightedProgress ?: 0}%", Color(0xFF2EC4B6))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard("Health", state.selectedProject?.health ?: "Healthy", Color(0xFFE91E63))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard("Active Tasks", state.tasks.count { it.status != "Done" }.toString(), Color(0xFFFFB800))
                }
            }
        }

        // Project List Section
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Your Ecosystem",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "${state.projects.size} Projects",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(state.projects) { project ->
            ProjectOverviewItem(
                project = project,
                isSelected = project.id == state.selectedProject?.id,
                onClick = { viewModel.selectProject(project.id) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ProjectOverviewItem(
    project: ProjectEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val style = LocalAppStyle.current
    val projectColor = if (project.color != -1) Color(project.color) else style.accentColor

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) style.surfaceColor else style.surfaceColor.copy(alpha = 0.5f)
        ),
        border = if (isSelected) BorderStroke(1.dp, projectColor.copy(alpha = 0.4f)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(projectColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = projectColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    project.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    project.status.uppercase(),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${project.progress}%",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                LinearProgressIndicator(
                    progress = { project.progress / 100f },
                    modifier = Modifier.width(60.dp).height(4.dp),
                    color = projectColor,
                    trackColor = Color.White.copy(alpha = 0.05f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, color: Color) {
    val style = LocalAppStyle.current
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        colors = CardDefaults.cardColors(containerColor = style.surfaceColor)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun GoalsTree(goals: List<GoalEntity>) {
    LazyColumn {
        items(goals) { goal ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(goal.title, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun NotesView(notes: List<NoteEntity>) {
    LazyVerticalGrid(columns = GridCells.Fixed(2)) {
        items(notes) { note ->
            Card(modifier = Modifier.padding(8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(note.title, fontWeight = FontWeight.Bold)
                    Text(note.content, maxLines = 3)
                }
            }
        }
    }
}

@Composable
fun TasksKanban(tasks: List<TaskEntity>, onUpdateTask: (TaskEntity) -> Unit) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        val statuses = listOf("Todo", "In Progress", "Review", "Done")
        statuses.forEach { status ->
            Column(modifier = Modifier.width(300.dp).padding(8.dp)) {
                Text(status.uppercase(), fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                tasks.filter { it.status == status }.forEach { task ->
                    TaskItem(task, onUpdateTask)
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: TaskEntity, onUpdateTask: (TaskEntity) -> Unit) {
    val style = LocalAppStyle.current
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = style.surfaceColor)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(task.title, fontWeight = FontWeight.Bold, color = Color.White)
            Text(task.description, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
            LinearProgressIndicator(progress = { task.progress / 100f }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        }
    }
}

@Composable
fun FeaturePlanner(features: List<FeatureEntity>) {
    LazyColumn {
        items(features) { feature ->
            Text(feature.title, modifier = Modifier.padding(16.dp), color = Color.White)
        }
    }
}

@Composable
fun BugTracker(bugs: List<BugEntity>) {
    LazyColumn {
        items(bugs) { bug ->
            Text(bug.title, modifier = Modifier.padding(16.dp), color = Color.Red)
        }
    }
}

@Composable
fun IdeaBacklog(ideas: List<IdeaEntity>, onConvert: (IdeaEntity) -> Unit) {
    LazyColumn {
        items(ideas) { idea ->
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(idea.title, color = Color.White)
                if (idea.status != "Converted") {
                    Button(onClick = { onConvert(idea) }) { Text("Convert") }
                }
            }
        }
    }
}

@Composable
fun ResourceDirectory(resources: List<ResourceEntity>) {
    LazyColumn {
        items(resources) { res ->
            Text(res.title, modifier = Modifier.padding(16.dp), color = Color.White)
        }
    }
}

@Composable
fun ActivityLogView(logs: List<ActivityLogEntity>) {
    LazyColumn {
        items(logs) { log ->
            Text("${log.action}: ${log.description}", modifier = Modifier.padding(8.dp), color = Color.White.copy(alpha = 0.5f))
        }
    }
}
