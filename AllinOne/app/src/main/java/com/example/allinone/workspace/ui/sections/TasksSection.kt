@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.example.allinone.workspace.ui.sections

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle
import com.example.allinone.workspace.data.TaskEntity
import com.example.allinone.workspace.ui.WorkspaceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskViewSection(
    tasks: List<TaskEntity>,
    onUpdateTask: (TaskEntity) -> Unit,
    onViewTask: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit
) {
    val style = LocalAppStyle.current
    val statuses = listOf("Todo", "In Progress", "Review", "Done")
    val pagerState = rememberPagerState(pageCount = { statuses.size })
    val scope = rememberCoroutineScope()
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val highPriorityCount = tasks.count { it.priority == 2 }
            val total = tasks.size
            Box(modifier = Modifier.weight(1f)) { MetricCard("High Priority", highPriorityCount.toString(), Color(0xFFFF5252)) }
            Box(modifier = Modifier.weight(1f)) { MetricCard("Total Tasks", total.toString(), style.accentColor) }
        }

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = style.accentColor,
            edgePadding = 0.dp,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = style.accentColor
                )
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            statuses.forEachIndexed { index, status ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(
                            status.uppercase(),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = if (pagerState.currentPage == index) Color.White else Color.White.copy(alpha = 0.4f)
                        )
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) { pageIndex ->
            val status = statuses[pageIndex]
            val statusTasks = tasks.filter { it.status == status }
            
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
                if (statusTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(top = 40.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (status == "Done") Icons.Default.CheckCircle else Icons.Default.Task,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.05f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No tasks in $status", color = Color.White.copy(alpha = 0.1f), fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        val sortedTasks = statusTasks.sortedByDescending { it.priority }
                        itemsIndexed(
                            sortedTasks, 
                            key = { _, task -> task.id }
                        ) { index, task ->
                            Box(modifier = Modifier.animateItem()) {
                                TaskItemUI(
                                    task = task,
                                    onUpdateTask = onUpdateTask,
                                    onViewTask = onViewTask,
                                    onEditTask = onEditTask,
                                    onDeleteTask = onDeleteTask,
                                    index = index + 1
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItemUI(task: TaskEntity, onUpdateTask: (TaskEntity) -> Unit, onViewTask: (TaskEntity) -> Unit, onEditTask: (TaskEntity) -> Unit, onDeleteTask: (TaskEntity) -> Unit, index: Int? = null) {
    val style = LocalAppStyle.current
    var showMenu by remember { mutableStateOf(false) }
    val isDone = task.status == "Done"
    val priorityColor = when (task.priority) {
        2 -> Color(0xFFFF5252)
        1 -> Color(0xFFFFB800)
        else -> Color(0xFF2EC4B6)
    }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .combinedClickable(
                    onClick = { onViewTask(task) },
                    onLongClick = { showMenu = true }
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.5.dp, if (task.priority == 2 && !isDone) priorityColor else priorityColor.copy(alpha = 0.3f))
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Vertical Accent Bar
                    Box(modifier = Modifier.width(4.dp).fillMaxHeight().clip(CircleShape).background(priorityColor.copy(alpha = 0.8f)))
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isDone,
                                onCheckedChange = { checked ->
                                    val newStatus = if (checked) "Done" else "Todo"
                                    val newProgress = if (checked) 100 else 0
                                    onUpdateTask(task.copy(status = newStatus, progress = newProgress))
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF2EC4B6),
                                    uncheckedColor = Color.White.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                val titleText = if (index != null) "$index. ${task.title}" else task.title
                                Text(
                                    titleText,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDone) Color.White.copy(alpha = 0.4f) else Color.White,
                                    textDecoration = if (isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                    fontSize = 18.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            
                            val priorityIcon = when(task.priority) {
                                2 -> Icons.Default.KeyboardDoubleArrowUp
                                1 -> Icons.Default.KeyboardArrowUp
                                else -> Icons.Default.KeyboardArrowDown
                            }
                            Icon(
                                imageVector = priorityIcon, 
                                contentDescription = null, 
                                tint = if (isDone) Color.White.copy(alpha = 0.2f) else priorityColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (task.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                task.description,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.4f),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { task.progress / 100f },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = if (isDone) Color(0xFF2EC4B6) else style.accentColor,
                                trackColor = Color.White.copy(alpha = 0.05f)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            if (!isDone) {
                                IconButton(
                                    onClick = {
                                        val nextStatus = when(task.status) {
                                            "Todo" -> "In Progress"
                                            "In Progress" -> "Review"
                                            "Review" -> "Done"
                                            else -> "Done"
                                        }
                                        val newProgress = if (nextStatus == "Done") 100 else task.progress
                                        onUpdateTask(task.copy(status = nextStatus, progress = newProgress))
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(style.accentColor.copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight, 
                                        contentDescription = "Next", 
                                        tint = style.accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle, 
                                    contentDescription = "Completed", 
                                    tint = Color(0xFF2EC4B6),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                val displayTime = task.dueDate ?: task.createdAt
                CreatedAtText(
                    timestamp = displayTime,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                )
            }
        }
        WorkspaceDropdown(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            WorkspaceDropdownItem(
                text = "View Details",
                onClick = { onViewTask(task); showMenu = false },
                icon = Icons.Default.Description
            )
            WorkspaceDropdownItem(
                text = "Edit",
                onClick = { onEditTask(task); showMenu = false },
                icon = Icons.Default.Edit
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.1f))
            WorkspaceDropdownItem(
                text = "Delete",
                onClick = { onDeleteTask(task); showMenu = false },
                icon = Icons.Default.Delete,
                isDestructive = true
            )
        }
    }
}

@Composable
fun TaskDetailSection(
    task: TaskEntity,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onUpdateTask: (TaskEntity) -> Unit
) {
    val style = LocalAppStyle.current
    val accentColor = when(task.priority) {
        2 -> Color(0xFFFF5252)
        1 -> Color(0xFFFFB800)
        else -> Color(0xFF2EC4B6)
    }

    val isDone = task.status == "Done"

    Box(modifier = Modifier.fillMaxSize().background(style.backgroundColor)) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(accentColor.copy(alpha = 0.15f)))
        
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = accentColor, modifier = Modifier.size(28.dp)) }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp)) {
                Text(text = task.title, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(24.dp))
                
                DetailInfoItem(label = "STATUS", value = task.status.uppercase(), color = accentColor)
                DetailInfoItem(label = "PRIORITY", value = when(task.priority) { 2 -> "High"; 1 -> "Medium"; else -> "Low" }, color = accentColor)
                DetailInfoItem(label = "PROGRESS", value = "${task.progress}%", color = accentColor)
                
                if (task.dueDate != null) {
                    val dueStr = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault()).format(Date(task.dueDate))
                    DetailInfoItem(label = "DUE DATE / REMINDER", value = dueStr, color = accentColor)
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        val newStatus = if (isDone) "Todo" else "Done"
                        val newProgress = if (isDone) 0 else 100
                        onUpdateTask(task.copy(status = newStatus, progress = newProgress))
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDone) Color.White.copy(alpha = 0.1f) else accentColor,
                        contentColor = if (isDone) Color.White else Color.Black
                    )
                ) {
                    Icon(imageVector = if (isDone) Icons.Default.Refresh else Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(if (isDone) "REOPEN TASK" else "COMPLETE TASK", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("DESCRIPTION", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (task.description.isNotBlank()) task.description else "No description provided.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
fun TaskAddEditSection(
    task: TaskEntity? = null,
    projectId: String,
    viewModel: WorkspaceViewModel,
    onBack: () -> Unit
) {
    val style = LocalAppStyle.current
    
    var title by remember(task) { mutableStateOf(task?.title ?: "") }
    var description by remember(task) { mutableStateOf(task?.description ?: "") }
    var priority by remember(task) { mutableStateOf(task?.priority ?: 1) }
    var status by remember(task) { mutableStateOf(task?.status ?: "Todo") }
    var dueDate by remember(task) { mutableStateOf(task?.dueDate) }

    val context = androidx.compose.ui.platform.LocalContext.current

    val dynamicAccentColor = when(priority) {
        2 -> Color(0xFFFF5252)
        1 -> Color(0xFFFFB800)
        else -> Color(0xFF2EC4B6)
    }

    Box(modifier = Modifier.fillMaxSize().background(style.backgroundColor)) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(dynamicAccentColor.copy(alpha = 0.15f)))
        
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().imePadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                TextButton(
                    onClick = {
                        val progress = if (status == "Done") 100 else if (task?.status == "Done") 0 else task?.progress ?: 0
                        val updated = task?.copy(title = title, description = description, priority = priority, status = status, progress = progress, dueDate = dueDate)
                            ?: TaskEntity(projectId = projectId, title = title, description = description, priority = priority, status = status, progress = progress, dueDate = dueDate)
                        
                        if (task == null) {
                             viewModel.insertTask(updated)
                        }
                        else viewModel.updateTask(updated)
                        onBack()
                    },
                    enabled = title.isNotBlank()
                ) {
                    Text("SAVE", color = dynamicAccentColor, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp)) {
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black),
                    cursorBrush = SolidColor(dynamicAccentColor),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (title.isEmpty()) { Text("Task Title", color = Color.White.copy(alpha = 0.2f), fontSize = 32.sp, fontWeight = FontWeight.Black) }
                        innerTextField()
                    }
                )
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("STATUS", color = dynamicAccentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val statuses = listOf("Todo", "In Progress", "Review", "Done")
                    statuses.forEach { label ->
                        val isSel = status == label
                        Surface(
                            onClick = { status = label },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) dynamicAccentColor else dynamicAccentColor.copy(alpha = 0.1f),
                            border = if (isSel) null else BorderStroke(1.dp, dynamicAccentColor.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) { Box(contentAlignment = Alignment.Center) { Text(label.uppercase(), color = if (isSel) Color.White else dynamicAccentColor, fontSize = 8.sp, fontWeight = FontWeight.Black) } }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("PRIORITY", color = dynamicAccentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("LOW", "MED", "HIGH").forEachIndexed { index, label ->
                        val isSel = priority == index
                        val color = when(index) { 2 -> Color(0xFFFF5252); 1 -> Color(0xFFFFB800); else -> Color(0xFF2EC4B6) }
                        Surface(
                            onClick = { priority = index },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) color else color.copy(alpha = 0.1f),
                            border = if (isSel) null else BorderStroke(1.dp, color.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) { Box(contentAlignment = Alignment.Center) { Text(label, color = if (isSel) Color.White else color, fontSize = 10.sp, fontWeight = FontWeight.Black) } }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("REMINDER / DUE DATE", color = dynamicAccentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        dueDate?.let { calendar.timeInMillis = it }
                        
                        val datePicker = android.app.DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                calendar.set(Calendar.YEAR, year)
                                calendar.set(Calendar.MONTH, month)
                                calendar.set(Calendar.DAY_OF_MONTH, day)
                                
                                android.app.TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                                        calendar.set(Calendar.MINUTE, minute)
                                        dueDate = calendar.timeInMillis
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    false
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        datePicker.show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (dueDate != null) dynamicAccentColor else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (dueDate != null) {
                                dueDate?.let { SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(it)) } ?: ""
                            } else {
                                "Set reminder time..."
                            },
                            color = if (dueDate != null) Color.White else Color.White.copy(alpha = 0.3f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (dueDate != null) {
                            IconButton(onClick = { dueDate = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("DESCRIPTION", color = dynamicAccentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                BasicTextField(
                    value = description,
                    onValueChange = { description = it },
                    textStyle = TextStyle(color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, lineHeight = 24.sp),
                    cursorBrush = SolidColor(dynamicAccentColor),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    decorationBox = { innerTextField ->
                        if (description.isEmpty()) {
                            Text("What needs to be done?", color = Color.White.copy(alpha = 0.2f), fontSize = 16.sp)
                        }
                        innerTextField()
                    }
                )
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
