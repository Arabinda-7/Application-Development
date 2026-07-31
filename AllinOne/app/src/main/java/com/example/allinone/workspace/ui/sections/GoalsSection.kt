@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.example.allinone.workspace.ui.sections

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle
import com.example.allinone.workspace.data.GoalEntity
import com.example.allinone.workspace.ui.WorkspaceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GoalViewSection(
    goals: List<GoalEntity>,
    onViewGoal: (GoalEntity) -> Unit,
    onEditGoal: (GoalEntity) -> Unit,
    onDeleteGoal: (GoalEntity) -> Unit
) {
    val style = LocalAppStyle.current
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        items(goals, key = { it.id }) { goal ->
            val color = if (goal.color != -1) Color(goal.color) else style.accentColor
            var showMenu by remember { mutableStateOf(false) }
            Box(modifier = Modifier.animateItem()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .combinedClickable(
                            onClick = { onViewGoal(goal) },
                            onLongClick = { showMenu = true }
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.5.dp, color)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        Row(modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Vertical Accent Bar
                            Box(modifier = Modifier.width(4.dp).fillMaxHeight().clip(CircleShape).background(color.copy(alpha = 0.8f)))
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = goal.title, 
                                    color = color, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 20.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (goal.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        goal.description, 
                                        color = Color.White.copy(alpha = 0.6f), 
                                        fontSize = 14.sp, 
                                        maxLines = 2, 
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            
                            val priorityColor = when(goal.priority) { 2 -> Color.Red; 1 -> Color(0xFFFFB800); else -> Color(0xFF2EC4B6) }
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(priorityColor))
                        }
                        CreatedAtText(
                            timestamp = goal.createdAt,
                            deadline = goal.deadline,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                        )
                    }
                }
                WorkspaceDropdown(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    WorkspaceDropdownItem(
                        text = "View Details",
                        onClick = { onViewGoal(goal); showMenu = false },
                        icon = Icons.Default.Description
                    )
                    WorkspaceDropdownItem(
                        text = "Edit",
                        onClick = { onEditGoal(goal); showMenu = false },
                        icon = Icons.Default.Edit
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.1f))
                    WorkspaceDropdownItem(
                        text = "Delete",
                        onClick = { onDeleteGoal(goal); showMenu = false },
                        icon = Icons.Default.Delete,
                        isDestructive = true
                    )
                }
            }
        }
    }
}

@Composable
fun GoalDetailSection(
    goal: GoalEntity,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val style = LocalAppStyle.current
    val goalColor = if (goal.color != -1) Color(goal.color) else style.accentColor
    
    Box(modifier = Modifier.fillMaxSize().background(style.backgroundColor)) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(goalColor.copy(alpha = 0.15f)))
        
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = goalColor, modifier = Modifier.size(28.dp)) }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp)) {
                Text(text = goal.title, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(24.dp))
                
                DetailInfoItem(label = "PRIORITY", value = when(goal.priority) { 2 -> "High"; 1 -> "Medium"; else -> "Low" }, color = goalColor)
                
                if (goal.deadline != null) {
                    val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(goal.deadline))
                    DetailInfoItem(label = "DEADLINE", value = date, color = goalColor)
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("DESCRIPTION", color = goalColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (goal.description.isNotBlank()) goal.description else "No description provided.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
fun GoalAddEditSection(
    goal: GoalEntity? = null,
    projectId: String,
    viewModel: WorkspaceViewModel,
    onBack: () -> Unit
) {
    val style = LocalAppStyle.current
    val projectColorHex = com.example.allinone.DataManager.globalProjectColor
    val projectColor = if (projectColorHex != -1) Color(projectColorHex) else style.accentColor

    var title by remember(goal) { mutableStateOf(goal?.title ?: "") }
    var description by remember(goal) { mutableStateOf(goal?.description ?: "") }
    var colorInt by remember(goal) { mutableIntStateOf(goal?.color ?: -1) }
    var priority by remember(goal) { mutableIntStateOf(goal?.priority ?: 1) }
    var deadline by remember(goal) { mutableStateOf(goal?.deadline) }

    val dynamicAccentColor = if (colorInt != -1) Color(colorInt) else projectColor

    val context = androidx.compose.ui.platform.LocalContext.current

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
                        val updated = goal?.copy(title = title, description = description, priority = priority, color = colorInt, deadline = deadline)
                            ?: GoalEntity(projectId = projectId, title = title, description = description, priority = priority, color = colorInt, deadline = deadline)
                        
                        if (goal == null) viewModel.addGoal(title, projectId, description, colorInt, priority, deadline)
                        else viewModel.updateGoal(updated)
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
                        if (title.isEmpty()) { Text("Goal Title", color = Color.White.copy(alpha = 0.2f), fontSize = 32.sp, fontWeight = FontWeight.Black) }
                        innerTextField()
                    }
                )
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("PRIORITY", color = dynamicAccentColor, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf("LOW", "MED", "HIGH").forEachIndexed { index, label ->
                                val isSel = priority == index
                                val color = when(index) { 2 -> Color(0xFFFF5252); 1 -> Color(0xFFFFB800); else -> Color(0xFF2EC4B6) }
                                Box(modifier = Modifier.weight(1f).height(44.dp).clip(CircleShape).background(if (isSel) color else color.copy(alpha = 0.1f)).border(1.dp, if (isSel) color else color.copy(alpha = 0.3f), CircleShape).clickable { priority = index }, contentAlignment = Alignment.Center)
                                { Text(label, color = if (isSel) Color.White else color, fontSize = 11.sp, fontWeight = FontWeight.Black) }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("THEME COLOR", color = dynamicAccentColor, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        val colors = listOf(0xFFFF7A59, 0xFFFFB800, 0xFF2EC4B6, 0xFF3A86F0, 0xFF1A73E8, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF4CAF50)
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            colors.forEach { c ->
                                val isSel = colorInt == c.toInt()
                                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(c)).border(if (isSel) 3.dp else 0.dp, Color.White, CircleShape).clickable { colorInt = c.toInt() })
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Text("DEADLINE / REMINDER", color = dynamicAccentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        deadline?.let { calendar.timeInMillis = it }
                        
                        android.app.DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                calendar.set(Calendar.YEAR, year)
                                calendar.set(Calendar.MONTH, month)
                                calendar.set(Calendar.DAY_OF_MONTH, day)
                                deadline = calendar.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
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
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = if (deadline != null) dynamicAccentColor else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (deadline != null) {
                                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(deadline!!))
                            } else {
                                "Set deadline date..."
                            },
                            color = if (deadline != null) Color.White else Color.White.copy(alpha = 0.3f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (deadline != null) {
                            IconButton(onClick = { deadline = null }, modifier = Modifier.size(24.dp)) {
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
                            Text("What is the major milestone?", color = Color.White.copy(alpha = 0.2f), fontSize = 16.sp)
                        }
                        innerTextField()
                    }
                )
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
