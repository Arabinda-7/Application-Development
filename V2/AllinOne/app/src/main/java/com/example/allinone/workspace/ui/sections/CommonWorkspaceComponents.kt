@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.example.allinone.workspace.ui.sections

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle
import com.example.allinone.workspace.data.BugEntity
import com.example.allinone.workspace.data.FeatureEntity
import com.example.allinone.workspace.data.ProjectEntity
import com.example.allinone.workspace.data.TaskEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CreatedAtText(timestamp: Long, modifier: Modifier = Modifier) {
    val timeStr = remember(timestamp) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
    Text(
        text = timeStr,
        color = Color.White.copy(alpha = 0.25f),
        fontSize = 9.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}

@Composable
fun ProjectOverviewItem(
    project: ProjectEntity, 
    isSelected: Boolean, 
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val style = LocalAppStyle.current
    val projectColor = if (project.color != -1) Color(project.color) else style.accentColor
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(20.dp), 
        colors = CardDefaults.cardColors(containerColor = Color.Transparent), 
        border = BorderStroke(1.5.dp, if (isSelected) projectColor else projectColor.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                // Vertical Accent Bar
                Box(modifier = Modifier.width(4.dp).fillMaxHeight().clip(CircleShape).background(projectColor.copy(alpha = 0.8f)))
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) { 
                    Text(
                        project.name, 
                        color = Color.White, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 20.sp, 
                        maxLines = 2, 
                        overflow = TextOverflow.Ellipsis
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
                    Text("${project.progress}%", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    LinearProgressIndicator(
                        progress = { project.progress / 100f }, 
                        modifier = Modifier.width(60.dp).height(4.dp), 
                        color = projectColor, 
                        trackColor = Color.White.copy(alpha = 0.05f), 
                        strokeCap = StrokeCap.Round
                    ) 
                }
            }
            CreatedAtText(
                timestamp = project.createdAt,
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
            )
        }
    }
}

