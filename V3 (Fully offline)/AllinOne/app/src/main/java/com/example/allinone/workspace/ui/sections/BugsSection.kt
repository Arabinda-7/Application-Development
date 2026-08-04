package com.example.allinone.workspace.ui.sections

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.allinone.workspace.data.BugEntity
import com.example.allinone.workspace.ui.WorkspaceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BugViewSection(
    bugs: List<BugEntity>,
    viewModel: WorkspaceViewModel,
    onViewBug: (BugEntity) -> Unit,
    onEditBug: (BugEntity) -> Unit,
    onDeleteBug: (BugEntity) -> Unit
) {
    val style = LocalAppStyle.current
    val statuses = listOf("Open", "Confirmed", "Fixing", "Fixed", "Verified")
    val pagerState = rememberPagerState(pageCount = { statuses.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val criticalCount = bugs.count { it.severity == "Critical" }
            val total = bugs.size
            Box(modifier = Modifier.weight(1f)) { MetricCard("Critical", criticalCount.toString(), Color.Red) }
            Box(modifier = Modifier.weight(1f)) { MetricCard("Total Bugs", total.toString(), style.accentColor) }
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
            val statusBugs = bugs.filter { it.status == status }
            
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
                if (statusBugs.isEmpty()) {
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
                                imageVector = if (status == "Verified") Icons.Default.CheckCircle else Icons.Default.BugReport,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.05f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No bugs in $status", color = Color.White.copy(alpha = 0.1f), fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(statusBugs.sortedByDescending { it.priority }, key = { it.id }) { bug ->
                            Box(modifier = Modifier.animateItem()) {
                                BugItemCard(
                                    bug = bug, 
                                    onUpdate = { viewModel.updateBug(it) }, 
                                    onViewBug = onViewBug,
                                    onEditBug = onEditBug, 
                                    onDeleteBug = onDeleteBug
                                )
                                Spacer(modifier = Modifier.height(12.dp))
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
fun BugDetailSection(
    bug: BugEntity,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val style = LocalAppStyle.current
    val accentColor = when (bug.severity) {
        "Critical" -> Color.Red
        "High" -> Color(0xFFFF5252)
        "Medium" -> Color(0xFFFFB800)
        else -> Color(0xFF2EC4B6)
    }

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
                Text(text = bug.title, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(24.dp))
                
                DetailInfoItem(label = "STATUS", value = bug.status.uppercase(), color = accentColor)
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) { DetailInfoItem(label = "SEVERITY", value = bug.severity, color = accentColor) }
                    Box(modifier = Modifier.weight(1f)) { DetailInfoItem(label = "ENVIRONMENT", value = bug.environment, color = accentColor) }
                }

                if (bug.version.isNotBlank()) {
                    DetailInfoItem(label = "VERSION", value = bug.version, color = accentColor)
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("DESCRIPTION", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (bug.description.isNotBlank()) bug.description else "No description provided.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )

                if (bug.stepsToReproduce.isNotBlank()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("STEPS TO REPRODUCE", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(16.dp)) {
                        Text(
                            text = bug.stepsToReproduce,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun BugAddEditSection(
    bug: BugEntity? = null,
    projectId: String,
    viewModel: WorkspaceViewModel,
    onBack: () -> Unit
) {
    val style = LocalAppStyle.current
    
    var title by remember(bug) { mutableStateOf(bug?.title ?: "") }
    var description by remember(bug) { mutableStateOf(bug?.description ?: "") }
    var severity by remember(bug) { mutableStateOf(bug?.severity ?: "Medium") }
    var priority by remember(bug) { mutableIntStateOf(bug?.priority ?: 1) }
    var env by remember(bug) { mutableStateOf(bug?.environment ?: "Production") }
    var version by remember(bug) { mutableStateOf(bug?.version ?: "") }
    var deadline by remember(bug) { mutableStateOf(bug?.deadline) }
    var steps by remember(bug) { mutableStateOf(bug?.stepsToReproduce ?: "") }

    val context = androidx.compose.ui.platform.LocalContext.current

    val dynamicAccentColor = when (severity) {
        "Critical" -> Color.Red
        "High" -> Color(0xFFFF5252)
        "Medium" -> Color(0xFFFFB800)
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
                        val updated = bug?.copy(
                            title = title,
                            description = description,
                            severity = severity,
                            priority = priority,
                            environment = env,
                            version = version,
                            deadline = deadline,
                            stepsToReproduce = steps
                        ) ?: BugEntity(
                            projectId = projectId,
                            title = title,
                            description = description,
                            severity = severity,
                            priority = priority,
                            environment = env,
                            version = version,
                            deadline = deadline,
                            stepsToReproduce = steps
                        )
                        if (bug == null) viewModel.addBug(title, projectId, description, severity, priority, env, version, steps, deadline)
                        else viewModel.updateBug(updated)
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
                        if (title.isEmpty()) { Text("Bug Summary", color = Color.White.copy(alpha = 0.2f), fontSize = 32.sp, fontWeight = FontWeight.Black) }
                        innerTextField()
                    }
                )
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("SEVERITY", color = dynamicAccentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Low", "Medium", "High", "Critical").forEach { s ->
                        val isSel = severity == s
                        val color = when(s) { "Critical" -> Color.Red; "High" -> Color(0xFFFF5252); "Medium" -> Color(0xFFFFB800); else -> Color(0xFF2EC4B6) }
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSel) color else color.copy(alpha = 0.1f)).clickable { severity = s }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(s.uppercase(), color = if (isSel) Color.White else color, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("ENVIRONMENT", color = dynamicAccentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Dev", "QA", "Staging", "Prod").forEach { e ->
                        val isSel = env == e
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSel) dynamicAccentColor else dynamicAccentColor.copy(alpha = 0.1f)).clickable { env = e }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(e.uppercase(), color = if (isSel) Color.Black else dynamicAccentColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("DESCRIPTION", color = dynamicAccentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                BasicTextField(
                    value = description,
                    onValueChange = { description = it },
                    textStyle = TextStyle(color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, lineHeight = 24.sp),
                    cursorBrush = SolidColor(dynamicAccentColor),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    decorationBox = { innerTextField ->
                        if (description.isEmpty()) { Text("Briefly explain the issue...", color = Color.White.copy(alpha = 0.2f), fontSize = 16.sp) }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text("STEPS TO REPRODUCE", color = dynamicAccentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                BasicTextField(
                    value = steps,
                    onValueChange = { steps = it },
                    textStyle = TextStyle(color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, lineHeight = 20.sp),
                    cursorBrush = SolidColor(dynamicAccentColor),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(16.dp),
                    decorationBox = { innerTextField ->
                        if (steps.isEmpty()) { Text("1. Go to...\n2. Click on...\n3. Observe...", color = Color.White.copy(alpha = 0.2f), fontSize = 14.sp) }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = version,
                    onValueChange = { version = it },
                    label = { Text("App Version") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = dynamicAccentColor, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text("DEADLINE / FIX DATE", color = dynamicAccentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
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
                                "Set target fix date..."
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

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
