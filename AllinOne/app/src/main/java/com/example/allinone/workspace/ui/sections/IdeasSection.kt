@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.example.allinone.workspace.ui.sections

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
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
import com.example.allinone.workspace.data.IdeaEntity
import com.example.allinone.workspace.ui.WorkspaceViewModel

@Composable
fun IdeaViewSection(
    ideas: List<IdeaEntity>,
    onConvert: (IdeaEntity) -> Unit,
    onViewIdea: (IdeaEntity) -> Unit,
    onEditIdea: (IdeaEntity) -> Unit,
    onDeleteIdea: (IdeaEntity) -> Unit
) {
    val style = LocalAppStyle.current
    val ideaAccentColor = Color(0xFF2EC4B6)
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        items(ideas, key = { it.id }) { idea ->
            var showMenu by remember { mutableStateOf(false) }
            Box(modifier = Modifier.animateItem()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .combinedClickable(
                            onClick = { onViewIdea(idea) },
                            onLongClick = { showMenu = true }
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.5.dp, ideaAccentColor)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        Row(modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Vertical Accent Bar
                            Box(modifier = Modifier.width(4.dp).fillMaxHeight().clip(CircleShape).background(ideaAccentColor.copy(alpha = 0.8f)))
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    idea.title, 
                                    color = ideaAccentColor, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 20.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (idea.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = idea.description,
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 14.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (idea.status != "Converted") { 
                                Button(onClick = { onConvert(idea) }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), modifier = Modifier.height(32.dp)) { 
                                    Text("GRADUATE", fontSize = 10.sp, fontWeight = FontWeight.Black) 
                                } 
                            }
                        }
                        CreatedAtText(
                            timestamp = idea.createdAt,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                        )
                    }
                }
                WorkspaceDropdown(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    WorkspaceDropdownItem(
                        text = "View Details",
                        onClick = { onViewIdea(idea); showMenu = false },
                        icon = Icons.Default.Description
                    )
                    WorkspaceDropdownItem(
                        text = "Edit",
                        onClick = { onEditIdea(idea); showMenu = false },
                        icon = Icons.Default.Edit
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.1f))
                    WorkspaceDropdownItem(
                        text = "Delete",
                        onClick = { onDeleteIdea(idea); showMenu = false },
                        icon = Icons.Default.Delete,
                        isDestructive = true
                    )
                }
            }
        }
    }
}

@Composable
fun IdeaDetailSection(
    idea: IdeaEntity,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val style = LocalAppStyle.current
    val accentColor = Color(0xFF2EC4B6)

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
                Text(text = idea.title, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) { DetailInfoItem(label = "IMPACT", value = "${idea.impact}/5", color = accentColor) }
                    Box(modifier = Modifier.weight(1f)) { DetailInfoItem(label = "DIFFICULTY", value = "${idea.difficulty}/5", color = accentColor) }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("VISION / DESCRIPTION", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (idea.description.isNotBlank()) idea.description else "No vision described yet.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun IdeaAddEditSection(
    idea: IdeaEntity? = null,
    projectId: String,
    viewModel: WorkspaceViewModel,
    onBack: () -> Unit
) {
    val style = LocalAppStyle.current
    val accentColor = Color(0xFF2EC4B6)

    var title by remember(idea) { mutableStateOf(idea?.title ?: "") }
    var description by remember(idea) { mutableStateOf(idea?.description ?: "") }
    var impact by remember(idea) { mutableFloatStateOf(idea?.impact?.toFloat() ?: 3f) }
    var difficulty by remember(idea) { mutableFloatStateOf(idea?.difficulty?.toFloat() ?: 3f) }

    Box(modifier = Modifier.fillMaxSize().background(style.backgroundColor)) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(accentColor.copy(alpha = 0.15f)))
        
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().imePadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                TextButton(
                    onClick = {
                        val updated = idea?.copy(title = title, description = description, impact = impact.toInt(), difficulty = difficulty.toInt())
                            ?: IdeaEntity(projectId = projectId, title = title, description = description, impact = impact.toInt(), difficulty = difficulty.toInt())
                        
                        if (idea == null) viewModel.addIdea(title, projectId, description, impact.toInt(), difficulty.toInt())
                        else viewModel.updateIdea(updated)
                        onBack()
                    },
                    enabled = title.isNotBlank()
                ) {
                    Text("SAVE", color = accentColor, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp)) {
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black),
                    cursorBrush = SolidColor(accentColor),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (title.isEmpty()) { Text("Idea Summary", color = Color.White.copy(alpha = 0.2f), fontSize = 32.sp, fontWeight = FontWeight.Black) }
                        innerTextField()
                    }
                )
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("IMPACT", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Slider(value = impact, onValueChange = { impact = it }, valueRange = 1f..5f, steps = 3, colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor))
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("DIFFICULTY", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Slider(value = difficulty, onValueChange = { difficulty = it }, valueRange = 1f..5f, steps = 3, colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor))

                Spacer(modifier = Modifier.height(24.dp))
                Text("DESCRIPTION", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                BasicTextField(
                    value = description,
                    onValueChange = { description = it },
                    textStyle = TextStyle(color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, lineHeight = 24.sp),
                    cursorBrush = SolidColor(accentColor),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                    decorationBox = { innerTextField ->
                        if (description.isEmpty()) {
                            Text("What's the vision for this idea?", color = Color.White.copy(alpha = 0.2f), fontSize = 16.sp)
                        }
                        innerTextField()
                    }
                )
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