@Composable
fun ProjectStatsDialog(
    project: ProjectEntity,
    viewModel: com.example.allinone.workspace.ui.WorkspaceViewModel,
    onDismiss: () -> Unit
) {
    val stats by viewModel.getProjectStats(project.id).collectAsState(initial = com.example.allinone.workspace.ui.ProjectStats())
    val style = LocalAppStyle.current
    val projectColor = if (project.color != -1) Color(project.color) else style.accentColor

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = style.backgroundColor,
        tonalElevation = 8.dp,
        modifier = Modifier.padding(16.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = project.name,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "LIFECYCLE SUMMARY",
                    color = projectColor.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick Summary Bar
                Surface(
                    color = Color.White.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickStatItem(Icons.Default.Checklist, stats.totalTasks.toString(), "Tasks")
                        VerticalDivider(modifier = Modifier.height(24.dp).align(Alignment.CenterVertically), color = Color.White.copy(alpha = 0.1f))
                        QuickStatItem(Icons.Default.Extension, stats.totalFeatures.toString(), "Features")
                        VerticalDivider(modifier = Modifier.height(24.dp).align(Alignment.CenterVertically), color = Color.White.copy(alpha = 0.1f))
                        QuickStatItem(Icons.Default.BugReport, stats.totalBugs.toString(), "Bugs")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                StatsSection(
                    title = "TASKS",
                    icon = Icons.Default.Checklist,
                    total = stats.totalTasks,
                    breakdown = stats.taskBreakdown,
                    themeColor = style.accentColor,
                    completedCount = stats.taskBreakdown["Done"] ?: 0
                )
                StatsSection(
                    title = "FEATURES",
                    icon = Icons.Default.Extension,
                    total = stats.totalFeatures,
                    breakdown = stats.featureBreakdown,
                    themeColor = Color(0xFF2EC4B6),
                    completedCount = stats.featureBreakdown["Shipped"] ?: 0
                )
                StatsSection(
                    title = "BUGS",
                    icon = Icons.Default.BugReport,
                    total = stats.totalBugs,
                    breakdown = stats.bugBreakdown,
                    themeColor = Color.Red,
                    completedCount = stats.bugBreakdown["Verified"] ?: 0
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.textButtonColors(containerColor = projectColor.copy(alpha = 0.1f))
            ) {
                Text("DISMISS", color = projectColor, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    )
}

@Composable
private fun QuickStatItem(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text(label.uppercase(), color = Color.White.copy(alpha = 0.3f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatsSection(
    title: String,
    icon: ImageVector,
    total: Int,
    breakdown: Map<String, Int>,
    themeColor: Color,
    completedCount: Int
) {
    val progress = if (total > 0) completedCount.toFloat() / total else 0f
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = themeColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Icon(icon, null, tint = themeColor, modifier = Modifier.padding(6.dp).size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text("$completedCount/$total", color = themeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = themeColor,
                trackColor = Color.White.copy(alpha = 0.05f),
                strokeCap = StrokeCap.Round
            )
            
            if (breakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    breakdown.forEach { (status, count) ->
                        Surface(
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(4.dp).background(themeColor, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$count $status",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureItemCard(
    feature: FeatureEntity,
    linkedTasks: List<TaskEntity>,
    onUpdate: (FeatureEntity) -> Unit,
    onViewFeature: (FeatureEntity) -> Unit,
    onEditFeature: (FeatureEntity) -> Unit,
    onDeleteFeature: (FeatureEntity) -> Unit,
    onQuickTasks: (FeatureEntity) -> Unit
) {
    val style = LocalAppStyle.current
    var showMenu by remember { mutableStateOf(false) }
    val progress = if (linkedTasks.isNotEmpty()) { linkedTasks.count { it.status == "Done" }.toFloat() / linkedTasks.size } else 0f
    Box {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = style.surfaceColor),
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onViewFeature(feature) },
                    onLongClick = { showMenu = true }
                )
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(feature.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            if (feature.targetVersion.isNotBlank()) { Text(feature.targetVersion, color = style.accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                        }
                        Surface(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) { Text(feature.effortSize, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black) }
                    }
                    if (feature.description.isNotBlank()) { Spacer(modifier = Modifier.height(4.dp)); Text(feature.description, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f), maxLines = 2, overflow = TextOverflow.Ellipsis) }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) { LinearProgressIndicator(progress = { progress }, modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape), color = if (progress == 1f) Color(0xFF2EC4B6) else style.accentColor, trackColor = Color.White.copy(alpha = 0.05f)); Spacer(modifier = Modifier.width(8.dp)); Text("${(progress * 100).toInt()}%", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (linkedTasks.isEmpty() && feature.status != "Shipped") { TextButton(onClick = { onQuickTasks(feature) }, modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 8.dp)) { Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("QUICK TASKS", fontSize = 10.sp, fontWeight = FontWeight.Bold) } }
                        Spacer(modifier = Modifier.weight(1f))
                        if (feature.status != "Shipped") { IconButton(onClick = { val nextStatus = when(feature.status) { "Backlog" -> "Planning"; "Planning" -> "Development"; "Development" -> "Testing"; "Testing" -> "Shipped"; else -> "Shipped" }; onUpdate(feature.copy(status = nextStatus)) }, modifier = Modifier.size(32.dp).background(style.accentColor.copy(alpha = 0.1f), CircleShape)) { Icon(if (feature.status == "Testing") Icons.Default.RocketLaunch else Icons.Default.ChevronRight, contentDescription = "Next", tint = style.accentColor, modifier = Modifier.size(16.dp)) } }
                    }
                }
                CreatedAtText(
                    timestamp = feature.createdAt,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                )
            }
        }
        WorkspaceDropdown(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            WorkspaceDropdownItem(text = "View Details", onClick = { onViewFeature(feature); showMenu = false }, icon = Icons.Default.Description)
            WorkspaceDropdownItem(text = "Edit", onClick = { onEditFeature(feature); showMenu = false }, icon = Icons.Default.Edit)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.1f))
            WorkspaceDropdownItem(text = "Delete", onClick = { onDeleteFeature(feature); showMenu = false }, icon = Icons.Default.Delete, isDestructive = true)
        }
    }
}

@Composable
fun BugItemCard(
    bug: BugEntity,
    onUpdate: (BugEntity) -> Unit,
    onViewBug: (BugEntity) -> Unit,
    onEditBug: (BugEntity) -> Unit,
    onDeleteBug: (BugEntity) -> Unit
) {
    val style = LocalAppStyle.current
    var showMenu by remember { mutableStateOf(false) }
    val severityColor = when (bug.severity) { "Critical" -> Color.Red; "High" -> Color(0xFFFF5252); "Medium" -> Color(0xFFFFB800); else -> Color(0xFF2EC4B6) }
    val accentColor = style.accentColor
    
    Box {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.5.dp, severityColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .combinedClickable(
                    onClick = { onViewBug(bug) },
                    onLongClick = { showMenu = true }
                )
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Vertical Accent Bar
                    Box(modifier = Modifier.width(4.dp).fillMaxHeight().clip(CircleShape).background(severityColor.copy(alpha = 0.8f)))
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    bug.title, 
                                    fontWeight = FontWeight.Bold, 
                                    color = Color.White, 
                                    fontSize = 20.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) { 
                                    Surface(color = severityColor.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) { 
                                        Text(bug.severity.uppercase(), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), color = severityColor, fontSize = 8.sp, fontWeight = FontWeight.Black) 
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(bug.environment, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp) 
                                }
                            }
                            val priorityIcon = when(bug.priority) { 
                                2 -> Icons.Default.KeyboardDoubleArrowUp
                                1 -> Icons.Default.KeyboardArrowUp
                                else -> Icons.Default.KeyboardArrowDown 
                            }
                            Icon(imageVector = priorityIcon, contentDescription = null, tint = severityColor, modifier = Modifier.size(18.dp))
                        }
                        if (bug.description.isNotBlank()) { 
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                bug.description, 
                                fontSize = 14.sp, 
                                color = Color.White.copy(alpha = 0.6f), 
                                maxLines = 3, 
                                overflow = TextOverflow.Ellipsis
                            ) 
                        }
                        if (bug.version.isNotBlank()) { 
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("v${bug.version}", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold) 
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) { 
                            if (bug.status != "Verified") { 
                                IconButton(
                                    onClick = { 
                                        val nextStatus = when(bug.status) { 
                                            "Open" -> "Confirmed"
                                            "Confirmed" -> "Fixing"
                                            "Fixing" -> "Fixed"
                                            "Fixed" -> "Verified"
                                            else -> "Verified" 
                                        }
                                        onUpdate(bug.copy(status = nextStatus)) 
                                    }, 
                                    modifier = Modifier.size(32.dp).background(accentColor.copy(alpha = 0.1f), CircleShape)
                                ) { 
                                    Icon(if (bug.status == "Fixed") Icons.Default.Verified else Icons.Default.ChevronRight, contentDescription = "Next", tint = accentColor, modifier = Modifier.size(16.dp)) 
                                } 
                            } else { 
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2EC4B6), modifier = Modifier.size(24.dp)) 
                            } 
                        }
                    }
                }
                CreatedAtText(
                    timestamp = bug.createdAt,
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                )
            }
        }
        WorkspaceDropdown(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            WorkspaceDropdownItem(text = "View Details", onClick = { onViewBug(bug); showMenu = false }, icon = Icons.Default.Description)
            WorkspaceDropdownItem(text = "Edit", onClick = { onEditBug(bug); showMenu = false }, icon = Icons.Default.Edit)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.1f))
            WorkspaceDropdownItem(text = "Delete", onClick = { onDeleteBug(bug); showMenu = false }, icon = Icons.Default.Delete, isDestructive = true)
        }
    }
}

@Composable
fun WorkspaceDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val style = LocalAppStyle.current
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(surface = style.surfaceColor),
        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(style.borderRadius))
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier
                .background(style.surfaceColor)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(style.borderRadius)),
            content = content
        )
    }
}

@Composable
fun WorkspaceDropdownItem(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    isDestructive: Boolean = false
) {
    val style = LocalAppStyle.current
    DropdownMenuItem(
        text = { Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
        onClick = onClick,
        leadingIcon = icon?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(20.dp)) } },
        colors = MenuDefaults.itemColors(
            textColor = if (isDestructive) Color.Red else Color.White,
            leadingIconColor = if (isDestructive) Color.Red else style.accentColor
        )
    )
}

@Composable
fun MetricCard(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize().height(IntrinsicSize.Min)) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vertical Accent Bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.8f))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = label.uppercase(),
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun DetailInfoItem(label: String, value: String, color: Color) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(text = label, color = color.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(text = value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
