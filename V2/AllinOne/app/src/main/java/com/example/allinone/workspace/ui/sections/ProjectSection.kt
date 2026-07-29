package com.example.allinone.workspace.ui.sections

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle
import com.example.allinone.workspace.data.ProjectEntity
import com.example.allinone.workspace.ui.WorkspaceViewModel

@Composable
fun ProjectAddEditSection(
    project: ProjectEntity? = null,
    viewModel: WorkspaceViewModel,
    onBack: () -> Unit
) {
    val style = LocalAppStyle.current
    val context = LocalContext.current
    
    var title by remember(project) { mutableStateOf(project?.name ?: "") }
    var description by remember(project) { mutableStateOf(project?.description ?: "") }
    var colorInt by remember(project) { mutableIntStateOf(project?.color ?: -1) }
    var status by remember(project) { mutableStateOf(project?.status ?: "Active") }
    var health by remember(project) { mutableStateOf(project?.health ?: "Healthy") }
    
    val dynamicAccentColor = if (colorInt != -1) Color(colorInt) else style.accentColor

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
                        if (status == "Completed" && project != null) {
                            val (canComplete, message) = viewModel.canCompleteProject(project.id)
                            if (!canComplete) {
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                return@TextButton
                            }
                        }

                        if (project == null) {
                            viewModel.addProject(title, description, colorInt, "")
                        } else {
                            viewModel.updateProject(project.copy(name = title, description = description, color = colorInt, status = status, health = health))
                        }
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
                        if (title.isEmpty()) { Text("Project Name", color = Color.White.copy(alpha = 0.2f), fontSize = 32.sp, fontWeight = FontWeight.Black) }
                        innerTextField()
                    }
                )
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("THEME COLOR", color = dynamicAccentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(16.dp))
                val colors = listOf(0xFFFF7A59, 0xFFFFB800, 0xFF2EC4B6, 0xFF3A86F0, 0xFF1A73E8, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF4CAF50)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.SpaceEvenly) {
                    colors.forEach { c ->
                        val isSel = colorInt == c.toInt()
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(c)).border(if (isSel) 3.dp else 0.dp, Color.White, CircleShape).clickable { colorInt = c.toInt() })
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("STATUS", color = dynamicAccentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Active", "Completed", "Archived").forEach { s ->
                        val isSel = status == s
                        Surface(
                            onClick = {
                                if (s == "Completed" && project != null) {
                                    val (canComplete, message) = viewModel.canCompleteProject(project.id)
                                    if (!canComplete) {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    } else {
                                        status = s
                                    }
                                } else {
                                    status = s
                                }
                            },
                            color = if (isSel) dynamicAccentColor else Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(s, color = if (isSel) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("HEALTH", color = dynamicAccentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Healthy", "At Risk", "Delayed").forEach { h ->
                        val isSel = health == h
                        val healthColor = when(h) {
                            "Healthy" -> Color(0xFF2EC4B6)
                            "At Risk" -> Color(0xFFFFB800)
                            else -> Color.Red
                        }
                        Surface(
                            onClick = { health = h },
                            color = if (isSel) healthColor else Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(h, color = if (isSel) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            Text("What is the high-level objective of this workspace?", color = Color.White.copy(alpha = 0.2f), fontSize = 16.sp)
                        }
                        innerTextField()
                    }
                )
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
