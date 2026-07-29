package com.example.allinone.workspace.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle
import com.example.allinone.workspace.data.*
import com.example.allinone.workspace.ui.sections.WorkspaceDropdown

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
fun WorkspaceSidebar(selectedTab: WorkspaceTab, onTabSelected: (WorkspaceTab) -> Unit, onBack: () -> Unit, isExpanded: Boolean, onToggleExpand: () -> Unit) {
    val style = LocalAppStyle.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val expandedWidth = configuration.screenWidthDp.dp / 4
    val sidebarWidth by animateDpAsState(targetValue = if (isExpanded) expandedWidth else 0.dp, animationSpec = spring(stiffness = Spring.StiffnessLow), label = "SidebarWidth")
    val sidebarBg by animateColorAsState(targetValue = if (isExpanded) style.surfaceColor else Color.Transparent, label = "SidebarBg")

    if (isExpanded || sidebarWidth > 0.dp) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
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
        Surface(color = sidebarBg, modifier = Modifier.fillMaxHeight().width(sidebarWidth).shadow(if (isExpanded) 12.dp else 0.dp)) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.Start) {
                Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(64.dp).clickable { onToggleExpand() }, contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.MenuOpen, contentDescription = "Close Sidebar", tint = Color.White)
                        AnimatedVisibility(visible = isExpanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("SECTIONS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            }
                        }
                    }
                }
                
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(tween(400)) + slideInHorizontally(tween(400)) { -20 },
                    exit = fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -20 },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
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
                    }
                }
                if (!isExpanded) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun NoProjectsScreen(onAddProject: () -> Unit, onImportProject: () -> Unit, onTrySample: () -> Unit, onToggleMenu: () -> Unit, isSidebarExpanded: Boolean, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
            androidx.compose.animation.AnimatedVisibility(visible = !isSidebarExpanded, enter = fadeIn(), exit = fadeOut()) {
                IconButton(onClick = onToggleMenu) { Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White) }
            }
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
    Row(modifier = modifier.fillMaxWidth().height(72.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.width(56.dp), contentAlignment = Alignment.CenterStart) {
                androidx.compose.animation.AnimatedVisibility(visible = !isSidebarExpanded, enter = fadeIn(), exit = fadeOut()) {
                    IconButton(onClick = onToggleMenu) { Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White) }
                }
            }
            Column {
                Text("Workspace", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                Text(selectedProject?.name ?: "Select Project", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }
        var showProjectMenu by remember { mutableStateOf(false) }
        Box {
            if (currentTab == WorkspaceTab.Dashboard) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditProject, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Project", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onImportRequest, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.UploadFile, contentDescription = "Import", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = { showProjectMenu = true }, 
                        shape = RoundedCornerShape(10.dp), 
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp)); Text("Switch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            WorkspaceDropdown(expanded = showProjectMenu, onDismissRequest = { showProjectMenu = false }) {
                projects.forEach { project ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(project.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = Color.White, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
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
