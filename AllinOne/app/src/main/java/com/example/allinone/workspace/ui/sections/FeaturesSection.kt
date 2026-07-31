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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle
import com.example.allinone.workspace.data.FeatureEntity
import com.example.allinone.workspace.data.TaskEntity
import com.example.allinone.workspace.ui.WorkspaceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FeatureViewSection(
    features: List<FeatureEntity>,
    tasks: List<TaskEntity>,
    viewModel: WorkspaceViewModel,
    onViewFeature: (FeatureEntity) -> Unit,
    onEditFeature: (FeatureEntity) -> Unit,
    onDeleteFeature: (FeatureEntity) -> Unit
) {
    val style = LocalAppStyle.current
    val statuses = listOf("Backlog", "Planning", "Development", "Testing", "Shipped")
    val pagerState = rememberPagerState(pageCount = { statuses.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val shipped = features.count { it.status == "Shipped" }
            val total = features.size
            Box(modifier = Modifier.weight(1f)) { MetricCard("In Progress", features.count { it.status == "Development" }.toString(), style.accentColor) }
            Box(modifier = Modifier.weight(1f)) { MetricCard("Shipped", "$shipped / $total", Color(0xFF2EC4B6)) }
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
            val statusFeatures = features.filter { it.status == status }
            
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
                if (statusFeatures.isEmpty()) {
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
                                imageVector = if (status == "Shipped") Icons.Default.RocketLaunch else Icons.Default.Category,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.05f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No features in $status", color = Color.White.copy(alpha = 0.1f), fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(statusFeatures, key = { it.id }) { feature ->
                            Box(modifier = Modifier.animateItem()) {
                                FeatureItemCard(
                                    feature = feature,
                                    linkedTasks = tasks.filter { it.milestoneId == feature.id },
                                    onUpdate = { viewModel.updateFeature(it) },
                                    onViewFeature = onViewFeature,
                                    onEditFeature = onEditFeature,
                                    onDeleteFeature = onDeleteFeature,
                                    onQuickTasks = { viewModel.quickTasks(it) }
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
fun FeatureDetailSection(
    feature: FeatureEntity,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val style = LocalAppStyle.current
    val projectColorHex = com.example.allinone.DataManager.globalProjectColor
    val accentColor = if (projectColorHex != -1) Color(projectColorHex) else style.accentColor

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
                Text(text = feature.title, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(24.dp))
                
                DetailInfoItem(label = "STATUS", value = feature.status.uppercase(), color = accentColor)
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) { DetailInfoItem(label = "COMPLEXITY", value = feature.complexity, color = accentColor) }
                    Box(modifier = Modifier.weight(1f)) { DetailInfoItem(label = "EFFORT", value = feature.effortSize, color = accentColor) }
                }

                if (feature.targetVersion.isNotBlank()) {
                    DetailInfoItem(label = "TARGET VERSION", value = feature.targetVersion, color = accentColor)
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("DESCRIPTION", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (feature.description.isNotBlank()) feature.description else "No description provided.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )

                if (feature.requirements.isNotBlank()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("REQUIREMENTS", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(16.dp)) {
                        Text(
                            text = feature.requirements,
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
fun FeatureAddEditSection(
    feature: FeatureEntity? = null,
    projectId: String,
    viewModel: WorkspaceViewModel,
    onBack: () -> Unit
) {
    val style = LocalAppStyle.current
    val projectColorHex = com.example.allinone.DataManager.globalProjectColor
    val projectColor = if (projectColorHex != -1) Color(projectColorHex) else style.accentColor

    var title by remember(feature) { mutableStateOf(feature?.title ?: "") }
    var description by remember(feature) { mutableStateOf(feature?.description ?: "") }
    var complexity by remember(feature) { mutableStateOf(feature?.complexity ?: "Medium") }
    var effort by remember(feature) { mutableStateOf(feature?.effortSize ?: "M") }
    var requirements by remember(feature) { mutableStateOf(feature?.requirements ?: "") }
    var version by remember(feature) { mutableStateOf(feature?.targetVersion ?: "") }
    var status by remember(feature) { mutableStateOf(feature?.status ?: "Backlog") }
    var deadline by remember(feature) { mutableStateOf(feature?.deadline) }

    val context = androidx.compose.ui.platform.LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(style.backgroundColor)) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(projectColor.copy(alpha = 0.15f)))
        
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().imePadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                TextButton(
                    onClick = {
                        val updated = feature?.copy(
                            title = title,
                            description = description,
                            complexity = complexity,
                            effortSize = effort,
                            requirements = requirements,
                            targetVersion = version,
                            status = status,
                            deadline = deadline
                        ) ?: FeatureEntity(
                            projectId = projectId,
                            title = title,
                            description = description,
                            complexity = complexity,
                            effortSize = effort,
                            requirements = requirements,
                            targetVersion = version,
                            status = status,
                            deadline = deadline
                        )
                        if (feature == null) viewModel.addFeature(title, projectId, description, complexity, effort, requirements, version, status, deadline)
                        else viewModel.updateFeature(updated)
                        onBack()
                    },
                    enabled = title.isNotBlank()
                ) {
                    Text("SAVE", color = projectColor, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp)) {
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black),
                    cursorBrush = SolidColor(projectColor),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (title.isEmpty()) { Text("Feature Title", color = Color.White.copy(alpha = 0.2f), fontSize = 32.sp, fontWeight = FontWeight.Black) }
                        innerTextField()
                    }
                )
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("STATUS", color = projectColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Backlog", "Planning", "Development", "Testing", "Shipped").forEach { s ->
                        val isSel = status == s
                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (isSel) projectColor else projectColor.copy(alpha = 0.1f)).clickable { status = s }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text(s.uppercase(), color = if (isSel) Color.Black else projectColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("COMPLEXITY", color = projectColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Easy", "Medium", "Hard").forEach { c ->
                                val isSel = complexity == c
                                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(4.dp)).background(if (isSel) projectColor else projectColor.copy(alpha = 0.1f)).clickable { complexity = c }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(c.take(1), color = if (isSel) Color.Black else projectColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("EFFORT", color = projectColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("S", "M", "L", "XL").forEach { e ->
                                val isSel = effort == e
                                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(4.dp)).background(if (isSel) projectColor else projectColor.copy(alpha = 0.1f)).clickable { effort = e }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(e, color = if (isSel) Color.Black else projectColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("DESCRIPTION", color = projectColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                BasicTextField(
                    value = description,
                    onValueChange = { description = it },
                    textStyle = TextStyle(color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, lineHeight = 24.sp),
                    cursorBrush = SolidColor(projectColor),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    decorationBox = { innerTextField ->
                        if (description.isEmpty()) { Text("High level goal of this feature...", color = Color.White.copy(alpha = 0.2f), fontSize = 16.sp) }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text("REQUIREMENTS / TECH NOTES", color = projectColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                BasicTextField(
                    value = requirements,
                    onValueChange = { requirements = it },
                    textStyle = TextStyle(color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, lineHeight = 20.sp),
                    cursorBrush = SolidColor(projectColor),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(16.dp),
                    decorationBox = { innerTextField ->
                        if (requirements.isEmpty()) { Text("Add specific requirements or technical implementation notes...", color = Color.White.copy(alpha = 0.2f), fontSize = 14.sp) }
                        innerTextField()
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = version,
                    onValueChange = { version = it },
                    label = { Text("Target Version (e.g. v1.0)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = projectColor, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text("DEADLINE / RELEASE DATE", color = projectColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
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
                            tint = if (deadline != null) projectColor else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (deadline != null) {
                                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(deadline!!))
                            } else {
                                "Set target release date..."
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
